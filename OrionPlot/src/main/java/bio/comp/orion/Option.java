package bio.comp.orion;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

class Option extends Argument{
	private String _shortName;
	private List<String> _choices;
	public Option(String name, String shortName, String desc){
		super(name, desc);
		_name = name;
		_shortName = shortName;
		_choices = new ArrayList<String>();
	}

	public Option(String name, String shortName){
		this(name, shortName, null);
	}
	public Option(String name){
		this(name, name.substring(0, 1));
	}
	public boolean isValidChoice(String argChoice){
		if(hasLimitedChoices()){
			return _choices.contains(argChoice);
		}
		else{
			return true;
		}
	}
	public void addChoice(String choice){
		_choices.add(choice);
	}
	public boolean hasLimitedChoices(){
		return _choices.size() > 0;
	}
	public String[] validChoices(){
		if(hasLimitedChoices()){
			return _choices.toArray(new String[]{});
		}
		else{
			return new String[] { "*" };
		}
	}
	public String shortName(){
		return _shortName;
	}
	public static boolean isOption(String arg){
		return arg != null && arg.startsWith("--") && arg.length() > 2;
	}
	public static boolean isShortOption(String arg){
		return arg != null && arg.startsWith("-") && arg.length() == 2;
	}
	public static String getOptionName(String option){
		if(isOption(option)){
			return option.substring(2);
		}
		else if(isShortOption(option)){
			return option.substring(1);
		}
		else{
			return option;
		}
	}
	public boolean matchesArg(String arg){
		if(isShortOption(arg) && _shortName.equals(arg.substring(1))){
			return true;
		}
		else if(isOption(arg) && _name.equals(arg.substring(2))){
			return true;
		}
		else{
			return false;
		}
	}
	
	public String getName(){
		return _name;
	}
	public String toString(){
		return String.format("option '%s' : %s", _name, Joiner.on(", ").join(_values));
	}
	public String toHelpString(){
		String lon = "-" + getName();
		String shopt = (_shortName != null) ? "[ -" + _shortName + " ]" : "";
		String validOpts = (_choices == null || _choices.size() ==0 )? "" : ": ( " + Joiner.on(", ").join(_choices) + " )";
		String description = Strings.isNullOrEmpty(_desc) ? "" : " - " + _desc;
		return String.format("%s %s%s%s", lon, shopt,description, validOpts);
	}
}