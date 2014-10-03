package bio.comp.orion.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

		class ColorIndexTableModel implements TableModel{
			 class ColorIndex{
				Integer _idx;
				Color _color;
				ColorIndex(Integer idx, Color color){
					_idx = idx;
					_color = color;
				}
				Integer getIndex(){
					return _idx;
				}
				Color getColor(){
					return _color;
				}
				ColorIndex changeColor(Color aColor){
					return new ColorIndex(_idx, aColor);
				}
			}
			List<ColorIndex> colorIndexes = new ArrayList<ColorIndex>();
			Set<TableModelListener> listeners = new HashSet<TableModelListener>();
			ColorIndexTableModel(){
				
			}
			static ColorIndexTableModel createWithColors(Color[] colors){
				ColorIndexTableModel model = new ColorIndexTableModel();
				model.addColorIndexes(colors);
				return model;
			}
			@Override
			public void addTableModelListener(TableModelListener listener) {
				// TODO Auto-generated method stub
				listeners.add(listener);
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				// TODO Auto-generated method stub

				return (ColorIndexConstants.TABLE_COLUMN_CLASSES.length > columnIndex) ? ColorIndexConstants.TABLE_COLUMN_CLASSES[columnIndex] : String.class;
			}
			
			@Override
			public int getColumnCount() {
				// TODO Auto-generated method stub
				return ColorIndexConstants.TABLE_COLUMN_NAMES.length;
			}

			@Override
			public String getColumnName(int columnIndex) {
				// TODO Auto-generated method stub
				return ColorIndexConstants.TABLE_COLUMN_NAMES[columnIndex];
			}

			@Override
			public int getRowCount() {
				// TODO Auto-generated method stub
				return colorIndexes.size();
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				// TODO Auto-generated method stub
				Object value = null;
				if(colorIndexes.size() > rowIndex){
					ColorIndex colorIdx = colorIndexes.get(rowIndex);
					if(getColumnCount() > columnIndex){
						switch (columnIndex) {
						case ColorIndexConstants.INDEX_TABLE_COLUMN:
							value = colorIdx.getIndex();
							break;
						case ColorIndexConstants.COLOR_TABLE_COLUMN:
							value = colorIdx.getColor();
							break;
						default:
							break;
						}
					}
				}
				return value;
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				// TODO Auto-generated method stub
				return (ColorIndexConstants.COLOR_TABLE_COLUMN == columnIndex);
			}

			@Override
			public void removeTableModelListener(TableModelListener listener) {
				// TODO Auto-generated method stub
				listeners.remove(listener);
			}

			private void _notifyTableModelListeners(TableModelEvent evt){
				if(evt == null){
					evt = new TableModelEvent(this);
				}
                for (TableModelListener listener : listeners) {
                    listener.tableChanged(evt);
                }
			}
			@Override
			public void setValueAt(Object value, int rowIndex, int columnIndex) {
				// TODO Auto-generated method stub
                ColorIndex oldValue = (colorIndexes.size() > rowIndex) ? colorIndexes.get(rowIndex) : null;
                ColorIndex newValue = null;
				switch (columnIndex) {
				case ColorIndexConstants.INDEX_TABLE_COLUMN:
					//Ignore forced changes to INDEX
					break;
				case ColorIndexConstants.COLOR_TABLE_COLUMN:
					if(value instanceof Color){
						Color newColor = (Color)value;
					    newValue = oldValue.changeColor(newColor);
					}
					break;
				default:
					break;
				}
				if(newValue != null){
					colorIndexes.set(rowIndex, newValue);
					_notifyTableModelListeners(new TableModelEvent(this, rowIndex, rowIndex, columnIndex));
				}
			}
			public int indexForValue(Integer value){
				int index = -1;
				for(int i = 0; i < colorIndexes.size(); ++i){
					ColorIndex cIndex = colorIndexes.get(i);
					if(cIndex.getIndex().equals(value)){
						index = i;
					}
				}
				return index;
			}
			public Color colorForValue(Integer value){
				Color cv = null;
				int idx = indexForValue(value);
				if(idx >= 0 && idx < colorIndexes.size()){
					ColorIndex cIndex = colorIndexes.get(idx);
					cv = (cIndex != null) ? cIndex.getColor() : null;
				}
				return cv;
			}
			private int _addColorIndex(Integer value, Color color){
				int existingIdx = indexForValue(value);
				if(existingIdx >= 0){
					colorIndexes.set(existingIdx, new ColorIndex(value, color));
				}
				else{
					existingIdx = colorIndexes.size();
					colorIndexes.add(new ColorIndex(value, color));
				}
				return existingIdx;
			}
			public void addColorIndex(Integer value, Color color){
				int row = _addColorIndex(value, color);
				_notifyTableModelListeners(new TableModelEvent(this, row));
			}
			
			public void addColorIndexIfAbsent(Integer value, Color color){
				int idx = indexForValue(value);
				if(idx < 0){
					addColorIndex(value, color);
				}
			}
			public void addColorIndexes(Color[] colors){
				int loRow = colorIndexes.size();
				int hiRow= 0;
				for(int idx = 0; idx < colors.length; ++idx){
					Color color = colors[idx];
					int row = _addColorIndex(idx, color);
					loRow = Math.min(loRow, row);
					hiRow = Math.max(hiRow, row);
				}
				if(loRow <= hiRow){
					_notifyTableModelListeners(new TableModelEvent(this, loRow, hiRow));
				}
			}
			class OColorWell extends JPanel{
			   private Color _wellColor;	
			   OColorWell(Color wellColor){
				   _wellColor = wellColor;
			   }
			   OColorWell(){
				   this(Color.black);
			   }
			   @Override
			   public void paintComponent(Graphics g){
				  super.paintComponent(g); 
				  Dimension dims = getSize();
				  Color saveColor = g.getColor();
				  g.setColor(_wellColor);
				  g.fillRect(0 ,0 , (int)dims.getWidth(), (int)dims.getHeight());
				  
				  g.setColor(saveColor);
			   }
			}
		class ColorCellRenderer implements TableCellRenderer{
			TableCellRenderer _fallback;
			ColorCellRenderer(TableCellRenderer fallback){
				_fallback = fallback;
			}
			ColorCellRenderer(){
				this(null);
			}
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				// TODO Auto-generated method stub
				Component comp;
				if(value instanceof Color){
					Color color = (Color) value;
					JPanel jcc = new OColorWell(color);
					comp = jcc;
				}
				else{
					comp = _fallback ==null? new JLabel() :_fallback.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); 
				}

				return comp;
			}
			
		}
			public TableCellRenderer createColorCellRenderer(){
				return new ColorCellRenderer();
			}
			public TableCellRenderer createColorCellRenderer(TableCellRenderer _default){
				return new ColorCellRenderer(_default);
			}
		};
