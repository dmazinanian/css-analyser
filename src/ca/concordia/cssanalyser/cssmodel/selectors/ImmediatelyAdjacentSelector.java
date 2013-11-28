package ca.concordia.cssanalyser.cssmodel.selectors;

/**
 * selector + selector
 * @author Davood Mazinanian
 *
 */
public class ImmediatelyAdjacentSelector extends IndirectAdjacentSelector {

	public ImmediatelyAdjacentSelector(AtomicSelector firstSelector, AtomicSelector secondSelector) {
		super(firstSelector, secondSelector);
	}

	@Override
	public String toString() {
		return beforeMainSelector + " + " + mainSelector;
	}
	
	@Override
	public Selector clone() {
		return new ImmediatelyAdjacentSelector(getFirstSelector(), getSecondSelector());
	}
}
