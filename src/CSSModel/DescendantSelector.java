package CSSModel;

/**
 * selector1 selector2
 * @author Davood Mazinanian
 */
public class DescendantSelector extends AtomicSelector {
	
	protected final AtomicSelector parentSelector;
	protected final AtomicSelector childSelector; 
	
	public DescendantSelector(AtomicSelector parent, AtomicSelector child) {
		parentSelector = parent;
		childSelector = child;
	}
	
	public AtomicSelector getParentSelector() {
		return parentSelector;
	}
	
	public AtomicSelector getChildSelector() {
		return childSelector;
	}
	
	@Override
	public String toString() {
		return parentSelector + " " + childSelector;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DescendantSelector))
			return false;
		DescendantSelector otherObj = (DescendantSelector)obj;
		return (parentSelector.equals(otherObj.parentSelector) &&
				childSelector.equals(otherObj.childSelector));
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + (parentSelector == null ? 0 : parentSelector.hashCode());
		result = 31 * result + (childSelector == null ? 0 : childSelector.hashCode());
		return result;
	}
	
}
