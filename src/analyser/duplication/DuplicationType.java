package analyser.duplication;

public enum DuplicationType {
	/*IDENTICAL_SELECTOR("Identical Selector"), 
	IDENTICAL_PROPERTY_AND_VALUE("Identical property and value"),
	IDENTICAL_VALUE("Identical value"), 
	IDENTICAL_EFFECT("Identical effect on a same selector"),
	OVERRIDEN_PROPERTY("Overruden property for a selector");*/
	
	/**
	 * A pair of selectors having a common set of lexically identical declarations except
	 * for variations in the order of declarations, whitespace, and inline comments 
	 */
	TYPE_I("Lexically identical declarations"),
	
	/**
	 * A pair of selectors having a common set of one-to-one equivalent declarations.
	 * Two declarations are considered equivalent if they declare the same property
	 * with an equivalent value. For example, value black is equivalent with rgb(0, 0, 0)
	 */
	TYPE_II("Equivalent values"),
	
	/**
	 * A pair of selectors (A, B) where a set of x declarations in A is equivalent with
	 * a set of y declarations in B (x ≠ y). For example, shorthand property border can
	 * be used to replace the individual properties border-width, border-style, and border-color 
	 */
	TYPE_III("Swappable shorthand and non-shorthand declarations"),
	
	/**
	 * A pair of selectors, which may share or not identical or equivalent declarations,
	 * but select exactly the same set of DOM elements.
	 */
	TYPE_IV("Identical elements in DOM");

	String stringRepresentation;

	DuplicationType(String value) {
		stringRepresentation = value;
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
