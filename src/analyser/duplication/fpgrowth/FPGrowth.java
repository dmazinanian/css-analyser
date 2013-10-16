package analyser.duplication.fpgrowth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import analyser.duplication.apriori.Item;
import analyser.duplication.apriori.ItemSet;
import analyser.duplication.apriori.ItemSetList;

public class FPGrowth {
	
	
	private final DataSet initialDataSet;
	private final Map<Integer, ItemSetList> resultItemSetLists;
	
	public FPGrowth(DataSet initialDataSet) {
		this.initialDataSet = initialDataSet;
		resultItemSetLists = new HashMap<>();
	}
	
	public List<ItemSetList> mine(int minSupport) {
		
		FPTree tree = generateFPTree(initialDataSet.getTransactions().values());

		fpGrowth(tree, new HashSet<Item>(), minSupport);

		List<ItemSetList> results = new ArrayList<>();
		for (int i = 1; i <= resultItemSetLists.size(); i++) {
			if (resultItemSetLists.get(i) != null) {
				results.add(resultItemSetLists.get(i));
				// Remove redundant subsets
				if (i > 1 && results.get(i - 2) != null) {
					System.out.println("Removing " + i);
					results.get(i - 2).removeSubsets(results.get(i - 1));
				}
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

	private Set<Set<Item>> getAllSubsets(Set<Item> s) {
		// Copy
		List<Item> items = new ArrayList<>(s);
		Set<Set<Item>> toReturn = new HashSet<>();

		/*for (Item item : s) { // Old style subset finder
			set.remove(item);
			Set<Set<Item>> currentSubsets = getAllSubsets(set);
			for (Set<Item> subset : currentSubsets)
				subset.add(item);
			Set<Item> sss = new HashSet<Item>();
			sss.add(item);
			currentSubsets.add(sss);
			toReturn.addAll(currentSubsets);
			set.add(item);
		}*/

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
		for (Item i : tree.getHeaderTable()) {
			if (i.getFirstDeclaration().toString().contains("list-style: disc inside"))
				System.out.print("");
		}
		if (tree.hasASinglePath()) {
			// All combinations required
			Set<Item> items = new HashSet<>();
			Node node = tree.getRoot();
			while (node.getChildern().iterator().hasNext()) {
				node = node.getChildern().iterator().next();
				items.add(node.getItem());
			}
			
			for (Set<Item> is : getAllSubsets(items)) {
				is.addAll(currentItems);
				addItemSet(is);
			}
			addItemSet(currentItems);
		} else {
			// Start from the end of the header table of tree.
			for (Item item : tree.getHeaderTable()) {
				if (item.getFirstDeclaration().toString().contains("list-style: disc inside"))
					System.out.print("");
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
				
				Set<Item> newItemSet = new HashSet<>(currentItems);
				newItemSet.add(item);
				addItemSet(newItemSet);
				if (!conditionalFP.isEmpty())
					fpGrowth(conditionalFP, newItemSet, minSupport);
			}
		}
	}
	
	private void addItemSet(Set<Item> is) {
		ItemSet newItemSet = new ItemSet();
		newItemSet.addAll(is);
		ItemSetList correspondingItemSetList = resultItemSetLists.get(is.size());
		if (correspondingItemSetList == null) {
			correspondingItemSetList = new ItemSetList();
			resultItemSetLists.put(is.size(), correspondingItemSetList);
		}
		correspondingItemSetList.add(newItemSet);
	}
}
