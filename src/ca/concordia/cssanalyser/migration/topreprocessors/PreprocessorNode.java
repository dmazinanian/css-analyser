package ca.concordia.cssanalyser.migration.topreprocessors;

public abstract class PreprocessorNode<T> {
	
	private final T realNode;
	
	public PreprocessorNode(T realPreprocessorNode) {
		this.realNode = realPreprocessorNode;
	}
	
	public T getRealNode() {
		return this.realNode;
	}
	
}
