package ca.concordia.cssanalyser.migration.topreprocessors;

public interface PreprocessorCodePrinter<T> {
	
	public String getString(T styleSheet);
	
}
