package CSSModel.selectors;
/**
 * Represents CSS3 pseudo elements like ::selector.
 * Have a look at {@link PseudoClass <code>PseudoClass</code>} for 
 * more information
 * 
 * @author Davood Mazinanian
 * 
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
