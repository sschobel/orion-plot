package bio.comp.orion.model;

import java.awt.Color;
import java.util.List;

import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

public class MatrixHeaders {
	
	public static final double BLOCK_WIDTH = 20.0;
	public static final double BLOCK_HEIGHT = 20.0;
	private static class BaseMatrixHeader implements MatrixHeader{
		private String _name;
		private Color _color;
		private Double _width;
		private Double _height;
		private BaseMatrixHeader(String name, Color color, Double width){
			this(name, color, width, null);
		}
		private BaseMatrixHeader(String name, Color color, Double width, Double height){
			_name = name;
			_color = color;
			_width = width;
			_height = height;
		}
		@Override
		public String getName() {
			
			return _name;
		}
		@Override
		public Color getColor(Color defaultColor) {
			
			return (_color != null) ? _color : defaultColor;
		}
		@Override
		public Double getWidth(Double defaultWidth) {
			
			return (_width != null) ? _width : defaultWidth;
		}
		@Override
		public Color getColor() {
			
			return getColor(Color.black);
		}
		@Override
		public Double getWidth() {
			
			return getWidth(MatrixHeaders.BLOCK_WIDTH);
		}
		public Double getHeight(Double _default){
			return (_height != null) ? _height : _default;
		}
		@Override
		public Double getHeight(){
			return getHeight(MatrixHeaders.BLOCK_HEIGHT);
		}
		@Override
		public String toString(){
			return MoreObjects.toStringHelper("DefaultMatrixHeader")
			.add("name", 	getName())
			.add("color",	getColor())
			.add("width", 	getWidth())
			.add("height", 	getHeight())
			.omitNullValues().toString();
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
	public static MatrixHeader createHeader(String name, Color color, Double width, Double height){
		return new BaseMatrixHeader(name, color, width, height);
	}
	public static MatrixHeader createHeader(JSONObject obj){
		if(obj == null){
			return new BaseMatrixHeader(null, null, null);
		}
		else{
			String name = obj.optString("name");
			Color color= Colors.fromHexString.apply(obj.optString("color"));
			Double width = obj.optDouble("width", BLOCK_WIDTH);
			return new BaseMatrixHeader(name, color, width);
		}
	}
	
	public static final Function<String, MatrixHeader> fromString = new Function<String, MatrixHeader>(){

		@Override
		public MatrixHeader apply(String input) {
			Splitter entrySplitter = 
			Splitter.on(",").trimResults().omitEmptyStrings();
			Splitter kvSplitter = Splitter.on(":").trimResults().omitEmptyStrings();
			String name = null; Double width = null; Color color = null;
			int i = 0;
			for(String entry : entrySplitter.splitToList(input)){
				List<String> kvPair = kvSplitter.splitToList(entry);
				String key = kvPair.size() > 0 ? kvPair.get(0) : null;
				String val = kvPair.size() > 1 ? kvPair.get(1) : null;
				switch (kvPair.size()) {
				case 1:{
					val = key;
					switch (i) {
					case 0:
						name = val;
						break;
					case 1:
						width = Double.valueOf(val);
						break;
					case 2:
						color = Colors.fromHexString(val);
						break;
					default:
						break;
					}
				}
					break;
				case 2:{
					if(key.equals("name")){
						name = val;
					}
					else if(key.equals("width")){
						width = Double.valueOf(val);
					}
					else if (key.equals("color")){
						color = Colors.fromHexString(val);
					}
				}
					break;
				default:
					throw new RuntimeException(String.format("Invalid header format %s, too many :", entry));
				}
				++i;
			}
			return MatrixHeaders.createHeader(name, color, width);
		}
		
	};
}
