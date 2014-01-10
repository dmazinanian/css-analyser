package ca.concordia.cssanalyser.cssmodel.selectors;


/**
 * Specific kind of PseudoClass which is :not(selector)
 * 
 * @author Davood Mazinanian
 *
 */
public class NegationPseudoClass extends PseudoClass {

	private final SingleSelector insideSelector;
	
	public NegationPseudoClass(SingleSelector selector) {
		super("not", selector.toString());
		insideSelector = selector;
	}

	public SingleSelector getSelector() {
		return insideSelector;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((insideSelector == null) ? 0 : insideSelector.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NegationPseudoClass other = (NegationPseudoClass) obj;
		if (insideSelector == null) {
			if (other.insideSelector != null)
				return false;
		} else if (!insideSelector.equals(other.insideSelector))
			return false;
		return true;
	}
	
}
