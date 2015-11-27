package ca.concordia.cssanalyser.crawler.plugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.plugin.GeneratesOutput;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.state.StateVertex;

import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.io.IOHelper;

/**
 * This plugin, written for Crawljax, is responsible for 
 * downloading all CSS files (including those which are added
 * dynamically at runtime) for analysing purposes.
 * @author Davood Mazinanian
 * 
 */
public class CSSCatcher implements OnNewStatePlugin, GeneratesOutput {
	
	private static final Logger LOGGER = FileLogger.getLogger(CSSCatcher.class);

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
				cssHrefs.add(href);
				fetchAndWriteFile(href, arg1.getName(), arg1.getUrl());
			}
		}

	}

	/**
	 * Fetches the file, given by href, and saves it to the
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
		
		int questionMark = href.indexOf("?");
		if (questionMark >= 0)
			href = href.substring(0, questionMark);
		
		int lastSlashPosition = href.lastIndexOf('/');
		
		// Get the name of file and append it to the desired folder
		String cssFileName = href.substring(lastSlashPosition + 1).replaceAll("[\\\\\\/:\\*\\?\\\"\\<\\>\\|]", "_");
		if (cssFileName.length() > 128)
			cssFileName = cssFileName.substring(0, 128);
		
		//If file name does not end with .css, add it
		if (!cssFileName.endsWith(".css"))
			cssFileName = cssFileName + ".css";
		
		String cssFilePath = folderPath + "/" + cssFileName;
		
		while ((new File(cssFilePath)).exists())
			cssFilePath += "_.css";

			
		try {

			StringBuilder builder = new StringBuilder();

			if (!href.startsWith("file://")) {
				getRemoteFileContents(href, builder);
			} else {
				String localFile = IOHelper.readFileToString(href.replaceFirst("file://[/]?", ""));
				builder.append(localFile);
			}
			
			if (builder.length() > 0) {

				final String EOL_CHAR = "\n";

				// Lets add some information to the head of this CSS file
				String headerText = String.format("/* " + EOL_CHAR +
						" * Created by CSSCatcher plugin for Crawljax" + EOL_CHAR +
						" * CSS file is for Crawljax DOM state %s" + EOL_CHAR +
						" * CSS file was contained in %s" + EOL_CHAR +
						" * Downloaded from %s" + EOL_CHAR +
						" */" + EOL_CHAR + EOL_CHAR,  forWebSite, stateName, href);

				IOHelper.writeStringToFile(headerText + builder.toString().replace("\r\n", EOL_CHAR), cssFilePath);

			}

		} catch (MalformedURLException e) {

			LOGGER.warn("Malformed url for file:" + href);

		} catch (IOException e) {

			LOGGER.warn("IOException for file:" + href);
			e.printStackTrace();

		}
	}
	
	private void getRemoteFileContents(String href, StringBuilder builder) {
		try {
			URLConnection urlConnection = (new URL(href)).openConnection();
			int contentLength = urlConnection.getContentLength();
			if (!"text/css".equals(urlConnection.getContentType()) || contentLength == -1) {
				LOGGER.warn("{} is not a CSS file, skipping", href);
			} else {
				InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
				byte[] data = new byte[contentLength];
				int bytesRead = 0;
				int offset = 0;
				while (offset < contentLength) {
					bytesRead = inputStream.read(data, offset, data.length - offset);
					if (bytesRead == -1)
						break;
					offset += bytesRead;
				}
				inputStream.close();

				if (offset != contentLength) {
					throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
				}
				builder.append(new String(data));	
			}
		} catch(IOException ex) {
			ex.printStackTrace();
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
			LOGGER.info(String.format("Created folder %s", path));
		}
		outputPatch = folder.getAbsolutePath();
		
	}

}
