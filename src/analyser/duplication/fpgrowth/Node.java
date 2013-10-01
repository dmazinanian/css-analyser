package analyser.duplication.fpgrowth;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import analyser.duplication.apriori.Item;


public class Node {
	
	private final Item item;
	private int numberOfTransactions;
	private final Map<Item, Node> children;
	private Node parent;
	private Node linkNode;
	
	public Node(Item item, Node parent) {
		this(item, parent, 0);
	}
	
	public Node(Item item, Node parent, int initialNumberOfTransactions) {
		children = new HashMap<>();
		this.item = item;
		setNumberOfTransactions(initialNumberOfTransactions);
		this.parent = parent;
	}
	
	public Node getParent() {
		return this.parent;
	}
	
	//public void setParent(Node newParent) {
	//	parent = newParent;
	//}
	
	public void addChild(Node node) {
		children.put(node.getItem(), node);
		node.parent = this;
	}
	
	public void removeChild(Node node) {
		children.remove(node.getItem());
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
		return children.values();
	}
	
	public Node getChild(Item forItem) {
		return children.get(forItem);
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
	public Node clone() {
		Node newNode = new Node(this.item, this.parent, this.numberOfTransactions);
		newNode.children.putAll(this.children);
		newNode.linkNode = this.linkNode;
		return newNode;
	}
}
