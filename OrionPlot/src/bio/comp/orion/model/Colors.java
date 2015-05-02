package bio.comp.orion.model;

import java.awt.Color;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

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
}
