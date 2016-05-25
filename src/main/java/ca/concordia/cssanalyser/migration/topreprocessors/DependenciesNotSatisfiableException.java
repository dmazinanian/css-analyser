package ca.concordia.cssanalyser.migration.topreprocessors;

public class DependenciesNotSatisfiableException extends Exception {

	private static final long serialVersionUID = 1871432074730401282L;

	public DependenciesNotSatisfiableException(String message) {
        super(message);
    }
	
	public DependenciesNotSatisfiableException(Throwable throwable) {
		super(throwable);
	}
}
