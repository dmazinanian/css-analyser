package ca.concordia.cssanalyser.cssmodel.selectors;

/**
 * Selector1 > Selector2
 * @author Davood Mazinanian
 */
public class ChildSelector extends DescendantSelector {

	public ChildSelector(SingleSelector parent, SingleSelector child) {
		super(parent, child);
	}
	
	@Override
	public String toString() {
		return parentSelector + " > " + childSelector;
	}

	@Override
	public Selector clone() {
		return new ChildSelector(getParentSelector(), getChildSelector());
	}
	
}
