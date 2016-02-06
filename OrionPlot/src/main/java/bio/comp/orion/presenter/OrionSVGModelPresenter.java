package bio.comp.orion.presenter;

import java.awt.Color;
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
import bio.comp.orion.presenter.OrionModelPresenter.BaseOrionModelPresenter.LegendMetrics;
import bio.comp.orion.ui.SVGDocumentUpdater;

public class OrionSVGModelPresenter extends
		BaseOrionModelPresenter<SVGDocument> implements SVGDocumentUpdater {
	static String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

	public static SVGDocument createEmptySVGDocument() {
		final DOMImplementation impl = SVGDOMImplementation
				.getDOMImplementation();
		final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		final Document doc = impl.createDocument(svgNS, "svg", null);
		return (SVGDocument) doc;
	}


	public OrionSVGModelPresenter(final OrionModel model) {
		_model = model;
	}

	public static Point2D pointByTranslatingPoint(Point2D pt, double dx,
			double dy) {
		return new Point2D.Double(pt.getX() + dx, pt.getY() + dy);
	}
	public static Element createSVGRect(final SVGDocument document,
			Rectangle2D r, Color fillC) {
		return createSVGRect(document, r, Color.BLACK,fillC);

	}

	public static Element createSVGRect(final SVGDocument document,
			Rectangle2D r, Color strokeC, Color fillC) {
		final Element rect = document.createElementNS(svgNS, "rect");
		rect.setAttributeNS(null, "x", Double.toString(r.getX()));
		rect.setAttributeNS(null, "y", Double.toString(r.getY()));
		rect.setAttributeNS(null, "width", Double.toString(r.getWidth()));
		rect.setAttributeNS(null, "height", Double.toString(r.getHeight()));

		if(fillC != null){
            rect.setAttributeNS(null, "fill", Colors.toHexString(fillC));
		}
		if(strokeC != null){
             rect.setAttributeNS(null, "stroke", Colors.toHexString(strokeC));
		}
		return rect;
	}

	public static Element createSVGLabel(final SVGDocument document,
			Point2D origin, String text, Color rc) {
		final Element label = SVGUtilities.createText(document,
				(float) origin.getX(), (float) origin.getY(), text);
		label.setAttributeNS(null, "stroke", Colors.toHexString(rc));
		return label;
	}

	@Override
	public void present(final SVGDocument document) {
		final Element svgRoot = document.getDocumentElement();
		final NodeList existingChildren = svgRoot.getChildNodes();
		for (int i = 0; i < existingChildren.getLength(); ++i) {
			final Node in = existingChildren.item(i);
			svgRoot.removeChild(in);
		}
		final Rectangle2D graphBounds = OrionSVGModelPresenter.this
				.getGraphBounds();
		svgRoot.setAttributeNS(null, "width",
				Integer.toString((int) graphBounds.getWidth()));
		svgRoot.setAttributeNS(null, "height",
				Integer.toString((int) graphBounds.getHeight()));
		final Element labelG = document.createElementNS(svgNS, "g");
		labelG.setAttributeNS(null, "id", "labels");
		svgRoot.appendChild(labelG);
		forEachLabel(new LabelIterator() {
			@Override
			public void iterate(final int idx, final Rectangle2D frame,
					final String labelText) {

				labelG.appendChild(createSVGLabel(document, new Point2D.Double(
						(float) frame.getMaxX(),
						(float) (frame.getMaxY() - (frame.getHeight() / 4.0))),
						labelText, Color.BLACK));
			}
		});
		final Element cellG = document.createElementNS(svgNS, "g");
		cellG.setAttributeNS(null, "id", "cells");
		svgRoot.appendChild(cellG);

		final Element headerG = document.createElementNS(svgNS, "g");
		svgRoot.appendChild(headerG);
		final int numHeaders = _model.getHeaderCount();
		for (int i = 0; i < numHeaders; ++i) {
			final Rectangle2D cellRect = getCellBounds(0, i);
			final MatrixHeader header = _model.getMatrixHeader(i);
			if (header != null) {
				Point2D origin = new Point2D.Double(
						(cellRect.getMinX() + (cellRect.getWidth() / 4.0f)),
						(cellRect.getMinY() - (getVerticalInset() / 4.0)));
				final Element label = createSVGLabel(document, origin,
						header.getName(), header.getColor());
				headerG.appendChild(label);
			}
		}

		overAllCells(new CellIterator() {
			@Override
			public void iterate(final DataLine srcLine, final int row,
					final int cell, final List<Rectangle2D> cellRects,
					final List<Integer> vals, final List<Color> colors) {

				for (int i = 0; i < cellRects.size(); ++i) {
					final Rectangle2D r = cellRects.get(i);
					final Color rc = (i < colors.size()) ? colors.get(i)
							: Color.PINK;
					final Integer val = (i < vals.size()) ? vals.get(i) : 1;
                    Element re = createSVGRect(document, r, rc);
                    re.setAttributeNS(null, "id", val.toString());
					cellG.appendChild(re);
				}
			}
		});

		if (_showLegend) {
			final Element legendG = document.createElementNS(svgNS, "g");
			Rectangle2D matrixBounds = getMatrixBounds(INCLUDE_PADDING);
			final double dy = LegendMetrics.NEXT_Y, dx = LegendMetrics.NEXT_X;
			Point2D legendOrigin = new Point2D.Double(matrixBounds.getMinX(),
					matrixBounds.getMaxY() + dy);
			legendG.appendChild(createSVGLabel(document, legendOrigin, "Legend", Color.BLACK));
			final Point2D start = pointByTranslatingPoint(legendOrigin, 0, 25);
			forEachColorMapEntry(new ColorMapIterator() {

				@Override
				public void iterate(int idx, Object key, Color value) {
					Point2D rowOrigin = pointByTranslatingPoint(start, 0, dy * idx);
					Point2D textOrigin = pointByTranslatingPoint(rowOrigin, LegendMetrics.PAD, 15);
					Point2D colorOrigin = pointByTranslatingPoint(rowOrigin,
							dx, 0);
					Element label = createSVGLabel(document, textOrigin,
							key.toString(), Color.BLACK);
					Element color = createSVGRect(document,
							new Rectangle2D.Double(colorOrigin.getX(),
									colorOrigin.getY(),
									LegendMetrics.CELL_WIDTH,
									LegendMetrics.CELL_HEIGHT), value);
					color.setAttributeNS(null, "id", key.toString());
					legendG.appendChild(label);
					legendG.appendChild(color);

				}
			});
			svgRoot.appendChild(legendG);
		}

	}

	protected void registerListeners(final SVGDocument _document) {
		final Element elt = _document.getElementById("an-id");
		final EventTarget t = (elt != null) ? (EventTarget) elt : null;
		t.addEventListener("SVGLoad", new EventListener() {

			@Override
			public void handleEvent(final Event evt) {

				System.out.format("handleEvent/%s/(%s)", "SVGLoad", evt);
			}
		}, false);
		t.addEventListener("click", new EventListener() {

			@Override
			public void handleEvent(final Event evt) {

				System.out.format("handleEvent/%s/(%s)", "click", evt);
			}
		}, false);
	}

	@Override
	public void update(final SVGDocument document) {
		present(document);
	}

}
