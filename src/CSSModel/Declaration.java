package CSSModel;

import java.util.List;

public class Declaration {

	private final String property;
	private final List<String> value;
	private final Selector belongsTo;
	private final int lineNumber;
	private final int colNumber;
	private final boolean isImportant;

	public Declaration(String property, List<String> value, Selector belongsTo, boolean important) {
		this(property, value, belongsTo, -1, -1, important);
	}

	public Declaration(String property, List<String> value, Selector belongsTo,
			int fileLineNumber, int fileColNumber, boolean important) {
		this.property = property;
		this.value = value;
		this.belongsTo = belongsTo;
		lineNumber = fileLineNumber;
		colNumber = fileColNumber;
		isImportant = important;
	}
	
	public boolean isImportant() {
		return isImportant;
	}

	public Selector getSelector() {
		return belongsTo;
	}

	public String getProperty() {
		return property;
	}

	public List<String> getValues() {
		return value;
	}

	public boolean valuesEquals(Declaration otherDeclaration) {
		return (value.size() == otherDeclaration.value.size() &&
				value.containsAll(otherDeclaration.value));
	}
	
	public int getLineNumber() {
		return lineNumber;
	}

	public int getColumnNumber() {
		return colNumber;
	}

	@Override
	public String toString() {
		String valueString = "";
		for (String v : value)
			valueString += v + " ";
		valueString = valueString.substring(0, valueString.length()-1);
		return String.format("%s: %s", property, valueString);
	}

	/**
	 * The equals method for Declaration only takes the values for 
	 * property: value into account. It doesn't take the Selector 
	 * to which this declaration belongs into account 
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Declaration))
			return false;
		Declaration otherDiclaration = (Declaration) obj;
		return (property.equals(otherDiclaration.property) &&
				valuesEquals(otherDiclaration));
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + property.hashCode();
		int h = 0;
		for (String v : value)
			h += v.hashCode();
		result = 31 * result + h;
		return result;
	}

}
