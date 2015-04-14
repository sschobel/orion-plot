package bio.comp.orion.model;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public interface OrionModel extends Iterable<DataLine> {
	public int getDataMatrixEntryCount();
	public DataLine getDataMatrixEntry(int idx);
	public Color colorForValue(int matrixIndex, int cellIndex, int subIndex, int value); 
	public Color colorForValue(int cellValue);
	public int getHeaderCount();
	public String getHeader(int index);
	public DataLine[] getDataMatrix();
	public SubCellFalseColorCoder getColorCoder();
	public void setColorCoder(SubCellFalseColorCoder colorCoder);
	public Set<Integer> getMatrixUniques();

	
	public static class DefaultOrionModel implements OrionModel{
		DataLine[] _matrix;
		MatrixHeader[] _headers;
		SubCellFalseColorCoder _colorLookup;
		public DefaultOrionModel(DataLine[] matrix, MatrixHeader[] headers, SubCellFalseColorCoder colorCoder){
			_headers = headers;
			_matrix = matrix;
			_colorLookup = colorCoder;
		}
		public DefaultOrionModel(){
			this(null, null, null);
		}
		
		@Override
		public Set<Integer> getMatrixUniques(){
			HashSet<Integer> uniqs = new HashSet<Integer>();
			for(DataLine dl : _matrix){
				for(int i = 0; i < dl.getLength(); ++i){
					List<Integer> entryValues = dl.getValuesAt(i);
					uniqs.addAll(entryValues);
				}
			}
			return uniqs;
		}
		
		@Override
		public int getDataMatrixEntryCount() {
			// TODO Auto-generated method stub
			return _matrix != null ? _matrix.length : 0;
		}
		@Override
		public DataLine getDataMatrixEntry(int idx) {
			// TODO Auto-generated method stub
			return _matrix != null && idx < _matrix.length ? 
					_matrix[idx] : null;
		}
		public DataLine[] getDataMatrix(){
			return _matrix;
		}
		@Override
		public Color colorForValue(int value) {
			// TODO Auto-generated method stub
			return colorForValue(-1,-1,-1,value);
		}
		@Override
		public int getHeaderCount() {
			// TODO Auto-generated method stub
			return _headers != null ? _headers.length : 0;
		}
		@Override
		public String getHeader(int index) {
			// TODO Auto-generated method stub
			return _headers != null && index < _headers.length ? _headers[index].getLineName() : "";
		}
		@Override
		public Color colorForValue(int matrixIndex, int cellIndex,
				int subIndex, int value) {
			// TODO Auto-generated method stub
			return _colorLookup != null ? _colorLookup.colorForSubCell(matrixIndex, cellIndex, subIndex, value) : Color.black;
		}
		@Override
		public Iterator<DataLine> iterator() {
			// TODO Auto-generated method stub
			return new Iterator<DataLine>(){
				private int current;
				{
					current = 0;
				}

				@Override
				public boolean hasNext() {
					// TODO Auto-generated method stub
					return _matrix != null && current < _matrix.length;
				}

				@Override
				public DataLine next() {
					// TODO Auto-generated method stub
					DataLine line = hasNext() ? _matrix[current++] : null;
					return line;
				}

				@Override
				public void remove() {
					// TODO Auto-generated method stub
					throw new UnsupportedOperationException();
				}
				
			};
		}
		@Override
		public SubCellFalseColorCoder getColorCoder() {
			// TODO Auto-generated method stub
			return _colorLookup;
		}
		@Override
		public void setColorCoder(SubCellFalseColorCoder colorCoder) {
			// TODO Auto-generated method stub
			_colorLookup = colorCoder;
		}
	}
}