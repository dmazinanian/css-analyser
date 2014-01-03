package ca.concordia.cssanalyser.analyser;


import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import ca.concordia.cssanalyser.analyser.duplication.Duplication;
import ca.concordia.cssanalyser.analyser.duplication.DuplicationFinder;
import ca.concordia.cssanalyser.analyser.duplication.DuplicationsList;
import ca.concordia.cssanalyser.analyser.duplication.apriori.Item;
import ca.concordia.cssanalyser.analyser.duplication.apriori.ItemSet;
import ca.concordia.cssanalyser.analyser.duplication.apriori.ItemSetList;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupedSelectors;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector.UnsupportedSelectorToXPathException;
import ca.concordia.cssanalyser.dom.DOMHelper;
import ca.concordia.cssanalyser.dom.Model;
import ca.concordia.cssanalyser.io.IOHelper;
import ca.concordia.cssanalyser.parser.CSSParser;



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
		
		String headerLine = "file_name|" +
				"size|" +
				"sloc|" +
				"num_selectors|" +
				"num_atomic_sel|" +
				"num_grouped_sel|" +
				"num_decs|" +
				"typeI|" +
				"typeII|" +
				"typeIII|" +
				"typeIV_A|" +
				"typeIV_B|" +
				"number_of_duplicated_declarations|" +
				"selectors_with_duplicated_declaration|" +
				"longest_dup|" +
				"max_sup_longest_dup";
		List<String> analytics = new ArrayList<>();
		//analytics.add(headerLine);
	
		// Do the analysis for each CSS file
		for (StyleSheet styleSheet: model.getStyleSheets()) {
						
			String filePath = styleSheet.getFilePath();
			String folderName = filePath + ".analyse";
			
			LOGGER.info("Finding different kinds of duplication in " + filePath);
			
			DuplicationFinder duplicationFinder = new DuplicationFinder(styleSheet);
			duplicationFinder.findDuplications();

			
			IOHelper.createFolder(folderName, true);
			
			DuplicationsList typeIDuplications = duplicationFinder.getTypeIDuplications();
			IOHelper.writeLinesToFile(typeIDuplications, folderName + "/typeI.txt");
		
			DuplicationsList typeIIDuplications = duplicationFinder.getTypeIIDuplications();
			IOHelper.writeLinesToFile(typeIIDuplications, folderName + "/typeII.txt");
			
			DuplicationsList typeIIIDuplications = duplicationFinder.getTypeIIIDuplications();
			IOHelper.writeLinesToFile(typeIIIDuplications, folderName + "/typeIII.txt");
			
			DuplicationsList typeIVADuplications = duplicationFinder.getTypeIVADuplications();
			IOHelper.writeLinesToFile(typeIVADuplications, folderName + "/typeIVA.txt");
			
			if (!dontUseDOM) {
				duplicationFinder.findTypeFourBDuplication(model.getDocument());
				DuplicationsList typeIVBDuplications = duplicationFinder.getTypeIVBDuplications();
				IOHelper.writeLinesToFile(typeIVBDuplications, folderName + "/typeIVB.txt");
			}
			
			List<String> xPaths = new ArrayList<>();
			for (Selector selector : styleSheet.getAllSelectors()) {
				try {
					xPaths.add(selector + "\n" + selector.getXPath() + "\n\n");
				} catch (UnsupportedSelectorToXPathException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					LOGGER.warn(String.format("No XPath for selector %s", selector));
				}
			}
			IOHelper.writeLinesToFile(xPaths, folderName + "/xPaths.txt");

			
			
			List<ItemSetList> aprioriResults = null, fpgrowthResults = null;
			
			if (doApriori) {
			
				LOGGER.info("Applying apriori algorithm with minimum support count of " + MIN_SUPPORT + " on " + filePath);

				long start = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				aprioriResults = duplicationFinder.apriori(MIN_SUPPORT);
				long end = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				long time = (end - start) / 1000000L;
				IOHelper.writeLinesToFile(aprioriResults, folderName + "/apriori.txt");
				
				LOGGER.info("Done Apriori in " + time);
			
			}
			
			if (doFPGrowth) {

				LOGGER.warn("Applying fpgrowth algorithm with minimum support count of " + MIN_SUPPORT + " on " + filePath);
				
				long start = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				fpgrowthResults = duplicationFinder.fpGrowth(MIN_SUPPORT);
				long end = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				long time = (end - start) / 1000000L;
				IOHelper.writeLinesToFile(fpgrowthResults, folderName + "/fpgrowth.txt");
				
				LOGGER.info("Done FP-Growth in" + time);
			}
			
			if (compareAprioriAndFPGrowth)
				compareAprioriAndFPGrowth(aprioriResults, fpgrowthResults);
			
			getAnalytics(styleSheet, duplicationFinder, fpgrowthResults, analytics); 
		}
		
		IOHelper.writeLinesToFile(analytics, "E:/Davood/joomla-cms-css-typeIV/analytics.txt", true);
					
	}
	
	private void getAnalytics(StyleSheet styleSheet, DuplicationFinder finder, List<ItemSetList> dupResults, List<String> analytics) {
		
		File file = new File(styleSheet.getFilePath());
		
		String fileName = file.getName();
		
		long size = (file.length() / 1024) + (file.length() % 1024 != 0 ? 1 : 0);
		int sloc = 0;
		String[] lines = styleSheet.toString().split("\r\n|\r|\n");
		for (String l : lines)
			if (!"".equals(l.trim()))
				sloc++;
		
		int numberOfSelectors = styleSheet.getAllSelectors().size();
		int numberOfAtomicSelectors = styleSheet.getAllAtomicSelectors().size();
		int numberOfDeclarations = styleSheet.getAllDeclarations().size();
		int numberOfGroupedSelectors = 0;
		for (Selector selector : styleSheet.getAllSelectors())
			if (selector instanceof GroupedSelectors)
				numberOfGroupedSelectors++;
		
		int numberOfTypeIDuplications = finder.getTypeIDuplications().getSize();
		int numberOfTypeIIDuplications = finder.getTypeIIDuplications().getSize();
		int numberOfTypeIIIDuplications = finder.getTypeIIIDuplications().getSize();
		int numberOfTypeIVADuplications = finder.getTypeIVADuplications().getSize();
		int numberOfTypeIVBDuplications = 0;
		if (finder.getTypeIVBDuplications() != null) 
			finder.getTypeIVBDuplications().getSize();

		Set<Selector> selectorsInDuplicatedDeclarations = new HashSet<>();
		for (Duplication d : finder.getTypeIDuplications())
			selectorsInDuplicatedDeclarations.addAll(d.getSelectors());
		
		for (Duplication d : finder.getTypeIIDuplications())
			selectorsInDuplicatedDeclarations.addAll(d.getSelectors());
		
		for (Duplication d : finder.getTypeIIIDuplications())
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
		line.append(size + "|");
		line.append(sloc + "|");
		line.append(numberOfSelectors + "|");
		line.append(numberOfAtomicSelectors + "|");
		line.append(numberOfGroupedSelectors + "|");
		line.append(numberOfDeclarations + "|");
		line.append(numberOfTypeIDuplications + "|");
		line.append(numberOfTypeIIDuplications + "|");
		line.append(numberOfTypeIIIDuplications + "|");
		line.append(numberOfTypeIVADuplications + "|");
		line.append(numberOfTypeIVBDuplications + "|");
		line.append(numberOfDuplicatedDeclarations + "|");
		line.append(numberOfSelectorsWithDuplications + "|");
		line.append(longestDupLength + "|");
		line.append(maxSupForLongestDup );
		analytics.add(line.toString());
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
