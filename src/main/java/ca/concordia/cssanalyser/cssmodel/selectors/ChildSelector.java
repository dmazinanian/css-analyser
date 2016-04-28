package ca.concordia.cssanalyser.cssmodel.selectors;

/**
 * Selector1 > Selector2
 * @author Davood Mazinanian
 */
public class ChildSelector extends DescendantSelector {

	public ChildSelector(BaseSelector parent, SimpleSelector child) {
		super(parent, child, '>');
	}
	
	@Override
	public String toString() {
		return getLeftHandSideSelector() + " " + String.valueOf(getCombinatorCharacter()) + " " + getRightHandSideSelector(); 
	}

	@Override
	public ChildSelector clone() {
		ChildSelector newOne = new ChildSelector(getParentSelector().clone(), getChildSelector().clone());
		newOne.setLocationInfo(getLocationInfo());
		newOne.addMediaQueryLists(mediaQueryLists);
		return newOne;
	}
	
}
