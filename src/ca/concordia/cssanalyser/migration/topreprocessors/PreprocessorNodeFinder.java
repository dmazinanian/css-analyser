package ca.concordia.cssanalyser.migration.topreprocessors;


public abstract class PreprocessorNodeFinder<T> {
	
	public abstract PreprocessorNode<T> perform(PreprocessorNode<T> root, int start, int length);	
	
}
