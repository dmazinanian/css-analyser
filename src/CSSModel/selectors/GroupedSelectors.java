package CSSModel.selectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import CSSModel.declaration.Declaration;

public class GroupedSelectors extends Selector implements Collection<AtomicSelector> {

	private Set<AtomicSelector> listOfAtomicSelectors;
	
	public GroupedSelectors() {
		this(-1, -1);
	}

	public GroupedSelectors(int line, int col) {
		super(line, col);
		// To preserve the order of selectors as in the CSS file, we
		// use LinkedHashSet
		listOfAtomicSelectors = new LinkedHashSet<>();
	}

	public Set<AtomicSelector> getAtomicSelectors() {
		return listOfAtomicSelectors;
	}

	@Override
	public void addCSSRule(Declaration rule) {
		super.addCSSRule(rule);
		for (AtomicSelector atomicSelector : listOfAtomicSelectors)
			atomicSelector.addCSSRule(rule);
	}
	
	@Override
	public Iterator<AtomicSelector> iterator() {
		return listOfAtomicSelectors.iterator();
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("");
		for (AtomicSelector atomicSelector : listOfAtomicSelectors)
			result.append(atomicSelector + ", ");
		// Remove last , and space
		result.delete(result.length() - 2, result.length()); 
		return result.toString();
	}
	
	/**
	 * Returns true of the list of selector for both
	 * GroupedSelectors are the same, regardless of the
	 * order of their selectors.
	 * @param otherSelector
	 * @return
	 */
	@Override
	public boolean selectorEquals(Selector otherSelector) {
		if (!generalEquals(otherSelector))
			return false;
		GroupedSelectors otherObj = (GroupedSelectors)otherSelector;
		if (listOfAtomicSelectors.size() != otherObj.listOfAtomicSelectors.size())
			return false;
		//return listOfSelectors.containsAll(otherObj.listOfSelectors);
		List<AtomicSelector> tempList = new ArrayList<>(otherObj.listOfAtomicSelectors);
		for (Selector selector : listOfAtomicSelectors) {
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
		if (!(otherSelector instanceof GroupedSelectors))
			return false;
		if (this.parentMedia != null) {
			GroupedSelectors otherGroupedSelector = (GroupedSelectors)otherSelector;
			if (otherGroupedSelector.parentMedia == null)
				return false;
			if (!parentMedia.equals(otherGroupedSelector.parentMedia))
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
		GroupedSelectors otherGroupedSelector = (GroupedSelectors)obj;

		return lineNumber == otherGroupedSelector.lineNumber &&
				columnNumber == otherGroupedSelector.columnNumber &&
				otherGroupedSelector.listOfAtomicSelectors.equals(listOfAtomicSelectors);
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = result * 31 + lineNumber;
		result = result * 31 + columnNumber;
		result = result * 31 + listOfAtomicSelectors.hashCode();
		return result;
	}

	@Override
	public boolean add(AtomicSelector atomicSelector) {
		return listOfAtomicSelectors.add(atomicSelector);
	}

	@Override
	public boolean addAll(Collection<? extends AtomicSelector> atomicSelectors) {
		return listOfAtomicSelectors.addAll(atomicSelectors);
	}

	@Override
	public void clear() {
		listOfAtomicSelectors.clear();
	}

	@Override
	public boolean contains(Object selector) {
		return listOfAtomicSelectors.contains(selector);
	}

	@Override
	public boolean containsAll(Collection<?> lisstOfAtomicSelectors) {
		return listOfAtomicSelectors.containsAll(lisstOfAtomicSelectors);
	}

	@Override
	public boolean isEmpty() {
		return listOfAtomicSelectors.isEmpty();
	}

	@Override
	public boolean remove(Object atomicSelector) {
		return listOfAtomicSelectors.remove(atomicSelector);
	}

	@Override
	public boolean removeAll(Collection<?> listOfAtomicSelectors) {
		return listOfAtomicSelectors.removeAll(listOfAtomicSelectors);
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		return listOfAtomicSelectors.retainAll(arg0);
	}

	@Override
	public int size() {
		return listOfAtomicSelectors.size();
	}

	@Override
	public Object[] toArray() {
		return listOfAtomicSelectors.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return listOfAtomicSelectors.toArray(arg0);
	}	
	
	@Override
	public Selector clone() {
		GroupedSelectors newOne = new GroupedSelectors(lineNumber, columnNumber);
		newOne.listOfAtomicSelectors = new HashSet<>(listOfAtomicSelectors);
		newOne.parentMedia = parentMedia;
		newOne.declarations = new ArrayList<>(declarations);
		return newOne;
	}
}
