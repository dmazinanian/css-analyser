package analyser.duplication.fpgrowth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
	private final int minSupport;
	private final Map<Integer, ItemSetList> resultItemSetList;
	
	public FPGrowth(DataSet initialDataSet, final int minSupport) {
		this.initialDataSet = initialDataSet;
		this.minSupport = minSupport;
		resultItemSetList = new HashMap<>();
	}
	
	public List<ItemSetList> mine() {
		
		FPTree tree = generateFPTree(initialDataSet.getTransactions().values());

		fpGrowth(tree, new ItemSet(), minSupport);

		return new ArrayList<>(resultItemSetList.values());
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
		Node node = root.getChild(item);
		if (node != null) {
			node.incrementNumberOfTransactions();
		} else {
			node = new Node(item, root, 1);
			tree.addNodeLinkItem(node);
			root.addChild(node);
		}
		if (items.size() > 0)
			insert_tree(items, node, tree);
	}

	private ItemSetList getAllSubsets(Set<Item> s) {
		Set<Item> set = new LinkedHashSet<>(s);
		ItemSetList subsets = new ItemSetList();

		for (Item item : s) {
			set.remove(item);
			ItemSetList currentSubset = getAllSubsets(set);
			for (ItemSet subset : currentSubset)
				subset.add(item);
			ItemSet sss = new ItemSet();
			sss.add(item);
			currentSubset.add(sss);
			subsets.addAll(currentSubset);
			set.add(item);
		}

		return subsets;
	}


	private void fpGrowth(FPTree tree, ItemSet itemSet, int minSupport) {
		if (tree.isEmpty()) {
			return;	
		}
		else if (tree.hasASinglePath()) {
			// All combinations required
			Set<Item> items = new HashSet<>();
			Node node = tree.getRoot();
			while (node != null) {
				if (node.getChildern().iterator().hasNext())
					node = node.getChildern().iterator().next();
				else
					break;
				items.add(node.getItem());
			}
			for (ItemSet is : getAllSubsets(items)) {
				is.addAll(itemSet);
				addItemSet(is);
			}
		} else {
			// Start from the end of the header table of tree.
			for (Item item : tree.getHeaderTable()) {
				// First see if the current prefix is frequent.
				Node n = tree.getItemNodeMap().get(item);
				int support = 0;
				while (n != null) {
					support += n.getNumberOfTransactions();
					n = n.getLinkNode();
				}
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
						//if (currentNode.getNumberOfTransactions() < pathSupport)
						//	pathSupport = currentNode.getNumberOfTransactions();
						currentNode = currentNode.getParent();
					}
					Node parentNodeConditional = conditionalFP.getRoot();
					while (!currentPath.empty()) {
						Node currentOriginalNode = currentPath.pop();
						Node newNodeConditional = parentNodeConditional.getChild(currentOriginalNode.getItem());
						if (newNodeConditional != null) {
							newNodeConditional.setNumberOfTransactions(newNodeConditional.getNumberOfTransactions() + pathSupport);
						} else {
							newNodeConditional = new Node(currentOriginalNode.getItem(), parentNodeConditional, pathSupport);
							parentNodeConditional.addChild(newNodeConditional);
							conditionalFP.addNodeLinkItem(newNodeConditional);
							parentNodeConditional.addChild(newNodeConditional);
						}
						parentNodeConditional = newNodeConditional;
					}

					// Continue with another node in the linked-list
					node = node.getLinkNode();
				}

				// Remove unnecessary nodes (those which don't have the minsup)
				for (Item i : conditionalFP.getHeaderTable()) {
					Set<Node> itemNodes = new HashSet<>();
					n = conditionalFP.getFirstNode(i);
					int itemsSup = 0;
					do {
						itemsSup += n.getNumberOfTransactions();
						itemNodes.add(n);
						n = n.getLinkNode();
					} while (n != null);

					if (itemsSup < minSupport) {
						for (Node nodeToDelete : itemNodes)
							conditionalFP.removeNode(nodeToDelete);
					}
				}
				ItemSet newItemSet = itemSet.clone();
				newItemSet.add(item);
				addItemSet(newItemSet);
				fpGrowth(conditionalFP, newItemSet, minSupport);
			}
		}
	}

	private void addItemSet(ItemSet is) {
		if (resultItemSetList.get(is.size()) == null)
			resultItemSetList.put(is.size(), new ItemSetList());
		resultItemSetList.get(is.size()).add(is);
	}
}
