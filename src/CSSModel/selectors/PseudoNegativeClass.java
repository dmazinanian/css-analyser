package CSSModel.selectors;


/**
 * Specific kind of PseudoClass which is :not(selector)
 * 
 * @author Davood Mazinanian
 *
 */
public class PseudoNegativeClass extends PseudoClass {

	private final AtomicElementSelector insideSelector;
	
	public PseudoNegativeClass(AtomicElementSelector selector) {
		super("not", selector.toString());
		insideSelector = selector;
	}

	public AtomicElementSelector getSelector() {
		return insideSelector;
	}
	
}
