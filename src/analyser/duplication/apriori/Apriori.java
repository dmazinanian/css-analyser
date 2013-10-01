package analyser.duplication.apriori;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Apriori {
	
	private ItemSetList C1;
	
	public Apriori(ItemSetList listOfFrequentItems) {
		C1 = listOfFrequentItems;
	}
	
	public List<ItemSetList> apriori(final int minSupport) {
		
		List<ItemSetList> l = new ArrayList<>(); // Keeping L(k), the frequent itemsets of size k
		
		l.add(getLfromC(C1, minSupport)); // Generating L(1) by cutting C(1)

		int k = 1;
		while (true) {
			// Generating L(k) by using L(k-1)
			l.add(generateCandidates(l.get(k - 1), minSupport));
			
			// Removing previous step's redundant itemsets
			List<ItemSet> toRemove = new ArrayList<>(l.get(k - 1).size());
			for (ItemSet itemset : l.get(k - 1)) {
				if (l.get(k).containsItemsSubset(itemset))
					toRemove.add(itemset);
			}
			l.get(k - 1).removeAll(toRemove);
			
			if (l.get(k).size() == 0) { // If L(k) is empty break
				l.remove(k);
				break;
			}

			k++;
		} 
		
		return l;
	}


	private ItemSetList generateCandidates(ItemSetList itemSetList, int minSupport) {
		
		/* itemSetList is L(k-1), which is a table of ItemSets
		 * toReturn is L(k)
		 */
		ItemSetList toReturn = new ItemSetList();
		
		Set<Item> unionAll = new HashSet<>(); 
		/*
		 * First find the union of all L(k-1) declarations
		 * This set will be used later in order to create itemsets with k declarations.		
		 */
		for (ItemSet itemset : itemSetList) {
			unionAll.addAll(itemset);
		}

		for (ItemSet itemset : itemSetList) {
			/* First we create a new set, which will be our new item set. Initially,
			 * this set contains first member of L(k-1). One at a time, we
			 * add one new member to this set,  to create an itemset with k members.
			 */
			
			for (Item item : unionAll) {
				/* 
				 * Each time we add one item from unionAll ( union of all the declarations in L(k-1) )
				 * to create itemset with k members.
				 * newItemSet must not contain new declaration, otherwise, after 
				 * adding this new declaration, newItemSet would not have k members
				 */
				ItemSet newItemSet = itemset.clone();
				if (!newItemSet.contains(item)) {
					
					newItemSet.add(item);
					
					/*
					 * Also, L(k) should not contain this new itemset.
					 */
					if (newItemSet.getSupport().size() >= minSupport && !toReturn.contains(newItemSet)) {		
							toReturn.add(newItemSet.clone()); 
					} 
				}
			}
		}
		return toReturn;
	}

	private ItemSetList getLfromC(ItemSetList itemSetList, final int minSupportCount) {
		
		ItemSetList Lk = new ItemSetList();
		for (ItemSet itemset : itemSetList) {
			if (itemset.getSupport().size() >= minSupportCount) {
				Lk.add(itemset);
			}
		}
		return Lk;	
	}
}
