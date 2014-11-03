package ca.concordia.cssanalyser.analyser;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import ca.concordia.cssanalyser.analyser.duplication.DuplicationDetector;
import ca.concordia.cssanalyser.analyser.duplication.DuplicationIncstanceList;
import ca.concordia.cssanalyser.analyser.duplication.DuplicationInstance;
import ca.concordia.cssanalyser.analyser.duplication.items.Item;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSetList;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.dom.DOMHelper;
import ca.concordia.cssanalyser.dom.Model;
import ca.concordia.cssanalyser.io.IOHelper;
import ca.concordia.cssanalyser.parser.flute.FluteCSSParser;
import ca.concordia.cssanalyser.refactoring.RefactorDuplications;
import ca.concordia.cssanalyser.refactoring.RefactorToSatisfyDependencies;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSDependencyDetector;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSDependencyDifferenceList;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;


/**
 * @author Davood Mazinanian
 * 
 */
public class CSSAnalyser {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CSSAnalyser.class);
	
	private final Model model;
	private boolean doApriori = false;
	private boolean doFPGrowth = true;
	private boolean dontUseDOM = false;
	private boolean compareAprioriAndFPGrowth = false;
	private final String folderPath;
	
	/**
	 * Through this constructor, one should pass the
	 * folder containing all CSS files (or a single CSS file). Program will search
	 * for all files with extension ".css" in the given folder and conducts the 
	 * analysis for each file.
	 * @param cssContainingFolder
	 * @param domStateHTMLPath
	 * @throws FileNotFoundException Could not find the given directory or css file.
	 */
	public CSSAnalyser(String domStateHTMLPath, String cssContainingFolderOrFilePath) throws FileNotFoundException {
		
		if (!IOHelper.exists(cssContainingFolderOrFilePath))
			throw new FileNotFoundException("Folder not found: " + cssContainingFolderOrFilePath);
		
		List<File> cssFiles = null;
		
		if (IOHelper.isFolder(cssContainingFolderOrFilePath)) {
			this.folderPath = cssContainingFolderOrFilePath;
			// Search for all CSS files in this folder
			cssFiles = IOHelper.searchForFiles(cssContainingFolderOrFilePath, "css");
			if (cssFiles.size() == 0) {
				LOGGER.error("There is no CSS file in " + cssContainingFolderOrFilePath);
			}
		} else {
			cssFiles = new ArrayList<>();
			cssFiles.add(new File(cssContainingFolderOrFilePath));
			this.folderPath = IOHelper.getContainingFolder(cssContainingFolderOrFilePath);
		}
		
		Document document = null;
		if (domStateHTMLPath != null)
			document = DOMHelper.getDocument(domStateHTMLPath);
		else 
			dontUseDOM = true;
		model = new Model(document);
		parseStyleSheets(cssFiles);
		
	}
	
	public CSSAnalyser(String cssContainingFolderOrCSSFilePath) throws FileNotFoundException {
		this(null, cssContainingFolderOrCSSFilePath);
		
	}
	
	/**
	 * Identifies whether analyzer should do Apriori to find
	 * frequent declarations
	 * @param value
	 */
	public void setApriori(boolean value) {
		doApriori = value;
	}
	
	/**
	 * Identifies whether analyzer should do FP-Growth to find
	 * frequent declarations
	 * @param value
	 */
	public void setFPGrowth(boolean value) {
		doFPGrowth = value;
	}
	
	/**
	 * Identifies whether we have to compare apriori results with fpgrowth results
	 * @param value
	 */
	public void compareAproiriAndFPGrowth(boolean value) {
		compareAprioriAndFPGrowth = value;
	}
	
	private void parseStyleSheets(List<File> files ) {
				
		for (File file : files) {
			String filePath = file.getAbsolutePath();
			LOGGER.info("Now parsing " + filePath);
			FluteCSSParser parser = new FluteCSSParser();
			try {
				StyleSheet styleSheet = parser.parseExternalCSS(filePath);
				model.addStyleSheet(styleSheet);
			} catch (Exception ex) {
				LOGGER.warn("Couldn't parse " + file + ". Skipping to the next file.");
			}
		}
		
	}

	/**
	 * Invoking this method will result to the creation of 
	 * one folder for each CSS file in the specified analysis folder. 
	 * The name of this folder would end with ".analyse".
	 * The output results for each kind of analysis is written in the 
	 * separate files inside this folder.
	 * @throws IOException
	 */
	public void analyse(final int MIN_SUPPORT) throws IOException {
		
		IOHelper.deleteFile(folderPath + "/analytics.txt");
		
		String headerLine = "file_name|" +
				"size|" +
				"sloc|" +
				"num_selectors|" +
				"num_base_sel|" +
				"num_grouped_sel|" +
				"num_decs|" +
				"IOnly|" +
				"IIOnly|" +
				"IIIOnly|" +
				"I_II|" +
				"I_III|" +
				"II_III|" +
				"I_II_III|" +
				"number_of_duplicated_declarations|" +
				"selectors_with_duplicated_declaration|" +
				"longest_dup|" +
				"max_sup_longest_dup|" +
				"clone_sets|" +
				"refactoring_opportunities|" +
				"applied_refactorings_count|" +
				"number_of_positive_refactorings|" +
				"size_after|" +
				"number_of_order_dependencies|" +
				"refactoring_opportunities_excluded_subsumed|" + 
				"positive_excluded_subsumed" + System.lineSeparator();
		
		IOHelper.writeStringToFile(headerLine, folderPath + "/analytics.txt", false);

		
	
		// Do the analysis for each CSS file
		for (StyleSheet styleSheet : model.getStyleSheets()) {
						
			String filePath = styleSheet.getFilePath();
			String analyticsFolderPath = filePath + ".analyse";
			
//			CSSValueOverridingDependencyList originalDependencies = styleSheet.getValueOverridingDependencies(model.getDocument());
//			IOHelper.writeStringToFile(originalDependencies.toString() + "\n\n\n\n" + originalDependencies.size(), folderName + "/orderDependencies.txt");
			
			
//			if (originalDependencies != null) // correct always
//				continue;
					
			LOGGER.info("Finding different types of duplication in " + filePath);
			
			DuplicationDetector duplicationDetector = new DuplicationDetector(styleSheet);
			duplicationDetector.findDuplications();

			
			IOHelper.createFolder(analyticsFolderPath, true);
			
			IOHelper.writeStringToFile(styleSheet.toString(), analyticsFolderPath + "/formatted.css");

			
			DuplicationIncstanceList typeIDuplications = duplicationDetector.getTypeIDuplications();
			IOHelper.writeLinesToFile(typeIDuplications, analyticsFolderPath + "/typeI.txt");
		
			DuplicationIncstanceList typeIIDuplications = duplicationDetector.getTypeIIDuplications();
			IOHelper.writeLinesToFile(typeIIDuplications, analyticsFolderPath + "/typeII.txt");
			
			DuplicationIncstanceList typeIIIDuplications = duplicationDetector.getTypeIIIDuplications();
			IOHelper.writeLinesToFile(typeIIIDuplications, analyticsFolderPath + "/typeIII.txt");
			
//			if (typeToItemsMapper != null) continue;
			
			//			DuplicationIncstanceList typeIVADuplications = duplicationFinder.getTypeIVADuplications();
//			IOHelper.writeLinesToFile(typeIVADuplications, folderName + "/typeIVA.txt");
			
			//if (!dontUseDOM) {
				//duplicationFinder.findTypeFourBDuplication(model.getDocument());
				//DuplicationIncstanceList typeIVBDuplications = duplicationFinder.getTypeIVBDuplications();
				//IOHelper.writeLinesToFile(typeIVBDuplications, folderName + "/typeIVB.txt");
			//}
						
		
			List<ItemSetList> aprioriResults = null, fpgrowthResults = null;
			
			if (doApriori) {
			
				LOGGER.info("Applying apriori algorithm with minimum support count of " + MIN_SUPPORT + " on " + filePath);

				long start = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				aprioriResults = duplicationDetector.apriori(MIN_SUPPORT);
				long end = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				long time = (end - start) / 1000000L;
				IOHelper.writeLinesToFile(aprioriResults, analyticsFolderPath + "/apriori.txt");
				
				LOGGER.info("Done Apriori in " + time);
			
			}
			
			if (doFPGrowth) {

				fpgrowthResults = duplicationDetector.fpGrowth(MIN_SUPPORT, false);
				IOHelper.writeLinesToFile(fpgrowthResults, analyticsFolderPath + "/fpgrowth-subsumed.txt");
				
//				int numberOfPositiveSubsumed = 0, numberOrRefactoringsSubsumed = 0;
//				for (ItemSetList isl : fpgrowthResults) {
//					for (ItemSet is : isl) {
//						numberOrRefactoringsSubsumed++;
//						if (is.getRefactoringImpact() > 0) {
//							numberOfPositiveSubsumed++;
//						}
//					}
//				}
//				String str = "Subsumed\tPositive\r\n" + String.valueOf(numberOrRefactoringsSubsumed) + "\t" + String.valueOf(numberOfPositiveSubsumed);
//				IOHelper.writeStringToFile(str, analyticsFolderPath + "/refactoring-opportunities-positive-subsumed.txt");
//				
				long start, end, time;
				LOGGER.info("Applying grouping refactoring opportunities");
				start = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				BatchGroupingRefactoringResult refactoringResults;
				if (!dontUseDOM) {
					refactoringResults = refactorGroupingOpportunities(MIN_SUPPORT, styleSheet, analyticsFolderPath, fpgrowthResults, model.getDocument());
				} else {
					refactoringResults = refactorGroupingOpportunities(MIN_SUPPORT, styleSheet, analyticsFolderPath, fpgrowthResults);
				}
				end = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				time = (end - start) / 1000000L;
				LOGGER.info("Applied " + refactoringResults.getNumberOfAppliedRefactorings() + " grouping refactoring(s) in " + time + " ms");
				LOGGER.info("Collecting more info for the further analysis...");
				
				List<ItemSetList> fpgrowthResultsSubsumed = duplicationDetector.fpGrowth(MIN_SUPPORT, true);
				
				String analytics = getAnalytics(styleSheet, refactoringResults, duplicationDetector, fpgrowthResults, fpgrowthResultsSubsumed);
				
				IOHelper.writeStringToFile(analytics + System.lineSeparator(), folderPath + "/analytics.txt" , true);
								
			}
			
			if (compareAprioriAndFPGrowth)
				compareAprioriAndFPGrowth(aprioriResults, fpgrowthResults);
			
			LOGGER.info("Done analysis for " + filePath);
		}
		
		LOGGER.info("Done.");
					
	}
	
	private static class BatchGroupingRefactoringResult {
		private final StyleSheet styleSheet;
		private final int numberOfAppliedRefactorings;
		private final int numberOfPositiveRefactorins;
		public BatchGroupingRefactoringResult(StyleSheet styleSheet, int appliedRefactorings, int positiveRefactorins) {
			this.styleSheet = styleSheet;
			this.numberOfAppliedRefactorings = appliedRefactorings;
			this.numberOfPositiveRefactorins = positiveRefactorins;
		}
		public StyleSheet getStyleSheet() {
			return styleSheet;
		}
		public int getNumberOfAppliedRefactorings() {
			return numberOfAppliedRefactorings;
		}
		public int getNumberOfPositiveRefactorins() {
			return numberOfPositiveRefactorins;
		}
	}
	
	private BatchGroupingRefactoringResult refactorGroupingOpportunities(final int MIN_SUPPORT, StyleSheet styleSheet, String folderName, List<ItemSetList> fpgrowthResults) {
		
		return refactorGroupingOpportunities(MIN_SUPPORT, styleSheet, folderName, fpgrowthResults, null);
		
	}
	
	private BatchGroupingRefactoringResult refactorGroupingOpportunities(int MIN_SUPPORT, StyleSheet styleSheet, String folderName,	List<ItemSetList> fpgrowthResults, Document dom) {

		StyleSheet originalStyleSheet = styleSheet;
				
		CSSValueOverridingDependencyList originalDependencies = originalStyleSheet.getValueOverridingDependencies(dom);
		
		IOHelper.writeStringToFile(originalDependencies.toString() + "\n\n\n\n" + originalDependencies.size(), folderName + "/orderDependencies.txt");
		
		TreeSet<ItemSet> itemSetsTreeSet = new TreeSet<>(new Comparator<ItemSet>() {

			@Override
			public int compare(ItemSet o1, ItemSet o2) {
				if (o1 == o2)
					return 0;
				
				int i = o1.getGroupingRefactoringImpact();
				int j = o2.getGroupingRefactoringImpact();

				if (i != j)
					return -Integer.compare(i, j);
				return 1;
			}

		});
		List<ItemSet> itemSetsSortedList = null;
		
		List<ItemSet> listOfInfeasibleRefactorings = new ArrayList<>();
		
		boolean firstRun = true;
		int numberOfPositiveRefactorings = 0; 
		int refactoringRound = 0;
		boolean refactoringWasPossible = true;
		
		while (true) {

			if (refactoringWasPossible) {
				refactoringRound++;
				itemSetsTreeSet.clear();
				for (ItemSetList isl : fpgrowthResults) {
					for (ItemSet is : isl) {
						if (is.getGroupingRefactoringImpact() > 0) {
							/*
							 *  If a shorthand declaration is virtual in all its selectors in an item of the itemset,
							 *  the itemset cannot be used for the refactoring.
							 *  It should be real in at least one of the selectors in the current itemset's support.
							 */
							boolean itemSetIsDoable = true;
							for (Item currentItem : is) {
								boolean declarationIsNotVirtualInAllSelectors = false;
								for (Declaration declaration : currentItem) {
									if (declaration instanceof ShorthandDeclaration) {
										ShorthandDeclaration shorthand = (ShorthandDeclaration)declaration;
										if (is.getSupport().contains(shorthand.getSelector()) && !shorthand.isVirtual()) {
											declarationIsNotVirtualInAllSelectors = true;
											break;
										}
									}
								}
								if (!declarationIsNotVirtualInAllSelectors) {
									itemSetIsDoable = false;
									break;
								}
							}
							
							if (itemSetIsDoable)
								itemSetsTreeSet.add(is);
						}
					}
				}


				itemSetsSortedList = new ArrayList<>(itemSetsTreeSet);
				
				if (firstRun) {
					firstRun = false;
					numberOfPositiveRefactorings = itemSetsSortedList.size();
				}
			}
			
			// Find a feasible refactoring opportunity with max impact 
			ItemSet itemSetWithMaxImpact = null;
			do {
				if (itemSetsSortedList.size() == 0) {
					itemSetWithMaxImpact = null;
					break;
				}
				itemSetWithMaxImpact = itemSetsSortedList.get(0);
				itemSetsSortedList.remove(0);
			} while (containsItemSet(listOfInfeasibleRefactorings, itemSetWithMaxImpact));
			
			if (itemSetWithMaxImpact == null)
				// No more refactoring is possible to reduce the size
				break;
			

			LOGGER.info("Applying round " + refactoringRound + " of refactoring on " + originalStyleSheet.getFilePath() + " to reduce " + itemSetWithMaxImpact.getGroupingRefactoringImpact() + " characters.");
			StyleSheet newStyleSheet = RefactorDuplications.groupingRefactoring(styleSheet, itemSetWithMaxImpact);
			IOHelper.writeStringToFile(newStyleSheet.toString(), folderName + "/refactored" + refactoringRound + ".css");

			FluteCSSParser parser = new FluteCSSParser();
			try {
				newStyleSheet = parser.parseExternalCSS(folderName + "/refactored" + refactoringRound + ".css");
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			CSSValueOverridingDependencyList refactoredDependencies = newStyleSheet.getValueOverridingDependencies(dom);

			CSSDependencyDifferenceList differences = originalDependencies.getDifferencesWith(refactoredDependencies);

			if (differences.size() > 0 && !differences.allMissing()) {
				
				IOHelper.writeStringToFile(differences.toString(), folderName + "/dependency-differences" + refactoringRound + ".txt");
				
				LOGGER.info("Reordering needed at round " + refactoringRound);

				RefactorToSatisfyDependencies r = new RefactorToSatisfyDependencies();
				StyleSheet refactoredAndOrdered = r.refactorToSatisfyOverridingDependencies(newStyleSheet, originalDependencies); 

				if (refactoredAndOrdered == null) { // It was not possible to satisfy constraints 

					refactoringWasPossible = false;
					listOfInfeasibleRefactorings.add(itemSetWithMaxImpact);
					LOGGER.info("Reordering was not feasible, applying the next refactoring opportunity at round " + refactoringRound);
					
				} else {

					refactoringWasPossible = true;

					CSSDependencyDetector dependencyDetector2 = new CSSDependencyDetector(refactoredAndOrdered, dom); 

					IOHelper.writeStringToFile(refactoredAndOrdered.toString(), folderName + "/refactored-reordered" + refactoringRound + ".css");

					CSSValueOverridingDependencyList dependenciesReordered = dependencyDetector2.findOverridingDependancies();
					differences = originalDependencies.getDifferencesWith(dependenciesReordered);
					
					if (differences.size() > 0) {
						LOGGER.warn("Differences in dependencies after reordering " + refactoringRound + "\n");  
						LOGGER.warn(differences.toString() + "\n");	
						IOHelper.writeStringToFile(differences.toString(), folderName + "/dependency-differences-after-reordering" + refactoringRound + ".txt");
					}
					
					try {
						newStyleSheet = parser.parseExternalCSS(folderName + "/refactored-reordered" + refactoringRound + ".css");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			} else { // If there were not difference between dependencies after grouping
				refactoringWasPossible = true;
			}
			
			if (refactoringWasPossible) {
				styleSheet = newStyleSheet;
				DuplicationDetector duplicationFinderRefacored = new DuplicationDetector(styleSheet);
				duplicationFinderRefacored.findDuplications();
				fpgrowthResults = duplicationFinderRefacored.fpGrowth(MIN_SUPPORT, false);
				duplicationFinderRefacored = null;
			}
		}
		
		
		int numberOfAppliedRefactorings = refactoringRound - 1;
		return new BatchGroupingRefactoringResult(styleSheet, numberOfAppliedRefactorings , numberOfPositiveRefactorings);
		
	}

	private boolean containsItemSet(List<ItemSet> listOfItemSetsToCheck, ItemSet itemSet) {
		boolean itemSetFound = true;
		for (ItemSet is : listOfItemSetsToCheck) {
			if (is.size() == itemSet.size() && is.getSupport().size() == itemSet.getSupport().size()) {
				for (Item i : is) {
					boolean itemFound = false;
					for (Item j : itemSet) {
						if (i.getFirstDeclaration().declarationEquals(j.getFirstDeclaration())) {
							itemFound = true;
							break;
						}
					}
					if (!itemFound) {
						itemSetFound = false;
						break;
					}
				}
				if (itemSetFound)
					return true;
			}
		}
		return false;
	}
	
	private String getAnalytics(StyleSheet styleSheet, BatchGroupingRefactoringResult refactoringResults, DuplicationDetector finder, List<ItemSetList> dupResults, List<ItemSetList> dupResultsSubsumed) {
		
		File originalCSSFile = new File(styleSheet.getFilePath() + ".analyse/formatted.css");
		File refactoredCSSFile = new File(refactoringResults.getStyleSheet().getFilePath());
		
		String fileName = (new File(styleSheet.getFilePath())).getName();
		
		float sizeBeforeRefactoring = (originalCSSFile.length() / 1024F);// + (originalCSSFile.length() % 1024 != 0 ? 1 : 0);
		float sizeAfterRefactoring = (refactoredCSSFile.length() / 1024F);// + (refactoredCSSFile.length() % 1024 != 0 ? 1 : 0);
		int sloc = 0;
		String[] lines = styleSheet.toString().split("\r\n|\r|\n");
		for (String l : lines)
			if (!"".equals(l.trim()))
				sloc++;
		
		int numberOfCloneSets = 0;
		if (dupResults.size() > 0) 
			numberOfCloneSets = dupResults.get(0).size();
		
		int numberOfRefactoringOpportunities = 0;
		for (ItemSetList isl : dupResults)
			numberOfRefactoringOpportunities += isl.size();
		
		int numberOfRefactoringOpportunitiesExcludedSubsumed = 0;
		int numberOfRefactoringOpportunitiesExcludedSubsumedPositive = 0;
		for (ItemSetList isl : dupResultsSubsumed) {
			numberOfRefactoringOpportunitiesExcludedSubsumed += isl.size();
			for (ItemSet is : isl)
				if (is.getGroupingRefactoringImpact() > 0)
					numberOfRefactoringOpportunitiesExcludedSubsumedPositive++;
		}
		
		IOHelper.writeStringToFile("\n\nNumber of all possible refactoring opportunities " + numberOfRefactoringOpportunities, styleSheet.getFilePath() + ".analyse/fpgrowth.txt", true);
			
		int numberOfSelectors = styleSheet.getNumberOfSelectors();
		int numberOfAtomicSelectors = styleSheet.getAllBaseSelectors().size();
		int numberOfDeclarations = styleSheet.getAllDeclarations().size();
		int numberOfGroupedSelectors = 0;
		for (Selector selector : styleSheet.getAllSelectors())
			if (selector instanceof GroupingSelector)
				numberOfGroupedSelectors++;
		
//		String cloneSetTypesCount = "Number of clone sets including type I to III instnaces: ";
//		Map<Integer, List<Item>> typeToItemsMapper = finder.getItemsIncludingTypenstances();
//		cloneSetTypesCount += typeToItemsMapper.get(1).size() + "\t" + typeToItemsMapper.get(2).size() + "\t" + typeToItemsMapper.get(3).size(); 
//		IOHelper.writeStringToFile(cloneSetTypesCount, styleSheet.getFilePath() + ".analyse/clone types.txt");
		
		int conductedRefactorings = refactoringResults.getNumberOfAppliedRefactorings();
		int numberOfPositiveRefactorings = refactoringResults.getNumberOfPositiveRefactorins();
		
		String cloneSetTypesCount = "Number of clone sets including type I to III instnaces: 1 2 3 12 13 23 123\r\n";
		Map<Integer, List<Item>> typeToItemsMapper = finder.getItemsIncludingTypenstances();
		cloneSetTypesCount += typeToItemsMapper.get(1).size() + "\t" + typeToItemsMapper.get(2).size() + "\t" + typeToItemsMapper.get(3).size() +
				"\t" + typeToItemsMapper.get(12).size() + "\t" + typeToItemsMapper.get(13).size() +
				"\t" + typeToItemsMapper.get(23).size() + "\t" + typeToItemsMapper.get(123).size(); 
		IOHelper.writeStringToFile(cloneSetTypesCount, styleSheet.getFilePath() + ".analyse/clone types.txt");
			
		
		int numberOfTypeIDuplications = typeToItemsMapper.get(1).size(); //finder.getTypeIDuplications().getSize();
		int numberOfTypeIIDuplications =  typeToItemsMapper.get(2).size();
		int numberOfTypeIIIDuplications = typeToItemsMapper.get(3).size();
		int numberOfTypeI_IIDuplications = typeToItemsMapper.get(12).size();
		int numberOfTypeI_IIIDuplications = typeToItemsMapper.get(13).size();
		int numberOfTypeII_IIIDuplications = typeToItemsMapper.get(23).size();
		int numberOfTypeI_II_IIIDuplications = typeToItemsMapper.get(123).size();
		
		//int numberOfTypeIVADuplications = finder.getTypeIVADuplications().getSize();
		//int numberOfTypeIVBDuplications = 0;
		if (finder.getTypeIVBDuplications() != null) 
			finder.getTypeIVBDuplications().getSize();

		Set<Selector> selectorsInDuplicatedDeclarations = new HashSet<>();
		for (DuplicationInstance d : finder.getTypeIDuplications())
			selectorsInDuplicatedDeclarations.addAll(d.getSelectors());
		
		for (DuplicationInstance d : finder.getTypeIIDuplications())
			selectorsInDuplicatedDeclarations.addAll(d.getSelectors());
		
		for (DuplicationInstance d : finder.getTypeIIIDuplications())
			selectorsInDuplicatedDeclarations.addAll(d.getSelectors());
		
		int numberOfSelectorsWithDuplications = selectorsInDuplicatedDeclarations.size();
			
		Set<Declaration> duplicatedDeclarations = new HashSet<>();
		for (ItemSetList isl : dupResults)
			for (ItemSet is : isl)
				for (Item i : is)
					for (Declaration d : i)
						duplicatedDeclarations.add(d);
		
		int numberOfDuplicatedDeclarations = duplicatedDeclarations.size(); 
				
		int longestDupLength = dupResults.size();
		int maxSupForLongestDup = 0; 
		try {
			maxSupForLongestDup = dupResults.get(dupResults.size() - 1).getMaximumSupport();
		} catch (Exception ex) {
			// Swallow
		}
				
		StringBuilder line = new StringBuilder();
		line.append(fileName + "|");
		line.append(sizeBeforeRefactoring + "|");
		line.append(sloc + "|");
		line.append(numberOfSelectors + "|");
		line.append(numberOfAtomicSelectors + "|");
		line.append(numberOfGroupedSelectors + "|");
		line.append(numberOfDeclarations + "|");
		line.append(numberOfTypeIDuplications + "|");
		line.append(numberOfTypeIIDuplications + "|");
		line.append(numberOfTypeIIIDuplications + "|");
		line.append(numberOfTypeI_IIDuplications + "|");
		line.append(numberOfTypeI_IIIDuplications + "|");
		line.append(numberOfTypeII_IIIDuplications + "|");
		line.append(numberOfTypeI_II_IIIDuplications + "|");
		//line.append(numberOfTypeIVADuplications + "|");
		//line.append(numberOfTypeIVBDuplications + "|");
		line.append(numberOfDuplicatedDeclarations + "|");
		line.append(numberOfSelectorsWithDuplications + "|");
		line.append(longestDupLength + "|");
		line.append(maxSupForLongestDup + "|");
		line.append(numberOfCloneSets + "|");
		line.append(numberOfRefactoringOpportunities + "|");
		line.append(conductedRefactorings + "|");
		line.append(numberOfPositiveRefactorings + "|");
		line.append(sizeAfterRefactoring + "|");
		line.append(styleSheet.getLastComputetOrderDependencies().size() + "|");
		line.append(numberOfRefactoringOpportunitiesExcludedSubsumed + "|");
		line.append(numberOfRefactoringOpportunitiesExcludedSubsumedPositive);
		return line.toString();
	}

	private void compareAprioriAndFPGrowth(List<ItemSetList> aprioriResults,
			List<ItemSetList> fpgrowthResults) {
		// Compare APRIORI and FP-GROWTH
		if (doApriori && doFPGrowth && aprioriResults.size() == fpgrowthResults.size())
		{
			StringBuilder outText = new StringBuilder();
			for (int i = 0; i < aprioriResults.size(); i++) {
					outText.append("\nItems below are in APRIORI but not in FPGROWTH (" + i + ")\n");
					for (ItemSet is : aprioriResults.get(i)) {
						boolean found = false;
						for (ItemSet fpis : fpgrowthResults.get(i))
							if (fpis.equals(is)) {
								found = true;
								break;
							}
						if (!found) {
							for (Item k : is)
								outText.append("(" + k.getFirstDeclaration() + "), ");
							outText.append("\n");
						}
					}
					outText.append("\nItems below are in FPGROWTH but not in APRIORI (" + i + ")\n\n");
					for (ItemSet is : fpgrowthResults.get(i)) {
						boolean found = false;
						for (ItemSet apis : aprioriResults.get(i))
							if (is.equals(apis)) {
								found = true;
								break;
							}
						if (!found) {
							for (Item k : is)
								outText.append("(" + k.getFirstDeclaration() + "), ");
							outText.append("\n");
						}
					}
			}
			LOGGER.warn(outText.toString());
		}
	}

	
}
