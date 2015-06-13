package bio.comp.orion.model;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class MatrixReaders {

	private abstract static class BaseMatrixReader implements MatrixReader {
		private static final Logger errors = Logger.getLogger("errors");
		protected DataLine[] _matrix;
		protected List<MatrixHeader> _headers;
		protected Map<Integer, Color> _palette;
		protected File _file;

		protected BaseMatrixReader(File file) {
			_file = file;
			_palette = new HashMap<Integer, Color>();
			_headers = new ArrayList<MatrixHeader>();

		}

		public DataLine[] getMatrix() {
			if (_matrix == null) {
				_matrix = initMatrix();
			}
			return _matrix;
		}

		public MatrixHeader[] getHeaders() {
			if (_headers == null) {
				_headers = Arrays.asList(initHeaders());
			}
			return _headers.toArray(new MatrixHeader[0]);
		}

		@SuppressWarnings("unused")
		public String[] getHeaderNames() {

			MatrixHeader[] headers = getHeaders();

			int length = (headers != null) ? headers.length : 0;
			String[] headerNames = new String[length];
			for (int i = 0; i < length; ++i) {
				headerNames[i] = headers[i].getName();
			}
			return headerNames;
		}

		protected DataLine[] initMatrix() {
			return null;
		}

		protected MatrixHeader[] initHeaders() {
			return null;
		}

		protected Map<Integer, Color> getColorMap() {
			return _palette;
		}

		public final Set<Integer> getUniqueValuesAsSet() {
			HashSet<Integer> uniqs = new HashSet<Integer>();
			for (DataLine line : getMatrix()) {
				for (List<Integer> entry : line) {
					uniqs.addAll(entry);
				}
			}
			return uniqs;
		}

		@SuppressWarnings("unused")
		public final int[] getUniqueValues() {
			Set<Integer> uniqs = getUniqueValuesAsSet();
			Iterator<Integer> uiter = uniqs.iterator();
			int[] vals = new int[uniqs.size()];
			for (int i = 0; i < vals.length; ++i) {
				vals[i] = uiter.next();
			}
			return vals;
		}

		@Override
		public OrionModel getModel() {

			return new OrionModel.DefaultOrionModel(getMatrix(), getHeaders(),
					getColorMap());
		}

		private static final String COLUMN_PREFIX = "#headers:";
		private static final String PALETE_PREFIX = "#palette:";
		private static final String PALETE_PREFIX_ALT = "#pallete:";
		private static final String DECL_SEPARATOR = ";";

		protected DataLine readLineIntoMatrix(String line) {
			// System.out.println(String.format("Reading; %s", line));
			if (line.startsWith("#")) {
				if (line.startsWith(COLUMN_PREFIX)) {
					// read columns
					String colNameStr = line.substring(COLUMN_PREFIX.length());
					try {
						List<String> cols = Splitter.on(DECL_SEPARATOR)
								.omitEmptyStrings().trimResults()
								.splitToList(colNameStr);
						List<MatrixHeader> headerList = Lists.transform(cols,
								MatrixHeaders.fromString);
						_headers.addAll(headerList);
					} catch (IllegalArgumentException iae) {
						errors.log(Level.INFO,
								String.format(
										"Failed to parse column header %s",
										colNameStr), iae);
					}

				}
				if (line.startsWith(PALETE_PREFIX)
						|| line.startsWith(PALETE_PREFIX_ALT)) {
					String palleteDeclStr = line.substring(PALETE_PREFIX
							.length());
					Splitter declSplitter = Splitter.on("=").trimResults();
					try {
						Map<String, String> palleteMap = Splitter
								.on(DECL_SEPARATOR).omitEmptyStrings()
								.trimResults()
								.withKeyValueSeparator(declSplitter)
								.split(palleteDeclStr);
						for (Map.Entry<String, String> entry : palleteMap
								.entrySet()) {
							try {
								Integer lookup = Integer
										.valueOf(entry.getKey());
								Color color = Colors.fromHexString(entry
										.getValue());
								_palette.put(lookup, color);
							} catch (NumberFormatException nfe) {
								errors.log(
										Level.INFO,
										String.format(
												"Failed to convert value %s into Integer",
												entry.getKey()), nfe);
							} catch (RuntimeException re) {
								errors.log(
										Level.INFO,
										String.format(
												"Failed to convert value %s into Color",
												entry.getValue()), re);
							}
						}
					} catch (IllegalArgumentException iae) {
						errors.log(Level.INFO, String.format(
								"Failed to parse pallette %s", palleteDeclStr),
								iae);

					}

				}
				return null;
			}
			StringTokenizer st = new StringTokenizer(line);
			int numTokens = st.countTokens();
			if (numTokens == 0) {
				return null;
			}
			DataLine dl = new DataLine(st.nextToken(), numTokens);
			for (int j = 0; j < numTokens - 1; j++) {
				String tok = st.nextToken();
				// System.out.println(String.format("i %d of %d", j,
				// numTokens));
				if (tok != null && !tok.isEmpty()) {
					StringTokenizer st_comma = new StringTokenizer(tok, ",");
					while (st_comma.hasMoreTokens()) {
						String comma_tok = st_comma.nextToken();
						Integer value = Integer.parseInt(comma_tok);

						dl.addValueAt(j, value);
					}
				} else {
					dl.addValueAt(j, -1);
				}
			}
			return dl;
		}
	}

	private static class CSVMatrixReader extends BaseMatrixReader implements
			MatrixReader {

		protected DataLine[] readLinesIntoMatrix(List<String> lines) {
			List<DataLine> matrix = new ArrayList<DataLine>();
			if (!lines.isEmpty()) {

				for (int i = 0; i < lines.size(); ++i) {
					DataLine dl = readLineIntoMatrix(lines.get(i));
					if (dl != null) {
						matrix.add(dl);
					}
				}
			}
			return matrix.toArray(new DataLine[0]);
		}

		protected DataLine[] readFileIntoMatrix(File file,
				List<MatrixHeader> _headers) {
			ArrayList<String> lines = new ArrayList<String>();
			BufferedReader br = null;
			try {
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);
				String line = null;
				while ((line = br.readLine()) != null) {
					lines.add(line);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			return readLinesIntoMatrix(lines);

		}

		protected CSVMatrixReader(File file) {
			super(file);
			_matrix = readFileIntoMatrix(file, _headers);
		}

	}

	private static class JSONMatrixReader extends BaseMatrixReader implements
			MatrixReader {
		private JSONObject _jsonObject;

		protected JSONMatrixReader(File file) {
			super(file);
		}

		protected JSONObject getJSONObject() {
			if (_jsonObject == null) {
				try {
					_jsonObject = new JSONObject(new JSONTokener(
							new FileReader(_file)));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return _jsonObject;
		}

		private DataLine parseBodyObject(JSONObject bodyObject) {
			DataLine result = null;
			if (bodyObject != null) {
				String raw = bodyObject.getString("raw");
				if (raw != null) {
					result = readLineIntoMatrix(String.format("%s"));
				}
			}
			return result;
		}

		private MatrixHeader parseHeadObject(JSONObject headObject) {
			MatrixHeader result = null;
			if (headObject != null) {

			}
			return result;
		}

		private DataLine[] parseBody(JSONArray bodyArray) {
			if (bodyArray == null) {
				return new DataLine[0];
			}
			DataLine[] body = new DataLine[bodyArray.length()];
			for (int i = 0; i < bodyArray.length(); ++i) {
				DataLine dl = parseBodyObject(bodyArray.getJSONObject(i));
				if (dl != null) {
					body[i] = dl;
				}
			}

			return body;

		}

		private MatrixHeader[] parseHead(JSONArray headArray) {
			if (headArray == null) {
				return new MatrixHeader[0];
			}
			MatrixHeader[] head = new MatrixHeader[headArray.length()];
			for (int i = 0; i < headArray.length(); ++i) {
				head[i] = parseHeadObject(headArray.getJSONObject(i));
			}
			return head;
		}

		protected DataLine[] initMatrix() {
			JSONObject obj = getJSONObject();
			if (obj == null) {
				return null;
			}
			return parseBody(obj.getJSONArray("body"));
		}

		protected MatrixHeader[] initHeaders() {
			JSONObject obj = getJSONObject();
			if (obj == null) {
				return null;
			}
			return parseHead(obj.getJSONArray("head"));
		}
	}

	private static class NULLMatrixReader extends BaseMatrixReader implements
			MatrixReader {
		protected NULLMatrixReader(File file) {
			super(file);
		}

		protected DataLine[] initMatrix() {
			return new DataLine[0];
		}

		protected MatrixHeader[] initHeaders() {
			return new MatrixHeader[0];
		}

		@Override
		public OrionModel getModel() {

			return new OrionModel.DefaultOrionModel();
		}
	}

	public static final MatrixReader readerForFile(String filePath) {
		return readerForFile(new File(filePath));
	}

	public static final MatrixReader readerForFile(File file) {
		String path = file.getAbsolutePath();
		if (path.endsWith(".csv")) {
			return new CSVMatrixReader(file);
		} else if (path.endsWith(".json")) {
			return new JSONMatrixReader(file);
		} else {
			return new NULLMatrixReader(file);
		}
	}
}
