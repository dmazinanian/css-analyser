package ca.concordia.cssanalyser.dom;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector.UnsupportedSelectorToXPathException;
import ca.concordia.cssanalyser.io.IOHelper;

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
	 * @throws BadXPathException 
	 */
	public static NodeList queryDocument(Document doc, String XPath) throws BadXPathException {

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
			throw new BadXPathException(e);
		}
		
	}
	
	@SuppressWarnings("serial")
	public static class BadXPathException extends Exception {
		public BadXPathException(XPathExpressionException xpathExpressionException) {
			
		}
	}

	/**
	 * Maps every base selector to a node list in the stylesheet
	 * @param dom
	 * @param styleSheet
	 * @return
	 */
	public static Map<BaseSelector, NodeList> mapStylesheetOnDocument(Document dom, StyleSheet styleSheet) {
		List<BaseSelector> allSelectors = styleSheet.getAllBaseSelectors();
		Map<BaseSelector, NodeList> selectorNodeListMap = new LinkedHashMap<>();
		for (BaseSelector selector : allSelectors) {
			NodeList nodes;
			try {
				nodes = queryDocument(dom, selector.getXPath());
				selectorNodeListMap.put(selector, nodes);
			} catch (BadXPathException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedSelectorToXPathException ex) {
				
			}
		}
		return selectorNodeListMap;
	}

	/**
	 * Returns a list of documents' node in addition to the CSS selectors which
	 * select each node
	 * @param document
	 * @param styleSheet
	 * @return
	 */
	public static Map<Node, List<BaseSelector>> getCSSClassesForDOMNodes(Document document, StyleSheet styleSheet) {
		
		// Map every node in the DOM tree to a list of selectors in the stylesheet
		Map<Node, List<BaseSelector>> nodeToSelectorsMapping = new HashMap<>();
		for (BaseSelector selector : styleSheet.getAllBaseSelectors()) {
			try {
				NodeList matchedNodes = queryDocument(document, selector.getXPath());
				for (int i = 0; i < matchedNodes.getLength(); i++) {
					Node node = matchedNodes.item(i);
					List<BaseSelector> correspondingSelectors = nodeToSelectorsMapping.get(node);
					if (correspondingSelectors == null) {
						correspondingSelectors = new ArrayList<>();
					}
					correspondingSelectors.add(selector);
					nodeToSelectorsMapping.put(node, correspondingSelectors);
				}
			} catch (BadXPathException e) {
				e.printStackTrace();
			} catch (UnsupportedSelectorToXPathException e) {
				e.printStackTrace();
			}
		}
		return nodeToSelectorsMapping;
	}
	
}
