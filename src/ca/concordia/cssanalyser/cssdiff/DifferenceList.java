package ca.concordia.cssanalyser.cssdiff;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;

public class DifferenceList implements List<Difference> {
	
	private List<Difference> differences;
	private StyleSheet styleSheet1, styleSheet2;
	
	public DifferenceList(StyleSheet styleSheet1, StyleSheet styleSheet2) {
		this.styleSheet1 = styleSheet1;
		this.styleSheet2 = styleSheet2;
	}
	
	public StyleSheet getStyleSheet1() {
		return styleSheet1;
	}
	
	public StyleSheet getStyleSheet2() {
		return styleSheet2;
	}

	@Override
	public int size() {
		return differences.size();
	}

	@Override
	public boolean isEmpty() {
		return differences.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return differences.contains(o);
	}

	@Override
	public Iterator<Difference> iterator() {
		return differences.iterator();
	}

	@Override
	public Object[] toArray() {
		return differences.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return differences.toArray(a);
	}

	@Override
	public boolean add(Difference e) {
		return differences.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return differences.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return differences.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Difference> c) {
		return differences.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Difference> c) {
		return differences.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return differences.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return differences.retainAll(c);
	}

	@Override
	public void clear() {
		differences.clear();
	}

	@Override
	public Difference get(int index) {
		return differences.get(index);
	}

	@Override
	public Difference set(int index, Difference element) {
		return differences.set(index, element);
	}

	@Override
	public void add(int index, Difference element) {
		differences.add(index, element);
	}

	@Override
	public Difference remove(int index) {
		return differences.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return differences.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return differences.lastIndexOf(o);
	}

	@Override
	public ListIterator<Difference> listIterator() {
		return differences.listIterator();
	}

	@Override
	public ListIterator<Difference> listIterator(int index) {
		return differences.listIterator(index);
	}

	@Override
	public List<Difference> subList(int fromIndex, int toIndex) {
		return differences.subList(fromIndex, toIndex);
	}
	
	

}
