package ca.concordia.cssanalyser.analyser;


import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import ca.concordia.cssanalyser.analyser.duplication.DuplicationDetector;
import ca.concordia.cssanalyser.analyser.duplication.DuplicationIncstanceList;
import ca.concordia.cssanalyser.analyser.duplication.items.Item;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSetList;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.dom.DOMHelper;
import ca.concordia.cssanalyser.dom.Model;
import ca.concordia.cssanalyser.io.IOHelper;
import ca.concordia.cssanalyser.parser.CSSParser;
import ca.concordia.cssanalyser.refactoring.RefactorDuplications;
import ca.concordia.cssanalyser.refactoring.RefactorToSatisfyDependencies;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSDependencyDetector;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSDependencyDifferenceList;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;



/**
 * Main CSS analysis tool
 * @author Davood Mazinanian
 * 
 */
public class CSSAnalyser {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CSSAnalyser.class);
	private final String folderPath;
	private final Model model;
	private boolean doApriori = false;
	private boolean doFPGrowth = true;
	private boolean dontUseDOM = false;
	private boolean compareAprioriAndFPGrowth = false;
	
	/**
	 * Through this constructor, one should pass the
	 * folder containing all CSS files. Program will search
	 * for all files with extension ".css" and does the 
	 * analysis for these files 
	 * @param cssContainingFolder
	 */
	public CSSAnalyser(String domStateHTMLPath, String cssContainingFolder) {
		
		folderPath = cssContainingFolder;
		Document document = DOMHelper.getDocument(domStateHTMLPath);
		model = new Model(document);
		parseStyleSheets();
		
	}
	
	public CSSAnalyser(String cssContainingFolder) {
		dontUseDOM = true;
		model = new Model(null);
		folderPath = cssContainingFolder;
		parseStyleSheets();
		
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
	
	private void parseStyleSheets() {
		
		// Lets search for all CSS files
		List<File> files = IOHelper.searchForFiles(folderPath, "css");

		if (files.size() == 0) {
			LOGGER.warn("No CSS file found in " + folderPath);
			return;
		}
		
		for (File file : files) {
			String filePath = file.getAbsolutePath();
			LOGGER.info("Now parsing " + filePath);
			CSSParser parser = new CSSParser();
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
		
//		String headerLine = "file_name|" +
//				"size|" +
//				"sloc|" +
//				"num_selectors|" +
//				"num_atomic_sel|" +
//				"num_grouped_sel|" +
//				"num_decs|" +
//				"typeI|" +
//				"typeII|" +
//				"typeIII|" +
//				"typeIV_A|" +
//				"typeIV_B|" +
//				"number_of_duplicated_declarations|" +
//				"selectors_with_duplicated_declaration|" +
//				"longest_dup|" +
//				"max_sup_longest_dup";
//		List<String> analytics = new ArrayList<>();
		//analytics.add(headerLine);
	
		// Do the analysis for each CSS file
		for (StyleSheet styleSheet : model.getStyleSheets()) {
						
			String filePath = styleSheet.getFilePath();
			String folderName = filePath + ".analyse";
					
			LOGGER.warn("Finding different types of duplication in " + filePath);
			
			DuplicationDetector duplicationFinder = new DuplicationDetector(styleSheet);
			duplicationFinder.findDuplications();

			
			IOHelper.createFolder(folderName, true);
			
			IOHelper.writeStringToFile(styleSheet.toString(), folderName + "/formatted.css");

			
			DuplicationIncstanceList typeIDuplications = duplicationFinder.getTypeIDuplications();
			IOHelper.writeLinesToFile(typeIDuplications, folderName + "/typeI.txt");
		
			DuplicationIncstanceList typeIIDuplications = duplicationFinder.getTypeIIDuplications();
			IOHelper.writeLinesToFile(typeIIDuplications, folderName + "/typeII.txt");
			
			DuplicationIncstanceList typeIIIDuplications = duplicationFinder.getTypeIIIDuplications();
			IOHelper.writeLinesToFile(typeIIIDuplications, folderName + "/typeIII.txt");
			
//			DuplicationIncstanceList typeIVADuplications = duplicationFinder.getTypeIVADuplications();
//			IOHelper.writeLinesToFile(typeIVADuplications, folderName + "/typeIVA.txt");
			
			//if (!dontUseDOM) {
				//duplicationFinder.findTypeFourBDuplication(model.getDocument());
				//DuplicationIncstanceList typeIVBDuplications = duplicationFinder.getTypeIVBDuplications();
				//IOHelper.writeLinesToFile(typeIVBDuplications, folderName + "/typeIVB.txt");
			//}
						
		
			List<ItemSetList> aprioriResults = null, fpgrowthResults = null;
			
			if (doApriori) {
			
				LOGGER.warn("Applying apriori algorithm with minimum support count of " + MIN_SUPPORT + " on " + filePath);

				long start = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				aprioriResults = duplicationFinder.apriori(MIN_SUPPORT);
				long end = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				long time = (end - start) / 1000000L;
				IOHelper.writeLinesToFile(aprioriResults, folderName + "/apriori.txt");
				
				LOGGER.warn("Done Apriori in " + time);
			
			}
			
			if (doFPGrowth) {

				LOGGER.warn("Applying fpgrowth algorithm with minimum support count of " + MIN_SUPPORT + " on " + filePath);
				
				long start = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				fpgrowthResults = duplicationFinder.fpGrowth(MIN_SUPPORT);
				long end = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				long time = (end - start) / 1000000L;
				IOHelper.writeLinesToFile(fpgrowthResults, folderName + "/fpgrowth.txt");
				
				LOGGER.warn("Done FP-Growth in " + time);
								
				if (!dontUseDOM) {
					refactorGroupingOpportunities(MIN_SUPPORT, styleSheet, folderName, fpgrowthResults, model.getDocument());
				} else {
					refactorGroupingOpportunities(MIN_SUPPORT, styleSheet, folderName, fpgrowthResults);
				}
				
			}
			
			if (compareAprioriAndFPGrowth)
				compareAprioriAndFPGrowth(aprioriResults, fpgrowthResults);
			
			//getAnalytics(styleSheet, duplicationFinder, fpgrowthResults, analytics); 
		}
		
		//IOHelper.writeLinesToFile(analytics, "E:/Davood/joomla-cms-css-typeIV/analytics.txt", true);
					
	}
	
	private void refactorGroupingOpportunities(int MIN_SUPPORT, StyleSheet styleSheet, String folderName, 
			List<ItemSetList> fpgrowthResults, Document dom) {

		StyleSheet originalStyleSheet = styleSheet;
		
		CSSDependencyDetector dependencyDetector, refactoredDependencyDetector;
		
		if (!dontUseDOM) {                                                                                               
			dependencyDetector = new CSSDependencyDetector(originalStyleSheet, dom);
		} else {                                                                                                         
			dependencyDetector = new CSSDependencyDetector(originalStyleSheet);                                     
		}
		
		CSSValueOverridingDependencyList originalDependencies = dependencyDetector.findOverridingDependancies();
		
		TreeSet<ItemSet> itemSetsTreeSet = new TreeSet<>(new Comparator<ItemSet>() {

			@Override
			public int compare(ItemSet o1, ItemSet o2) {
				if (o1 == o2)
					return 0;
				
				int i = o1.getRefactoringImpact();
				int j = o2.getRefactoringImpact();

				if (i != j)
					return -Integer.compare(i, j);
				return 1;
			}

		});
		List<ItemSet> itemSetsSortedList = null;
		
		List<ItemSet> listOfInfeasibleRefactorings = new ArrayList<>();
		
		int i = 0;
		boolean refactoringWasPossible = true;
		while (true) {

			if (refactoringWasPossible) {
				i++;
				itemSetsTreeSet.clear();
				for (ItemSetList isl : fpgrowthResults) {
					for (ItemSet is : isl) {
						if (is.getRefactoringImpact() > 0) {
							itemSetsTreeSet.add(is);
						}
					}
				}


				itemSetsSortedList = new ArrayList<>(itemSetsTreeSet);
			}
			
			if (i == 11)
				System.out.print("");
			
			if (itemSetsSortedList.size() == 0) {
				// No more refactoring is possible to reduce the size
				break;
			}
			
			// Find a feasible refactoring opportunity with max impact 
			ItemSet itemSetWithMaxImpact = null;
			do {
				itemSetWithMaxImpact = itemSetsSortedList.get(0);
				itemSetsSortedList.remove(0);
			} while (containsItemSet(listOfInfeasibleRefactorings, itemSetWithMaxImpact));

			LOGGER.warn("Applying round " + i + " of refactoring on " + originalStyleSheet.getFilePath() + " to reduce " + itemSetWithMaxImpact.getRefactoringImpact() + " characters.");
			StyleSheet newStyleSheet = RefactorDuplications.groupingRefactoring(styleSheet, itemSetWithMaxImpact);
			IOHelper.writeStringToFile(newStyleSheet.toString(), folderName + "/refactored" + i + ".css");

			CSSParser parser = new CSSParser();
			try {
				newStyleSheet = parser.parseExternalCSS(folderName + "/refactored" + i + ".css");
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			if (!dontUseDOM) {
				refactoredDependencyDetector = new CSSDependencyDetector(newStyleSheet, dom);          
			} else {
				refactoredDependencyDetector = new CSSDependencyDetector(newStyleSheet); 
			}

			CSSValueOverridingDependencyList refactoredDependencies = refactoredDependencyDetector.findOverridingDependancies();

			CSSDependencyDifferenceList differences = originalDependencies.getDifferencesWith(refactoredDependencies);

			if (differences.size() > 0) {
				
				IOHelper.writeStringToFile(differences.toString(), folderName + "/dependency-differences" + i + ".txt");
				
				LOGGER.warn("Reordering needed at round " + i);

				RefactorToSatisfyDependencies r = new RefactorToSatisfyDependencies();
				StyleSheet refactoredAndOrdered = r.refactorToSatisfyOverridingDependencies(newStyleSheet, originalDependencies); 

				if (refactoredAndOrdered == null) { // It was not possible to satisfy constraints 

					refactoringWasPossible = false;
					listOfInfeasibleRefactorings.add(itemSetWithMaxImpact);
					LOGGER.warn("Reordering was not feasible, applying the next refactoring opportunity at round " + i);
					
				} else {

					refactoringWasPossible = true;

					CSSDependencyDetector dependencyDetector2 = new CSSDependencyDetector(refactoredAndOrdered, dom); 

					IOHelper.writeStringToFile(refactoredAndOrdered.toString(), folderName + "/refactored-reordered" + i + ".css");

					CSSValueOverridingDependencyList dependenciesReordered = dependencyDetector2.findOverridingDependancies();
					differences = originalDependencies.getDifferencesWith(dependenciesReordered);
					
					if (differences.size() > 0) {
						LOGGER.warn("Differences in dependencies after reordering " + i + "\n");  
						LOGGER.warn(differences.toString() + "\n");	
						IOHelper.writeStringToFile(differences.toString(), folderName + "/dependency-differences-after-reordering" + i + ".txt");
					}
					
					try {
						newStyleSheet = parser.parseExternalCSS(folderName + "/refactored-reordered" + i + ".css");
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
				fpgrowthResults = duplicationFinderRefacored.fpGrowth(MIN_SUPPORT);
				duplicationFinderRefacored = null;
			}
		}
		
		// styleSheet object is the final refactored version
		//model.compareAppliedStyles(originalStyleSheet, styleSheet);
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

	private void refactorGroupingOpportunities(final int MIN_SUPPORT,
			StyleSheet styleSheet, String folderName,
			List<ItemSetList> fpgrowthResults) {
		
		refactorGroupingOpportunities(MIN_SUPPORT, styleSheet, folderName, fpgrowthResults, null);
		
	}
	
//	private void getAnalytics(StyleSheet styleSheet, DuplicationDetector finder, List<ItemSetList> dupResults, List<String> analytics) {
//		
//		File file = new File(styleSheet.getFilePath());
//		
//		String fileName = file.getName();
//		
//		long size = (file.length() / 1024) + (file.length() % 1024 != 0 ? 1 : 0);
//		int sloc = 0;
//		String[] lines = styleSheet.toString().split("\r\n|\r|\n");
//		for (String l : lines)
//			if (!"".equals(l.trim()))
//				sloc++;
//		
//		int numberOfSelectors = styleSheet.getAllSelectors().size();
//		int numberOfAtomicSelectors = styleSheet.getAllBaseSelectors().size();
//		int numberOfDeclarations = styleSheet.getAllDeclarations().size();
//		int numberOfGroupedSelectors = 0;
//		for (Selector selector : styleSheet.getAllSelectors())
//			if (selector instanceof GroupingSelector)
//				numberOfGroupedSelectors++;
//		
//		int numberOfTypeIDuplications = finder.getTypeIDuplications().getSize();
//		int numberOfTypeIIDuplications = finder.getTypeIIDuplications().getSize();
//		int numberOfTypeIIIDuplications = finder.getTypeIIIDuplications().getSize();
//		int numberOfTypeIVADuplications = finder.getTypeIVADuplications().getSize();
//		int numberOfTypeIVBDuplications = 0;
//		if (finder.getTypeIVBDuplications() != null) 
//			finder.getTypeIVBDuplications().getSize();
//
//		Set<Selector> selectorsInDuplicatedDeclarations = new HashSet<>();
//		for (DuplicationInstance d : finder.getTypeIDuplications())
//			selectorsInDuplicatedDeclarations.addAll(d.getSelectors());
//		
//		for (DuplicationInstance d : finder.getTypeIIDuplications())
//			selectorsInDuplicatedDeclarations.addAll(d.getSelectors());
//		
//		for (DuplicationInstance d : finder.getTypeIIIDuplications())
//			selectorsInDuplicatedDeclarations.addAll(d.getSelectors());
//		
//		int numberOfSelectorsWithDuplications = selectorsInDuplicatedDeclarations.size();
//			
//		Set<Declaration> duplicatedDeclarations = new HashSet<>();
//		for (ItemSetList isl : dupResults)
//			for (ItemSet is : isl)
//				for (Item i : is)
//					for (Declaration d : i)
//						duplicatedDeclarations.add(d);
//		
//		int numberOfDuplicatedDeclarations = duplicatedDeclarations.size(); 
//				
//		int longestDupLength = dupResults.size();
//		int maxSupForLongestDup = 0; 
//		try {
//			maxSupForLongestDup = dupResults.get(dupResults.size() - 1).getMaximumSupport();
//		} catch (Exception ex) {
//			// Swallow
//		}
//				
//		StringBuilder line = new StringBuilder();
//		line.append(fileName + "|");
//		line.append(size + "|");
//		line.append(sloc + "|");
//		line.append(numberOfSelectors + "|");
//		line.append(numberOfAtomicSelectors + "|");
//		line.append(numberOfGroupedSelectors + "|");
//		line.append(numberOfDeclarations + "|");
//		line.append(numberOfTypeIDuplications + "|");
//		line.append(numberOfTypeIIDuplications + "|");
//		line.append(numberOfTypeIIIDuplications + "|");
//		line.append(numberOfTypeIVADuplications + "|");
//		line.append(numberOfTypeIVBDuplications + "|");
//		line.append(numberOfDuplicatedDeclarations + "|");
//		line.append(numberOfSelectorsWithDuplications + "|");
//		line.append(longestDupLength + "|");
//		line.append(maxSupForLongestDup );
//		analytics.add(line.toString());
//	}

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
