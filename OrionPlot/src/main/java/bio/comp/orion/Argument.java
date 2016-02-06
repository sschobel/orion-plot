package bio.comp.orion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

class Argument extends Object{	
	protected String _name;
	protected List<String> _values;
	protected String _desc;

	Argument(String name, String desc){
		_values = new ArrayList<String>();
		_name = name;
		_desc = desc;
	}
	public void addValues(String... values){
		_values.addAll(Arrays.asList(values));
	}
	public void addValue(String value){
		this.addValues(value);
		_valuesArr = null;
	}
	public String getDescription(){
		return _desc;
	}
	public void setDescription(String desc){
		_desc = desc;
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
	public String toHelpString(){
		String description = Strings.isNullOrEmpty(_desc) ? "" : "- " + _desc;

		return "[ " + _name + " ]" + description;
	}
}