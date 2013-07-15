package CSSModel;

/**
 * selector ~ selector
 * @author Davood Mazinanian
 *
 */
public class IndirectAdjacentSelector extends ImmediatelyAdjacentSelector {

	public IndirectAdjacentSelector(AtomicSelector firstSelector, AtomicSelector secondSelector) {
		super(firstSelector, secondSelector);
	}

	@Override
	public String toString() {
		return beforeMainSelector + " ~ " + mainSelector;
	}
}
