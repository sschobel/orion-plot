package bio.comp.orion.model;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public interface OrionModel extends Iterable<DataLine> {
	public int getDataMatrixEntryCount();
	public DataLine getDataMatrixEntry(int idx);
	public Color colorForValue(int matrixIndex, int cellIndex, int subIndex, int value); 
	public Color colorForValue(int cellValue);
	public Map<Integer, Color>getColorMap();
	public int getHeaderCount();
	public String getHeader(int index);
	public MatrixHeader getMatrixHeader(int index);
	public DataLine[] getDataMatrix();
	public Set<Integer> getMatrixUniques();
	public int getMaxDataLineLength();
	public int getMaxDataLineRowIndex();

	
	public static class DefaultOrionModel implements OrionModel{
		DataLine[] _matrix;
		MatrixHeader[] _headers;
		Map<Integer, Color> _colorPalette = new HashMap<Integer, Color>();
		public DefaultOrionModel(DataLine[] matrix, MatrixHeader[] headers){
			this(matrix, headers, null);
		}
		public DefaultOrionModel(DataLine[] matrix, MatrixHeader[] headers, Map<Integer, Color>palette ){
			_headers = headers;
			_matrix = matrix;
			if(palette != null){
                _colorPalette.putAll(palette);
			}
			finalizeColorPalette();
		}
		public DefaultOrionModel(){
			this(null, null, null);
		}
		private void finalizeColorPalette(){
			HashMap<Integer, Color> defaultColors = new HashMap<Integer, Color>();
			Iterator<Color> colorGenerator = new SubCellFalseColorCoder.UniqueColorIterable().iterator();
			for(Integer uniq : getMatrixUniques()){
				if(!_colorPalette.containsKey(uniq)){
					defaultColors.put(uniq, colorGenerator.next());
				}
			}
			_colorPalette.putAll(defaultColors);
		}
		
		@Override
		public Set<Integer> getMatrixUniques() {
			HashSet<Integer> uniqs = new HashSet<Integer>();
			if (_matrix != null) {
				for (DataLine dl : _matrix) {
					for (int i = 0; i < dl.getLength(); ++i) {
						List<Integer> entryValues = dl.getValuesAt(i);
						uniqs.addAll(entryValues);
					}
				}
			}
			return uniqs;
		}

		@Override
		public int getDataMatrixEntryCount() {

			return _matrix != null ? _matrix.length : 0;
		}

		@Override
		public DataLine getDataMatrixEntry(int idx) {

			return _matrix != null && idx < _matrix.length ? _matrix[idx]
					: null;
		}

		public DataLine[] getDataMatrix() {
			return _matrix;
		}

		@Override
		public Color colorForValue(int value) {

			return colorForValue(-1, -1, -1, value);
		}

		@Override
		public int getHeaderCount() {

			return _headers != null ? _headers.length : 0;
		}

		@Override
		public String getHeader(int index) {

			return _headers != null && index < _headers.length ? _headers[index]
					.getName() : "";
		}

		@Override
		public MatrixHeader getMatrixHeader(int index) {
			return _headers != null && index >= 0 && index < _headers.length ? _headers[index]
					: MatrixHeaders.createHeader("");
		}

		@Override
		public Color colorForValue(int matrixIndex, int cellIndex,
				int subIndex, int value) {
			return _colorPalette.getOrDefault(value, Color.BLACK);
		}

		@Override
		public Map<Integer, Color> getColorMap() {
			return _colorPalette;
		}

		@Override
		public Iterator<DataLine> iterator() {

			return new Iterator<DataLine>() {
				private int current;
				{
					current = 0;
				}

				@Override
				public boolean hasNext() {
					
					return _matrix != null && current < _matrix.length;
				}

				@Override
				public DataLine next() {
					
					DataLine line = hasNext() ? _matrix[current++] : null;
					return line;
				}

				@Override
				public void remove() {
					
					throw new UnsupportedOperationException();
				}
				
			};
		}
		@Override
		public int getMaxDataLineLength() {
			int max = 0;
			for(DataLine line : _matrix){
				max = Math.max(max, line.getLength());
			}
			return max;
		}
		@Override
		public int getMaxDataLineRowIndex() {
			// TODO Auto-generated method stub
			int idx = -1;
			int max = 0;
			for(int i = 0; i < _matrix.length; ++i){
				DataLine line = _matrix[i];
				if(line.getLength() > max){
					max = line.getLength();
					idx = i;
				}
			}
			return idx;
		}
	}


	
	
}
