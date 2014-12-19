package ca.concordia.cssanalyser.migration.topreprocessors;

public abstract class PreprocessorNode<T> {
	
	private final T realNode;
	
	public PreprocessorNode(T realPreprocessorNode) {
		this.realNode = realPreprocessorNode;
	}
	
	public T getRealNode() {
		return this.realNode;
	}
	
	public abstract void deleteChild(PreprocessorNode<T> childNode);
	
	public abstract void addChild(PreprocessorNode<T> child);

	public abstract PreprocessorNode<T> getParent();
	
	@Override
	public String toString() {
		if (realNode != null)
			return "PreprocessorNode: " + realNode.toString();
		return "Null PreprocessorNode";
	}
}
