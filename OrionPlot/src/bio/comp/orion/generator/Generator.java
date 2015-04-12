package bio.comp.orion.generator;

import java.util.HashMap;
import java.util.Map;

public interface Generator {

public static class Result<T>{
	Class<T> _resultType;
	T _result;
	Map<String, Object>_otherInfo;
	@SuppressWarnings("unchecked")
	public Result(T result){
		this((Class<T>)result.getClass());		
		_result = result;
	}
	public Result(Class<T> typeOfResult){
		_resultType =typeOfResult;
		_otherInfo = new HashMap<String, Object>();
	}
	public T get(){
		return _result;
	}
	public  void  setOtherInfo(String key, Object other){
		_otherInfo.put(key, other);
	}
	public Object getOtherInfo(String key){
		return _otherInfo.containsKey(key) ? _otherInfo.get(key) : null;
	}
	public boolean isValid(){
		return true;
	}
	public Error<T> getAsError(){
		return null;
	}
	public static <T> Result<T> CreateValidResult(T result){
		return new Result<T>(result);
	}
	
}
public static class Error<T> extends Result<T>{
	int _code;
	String _description;
	public String getDescription(){
		return _description;
	}
	public int getErrorCode(){
		return _code;
	}
	public Error(String desc, int code, Class<T>typeOfResult){
		super(typeOfResult);
		_description = desc;
		_code = code;
		_resultType = typeOfResult;
	}
	public Error(Class<T>typeOfResult){
		this("", 0, typeOfResult);
	}
	public static <T> Error<T> ErrorForResultOfType(Class<T> typeOfResult, int code, String description){
		return new Error<T>(typeOfResult);
	}
	public boolean isValid(){
		return false;
	}
	public Error<T> getAsError(){
		return this;
	}
}
}
