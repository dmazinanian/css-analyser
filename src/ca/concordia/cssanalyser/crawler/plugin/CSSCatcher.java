package ca.concordia.cssanalyser.crawler.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.plugin.GeneratesOutput;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.state.StateVertex;

/**
 * This plugin, written for Crawljax, is responsible for 
 * downloading all CSS files (including those which are added
 * dynamically at runtime) for analysing purposes.
 * @author Davood Mazinanian
 * 
 */
public class CSSCatcher implements OnNewStatePlugin, GeneratesOutput {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CSSCatcher.class);

	private final Set<String> cssHrefs;
	private String outputPatch = "";

	public CSSCatcher() {
		cssHrefs = new HashSet<>();
	}

	@Override
	public void onNewState(CrawlerContext arg0, StateVertex arg1) {

		if ("".equals(getOutputFolder())) {
			LOGGER.warn("Output folder for CSSCather has not been set. " +
					"So there will be no output for CSSCatcher. " +
					"Use CSSCather.setOutputFolder() before CrawljaxRunner.call()");
		} 
		
		EmbeddedBrowser browser = arg0.getBrowser();

		int styleSheetLength = Integer.valueOf(browser.executeJavaScript("return document.styleSheets.length").toString());
		for (int i = 0; i < styleSheetLength; i++) {
			
			Object hrefObj = browser.executeJavaScript("return document.styleSheets[" + i + "].href");
			
			if (hrefObj != null) {
				String href = hrefObj.toString();
				//if (!cssHrefs.contains(href)) {
					cssHrefs.add(href);
					fetchAndWriteFile(href, arg1.getName(), arg1.getUrl());
				//}
			}
		}

	}

	/**
	 * Fethes the file, given by href, and saves it to the
	 * folder initialized with setOutputFolder()
	 * @param href
	 * @param forWebSite
	 */
	private void fetchAndWriteFile(String href, String stateName, String forWebSite) {
		
		File rootFile = new File(getOutputFolder());
		if (!rootFile.exists() || !rootFile.isDirectory())
			rootFile.mkdir();
		
		String folderPath =  getOutputFolder() + "/" + stateName;
		
		// Create the desired folder. One folder for each state

		File outputFolder = new File(folderPath);
		if (!outputFolder.exists() || !outputFolder.isDirectory())
			outputFolder.mkdir();
		
		int lastSlashPosition = href.lastIndexOf('/');
		
		// Get the name of file and append it to the desired folder
		String cssFileName = DigestUtils.shaHex(href.substring(lastSlashPosition));//href.substring(lastSlashPosition).replaceAll("[<>\\/?*:\"|]", "_");
		//if (cssFileName.length() > 128)
		//	cssFileName = cssFileName.substring(0, 128);
		
		// If file name does not end with .css, add it
		//if (!cssFileName.endsWith("css"))
			cssFileName = cssFileName + ".css";
		
		String cssFilePath = folderPath + "/" + cssFileName;
		
		//while ((new File(cssFilePath)).exists())
		//	cssFilePath += "_.css";
		if ((new File(cssFilePath)).exists())
			return;
		
		FileOutputStream fos = null;
		
		try {
			
			fos = new FileOutputStream(cssFilePath);
			
			// Lets add some information to the head of this css file
			String headerText = String.format("/* \n" +
										  " * Created by CSSCatcher plugin for Crawljax\n" +
										  " * CSS file is for Crawljax DOM state %s\n" +
										  " * CSS file was contained in %s" +
										  " * Downloaded from %s\n" +
										  " */\n\n",  forWebSite, stateName, href);
			
			byte[] headerBytes = headerText.getBytes();
			
			fos.write(headerBytes);
			
			URL remoteCSSFile = new URL(href);
			HttpURLConnection urlConnection = (HttpURLConnection) remoteCSSFile.openConnection();
		    urlConnection.setUseCaches(false);
		    urlConnection.setDoOutput(false);
		    urlConnection.setReadTimeout(10000);
			ReadableByteChannel rbc = Channels.newChannel(urlConnection.getInputStream());
			fos.getChannel().transferFrom(rbc, headerBytes.length, Long.MAX_VALUE);
					
		} catch (MalformedURLException e) {
			
			LOGGER.warn("Malformed url for file:" + href);
			
		} catch (IOException e) {
			
			LOGGER.warn("IOException for file:" + href);
			e.printStackTrace();
			
		} finally {
			
			try {
				fos.close();
				LOGGER.info("Saved file" + cssFilePath);
			} catch (Exception e) {
				// Swallow 
			}
			
		}
	}

	/**
	 * Returns a collection of all CSS hrefs,
	 * downloaded during crawling session.
	 * @return
	 */
	public Collection<String> getAllCSSHrefs() {
		return cssHrefs;
	}

	/**
	 * Gets the output folder in which
	 * the caught CSS files would be written
	 */
	@Override
	public String getOutputFolder() {
		return outputPatch;
	}

	/**
	 * Sets the output folder in which 
	 * the caught CSS files would be written.
	 * Must be called before using CrawljaxRunner.call() to
	 * let it write the CSS files in the desired folder. If not,
	 * no files would be created, but still one can access the 
	 * URIs of the caught CSS files using <code>getAllCSSHrefs()</code> method.
	 * If the specified folder does not exist, it will create the folder.
	 * @param path
	 */
	@Override
	public void setOutputFolder(String path) {
		
		File folder = new File(path);
		if (folder.exists()) {
			LOGGER.warn(String.format("CSSCatcher: output folder %s is not empty. Existing files would be overwriten.", path));
		} else {
			folder.mkdir();
			LOGGER.info(String.format("Folder %s did not exits, it was created.", path));
		}
		outputPatch = folder.getAbsolutePath();
		
	}

}
