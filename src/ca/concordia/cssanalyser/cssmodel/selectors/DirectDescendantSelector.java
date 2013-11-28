package ca.concordia.cssanalyser.cssmodel.selectors;

/**
 * Selector1 > Selector2
 * @author Davood Mazinanian
 */
public class DirectDescendantSelector extends DescendantSelector {

	public DirectDescendantSelector(AtomicSelector parent, AtomicSelector child) {
		super(parent, child);
	}
	
	@Override
	public String toString() {
		return parentSelector + " > " + childSelector;
	}

	@Override
	public Selector clone() {
		return new DirectDescendantSelector(getParentSelector(), getChildSelector());
	}
	
}
