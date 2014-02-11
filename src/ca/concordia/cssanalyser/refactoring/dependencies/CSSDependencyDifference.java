package ca.concordia.cssanalyser.refactoring.dependencies;

public class CSSDependencyDifference<E> {
	
	enum CSSDependencyDifferenceType {
		MISSING,
		RIVERSED,
		ADDED
	}
	
	private final CSSDependencyDifferenceType type;
	private final CSSDependency<E> dependency;
	
	public CSSDependencyDifference(CSSDependencyDifferenceType type, CSSDependency<E> dependency) {
		this.type = type;
		this.dependency = dependency;
	}
	
	@Override
	public String toString() {
		String toReturn = "";
		switch (type) {
			case ADDED:
				toReturn = "Added dependency";
				break;
			case MISSING:
				toReturn = "Missing dependency";
				break;
			case RIVERSED:
				toReturn = "Riveresed dependency";
				break;
		}
		return toReturn + ": " + dependency;
	}
	
}
