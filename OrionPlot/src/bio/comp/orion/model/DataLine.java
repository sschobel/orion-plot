package bio.comp.orion.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;


public class DataLine implements Iterable<List<Integer>>{
	
	public static class DataPoint{
		public List<Integer> _values;
		public List<Integer> getValues(){
			return _values;
		}
		public DataPoint(){
			_values=new ArrayList<Integer>();
		}
		
		public DataPoint(int x){
			_values=new ArrayList<Integer>();
			_values.add(x);
		}
		public DataPoint(int x, int y){
			_values=new ArrayList<Integer>();
			_values.add(x);
			_values.add(y);
		}
		public void addValue(int val){
			_values.add(val);
		}
		public int numValues(){
			return _values.size();
		}
		public Integer valueAt(int index){
			return index > _values.size() ? _values.get(index) : null;
		}
		public String toString(){
			StringBuilder sb = new StringBuilder();
			Iterator<Integer> vi = _values.iterator();
			if(vi.hasNext()){
				sb.append(vi.next());
				
			}
			while(vi.hasNext()){
				sb.append(",");
				sb.append(vi.next());
			}
			return sb.toString();
		}
	}
	
	public String _title;
	private Color _titleColor;
	private List<DataPoint> _data;
	public DataLine(String title, int numPoints){
		_title = parseTitle(title);
		_data = new ArrayList<DataPoint>(numPoints);
		reserveDataPointsToIndex(numPoints - 1);
	};
	
	private String parseTitle(String unparsed){
		StringTokenizer st = new StringTokenizer(unparsed, "#");
		String parsed = st.nextToken();
		if(st.hasMoreTokens()){
			String colorToken=st.nextToken();
			int parsedColor = 0;
			try{
				parsedColor = Integer.parseInt(colorToken, 16);
			}catch(NumberFormatException nfe){
				nfe.printStackTrace();
				parsedColor = 0;
			}
			_titleColor = new Color(parsedColor);
		}
		return parsed;
	}
	
	private void reserveDataPointsToIndex(int index){
		while(index >= _data.size()){
			_data.add(new DataPoint());
		}
	}
	
	public void addValueAt(int index, int value){
		reserveDataPointsToIndex(index);
		DataPoint aPoint = _data.get(index) ;
		if(aPoint == null){
			aPoint = new DataPoint(value);
		}
		else{
			aPoint.addValue(value);
		}
	}
	
	public String getTitle(){
		return _title;
	}
	
	public Color getTitleColor(){
		return _titleColor;
	}
	
	public List<Integer> getValuesAt(int index){
		return index < _data.size() && _data.get(index) != null ? _data.get(index).getValues() : new ArrayList<Integer>();
	}
	
	public int getLength() {
		// TODO Auto-generated method stub
		return _data != null ? _data.size() : 0;
	}
	
	public String toFileString(){
		StringBuilder sb= new StringBuilder();
		if(_title != null){
			sb.append(_title).append('\t');
		}
		if(_data != null){
			for(DataPoint i : _data){
				sb.append(i.toString()).append('\t');
			}
		}
		return sb.toString();
	}
	
	public String toString(){
		return toFileString();
	}

	@Override
	public Iterator<List<Integer>> iterator() {
		// TODO Auto-generated method stub
		return new Iterator<List<Integer>>(){
			int i = 0;

			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				return i < DataLine.this.getLength();
			}

			@Override
			public List<Integer> next() {
				// TODO Auto-generated method stub
				return DataLine.this.getValuesAt(i++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
	}

	
	
	
}
