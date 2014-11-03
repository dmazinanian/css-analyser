package ca.concordia.cssanalyser.cssmodel.selectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;


public class GroupingSelector extends Selector implements Collection<BaseSelector> {

	private Set<BaseSelector> listOfBaseSelectors;
	
	public GroupingSelector() {
		this(-1, -1);
	}

	public GroupingSelector(int line, int col) {
		super(line, col);
		// To preserve the order of selectors as in the CSS file, we
		// use LinkedHashSet
		listOfBaseSelectors = new LinkedHashSet<>();
	}

	public Set<BaseSelector> getBaseSelectors() {
		return listOfBaseSelectors;
	}

	@Override
	public void addDeclaration(Declaration declaration) {
		for (BaseSelector baseSelector : listOfBaseSelectors)
			baseSelector.addDeclaration(declaration);
		// The parent of the selector must be the grouping selector
		super.addDeclaration(declaration);
	}
	
	@Override
	public void removeDeclaration(Declaration d) {
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
	public boolean selectorEquals(Selector otherSelector) {
		if (!generalEquals(otherSelector))
			return false;
		GroupingSelector otherObj = (GroupingSelector)otherSelector;
		if (listOfBaseSelectors.size() != otherObj.listOfBaseSelectors.size())
			return false;
		//return listOfSelectors.containsAll(otherObj.listOfSelectors);
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

	private boolean generalEquals(Object otherSelector) {
		if (otherSelector == null)
			return false;
		if (otherSelector == this)
			return true;
		if (!(otherSelector instanceof GroupingSelector))
			return false;
		if (this.mediaQueryLists != null) {
			GroupingSelector otherGroupedSelector = (GroupingSelector)otherSelector;
			if (otherGroupedSelector.mediaQueryLists == null)
				return false;
			if (!mediaQueryLists.equals(otherGroupedSelector.mediaQueryLists))
				return false;
		}
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
		GroupingSelector otherGroupedSelector = (GroupingSelector)obj;

		return lineNumber == otherGroupedSelector.lineNumber &&
				columnNumber == otherGroupedSelector.columnNumber &&
				//otherGroupedSelector.listOfBaseSelectors.equals(listOfBaseSelectors);
				otherGroupedSelector.listOfBaseSelectors.size() == listOfBaseSelectors.size() &&
				otherGroupedSelector.listOfBaseSelectors.containsAll(listOfBaseSelectors);
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = result * 31 + lineNumber;
		result = result * 31 + columnNumber;
		result = result * 31 + listOfBaseSelectors.hashCode();
		return result;
	}

	@Override
	public boolean add(BaseSelector baseSelector) {
		return listOfBaseSelectors.add(baseSelector);
	}

	@Override
	public boolean addAll(Collection<? extends BaseSelector> baseSelectors) {
		return listOfBaseSelectors.addAll(baseSelectors);
	}

	@Override
	public void clear() {
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
		return listOfBaseSelectors.remove(atomicSelector);
	}

	@Override
	public boolean removeAll(Collection<?> listOfAtomicSelectors) {
		return listOfAtomicSelectors.removeAll(listOfAtomicSelectors);
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
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
		GroupingSelector newOne = new GroupingSelector(lineNumber, columnNumber);
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
	
}
