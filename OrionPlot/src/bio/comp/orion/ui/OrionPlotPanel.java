package bio.comp.orion.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import bio.comp.orion.model.DataLine;
import bio.comp.orion.model.MatrixHeaders;

public class OrionPlotPanel extends JPanel implements SubCellFalseColorCoder{

	/**
	 * 
	 */
	private static final long serialVersionUID = 507178960111041135L;
	private DataLine[] _matrix;
	private Color[] _colors;
	private int _freeColorIdx;
	private String[] _headers;
	private List<JLabel> _rowLabels;
	private Map<Integer, Color> _colorLookup;
	private SubCellFalseColorCoder _coloringDelegate;
	
	public static final String MATRIX_PROPERTY = "matrix";
	public static final String COLORS_PROPERTY = "colors";
	public static final String HEADERRS_PROPERTY = "headers";
	private static Shape __defaultShape;//  = new Rectangle2D.Double(0, 0, BLOCK_WIDTH, BLOCK_WIDTH);
	private static Shape __twoValueShapeAlpha;// = new Rectangle2D.Double(0, 0, BLOCK_WIDTH/2, BLOCK_HEIGHT);
	private static Shape __twoValueShapeBeta;//  = new Rectangle2D.Double(BLOCK_WIDTH/2, 0, BLOCK_WIDTH/2, BLOCK_HEIGHT);
	private static final int DEFAULT_LABEL_GAP = 5;
	private int _labelGap = DEFAULT_LABEL_GAP;
	
	//private Shape[] _shapes = { _defaultShape, _twoValueShapeAlpha, _twoValueShapeBeta };

	private Shape getDefaultShape(){
		if(__defaultShape == null){
			__defaultShape=new Rectangle2D.Double(0, 0, getBlockWidth(), getBlockHeight());
		}
		return __defaultShape;
	}
	
	private Shape getTwoValueShapeAlpha(){
		if(__twoValueShapeAlpha == null){
			__twoValueShapeAlpha = new Rectangle2D.Double(0,0, getBlockWidth(), getBlockHeight()/2);
		}
		return __twoValueShapeAlpha;
	}
	
	private Shape getTwoValueShapeBeta(){
		if(__twoValueShapeBeta == null){
			__twoValueShapeBeta = new Rectangle2D.Double(0, getBlockHeight()/2, getBlockWidth(), getBlockHeight()/2);
		}
		return __twoValueShapeBeta;
	}
	
	private static GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	
	private double vInset = 15.0;
	private double hInset = 15.0;
	
	public double getVerticalInset(){
		return vInset;
	}
	
	public double getHorizontalInset(){
		return hInset;
	}
	
	public OrionPlotPanel(){
		super();
		_colors = new Color[] { Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.PINK, Color.GRAY, Color.DARK_GRAY, Color.BLACK };
		_freeColorIdx=0;
		_rowLabels = new ArrayList<JLabel>();
		_colorLookup = new HashMap<Integer, Color>();
		this.setLayout(new MigLayout());
		this.setBackground(Color.white);
		System.err.printf("%s\n", ge.getDefaultScreenDevice().getDefaultConfiguration());
	}
	
/*
	private Color nextAvailableColor(){
		if(_freeColorIdx < _colors.length){
			return _colors[_freeColorIdx++];
		}
		else{
			return Color.black;
		}
	}
	private Color colorForValue(Integer value){
		if (value == null){
			return Color.black;
		}
		else{
			if(!_colorLookup.containsKey(value)){
				_colorLookup.put(value, nextAvailableColor());
			}
			return _colorLookup.get(value);
		}
	}
	
	private Shape shapeForMatrixValue(final Shape[] shapes, final Shape defaultShape, final int value, final int div_idx, final int divisions){
		switch(divisions){
		case 1:
			return getDefaultShape();
		case 2:
			System.out.println(String.format("Shape for value %d index %d of %d", value, div_idx, divisions));
			return div_idx == 0 ? getTwoValueShapeAlpha() : getTwoValueShapeBeta();
		default:
			return getDefaultShape();
		}
		
	}
*/	
	
	@Override
    public Color colorForSubCell(int row, int cell, int subCell, int subCellValue){
    	if(_coloringDelegate != null){
    		return _coloringDelegate.colorForSubCell(row, cell, subCell, subCellValue);
    	}
    	else{
    		return this._colorForSubCell(row, cell, subCell, subCellValue) ;
    	}
    }
    
    public void setColoringDelegate(SubCellFalseColorCoder delegate){
    	_coloringDelegate = delegate;
    }
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		
		if(g instanceof Graphics2D){
			Graphics2D g2 = (Graphics2D)g;
			Rectangle clipBounds = g2.getClipBounds();
			double w = getBlockWidth(), h = w;
			AffineTransform saveTx = g2.getTransform();
			Color saveChroma = g2.getColor();
			AffineTransform insetTx = AffineTransform.getTranslateInstance(getHorizontalInset(), getVerticalInset());
			g2.transform(insetTx);
			AffineTransform rowTx = AffineTransform.getTranslateInstance(0, h * 1.25);
			AffineTransform colTx = AffineTransform.getTranslateInstance(w * 1.25, 0);
			if(_matrix != null){
			for(int row_i = 0; row_i < _matrix.length; row_i++){
				AffineTransform rowSave = g2.getTransform();
				for(int cell_j = 0; cell_j < _matrix[row_i].getLength(); cell_j++){
					//System.out.println(_matrix[i].toFileString());
					List<Integer> values = _matrix[row_i].getValuesAt(cell_j);
					//System.out.println(String.format("Values %s", values.toString()));
					int idx = 0;
					if(!values.isEmpty()){

						if(values.size() < 2){
							int alpha = values.get(0);
							Color aColor = colorForSubCell(row_i, cell_j, 0, alpha);
							g2.setColor(aColor);
							g2.fill(getDefaultShape());
							g2.setColor(Color.black);
							g2.draw(getDefaultShape());
						
						}
						else{
							int alpha = values.get(0);
							int beta  = values.get(1);
							Color aColor = colorForSubCell(row_i, cell_j, 0, alpha);
							Color bColor = colorForSubCell(row_i, cell_j, 1, beta);
							g2.setColor(aColor);
							g2.fill(getTwoValueShapeAlpha());
							g2.setColor(Color.black);
							g2.draw(getTwoValueShapeAlpha());
							g2.setColor(bColor);
							g2.fill(getTwoValueShapeBeta());
							g2.setColor(Color.black);
							g2.draw(getTwoValueShapeBeta());
							
						}
					}
					g2.transform(colTx);
				}
				g2.setTransform(rowSave);
				g2.transform(rowTx);
			}
			}
			g2.setTransform(saveTx);
			g2.setColor(saveChroma);
		}
		
	}
	
	public double getPlotElementPad(){
		return 1.25;
	}
	
	private double _blockWidth = MatrixHeaders.BLOCK_WIDTH;
	
	public double getBlockWidth(){
		return _blockWidth;
	}
	
	private double _blockHeight = MatrixHeaders.BLOCK_HEIGHT;
	
	public double getBlockHeight(){
		return _blockHeight;
	}
	
	public static final boolean INCLUDE_PADDING=true;
	public static final boolean EXCLUDE_PADDING=false;
	
	public double getPlotElementWidth(boolean includePadding){
		return getBlockWidth() * (includePadding ? getPlotElementPad() : 1.0);
	}
	
	public double getPlotElementHeight(boolean includePadding){
		return getBlockHeight() * (includePadding ? getPlotElementPad() : 1.0);
	}
	
	public Rectangle2D getMatrixBounds(boolean includePadding){
		int height = _matrix.length;
		int width = 0;
		for(DataLine row : _matrix){
			width = row.getLength() > width ? row.getLength() : width;
		}
		double h = (double)height * getPlotElementHeight(includePadding);
		double w = (double)width  * getPlotElementWidth(includePadding);
		return new Rectangle2D.Double((double)getHorizontalInset(), (double)getVerticalInset(), w + getHorizontalInset() , h + getVerticalInset());
	}
	public Rectangle2D getMatrixBounds(){
		return getMatrixBounds(INCLUDE_PADDING);
	}
	
	public int getLabelGap(){
		return _labelGap;
	}
	
	public Rectangle2D calculateComponentBounds(){
		Rectangle2D bounds = getMatrixBounds();
		Rectangle lbounds = new Rectangle();
		for(JLabel label : _rowLabels){
			 label.getBounds(lbounds);
			 if(label.getParent() != null){
				 bounds.add(new Rectangle2D.Double(lbounds.getX(), lbounds.getY(), lbounds.getWidth(), lbounds.getHeight()));
			 }
		}
		return bounds;
	}
	
	
	private void setupRowLabels(){
		int componentCount = getComponentCount();
		List<Component> comps = new ArrayList<Component>(componentCount);
		for(int i = 0; i < componentCount; ++i){
			Component c = getComponent(i);
			comps.add(c);
		}
		for(Component c : comps){
			remove(c);
		}
		Rectangle2D bounds = getMatrixBounds(EXCLUDE_PADDING);
		int maxX = bounds != null ? ((int)bounds.getMaxX()) : 0; 
		String labelMigLayout = String.format("gapleft %d, wrap, h %d!", maxX + getLabelGap(), ((int)getPlotElementHeight(EXCLUDE_PADDING)) + 1);
		for(DataLine dl : _matrix){	
			/*
			for(int i = 0; i < dl.getLength(); ++i){
				List<Integer> values = dl.getValuesAt(i);
				Component pointComp = componentForMatrixValue(values);
				add(pointComp);
			}
			*/
			JLabel label = new JLabel(dl.getTitle());
			label.setForeground(dl.getTitleColor());
			add(label, labelMigLayout);
		}
		
	}
	
	private void resizePlot(){
		setupRowLabels();
		Rectangle2D r = calculateComponentBounds();
		System.out.format("resizing window to %d %d\n", (int)r.getWidth(), (int)r.getHeight());
		setPreferredSize(new Dimension((int)r.getWidth(), (int)r.getHeight()));
		revalidate();
	}
	
	public DataLine [] getPlotMatrix(){
		return _matrix;
	}
	
	public Color [] getColorMap(){
		return _colors;
	}
	
	public String[] getHeaders(){
		return _headers;
	}
	
	public void setPlotMatrix(DataLine[] matrix){
		DataLine[] _oldMatrix = _matrix;
		_matrix = matrix;
		resizePlot();
		this.firePropertyChange("matrix", _oldMatrix, _matrix);
	}
	
	public void setColorMap(Color[] colors){
		Color[] _oldColors = _colors;
		_colors = colors;
		this.firePropertyChange("colors",	 _oldColors, _colors);
	}

	public void setHeaders(String[] headers) {
		// TODO Auto-generated method stub
		String[] _old = _headers;
		_headers = headers;
		this.firePropertyChange("headers", _old, _headers);
	}

	private Color _colorForSubCell(int row, int cell, int subCell,
			int subCellValue) {
		// TODO Auto-generated method stub
    		return 0 <= subCellValue && subCellValue < _colors.length ? _colors[subCellValue] : Color.black;
	}

}
