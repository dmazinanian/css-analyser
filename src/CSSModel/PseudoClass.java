package CSSModel;

/**
 * Represents pseudo classes like :after
 * 
 * @author Davood Mazinanian
 */
public class PseudoClass {

	private final String name;

	// Null denotes that this pseudo-class doesn't have an initial value
	private String value = null;

	public PseudoClass(String name) {
		this.name = name;
	}

	public PseudoClass(String name, String value) {
		this(name);
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PseudoClass))
			return false;
		PseudoClass otherPseudoClass = (PseudoClass)obj;
		if (name.equals(otherPseudoClass.name) &&
				(value == otherPseudoClass.value))
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		String ret = name;
		if (value != null)
			ret += String.format("(%s)", value);
		return ret;
	}

}
