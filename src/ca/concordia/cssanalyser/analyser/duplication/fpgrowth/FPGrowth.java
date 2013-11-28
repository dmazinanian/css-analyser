package ca.concordia.cssanalyser.analyser.duplication.fpgrowth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.concordia.cssanalyser.analyser.duplication.apriori.Item;
import ca.concordia.cssanalyser.analyser.duplication.apriori.ItemSet;
import ca.concordia.cssanalyser.analyser.duplication.apriori.ItemSetList;


public class FPGrowth {
	
	private static Logger LOGGER = LoggerFactory.getLogger(FPGrowth.class);
	
	private final DataSet initialDataSet;
	private final Map<Integer, ItemSetList> resultItemSetLists;
	
	public FPGrowth(DataSet initialDataSet) {
		this.initialDataSet = initialDataSet;
		resultItemSetLists = new HashMap<>();
	}
	
	public List<ItemSetList> mine(int minSupport) {
		
		FPTree tree = generateFPTree(initialDataSet.getTransactions().values());

		fpGrowth(tree, new HashSet<Item>(), minSupport);

		// Deliver results in order. Could we use TreeMap?!
		List<ItemSetList> results = new ArrayList<>();
		for (int i = 1; i <= resultItemSetLists.size(); i++) {
			if (resultItemSetLists.get(i) != null) {
				results.add(resultItemSetLists.get(i));
			}
		}

		return results;
	}

	private FPTree generateFPTree(Collection<TreeSet<Item>> itemSet) {
		// First pass over the Stylesheet (dataset)
		FPTree tree = new FPTree();

		for (TreeSet<Item> orderedImtes : itemSet) {
			List<Item> items = new ArrayList<>(orderedImtes.descendingSet());
			if (items.size() > 0)
				insert_tree(items, tree.getRoot(), tree);
		}

		return tree;
	}

	private void insert_tree(List<Item> items, Node root, FPTree tree) {
		Item item = items.get(0);
		items.remove(0);
		Node node = root.getFirstChildForItem(item);
		if (node != null) {
			node.incrementNumberOfTransactions();
		} else {
			node = new Node(item, tree, 1);
			tree.addNodeLinkItem(node);
			root.addChild(node);
		}
		if (items.size() > 0)
			insert_tree(items, node, tree);
	}

	private Set<Set<Item>> getAllSubsets(Collection<Item> s) {
		// Copy
		List<Item> items = new ArrayList<>(s);
		Set<Set<Item>> toReturn = new HashSet<>();

		// Find subsets using binary representation. Fast :)
		for (int i = 1; i < Math.pow(2, s.size()); i++) {
			Set<Item> newSubSet = new HashSet<>();
			String bits = Integer.toBinaryString(i);
			for (int j = 0; j < bits.length(); j++) {
				if (bits.charAt(j) == '1')
					newSubSet.add(items.get(bits.length() - j - 1));
			}
			toReturn.add(newSubSet);
		}

		return toReturn;
	}
	
	private void fpGrowth(FPTree tree, Set<Item> currentItems, int minSupport) {
		fpGrowth(tree, currentItems, minSupport, true);
	}
	
	private void fpGrowth(FPTree tree, Set<Item> currentItems, int minSupport, boolean topLevel) {
		if (tree.hasASinglePath()) {
			// All combinations required
			Set<Item> itemsAlongThePath = new HashSet<>();
			Node node = tree.getRoot();
			// Get items along the single path
			while (node.getChildern().iterator().hasNext()) {
				node = node.getChildern().iterator().next();
				itemsAlongThePath.add(node.getItem());

			}
			for (Set<Item> itemSet : getAllSubsets(itemsAlongThePath)) {
				itemSet.addAll(currentItems);
				addItemSet(itemSet);
			}
			//addItemSet(currentItems);
		} else {
			int x = 0;
			// Start from the end of the header table of tree.
			for (Item item : tree.getHeaderTable()) {
				if (topLevel) {
					LOGGER.info("Item " + ++x + " of " + tree.getHeaderTable().size());
				}
				// First see if the current prefix is frequent.
				int support = tree.getTotalSupport(item);
				if (support < minSupport)
					continue;
				// Construct the conditional pattern base for every item
				// For each path, we do have a conditional pattern base
				Node node = tree.getFirstNode(item);
				FPTree conditionalFP = new FPTree();
				while (node != null) {
					/*
					 * For this node, go up through it's path to the root
					 * to create the pattern base
					 */
					Stack<Node> currentPath = new Stack<>();
					int pathSupport = node.getNumberOfTransactions();
					Node currentNode = node.getParent();
					while (currentNode != null && currentNode.getItem() != null) {
						currentPath.add(currentNode);
						currentNode = currentNode.getParent();
					}
					// Add current path to the conditional fp-tree
					Node parentNodeConditional = conditionalFP.getRoot();
					while (!currentPath.empty()) {
						Node currentOriginalNode = currentPath.pop();
						Node newNodeConditional = parentNodeConditional.getFirstChildForItem(currentOriginalNode.getItem());
						if (newNodeConditional != null) {
							newNodeConditional.setNumberOfTransactions(newNodeConditional.getNumberOfTransactions() + pathSupport);
						} else {
							newNodeConditional = new Node(currentOriginalNode.getItem(), conditionalFP, pathSupport);
							conditionalFP.addNodeLinkItem(newNodeConditional);
							parentNodeConditional.addChild(newNodeConditional);
						}
						parentNodeConditional = newNodeConditional;
					}

					// Continue with another node in the linked-list
					node = node.getLinkNode();
				}

				conditionalFP.prune(minSupport);
				
				ItemSet newItemSet = new ItemSet();
				newItemSet.addAll(currentItems);
				newItemSet.add(item);
				addItemSet(newItemSet);
				if (!conditionalFP.isEmpty())
					fpGrowth(conditionalFP, newItemSet, minSupport, false);
			}
		}
	}
	
	/*
	 * Add itemset to the result.
	 * Check if the new itemset has a better suprtset, or
	 * delete all subsets
	 */
	private void addItemSet(Set<Item> is) {
		ItemSet newItemSet = new ItemSet();
		newItemSet.addAll(is);
		for (int i = newItemSet.size() + 1; i <= resultItemSetLists.keySet().size(); i++) {
			ItemSetList isl = resultItemSetLists.get(i);
			if (isl != null && isl.containsSuperSet(newItemSet)) {
				return;
			}
		}
		ItemSetList correspondingItemSetList = resultItemSetLists.get(newItemSet.size());
		if (correspondingItemSetList == null) {
			correspondingItemSetList = new ItemSetList();
			resultItemSetLists.put(newItemSet.size(), correspondingItemSetList);
		}
		correspondingItemSetList.add(newItemSet);
		for (int i = 1; i < newItemSet.size(); i++) {
			ItemSetList isl = resultItemSetLists.get(i);
			if (isl != null)
				isl.removeSubset(newItemSet);
		}
	}
}
