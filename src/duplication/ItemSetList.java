package duplication;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import CSSModel.Declaration;
import CSSModel.Selector;

/**
 * This class is mainly used for representing C and L tables in the APriori
 * algorithm.
 * 
 * @author Davood Mazinanian
 * 
 */
public class ItemSetList implements Iterable<ItemSet> {

	private final Set<ItemSet> itemsets;

	public ItemSetList() {

		itemsets = new HashSet<>();
	}

	public void addItemSet(ItemSet itemSetAndSupport) {
		itemsets.add(itemSetAndSupport);
	}

	public void addItemSet(Set<Declaration> itemset, Set<Selector> selectors) {
		itemsets.add(new ItemSet(itemset, selectors));
	}

	public int getNumberOfItems() {
		return itemsets.size();
	}

	public Set<ItemSet> getItemsetsAndSupports() {
		return itemsets;
	}

	@Override
	public String toString() {

		String sets = "";
		for (ItemSet itemSetAndSupport : itemsets) {

			String set = "{";
			for (Declaration d : itemSetAndSupport.getItemSet()) {
				set += d + ", ";
			}

			set = set.substring(0, set.length() - 2) + "}";

			sets += set + ", " + itemSetAndSupport.getSupport() + " : {";

			for (Selector s : itemSetAndSupport.getSelectors())
				sets += s + " - ";

			sets = sets.substring(0, sets.length() - 3) + "}\n";
		}
		if (itemsets.iterator().hasNext())
			sets = itemsets.iterator().next().getItemSet().size()
					+ "-itemsets of declarations (Itemset, Support)\n" + sets;
		return sets;
	}

	@Override
	public Iterator<ItemSet> iterator() {
		return itemsets.iterator();
	}

}
