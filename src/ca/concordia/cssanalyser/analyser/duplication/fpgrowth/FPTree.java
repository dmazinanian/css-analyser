package ca.concordia.cssanalyser.analyser.duplication.fpgrowth;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ca.concordia.cssanalyser.analyser.duplication.items.Item;


public class FPTree {
	
	private final Node root;
	private final Map<Item, Node> itemNodeMap;
	private final TreeSet<Item> headerTable;
	private long nodeAutoID = -1;
	
	public FPTree() {
		root = new Node();
		itemNodeMap = new HashMap<Item, Node>();
		headerTable = new TreeSet<>();
	}
	
	public Node getRoot() {
		return root;
	}
	
	public long getNodeAutoID() {
		return ++nodeAutoID;
	}
	
	public void addNodeLinkItem(Node node) {
		Item item = node.getItem();
		Node previousNode = itemNodeMap.get(item);
		if (previousNode != null && previousNode != node) {
			node.setLinkNode(previousNode);
		}
		itemNodeMap.put(item, node);
		headerTable.add(item);
	}
	
	public Node getFirstNode(Item item) {
		return itemNodeMap.get(item);
	}
	
	public boolean hasASinglePath() {
		Node node = root;
		while (node.getChildern().iterator().hasNext()) {
			if (node.getChildern().size() > 1)
				return false;
			node = node.getChildern().iterator().next();
		}
		return true;
	}
	
	public boolean isEmpty() {
		return root.getChildern().size() == 0;
	}
	
	public void removeNode(Node node) {
		// Update links
		Node previous = itemNodeMap.get(node.getItem());
		// If node is the first node in the linked-list:
		if (previous == node) {
			if (node.getLinkNode() == null) {
				itemNodeMap.remove(node.getItem());
				headerTable.remove(node.getItem());
			}
			else
				itemNodeMap.put(node.getItem(), node.getLinkNode());
		} else {
			// Find the node pointing to this node as next
			while (previous != null && previous.getLinkNode() != node)
				previous = previous.getLinkNode();
			if (previous != null)
				previous.setLinkNode(node.getLinkNode());
		}
		
		// Connect children to the parent of this node
		for (Node child : node.getChildern())
			node.getParent().addChild(child);
		
		node.getParent().removeChild(node);
	}
	
	public Map<Item, Node> getItemNodeMap() {
		return itemNodeMap;
	}
	
	/**
	 * Returns the header table. Items are sorted ascending
	 * @return
	 */
	public SortedSet<Item> getHeaderTable() {
		return headerTable; 
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
//		getTreeString(sb, root);
//		sb.append("\n-------------\n");
//		for (Item i : itemNodeMap.keySet())
//		{
//			Node node = itemNodeMap.get(i);
//			sb.append(i + " -> " + node +" -> ");
//			while (node.getLinkNode() != null) {
//				sb.append(node.getLinkNode() + " -> ");
//				node = node.getLinkNode();
//			}
//			sb.append("\n");
//		}
		getTreeString(sb, root);
		return sb.toString();
		
	}

	private void getTreeString(StringBuilder stringBuilder, Node node) {
		/*stringBuilder.append(node.toString() + "(");
		for (Node child : node.getChildern())
			stringBuilder.append(child + " ");
		stringBuilder.append(")\n");
		for (Node child : node.getChildern())
			getTreeString(stringBuilder, child);
		*/
		if (node == root) {
			stringBuilder.append(String.format("tree.fpgrowth(%s)", format("null")));
		} else if (node.getChildern().size() == 0) {
			stringBuilder.append(String.format("leaf(%s)", 
					format(node.getItem().getFirstDeclaration().getProperty() + 
							"$^{" + node.getNumberOfTransactions() + "}$" +
							node.getItem().getSupport())));
		} else {
			stringBuilder.append(String.format("tree(%s)", 
					format(node.getItem().getFirstDeclaration().getProperty() + 
							"$^{" + node.getNumberOfTransactions() + "}$" +
							node.getItem().getSupport())));
		}
		if (node.getChildern().size() != 0) {
			stringBuilder.append("(\n");
			for (Node child : node.getChildern()) {
				getTreeString(stringBuilder, child);
			}
			stringBuilder.append("\n)");
		}
		
		if (node == root)
			stringBuilder.append(";\ndrawtrees(fpgrowth);");
		else
			stringBuilder.append(", ");
	}
	
	private String format(String s) {
		return "btex " + s.replace("#", "\\#").replace("%", "\\%").replace("_", "\\_") + " etex";
	}

	void prune(int minSupport) {
		Node n;
		// Remove unnecessary nodes (those which don't have the minsup)
		Set<Node> nodesToDelete = new HashSet<>();
		for (Item i : getHeaderTable()) {
			Set<Node> currentNodes = new HashSet<>();
			n = getFirstNode(i);
			int itemsSup = 0;
			do {
				itemsSup += n.getNumberOfTransactions();
				currentNodes.add(n);
				n = n.getLinkNode();
			} while (n != null);
	
			if (itemsSup < minSupport) {
				nodesToDelete.addAll(currentNodes);
			}
		}
		
		for (Node nodeToDelete : nodesToDelete)
			removeNode(nodeToDelete);
	
	}

	public int getTotalSupport(Item item) {
		Node n = getFirstNode(item);
		int support = 0;
		while (n != null) {
			support += n.getNumberOfTransactions();
			n = n.getLinkNode();
		}
		return support;
	}
	
}
