package ca.concordia.cssanalyser.analyser.duplication.deprecated;
/*
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import duplication.Duplication;
import duplication.DuplicationsList;
import duplication.IdenticalEffects;
import duplication.IdenticalSelectors;
import duplication.IdenticalValues;
import duplication.OverriddenProperties;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.selectors.SingleSelector;


public class DuplicationFinder {
	/**
	 * This method finds the selectors which are repetitive in the list of all
	 * selectors. Although the duplicated selectors are not necessary because we
	 * have grouping in CSS.
	 * Similarity function:
	 * 
	 * @return An object of {@link DuplicationsList}
	 * /
	public DuplicationsList findIdenticalSelectors() {

		DuplicationsList duplicationList = new DuplicationsList();

		/*
		 * This is the list of the indices of already visited selectors I have
		 * used the indices so we won't rely on the equals() method of the
		 * selectors. Also, I am using a HashSet to get a constant time
		 * search.
		 * /
		Set<Integer> visited = new HashSet<>();

		List<SingleSelector> allAtomicSelectors = stylesheet.getAllAtomicSelectors();

		// So start from the first selector
		int currentSelectorIndex = -1;
		while (++currentSelectorIndex < allAtomicSelectors.size()) {

			SingleSelector currentSelector = allAtomicSelectors.get(currentSelectorIndex);

			if (visited.contains(currentSelectorIndex))
				continue;
			/*
			 * Start checking from next index in the temporary list. First we
			 * assume that a duplication has just happened. so we create the
			 * appropriate object for it (considering that we only can have
			 * one duplication object of every selector).
			 * /
			IdenticalSelectors duplication = new IdenticalSelectors(currentSelector);

			int checkingSelectorIndex =  currentSelectorIndex;
			
			while (++checkingSelectorIndex < allAtomicSelectors.size()) {
				
				SingleSelector checkingSelector = allAtomicSelectors.get(checkingSelectorIndex);

				if (currentSelector.equals(checkingSelector)) {
					// So it seems that we have found a duplication in selectors
					duplication.addSelector(checkingSelector);
					visited.add(checkingSelectorIndex);
				}

			}
			
			// If we have more than one occurrence of one selector in the list:
			if (duplication.getNumberOfOccurrences() > 1) {
				duplicationList.addDuplication(duplication);
			}

		}

		return duplicationList;
	}
	
	/**
	 * Finds all the duplications, where only values for different 
	 * properties across different selectors are exactly the same.
	 * @return An object of {@link DuplicationsList}
	 * /
	// Consider very carefully about different variations in the values.
	public DuplicationsList findIdenticalValues() {
		
		DuplicationsList duplicationList = new DuplicationsList();

		// Get a list of all declarations
		List<Declaration> allDeclarations = stylesheet.getAllDeclarations();
		
		// Yeah don't repeat for different declaration
		Set<Integer> visitedDeclarations = new HashSet<>();
		
		int currentDecIndex = -1;
		
		while (++currentDecIndex < allDeclarations.size()) {
			
			Declaration currentDeclaration = allDeclarations.get(currentDecIndex);

			if (visitedDeclarations.contains(currentDecIndex))
				continue;
			
			IdenticalValues duplication = new IdenticalValues();
			
			duplication.addDeclaration(currentDeclaration);

			// Start from the next index
			int checkingDecIndex = currentDecIndex;
			while (++checkingDecIndex < allDeclarations.size()) {

				Declaration checkingDeclaration = allDeclarations.get(checkingDecIndex);
				
				if (currentDeclaration.valuesEqual(checkingDeclaration)) {
					// Found the desirable duplication
					duplication.addDeclaration(checkingDeclaration);
					visitedDeclarations.add(checkingDecIndex);
				}
			}
			
			if (duplication.getNumberOfDeclarations() > 1) 
				duplicationList.addDuplication(duplication);

		}

		return duplicationList;
	}

	/**
	 * Finds the overriden values.
	 * @return
	 * /
	public DuplicationsList findOverridenValues(DuplicationsList identicalSelectorsDuplication) {
		return findIdenticalSelectorAndDeclaration(true, identicalSelectorsDuplication);
	}

	/**
	 * This method finds the cases in which the selector, property and values
	 * are all the same.
	 * @return
	 * /
	public DuplicationsList findIdenticalEffects(DuplicationsList identicalSelectorsDuplication) {
		return findIdenticalSelectorAndDeclaration(false, identicalSelectorsDuplication);
	}
	
	/**
	 * Checks for identical selectors, and based on the onlyCheckProperties
	 * value, checks to see whether there is an equal declaration (false value for onlyCheckProperties)
	 * or an equal property (true value for onlyCheckProperties).
	 * @param onlyCheckProperties Only check for properties to be the same
	 * @param duplicatedSelectors The result of {@link #findIdenticalSelectors()} method
	 * @return An object of DuplicationList
	 * /
	public DuplicationsList findIdenticalSelectorAndDeclaration(boolean onlyCheckProperties, DuplicationsList duplicatedSelectors) {
		
		DuplicationsList duplicationList = new DuplicationsList();

		if (duplicatedSelectors == null)
			duplicatedSelectors = findIdenticalSelectors();
		
		for (Duplication selectorDuplication : duplicatedSelectors) {
			
			// This is a list of identical selectors
			List<Selector> identicalSelectors = ((IdenticalSelectors)selectorDuplication).getListOfSelectors();
			
			Duplication duplication;
			
			if (onlyCheckProperties) {
				duplication = new OverriddenProperties(identicalSelectors.get(0));
			} else {
				duplication = new IdenticalEffects(identicalSelectors.get(0));
			}
			boolean mustAdd = false;

			int currentSelectorIndex = -1;
			while (++currentSelectorIndex < identicalSelectors.size()) {
				
				Selector currentSelector = identicalSelectors.get(currentSelectorIndex);
				List<Declaration> currentDeclarations = currentSelector.getDeclarations();
				
				/* For each declaration for the current selector, 
				 * check all selectors to see whther they have the same declarations or not
				 * /
				for (Declaration currentDeclaration : currentDeclarations) {
					
					List<Declaration> currentEqualDeclarations = new ArrayList<>();
					currentEqualDeclarations.add(currentDeclaration);
				
					int checkingSelectorIndex = currentSelectorIndex;
					while (++checkingSelectorIndex < identicalSelectors.size()) {
						Selector checkingSelector = identicalSelectors.get(checkingSelectorIndex);
						List<Declaration> checkingDeclarations = checkingSelector.getDeclarations();
	
						if (onlyCheckProperties) {
							for (Declaration checkingDeclaration : checkingDeclarations) {
								if (currentDeclaration.getProperty().equals(checkingDeclaration.getProperty())
										&& !currentDeclaration.valuesEqual(checkingDeclaration)) {
									currentEqualDeclarations.add(checkingDeclaration);
								}
							}

						} else {

							int index = checkingDeclarations.indexOf(currentDeclaration);

							if (index >= 0) {

								Declaration checkingDeclaration = checkingDeclarations.get(index); 
								currentEqualDeclarations.add(checkingDeclaration);
							}
						}

					}
					
					// If we have found some duplications, add the declarations.
					if (currentEqualDeclarations.size() > 1) {
						if (onlyCheckProperties) {
							((OverriddenProperties)duplication).addAllDeclarations(currentEqualDeclarations);
						}
						else {
							((IdenticalEffects)duplication).addAllDeclarations(currentEqualDeclarations);
						}
						mustAdd = true;
					}
				}
			}
			
			
			if (mustAdd)
				duplicationList.addDuplication(duplication);
			
			
		}

		return duplicationList;
	}
	
}*/
