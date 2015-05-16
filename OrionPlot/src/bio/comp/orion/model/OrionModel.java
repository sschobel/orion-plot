package bio.comp.orion.model;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public interface OrionModel extends Iterable<DataLine> {
	public int getDataMatrixEntryCount();
	public DataLine getDataMatrixEntry(int idx);
	public Color colorForValue(int matrixIndex, int cellIndex, int subIndex, int value); 
	public Color colorForValue(int cellValue);
	public int getHeaderCount();
	public String getHeader(int index);
	public MatrixHeader getMatrixHeader(int index);
	public DataLine[] getDataMatrix();
	public SubCellFalseColorCoder getColorCoder();
	public void setColorCoder(SubCellFalseColorCoder colorCoder);
	public Set<Integer> getMatrixUniques();
	public int getMaxDataLineLength();
	public int getMaxDataLineRowIndex();

	
	public static class DefaultOrionModel implements OrionModel{
		DataLine[] _matrix;
		MatrixHeader[] _headers;
		Map<Integer, Color> _colorPalette = new HashMap<Integer, Color>();
		SubCellFalseColorCoder _colorLookup;
		SubCellFalseColorCoder _colorLookupWrapper;
		public DefaultOrionModel(DataLine[] matrix, MatrixHeader[] headers, SubCellFalseColorCoder colorCoder){
			this(matrix, headers, null, colorCoder);
		}
		public DefaultOrionModel(DataLine[] matrix, MatrixHeader[] headers, Map<Integer, Color>palette, SubCellFalseColorCoder colorCoder){
			_colorPalette = palette;
			_headers = headers;
			_matrix = matrix;
			_colorLookup = colorCoder;
			//This wrapper allows for changing the color coder without replacing it for every user
			_colorLookupWrapper = new SubCellFalseColorCoder(){

				@Override
				public Color colorForSubCell(int row, int cell, int subCell,
						int subCellValue) {
					// TODO Auto-generated method stub
					Color paletteColor = _colorPalette != null && _colorPalette.containsKey(subCellValue) ? _colorPalette.get(subCellValue) : null;
					Color lookupColor = _colorLookup != null ? _colorLookup.colorForSubCell(row, cell, subCell, subCellValue) : null;
					return MoreObjects.firstNonNull(lookupColor, MoreObjects.firstNonNull(paletteColor, Color.black));
				}

				@Override
				public int[] codesForValues() {
					// TODO Auto-generated method stub
					Set<Integer> paletteValues = _colorPalette.keySet();
					int [] lookupValueArray =  _colorLookup != null ? _colorLookup.codesForValues() : new int[0];
					
					Set<Integer> lookupValues = new HashSet<Integer>();
					for(int i : lookupValueArray){
						lookupValues.add(i);
					}
					lookupValues.addAll(paletteValues);
					int valueArray[] = new int[lookupValues.size()];
					int i = 0;
					for(Integer I : lookupValues){
						valueArray[i++]= I;
					}
					return valueArray;
				}
				
				
			};
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
			
			return _matrix != null ? _matrix.length : 0;
		}
		@Override
		public DataLine getDataMatrixEntry(int idx) {
			
			return _matrix != null && idx < _matrix.length ? 
					_matrix[idx] : null;
		}
		public DataLine[] getDataMatrix(){
			return _matrix;
		}
		@Override
		public Color colorForValue(int value) {
			
			return colorForValue(-1,-1,-1,value);
		}
		@Override
		public int getHeaderCount() {
			
			return _headers != null ? _headers.length : 0;
		}
		@Override
		public String getHeader(int index) {
			
			return _headers != null && index < _headers.length ? _headers[index].getName() : "";
		}
		@Override
		public MatrixHeader getMatrixHeader(int index){
			return _headers != null && index >= 0 && index < _headers.length ? _headers[index] : MatrixHeaders.createHeader("");
		}
		@Override
		public Color colorForValue(int matrixIndex, int cellIndex,
				int subIndex, int value) {
			
			return _colorLookupWrapper.colorForSubCell(matrixIndex, cellIndex, subIndex, value);
		}
		@Override
		public Iterator<DataLine> iterator() {
			
			return new Iterator<DataLine>(){
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
		public SubCellFalseColorCoder getColorCoder() {
			
			return _colorLookupWrapper;
		}
		@Override
		public void setColorCoder(SubCellFalseColorCoder colorCoder) {
			
			_colorLookup = colorCoder;
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
