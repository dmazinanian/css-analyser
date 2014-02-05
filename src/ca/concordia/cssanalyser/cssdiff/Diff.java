package ca.concordia.cssanalyser.cssdiff;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ca.concordia.cssanalyser.cssdiff.differences.ChangedDeclarationValueDifference;
import ca.concordia.cssanalyser.cssdiff.differences.Difference;
import ca.concordia.cssanalyser.cssdiff.differences.DifferenceList;
import ca.concordia.cssanalyser.cssdiff.differences.IndividualDeclarationsMergedDifference;
import ca.concordia.cssanalyser.cssdiff.differences.RemovedDeclarationDifference;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class Diff {
	
//	private static class SelectorMapping {
//		private Selector selector;
//		private boolean isMapped = false;
//		public boolean couldBeMapped(Selector selector) {
//			return selector.selectorEquals(this.selector);
//		}
//		public boolean isMapped() {
//			return isMapped;
//		}
//		public static SelectorMapping getMapping(Selector s) {
//			if (s instanceof GroupingSelector)
//				return new GroupedSelectorMapping();
//			else
//				return new SelectorMapping();
//		}
//	}
//	
//	private class GroupedSelectorMapping extends SelectorMapping {
//		@Override
//		public boolean couldBeMapped(Selector selector) {
//			// TODO Auto-generated method stub
//			return false;
//		}
//		@Override
//		public boolean isMapped() {
//			// TODO Auto-generated method stub
//			return false;
//		}
//	}
	
	
	public static DifferenceList diff(StyleSheet styleSheet1, StyleSheet styleSheet2) {
		
		DifferenceList differences = new DifferenceList(styleSheet1, styleSheet2);
		
		Set<Selector> alreadyMapped = new HashSet<>();
		for (Selector selector1 : styleSheet1.getAllSelectors()) {
			
			Selector candidateMapped = null;
			for (Selector selector2 : styleSheet2.getAllSelectors()) {
				
				if (alreadyMapped.contains(selector2) && candidateMapped == null || distance(selector2, selector1) < distance(candidateMapped, selector1)) {
					candidateMapped = selector2;
				}
				
			}
			
			if (candidateMapped != null) {
				
			} else {
				
			}
			
			
		}
		
		// The first Selector is the selector in the second stylesheet
//		Map<Selector, Selector> selectorMapping = new HashMap<>();
//		
//		for (Selector selector1 : styleSheet1.getAllSelectors()) {
//			
//			Selector candidateMapped = null;
//			
//			for (Selector selector2 : styleSheet2.getAllSelectors()) {
//				
//				if (selector2.selectorEquals(selector1)) {
//					
//					// If candidate selector has not been yet found or 
//					// the new selector has less distance with the candidate
// 					if (candidateMapped == null || distance(selector2, selector1) < distance(candidateMapped, selector1)) {
//						candidateMapped = selector2;
//					}
//				}
//				
//			}
//			
//			if (candidateMapped != null) {
//				
//				// Check to see whether two selectors are merged.
//				// If the mapped selector is already mapped to another selector as well
//				if (selectorMapping.get(candidateMapped) != null) {
//					// There should be a merging
//					
//					// Search for the existing difference 
//					// TODO: use a map? selector -> difference
//					boolean differenceFound = false;
//					for (Difference difference : differences) {
//						if (difference instanceof SelectorsMergedDifference) {
//							SelectorsMergedDifference selectorMergedDifference = (SelectorsMergedDifference)difference;
//							if (selectorMergedDifference.getFinalselector().selectorEquals(candidateMapped)) {
//								selectorMergedDifference.addMergedSelector(selector1);
//								differenceFound = true;
//								break;
//							}
//						}
//					}
//					
//					if (!differenceFound) {
//						SelectorsMergedDifference selectorMergedDifference =
//								new SelectorsMergedDifference(styleSheet1, styleSheet2, candidateMapped);
//						selectorMergedDifference.addMergedSelector(selectorMapping.get(candidateMapped));
//						selectorMergedDifference.addMergedSelector(selector1);
//						differences.add(selectorMergedDifference);
//					}
//					
//				}
//				
//				selectorMapping.put(candidateMapped, selector1);
//				
//				findDifferencesInDeclarations(selector1, candidateMapped, styleSheet1, styleSheet2, differences);
//				
//				
//				
//			} else {
//				// We couldn't map this selector. So maybe it is renamed, but we don't know yet
//				Difference difference = new RemovedSelectorDifference(styleSheet1, styleSheet2, selector1);
//				differences.add(difference);
//			} 
//			
//		}
		
//		for (Selector selector2 : styleSheet2.getAllSelectors()) {
//			// If there is a new selector in the second style sheet
//			if (selectorMapping.get(selector2) == null) {
//				if (selector2 instanceof GroupingSelector) {
//					for (Selector singleSelector : ((GroupingSelector)selector2).getSingleSelectors()) {
//						for (Selector s : styleSheet1.getAllSelectors()) {
//							if (singleSelector.selectorEquals(s)) {
//								// There was a grouping?!
//								
//							}
//						}
//					}
// 				}
//			}
//		}
			
		return differences;
	}

	/**
	 * Finds differences between the declarations of two selectors
	 * @param selector1
	 * @param candidateMapped
	 * @param styleSheet1
	 * @param styleSheet2
	 * @param differences
	 */
	private static void findDifferencesInDeclarations(Selector selector1, Selector candidateMapped, 
			StyleSheet styleSheet1, StyleSheet styleSheet2,
			DifferenceList differences) {

		// Find changes in declarations

		Map<Declaration, Boolean> declarationAlreadyMapped = new HashMap<>();

		for (Declaration declarationInSelector1 : selector1.getDeclarations()) {

			boolean declarationFound = false;

			for (Declaration declarationInSelector2 : candidateMapped.getDeclarations()) {

				// If the declaration in the second selector is a shorthand, it might be mapped to more than one 
				// declaration in the first selector.
				if (!(declarationInSelector2 instanceof ShorthandDeclaration) 
						&& declarationAlreadyMapped.get(declarationInSelector2) != null 
						&&  declarationAlreadyMapped.get(declarationInSelector2) )
					continue;

				if (declarationInSelector1.declarationEquals(declarationInSelector2)) {
					declarationFound = true;
					declarationAlreadyMapped.put(declarationInSelector2, true);
					break;
				} else if (declarationInSelector1.getProperty().equals(declarationInSelector2.getProperty())) {
					declarationFound = true;
					declarationAlreadyMapped.put(declarationInSelector2, true);
					Difference difference = new ChangedDeclarationValueDifference(styleSheet1, styleSheet2, 
							declarationInSelector1, declarationInSelector2);
					differences.add(difference);
					break;
				}
			}

			// If a declaration could not be found, either it is removed or merged!
			if (!declarationFound) {
				
				// Lets see if it is merged in a shorthand
				Set<String> appropriateShorthandProperties = 
						ShorthandDeclaration.getShorthandPropertyNames(declarationInSelector1.getProperty());
				
				for (Declaration declarationInSelector2 : candidateMapped.getDeclarations()) {
					if (declarationInSelector2 instanceof ShorthandDeclaration &&
							appropriateShorthandProperties.contains(declarationInSelector2.getProperty())) {
						// It seems that the declaration is merged.
						// Lets see if there is already a corresponding difference in the differences list
						boolean differenceForShorthandFound = false;
						for (Difference d : differences) {
							if (d instanceof IndividualDeclarationsMergedDifference) {
								IndividualDeclarationsMergedDifference currentMergedDifference = (IndividualDeclarationsMergedDifference)d;
								if (currentMergedDifference.
										getShorthandDeclaration().
										getProperty().equals(declarationInSelector2.getProperty())) {
									currentMergedDifference.addIndividualDeclaration(declarationInSelector1);
									differenceForShorthandFound = true;
									break;
								}
							}	
						}

						if (!differenceForShorthandFound) {
							IndividualDeclarationsMergedDifference currentMergedDifference = 
									new IndividualDeclarationsMergedDifference(styleSheet1, styleSheet2, 
											(ShorthandDeclaration)declarationInSelector2);
							differences.add(currentMergedDifference);
							currentMergedDifference.addIndividualDeclaration(declarationInSelector1);
						}

						declarationFound = true;
						break;
					}
				}

				if (!declarationFound) {
					Difference difference = new RemovedDeclarationDifference(styleSheet1, styleSheet2, declarationInSelector1);
					differences.add(difference);
				}
			} else {
				break;
			}
		}
		
	}

	/**
	 * Calculates the distance between two selectors based on their declarations.
	 * Uses the Jackard Similarity Coefficient. The distance would be computed based on the 
	 * property names of the declarations
	 * @param selector2
	 * @param selector1
	 * @return
	 */
	private static float distance(Selector selector1, Selector selector2) {
		
		Set<String> selector1Properties = new HashSet<>();
		Set<String> selector2Properties = new HashSet<>();
		
		for (Declaration d : selector1.getDeclarations())
			selector1Properties.add(d.getProperty());
		
		for (Declaration d : selector2.getDeclarations())
			selector2Properties.add(d.getProperty());
		
		Set<String> union = new HashSet<>();
		Set<String> intersection = new HashSet<>();
		
		union.addAll(selector1Properties);
		union.addAll(selector2Properties);
	
		intersection.addAll(selector1Properties);
		intersection.retainAll(selector2Properties);
		
		return 1 - (union.size() / (float)intersection.size());
	}
}
