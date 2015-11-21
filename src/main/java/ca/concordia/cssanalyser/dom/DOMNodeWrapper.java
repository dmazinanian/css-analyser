package ca.concordia.cssanalyser.dom;

import java.util.Set;

import org.w3c.dom.Node;

import ca.concordia.cssanalyser.cssmodel.selectors.PseudoClass;

/**
 * This class represents a DOM node, in addition 
 * to the state of the node to be selected. For
 * example, in case of <code>.class:hover</code>, the DOM element
 * of this class will be the one selected by <code>.class</code>,
 * and the state would be <i>hover</i>.
 * This class helps us in dealing with the pseudo classes for which
 * there is no XPath expression. 
 * 
 * @author Davood Mazinanian
 */
public class DOMNodeWrapper {
	
	private final Node domNode;
	private final Set<PseudoClass> pseudoClasses;
	
	public DOMNodeWrapper(Node node, Set<PseudoClass> pseudoClasses) {
		this.domNode = node;
		this.pseudoClasses = pseudoClasses;
	}
	
	public Node getNode() {
		return this.domNode;
	}
	
	public Set<PseudoClass> getPseudoClasses() {
		return this.pseudoClasses;
	}

	@Override
	public String toString() {
		String toReturn = domNode.toString();
		for (PseudoClass pseudoClass : pseudoClasses) {
			toReturn += ":" + pseudoClass;
		}
		return toReturn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domNode == null) ? 0 : domNode.hashCode());
		result = prime * result
				+ ((pseudoClasses == null) ? 0 : pseudoClasses.hashCode());
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
		DOMNodeWrapper other = (DOMNodeWrapper) obj;
		if (domNode == null) {
			if (other.domNode != null)
				return false;
		} else if (!domNode.equals(other.domNode))
			return false;
		if (pseudoClasses == null) {
			if (other.pseudoClasses != null)
				return false;
		} else if (!pseudoClasses.equals(other.pseudoClasses))
			return false;
		return true;
	}
	
	
	
}
