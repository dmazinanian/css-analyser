package ca.concordia.cssanalyser.refactoring.topreprocessors.less;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.refactoring.topreprocessors.DeclarationValueDifference;
import ca.concordia.cssanalyser.refactoring.topreprocessors.MixinRefactoringOpportunity;

public class LessMixinRefactoringOpportunity extends MixinRefactoringOpportunity {

	public LessMixinRefactoringOpportunity(Iterable<Selector> forSelectors,
			Iterable<Declaration> declarations,
			Iterable<DeclarationValueDifference> differences) {
		super(forSelectors, declarations, differences);
		// TODO Auto-generated constructor stub
	}

	 

	
	
	
	
	
}
