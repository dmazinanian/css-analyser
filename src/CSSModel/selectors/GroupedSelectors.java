package CSSModel.selectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import CSSModel.declaration.Declaration;

public class GroupedSelectors extends Selector implements Collection<AtomicSelector> {

	private final Set<AtomicSelector> listOfSelectors;
	
	public GroupedSelectors() {
		this(-1, -1);
	}

	public GroupedSelectors(int line, int col) {
		super(line, col);
		// To preserve the order of selectors as in the CSS file, we
		// use LinkedHashSet
		listOfSelectors = new LinkedHashSet<>();
	}

	public Set<AtomicSelector> getAtomicSelectors() {
		return listOfSelectors;
	}

	@Override
	public void addCSSRule(Declaration rule) {
		super.addCSSRule(rule);
		for (AtomicSelector atomicSelector : listOfSelectors)
			atomicSelector.addCSSRule(rule);
	}
	
	@Override
	public Iterator<AtomicSelector> iterator() {
		return listOfSelectors.iterator();
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("");
		for (AtomicSelector atomicSelector : listOfSelectors)
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
		if (listOfSelectors.size() != otherObj.listOfSelectors.size())
			return false;
		//return listOfSelectors.containsAll(otherObj.listOfSelectors);
		List<AtomicSelector> tempList = new ArrayList<>(otherObj.listOfSelectors);
		for (Selector selector : listOfSelectors) {
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
		GroupedSelectors otherObj = (GroupedSelectors)obj;

		return lineNumber == otherObj.lineNumber &&
				columnNumber == otherObj.columnNumber &&
				otherObj.listOfSelectors.equals(listOfSelectors);
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = result * 31 + lineNumber;
		result = result * 31 + columnNumber;
		result = result * 31 + listOfSelectors.hashCode();
		return result;
	}

	@Override
	public boolean add(AtomicSelector atomicSelector) {
		return listOfSelectors.add(atomicSelector);
	}

	@Override
	public boolean addAll(Collection<? extends AtomicSelector> listOfAtomicSelectors) {
		return listOfSelectors.addAll(listOfAtomicSelectors);
	}

	@Override
	public void clear() {
		listOfSelectors.clear();
	}

	@Override
	public boolean contains(Object selector) {
		return listOfSelectors.contains(selector);
	}

	@Override
	public boolean containsAll(Collection<?> lisstOfAtomicSelectors) {
		return listOfSelectors.containsAll(lisstOfAtomicSelectors);
	}

	@Override
	public boolean isEmpty() {
		return listOfSelectors.isEmpty();
	}

	@Override
	public boolean remove(Object atomicSelector) {
		return listOfSelectors.remove(atomicSelector);
	}

	@Override
	public boolean removeAll(Collection<?> listOfAtomicSelectors) {
		return listOfSelectors.removeAll(listOfAtomicSelectors);
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		return listOfSelectors.retainAll(arg0);
	}

	@Override
	public int size() {
		return listOfSelectors.size();
	}

	@Override
	public Object[] toArray() {
		return listOfSelectors.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return listOfSelectors.toArray(arg0);
	}	
}
