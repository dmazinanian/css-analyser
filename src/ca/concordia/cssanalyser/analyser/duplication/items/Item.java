package ca.concordia.cssanalyser.analyser.duplication.items;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;


/**
 * 
 * @author Davood Mazinanian
 *
 */
public class Item implements Set<Declaration>, Cloneable, Comparable<Item> {

	private final Set<Declaration> declarations;
	/*
	 * When finding type III clones, we always create a new shorthand declaration
	 * from some individual declarations inside a selector. Those declarations are
	 * not exists in the real stylesheet, but they are used in the duplication 
	 * finding analysis. So we have to distinguish them with real declarations.
	 */
	private final Set<Declaration> virtualDeclarations = new HashSet<>();
	private final Set<Selector> support;
	private ItemSet paretnItemSet;
	private Set<Integer> duplicationTypes = new HashSet<>();
	
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
		declarations.add(declaration);
		support.add(declaration.getSelector());
	}
	
	/**
	 * Creates the item and points its 
	 * declarations set to the given <code>Set<Declaration></code>
	 * @param declarations
	 */
	public Item(Set<Declaration> declarations) {
		this.declarations = declarations;
		support = new HashSet<>();
		for (Declaration d : declarations)
			support.add(d.getSelector());
	}
	
	@Override
	public boolean add(Declaration e) {
		return add(e, false);
	}
	
	public boolean add(Declaration e, boolean isVirtual) {
		if (isVirtual)
			virtualDeclarations.add(e);
		boolean supportsChanged = support.add(e.getSelector());
		boolean declarationsChanged = declarations.add(e);
		if (paretnItemSet != null && supportsChanged)
			paretnItemSet.rebuildSupport();
		return declarationsChanged;
	}

	@Override
	public boolean addAll(Collection<? extends Declaration> c) {
		for (Declaration d : c)
			support.add(d.getSelector());
		return declarations.addAll(c);
	}

	@Override
	public void clear() {
		declarations.clear();
		support.clear();
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
		if (o instanceof Declaration)
		{
			Declaration d = (Declaration) o;
			support.remove(d.getSelector());
		}
		rebuildSupport();
		return declarations.remove(o);
	}

	private void rebuildSupport() {
		for (Declaration d : declarations) {
			support.add(d.getSelector());
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = declarations.removeAll(c);
		rebuildSupport();
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = retainAll(c);
		rebuildSupport();
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
		return new Item(new HashSet<Declaration>(declarations));
	}
	
	public Set<Selector> getSupport() {
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
		if (this.getSupport().size() != otherItem.getSupport().size()) {
			return Integer.compare(this.getSupport().size(), otherItem.getSupport().size());
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
		return this.duplicationTypes;
	}
	
}
