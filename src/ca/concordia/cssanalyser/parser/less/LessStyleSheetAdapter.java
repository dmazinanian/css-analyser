package ca.concordia.cssanalyser.parser.less;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.cssmodel.selectors.SimpleSelector;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.RuleSet;

/**
 * Adapts a Less StyleSheet object to a CSSAnalyser StyleSheet object 
 * @author Davood Mazinanian
 *
 */
public class LessStyleSheetAdapter {

	public StyleSheet adapt(com.github.sommeri.less4j.core.ast.StyleSheet lessStyleSheet) {
		return adapt(lessStyleSheet, null);
	}
	
	public StyleSheet adapt(com.github.sommeri.less4j.core.ast.StyleSheet lessStyleSheet, String path) {
		
		StyleSheet styleSheet = new StyleSheet();
		
 		for (ASTCssNode node : lessStyleSheet.getChilds()) {
//			if (node instanceof RuleSet) {
//				RuleSet ruleSetNode = (RuleSet)node;
//				Selector selector;
//				if (ruleSetNode.getSelectors().size() == 1) {
//					com.github.sommeri.less4j.core.ast.Selector lessSelector = ruleSetNode.getSelectors().get(0);
//					selector = new BaseSelector(lessSelector.getSourceLine(), lessSelector.getSourceColumn());
//
//				} else {
//					
//				}
//				for (com.github.sommeri.less4j.core.ast.Selector s : ruleSetNode.getSelectors()) {
//					
//				}
//			}
 			System.out.println();
		}
		
 		//System.out.println(s.getUnderlyingStructure().getTokenStartIndex());
		
		return styleSheet;
	}
}
