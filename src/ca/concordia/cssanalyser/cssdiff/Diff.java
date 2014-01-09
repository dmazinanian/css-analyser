package ca.concordia.cssanalyser.cssdiff;

import java.util.Set;

import ca.concordia.cssanalyser.cssdiff.differences.ChangedDeclarationValueDifference;
import ca.concordia.cssanalyser.cssdiff.differences.DeclarationsMergedDifference;
import ca.concordia.cssanalyser.cssdiff.differences.Difference;
import ca.concordia.cssanalyser.cssdiff.differences.DifferenceList;
import ca.concordia.cssanalyser.cssdiff.differences.RemovedDeclarationDifference;
import ca.concordia.cssanalyser.cssdiff.differences.RemovedSelectorDifference;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class Diff {
	
	public static DifferenceList diff(StyleSheet styleSheet1, StyleSheet styleSheet2) {
		
		DifferenceList differences = new DifferenceList(styleSheet1, styleSheet2);
		
		for (Selector selector1 : styleSheet1.getAllSelectors()) {
			
			boolean selectorFound = false;
			for (Selector selector2 : styleSheet2.getAllSelectors()) {
				
				if (selector2.selectorEquals(selector1)) {
					
					selectorFound = true;
					
					// Find changes in declarations
					boolean declarationFound = false;
					// When we check a declaration in the second selector, it means that it already exists in the first one
					boolean[] checked = new boolean[selector2.getDeclarations().size()];
					
					for (Declaration declarationInSelector1 : selector1.getDeclarations()) {
						
						for (int j = 0; j < selector2.getDeclarations().size(); j++) {
							Declaration declarationInSelector2 =  selector2.getDeclarations().get(j);
							if (declarationInSelector1.declarationEquals(declarationInSelector2)) {
								declarationFound = checked[j] = true;
								break;
							} else if (declarationInSelector1.getProperty().equals(declarationInSelector2.getProperty())) {
								declarationFound = checked[j] = true;
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
							for (int j = 0; j < selector2.getDeclarations().size(); j++) {
								Declaration declarationInSelector2 =  selector2.getDeclarations().get(j);
								if (!checked[j] && declarationInSelector2 instanceof ShorthandDeclaration &&
										appropriateShorthandProperties.contains(declarationInSelector2.getProperty())) {
									// It seems that the declaration is merged.
									// Lets see if there is already a corresponding difference in the differences list
									boolean differenceFound = false;
									for (Difference d : differences) {
										if (d instanceof DeclarationsMergedDifference) {
											DeclarationsMergedDifference currentMergedDifference = (DeclarationsMergedDifference)d;
											if (currentMergedDifference.
													getShorthandDeclaration().
													getProperty().equals(declarationInSelector2.getProperty())) {
												currentMergedDifference.addIndividualDeclaration(declarationInSelector1);
												differenceFound = true;
												break;
											}
										}	
									}
									
									if (!differenceFound) {
										DeclarationsMergedDifference currentMergedDifference = 
												new DeclarationsMergedDifference(styleSheet1, styleSheet2, 
														(ShorthandDeclaration)declarationInSelector2);
										differences.add(currentMergedDifference);
										currentMergedDifference.addIndividualDeclaration(declarationInSelector1);
									}
									
									declarationFound = true;
								}
							}
							
							if (!declarationFound) {
								Difference difference = new RemovedDeclarationDifference(styleSheet1, styleSheet2, declarationInSelector1);
								differences.add(difference);
							}
						} else {
							;
							break;
						}
					}
										
					break;
				}
				
			}
			if (!selectorFound) {
				Difference difference = new RemovedSelectorDifference(styleSheet1, styleSheet2, selector1);
				differences.add(difference);
			} //else {
//				// Finish with this selector
//				break;
//			}
			
		}
		
//		for (Selector selector : styleSheet2.getAllSelectors()) {
//			boolean found = false;
//			for (Selector s : styleSheet1.getAllSelectors()) {
//				if (s.selectorEquals(selector)) {
//					found = true;
//					break;
//				}
//			}
//			if (!found) {
//				AbstractDifference difference = new RemovedSelectorDifference(styleSheet2, styleSheet1, selector);
//				differences.add(difference);
//			}
//		}
		
		return differences;
	}
}
