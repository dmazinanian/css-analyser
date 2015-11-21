package ca.concordia.cssanalyser.dom;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.io.IOHelper;

public final class DOMHelper {
	
	private static final Logger LOGGER = FileLogger.getLogger(DOMHelper.class);
		
	/**
	 * Reads all the HTML files (with extension .html) in the given folder.
	 * @param The folder in which the search must be conducted
	 * @return a List of org.w3c.dom.Document objects 
	 */
	public static List<Document> getDocuments(String folderPath) {
		
		List<Document> allDocuments = new ArrayList<>();
		
		for (File file : IOHelper.searchForFiles(folderPath, "html")) {
			
			try {
				
				Document d = com.crawljax.util.DomUtils.asDocument(file.getAbsolutePath());
				allDocuments.add(d);
				
			} catch (IOException e) {
				
				LOGGER.warn(String.format("Reading HTML file %s into DOM failed.", file.getAbsolutePath()));
				
			}			
		}
		
		return allDocuments;
	}
	
	/**
	 * Reads a HTML file into Document object
	 * @param htmlPath
	 * @return
	 * @throws  
	 */
	public static Document getDocument(String htmlPath) {
		try {
			
			String html = IOHelper.readFileToString(htmlPath);
			return com.crawljax.util.DomUtils.asDocument(html);
			
		} catch (IOException e) {
			LOGGER.warn(String.format("IO exception for HTML file %s", htmlPath));
		}	
		return null;
	}
	
	/**
	 * Queries a document with a given xPath and returns the result elements
	 * @param doc Document to be queried
	 * @param XPath XPath of the query
	 * @return A list of elements (org.w3c.dom.Element)
	 * @throws BadXPathException 
	 */
	public static NodeList queryDocument(Document doc, String XPath) throws BadXPathException {

		try {

			XPath xPathObj = XPathFactory.newInstance().newXPath();

			NodeList nodes = (NodeList) xPathObj.evaluate(XPath, doc, XPathConstants.NODESET);

			return nodes;

		} catch (XPathExpressionException e) {
			throw new BadXPathException(e);
		}
		
	}
	
	@SuppressWarnings("serial")
	public static class BadXPathException extends Exception {
		public BadXPathException(XPathExpressionException xpathExpressionException) {
			
		}
	}
	
}
