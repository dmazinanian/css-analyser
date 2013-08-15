package analyser;

import io.IOHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import parser.CSSParser;
import CSSModel.StyleSheet;
import CSSModel.selectors.AtomicSelector;
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
		
		// Do the analysis for each CSS file
		for (StyleSheet styleSheet: model.getStyleSheets()) {
			
			// String filePath = styleSheet.getFilePath();
			
			//LOGGER.info(styleSheet);
			
			for (Selector selector : styleSheet.getAllSelectors()) {
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
			}
			
			/*LOGGER.info("Finding different kinds of duplication in " + filePath);
			
			DuplicationFinder duplicationFinder = new DuplicationFinder(styleSheet);

			String folderName = filePath + ".analyse";
			
			FileUtils.createFolder(folderName, true);
				
			LOGGER.info("Finding identical selectors in " + filePath);
			FileWriter fw = FileUtils.openFile(folderName + "/identical_selectors.txt");
			DuplicationsList identicalSelectorsDuplication = duplicationFinder.findIdenticalSelectors();
			for (Duplication duplication : identicalSelectorsDuplication)
				FileUtils.writeFile(fw, duplication.toString());
			FileUtils.closeFile(fw);

			LOGGER.info("Finding identical declarations in " + filePath);
			fw = FileUtils.openFile(folderName + "/identical_declarations.txt");
			for (Duplication duplication : duplicationFinder.findIdenticalDeclarations()) {
				FileUtils.writeFile(fw, duplication.toString());
			}
			FileUtils.closeFile(fw);

			LOGGER.info("Finding identical values in " + filePath);
			fw = FileUtils.openFile(folderName + "/identical_values.txt");
			for (Duplication duplication : duplicationFinder.findIdenticalValues()) {
				FileUtils.writeFile(fw, duplication.toString());
			}
			FileUtils.closeFile(fw);
			
			LOGGER.info("Finding identical effects (identical selector and declarations at the same time) in " + filePath);
			fw = FileUtils.openFile(folderName + "/identical_effects.txt");
			for (Duplication duplication : duplicationFinder.findIdenticalEffects(identicalSelectorsDuplication)) {
				FileUtils.writeFile(fw, duplication.toString());
			}
			FileUtils.closeFile(fw);
			
			LOGGER.info("Finding overriden values in " + filePath);
			fw = FileUtils.openFile(folderName + "/overriden_values.txt");
			for (Duplication duplication : duplicationFinder.findOverridenValues(identicalSelectorsDuplication)) {
				FileUtils.writeFile(fw, duplication.toString());
			}
			FileUtils.closeFile(fw);
			
			
			final int MIN_SUPPORT_COUNT = 2;
			
			LOGGER.info("Applying apriori algorithm with minimum support count of " + MIN_SUPPORT_COUNT);
			
			ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean(); 
			long start = threadMXBean.getCurrentThreadCpuTime();
			List<ItemSetList> l = duplicationFinder.apriori(MIN_SUPPORT_COUNT);
			long end = threadMXBean.getCurrentThreadCpuTime();
			long time = (end - start) / 1000000L;
			fw = FileUtils.openFile(folderName + "/apriori.txt");
			for (ItemSetList itemsetList : l) {
				FileUtils.writeFile(fw, itemsetList.toString());
			}
			String s = "CPU time (miliseconds) for apriori algithm: %s\n" ;
			FileUtils.writeFile(fw, String.format(s, time));
			FileUtils.closeFile(fw);
			
			LOGGER.info(s);*/

			
			
			LOGGER.info("Done");
		}
	}

	
}
