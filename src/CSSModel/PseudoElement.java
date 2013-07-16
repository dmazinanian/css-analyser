package CSSModel;
/**
 * Represents pseudo elements like ::selector
 * @author Davood Mazinanian
 */
public class PseudoElement {
	
	private String name;

	public PseudoElement(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
