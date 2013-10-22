package app;

import io.IOHelper;

import java.io.File;

import java.io.IOException;
import java.util.List;


import analyser.CSSAnalyser;

public class CSSAnalyserApp {


	public static void main(String[] args) throws IOException {
		
//		for (String s : args)
//			System.out.println("arg " + s);
		
		
		
		final String PAGE_URI = "http://test.com";
		
		final int MIN_APRIORI_SUPPORT = 2;
		
		String outputFolderPath = "E:/davood/" + PAGE_URI.replace("http://", "").replace("/", "_");
	
		
//		File folder = new File(outputFolderPath);
//		// Clean the folder before running
//		if (folder.exists())
//			try {
//				FileUtils.deleteDirectory(folder);
//			} catch (IOException e) {
//				
//			}
//		
//		folder.mkdir();
		
		// Make sure to configure crawljax in Crawler class
		//Crawler crawler = new Crawler(PAGE_URI, outputFolderPath);
		//crawler.start();		

		//System.out.println(System.getProperty("user.dir"));
		
		// Get all dom states in outputFolder/crawljax/doms		
		List<File> allStatesFiles = IOHelper.searchForFiles(outputFolderPath + "/crawljax/doms", "html");	
		
		for (File domStateHtml : allStatesFiles) {
			
			String stateName = domStateHtml.getName();
			// Remove .html
			String correspondingCSSFolderName = stateName.substring(0, stateName.length() - 5);
			
			CSSAnalyser cssAnalyser = new CSSAnalyser(domStateHtml.getAbsolutePath(), outputFolderPath + "/css/" + correspondingCSSFolderName, PAGE_URI);
			cssAnalyser.analyse(MIN_APRIORI_SUPPORT);
			
		}
			
		
	}

	
}
