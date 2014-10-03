/**
 * 
 */
package bio.comp.orion.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @author sethschobel
 *
 */
public class PlotInfo {
  
	public interface PlotShape extends Shape{
		void draw(Graphics2D g2);
		
	}
	
	
	
	public class SimplePlotShape implements PlotShape {

		protected Shape _shape;
		protected Color _color;
		
		@Override
		public boolean contains(Point2D arg0) {
			// TODO Auto-generated method stub
			return _shape.contains(arg0);
		}

		@Override
		public boolean contains(Rectangle2D arg0) {
			// TODO Auto-generated method stub
			return _shape.contains(arg0);
		}

		@Override
		public boolean contains(double x, double y) {
			// TODO Auto-generated method stub
			return _shape.contains(x, y);
		}

		@Override
		public boolean contains(double x, double y, double w,
				double h) {
			// TODO Auto-generated method stub
			return _shape.contains(x, y, w, h);
		}

		@Override
		public Rectangle getBounds() {
			// TODO Auto-generated method stub
			return _shape.getBounds();
		}

		@Override
		public Rectangle2D getBounds2D() {
			// TODO Auto-generated method stub
			return _shape.getBounds2D();
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at) {
			// TODO Auto-generated method stub
			return _shape.getPathIterator(at);
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at, double flatness) {
			// TODO Auto-generated method stub
			return _shape.getPathIterator(at, flatness);
		}

		@Override
		public boolean intersects(Rectangle2D test) {
			// TODO Auto-generated method stub
			return _shape.intersects(test);
		}

		@Override
		public boolean intersects(double x, double y, double w,
				double h) {
			// TODO Auto-generated method stub
			return _shape.intersects(x, y, w, h);
		}

		@Override
		public void draw(Graphics2D g2) {
			// TODO Auto-generated method stub
			Color oldColor = g2.getColor();
			g2.setColor(_color);
			g2.draw(_shape);
			g2.setColor(oldColor);
		}
	}
	
	public class ComplexPlotShape implements PlotShape {

		private Rectangle2D _calculatedBounds = null;
		private PlotShape[] _children = new PlotShape[0];
		
		private ComplexPlotShape(PlotShape[] shapes){
			_children = shapes;
		}
		
		ComplexPlotShape CreateFrom(PlotShape... shapes){
			return new ComplexPlotShape(shapes);
			
		}
		
		private Rectangle2D calculateBounds(){
			if(_calculatedBounds == null){
				for(PlotShape child : _children){
					_calculatedBounds = (_calculatedBounds == null) ? child.getBounds2D() : _calculatedBounds.createUnion(child.getBounds2D());
				}
			}
			return _calculatedBounds;
		}
		
		@Override
		public boolean contains(Point2D p) {
			// TODO Auto-generated method stub
			return calculateBounds().contains(p);
		}

		@Override
		public boolean contains(Rectangle2D r) {
			// TODO Auto-generated method stub
			return calculateBounds().contains(r.getX(), r.getY(), r.getWidth(), r.getHeight() );
		}

		@Override
		public boolean contains(double x, double y) {
			// TODO Auto-generated method stub
			return calculateBounds().contains(x, y);
		}

		@Override
		public boolean contains(double x, double y, double w, double h) {
			// TODO Auto-generated method stub
			return calculateBounds().contains(x, y, w, h);
		}

		@Override
		public Rectangle getBounds() {
			// TODO Auto-generated method stub
			Rectangle2D _r2d2 = calculateBounds();
			return new Rectangle( (int)_r2d2.getX(), (int)_r2d2.getY(), (int)_r2d2.getWidth(), (int)_r2d2.getHeight());
		}

		@Override
		public Rectangle2D getBounds2D() {
			// TODO Auto-generated method stub
			return calculateBounds();
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at) {
			// TODO Auto-generated method stub
			return null;
		}

		private class ComplexPathIterator implements PathIterator{

			final private PlotShape[] _itershapes;
			int idx = 0;
			PathIterator current = null;
			
			public ComplexPathIterator(PlotShape[] shapes){
				_itershapes = shapes;
				
			}
			
			@Override
			public int currentSegment(float[] coords) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int currentSegment(double[] coords) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getWindingRule() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public boolean isDone() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void next() {
				// TODO Auto-generated method stub
				
			}
			
		}
		
		@Override
		public PathIterator getPathIterator(AffineTransform at, double flatness) {
			// TODO Auto-generated method stub
			return new ComplexPathIterator(_shapes);
		}

		@Override
		public boolean intersects(Rectangle2D r) {
			// TODO Auto-generated method stub
			return calculateBounds().intersects(r);
		}

		@Override
		public boolean intersects(double x, double y, double w, double h) {
			// TODO Auto-generated method stub
			return calculateBounds().intersects(x, y, w, h);
		}

		@Override
		public void draw(Graphics2D g2) {
			// TODO Auto-generated method stub
			for(PlotShape shape : _children){
				shape.draw(g2);
			}
		}
		
	}
	
	private int[][] _plot;
	private PlotShape[] _shapes;
	
	
	
	private PropertyChangeSupport _pcs = new PropertyChangeSupport(this);
	public void addPropertyChangeListener(String prop, PropertyChangeListener listener){ _pcs.addPropertyChangeListener(prop, listener); }
	public void removePropertyChangeListener(String prop, PropertyChangeListener listener) { _pcs.removePropertyChangeListener(prop, listener); }
  
}
