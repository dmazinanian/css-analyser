package ca.concordia.cssanalyser.migration.topreprocessors;

public abstract class PreprocessorNodeFinder<T, E> {
	
	private final T lessStyleSheet;
	
	public PreprocessorNodeFinder(T lessStyleSheet) {
		this.lessStyleSheet = lessStyleSheet;
	}
	
	public T getRealStyleSheet() {
		return this.lessStyleSheet;
	}
	
	/**
	 * Performs finding the node starting from the given root node in the style sheet
	 * @param root
	 * @param start
	 * @param length
	 * @return
	 */
	public abstract PreprocessorNode<E> perform(PreprocessorNode<E> root, int start, int length);	
	
	/**
	 * Performs finding the node starting from the root node of the style sheet
	 * @param start
	 * @param lentgh
	 * @return
	 */
	public abstract PreprocessorNode<E> perform(int start, int lentgh);
	
}
