package ca.concordia.cssanalyser.refactoring.dependencies;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A dependency between two elements in the style sheet.
 * The type of the edge is provided using E
 * @author Davood Mazinanian
 *
 * @param <E>
 */
public abstract class CSSDependency<E> {
	private Set<E> labels = new LinkedHashSet<>();
	private final CSSDependencyNode fromNode;
	private final CSSDependencyNode toNode;
	
	public CSSDependency(CSSDependencyNode fromNode, CSSDependencyNode toNode) {
		this.fromNode = fromNode;
		this.toNode = toNode;
	}
	
	public Set<E> getDependencyLabels() {
		return this.labels;
	}
	
	public boolean addDependencyLabel(E label) {
		return this.labels.add(label);
	}
	
	public boolean removeDependencyLabel(E label) {
		return this.labels.remove(label);
	}
	
	public CSSDependencyNode getStartingNode() {
		return this.fromNode;
	}
	
	public CSSDependencyNode getEndingNode() {
		return this.toNode;
	}
	
	protected String getLabelsString() {
		StringBuilder builder = new StringBuilder();
		for (E label : getDependencyLabels()) {
			builder.append(label + ", ");
		}
		if (builder.length() > 2)
			builder.delete(builder.length() - 2, builder.length());
		return builder.toString();
	}
}
