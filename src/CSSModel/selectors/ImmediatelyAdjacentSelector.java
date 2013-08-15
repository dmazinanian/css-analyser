package CSSModel.selectors;

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
}
