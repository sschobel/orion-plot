package bio.comp.orion.ui;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGLoadEventDispatcherEvent;
import org.apache.batik.swing.svg.SVGLoadEventDispatcherListener;
import org.apache.batik.util.RunnableQueue;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

import bio.comp.orion.model.OrionModel;
import bio.comp.orion.presenter.OrionSVGModelPresenter;

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
	

	private static GraphicsEnvironment ge = GraphicsEnvironment
			.getLocalGraphicsEnvironment();

	

	private static class RunnableRunner implements Runner {

		@Override
		public void invokeAndWait(Runnable r) throws InterruptedException {
			
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				throw new InterruptedException(e.toString());
			}

		}

		@Override
		public void invokeLater(Runnable r) {
			
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
				
				_document = OrionPlotPanel.this.getSVGDocument();
				_runner = new Runner() {
					RunnableQueue batikrunner = OrionPlotPanel.this
							.getUpdateManager().getUpdateRunnableQueue();

					@Override
					public void invokeAndWait(Runnable r)
							throws InterruptedException {
						
						batikrunner.invokeAndWait(r);
					}

					@Override
					public void invokeLater(Runnable r) {
						
						batikrunner.invokeLater(r);

					}

				};
				OrionPlotPanel.this.loadSVG();
			}

			@Override
			public void svgLoadEventDispatchFailed(SVGLoadEventDispatcherEvent e) {
				

			}

			@Override
			public void svgLoadEventDispatchCompleted(
					SVGLoadEventDispatcherEvent e) {
				

			}

			@Override
			public void svgLoadEventDispatchCancelled(
					SVGLoadEventDispatcherEvent e) {
				

			}
		});
		System.err.printf("%s\n", ge.getDefaultScreenDevice()
				.getDefaultConfiguration());
	}

	

	private Runner getRunner() {
		return _runner;
	}

	public void updateDocument(final SVGDocumentUpdater updater) {
		if (updater != null && _document != null) {

			getRunner().invokeLater(new Runnable() {

				@Override
				public void run() {
					
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

	

	public void setModel(OrionModel model) {
		model = model == null ? new OrionModel.DefaultOrionModel() : model;
		OrionModel _oldModel = _model;
		_model = model;
		Document doc = OrionSVGModelPresenter.createEmptySVGDocument();
		this.setDocument(doc);
		this.firePropertyChange("matrix", _oldModel.getDataMatrix(),
				_model.getDataMatrix());
		;
	}

	public OrionModel getModel() {
		
		return _model;
	}

}
