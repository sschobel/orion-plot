package bio.comp.orion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Joiner;

class Argument extends Object{	
	protected String _name;
	protected List<String> _values;
	Argument(String name){
		_values = new ArrayList<String>();
		_name = name;
	}
	public void addValues(String... values){
		_values.addAll(Arrays.asList(values));
	}
	public void addValue(String value){
		this.addValues(value);
		_valuesArr = null;
	}
	public String toString(){
		return String.format("%s : %s", _name, Joiner.on(", ").join(_values));
	}
	private String[] _valuesArr;
	public String[] values(){
		if(_valuesArr == null){
			_valuesArr = _values.toArray(new String[0]);
		}
		return _valuesArr;
	}
}