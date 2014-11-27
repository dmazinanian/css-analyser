package ca.concordia.cssanalyser.cssmodel.selectors;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import ca.concordia.cssanalyser.dom.DOMHelper;
import ca.concordia.cssanalyser.dom.DOMHelper.BadXPathException;
import ca.concordia.cssanalyser.dom.DOMNodeWrapper;
import ca.concordia.cssanalyser.dom.DOMNodeWrapperList;

/**
 * Represents one of the binary CSS combinators
 * @author Davood Mazinanian
 *
 */
public abstract class Combinator extends BaseSelector {
	
	public abstract SimpleSelector getRightHandSideSelector();
	public abstract BaseSelector getLeftHandSideSelector(); 
	
	@Override
	public DOMNodeWrapperList getSelectedNodes(Document document) {
		DOMNodeWrapperList toReturn = new DOMNodeWrapperList();
		try {
			NodeList nodes = DOMHelper.queryDocument(document, getXPath());
			for (int i = 0; i < nodes.getLength(); i++) {
				toReturn.add(new DOMNodeWrapper(nodes.item(i), getRightHandSideSelector().getUnsupportedPseudoClasses()));
			}
			
		} catch (BadXPathException | UnsupportedSelectorToXPathException e) {
			e.printStackTrace();
		}

		return toReturn;
	}
}
