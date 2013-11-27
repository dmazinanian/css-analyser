package analyser;

import io.IOHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import analyser.duplication.Duplication;
import analyser.duplication.DuplicationFinder;
import analyser.duplication.DuplicationsList;
import analyser.duplication.TypeIDuplication;
import analyser.duplication.TypeIIDuplication;
import analyser.duplication.TypeIIIDuplication;
import analyser.duplication.apriori.Item;
import analyser.duplication.apriori.ItemSet;
import analyser.duplication.apriori.ItemSetList;

import parser.CSSParser;
import refactoring.RefactorerDuplications;
import CSSModel.StyleSheet;
import CSSModel.declaration.Declaration;
import CSSModel.selectors.GroupedSelectors;
import CSSModel.selectors.Selector;
import dom.DOMHelper;
import dom.Model;

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
		
		String headerLine = "file_name|size|sloc|num_selectors|num_atomic_sel|num_grouped_sel|num_decs|avg_dec_sel|grouping|typeI|typeII|typeIII|typeIV_A|total|num_dup_sel|dup_sel_weight|num_dup_dec|dup_dec_weight|longest_dup|max_sup_longest_dup";
		List<String> analytics = new ArrayList<>();
		analytics.add(headerLine);
	
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
				// TODO: TYPE IV B
			}
			
			
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
		
		IOHelper.writeLinesToFile(analytics, "e:/davood/analytics2.csv", true);
					
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
		float averageDeclarationsPerSelector = numberOfDeclarations / (float)numberOfSelectors;
		int numberOfGroupedSelectors = 0;
		for (Selector selector : styleSheet.getAllSelectors())
			if (selector instanceof GroupedSelectors)
				numberOfGroupedSelectors++;
		float grouppingIndex = (float)numberOfGroupedSelectors / numberOfSelectors;
		
		int numberOfTypeIDuplications = finder.getTypeIDuplications().getSize();
		int numberOfTypeIIDuplications = finder.getTypeIIDuplications().getSize();
		int numberOfTypeIIIDuplications = finder.getTypeIIIDuplications().getSize();
		int numberOfTypeIVADuplications = finder.getTypeIVADuplications().getSize();
		//int numberOfTypeIVBDuplications = finder.getTypeIVBDuplications().getSize();
		int totalDuplications = numberOfTypeIDuplications + 
								numberOfTypeIIDuplications + 
								numberOfTypeIIIDuplications + 
								numberOfTypeIVADuplications;
								//numberOfTypeIVBDuplications

		Set<Selector> selectorsInDuplications = new HashSet<>();
		for (Duplication d : finder.getTypeIDuplications())
			selectorsInDuplications.addAll(d.getSelectors());
		
		for (Duplication d : finder.getTypeIIDuplications())
			selectorsInDuplications.addAll(d.getSelectors());
		
		for (Duplication d : finder.getTypeIIIDuplications())
			selectorsInDuplications.addAll(d.getSelectors());
		
		int numberOfSelectorsWithDuplications = selectorsInDuplications.size();
			
		float selectorsWithDuplicationsWeight = (float)numberOfSelectorsWithDuplications / numberOfSelectors;

		Set<Declaration> duplicatedDeclarations = new HashSet<>();
		for (ItemSetList isl : dupResults)
			for (ItemSet is : isl)
				for (Item i : is)
					for (Declaration d : i)
						duplicatedDeclarations.add(d);
		
		int numberOfDuplicatedDeclarations = duplicatedDeclarations.size();
		
		float duplicatedDeclarationsWeight = (float)numberOfDuplicatedDeclarations / numberOfDeclarations;
		
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
		line.append(averageDeclarationsPerSelector + "|");
		line.append(grouppingIndex + "|");
		line.append(numberOfTypeIDuplications + "|");
		line.append(numberOfTypeIIDuplications + "|");
		line.append(numberOfTypeIIIDuplications + "|");
		line.append(numberOfTypeIVADuplications + "|");
		// line.append(numberOfTypeIVBDuplications + "|");
		line.append(totalDuplications + "|");
		line.append(numberOfSelectorsWithDuplications + "|");
		line.append(selectorsWithDuplicationsWeight + "|");
		line.append(numberOfDuplicatedDeclarations+ "|");
		line.append(duplicatedDeclarationsWeight+ "|");
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
