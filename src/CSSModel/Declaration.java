package CSSModel;

public class Declaration {

	private final String property;
	private final String value;
	private final Selector belongsTo;
	private final int lineNumber;
	private final int colNumber;
	private final boolean isImportant;

	public Declaration(String property, String value, Selector belongsTo, boolean important) {
		this(property, value, belongsTo, -1, -1, important);
	}

	public Declaration(String property, String value, Selector belongsTo,
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

	public String getValue() {
		return value;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getColumnNumber() {
		return colNumber;
	}

	@Override
	public String toString() {
		return String.format("%s: %s", property, value);
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
		//if (property == null && otherDiclaration.property != null)
		//	return false;
		//if (value == null && otherDiclaration.value != null)
		//	return false;
		return (property.equals(otherDiclaration.property) &&
				value.equals(otherDiclaration.value));
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + property.hashCode();
		result = 31 * result + value.hashCode();
		return result;
	}

}
