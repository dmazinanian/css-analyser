package dom;

import io.IOHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class DOMHelper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DOMHelper.class);
		
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
	 */
	public static NodeList queryDocument(Document doc, String XPath) {

		try {

			//List<Element> toReturn = new ArrayList<>();

			XPath xPathObj = XPathFactory.newInstance().newXPath();

			NodeList nodes = (NodeList) xPathObj.evaluate(XPath, doc, XPathConstants.NODESET);

			/*for (int i = 0; i < nodes.getLength(); ++i) {
				Element e = (Element) nodes.item(i);
				toReturn.add(e);
			}*/

			//return toReturn;
			return nodes;

		} catch (XPathExpressionException e) {
			LOGGER.warn(String.format("Error in XPath expression %s", XPath));
		}
		return null;
	}
	
}
