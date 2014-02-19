package ca.concordia.cssanalyser.refactoring.dependencies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import ca.concordia.cssanalyser.refactoring.dependencies.CSSDependencyDifference.CSSDependencyDifferenceType;

/**
 * 
 */

/**
 * A list of CSS dependencies. Backed by an ArrayList
 * 
 * @author Davood Mazinanian
 * 
 */
public abstract class CSSDependencyList<T extends CSSDependency<?>> implements List<T> {
	
	protected List<T> dependencies = new ArrayList<>();
	
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
	public Iterator<T> iterator() {
		return dependencies.iterator();
	}

	@Override
	public Object[] toArray() {
		return dependencies.toArray();
	}

	@Override
	public <U> U[] toArray(U[] a) {
		return dependencies.toArray(a);
	}

	@Override
	public boolean add(T e) {
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
	public boolean addAll(Collection<? extends T> c) {
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
		for (CSSDependency<?> dependency : dependencies) {
			builder.append(dependency + "\n");
		}
		return builder.toString();
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		
		return dependencies.addAll(index, c);
	}

	@Override
	public T get(int index) {
		return dependencies.get(index);
	}

	@Override
	public T set(int index, T element) {
		return dependencies.set(index, element);
	}

	@Override
	public void add(int index, T element) {
		dependencies.add(index, element);
	}

	@Override
	public T remove(int index) {
		return dependencies.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return dependencies.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return dependencies.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return dependencies.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return dependencies.listIterator(index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return dependencies.subList(fromIndex, toIndex);
	}

	
	
	public CSSDependencyDifferenceList getDifferencesWith(CSSDependencyList<?> otherDependencyList) {
		
		CSSDependencyDifferenceList toReturn = new CSSDependencyDifferenceList();
		
		List<CSSDependency<?>> missing = new ArrayList<>();
		List<CSSDependency<?>> added = new ArrayList<>();

		for (CSSDependency<?> d : dependencies) {
			boolean found = false;
			for (CSSDependency<?> rd : otherDependencyList)
				if (rd.equals(d)) {
					found = true;
					break;
				}
			if (!found) {
				missing.add(d);
			}
		}

		for (CSSDependency<?> rd : otherDependencyList) {
			boolean found = false;
			for (CSSDependency<?> d : dependencies)
				if (rd.equals(d)) {
					found = true;
					break;
				}
			if (!found) {
				if (!found) {
					added.add(rd);
				}
			}
		}
		
		Set<Integer> toRemoveFromMissing = new HashSet<>();
		Set<Integer> toRemoveFromAdded = new HashSet<>();
		
		for (int i = 0; i < missing.size(); i++) {
			if (toRemoveFromMissing.contains(i))
				continue;
			CSSDependency<?> d = missing.get(i);
//			if (d.getStartingNode().toString().contains("#yucs-top-bar .sp<[@media only screen and (-webkit-min-device-pixel-ratio: 2), only screen and (min--moz-device-pixel-ratio: 2), only screen and (-o-min-device-pixel-ratio: 2 / 1), only screen and (min-device-pixel-ratio: 2), only screen and (min-resolution: 192dpi), only screen and (min-resolution: 2dppx)]>$background-image: url('https://s1.yimg.com/kx/yucs/uh3/uh/images/659/uh_sprite_x2.png')"))
//				System.out.print("");
			
			for (int j = 0; j < added.size(); j++) {
				if (toRemoveFromAdded.contains(j))
					continue;
				CSSDependency<?> rd = added.get(j);
				
				if (rd.getStartingNode().nodeEquals(d.getEndingNode()) && 
						d.getStartingNode().nodeEquals(rd.getEndingNode()) &&
						rd.getDependencyLabels().equals(d.getDependencyLabels())) {
					toRemoveFromMissing.add(i);
					toRemoveFromAdded.add(j);
					toReturn.add(new CSSDependencyDifference<>(CSSDependencyDifferenceType.REVERSED, d));
				}
			}
		}
		
		for (int i = 0; i < missing.size(); i++) {
			if (!toRemoveFromMissing.contains(i))
				toReturn.add(new CSSDependencyDifference<>(CSSDependencyDifferenceType.MISSING, missing.get(i)));
		}
		
		for (int i = 0; i < added.size(); i++) {
			if (!toRemoveFromAdded.contains(i))
				toReturn.add(new CSSDependencyDifference<>(CSSDependencyDifferenceType.MISSING, added.get(i)));
		}

		return toReturn;
	}

	public CSSDependency<?> getDependency(CSSDependencyNode node1,
			CSSDependencyNode node2) {
		for (CSSDependency<?> dependency : dependencies) {
			if (dependency.getStartingNode().nodeEquals(node1)
					&& dependency.getEndingNode().nodeEquals(node2))
				return dependency;
		}
		return null;
	}
}
