package CSSModel;

public class PseudoNegativeClass extends PseudoClass {

	private final Selector insideSelector;
	
	public PseudoNegativeClass(String name, Selector selector) {
		super(name, selector.toString());
		insideSelector = selector;
	}

	public Selector getSelector() {
		return insideSelector;
	}
	
}
