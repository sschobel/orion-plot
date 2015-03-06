package bio.comp.orion.model;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class MatrixReaders {
	
	
	private abstract static class BaseMatrixReader implements MatrixReader{
		private DataLine[] _matrix;
		private MatrixHeader[] _headers;
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
		@Override
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
		protected abstract DataLine[] initMatrix();
		protected abstract MatrixHeader[] initHeaders();
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
		private DataLine[] _csvMatrix;
		private MatrixHeader[] _csvHeaders;
		protected CSVMatrixReader(File file){
			super(file);
		}
		protected DataLine[] initMatrix(){
			if(_csvMatrix == null){
				_csvMatrix = readFileIntoMatrix(_file, _csvHeaders);
			}
			return _csvMatrix;
		}
		protected MatrixHeader[] initHeaders(){
			if(_csvHeaders == null){
				_csvMatrix = readFileIntoMatrix(_file, _csvHeaders);
			}
			return _csvHeaders;
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