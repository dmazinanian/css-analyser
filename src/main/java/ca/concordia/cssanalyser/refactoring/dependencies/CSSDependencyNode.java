package ca.concordia.cssanalyser.refactoring.dependencies;

public interface CSSDependencyNode {
	public boolean nodeEquals(CSSDependencyNode otherCSSDependencyNode);
    // where there's an equals there should be a hashCode
    public int nodeHashCode();
}
