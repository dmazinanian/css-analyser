package CSSModel.selectors;

/**
 * selector1 ~ selector2
 * @author Davood Mazinanian
 */
public class IndirectAdjacentSelector extends AtomicSelector {
	
	protected final AtomicSelector beforeMainSelector;
	protected final AtomicSelector mainSelector;
	
	public IndirectAdjacentSelector(AtomicSelector firstSelector, AtomicSelector secondSelector) {
		beforeMainSelector = firstSelector;
		mainSelector = secondSelector;
	}
	
	public AtomicSelector getFirstSelector() {
		return beforeMainSelector;
	}
	
	public AtomicSelector getSecondSelector() {
		return mainSelector;
	}
	
	@Override
	public String toString() {
		return beforeMainSelector + " ~ " + mainSelector;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IndirectAdjacentSelector))
			return false;
		IndirectAdjacentSelector otherObj = (IndirectAdjacentSelector)obj;
		if (beforeMainSelector.equals(otherObj.beforeMainSelector) &&
				mainSelector.equals(otherObj.mainSelector))
			return true;
		return false;
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + (beforeMainSelector == null ? 0 : beforeMainSelector.hashCode());
		result = 31 * result + (mainSelector == null ? 0 : mainSelector.hashCode());
		return result;
	}
}
