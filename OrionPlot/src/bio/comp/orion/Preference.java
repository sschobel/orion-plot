package bio.comp.orion;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public enum Preference{
	OPEN_PREVIOUS_SESSION_ON_START(	"bio.comp.orion.span.sessions", 	Boolean.TRUE, 	Boolean.class	), 
	SAVE_FOLDER(					"bio.comp.orion.file.save.folder", 	".", 			String.class	),
	OPEN_FOLDER(					"bio.comp.orion.file.open.folder", 	".", 			String.class	),
	PREVIOUS_SESSION_PLOT(			"bio.comp.orion.last.open.plot", 	null, 			String.class	),
	IMG_FOLDER(						"bio.comp.orion.img.folder", 		".", 			String.class	);

	private final String _key;
	private final Object _default;
	private final Class<?> _type;
	private abstract static class Accessor<T>{
		public <RT> RT get(Preferences prefs, Preference key, Class<RT>returnType){
			if(Preconditions.notNull(prefs, key, returnType).met()){
				T default1 = (T) key.getDefault(returnType);
				T val = doGet(prefs, key.getKey(), default1);
				if(val != null){
					return returnType.cast(val);
				}
			}
			return null;
		}
		public <VT extends T> void set(Preferences prefs, Preference pref, VT value){
			if(Preconditions.notNull(prefs, pref, value).met()){
				doSet(prefs, pref.getKey(), value);
			}
		}
		//assume both arguments are not null
		public abstract T doGet(Preferences prefs, String key, T def);
		public abstract void doSet(Preferences prefs, String key, T value);
	}
	private static Map<Class<?>, Accessor<?>> PREFERENCE_ACCESSORS;
	static{
		PREFERENCE_ACCESSORS= new HashMap<Class<?>,Accessor<?>>(){
			private static final long serialVersionUID = 1L;
		{
			put(String.class, new Accessor<String>(){

				@Override
				public String doGet(Preferences prefs, String key, String def) {
					// TODO Auto-generated method stub
					return prefs.get(key, def);
				}

				@Override
				public void doSet(Preferences prefs, String key, String value) {
					// TODO Auto-generated method stub
					prefs.put(key, value);
					
				}
			});
			put(Float.class, new Accessor<Float>(){

				@Override
				public Float doGet(Preferences prefs, String key, Float def) {
					// TODO Auto-generated method stub
					return prefs.getFloat(key, def);
				}

				@Override
				public void doSet(Preferences prefs, String key, Float value) {
					// TODO Auto-generated method stub
					prefs.putFloat(key, value);
				}
				
			});
			put(Double.class, new Accessor<Double>(){

				@Override
				public Double doGet(Preferences prefs, String key, Double def) {
					// TODO Auto-generated method stub
					return prefs.getDouble(key, def);
				}

				@Override
				public void doSet(Preferences prefs, String key, Double value) {
					// TODO Auto-generated method stub
					
				}
				
			});
			put(Integer.class, new Accessor<Integer>(){

				@Override
				public Integer doGet(Preferences prefs, String key, Integer def) {
					// TODO Auto-generated method stub
					return prefs.getInt(key, def);
				}

				@Override
				public void doSet(Preferences prefs, String key, Integer value) {
					// TODO Auto-generated method stub
					prefs.putInt(key, value);
				}

			});
			put(Long.class, new Accessor<Long>(){

				@Override
				public Long doGet(Preferences prefs, String key, Long def) {
					// TODO Auto-generated method stub
					return prefs.getLong(key, def);
				}

				@Override
				public void doSet(Preferences prefs, String key, Long value) {
					// TODO Auto-generated method stub
					prefs.putLong(key, value);
				}

				
			});
			put(Boolean.class, new Accessor<Boolean>(){

				@Override
				public Boolean doGet(Preferences prefs, String key, Boolean def) {
					// TODO Auto-generated method stub
					return prefs.getBoolean(key, def);
				}

				@Override
				public void doSet(Preferences prefs, String key, Boolean value) {
					// TODO Auto-generated method stub
					prefs.putBoolean(key, value);
				}

				
			});
			put(byte[].class, new Accessor<byte[]>(){

				@Override
				public byte[] doGet(Preferences prefs, String key, byte[] def) {
					// TODO Auto-generated method stub
					return prefs.getByteArray(key, def);
				}

				@Override
				public void doSet(Preferences prefs, String key, byte[] value) {
					// TODO Auto-generated method stub
					prefs.putByteArray(key, value);
				}

				
			});
		}};
		
	}
	Preference(String key, Object someDefault, Class<?> valueType){
		_key = key;
		_default = someDefault;
		_type = valueType;
	}
	public String getKey(){
		return _key;
	}
	public Object getDefault(){
		return _default;
	}
	public <T> T getDefault(Class<T> ofType){
		if(ofType.equals(_type)){
			return ofType.cast(_default);
		}
		else{
			return null;
		}
	}
	public Object getDefaultObject(){
		return _default;
	}
	public Class<?> getType(){
		return _type;
	}

	public <T> T getPreference(Preferences prefs, Class<T> ofType){
		if(ofType == null){
			ofType = ((Class<T>) _type);
		}
		T tvalue = null;
		if(Preconditions.notNull(prefs, ofType).met()){
             Accessor<?> accessor = PREFERENCE_ACCESSORS.get(ofType);
             tvalue = accessor != null ? accessor.get(prefs, this, ofType) : null;
		}
		return tvalue;
	}
	public static boolean flush(Preferences prefs){
		boolean flushed = true;
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			flushed = false;
		}
		return flushed;
		
	}
	public <T> void setPreference(Preferences prefs, T tvalue){

		if(Preconditions.notNull(prefs).met()){
			if(tvalue == null){
				prefs.remove(getKey());
			}
			else{
				Accessor<T>accessor = (Accessor<T>) PREFERENCE_ACCESSORS.get(tvalue.getClass());
				if(accessor != null){
					accessor.set(prefs, this, tvalue);
				}
				
			}
		}
		
	}
}
