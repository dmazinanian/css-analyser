package ca.concordia.cssanalyser.cssmodel.selectors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents pseudo classes like :hover
 * Note that in CSS2.1, there is no deference between pseudo classes
 * and pseudo elements in terms of the prefix, both use ":" (like p:before).
 * In CSS3, we've got "::" for pseudo elements, like "::"
 * 
 * @author Davood Mazinanian
 */
public class PseudoClass {

	private final String name;

	// Null denotes that this pseudo-class doesn't have an initial value
	private String value = null;

	static String[] unsupportedPseudoClassesArray = new String[] {
		"link", "active", "hover", "visited", "focus", 
		"first-letter", "first-line", "before", "after", "target",
		"enabled", "disabled"
	};
	
	static Set<String> unsupportedPseudoClasses = new HashSet<>(Arrays.asList(unsupportedPseudoClassesArray));

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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : (":" + value).hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PseudoClass other = (PseudoClass) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String ret = name;
		if (value != null)
			ret += String.format("(%s)", value);
		return ret;
	}

	/**
	 * There is no XPath equivalence for these pseudo classes:
	 * (make sure to refer to ca.concordia.cssanalyser.cssmodel.selectors.PseudoElement for 
	 * a clear explanation about PseudoElements and PseudoClasses) 
	 */
	public static boolean isPseudoclassWithNoXpathEquivalence(String name) {
		return unsupportedPseudoClasses.contains(name);
	}
	
	public PseudoClass clone() {
		return new PseudoClass(name, value);
	}

	public boolean isPseudoclassWithNoXpathEquivalence() {
		return isPseudoclassWithNoXpathEquivalence(this.name);
	}

}
