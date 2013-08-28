package analyser.duplication;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import CSSModel.declaration.Declaration;
import CSSModel.selectors.Selector;

/**
 * This class is mainly used for representing C and L tables in the APriori
 * algorithm.
 * 
 * @author Davood Mazinanian
 * 
 */
public class ItemSetList implements Iterable<ItemSet> {

	private final Set<ItemSet> itemsets;
	private int maximumSupport = 0;

	public ItemSetList() {

		itemsets = new HashSet<>();
	}

	public void addItemSet(ItemSet itemset) {
		itemsets.add(itemset);
		if (itemset.getSupport() > maximumSupport)
			maximumSupport = itemset.getSupport();
	}

	public void addItemSet(Set<Declaration> itemset, List<Selector> supportSelectors) {
		itemsets.add(new ItemSet(itemset, supportSelectors));
		if (supportSelectors.size() > maximumSupport)
			maximumSupport = supportSelectors.size();
	}

	public int getNumberOfItems() {
		return itemsets.size();
	}

	public Collection<ItemSet> getItemsetsAndSupports() {
		return itemsets;
	}

	@Override
	public String toString() {

		StringBuilder sets = new StringBuilder();

		for (ItemSet itemSetAndSupport : itemsets) {

			StringBuffer set = new StringBuffer("{");

			for (Declaration d : itemSetAndSupport.getItemSet()) {
				set.append(d + ", ");
			}

			set.delete(set.length() - 2, set.length()).append("}");

			sets.append(set + ", " + itemSetAndSupport.getSupport() + " : {[");
			

			for (Selector s : itemSetAndSupport.getSelectors())
				sets.append(s + "] - [");

			sets.delete(sets.length() - 4, sets.length()).append("}\n");
		}

		if (itemsets.iterator().hasNext()) {
			String heading = String.format("%s-Itemsets of declarations (Itemset, Support count, Support)\nMaximum support is %s\n",
					itemsets.iterator().next().getItemSet().size(),
					maximumSupport);
			
			sets.insert(0, heading);
		}
		return sets.toString();
	}

	@Override
	public Iterator<ItemSet> iterator() {
		return itemsets.iterator();
	}

	public boolean contains(Set<Declaration> declarations) {
		// if (declarations.size() != itemsets.get(0).getItemSet().size())
		// return false;
		ItemSet i = new ItemSet(declarations, null);
		return itemsets.contains(i);
	}

}
