package ca.concordia.cssanalyser.analyser.duplication.items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.selectors.Selector;


/**
 * This class is mainly used for representing C and L tables in the APriori
 * algorithm.
 * 
 * @author Davood Mazinanian
 * 
 */
public class ItemSetList implements Set<ItemSet> {

	private final Set<ItemSet> itemsets;
	private int maximumSupport = 0;
	// Keep track of all itemsets with similar support
	private Map<Set<Selector>, Set<ItemSet>> supportItemSetMap = new HashMap<>();

	public ItemSetList() {
		itemsets = new LinkedHashSet<>();
	}

	@Override
	public boolean add(ItemSet itemSet) {
		if (itemSet.getSupport().size() > maximumSupport)
			maximumSupport = itemSet.getSupport().size();
		addtoSupportItemSetsMap(itemSet);
		itemSet.setParentItemSetList(this);
		return itemsets.add(itemSet);
	}

	public boolean add(Set<Item> itemsSet, Set<Selector> supportSelectors) {
		if (supportSelectors.size() > maximumSupport)
			maximumSupport = supportSelectors.size();
		ItemSet newItemSet = new ItemSet(itemsSet, supportSelectors);
		addtoSupportItemSetsMap(newItemSet);
		newItemSet.setParentItemSetList(this);
		return itemsets.add(newItemSet);
	}

	@Override
	public int size() {
		return itemsets.size();
	}

	@Override
	public String toString() {

		StringBuilder sets = new StringBuilder();

		for (ItemSet itemSetAndSupport : itemsets) {

			StringBuilder set = new StringBuilder("{");

			for (Item d : itemSetAndSupport) {
				set.append("(" + getRepresentativeItemString(d) + "), ");
			}

			set.delete(set.length() - 2, set.length()).append("}");

			sets.append(set);
			sets.append(", " + itemSetAndSupport.getSupport().size() + " : ");

			// for (Selector s : itemSetAndSupport.getSupport())
			// sets.append(s + ", ");
			sets.append(itemSetAndSupport.getSupport());
			sets.append("\n");
		}

		if (itemsets.iterator().hasNext()) {
			String heading = String
					.format("%s-Itemsets of declarations (Itemset, Support count, Support)\nMaximum support is %s\tNumber of cases is %s\n",
							itemsets.iterator().next().size(), maximumSupport,
							size());

			sets.insert(0, heading);
		}
		return sets.toString();
	}

	protected String getRepresentativeItemString(Item item) {
		return item.getFirstDeclaration().toString();
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
		if (changed) {
			for (ItemSet newItemSet : c) {
				if (maximumSupport < newItemSet.getSupport().size())
					maximumSupport = newItemSet.getSupport().size();
				addtoSupportItemSetsMap(newItemSet);
			}
		}
		return changed;
	}

	private void addtoSupportItemSetsMap(ItemSet newItemSet) {
		Set<ItemSet> toPut = supportItemSetMap.get(newItemSet.getSupport());
		if (toPut == null) {
			toPut = new HashSet<>();
			supportItemSetMap.put(newItemSet.getSupport(), toPut);
		}
		toPut.add(newItemSet);
	}

	private void removeFromSupportItemSetsMap(ItemSet newItemSet) {
		supportItemSetMap.get(newItemSet.getSupport()).remove(newItemSet);
	}

	@Override
	public void clear() {
		itemsets.clear();
		maximumSupport = 0;
		supportItemSetMap.clear();
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
		if (changed) {
			removeFromSupportItemSetsMap((ItemSet) o);
			if (((ItemSet) o).getSupport().size() == maximumSupport)
				calculateMaxSupport();
		}
		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = itemsets.removeAll(c);
		if (changed) {
			for (Object o : c) {
				removeFromSupportItemSetsMap((ItemSet) o);
			}
			calculateMaxSupport();
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = itemsets.retainAll(c);
		if (changed) {
			supportItemSetMap.clear();
			for (ItemSet i : itemsets)
				addtoSupportItemSetsMap(i);
			calculateMaxSupport();
		}
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
	 * Removes any ItemSet <code>is1</code> in current ItemSetList, when there
	 * is a corresponding ItemSet in the given itemSetList (parameter of the
	 * method) <code>is2</code> that:
	 * <ol>
	 * <li>is1 has the same support of the is2, and</li>
	 * <li>is1 Items are the subset of is2 Items.
	 * </ol>
	 * 
	 * @param itemSetList
	 */
	public void removeSubsets(ItemSetList itemSetList) {
		ArrayList<ItemSet> toRemove = new ArrayList<>();
		for (ItemSet itemSet : itemSetList) {
			for (ItemSet itemSet2 : itemsets) {
				if (itemSet.containsAll(itemSet2)
						&& itemSet.getSupport().equals(itemSet2.getSupport()))
					toRemove.add(itemSet2);
			}
		}
		itemsets.removeAll(toRemove);
	}

	public boolean containsSuperSet(ItemSet itemSetToCheck) {
		Set<ItemSet> allItemSetsWithSameSupport = supportItemSetMap
				.get(itemSetToCheck.getSupport());
		if (allItemSetsWithSameSupport != null) {
			for (ItemSet itemSet : supportItemSetMap.get(itemSetToCheck
					.getSupport()))
				if (itemSet.containsAll(itemSetToCheck))
					return true;
		}
		return false;
	}

	public void removeSubset(ItemSet superSet) {
		ArrayList<ItemSet> toRemove = new ArrayList<>();
		Set<ItemSet> allItemSetsWithSameSupport = supportItemSetMap
				.get(superSet.getSupport());
		if (allItemSetsWithSameSupport == null)
			return;
		for (ItemSet itemSet : allItemSetsWithSameSupport) {
			if (superSet.containsAll(itemSet))
				toRemove.add(itemSet);
		}
		itemsets.removeAll(toRemove);
	}

	public static ItemSet findItemSetWithMaxImpact(List<ItemSetList> itemSetList) {
		ItemSet itemSetWithMaxImpact = null;
		int maxImpact = Integer.MIN_VALUE;
		for (ItemSetList isl : itemSetList)
			for (ItemSet is : isl) {
				if (itemSetWithMaxImpact == null || is.getGroupingRefactoringImpact() > maxImpact) {
					itemSetWithMaxImpact = is;
					maxImpact = is.getGroupingRefactoringImpact();
				}
			}
		return itemSetWithMaxImpact;
	}

}
