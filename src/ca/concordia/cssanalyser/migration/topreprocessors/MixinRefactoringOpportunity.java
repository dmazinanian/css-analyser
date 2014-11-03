package ca.concordia.cssanalyser.migration.topreprocessors;

import java.util.Iterator;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class MixinRefactoringOpportunity extends PreprocessorRefactoringOpportunity {
	
	private final Iterable<Declaration> duplicatedDeclarations;
	private final Iterable<Selector> forSelectors;
	private final Iterable<DeclarationValueDifference> differences;
		
	
	public MixinRefactoringOpportunity(Iterable<Selector> forSelectors, Iterable<Declaration> declarations, Iterable<DeclarationValueDifference> differences) {
		this.duplicatedDeclarations = declarations;
		this.forSelectors = forSelectors;
		this.differences = differences;
	}
	
	public Iterable<Declaration> getRealDeclarations1() {
		return duplicatedDeclarations;
	}
	
	public Iterable<Selector> getSelectors() {
		return forSelectors;
	}
	
	
	public Iterator<DeclarationValueDifference> getDifferences() {
		return differences.iterator();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		// TODO
		return builder.toString();
	}

}
