package CSSModel.selectors;


/**
 * Specific kind of PseudoClass which is :not(selector)
 * 
 * @author Davood Mazinanian
 *
 */
public class PseudoNegativeClass extends PseudoClass {

	private final AtomicSelector insideSelector;
	
	public PseudoNegativeClass(AtomicSelector selector) {
		super("not", selector.toString());
		insideSelector = selector;
	}

	public AtomicSelector getSelector() {
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
		PseudoNegativeClass other = (PseudoNegativeClass) obj;
		if (insideSelector == null) {
			if (other.insideSelector != null)
				return false;
		} else if (!insideSelector.equals(other.insideSelector))
			return false;
		return true;
	}
	
}
