package ca.concordia.cssanalyser.app;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.concordia.cssanalyser.analyser.CSSAnalyser;
import ca.concordia.cssanalyser.crawler.Crawler;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.io.IOHelper;
import ca.concordia.cssanalyser.migration.topreprocessors.MixinRefactoringOpportunity;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorMigrationOpportunitiesDetector;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorMigrationOpportunity;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessRefactoringOpportunitiesDetector;
import ca.concordia.cssanalyser.parser.CSSParser;
import ca.concordia.cssanalyser.parser.CSSParserFactory;
import ca.concordia.cssanalyser.parser.CSSParserFactory.CSSParserType;
import ca.concordia.cssanalyser.parser.ParseException;

public class CSSAnalyserCLI {
	
	private static Logger LOGGER = FileLogger.getLogger(CSSAnalyserCLI.class);

	public static void main(String[] args) throws IOException {
		
		ParametersParser params = new ParametersParser(args);
		
		switch (params.getProgramMode()) {
		case CRAWL:
			if (params.getOutputFolderPath() == null) {
				LOGGER.error("Please provide an output folder using --outfolder:out/folder.");
				return;
			} else if (params.getUrl() == null && params.getListOfURLsToAnalyzeFilePath() == null) {
				LOGGER.error("Please provide a url using --url:http://url/to/site or the file containing list of urls using --urlfile:path/to/url");
				return;
			}
			
			List<String> urls = new ArrayList<>();
			
			if (params.getListOfURLsToAnalyzeFilePath() != null) {
				urls.addAll(params.getURLs());
			} else {
				urls.add(params.getUrl());
			}
			
			for (String currentUrl : urls) {
			
				String outputFolderPath = params.getOutputFolderPath() + currentUrl.replaceFirst("http[s]?://", "").replaceFirst("file://", "").replace("/", "_").replace(":", "_") + "/";
				// Make sure to configure ca.concordia.cssanalyser.crawler in Crawler class
				Crawler crawler = new Crawler(currentUrl, outputFolderPath);
				crawler.start();

				// Get all ca.concordia.cssanalyser.dom states in outputFolder/crawljax/doms		
				List<File> allStatesFiles = IOHelper.searchForFiles(outputFolderPath + "crawljax/doms", "html");	
				for (File domStateHtml : allStatesFiles) {

					String stateName = domStateHtml.getName();
					// Remove .html
					String correspondingCSSFolderName = stateName.substring(0, stateName.length() - 5);
					
					try {

						CSSAnalyser cssAnalyser = new CSSAnalyser(domStateHtml.getAbsolutePath(), outputFolderPath + "css/" + correspondingCSSFolderName);
						cssAnalyser.analyse(params.getFPGrowthMinsup());
					
					} catch (FileNotFoundException fnfe) {
						LOGGER.warn(fnfe.getMessage());
					}

				}
				
			}
			
			break;
		case FOLDER:

			List<String> folders = new ArrayList<>();
			
			if (params.getInputFolderPath() != null)
				folders.add(params.getInputFolderPath());
			else if (params.getListOfFoldersPathsToBeAnayzedFile() != null) {
				folders.addAll(params.getFoldersListToBeAnalyzed());
			} else {
				LOGGER.error("Please provide an input folder with --infolder:in/folder or list of folders using --foldersfile:path/to/file.");
				return;
			}
			
			for (String folder : folders) {
				List<File> allStatesFiles = IOHelper.searchForFiles(folder + "crawljax/doms", "html");	
				if (allStatesFiles.size() == 0) {
					LOGGER.warn("No HTML file found in " + folder + "crawljax/doms, skipping this folder");
				} else {
					for (File domStateHtml : allStatesFiles) {
	
						String stateName = domStateHtml.getName();
						// Remove .html
						String correspondingCSSFolderName = stateName.substring(0, stateName.length() - 5);
						
						try {
	
							CSSAnalyser cssAnalyser = new CSSAnalyser(domStateHtml.getAbsolutePath(), folder + "css/" + correspondingCSSFolderName);
							cssAnalyser.analyse(params.getFPGrowthMinsup());
						
						} catch (FileNotFoundException fnfe) {
							LOGGER.warn(fnfe.getMessage());
						}
	
					}
				}
			}
			
			break;
		case NODOM:
			CSSAnalyser cssAnalyser = null;
			if (params.getInputFolderPath() != null) {
				try {
					cssAnalyser = new CSSAnalyser(params.getInputFolderPath());
				} catch (FileNotFoundException fnfe) {
					LOGGER.warn(fnfe.getMessage());
				}
			} else {
				LOGGER.error("Please provide an input folder with --infolder:in/folder");
				return;
			}
			cssAnalyser.analyse(params.getFPGrowthMinsup());
			break;
		case DIFF:
			throw new RuntimeException("Not yet implemented");
		case PREP:
			if (!"".equals(params.getFilePath())) {


				try {
					

					CSSParser parser = CSSParserFactory.getCSSParser(CSSParserType.LESS);
					StyleSheet styleSheet = parser.parseExternalCSS(params.getFilePath());
//					PreprocessorRefactoringOpportunitiesDetector preprocessorOpportunities = new PreprocessorRefactoringOpportunitiesDetector(styleSheet);
//					Iterable<MixinRefactoringOpportunity> refactoringOpportunities = preprocessorOpportunities.findMixinOpportunities();
					

				} catch (ParseException e) {
//
//					e.printStackTrace();
//
				}
			}
			else 
				LOGGER.error("No CSS file is provided.");
			break;
		default:
		}		
	}

	
	
}
