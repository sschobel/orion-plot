package bio.comp.orion.model;

import java.awt.Color;

import org.json.JSONObject;

public class MatrixHeaders {
	
	public static final double BLOCK_WIDTH = 20.0;
	public static final double BLOCK_HEIGHT = 20.0;
	private static class BaseMatrixHeader implements MatrixHeader{
		private String _name;
		private Color _color;
		private Double _width;
		private BaseMatrixHeader(String name, Color color, Double width){
			_name = name;
			_color = color;
			_width = width;
		}
		@Override
		public String getLineName() {
			// TODO Auto-generated method stub
			return _name;
		}
		@Override
		public Color getLineColor(Color defaultColor) {
			// TODO Auto-generated method stub
			return (_color != null) ? _color : defaultColor;
		}
		@Override
		public Double getLineWidth(Double defaultWidth) {
			// TODO Auto-generated method stub
			return (_width != null) ? _width : defaultWidth;
		}
		@Override
		public Color getLineColor() {
			// TODO Auto-generated method stub
			return getLineColor(null);
		}
		@Override
		public Double getLineWidth() {
			// TODO Auto-generated method stub
			return getLineWidth(null);
		}
	}
	
	public static MatrixHeader createHeader(String name){
		MatrixHeader header = new BaseMatrixHeader(name, null, null);
		return header;
	}
	public static MatrixHeader createHeader(String name, Color color){
		return new BaseMatrixHeader(name, color, null);
	}
	public static MatrixHeader createHeader(String name, Color color, Double width){
		return new BaseMatrixHeader(name, color, width);
	}
	public static MatrixHeader createHeader(JSONObject obj){
		if(obj == null){
			return new BaseMatrixHeader(null, null, null);
		}
		else{
			String name = obj.optString("name");
			Color color= Colors.createColor(obj.optString("color"), Color.BLACK);
			Double width = obj.optDouble("width", BLOCK_WIDTH);
			return new BaseMatrixHeader(name, color, width);
		}
	}
}
