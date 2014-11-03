package ca.concordia.cssanalyser.migration.topreprocessors.less;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.migration.topreprocessors.DeclarationValueDifference;
import ca.concordia.cssanalyser.migration.topreprocessors.MixinRefactoringOpportunity;

public class LessMixinRefactoringOpportunity extends MixinRefactoringOpportunity {

	public LessMixinRefactoringOpportunity(Iterable<Selector> forSelectors,
			Iterable<Declaration> declarations,
			Iterable<DeclarationValueDifference> differences) {
		super(forSelectors, declarations, differences);
		// TODO Auto-generated constructor stub
	}

	 

	
	
	
	
	
}
