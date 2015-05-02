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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.sun.tools.javac.code.Attribute.Array;

public class MatrixReaders {
	
	
	private abstract static class BaseMatrixReader implements MatrixReader{
		protected DataLine[] _matrix;
		protected MatrixHeader[] _headers;
		protected File _file;
		protected BaseMatrixReader(File file){
			_file = file;
		}
		public DataLine[] getMatrix(){
			if(_matrix == null){
				_matrix = initMatrix();
			}
			return _matrix;
		}
		public MatrixHeader[] getHeaders(){
			if(_headers == null){
				_headers = initHeaders();
			}
			return _headers;
		}
		public String[] getHeaderNames() {
			// TODO Auto-generated method stub
			MatrixHeader[] headers = getHeaders();
			
			int length = (headers != null) ? headers.length : 0;
			String[] headerNames = new String[length];
			for (int i = 0; i < length; ++i){
				headerNames[i] = headers[i].getLineName();
			}
			return headerNames;
		}
		protected DataLine[] initMatrix(){
			return null;
		}
		protected MatrixHeader[] initHeaders(){
			return null;
		}
		protected Map<Integer, Color> getColorMap(){
			return null;
		}
		
		protected final Set<Integer> getUniqueValuesAsSet(){
			HashSet<Integer> uniqs = new HashSet<Integer>();
			for(DataLine line : getMatrix()){
				for(List<Integer> entry: line){
					uniqs.addAll(entry);
				}
			}
			return uniqs;
		}
		protected int[] getUniqueValues(){
			Set<Integer> uniqs = getUniqueValuesAsSet();
			Iterator<Integer> uiter = uniqs.iterator();
			int[] vals = new int[uniqs.size()];
			for(int i = 0; i < vals.length; ++i){
				vals[i] = uiter.next();
			}
			return vals;
		}

		@Override
		public OrionModel getModel() {
			// TODO Auto-generated method stub
			Map<Integer, Color> map = getColorMap();
			SubCellFalseColorCoder coder = null;
			if(map == null){
				
				coder = new SubCellFalseColorCoder.UsingDistinctColorsForValues(getUniqueValues());
			}
			else{
				coder = new SubCellFalseColorCoder.UsingColorMap(map);
			}
			return new OrionModel.DefaultOrionModel(getMatrix(), getHeaders(), coder);
		}

	}
	static DataLine readLineIntoMatrix(String line){
		//System.out.println(String.format("Reading; %s", line));
		StringTokenizer st = new StringTokenizer(line);
		int numTokens = st.countTokens();
		if(numTokens == 0){
			return null;
		}
		DataLine dl = new DataLine(st.nextToken(), numTokens);
		for(int j = 0; j < numTokens - 1; j++){
			String tok = st.nextToken();
			//System.out.println(String.format("i %d of %d", j, numTokens));
			if(tok != null && !tok.isEmpty()){
				StringTokenizer st_comma = new StringTokenizer(tok, ",");
				while(st_comma.hasMoreTokens()){
					String comma_tok = st_comma.nextToken();
                    Integer value = Integer.parseInt(comma_tok);
                     
					dl.addValueAt(j, value); 
				}
			}
			else{
				dl.addValueAt(j, -1);
			}
		}
		return dl;
	}
	private static class CSVMatrixReader extends BaseMatrixReader implements MatrixReader{
		


	static private DataLine[] readLinesIntoMatrix(List<String> lines){
		DataLine [] matrix = new DataLine[lines.size()];
		if(!lines.isEmpty()){

			for(int i = 0; i < lines.size(); ++i){
				matrix[i] = readLineIntoMatrix(lines.get(i));

			}
		}
		return matrix;
	}

	static private MatrixHeader[] tryReadingHeadersInLine(String line){
		MatrixHeader[] headers = null;
		return headers;
	}


	

	static private DataLine[] readFileIntoMatrix(File file, MatrixHeader[] _headers){
		ArrayList<String> lines = new ArrayList<String>();
		BufferedReader br = null;
		try {
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line = null;
			while((line = br.readLine()) != null){
				lines.add(line);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		MatrixHeader[] headers = tryReadingHeadersInLine(lines.get(0));
		if(headers != null){
			lines.remove(0);
			_headers = headers;
		}
		return readLinesIntoMatrix(lines);

	}
		
		protected CSVMatrixReader(File file){
			super(file);
			_matrix = readFileIntoMatrix(file, _headers);
		}
		
		
	}
	
	private static class JSONMatrixReader extends BaseMatrixReader implements MatrixReader{
		private JSONObject _jsonObject;
		protected JSONMatrixReader(File file){
			super(file);
		}
		protected JSONObject getJSONObject(){
			if(_jsonObject == null){
                try {
					_jsonObject = new JSONObject(new JSONTokener(new FileReader(_file)));
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
		
		private DataLine parseBodyObject(JSONObject bodyObject){
			DataLine result = null;
			if(bodyObject != null){
				String raw = bodyObject.getString("raw");
				if(raw != null){
					result = readLineIntoMatrix(String.format("%s"));
				}
			}
			return result;
		}
		
		private MatrixHeader parseHeadObject(JSONObject headObject){
			MatrixHeader result = null;
			if(headObject != null){
				
			}
			return result;
		}
		
		private DataLine[] parseBody(JSONArray bodyArray){
			if(bodyArray == null){
				return new DataLine[0];
			}
			DataLine[] body = new DataLine[bodyArray.length()];
			for(int i = 0; i < bodyArray.length(); ++i){
				body[i] = parseBodyObject(bodyArray.getJSONObject(i));
			}
			return body;
			
		}
		
		private MatrixHeader[] parseHead(JSONArray headArray){
			if(headArray == null){
				return new MatrixHeader[0];
			}
			MatrixHeader[]head = new MatrixHeader[headArray.length()];
			for(int i = 0; i < headArray.length(); ++i){
				head[i] = parseHeadObject(headArray.getJSONObject(i));
			}
			return head;
		}
		protected DataLine[] initMatrix(){
                JSONObject obj = getJSONObject();
                if(obj == null){
                	return null;
                }
                return parseBody(obj.getJSONArray("body"));
		}
		protected MatrixHeader[] initHeaders(){
				JSONObject obj = getJSONObject();
				if(obj == null){
					return null;
				}
				return parseHead(obj.getJSONArray("head"));
		}
	}
	private static class NULLMatrixReader extends BaseMatrixReader implements MatrixReader{
		protected NULLMatrixReader(File file){
			super(file);
		}
		protected DataLine[] initMatrix(){
			return new DataLine[0];
		}
		protected MatrixHeader[] initHeaders(){
			return new MatrixHeader[0];
		}
		@Override
		public OrionModel getModel() {
			// TODO Auto-generated method stub
			return new OrionModel.DefaultOrionModel();
		}
	}

	public static final MatrixReader readerForFile(String filePath){
		return readerForFile(new File(filePath));
	}
	public static final MatrixReader readerForFile(File file){
		String path = file.getAbsolutePath();
		if(path.endsWith(".csv")){
			return new CSVMatrixReader(file);
		}
		else if (path.endsWith(".json")){
			return new JSONMatrixReader(file);
		}
		else{
			return new NULLMatrixReader(file);
		}
	}
}
