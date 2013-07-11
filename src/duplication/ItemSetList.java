package duplication;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import CSSModel.Declaration;
import CSSModel.Selector;

public class ItemSetList implements Iterable<ItemSetAndSupport> {
	
	private final Set<ItemSetAndSupport> itemsets;
	
	public ItemSetList() {
		
		itemsets = new HashSet<>();
	}
	
	public void addItemSet(ItemSetAndSupport itemSetAndSupport) {
		itemsets.add(itemSetAndSupport);
	}
	
	public void addItemSet(Set<Declaration> itemset, Set<Selector> selectors) {
		itemsets.add(new ItemSetAndSupport(itemset, selectors));
	}
	
	public int getNumberOfItems() {
		return itemsets.size();
	}
	
	public Set<ItemSetAndSupport> getItemsetsAndSupports() {
		return itemsets;
	}
	
	@Override
	public String toString() {
		
		String sets = "";
		for (ItemSetAndSupport itemSetAndSupport : itemsets) {

			String set = "{";
			for (Declaration d : itemSetAndSupport.getItemSet()) {
				set += d + ", ";
			}
			
			set = set.substring(0, set.length() -2) + "}";
			
			sets += set + ", " + itemSetAndSupport.getSupport() + " : {";
			
			for (Selector s : itemSetAndSupport.getSelectors())
				sets += s + " - ";
			
			sets = sets.substring(0, sets.length() - 3) + "}\n";
		}
		if (itemsets.iterator().hasNext())
			sets = itemsets.iterator().next().getItemSet().size() + 
					"-itemsets of declarations (Itemset, Support)\n" + 
					sets; 
		return  sets;
	}

	@Override
	public Iterator<ItemSetAndSupport> iterator() {
		return itemsets.iterator();
	}

	
}
