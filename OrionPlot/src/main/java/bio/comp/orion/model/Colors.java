package bio.comp.orion.model;

import java.awt.Color;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Function;
import com.google.common.base.Strings;

public final class Colors {
	private Colors(){
		
	}
	
	
	private static Map<Class<?>, ColorParser> colorParsers;
	{
		colorParsers.put(String.class, new ColorParser() {

			@Override
			public Color parseFrom(Object anObj) {
				
				StringTokenizer st = new StringTokenizer((String)anObj, ",");
				float[] comps = new float[4];
				for (int i = 0; i < comps.length; i++) {
					if(st.hasMoreTokens()){
						float comp = (float)Integer.parseInt(st.nextToken());
						comps[i] = comp/(float)255;
					}
					else if (i == 0){
						comps[i] = 0;
					}
					else if (i > 0 && i < 3){
						comps[i] = comps[i-1];
					}
					else {
						comps[i] = 1.0f;
					}
				}
				return new Color(comps[0], comps[1], comps[2], comps[3]);
			}
		});
	}
	static final Color createColor(Object obj, Color _default){
		for(Entry<Class<?>, ColorParser> entry : colorParsers.entrySet()){
			if(entry.getKey().isAssignableFrom(obj.getClass())){
				return ((ColorParser) entry.getValue()).parseFrom(obj);
			}
		}
		return _default;
	}
	static final Color createColor(Object obj){
		return createColor(obj, null);
	}
	private static final String[] HEX_PREFIXES = new String[]{"0x", "x", "#" };
	public static final String toHexString(Color rc){
		if(rc == null){
			return "";
		}
		int cr = rc.getRed(), cg = rc.getGreen(), cb = rc.getBlue();
		String hex = String.format("#%02x%02x%02x", cr, cg, cb);
		return hex;
	}
	public static final Color fromHexString(String input){
					if(Strings.isNullOrEmpty(input)){
						return null;
					}
					for(String prefix : HEX_PREFIXES){
						String common = Strings.commonPrefix(input, prefix);
						if(common.length() > 0){
							input = input.substring(common.length());
						}
					}
					try{
					boolean hasAlpha = input.length() > 6;
					Integer val = Integer.valueOf(input, 16);
					return new Color(val.intValue(), hasAlpha);
					}catch(NumberFormatException e){
						Logger.getLogger("errors").log(Level.ALL, String.format("Failure to convert hex string %s  into integer", input));
					}
					return null;
	}
	public static final Function<String, Color> fromHexString = new Function<String, Color>(){

		@Override
		public Color apply(String input) {
			return Colors.fromHexString(input);
		}
		
	};
}
