package analyser.duplication.apriori;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import CSSModel.selectors.Selector;

/**
 * This class is mainly used for representing C and L tables in the APriori
 * algorithm.
 * 
 * @author Davood Mazinanian
 * 
 */
public class ItemSetList implements Set<ItemSet>, Comparable<ItemSetList> {

	private final Set<ItemSet> itemsets;
	private int maximumSupport = 0;

	public ItemSetList() {

		itemsets = new HashSet<>();
	}

	@Override
	public boolean add(ItemSet itemSet) {
		if (itemSet.getSupport().size() > maximumSupport)
			maximumSupport = itemSet.getSupport().size();
		itemSet.setParentItemSetList(this);
		return itemsets.add(itemSet);
	}

	public boolean add(Set<Item> itemsSet, Set<Selector> supportSelectors) {
		if (supportSelectors.size() > maximumSupport)
			maximumSupport = supportSelectors.size();
		ItemSet newItemSet = new ItemSet(itemsSet, supportSelectors);
		newItemSet.setParentItemSetList(this);
		return itemsets.add(newItemSet);
	}

	@Override
	public int size() {
		return itemsets.size();
	}

	public Set<ItemSet> getItemsets() {
		return itemsets;
	}

	@Override
	public String toString() {

		StringBuilder sets = new StringBuilder();

		for (ItemSet itemSetAndSupport : itemsets) {

			StringBuilder set = new StringBuilder("{");

			for (Item d : itemSetAndSupport) {
				set.append("(" + d.getFirstDeclaration() + "), ");
			}

			set.delete(set.length() - 2, set.length()).append("}");

			sets.append(set);
			sets.append(", " + itemSetAndSupport.getSupport().size() + " : ");
			

			//for (Selector s : itemSetAndSupport.getSupport())
			//	sets.append(s + ", ");
			sets.append(itemSetAndSupport.getSupport());
			sets.append("\n");
		}

		if (itemsets.iterator().hasNext()) {
			String heading = String.format("%s-Itemsets of declarations (Itemset, Support count, Support)\nMaximum support is %s\n",
					itemsets.iterator().next().size(),
					maximumSupport);
			
			sets.insert(0, heading);
		}
		return sets.toString();
	}

	@Override
	public Iterator<ItemSet> iterator() {
		return itemsets.iterator();
	}

	@Override
	public boolean addAll(Collection<? extends ItemSet> c) {
		for (ItemSet i : c)
			i.setParentItemSetList(this);
		boolean changed = itemsets.addAll(c);
		if (changed)
			calculateMaxSupport();
		return changed;
	}

	@Override
	public void clear() {
		itemsets.clear();
		maximumSupport = 0;
	}

	@Override
	public boolean contains(Object o) {
		return itemsets.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return itemsets.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return itemsets.isEmpty();
	}

	@Override
	public boolean remove(Object o) {
		boolean changed = itemsets.remove(o);
		if (changed)
			calculateMaxSupport();
		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = itemsets.removeAll(c);
		if (changed)
			calculateMaxSupport();
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = itemsets.retainAll(c);
		if (changed)
			calculateMaxSupport();
		return changed;
	}

	/**
	 * Calculates the maximum support count of current itemset list
	 */
	void calculateMaxSupport() {
		maximumSupport = 0;
		for (ItemSet itemset : itemsets) 
			if (maximumSupport < itemset.getSupport().size())
				maximumSupport = itemset.getSupport().size();
	}

	@Override
	public Object[] toArray() {
		return itemsets.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return itemsets.toArray(a);
	}

	public int getMaximumSupport() {
		return maximumSupport;
	}
	
	@Override
	public boolean equals(Object obj) {
		return itemsets.equals(obj);
	}
	
	@Override
	public int hashCode() {
			return itemsets.hashCode();
	}

	/**
	 * Check whether there is an ItemSet which contains all
	 * the items of the given ItemSet (through parameter). 
	 * Note that their support set must be the same as well.
	 * @param newItemSet
	 * @return
	 */
	public boolean containsItemsSubset(ItemSet newItemSet) {
		for (ItemSet itemSet : itemsets)
			if (itemSet.containsAll(newItemSet) && 
					itemSet.getSupport().equals(newItemSet.getSupport()))
				return true;
		return false;
	}

	@Override
	public int compareTo(ItemSetList o) {
		//if (itemsets.size() != o.itemsets.size())
		return 0;
	}
	

}
