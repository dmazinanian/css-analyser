package ca.concordia.cssanalyser.refactoring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.concordia.cssanalyser.analyser.duplication.items.Item;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;



public class RefactorerDuplications {
	
	public static StyleSheet groupingRefactoring(StyleSheet originalStyleSheet, ItemSet itemset) {
		
		// First create a new grouped selector for refactoring
		GroupingSelector newGroupingSelector = new GroupingSelector();
		for (Selector selector : itemset.getSupport()) {
			if (selector instanceof GroupingSelector) {
				GroupingSelector grouping = (GroupingSelector)selector;
				for (BaseSelector baseSelector : grouping.getBaseSelectors()) {
					newGroupingSelector.add((BaseSelector)baseSelector.copyEmptySelector());
				}
			} else {
				newGroupingSelector.add((BaseSelector)selector.copyEmptySelector());
			}
		}
		

		Set<Declaration> declarationsToBeRemoved = new HashSet<>();
		for (Item currentItem : itemset) {
			
			// Add declarations to this new grouping selector
			newGroupingSelector.addDeclaration(currentItem.getDeclarationWithMinimumChars().clone());
			
			//Mark declarations to be deleted from returning stylesheet
			for (Declaration currentDeclaration : currentItem) {
				if (itemset.getSupport().contains(currentDeclaration.getSelector())) {
					if (currentDeclaration instanceof ShorthandDeclaration && ((ShorthandDeclaration)currentDeclaration).isVirtual()) {
						for (Declaration individual : ((ShorthandDeclaration)currentDeclaration).getIndividualDeclarations())
							declarationsToBeRemoved.add(individual);
					} else {
						declarationsToBeRemoved.add(currentDeclaration);
					}
				}
			}
		}
		
		// Create a new empty stylesheet (refactored one)
		StyleSheet refactoredStyleSheet = new StyleSheet();
		
		// Adding selectors to the refactored declarations
		for (Selector selectorToBeAdded : originalStyleSheet.getAllSelectors()) {
			Selector newSelector = selectorToBeAdded.copyEmptySelector();
			// Only add declaration which are not marked to the refactored stylesheet
			for (Declaration d : selectorToBeAdded.getDeclarations()) {
				if (!declarationsToBeRemoved.contains(d)) {
					newSelector.addDeclaration(d.clone()); 
				}
				
			}
			refactoredStyleSheet.addSelector(newSelector);
		}
		
		// Add the new grouping selector at the end of the refactored stylesheet
		refactoredStyleSheet.addSelector(newGroupingSelector);
		
		// Remove empty selectors from refactored stylesheet
		List<Selector> selectorsToBeRemoved = new ArrayList<>(); 
		for (Selector selector : refactoredStyleSheet.getAllSelectors()) {
			if (selector.getDeclarations().size() == 0)
				selectorsToBeRemoved.add(selector);
		}
		refactoredStyleSheet.getAllSelectors().removeAll(selectorsToBeRemoved);
		
		return refactoredStyleSheet;

	}

}
