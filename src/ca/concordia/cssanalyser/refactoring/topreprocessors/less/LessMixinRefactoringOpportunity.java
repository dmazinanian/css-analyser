package ca.concordia.cssanalyser.refactoring.topreprocessors.less;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.refactoring.topreprocessors.MixinRefactoringOpportunity;
import ca.concordia.cssanalyser.refactoring.topreprocessors.PreprocessorRefactoringOpportunity;

public class LessMixinRefactoringOpportunity implements MixinRefactoringOpportunity {
	
	private final Set<Declaration> originalDeclarations;
	
	public LessMixinRefactoringOpportunity() {
		originalDeclarations = new HashSet<>();
	}
	
	public Iterable<Declaration> getListOfOriginalDeclarations() {
		return originalDeclarations;
	}

	@Override
	public Iterator<Declaration> getRealDeclarations() {
		// TODO Auto-generated method stub
		return null;
	}


	
	
	
}
