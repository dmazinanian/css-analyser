package ca.concordia.cssanalyser.analyser.duplication.fpgrowth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import ca.concordia.cssanalyser.analyser.duplication.items.Item;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSetList;


public class FPGrowth {
		
	private final Map<Integer, ItemSetList> resultItemSetLists;
	private final boolean removeSubsets;
	private int maxItemSetSize = -1;
	
	/**
	 * Creates a new object of FPGrowth class
	 * @param removeSubSets Identify whether the
	 * subsumed ItemSets must be removed or not. A subsumed
	 * ItemSet is the one of which all items are exists in a 
	 * bigger ItemSet, and the support of the bigger ItemSet
	 * is the same as the support set of the smaller ItemSet.
	 * 
	 * @param dummyObject a dummy object of type ItemSetList
	 * (or any subclass). The resulting ItemSetLists will
	 * have the same type if this given dummy object.
	 */
	public FPGrowth(boolean removeSubSets) {
		this.resultItemSetLists = new HashMap<>();
		this.removeSubsets = removeSubSets;
	}
	
	public List<ItemSetList> mine(Collection<TreeSet<Item>> dataSet, int minSupport) {
		
		FPTree tree = generateFPTree(dataSet);

		fpGrowth(tree, new HashSet<Item>(), minSupport);
		
		tree = null;

		// Deliver results in order.
		List<Integer> keys = new ArrayList<>(resultItemSetLists.keySet());
		Collections.sort(keys);
		List<ItemSetList> results = new ArrayList<>();
		for (int i : keys) {
			ItemSetList itemSetList = resultItemSetLists.get(i);
			if (itemSetList != null) {
				results.add(itemSetList);
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
				insertTree(items, tree.getRoot(), tree);
		}

		return tree;
	}

	private void insertTree(List<Item> items, Node root, FPTree tree) {
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
			insertTree(items, node, tree);
	}

	private Set<Set<Item>> getAllSubsets(Collection<Item> s) {
		// Copy
		List<Item> items = new ArrayList<>(s);
		Set<Set<Item>> toReturn = new HashSet<>();

		// Find subsets using binary representation. Fast :)
		for (int i = 1; i < Math.pow(2, s.size()); i++) {
			Set<Item> newSubSet = new HashSet<>();
			String bits = Integer.toBinaryString(i);
			// Add leading zeros
			int zerosToAdd = s.size() - bits.length();
			for (int j = 0; j < zerosToAdd; j++) {
				bits = "0" + bits;
			}
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
			// All combinations may be required
			Set<Item> itemsAlongThePath = new LinkedHashSet<>();
			Node node = tree.getRoot();
			// Get items along the single path
			while (node.getChildern().iterator().hasNext()) {
				node = node.getChildern().iterator().next();
				itemsAlongThePath.add(node.getItem());
			}
			if (removeSubsets) {
				// Don't get all subsets, many of them will be removed
				// Only get the maximal ones along the path 
				List<Set<Item>> itemSetsToConsider = new ArrayList<>();
				ItemSet itemsSoFar = new ItemSet();
				int lastSize = 0;
				for (Item item : itemsAlongThePath) {
					itemsSoFar.add(item);
					if (itemsSoFar.getSupportSize() < lastSize) {
						HashSet<Item> copy = new HashSet<>(itemsSoFar);
						copy.remove(item);
						itemSetsToConsider.add(copy);
					}
					lastSize = itemsSoFar.getSupportSize();
				}
				// The longest set is always a desired itemset
				itemSetsToConsider.add(itemsAlongThePath);
				for (Set<Item> itemSet : itemSetsToConsider) {
					itemSet.addAll(currentItems);
					addItemSet(itemSet);
				}
			} else {
				Set<Set<Item>> allSubsets = getAllSubsets(itemsAlongThePath);
				for (Set<Item> itemSet : allSubsets) {
					itemSet.addAll(currentItems);
					addItemSet(itemSet);
				}
				if (currentItems.size() > 0) {
					addItemSet(currentItems);
				}
			}
		} else {
			// Start from the end of the header table of tree.
			for (Item item : tree.getHeaderTable()) {
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
		if (removeSubsets) {
			// If this itemset is already in a superset, don't add it
			for (int i = newItemSet.size() + 1; i <= maxItemSetSize ; i++) {
				ItemSetList isl = resultItemSetLists.get(i);
				if (isl != null && isl.containsSuperSet(newItemSet)) {
					return;
				}
			}
		}
		ItemSetList correspondingItemSetList = resultItemSetLists.get(newItemSet.size());
		if (correspondingItemSetList == null) {
			correspondingItemSetList = new ItemSetList();
			resultItemSetLists.put(newItemSet.size(), correspondingItemSetList);
		}
		correspondingItemSetList.add(newItemSet);
		if (newItemSet.size() > maxItemSetSize)
			maxItemSetSize = newItemSet.size();
		if (removeSubsets) {
			// Remove sub sets
			for (int i = 1; i < newItemSet.size(); i++) {
				ItemSetList isl = resultItemSetLists.get(i);
				if (isl != null) {
					isl.removeSubset(newItemSet);
					if (isl.isEmpty())
						resultItemSetLists.remove(i);
				}
			}
		}
	}
}
