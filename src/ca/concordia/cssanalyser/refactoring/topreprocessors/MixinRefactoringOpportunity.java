package ca.concordia.cssanalyser.refactoring.topreprocessors;

import java.util.Iterator;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;

public interface MixinRefactoringOpportunity extends PreprocessorRefactoringOpportunity {
	
	public Iterator<Declaration> getRealDeclarations();

}
