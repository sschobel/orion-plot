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
import bio.comp.orion.presenter.OrionSVGModelPresenter;

import com.sun.xml.internal.ws.util.xml.NodeListIterator;

public class OrionPlotPanel extends JSVGCanvas {
	private interface Runner {
		public void invokeAndWait(Runnable r) throws InterruptedException;

		public void invokeLater(Runnable r);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 507178960111041135L;
	private SVGDocument _document;
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

	// private Shape[] _shapes = { _defaultShape, _twoValueShapeAlpha,
	// _twoValueShapeBeta };
	/*
	 * private Shape getDefaultShape() { if (__defaultShape == null) {
	 * __defaultShape = new Rectangle2D.Double(0, 0, getBlockWidth(),
	 * getBlockHeight()); } return __defaultShape; }
	 * 
	 * private Shape getTwoValueShapeAlpha() { if (__twoValueShapeAlpha == null)
	 * { __twoValueShapeAlpha = new Rectangle2D.Double(0, 0, getBlockWidth(),
	 * getBlockHeight() / 2); } return __twoValueShapeAlpha; }
	 * 
	 * private Shape getTwoValueShapeBeta() { if (__twoValueShapeBeta == null) {
	 * __twoValueShapeBeta = new Rectangle2D.Double(0, getBlockHeight() / 2,
	 * getBlockWidth(), getBlockHeight() / 2); } return __twoValueShapeBeta; }
	 */

	private static GraphicsEnvironment ge = GraphicsEnvironment
			.getLocalGraphicsEnvironment();

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

	private static class RunnableRunner implements Runner {

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

		this.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
		this.addSVGLoadEventDispatcherListener(new SVGLoadEventDispatcherListener() {

			@Override
			public void svgLoadEventDispatchStarted(
					SVGLoadEventDispatcherEvent e) {
				// TODO Auto-generated method stub
				_document = OrionPlotPanel.this.getSVGDocument();
				_runner = new Runner() {
					RunnableQueue batikrunner = OrionPlotPanel.this
							.getUpdateManager().getUpdateRunnableQueue();

					@Override
					public void invokeAndWait(Runnable r)
							throws InterruptedException {
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

	private Runner getRunner() {
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

	private void loadSVG() {
		OrionSVGModelPresenter svgPresenter = new OrionSVGModelPresenter(
				getModel());
		updateDocument(svgPresenter);

	}

	public void reload() {
		loadSVG();
	}

	public Document getDocument() {
		return _document;
	}

	/*
	 * private void reloadPlot() { loadSVG(); Rectangle2D r =
	 * calculateComponentBounds();
	 * System.out.format("resizing window to %d %d\n", (int) r.getWidth(), (int)
	 * r.getHeight()); setPreferredSize(new Dimension((int) r.getWidth(), (int)
	 * r.getHeight())); revalidate(); }
	 */

	public void setModel(OrionModel model) {
		model = model == null ? new OrionModel.DefaultOrionModel() : model;
		OrionModel _oldModel = _model;
		_model = model;
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		Document doc = impl.createDocument(svgNS, "svg", null);
		this.setDocument(doc);
		this.firePropertyChange("matrix", _oldModel.getDataMatrix(),
				_model.getDataMatrix());
		;
	}

	public OrionModel getModel() {
		// TODO Auto-generated method stub
		return _model;
	}

}
