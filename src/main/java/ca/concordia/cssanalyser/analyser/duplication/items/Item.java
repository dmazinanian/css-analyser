package ca.concordia.cssanalyser.analyser.duplication.items;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.media.MediaQueryList;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;


/**
 * An item contains a set of equal or equivalent declarations.
 * The set of selectors corresponding to these declarations is called the support of this item.
 * @author Davood Mazinanian
 *
 */
public class Item implements Set<Declaration>, Cloneable, Comparable<Item> {

	private final Set<Declaration> declarations;
	/*
	 * When finding type III clones, we always create a new shorthand declaration
	 * from some individual declarations inside a selector. Those declarations do
	 * not exist in the real stylesheet, but they are used in the duplication 
	 * finding analysis. So we have to distinguish them with real declarations.
	 */
	private final Set<Declaration> virtualDeclarations = new HashSet<>();
	private final Set<Selector> support;
	private ItemSet paretnItemSet;
	private Set<Integer> duplicationTypes = new HashSet<>();
	private Iterable<MediaQueryList> mediaQueryLists;
	
	/**
	 * Creates an empty Item
	 */
	public Item() {
		declarations = new LinkedHashSet<>();
		support = new LinkedHashSet<>();
	}
	
	
	/**
	 * Creates new Item using the given {@link Declaration} object.
	 * @param declaration
	 */
	public Item(Declaration declaration) {
		this();
		add(declaration);
	}
	
	/**
	 * Creates the item and points its 
	 * declarations set to the given <code>Set<Declaration></code>
	 * @param declarations
	 */
	public Item(Set<Declaration> declarations) {
		this();
		for (Declaration d : declarations)
			add(d);
	}
	
	@Override
	public boolean add(Declaration e) {
		if (e instanceof ShorthandDeclaration) {
			if (((ShorthandDeclaration)e).isVirtual())
				virtualDeclarations.add(e);
		}
		boolean supportsChanged = support.add(e.getSelector());
		boolean declarationsChanged = declarations.add(e);
		if (supportsChanged) {
			if (support.size() == 1)
				this.mediaQueryLists = e.getSelector().getMediaQueryLists();
			if (paretnItemSet != null)
				paretnItemSet.rebuildSupport();
		}
		return declarationsChanged;
	}
	

	@Override
	public boolean addAll(Collection<? extends Declaration> c) {
		boolean changed = false;
		for (Declaration d : declarations)
			if (add(d))
				changed = true;
		return changed;
	}

	@Override
	public void clear() {
		declarations.clear();
		support.clear();
		mediaQueryLists = null;
	}

	@Override
	public boolean contains(Object o) {
		return declarations.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return declarations.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return declarations.isEmpty();
	}

	@Override
	public Iterator<Declaration> iterator() {
		return declarations.iterator();
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof Declaration) {
			Declaration d = (Declaration) o;
			support.remove(d.getSelector());
		}
		if (support.isEmpty())
			mediaQueryLists = null;
		return declarations.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		for (Object o : c)
			if (remove(o))
				changed = true;
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = retainAll(c);
		if (changed) {
			support.clear();
			for (Declaration d : declarations) {
				support.add(d.getSelector());
			}
			if (support.isEmpty())
				mediaQueryLists = null;
		}
		return changed;
	}

	@Override
	public int size() {
		return declarations.size();
	}

	@Override
	public Object[] toArray() {
		return declarations.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return declarations.toArray(a);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		for (Declaration d : declarations)
			sb.append(d.toString() + ", ");
		if (sb.length() > 2)
			sb.delete(sb.length() - 2, sb.length());
		sb.append("} : {");
		for (Selector s : support)
			sb.append(s.toString() + ", ");
		if (sb.length() > 2)
			sb.delete(sb.length() - 2, sb.length());
		sb.append("}");
		
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		return declarations.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return declarations.hashCode();
	}
	
	@Override
	protected Item clone() {
		return new Item(declarations);
	}
	
	public Iterable<Selector> getSupport() {
		return support;
	}

	/**
	 * Returns the first declaration in the set
	 * which is used as the representative of the set
	 * @return
	 */
	public Declaration getFirstDeclaration() {
		if (size() > 0) {
			Declaration nonVirtualDeclaration = null;
			for (Declaration d : declarations) {
				nonVirtualDeclaration = d; 
				if (virtualDeclarations.contains(nonVirtualDeclaration))
					nonVirtualDeclaration = null;
				else
					break;
			}
			return nonVirtualDeclaration;  
		}
		else
			return null;
	}

	/**
	 * Sets the parent itemset which contains this item
	 * @param itemSet
	 */
	public void setParentItemSet(ItemSet itemSet) {
		paretnItemSet = itemSet;
	}
	
	/**
	 * Returns current imte's container itemset
	 * @return
	 */
	public ItemSet getParentItemSet() {
		return paretnItemSet;
	}
	
	/**
	 * Compares two {@link Item}s support counts. If they have different
	 * support counts, this method returns the result of 
	 * {@link Integer#compare()} method (current Item's support count
	 * as the first parameter and given item's support count as the
	 * second parameter). If two {@link Item}s 
	 * have the same support count but they are different (i.e. {@link #equals(Object)}
	 * method returns false for them), it returns the result of {@link String#compareTo()}
	 * method applied on the {@link Item#getFirstDeclaration()} methods of
	 * two Items.
	 * @param item1
	 * @param item2
	 * @return
	 */
	@Override
	public int compareTo(Item otherItem) {
		if (this.support.size() != otherItem.support.size()) {
			return Integer.compare(this.support.size(), otherItem.support.size());
		} else {
			/* 
			 * If two Items have the same support count,
			 * if they are really the same, return 0. Otherwise return 1
			 * so we can have two different Items with the same support count 
			 */
			if (this.equals(otherItem))
				return 0;
			else
				return getFirstDeclaration().toString().compareTo(otherItem.getFirstDeclaration().toString());
		}
	}


	/**
	 * This method returns the declaration with minimum characters
	 * @return
	 */
	public Declaration getDeclarationWithMinimumChars() {
		if (declarations.size() > 0) {
			Declaration min = null; 
			for (Declaration d : declarations) {
				if (d instanceof ShorthandDeclaration && ((ShorthandDeclaration)d).isVirtual())
					continue;
				if (min == null)
					min = d;
				else {
					if (min.toString().length() > d.toString().length())
						min = d;
				}
			}
			return min;
		}
		return null;
	}
	
	public void addDuplicationType(int type) {
		this.duplicationTypes.add(type);
	}
	
	public Set<Integer> getDuplicationTypes() {
		return new HashSet<>(this.duplicationTypes);
	}
	
	public Iterable<MediaQueryList> getMediaQueryLists() {
		return this.mediaQueryLists;
	}

	public int getSupportSize() {
		return this.support.size();
	}
	
	public boolean containsDifferencesInValues() {
		Declaration[] declarationsArray = declarations.toArray(new Declaration[]{});
		if (declarationsArray.length > 0) {
			Declaration baseDeclaration = declarationsArray[0];
			for (int i = 1; i < declarationsArray.length; i++) {
				if (!baseDeclaration.declarationIsEquivalent(declarationsArray[i])) {
					return true; 
				}
			}
			return false;
		}
		return false;
	}
	
}
