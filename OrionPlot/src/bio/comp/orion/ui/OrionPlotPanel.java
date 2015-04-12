package bio.comp.orion.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.batik.bridge.TextUtilities;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.script.Window;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGLoadEventDispatcherEvent;
import org.apache.batik.swing.svg.SVGLoadEventDispatcherListener;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.apache.batik.util.RunnableQueue;
import org.apache.fop.svg.SVGUtilities;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;

import bio.comp.orion.model.DataLine;
import bio.comp.orion.model.MatrixHeaders;
import bio.comp.orion.model.OrionModel;
import bio.comp.orion.model.SubCellFalseColorCoder;

import com.sun.xml.internal.ws.util.xml.NodeListIterator;

public class OrionPlotPanel extends JSVGCanvas  {
	private interface Runner{
		public void invokeAndWait(Runnable r) throws InterruptedException;
		public void invokeLater(Runnable r) ;
	}
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 507178960111041135L;
	private List<JLabel> _rowLabels;
	private SVGDocument _document;
	private Window _window;
	private Runner _runner;

    private OrionModel _model = new OrionModel.DefaultOrionModel();
	public static final String MATRIX_PROPERTY = "matrix";
	public static final String COLORS_PROPERTY = "colors";
	public static final String HEADERRS_PROPERTY = "headers";
	private static Shape __defaultShape;// = new Rectangle2D.Double(0, 0,
										// BLOCK_WIDTH, BLOCK_WIDTH);
	private static Shape __twoValueShapeAlpha;// = new Rectangle2D.Double(0, 0,
												// BLOCK_WIDTH/2, BLOCK_HEIGHT);
	private static Shape __twoValueShapeBeta;// = new
												// Rectangle2D.Double(BLOCK_WIDTH/2,
												// 0, BLOCK_WIDTH/2,
												// BLOCK_HEIGHT);
	private static final int DEFAULT_LABEL_GAP = 5;
	private int _labelGap = DEFAULT_LABEL_GAP;

	// private Shape[] _shapes = { _defaultShape, _twoValueShapeAlpha,
	// _twoValueShapeBeta };

	private Shape getDefaultShape() {
		if (__defaultShape == null) {
			__defaultShape = new Rectangle2D.Double(0, 0, getBlockWidth(),
					getBlockHeight());
		}
		return __defaultShape;
	}

	private Shape getTwoValueShapeAlpha() {
		if (__twoValueShapeAlpha == null) {
			__twoValueShapeAlpha = new Rectangle2D.Double(0, 0,
					getBlockWidth(), getBlockHeight() / 2);
		}
		return __twoValueShapeAlpha;
	}

	private Shape getTwoValueShapeBeta() {
		if (__twoValueShapeBeta == null) {
			__twoValueShapeBeta = new Rectangle2D.Double(0,
					getBlockHeight() / 2, getBlockWidth(), getBlockHeight() / 2);
		}
		return __twoValueShapeBeta;
	}

	private static GraphicsEnvironment ge = GraphicsEnvironment
			.getLocalGraphicsEnvironment();

	private double vInset = 15.0;
	private double hInset = 15.0;

	public double getVerticalInset() {
		return vInset;
	}

	public double getHorizontalInset() {
		return hInset;
	}

	private void registerListeners() {
		Element elt = _document.getElementById("an-id");
		EventTarget t = (elt != null) ? (EventTarget) elt : null;
		t.addEventListener("SVGLoad", new EventListener() {

			@Override
			public void handleEvent(Event evt) {
				// TODO Auto-generated method stub
				System.out.format("handleEvent/%s/(%s)", "SVGLoad", evt);
			}
		}, false);
		t.addEventListener("click", new EventListener() {

			@Override
			public void handleEvent(Event evt) {
				// TODO Auto-generated method stub
				System.out.format("handleEvent/%s/(%s)", "click", evt);
			}
		}, false);
	}

	private static class RunnableRunner implements Runner{



		@Override
		public void invokeAndWait(Runnable r) throws InterruptedException {
			// TODO Auto-generated method stub
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				throw new InterruptedException(e.toString());
			}
			
		}

		@Override
		public void invokeLater(Runnable r) {
			// TODO Auto-generated method stub
			SwingUtilities.invokeLater(r);
			
		}
		
	}
	
	public OrionPlotPanel() {
		super();
		_runner = new RunnableRunner();
		
		_rowLabels = new ArrayList<JLabel>();
		this.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
		this.addSVGLoadEventDispatcherListener(new SVGLoadEventDispatcherListener() {

			@Override
			public void svgLoadEventDispatchStarted(
					SVGLoadEventDispatcherEvent e) {
				// TODO Auto-generated method stub
				_document = OrionPlotPanel.this.getSVGDocument();
				_window = OrionPlotPanel.this.getUpdateManager()
						.getScriptingEnvironment().createWindow();
				_runner = new Runner(){
					RunnableQueue batikrunner = OrionPlotPanel.this.getUpdateManager().getUpdateRunnableQueue();
					@Override
					public void invokeAndWait(Runnable r) throws InterruptedException {
						// TODO Auto-generated method stub
						batikrunner.invokeAndWait(r);
					}

					@Override
					public void invokeLater(Runnable r) {
						// TODO Auto-generated method stub
						batikrunner.invokeLater(r);
						
					}
					
				};
				OrionPlotPanel.this.loadSVG();
			}

			@Override
			public void svgLoadEventDispatchFailed(SVGLoadEventDispatcherEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void svgLoadEventDispatchCompleted(
					SVGLoadEventDispatcherEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void svgLoadEventDispatchCancelled(
					SVGLoadEventDispatcherEvent e) {
				// TODO Auto-generated method stub

			}
		});
		System.err.printf("%s\n", ge.getDefaultScreenDevice()
				.getDefaultConfiguration());
	}

	/*
	 * private Color nextAvailableColor(){ if(_freeColorIdx < _colors.length){
	 * return _colors[_freeColorIdx++]; } else{ return Color.black; } } private
	 * Color colorForValue(Integer value){ if (value == null){ return
	 * Color.black; } else{ if(!_colorLookup.containsKey(value)){
	 * _colorLookup.put(value, nextAvailableColor()); } return
	 * _colorLookup.get(value); } }
	 * 
	 * private Shape shapeForMatrixValue(final Shape[] shapes, final Shape
	 * defaultShape, final int value, final int div_idx, final int divisions){
	 * switch(divisions){ case 1: return getDefaultShape(); case 2:
	 * System.out.println(String.format("Shape for value %d index %d of %d",
	 * value, div_idx, divisions)); return div_idx == 0 ?
	 * getTwoValueShapeAlpha() : getTwoValueShapeBeta(); default: return
	 * getDefaultShape(); }
	 * 
	 * }
	 */

	
	private List<Color> colorsForCell(int row, int cell) {
			DataLine line = _model.getDataMatrixEntry(row);
			ArrayList<Color> colors = new ArrayList<Color>();
			if (line != null) {
				List<Integer> cellValues = line.getValuesAt(cell);
				for (int i = 0; cellValues != null && i < cellValues.size(); ++i) {
					Color subcellColor = _model.colorForValue(
							cellValues.get(i));
					colors.add(subcellColor != null ? subcellColor : Color.PINK);
				}

			}
	
		return colors;
	}

	

	private interface CellIterator {
		public void iterate(DataLine sourceLine, int row, int cell, List<Rectangle2D> subcells, List<Integer> values,
				List<Color> colors);
	}

	private Rectangle2D partitionRectangleX(Rectangle2D src, Rectangle2D part,
			double ratio) {
		if (src != null) {
			if (part == null) {
				part = (Rectangle2D) src.clone();
			} else {
				part.setRect(src);
			}
			double srcWidth = src.getWidth();
			double srcX = src.getX();
			double partWidth = srcWidth / ratio;
			double partX = srcX;
			double remainWidth = srcWidth - partWidth;
			double remainX = srcX + partWidth;
			src.setRect(remainX, src.getY(), remainWidth, src.getHeight());
			part.setRect(partX, part.getY(), partWidth, part.getHeight());
		}
		return part;
	}

	private List<Rectangle2D> splitRectangle(Rectangle2D rect, int pieces) {
		ArrayList<Rectangle2D> parts = new ArrayList<Rectangle2D>(pieces);
		double partRatio = 1.0 / ((double) pieces);
		while (pieces-- > 0) {
			Rectangle2D remain = (Rectangle2D) rect.clone();
			Rectangle2D part = partitionRectangleX(remain, null, partRatio);
			parts.add(part);

		}
		return parts;
	}
	private AffineTransform getCellTransform(){
		double w = getBlockWidth(), h = w;

		AffineTransform place = AffineTransform.getScaleInstance(w * 1.25, h * 1.25);
		AffineTransform offset = AffineTransform.getTranslateInstance(
				getHorizontalInset(), getVerticalInset());
		AffineTransform tx = new AffineTransform(offset);
		tx.concatenate(place);
		return tx;
	}
	public void overAllCells(CellIterator iter) {
		AffineTransform tx = getCellTransform();
		Point2D cellOrigin = new Point2D.Double();
		Point2D txCellOrigin = new Point2D.Double();
		Rectangle2D cellRect = new Rectangle2D.Double();
		double cw = getBlockWidth();
		double ch = getBlockHeight();
		int matrixLen = _model.getDataMatrixEntryCount();
		for (int row_i = 0; iter != null
				&& row_i < matrixLen; row_i++) {
			DataLine line = _model.getDataMatrixEntry(row_i);
			for (int cell_j = 0; iter != null
					&& cell_j < line.getLength(); cell_j++) {
				List<Integer> cellValues = line.getValuesAt(cell_j);
				List<Color> cellColors = colorsForCell(row_i, cell_j);
				cellOrigin.setLocation((double) cell_j, (double) row_i);
				tx.transform(cellOrigin, txCellOrigin);
				cellRect.setRect(txCellOrigin.getX(), txCellOrigin.getY(), cw,
						ch);
				List<Rectangle2D> cellRects = splitRectangle(cellRect,
						cellValues.size());
				iter.iterate(line, row_i, cell_j, cellRects, cellValues, cellColors);
			}
		}
	}

	/*
	 * public void paintComponent(Graphics g){ super.paintComponent(g);
	 * 
	 * if(g instanceof Graphics2D){ Graphics2D g2 = (Graphics2D)g; Rectangle
	 * clipBounds = g2.getClipBounds(); double w = getBlockWidth(), h = w;
	 * AffineTransform saveTx = g2.getTransform(); Color saveChroma =
	 * g2.getColor(); AffineTransform insetTx =
	 * AffineTransform.getTranslateInstance(getHorizontalInset(),
	 * getVerticalInset()); g2.transform(insetTx); AffineTransform rowTx =
	 * AffineTransform.getTranslateInstance(0, h * 1.25); AffineTransform colTx
	 * = AffineTransform.getTranslateInstance(w * 1.25, 0); if(_matrix != null){
	 * for(int row_i = 0; row_i < _matrix.length; row_i++){ AffineTransform
	 * rowSave = g2.getTransform(); for(int cell_j = 0; cell_j <
	 * _matrix[row_i].getLength(); cell_j++){
	 * //System.out.println(_matrix[i].toFileString()); List<Integer> values =
	 * _matrix[row_i].getValuesAt(cell_j);
	 * //System.out.println(String.format("Values %s", values.toString())); int
	 * idx = 0; if(!values.isEmpty()){
	 * 
	 * if(values.size() < 2){ int alpha = values.get(0); Color aColor =
	 * colorForSubCell(row_i, cell_j, 0, alpha); g2.setColor(aColor);
	 * g2.fill(getDefaultShape()); g2.setColor(Color.black);
	 * g2.draw(getDefaultShape());
	 * 
	 * } else{ int alpha = values.get(0); int beta = values.get(1); Color aColor
	 * = colorForSubCell(row_i, cell_j, 0, alpha); Color bColor =
	 * colorForSubCell(row_i, cell_j, 1, beta); g2.setColor(aColor);
	 * g2.fill(getTwoValueShapeAlpha()); g2.setColor(Color.black);
	 * g2.draw(getTwoValueShapeAlpha()); g2.setColor(bColor);
	 * g2.fill(getTwoValueShapeBeta()); g2.setColor(Color.black);
	 * g2.draw(getTwoValueShapeBeta());
	 * 
	 * } } g2.transform(colTx); } g2.setTransform(rowSave); g2.transform(rowTx);
	 * } } g2.setTransform(saveTx); g2.setColor(saveChroma); }
	 * 
	 * }
	 */

	public double getPlotElementPad() {
		return 1.25;
	}

	private double _blockWidth = MatrixHeaders.BLOCK_WIDTH;

	public double getBlockWidth() {
		return _blockWidth;
	}

	private double _blockHeight = MatrixHeaders.BLOCK_HEIGHT;

	public double getBlockHeight() {
		return _blockHeight;
	}

	public static final boolean INCLUDE_PADDING = true;
	public static final boolean EXCLUDE_PADDING = false;

	public double getPlotElementWidth(boolean includePadding) {
		return getBlockWidth() * (includePadding ? getPlotElementPad() : 1.0);
	}

	public double getPlotElementHeight(boolean includePadding) {
		return getBlockHeight() * (includePadding ? getPlotElementPad() : 1.0);
	}

	public Rectangle2D getMatrixBounds(boolean includePadding) {
		int height = _model.getDataMatrixEntryCount();
		int width = 0;
		double h = (double) height * getPlotElementHeight(includePadding);
		double w = (double) width * getPlotElementWidth(includePadding);
		return new Rectangle2D.Double((double) getHorizontalInset(),
				(double) getVerticalInset(), w + getHorizontalInset(), h
						+ getVerticalInset());
	}

	public int countElementsInRow(int row) {
		int matrixLen = _model.getDataMatrixEntryCount();
		if (row < matrixLen) {
			DataLine matrixRow = _model.getDataMatrixEntry(row);
			return matrixRow.getLength();
		} else {
			return 0;
		}
	}

	public int maxCountOfElementsInAllRows() {
		int maxcount = 0;
		int matrixLen = _model.getDataMatrixEntryCount();
		for (int i = 0; i < matrixLen; ++i) {
			int rowcount = countElementsInRow(i);
			maxcount = (maxcount < rowcount) ? rowcount : maxcount;
		}
		return maxcount;

	}

	public Rectangle2D getRowBounds(int row, boolean uniform_width) {
		int matrixLen = _model.getDataMatrixEntryCount();
		if (row < matrixLen) {
			double x = (double) getHorizontalInset();
			int colcount = uniform_width ? maxCountOfElementsInAllRows()
					: countElementsInRow(row);
			double h = getPlotElementHeight(INCLUDE_PADDING);
			double w = colcount * getPlotElementWidth(INCLUDE_PADDING);
			double y = ((double) getVerticalInset()) + (h * (double) row);
			return new Rectangle2D.Double(x, y, w, h);
		} else {
			return null;
		}
	}

	public Rectangle2D getMatrixBounds() {
		return getMatrixBounds(INCLUDE_PADDING);
	}

	public int getLabelGap() {
		return _labelGap;
	}

	public Rectangle2D calculateComponentBounds() {
		Rectangle2D bounds = getMatrixBounds();
		Rectangle lbounds = new Rectangle();
		for (JLabel label : _rowLabels) {
			label.getBounds(lbounds);
			if (label.getParent() != null) {
				bounds.add(new Rectangle2D.Double(lbounds.getX(), lbounds
						.getY(), lbounds.getWidth(), lbounds.getHeight()));
			}
		}
		return bounds;
	}

	

	private interface LabelIterator {
		public void iterate(int idx, Rectangle2D frame, String labelText);
	}

	private void forEachLabel(LabelIterator iter) {
		for (int i = 0; iter != null && i < _model.getDataMatrixEntryCount(); ++i) {
			iter.iterate(i, getRowBounds(i, true), _model.getDataMatrixEntry(i).getTitle());
		}
	}



	public interface SVGDocumentUpdater {
		public void update(SVGDocument document);
	}
	
	private Runner getRunner(){
		 return _runner;
	}

	public void updateDocument(final SVGDocumentUpdater updater) {
		if (updater != null && _document != null) {

			getRunner().invokeLater(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							updater.update(_document);

						}
					});
		}
	}
	private Rectangle2D getLabelBounds(){
		Rectangle2D matrixBounds = getMatrixBounds();
		return new Rectangle2D.Double(matrixBounds.getMaxX(), matrixBounds.getMinY(), 400.0, matrixBounds.getHeight());
	}
	private Rectangle2D getGraphBounds(){
		return getMatrixBounds().createUnion(getLabelBounds());
	}
	private void loadSVG() {
		final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		updateDocument(new SVGDocumentUpdater() {

			@Override
			public void update(final SVGDocument document) {
				// TODO Auto-generated method stub

				Element svgRoot = document.getDocumentElement();
				NodeList existingChildren = svgRoot.getChildNodes();
				
				for(NodeListIterator ecIter = new NodeListIterator(existingChildren); ecIter.hasNext();){
					Node i= (Node)ecIter.next();
					svgRoot.removeChild(i);
				}
				Rectangle2D graphBounds = OrionPlotPanel.this.getGraphBounds();
				svgRoot.setAttributeNS(null, "width", Integer.toString((int)graphBounds.getWidth()));
				svgRoot.setAttributeNS(null, "height", Integer.toString((int)graphBounds.getHeight()));
				final Element labelG = document.createElementNS(svgNS, "g");
				labelG.setAttributeNS(null, "id", "labels");
				svgRoot.appendChild(labelG);
				forEachLabel(new LabelIterator() {
					@Override
					public void iterate(int idx, final Rectangle2D frame,
							String labelText) {
						// TODO Auto-generated method stub
						Element label = SVGUtilities.createText(document, (float)frame.getMaxX(),(float) frame.getCenterY(), labelText);
						
						label.setAttributeNS(null, "stroke", "black");

						labelG.appendChild(label);
					}
				});
				final Element cellG = document.createElementNS(svgNS, "g");
				cellG.setAttributeNS(null, "id", "cells");
				svgRoot.appendChild(cellG);
				AffineTransform tx = getCellTransform();
				Point2D hSrc = new Point2D.Double();
				Point2D hOrigin = new Point2D.Double();
				final Element headerG = document.createElementNS(svgNS, "g");
				svgRoot.appendChild(headerG);
				int numHeaders = _model.getHeaderCount();
				for(int i = 0; i < numHeaders; ++i){
					hSrc.setLocation(i, 0);
					tx.transform(hSrc, hOrigin);
					String header = _model.getHeader(i);
					if(header != null){
						Element label = SVGUtilities.createText(document, (float)hOrigin.getX(), (float)hOrigin.getY(), header);
						label.setAttributeNS(null, "stroke", "black");
						headerG.appendChild(label);
					}
				}

				overAllCells(new CellIterator() {
					@Override
					public void iterate(
							DataLine 			srcLine, 
							int 				row, 
							int 				cell, 
							List<Rectangle2D> 	cellRects,
							List<Integer> 		vals, 
							List<Color> 		colors
							) {
						// TODO Auto-generated method stub
						// TODO Auto-generated method stub

						for(int i = 0; i < cellRects.size(); ++i){
							Rectangle2D r = cellRects.get(i);
							Color rc = (i < colors.size()) ? colors.get(i) : Color.PINK;
							Integer rv = (i < vals.size()) ? vals.get(i) : Integer.valueOf(0);
							Element rect = document.createElementNS(svgNS, "rect");
							rect.setAttributeNS(null, "x", 		Double.toString(r.getX()));
							rect.setAttributeNS(null, "y", 		Double.toString(r.getY()));
							rect.setAttributeNS(null, "width", 	Double.toString(r.getWidth()));
							rect.setAttributeNS(null, "height", Double.toString(r.getHeight()));
							int cr = rc.getRed(), cg = rc.getGreen(), cb = rc.getBlue();
							String hex = String.format("#%02x%02x%02x", cr, cg, cb);
							rect.setAttributeNS(null, "fill", hex);
							rect.setAttributeNS(null, "stroke", "black");
							cellG.appendChild(rect);
						}
					}
				});
			}
		});
		
	}
	public void reload(){
		loadSVG();
	}
	public Document getDocument(){
		return _document;
	}
/*	
	private void reloadPlot() {
		           loadSVG();
		           Rectangle2D r = calculateComponentBounds();
		           System.out.format("resizing window to %d %d\n", (int) r.getWidth(),
		                           (int) r.getHeight());
		           setPreferredSize(new Dimension((int) r.getWidth(), (int) r.getHeight()));       revalidate();
		   }
		   */

	public void setModel(OrionModel model){
		model = model == null ? new OrionModel.DefaultOrionModel() : model;
		OrionModel _oldModel = _model;
		_model = model;
           DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
           String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
           Document doc = impl.createDocument(svgNS, "svg", null);
           this.setDocument(doc);
           this.firePropertyChange("matrix", _oldModel.getDataMatrix(), _model.getDataMatrix());;
	}

	public OrionModel getModel() {
		// TODO Auto-generated method stub
		return _model;
	}

	

	
}
