package bio.comp.orion.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		public ColorIndex changeIndex(Integer newInt) {
			return new ColorIndex(newInt, _color);
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
		
		listeners.add(listener);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		

		return (ColorIndexConstants.TABLE_COLUMN_CLASSES.length > columnIndex) ? ColorIndexConstants.TABLE_COLUMN_CLASSES[columnIndex] : String.class;
	}

	@Override
	public int getColumnCount() {
		
		return ColorIndexConstants.TABLE_COLUMN_NAMES.length;
	}

	@Override
	public String getColumnName(int columnIndex) {
		
		return ColorIndexConstants.TABLE_COLUMN_NAMES[columnIndex];
	}

	@Override
	public int getRowCount() {
		
		return colorIndexes.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
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
		
		return (ColorIndexConstants.COLOR_TABLE_COLUMN == columnIndex);
	}

	@Override
	public void removeTableModelListener(TableModelListener listener) {
		
		listeners.remove(listener);
	}
	public static class ColorModelEvent{
		private Object sender;
		private ColorIndex oldValue;
		private ColorIndex newValue;
		public ColorModelEvent(Object sender, ColorIndex oldValue, ColorIndex newValue){
			this.oldValue = oldValue;
			this.newValue = newValue;
			this.sender = sender;
		}
		/**
		 * @return the sender
		 */
		public Object getSender() {
			return sender;
		}
		/**
		 * @return the oldValue
		 */
		public ColorIndex getOldValue() {
			return oldValue;
		}
		/**
		 * @return the newValue
		 */
		public ColorIndex getNewValue() {
			return newValue;
		}
		public boolean didColorChange(){
			return !oldValue.getColor().equals(newValue.getColor());
		}
		public boolean didIndexChange(){
			return !oldValue.getIndex().equals(newValue.getIndex());
		}
		
	}
	public interface ColorModelListener{
		public void colorModelChanged(ColorModelEvent cme);
	}

	private List<ColorModelListener> colorModelListeners = new ArrayList<ColorModelListener>();
	public void addColorModelListener(ColorModelListener cml){
		if(!colorModelListeners.contains(cml)){
			colorModelListeners.add(cml);
		}
	}
	public void removeColorModelListener(ColorModelListener cml){
		if(colorModelListeners.contains(cml)){
			colorModelListeners.remove(cml);
		}
	}
	public void removeAllColorModelListeners(){
		colorModelListeners.clear();
	}
	private void _notifyTableModelListeners(TableModelEvent evt){
		if(evt == null){
			evt = new TableModelEvent(this);
		}
		for (TableModelListener listener : listeners) {
			listener.tableChanged(evt);
		}
	}
	private final void _notifyColorModelListeners(ColorModelEvent evt){
		if(evt != null){
			
		}
		for(ColorModelListener cml : colorModelListeners){
			cml.colorModelChanged(evt);
		}
		
	}
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		
		ColorIndex oldValue = (colorIndexes.size() > rowIndex) ? colorIndexes.get(rowIndex) : null;
		ColorIndex newValue = null;
		switch (columnIndex) {
		case ColorIndexConstants.INDEX_TABLE_COLUMN:
			if(value instanceof Integer){
				Integer newInt = (Integer)value;
				newValue = oldValue.changeIndex(newInt);
			}
			break;
		case ColorIndexConstants.COLOR_TABLE_COLUMN:
			if(value instanceof Color){
				Color newColor = (Color)value;
				newValue = oldValue.changeColor(newColor);
				_notifyColorModelListeners(new ColorModelEvent(this, oldValue, newValue));
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
	public int[] allIndexedValues(){
		int [] values = new int[colorIndexes.size()];
		for(int i = 0; i < values.length; ++i){
			ColorIndex idx = colorIndexes.get(i);
			values[i] = idx.getIndex();
		}
		return values;
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
	private int _addColorIndex(Integer value, Color color, ReplacementStrategy rep){
		int existingIdx = indexForValue(value);
		if(existingIdx >= 0){
			if(rep.replaceExisting){
                colorIndexes.set(existingIdx, new ColorIndex(value, color));
			}
		}
		else{
			existingIdx = colorIndexes.size();
			colorIndexes.add(new ColorIndex(value, color));
		}
		return existingIdx;
	}
	public void addColorIndex(Integer value, Color color, ReplacementStrategy rep){
		int row = _addColorIndex(value, color, rep);
		_notifyTableModelListeners(new TableModelEvent(this, row));
	}
    public void removeAllColorIndexes(){
    	colorIndexes.removeAll(colorIndexes);
    }
	public void addColorIndexIfAbsent(Integer value, Color color){
		int idx = indexForValue(value);
		if(idx < 0){
			addColorIndex(value, color, ReplacementStrategy.KEEP);
		}
	}
	public void addColorIndexes(Color[] colors){
		int loRow = colorIndexes.size();
		int hiRow= 0;
		for(int idx = 0; idx < colors.length; ++idx){
			Color color = colors[idx];
			int row = _addColorIndex(idx, color, ReplacementStrategy.REPLACE);
			loRow = Math.min(loRow, row);
			hiRow = Math.max(hiRow, row);
		}
		if(loRow <= hiRow){
			_notifyTableModelListeners(new TableModelEvent(this, loRow, hiRow));
		}
	}
	class OColorWell extends JPanel{
		/**
		 * 
		 */
		private static final long serialVersionUID = 6109784308860333251L;
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
	public enum ReplacementStrategy{
		KEEP(false),
		REPLACE(true);
		private final Boolean replaceExisting;
		ReplacementStrategy(boolean replaceExisting){
			this.replaceExisting= replaceExisting;
		}
		boolean replaceExisting(){
			return replaceExisting;
		}
	}
	public void addColorIndexes(Map<Integer, Color>colorMap){
		this.addColorIndexes(colorMap, ReplacementStrategy.KEEP);
	}
	public void addColorIndexes(Map<Integer, Color>fileCoder, ReplacementStrategy strat) {
		for(Map.Entry<Integer, Color> colorIndex : fileCoder.entrySet()){
			addColorIndex(colorIndex.getKey(), colorIndex.getValue(), strat);
		}
	}
};
