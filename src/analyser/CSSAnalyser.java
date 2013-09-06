package analyser;

import io.IOHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import analyser.duplication.Duplication;
import analyser.duplication.DuplicationFinder;
import analyser.duplication.ItemSetList;
import analyser.duplication.TypeIDuplication;

import parser.CSSParser;
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
	
	private final String analaysedWebSitename;
	
	/**
	 * Through this constructor, one should pass the
	 * folder containing all CSS files. Program will search
	 * for all files with extension ".css" and does the 
	 * analysis for these files 
	 * @param cssContainingFolder
	 */
	public CSSAnalyser(String domStateHTMLPath, String cssContainingFolder, String websiteName) {
		
		folderPath = cssContainingFolder;
		Document document = DOMHelper.getDocument(domStateHTMLPath);
		model = new Model(document);
		analaysedWebSitename = websiteName;
		parseStyleSheets();
		
	}
	
	private void parseStyleSheets() {
		
		// Lets search for all CSS files
		List<File> files = IOHelper.searchForFiles(folderPath, "css");

		for (File file : files) {
			
			String filePath = file.getAbsolutePath();
			
			LOGGER.info("Now parsing " + filePath);
			
			CSSParser parser = new CSSParser(filePath);

			StyleSheet styleSheet = parser.parseAndCreateStyleSheetObject();
			
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
	public void analyse() throws IOException {
		float sumOfAverages = 0,
			  totalSelectors = 0,
			  totalTypeISelectors = 0;
		FileWriter summaryFileWriter = IOHelper.openFile(folderPath + "/summary.txt");
		IOHelper.writeFile(summaryFileWriter, String.format("\r\n\r%45s\t'%s'\n\r\n\r", "STATISTICS FOR", analaysedWebSitename));
		IOHelper.writeFile(summaryFileWriter, String.format("%45s\t%15s\t%15s\t%15s\n\r\n\r", "FILE NAME", "#SELECTORS", "#TYPE I", "%"));
		
		for (StyleSheet styleSheet: model.getStyleSheets()) {
		
			totalSelectors+=styleSheet.getAllSelectors().size();
		
		}
		
		// Do the analysis for each CSS file
		for (StyleSheet styleSheet: model.getStyleSheets()) {
			
			// String filePath = styleSheet.getFilePath();
			
			//System.out.println(styleSheet);
			
			/*for (Selector selector : styleSheet.getAllSelectors()) {
				if (selector instanceof AtomicSelector) {
					AtomicSelector atomicSelector = (AtomicSelector)selector;
					String XPath = xpath.XPathHelper.AtomicSelectorToXPath(atomicSelector);
					System.out.println(atomicSelector + "->" + XPath);
					if (XPath == null) continue;
					NodeList nodeList = DOMHelper.queryDocument(model.getDocument(), XPath);
					if (nodeList == null)
						continue;
					for (int i = 0; i < nodeList.getLength(); ++i) {
						Element e = (Element) nodeList.item(i);
						System.out.println(e.getNodeName() + e.getTextContent());
					}
				}
			}*/
			
			String filePath = styleSheet.getFilePath(); 
			
			LOGGER.info("Finding different kinds of duplication in " + filePath);
			
			DuplicationFinder duplicationFinder = new DuplicationFinder(styleSheet);
			duplicationFinder.findDuplications();

			String folderName = filePath + ".analyse";
			
			IOHelper.createFolder(folderName, true);
			
			FileWriter fw = IOHelper.openFile(folderName + "/typeI.txt");
			
			Set<Selector> selectors = new HashSet<>();
			
			for (Duplication duplication : duplicationFinder.getTypeIDuplications()) {
				selectors.addAll(((TypeIDuplication)duplication).getSelectors());
				IOHelper.writeFile(fw, duplication.toString());
			}
			IOHelper.closeFile(fw);
			
			float currentAverage = 100 * selectors.size() / (float)styleSheet.getAllSelectors().size();
			
			sumOfAverages += currentAverage * (styleSheet.getAllSelectors().size() / (float)totalSelectors);
			//totalSelectors += styleSheet.getAllSelectors().size();
			totalTypeISelectors += selectors.size(); 
			
			IOHelper.writeFile(summaryFileWriter, String.format("%45s\t%15d\t%15d\t%15.3f\n", 
													filePath.substring(filePath.lastIndexOf('\\') + 1), 
													styleSheet.getAllSelectors().size(), 
													selectors.size(), 
													currentAverage));
			
			fw = IOHelper.openFile(folderName + "/typeII.txt");
			for (Duplication duplication : duplicationFinder.getTypeIIDuplications())
				IOHelper.writeFile(fw, duplication.toString());
			IOHelper.closeFile(fw);
			
			fw = IOHelper.openFile(folderName + "/typeIII.txt");
			for (Duplication duplication : duplicationFinder.getTypeIIIDuplications())
				IOHelper.writeFile(fw, duplication.toString());
			IOHelper.closeFile(fw);
			
			
			final int MIN_SUPPORT_COUNT = 2;
			
			LOGGER.info("Applying apriori algorithm with minimum support count of " + MIN_SUPPORT_COUNT);
			
			ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean(); 
			long start = threadMXBean.getCurrentThreadCpuTime();
			List<ItemSetList> l = duplicationFinder.apriori(MIN_SUPPORT_COUNT);
			long end = threadMXBean.getCurrentThreadCpuTime();
			long time = (end - start) / 1000000L;
			fw = IOHelper.openFile(folderName + "/apriori.txt");
			for (ItemSetList itemsetList : l) {
				IOHelper.writeFile(fw, itemsetList.toString());
			}
			String s = "CPU time (miliseconds) for apriori algorithm: %s\n" ;
			IOHelper.writeFile(fw, String.format(s, time));
			IOHelper.closeFile(fw);
			
			LOGGER.info(s);

			
			
			LOGGER.info("Done");
		}

		IOHelper.writeFile(summaryFileWriter, String.format("\n\r\n\r%45s\t%15.3f\t%15.3f\t%15.3f", 
															"Average", 
															totalSelectors / (float)model.getStyleSheets().size(), 
															totalTypeISelectors / (float)model.getStyleSheets().size(), 
															sumOfAverages )
							);
		IOHelper.closeFile(summaryFileWriter);
	}

	
}
