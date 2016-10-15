package ca.concordia.cssanalyser.cssmodel.selectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.LocationInfo;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSDependencyDetector;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;


public class GroupingSelector extends Selector implements Collection<BaseSelector> {

	private Set<BaseSelector> listOfBaseSelectors;
	private int hashCode = -1;
	private int selectorHashCode = -1;

	public GroupingSelector() {
		this(new LocationInfo());
	}

	public GroupingSelector(LocationInfo locationInfo) {
		super(locationInfo);
		// To preserve the order of selectors as in the CSS file, we use LinkedHashSet
		listOfBaseSelectors = new LinkedHashSet<>();
	}

	public Iterable<BaseSelector> getBaseSelectors() {
		return listOfBaseSelectors;
	}

	public int getBaseSelectorsSize() {
		return listOfBaseSelectors.size();
	}

	@Override
	public void addDeclaration(Declaration declaration) {
		hashCode = -1;
		for (BaseSelector baseSelector : listOfBaseSelectors)
			baseSelector.addDeclaration(declaration);
		// The parent of the selector must be the grouping selector
		super.addDeclaration(declaration);
	}

	@Override
	public void removeDeclaration(Declaration d) {
		hashCode = -1;
		for (BaseSelector baseSelector : this.listOfBaseSelectors)
			baseSelector.removeDeclaration(d);
		super.removeDeclaration(d);
	}

	@Override
	public Iterator<BaseSelector> iterator() {
		return listOfBaseSelectors.iterator();
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("");
		for (BaseSelector baseSelector : listOfBaseSelectors)
			result.append(baseSelector + ", ");
		// Remove last , and space
		result.delete(result.length() - 2, result.length());
		return result.toString();
	}

	/**
	 * Returns true of the list of selector for both
	 * GroupingSelector are the same, regardless of the
	 * order of their selectors.
	 * @param otherSelector
	 * @return
	 */
	@Override
	public boolean selectorEquals(Selector otherSelector, boolean considerMediaQueryLists) {
		if (!generalEquals(otherSelector))
			return false;
		GroupingSelector otherObj = (GroupingSelector)otherSelector;
		if (listOfBaseSelectors.size() != otherObj.listOfBaseSelectors.size())
			return false;
		if (considerMediaQueryLists && !mediaQueryListsEqual(otherSelector))
			return false;
		List<BaseSelector> tempList = new ArrayList<>(otherObj.listOfBaseSelectors);
		for (Selector selector : listOfBaseSelectors) {
			boolean valueFound = false;
			for (int i = 0; i < tempList.size(); i++) {
				if (tempList.get(i) != null && tempList.get(i).selectorEquals(selector)) {
					valueFound = true;
					tempList.set(i, null);
					break;
				}
			}
			if (!valueFound)
				return false;
		}
		return true;
	}

	/**
     * @param considerMediaQueryLists
	 * @return hashCode that ignores location info
	 */
	@Override
	public int selectorHashCode(boolean considerMediaQueryLists) {
        if (selectorHashCode == -1) {
            selectorHashCode = 0;
            if (considerMediaQueryLists)
                selectorHashCode += mediaQueryListsHashCode();
            for (Selector selector : listOfBaseSelectors) {
                selectorHashCode += selector.selectorHashCode(considerMediaQueryLists);
            }
        }
		return selectorHashCode;
	}


	private boolean generalEquals(Object otherSelector) {
		if (otherSelector == null)
			return false;
		if (otherSelector == this)
			return true;
		if (!(otherSelector instanceof GroupingSelector))
			return false;
		return true;
	}

	/**
	 * Two grouped selectors are equal if they are
	 * in the same line and columns in the file and
	 * their atomic selectors are equal. The order of
	 * selectors are important for being equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!generalEquals(obj))
			return false;
		return obj.hashCode() == hashCode();
	}

	@Override
	public int hashCode() {
		if (hashCode == -1) {
			hashCode = 17;
			hashCode = hashCode * 31 + getLocationInfo().hashCode();
			hashCode = hashCode * 31 + listOfBaseSelectors.hashCode();
			if (mediaQueryLists != null)
				hashCode = hashCode * 31 + mediaQueryLists.hashCode();
		}
		return hashCode;
	}

	@Override
	public boolean add(BaseSelector baseSelector) {
		hashCode = -1;
		return listOfBaseSelectors.add(baseSelector);
	}

	@Override
	public boolean addAll(Collection<? extends BaseSelector> baseSelectors) {
		hashCode = -1;
		return listOfBaseSelectors.addAll(baseSelectors);
	}

	@Override
	public void clear() {
		hashCode = -1;
		listOfBaseSelectors.clear();
	}

	@Override
	public boolean contains(Object selector) {
		return listOfBaseSelectors.contains(selector);
	}

	@Override
	public boolean containsAll(Collection<?> lisstOfAtomicSelectors) {
		return listOfBaseSelectors.containsAll(lisstOfAtomicSelectors);
	}

	@Override
	public boolean isEmpty() {
		return listOfBaseSelectors.isEmpty();
	}

	@Override
	public boolean remove(Object atomicSelector) {
		hashCode = -1;
		return listOfBaseSelectors.remove(atomicSelector);
	}

	@Override
	public boolean removeAll(Collection<?> listOfAtomicSelectors) {
		hashCode = -1;
		return listOfAtomicSelectors.removeAll(listOfAtomicSelectors);
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		hashCode = -1;
		return listOfBaseSelectors.retainAll(arg0);
	}

	@Override
	public int size() {
		return listOfBaseSelectors.size();
	}

	@Override
	public Object[] toArray() {
		return listOfBaseSelectors.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return listOfBaseSelectors.toArray(arg0);
	}

	@Override
	public Selector clone() {
		GroupingSelector newOne = new GroupingSelector();
		newOne.setLocationInfo(getLocationInfo());
		newOne.listOfBaseSelectors = new LinkedHashSet<>();
		for (BaseSelector s : this.listOfBaseSelectors)
			newOne.add(s.clone());
		if (this.mediaQueryLists != null)
			newOne.addMediaQueryLists(this.mediaQueryLists);
		for (Declaration d : this.declarations.keySet())
			newOne.addDeclaration(d.clone());
		return newOne;
	}

	@Override
	public String getXPath() throws UnsupportedSelectorToXPathException {
		StringBuilder xPath = new StringBuilder();
		for (BaseSelector atomicSelector : listOfBaseSelectors)
			xPath.append(atomicSelector.getXPath() + " | ");
		if (xPath.length() > 3)
			xPath.delete(xPath.length() - 3, xPath.length());
		return xPath.toString();
	}

	@Override
	public CSSValueOverridingDependencyList getIntraSelectorOverridingDependencies() {
		return CSSDependencyDetector.getValueOverridingDependenciesForSelector(this.listOfBaseSelectors.iterator().next());
	}

}
