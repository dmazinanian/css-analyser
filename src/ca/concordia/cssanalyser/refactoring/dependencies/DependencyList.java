package ca.concordia.cssanalyser.refactoring.dependencies;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */

/**
 * A set of CSS dependencies. Backed by a LinkedHashSet
 * 
 * @author Davood Mazinanian
 * 
 */
public abstract class DependencyList<E> implements Set<CSSDependency<E>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DependencyList.class);
	
	protected Set<CSSDependency<E>> dependencies = new LinkedHashSet<>();
	
	@Override
	public int size() {
		return dependencies.size();
	}

	@Override
	public boolean isEmpty() {
		return dependencies.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return dependencies.contains(o);
	}

	@Override
	public Iterator<CSSDependency<E>> iterator() {
		return dependencies.iterator();
	}

	@Override
	public Object[] toArray() {
		return dependencies.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return dependencies.toArray(a);
	}

	@Override
	public boolean add(CSSDependency<E> e) {
		
		return dependencies.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return dependencies.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return dependencies.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends CSSDependency<E>> c) {
		return dependencies.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return dependencies.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return dependencies.removeAll(c);
	}

	@Override
	public void clear() {
		dependencies.clear();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (CSSDependency<E> dependency : dependencies) {	
			builder.append(dependency + "\n");
		}
		return builder.toString();
	}
	
	
	
	public void printDifferences(DependencyList<E> otherDependencyList) {
		for (CSSDependency<E> d : dependencies) {
			boolean found = false;
			for (CSSDependency<E> rd : otherDependencyList)
				if (rd.equals(d)) {
					found = true;
					break;
				}
			if (!found) {
				LOGGER.warn(d + " is not in the refactored version");
			}
		}
			
		for (CSSDependency<E> rd : otherDependencyList) {
			boolean found = false;
			for (CSSDependency<E> d : dependencies)
				if (rd.equals(d)) {
					found = true;
					break;
				}
			if (!found) {
				LOGGER.warn(rd + " is not in the original version");
			}
		}
	}
	
	public CSSDependency<E> getDependency(CSSDependencyNode node1, CSSDependencyNode node2) {
		for (CSSDependency<E> dependency : dependencies) {
			if (dependency.getStartingNode().nodeEquals(node1) &&
					dependency.getEndingNode().nodeEquals(node2))
				return dependency;
		}
		return null;
	}

}
