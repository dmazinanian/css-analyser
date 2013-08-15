package CSSModel.selectors;

/**
 * Represents CSS "selector1 selector2" selectors
 * selector1 and selector2 are of type {@link AtomicSelector}
 * so for example, for selector1 selector2 selector 3, we have two selectors
 * one of which is again a {@link DescendantSelector} and anoter is an {@link DescendantSelector}
 * @author Davood Mazinanian
 */
public class DescendantSelector extends AtomicSelector {
	
	protected final AtomicSelector parentSelector;
	protected final AtomicSelector childSelector; 
	
	public DescendantSelector(AtomicSelector parent, AtomicSelector child) {
		parentSelector = parent;
		childSelector = child;
	}
	
	/**
	 * Returns the parent selector (the selector on the left hand side
	 * of a descendant selector)
	 * @return
	 */
	public AtomicSelector getParentSelector() {
		return parentSelector;
	}
	
	/**
	 * Returns the child selector (the selector on the right hand side
	 * of a descendant selector)
	 * @return
	 */
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
