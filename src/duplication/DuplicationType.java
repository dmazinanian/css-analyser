package duplication;

public enum DuplicationType {
	IDENTICAL_SELECTOR("Identical Selector"), 
	IDENTICAL_PROPERTY_AND_VALUE("Identical property and value"),
	IDENTICAL_VALUE("Identical value"), 
	IDENTICAL_EFFECT("Identical effect on a same selector"),
	OVERRIDEN_PROPERTY("Overruden property for a selector");

	String stringRepresentation;

	DuplicationType(String value) {
		stringRepresentation = value;
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
