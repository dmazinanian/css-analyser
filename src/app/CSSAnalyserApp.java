package app;

import java.io.File;

import java.io.IOException;

import org.apache.commons.io.FileUtils;

import analyser.CSSAnalyser;

import crawljax.Crawler;

public class CSSAnalyserApp {


	public static void main(String[] args) throws IOException {

		final String PAGE_URI = "http://yahoo.com";	
		
		String outputFolderPath = PAGE_URI.replace("http://", "").replace("/", "_");
		File folder = new File(outputFolderPath);
		// Clean the folder before running
		if (folder.exists())
			try {
				FileUtils.deleteDirectory(folder);
			} catch (IOException e) {
				
			}
		
		folder.mkdir();
		
		// Configure crawljax in Crawler class
		Crawler crawler = new Crawler(PAGE_URI, outputFolderPath);
		crawler.start();		

		//System.out.println(System.getProperty("user.dir"));
		
		CSSAnalyser cssAnalyser = new CSSAnalyser(outputFolderPath + "/css");
		cssAnalyser.analyse();
		
	}

	
}
