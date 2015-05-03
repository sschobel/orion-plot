package bio.comp.orion.presenter;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import bio.comp.orion.model.DataLine;
import bio.comp.orion.model.MatrixHeaders;
import bio.comp.orion.model.OrionModel;

public interface OrionModelPresenter<P> {
	void present(P presentee);

	public interface CellIterator {
		public void iterate(DataLine sourceLine, int row, int cell,
				List<Rectangle2D> subcells, List<Integer> values,
				List<Color> colors);
	}

	public interface LabelIterator {
		public void iterate(int idx, Rectangle2D frame, String labelText);
	}

	public abstract static class BaseOrionModelPresenter<P> implements
			OrionModelPresenter<P> {
		private static final int DEFAULT_LABEL_GAP = 5;
		private int _labelGap = DEFAULT_LABEL_GAP;
		protected OrionModel _model;

		public double getPlotElementPad() {

			return 5;
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

		protected void forEachLabel(LabelIterator iter) {
			for (int i = 0; iter != null
					&& i < _model.getDataMatrixEntryCount(); ++i) {
				iter.iterate(i, getRowBounds(i, true), _model
						.getDataMatrixEntry(i).getTitle());
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
				Rectangle2D first = getCellBounds(row, 0);
				Rectangle2D last = getCellBounds(row, colcount-1);
				double w = last.getMaxX() - first.getMinX();
				double h = Math.max(first.getHeight(), last.getHeight());
				return new Rectangle2D.Double(first.getMinX(), first.getMinY(), w, h);
			} else {
				return null;
			}
		}

		public int getLabelGap() {
			return _labelGap;
		}


		public double getBlockWidth(int col) {
			return _model.getMatrixHeader(col).getWidth();
		}


		public double getBlockHeight(int col) {
			return _model.getMatrixHeader(col).getHeight();
		}
		public double getBlockHeight(){
			return getBlockHeight(-1);
		}
		public double getBlockWidth(){
			return getBlockWidth(-1);
		}

		public double getPlotElementWidth(int col, boolean includePadding) {
			return getBlockWidth(col)
					* (includePadding ? getPlotElementPad() : 1.0);
		}

		public double getPlotElementHeight(int col, boolean includePadding) {
			return getBlockHeight(col)
					* (includePadding ? getPlotElementPad() : 1.0);
		}

		public static final boolean INCLUDE_PADDING = true;
		public static final boolean EXCLUDE_PADDING = false;

		public Rectangle2D getMatrixBounds(boolean includePadding) {
			int height = _model.getDataMatrixEntryCount();
			int width = _model.getMaxDataLineLength();
			double h = (double) height * getPlotElementHeight(-1, includePadding);
			Rectangle2D rowBounds = getRowBounds(0, false);
			double w = rowBounds.getWidth();
			return new Rectangle2D.Double((double) getHorizontalInset(),
					(double) getVerticalInset(), w + getHorizontalInset(), h
							+ getVerticalInset());
		}

		private double vInset = 15.0;
		private double hInset = 15.0;

		public double getVerticalInset() {
			return vInset;
		}

		public double getHorizontalInset() {
			return hInset;
		}

		protected Rectangle2D getMatrixBounds() {
			return getMatrixBounds(INCLUDE_PADDING);
		}

		protected Rectangle2D getLabelBounds() {
			Rectangle2D matrixBounds = getMatrixBounds();
			return new Rectangle2D.Double(matrixBounds.getMaxX(),
					matrixBounds.getMinY(), 400.0, matrixBounds.getHeight());
		}

		protected Rectangle2D getGraphBounds() {
			return getMatrixBounds().createUnion(getLabelBounds());
		}

		protected Rectangle2D getCellBounds(int row, int col) {
			double w = getBlockWidth(col), h = getBlockHeight(col);
			double y = (h + getPlotElementPad()) * row;
			double x = getPlotElementPad() * col;
			for(int i = col-1; i >= 0; i--){
				x += (getBlockWidth(i) + getPlotElementPad());
			}
			return new Rectangle2D.Double(x + getHorizontalInset(), y + getVerticalInset(), w, h);
		}

		protected List<Color> colorsForCell(int row, int cell) {
			DataLine line = _model.getDataMatrixEntry(row);
			ArrayList<Color> colors = new ArrayList<Color>();
			if (line != null) {
				List<Integer> cellValues = line.getValuesAt(cell);
				for (int i = 0; cellValues != null && i < cellValues.size(); ++i) {
					Color subcellColor = _model
							.colorForValue(cellValues.get(i));
					colors.add(subcellColor != null ? subcellColor : Color.PINK);
				}

			}

			return colors;
		}

		private Rectangle2D partitionRectangleX(Rectangle2D src,
				Rectangle2D part, double ratio) {
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

		public void overAllCells(CellIterator iter) {
			Point2D cellOrigin = new Point2D.Double();
			
			int matrixLen = _model.getDataMatrixEntryCount();
			for (int row_i = 0; iter != null && row_i < matrixLen; row_i++) {
				DataLine line = _model.getDataMatrixEntry(row_i);
				for (int cell_j = 0; iter != null && cell_j < line.getLength(); cell_j++) {
					List<Integer> cellValues = line.getValuesAt(cell_j);
					List<Color> cellColors = colorsForCell(row_i, cell_j);
					cellOrigin.setLocation(0, 0);
					Rectangle2D cellRect = getCellBounds(row_i, cell_j);
					List<Rectangle2D> cellRects = splitRectangle(cellRect,
							cellValues.size());
					iter.iterate(line, row_i, cell_j, cellRects, cellValues,
							cellColors);
				}
			}
		}
	}
}
