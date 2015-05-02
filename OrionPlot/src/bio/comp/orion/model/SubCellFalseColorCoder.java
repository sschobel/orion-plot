package bio.comp.orion.model;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public interface SubCellFalseColorCoder{
	public static Color[] DEFAULT_CODING = new Color[] { Color.RED, Color.ORANGE, Color.YELLOW,
			Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.PINK,
			Color.GRAY, Color.DARK_GRAY, Color.BLACK };
	public static Color[] BASE_COLORS = new Color[] { Color.RED, Color.ORANGE, Color.YELLOW,
			Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.PINK,};
	public static SubCellFalseColorCoder DEFAULT_COLOR_CODER = new UsingArrayOfColors(DEFAULT_CODING);
	Color colorForSubCell(int row, int cell, int subCell, int subCellValue);
	int[] codesForValues();
	
	public static class UsingColorMap implements SubCellFalseColorCoder{
		private Map<Integer, Color>_lookup;
		public UsingColorMap(Map<Integer, Color>lookup){
			_lookup = lookup;
		}
		public UsingColorMap(HashMap<Integer, Color> hashMap) {
			// TODO Auto-generated constructor stub
			this((Map<Integer, Color>)hashMap);
		}
		@Override
		public Color colorForSubCell(int row, int cell, int subCell,
				int subCellValue) {
			
			Integer sval = subCellValue;
			return _lookup != null && _lookup.containsKey(sval) ? _lookup.get(sval) : Color.black;
		}
		@Override
		public int[] codesForValues() {
			
			int [] values = new int[_lookup != null ? _lookup.keySet().size() : 0];
			Iterator<Integer> iter = _lookup.keySet().iterator();
			for(int i = 0; i < values.length; ++i){
				values[i] = iter.hasNext() ? iter.next() : -1;
			}
			return values;
		}
	}
	public static class UniqueColorIterable implements Iterable<Color>{

		private Color[] baseColors;
		public UniqueColorIterable(Color[] base){
			baseColors = base;
		}
		
		@Override
		public Iterator<Color> iterator() {
			
			return new Iterator<Color>(){
				
                int i = 0;
                public Color generateColor(int i){
                	int ci = i % baseColors.length;
                	int loopNum = i / baseColors.length;
                	int loopLevel = loopNum / 2;
                	boolean alterSaturation = (loopNum % 2 == 0) ? true : false;
                	Color base = baseColors[ci];
                	while(loopLevel-- > 0){
                		base = alterSaturation ? base.darker() : base.brighter();
                	}
                	return base;
                }
				
				@Override
				public boolean hasNext() {
					return true;
				}

				@Override
				public Color next() {
					return generateColor(i++);
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
					
				}
				
			};
		}
		
	}
	
	public static class UsingDistinctColorsForValues extends UsingColorMap{ 
		public UsingDistinctColorsForValues(final int[] values){
			super(new HashMap<Integer, Color>(){
				/**
				 * 
				 */
				private static final long serialVersionUID = 5389254432686542506L;

				{
					Iterator<Color> colors = new UniqueColorIterable(BASE_COLORS).iterator();
					for(int value : values){
						put(value, colors.next());
					}
				}
			});
			
		}
	}
	
	public static class UsingArrayOfColors implements SubCellFalseColorCoder{
		private Color[] _colors;
		public UsingArrayOfColors(Color[] colors) {
			_colors = colors;
		}

		@Override
		public Color colorForSubCell(int row, int cell, int subCell,
				int subCellValue) {
			
			return _colors != null && subCellValue < _colors.length ? _colors[subCellValue] : Color.BLACK;
		}
		
		@Override
		public int[] codesForValues(){
			int[] values = new int[_colors.length];
			for(int i = 0; i < values.length; ++i){
				values[i] = i;
			}
			return values;
		}
		
	}
}
