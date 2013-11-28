package ca.concordia.cssanalyser.analyser.duplication.fpgrowth;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import ca.concordia.cssanalyser.analyser.duplication.apriori.Item;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;


public class DataSet {
	
	private final Map<Selector, TreeSet<Item>> items;
	
	public DataSet() {
		this.items = new HashMap<>();
	}
		
	public void addItem(Selector selector, Item item) {
		TreeSet<Item> itemSet = items.get(selector);
		if (itemSet == null) {
			itemSet = new TreeSet<Item>();
			items.put(selector, itemSet);
		}
		itemSet.add(item);
	}
	
	public Map<Selector, TreeSet<Item>> getTransactions() {
		return items;
	}
	
	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		for (Selector selector : items.keySet()) {
			toReturn.append(selector.toString() + " {");
			for (Item i : items.get(selector).descendingSet())
				toReturn.append(i.getFirstDeclaration() + ", ");
			if (toReturn.length() > 3)
				toReturn.delete(toReturn.length() - 2, toReturn.length());
			toReturn.append("}\n");
		}
		return toReturn.toString();
	}
}
