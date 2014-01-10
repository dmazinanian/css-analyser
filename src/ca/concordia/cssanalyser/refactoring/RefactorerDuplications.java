package ca.concordia.cssanalyser.refactoring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.concordia.cssanalyser.analyser.duplication.apriori.Item;
import ca.concordia.cssanalyser.analyser.duplication.apriori.ItemSet;
import ca.concordia.cssanalyser.analyser.duplication.apriori.ItemSetList;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.SingleSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupedSelectors;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;



public class RefactorerDuplications {
	
	public StyleSheet refactor(StyleSheet originalStyleSheet, List<ItemSetList> itemSetLists) {

		StyleSheet refactoredStyleSheet = originalStyleSheet.clone();
		
		Set<Declaration> addedDeclarations = new HashSet<>();
				
		for (int i = itemSetLists.size() - 1; i >= 0; i--) {
			
			ItemSetList currentItemSetList = itemSetLists.get(i);
			
			for (ItemSet currentItemSet : currentItemSetList) {
				if (i == itemSetLists.size() - 1 || 
						(i < itemSetLists.size() - 2 && !itemSetLists.get(i + 1).containsSuperSet(currentItemSet))) {
					// Create a grouped selector for every duplication
					GroupedSelectors newGroupedSelector = new GroupedSelectors();
					for (Selector selector : currentItemSet.getSupport()) {
						if (selector instanceof SingleSelector)
							newGroupedSelector.add((SingleSelector)selector);
						else {
							for (SingleSelector atomicSelector : (GroupedSelectors)selector)
								newGroupedSelector.add(atomicSelector);
						}
							
					}
					for (Item item : currentItemSet) {
						 newGroupedSelector.addCSSRule(item.getFirstDeclaration());
						 addedDeclarations.addAll(item);
					}
					refactoredStyleSheet.addSelector(newGroupedSelector);
				}
			}
		}
		
		// Add all declarations which are only in one selector.
		for (Selector selector : originalStyleSheet.getAllSelectors()) {
			Selector toAdd = selector.clone();
			List<Declaration> declarationsToRemove = new ArrayList<>();
			for (Declaration declaration : toAdd.getDeclarations()) {
				if (addedDeclarations.contains(declaration)) {
					// Law of LoD :|
					declarationsToRemove.add(declaration);
				}
			}
			toAdd.getDeclarations().removeAll(declarationsToRemove);
			if (toAdd.getDeclarations().size() > 0) {
				refactoredStyleSheet.addSelector(toAdd);
			}
		}
		
		return refactoredStyleSheet;
	}
	
}
