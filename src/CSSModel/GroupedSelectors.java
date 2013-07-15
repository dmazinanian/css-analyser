package CSSModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class GroupedSelectors extends Selector implements Collection<AtomicSelector> {

	private final List<AtomicSelector> listOfSelectors;
	
	public GroupedSelectors() {
		this(-1, -1);
	}

	public GroupedSelectors(int line, int col) {
		super(line, col);
		listOfSelectors = new ArrayList<AtomicSelector>();
	}

	
	public List<AtomicSelector> getAtomicSelectors() {
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
		String result = "";
		for (AtomicSelector atomicSelector : listOfSelectors)
			result += atomicSelector + ", ";
		// Remove last , and space
		result = result.substring(0, result.length() - 2); 
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof GroupedSelectors))
			return false;
		GroupedSelectors otherObj = (GroupedSelectors)obj;
		if (size() != otherObj.size())
			return false;
		if (!otherObj.listOfSelectors.containsAll(listOfSelectors)) 
				return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		for (AtomicSelector atomicSelector : listOfSelectors)
			result = 31 * result * atomicSelector.hashCode();
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
