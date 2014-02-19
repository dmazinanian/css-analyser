package ca.concordia.cssanalyser.analyser;


import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;

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
	 * Invoking this method would result to the creation of 
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
					
			LOGGER.warn("Finding different kinds of duplication in " + filePath);
			
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
			
			DuplicationIncstanceList typeIVADuplications = duplicationFinder.getTypeIVADuplications();
			IOHelper.writeLinesToFile(typeIVADuplications, folderName + "/typeIVA.txt");
			
			if (!dontUseDOM) {
				//duplicationFinder.findTypeFourBDuplication(model.getDocument());
				//DuplicationIncstanceList typeIVBDuplications = duplicationFinder.getTypeIVBDuplications();
				//IOHelper.writeLinesToFile(typeIVBDuplications, folderName + "/typeIVB.txt");
			}
			
			//Map<BaseSelector, DOMNodeWrapperList> test = styleSheet.mapStylesheetOnDocument(model.getDocument());
			
			
		
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

//	private void WriteRefactoringOpportunitiesAndImpacts() {
	
	/*Comparator<ItemSet> comparator = new Comparator<ItemSet>() {
		public int compare(ItemSet o1, ItemSet o2) {
			return Integer.compare(o1.getRefactoringImpact(), o2.getRefactoringImpact());
		};
	};

		TreeSet<ItemSet> opportunities = new TreeSet<ItemSet>(comparator);
		for (ItemSetList isl : fpgrowthResults)
			for (ItemSet is : isl)
				opportunities.add(is);*/
//		
//		List<String> opp = new ArrayList<>();
//		
//		for (ItemSet is : opportunities)
//			//System.out.println(is + " : " + is.getRefactoringImpact());
//			opp.add(is.getSupport().size() + "," + is.size() + "," + is.getRefactoringImpact());
//		
//		IOHelper.writeLinesToFile(opp, folderName + "/oppnumbers.txt");
//		
//		List<String> opp2 = new ArrayList<>();
//		
//		for (ItemSet itemSetAndSupport : opportunities) {
//			//System.out.println(is + " : " + is.getRefactoringImpact());
//			StringBuilder sets = new StringBuilder();
//
//			
//
//				StringBuilder set = new StringBuilder("{");
//
//				for (Item d : itemSetAndSupport) {
//					set.append("(" + d.getFirstDeclaration() + "), ");
//				}
//
//				set.delete(set.length() - 2, set.length()).append("}");
//
//				sets.append(set);
//				sets.append(", " + itemSetAndSupport.getSupport().size() + " : ");
//
//				// for (Selector s : itemSetAndSupport.getSupport())
//				// sets.append(s + ", ");
//				sets.append(itemSetAndSupport.getSupport());
//
//			opp2.add(sets.toString() + ", " + itemSetAndSupport.getRefactoringImpact());
//		}
//		
//		IOHelper.writeLinesToFile(opp2, folderName + "/opp.txt");
		
//	}

	private void refactorGroupingOpportunities(int MIN_SUPPORT,
			StyleSheet styleSheet, String folderName,
			List<ItemSetList> fpgrowthResults, Document dom) {

		StyleSheet originalStyleSheet = styleSheet;
		
		CSSDependencyDetector dependencyDetector, refactoredDependencyDetector;
		
		if (!dontUseDOM) {                                                                                               
			dependencyDetector = new CSSDependencyDetector(originalStyleSheet, dom);
		} else {                                                                                                         
			dependencyDetector = new CSSDependencyDetector(originalStyleSheet);                                     
		}
		CSSValueOverridingDependencyList dependencies = dependencyDetector.findOverridingDependancies();

		int i = 0;
		while (true) {
			i++;
			// Find itemset with max impact
			ItemSet itemSetWithMaxImpact = ItemSetList.findItemSetWithMaxImpact(fpgrowthResults);
			
			if (itemSetWithMaxImpact == null || itemSetWithMaxImpact.getRefactoringImpact() < 0)
				break;

			LOGGER.warn("Applying round " + i + " of refactoring on " + originalStyleSheet.getFilePath() + ".");
			styleSheet = RefactorDuplications.groupingRefactoring(styleSheet, itemSetWithMaxImpact);
			IOHelper.writeStringToFile(styleSheet.toString(), folderName + "/refactored" + i + ".css");
													
			fpgrowthResults  = null;
			CSSParser parser = new CSSParser();
			try {
				styleSheet = parser.parseExternalCSS(folderName + "/refactored" + i + ".css");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			if (!dontUseDOM) {
				refactoredDependencyDetector = new CSSDependencyDetector(styleSheet, dom);          
			} else {
				refactoredDependencyDetector = new CSSDependencyDetector(styleSheet); 
			}
			
			CSSValueOverridingDependencyList refactoredDependencies = refactoredDependencyDetector.findOverridingDependancies();
			
			StringBuilder test = new StringBuilder(); 
			test.append(dependencies);                                                                                
			test.append(System.lineSeparator());                                                                                            
			test.append(refactoredDependencies);                                                                                                                                                                                  
			test.append(System.lineSeparator());                                    
			test.append(dependencies.getDifferencesWith(refactoredDependencies));
			
			//System.out.println(test);
			IOHelper.writeStringToFile(test.toString(), folderName + "/dependency-differences" + i + ".txt");
			
			RefactorToSatisfyDependencies r = new RefactorToSatisfyDependencies();
			StyleSheet s = r.refactorToSatisfyOverridingDependencies(styleSheet, dependencies); 
			CSSDependencyDetector dependencyDetector2 = new CSSDependencyDetector(s, dom); 
			CSSValueOverridingDependencyList dependencies2 = dependencyDetector2.findOverridingDependancies();        
			System.out.println("\n\nAfter refactoring " + i);  
			System.out.println(dependencies.getDifferencesWith(dependencies2));	
			
			DuplicationDetector duplicationFinderRefacored = new DuplicationDetector(styleSheet);
			duplicationFinderRefacored.findDuplications();
			fpgrowthResults = duplicationFinderRefacored.fpGrowth(MIN_SUPPORT);
			duplicationFinderRefacored = null;
			
			
		}
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
