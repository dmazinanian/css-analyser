package CSSModel;

import java.util.ArrayList;
import java.util.List;

public class Declaration {

	private final String property;
	private final List<DeclarationValue> values;
	private final Selector belongsTo;
	private final int lineNumber;
	private final int colNumber;
	private final boolean isImportant;
	private int hashCode = -1;

	public Declaration(String property, List<DeclarationValue> values, Selector belongsTo, boolean important) {
		this(property, values, belongsTo, -1, -1, important);
	}

	public Declaration(String property, List<DeclarationValue> values, Selector belongsTo,
			int fileLineNumber, int fileColNumber, boolean important) {
		this.property = property;
		this.values = values;
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

	public List<DeclarationValue> getValues() {
		return values;
	}

	public boolean valuesEqual(Declaration otherDeclaration) {
		if (values.size() != otherDeclaration.values.size()) 
			return false;
		// What a subtle logical error could be here if we don't do values.containsAll(otherDeclaration.values));
		// consider [border-width: 17 17] and [border-width: 17 28]
		// First copy:
		List<DeclarationValue> temp = new ArrayList<>(otherDeclaration.values);
		for (DeclarationValue value : values) {
			if (temp.contains(value))
				temp.remove(value);
			else
				return false;
		}
		return true;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}

	public int getColumnNumber() {
		return colNumber;
	}

	@Override
	public String toString() {
		StringBuilder valueString = new StringBuilder("");
		for (DeclarationValue v : values)
			valueString.append(v + " ");
		valueString.delete(valueString.length()-1, valueString.length());
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
		if (obj.getClass() != getClass())
			return false;
		Declaration otherDeclaration = (Declaration) obj;
		if (property.length() != otherDeclaration.property.length())
			return false;
		for (int i = 0; i < property.length(); i++)
			if (property.charAt(i) != otherDeclaration.property.charAt(i))
				return false;
		return (valuesEqual(otherDeclaration));
	}

	@Override
	public int hashCode() {
		if (hashCode == -1) {
			hashCode = 17;
			hashCode = 31 * hashCode + property.hashCode();
			int h = 0;
			for (DeclarationValue v : values)
				h += v.hashCode();
			hashCode = 31 * hashCode + h;
		}
		return hashCode;
	}

}
