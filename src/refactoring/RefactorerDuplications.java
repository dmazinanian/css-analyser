package refactoring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import analyser.duplication.apriori.Item;
import analyser.duplication.apriori.ItemSet;
import analyser.duplication.apriori.ItemSetList;

import CSSModel.StyleSheet;
import CSSModel.declaration.Declaration;
import CSSModel.selectors.AtomicSelector;
import CSSModel.selectors.GroupedSelectors;
import CSSModel.selectors.Selector;

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
						if (selector instanceof AtomicSelector)
							newGroupedSelector.add((AtomicSelector)selector);
						else {
							for (AtomicSelector atomicSelector : (GroupedSelectors)selector)
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
