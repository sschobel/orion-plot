package bio.comp.orion.presenter;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.fop.svg.SVGUtilities;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;

import bio.comp.orion.model.Colors;
import bio.comp.orion.model.DataLine;
import bio.comp.orion.model.MatrixHeader;
import bio.comp.orion.model.OrionModel;
import bio.comp.orion.presenter.OrionModelPresenter.BaseOrionModelPresenter;
import bio.comp.orion.ui.SVGDocumentUpdater;



public class OrionSVGModelPresenter extends
		BaseOrionModelPresenter<SVGDocument> implements SVGDocumentUpdater {
	public static SVGDocument createEmptySVGDocument() {
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		Document doc = impl.createDocument(svgNS, "svg", null);
		return (SVGDocument)doc;
	}

	static String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

	public OrionSVGModelPresenter(OrionModel model) {
		_model = model;
	}

	protected void registerListeners(SVGDocument _document) {
		Element elt = _document.getElementById("an-id");
		EventTarget t = (elt != null) ? (EventTarget) elt : null;
		t.addEventListener("SVGLoad", new EventListener() {

			@Override
			public void handleEvent(Event evt) {
				
				System.out.format("handleEvent/%s/(%s)", "SVGLoad", evt);
			}
		}, false);
		t.addEventListener("click", new EventListener() {

			@Override
			public void handleEvent(Event evt) {
				
				System.out.format("handleEvent/%s/(%s)", "click", evt);
			}
		}, false);
	}

	@Override
	public void present(final SVGDocument document) {
		Element svgRoot = document.getDocumentElement();
		NodeList existingChildren = svgRoot.getChildNodes();
		for (int i = 0; i < existingChildren.getLength(); ++i) {
			Node in = existingChildren.item(i);
			svgRoot.removeChild(in);
		}
		Rectangle2D graphBounds = OrionSVGModelPresenter.this.getGraphBounds();
		svgRoot.setAttributeNS(null, "width",
				Integer.toString((int) graphBounds.getWidth()));
		svgRoot.setAttributeNS(null, "height",
				Integer.toString((int) graphBounds.getHeight()));
		final Element labelG = document.createElementNS(svgNS, "g");
		labelG.setAttributeNS(null, "id", "labels");
		svgRoot.appendChild(labelG);
		forEachLabel(new LabelIterator() {
			@Override
			public void iterate(int idx, final Rectangle2D frame,
					String labelText) {
				
				Element label = SVGUtilities.createText(document,
						(float) frame.getMaxX(), (float) (frame.getMaxY() - (frame.getHeight()/4.0)),
						labelText);

				label.setAttributeNS(null, "stroke", "black");

				labelG.appendChild(label);
			}
		});
		final Element cellG = document.createElementNS(svgNS, "g");
		cellG.setAttributeNS(null, "id", "cells");
		svgRoot.appendChild(cellG);
		
		final Element headerG = document.createElementNS(svgNS, "g");
		svgRoot.appendChild(headerG);
		int numHeaders = _model.getHeaderCount();
		for (int i = 0; i < numHeaders; ++i) {
			Rectangle2D cellRect = getCellBounds(0, i);
			MatrixHeader header = _model.getMatrixHeader(i);
			if (header != null) {
				Element label = SVGUtilities.createText(document,
						(float)( cellRect.getMinX() + (cellRect.getWidth()/4.0f)), (float) (cellRect.getMinY() - (getVerticalInset()/4.0)), header.getName());
				label.setAttributeNS(null, "stroke", Colors.toHexString(header.getColor()));
				headerG.appendChild(label);
			}
		}

		overAllCells(new CellIterator() {
			@Override
			public void iterate(DataLine srcLine, int row, int cell,
					List<Rectangle2D> cellRects, List<Integer> vals,
					List<Color> colors) {

				for (int i = 0; i < cellRects.size(); ++i) {
					Rectangle2D r = cellRects.get(i);
					Color rc = (i < colors.size()) ? colors.get(i) : Color.PINK;
					Integer rv = (i < vals.size()) ? vals.get(i) : Integer
							.valueOf(0);
					Element rect = document.createElementNS(svgNS, "rect");
					rect.setAttributeNS(null, "x", Double.toString(r.getX()));
					rect.setAttributeNS(null, "y", Double.toString(r.getY()));
					rect.setAttributeNS(null, "width",
							Double.toString(r.getWidth()));
					rect.setAttributeNS(null, "height",
							Double.toString(r.getHeight()));
					
					rect.setAttributeNS(null, "fill", Colors.toHexString(rc));
					rect.setAttributeNS(null, "stroke", "black");
					cellG.appendChild(rect);
				}
			}
		});
	}

	@Override
	public void update(SVGDocument document) {
		present(document);
	}

}
