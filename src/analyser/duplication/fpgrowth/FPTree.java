package analyser.duplication.fpgrowth;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import analyser.duplication.apriori.Item;

public class FPTree {
	
	private final Node root;
	private final Map<Item, Node> itemNodeMap;
	private final TreeSet<Item> headerTable;
	
	public FPTree() {
		root = new Node(null, null);
		itemNodeMap = new HashMap<Item, Node>();
		headerTable = new TreeSet<>();
	}
	
	public Node getRoot() {
		return root;
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
		while (node != null) {
			if (node.getChildern().size() > 1)
				return false;
			if (node.getChildern().iterator().hasNext())
				node = node.getChildern().iterator().next();
			else
				break;
		}
		return true;
	}
	
	public boolean isEmpty() {
		return root.getChildern().size() == 0;
	}
	
	public void removeNode(Node node) {
		// Update linkes
		Node previous = itemNodeMap.get(node.getItem());
		// If node is the first node in the linked-list:
		if (previous == node) {
			if (node.getLinkNode() == null)
				itemNodeMap.remove(node.getItem());
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
	public TreeSet<Item> getHeaderTable() {
		return headerTable; 
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		getTreeString(sb, root);
		sb.append("\n-------------\n");
		for (Item i : itemNodeMap.keySet())
		{
			Node node = itemNodeMap.get(i);
			sb.append(i + " -> " + node +" -> ");
			while (node.getLinkNode() != null) {
				sb.append(node.getLinkNode() + " -> ");
				node = node.getLinkNode();
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private void getTreeString(StringBuilder stringBuilder, Node node) {
		stringBuilder.append(node.toString() + "(");
		for (Node child : node.getChildern())
			stringBuilder.append(child + " ");
		stringBuilder.append(")\n");
		for (Node child : node.getChildern())
			getTreeString(stringBuilder, child);
	}

	public boolean containsSinglePath() {
		// TODO Auto-generated method stub
		return false;
	}
	
	// TODO: check if dotted links are OK with the example
}
