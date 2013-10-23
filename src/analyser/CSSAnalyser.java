package analyser;

import io.IOHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
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
import analyser.duplication.apriori.Item;
import analyser.duplication.apriori.ItemSet;
import analyser.duplication.apriori.ItemSetList;

import parser.CSSParser;
import refactoring.RefactorerDuplications;
import CSSModel.StyleSheet;
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
			StyleSheet styleSheet = parser.parseExternalCSS(filePath);	
			model.addStyleSheet(styleSheet);
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
				
		// Do the analysis for each CSS file
		for (StyleSheet styleSheet: model.getStyleSheets()) {
						
			String filePath = styleSheet.getFilePath();
			String folderName = filePath + ".analyse";
			
			LOGGER.info("Finding different kinds of duplication in " + filePath);
			
			DuplicationFinder duplicationFinder = new DuplicationFinder(styleSheet);
			duplicationFinder.findDuplications();

			
			IOHelper.createFolder(folderName, true);
			
			DuplicationsList typeIDuplications = duplicationFinder.getTypeIDuplications();
			writeToFile(typeIDuplications, folderName + "/typeI.txt");
		
			DuplicationsList typeIIDuplications = duplicationFinder.getTypeIIDuplications();
			writeToFile(typeIIDuplications, folderName + "/typeII.txt");
			
			DuplicationsList typeIIIDuplications = duplicationFinder.getTypeIIIDuplications();
			writeToFile(typeIIIDuplications, folderName + "/typeIII.txt");
			
			if (!dontUseDOM) {
				// TODO: TYPE IV
			}
			
			
			List<ItemSetList> aprioriResults = null, fpgrowthResults = null;
			
			if (doApriori) {
			
				LOGGER.info("Applying apriori algorithm with minimum support count of " + MIN_SUPPORT);

				long start = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				aprioriResults = duplicationFinder.apriori(MIN_SUPPORT);
				long end = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				long time = (end - start) / 1000000L;
				writeToFile(aprioriResults, folderName + "/apriori.txt");
				
				LOGGER.info("Done Apriori in " + time);
			
			}
			
			if (doFPGrowth) {

				LOGGER.info("Applying fpgrowth algorithm with minimum support count of " + MIN_SUPPORT);
				
				long start = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				fpgrowthResults = duplicationFinder.fpGrowth(MIN_SUPPORT);
				long end = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				long time = (end - start) / 1000000L;
				writeToFile(fpgrowthResults, folderName + "/fpgrowth.txt");
				
				LOGGER.info("Done FP-Growth in" + time);
			}
			
			if (compareAprioriAndFPGrowth)
				compareAprioriAndFPGrowth(aprioriResults, fpgrowthResults);
		}
		
		
		
				
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

	private void writeToFile(Iterable<?> duplicationsList, String path) {
		
		try {
			
			BufferedWriter fw = IOHelper.openFile(path);

			for (Object row : duplicationsList) {
				IOHelper.writeFile(fw, row.toString());
			}
			
			IOHelper.closeFile(fw);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
}
