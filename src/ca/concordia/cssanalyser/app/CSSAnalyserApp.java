package ca.concordia.cssanalyser.app;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.concordia.cssanalyser.analyser.CSSAnalyser;
import ca.concordia.cssanalyser.crawler.Crawler;
import ca.concordia.cssanalyser.io.IOHelper;

public class CSSAnalyserApp {
	
	private static Logger LOGGER = LoggerFactory.getLogger(CSSAnalyserApp.class);

	public static void main(String[] args) throws IOException {
		
		ParametersParser params = new ParametersParser(args);
		
		switch (params.getProgramMode()) {
		case CRAWL:
			if ("".equals(params.getOutputFolderPath())) {
				LOGGER.error("Please provide an output folder using --outfolder:out/folder.");
				return;
			} else if ("".equals(params.getUrl()) && "".equals(params.getListOfURLsToAnalyzeFilePath())) {
				LOGGER.error("Please provide a url using --url:http://url/to/site or the file containing list of urls using --urlfile:path/to/url");
				return;
			}
			
			List<String> urls = new ArrayList<>();
			
			if (!"".equals(params.getListOfURLsToAnalyzeFilePath())) {
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

					CSSAnalyser cssAnalyser = new CSSAnalyser(domStateHtml.getAbsolutePath(), outputFolderPath + "css/" + correspondingCSSFolderName);
					cssAnalyser.analyse(params.getFPGrowthMinsup());

				}
				
			}
			
			break;
		case FOLDER:

			List<String> folders = new ArrayList<>();
			
			if (!"".equals(params.getInputFolderPath()))
				folders.add(params.getInputFolderPath());
			else if (!"".equals(params.getListOfFoldersPathsToBeCrawled())) {
				folders.addAll(params.getFoldersListToBeCrawled());
			} else {
				LOGGER.error("Please provide an input folder with --infolder:in/folder or list of folders using --foldersfile:path/to/file.");
				return;
			}
			
			for (String folder : folders) {
			
				List<File> allStatesFiles = IOHelper.searchForFiles(folder + "crawljax/doms", "html");	
				for (File domStateHtml : allStatesFiles) {

					String stateName = domStateHtml.getName();
					// Remove .html
					String correspondingCSSFolderName = stateName.substring(0, stateName.length() - 5);

					CSSAnalyser cssAnalyser = new CSSAnalyser(domStateHtml.getAbsolutePath(), folder + "css/" + correspondingCSSFolderName);
					cssAnalyser.analyse(params.getFPGrowthMinsup());

				}
			}
			
			break;
		case NODOM:
			if ("".equals(params.getInputFolderPath())) {
				LOGGER.error("Please provide an input folder with --infolder:in/folder.");
				return;
			}
			CSSAnalyser cssAnalyser = new CSSAnalyser(params.getInputFolderPath());
			cssAnalyser.analyse(params.getFPGrowthMinsup());

			break;
		case DIFF:
		case PREP:
			throw new RuntimeException("Not yet implemented");
		default:
		}		
	}

	
	
}
