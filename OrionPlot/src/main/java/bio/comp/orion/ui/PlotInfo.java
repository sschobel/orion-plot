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
			
			return _shape.contains(arg0);
		}

		@Override
		public boolean contains(Rectangle2D arg0) {
			
			return _shape.contains(arg0);
		}

		@Override
		public boolean contains(double x, double y) {
			
			return _shape.contains(x, y);
		}

		@Override
		public boolean contains(double x, double y, double w,
				double h) {
			
			return _shape.contains(x, y, w, h);
		}

		@Override
		public Rectangle getBounds() {
			
			return _shape.getBounds();
		}

		@Override
		public Rectangle2D getBounds2D() {
			
			return _shape.getBounds2D();
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at) {
			
			return _shape.getPathIterator(at);
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at, double flatness) {
			
			return _shape.getPathIterator(at, flatness);
		}

		@Override
		public boolean intersects(Rectangle2D test) {
			
			return _shape.intersects(test);
		}

		@Override
		public boolean intersects(double x, double y, double w,
				double h) {
			
			return _shape.intersects(x, y, w, h);
		}

		@Override
		public void draw(Graphics2D g2) {
			
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
			
			return calculateBounds().contains(p);
		}

		@Override
		public boolean contains(Rectangle2D r) {
			
			return calculateBounds().contains(r.getX(), r.getY(), r.getWidth(), r.getHeight() );
		}

		@Override
		public boolean contains(double x, double y) {
			
			return calculateBounds().contains(x, y);
		}

		@Override
		public boolean contains(double x, double y, double w, double h) {
			
			return calculateBounds().contains(x, y, w, h);
		}

		@Override
		public Rectangle getBounds() {
			
			Rectangle2D _r2d2 = calculateBounds();
			return new Rectangle( (int)_r2d2.getX(), (int)_r2d2.getY(), (int)_r2d2.getWidth(), (int)_r2d2.getHeight());
		}

		@Override
		public Rectangle2D getBounds2D() {
			
			return calculateBounds();
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at) {
			
			return null;
		}

		private class ComplexPathIterator implements PathIterator{

			@SuppressWarnings("unused")
			final private PlotShape[] _itershapes;
			@SuppressWarnings("unused")
			int idx = 0;
			@SuppressWarnings("unused")
			PathIterator current = null;
			
			public ComplexPathIterator(PlotShape[] shapes){
				_itershapes = shapes;
				
			}
			
			@Override
			public int currentSegment(float[] coords) {
				
				return 0;
			}

			@Override
			public int currentSegment(double[] coords) {
				
				return 0;
			}

			@Override
			public int getWindingRule() {
				
				return 0;
			}

			@Override
			public boolean isDone() {
				
				return false;
			}

			@Override
			public void next() {
				
				
			}
			
		}
		
		@Override
		public PathIterator getPathIterator(AffineTransform at, double flatness) {
			
			return new ComplexPathIterator(_shapes);
		}

		@Override
		public boolean intersects(Rectangle2D r) {
			
			return calculateBounds().intersects(r);
		}

		@Override
		public boolean intersects(double x, double y, double w, double h) {
			
			return calculateBounds().intersects(x, y, w, h);
		}

		@Override
		public void draw(Graphics2D g2) {
			
			for(PlotShape shape : _children){
				shape.draw(g2);
			}
		}
		
	}
	
	private PlotShape[] _shapes;
	
	
	
	private PropertyChangeSupport _pcs = new PropertyChangeSupport(this);
	public void addPropertyChangeListener(String prop, PropertyChangeListener listener){ _pcs.addPropertyChangeListener(prop, listener); }
	public void removePropertyChangeListener(String prop, PropertyChangeListener listener) { _pcs.removePropertyChangeListener(prop, listener); }
  
}
