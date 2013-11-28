package ca.concordia.cssanalyser.analyser.duplication.fpgrowth;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ca.concordia.cssanalyser.analyser.duplication.apriori.Item;



public class Node {
	
	private final Item item;
	private int numberOfTransactions;
	private final Map<Item, Node> itemToChildMap;
	private final Set<Node> children;
	private final long id;
	
	private FPTree ownerTree;
	private Node parent;
	private Node linkNode;
	
	/**
	 * Creates a root node
	 * @param ownerTree
	 */
	public Node() {
		this(null, null, 0);
	}
	
	public Node(Item item, FPTree ownerTree, int initialNumberOfTransactions) {
		itemToChildMap = new HashMap<>();
		children = new HashSet<>();
		this.item = item;
		this.ownerTree = ownerTree;
		setNumberOfTransactions(initialNumberOfTransactions);
		if (this.ownerTree != null)
			this.id = this.ownerTree.getNodeAutoID();
		else
			this.id = 0;
	}
	
	public Node getParent() {
		return this.parent;
	}
	
	public FPTree getOwnerTree() {
		return ownerTree;
	}
	
	public long getID() {
		return id;
	}
		
	public void addChild(Node node) {
		children.add(node);
		if (!itemToChildMap.containsKey(node.getItem()))
			itemToChildMap.put(node.getItem(), node);
		node.parent = this;
	}
	
	public void removeChild(Node node) {
		if (itemToChildMap.get(node.getItem()) == node) {
			itemToChildMap.remove(node.getItem());
		}
		Node nodeToDelete = node;
		for (Node child : children)
			if (child.getID() == node.getID()) {
				nodeToDelete = child;
				break;
			}
		children.remove(nodeToDelete);
	}
	
	public int getNumberOfTransactions() {
		return numberOfTransactions;
	}
	
	public void setNumberOfTransactions(int numberOfTransactions) {
		this.numberOfTransactions = numberOfTransactions;
	}
	
	public void incrementNumberOfTransactions() {
		this.numberOfTransactions++;
	}

	public Item getItem() {
		return item;
	}
	
	public Collection<Node> getChildern() {
		return children;
	}
	
	public Node getFirstChildForItem(Item forItem) {
		return itemToChildMap.get(forItem);
	}
	
	public Node getLinkNode() {
		return linkNode;
	}

	public void setLinkNode(Node linkNode) {
		this.linkNode = linkNode;
	}

	@Override
	public String toString() {
		if (this.getItem() == null)
			return "null";
		else
			return String.format("%s (%d)" , this.getItem().getFirstDeclaration(), this.getNumberOfTransactions());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
//		result = prime * result + ((item == null) ? 0 : item.hashCode());
//		result = prime * result
//				+ ((linkNode == null) ? 0 : linkNode.hashCode());
//		result = prime * result + numberOfTransactions;
//		result = prime * result
//				+ ((ownerTree == null) ? 0 : ownerTree.hashCode());
//		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (id != other.id)
			return false;
//		if (item == null) {
//			if (other.item != null)
//				return false;
//		} else if (!item.equals(other.item))
//			return false;
//		if (linkNode == null) {
//			if (other.linkNode != null)
//				return false;
//		} else if (!linkNode.equals(other.linkNode))
//			return false;
//		if (numberOfTransactions != other.numberOfTransactions)
//			return false;
//		if (ownerTree == null) {
//			if (other.ownerTree != null)
//				return false;
//		} else if (!ownerTree.equals(other.ownerTree))
//			return false;
//		if (parent == null) {
//			if (other.parent != null)
//				return false;
//		} else if (!parent.equals(other.parent))
//			return false;
		return true;
	}
	
	
}
