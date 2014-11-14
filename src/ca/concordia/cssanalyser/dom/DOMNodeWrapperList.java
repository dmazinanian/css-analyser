package ca.concordia.cssanalyser.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMNodeWrapperList implements Iterable<DOMNodeWrapper> {
	
	private Set<DOMNodeWrapper> allNodes = new HashSet<>();
	private Map<Node, Set<DOMNodeWrapper>> nodesMap = new HashMap<>();
	
	public void add(DOMNodeWrapper wrapperNode) {
		Set<DOMNodeWrapper> wrapperNodesSet = nodesMap.get(wrapperNode.getNode());
		if (wrapperNodesSet == null) {
			wrapperNodesSet = new HashSet<>();	
		}
		wrapperNodesSet.add(wrapperNode);
		nodesMap.put(wrapperNode.getNode(), wrapperNodesSet);
		allNodes.add(wrapperNode);
	}
	
	public void addAll(Iterable<? extends DOMNodeWrapper> nodeWrapperSet) {
		for (DOMNodeWrapper dnr : nodeWrapperSet)
			add(dnr);
	}	
	
	public Set<DOMNodeWrapper> getNodeWrappersForNode(Node node) {
		return nodesMap.get(node);
	}

	@Override
	public Iterator<DOMNodeWrapper> iterator() {
		return allNodes.iterator();
	}
	

	public List<Node> getAllDescendentNodes(DOMNodeWrapper nodeWrapper) {
		List<Node> children = new ArrayList<>();
		populateChildsRecursive(nodeWrapper.getNode(), children);
		return children;
	}
	
	private void populateChildsRecursive(Node parentDomNode, List<Node> children) {
		NodeList nodeList = parentDomNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			children.add(n);
			populateChildsRecursive(n, children);
		}
		
	}

	public int size() {
		return allNodes.size();
	}
	
	@Override
	public String toString() {
		return nodesMap.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((allNodes == null) ? 0 : allNodes.hashCode());
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
		DOMNodeWrapperList other = (DOMNodeWrapperList) obj;
		if (allNodes == null) {
			if (other.allNodes != null)
				return false;
		} else if (!allNodes.equals(other.allNodes))
			return false;
		return true;
	}
}
