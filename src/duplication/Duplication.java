package duplication;


/**
 * This class keeps the data of duplications. Every duplication is simple a list
 * of duplication occurrences, with a specific occurrence which is the
 * occurrence.
 * 
 * @author Davood Mazinanian
 * 
 */
public abstract class Duplication {

	protected final DuplicationType duplicationType;

	public Duplication(DuplicationType type) {
		duplicationType = type;
	}

	public DuplicationType getType() {
		return duplicationType;
	}

	@Override
	public String toString() {
		return "Duplication of type " + duplicationType;
	}
	
}
