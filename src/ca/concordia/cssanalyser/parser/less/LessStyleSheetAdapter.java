package ca.concordia.cssanalyser.parser.less;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.selectors.AdjacentSiblingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.ChildSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.DescendantSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.NegationPseudoClass;
import ca.concordia.cssanalyser.cssmodel.selectors.PseudoClass;
import ca.concordia.cssanalyser.cssmodel.selectors.PseudoElement;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.cssmodel.selectors.SiblingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.SimpleSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.conditions.SelectorCondition;
import ca.concordia.cssanalyser.cssmodel.selectors.conditions.SelectorConditionType;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.CssClass;
import com.github.sommeri.less4j.core.ast.IdSelector;
import com.github.sommeri.less4j.core.ast.InterpolableName;
import com.github.sommeri.less4j.core.ast.Nth;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.SelectorAttribute;
import com.github.sommeri.less4j.core.ast.SelectorPart;

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
 			if (node instanceof RuleSet) {
 				RuleSet ruleSetNode = (RuleSet)node;
 				Selector selector = null;
 				
 				if (ruleSetNode.getSelectors().size() == 1) { // One selector, this is a base selector
 					selector = getSelector(ruleSetNode.getSelectors().get(0));
 				} else { // More than 1 selector, it is a grouping selector
 					GroupingSelector grouping = new GroupingSelector();

 					for (com.github.sommeri.less4j.core.ast.Selector lessSelector : ruleSetNode.getSelectors()) {
 						grouping.add(getSelector(lessSelector));					
 					}
 					
 					com.github.sommeri.less4j.core.ast.Selector firstSelector = ruleSetNode.getSelectors().get(0);
 					com.github.sommeri.less4j.core.ast.Selector lastSelector = ruleSetNode.getSelectors().get(ruleSetNode.getSelectors().size() - 1);

 					grouping.setLineNumber(firstSelector.getSourceLine());
 					grouping.setColumnNumber(firstSelector.getSourceColumn());
 					grouping.setOffset(firstSelector.getUnderlyingStructure().getTokenStartIndex());
 					grouping.setLength(lastSelector.getUnderlyingStructure().getTokenStopIndex() - firstSelector.getUnderlyingStructure().getTokenStartIndex());
 					
 					selector = grouping;
 				}
 				System.out.println(selector + " <" + selector.getOffset() + ", " + selector.getLength() + ">");
 				styleSheet.addSelector(selector);
 			} else if (node instanceof com.github.sommeri.less4j.core.ast.Media) {
 				throw new NotImplementedException("Media not yet implemented");
 			}
		}
		
		
		return styleSheet;
	}

	private BaseSelector getSelector(com.github.sommeri.less4j.core.ast.Selector lessSelector) {
		
		BaseSelector toReturn = null;
		
		if (lessSelector.getParts().size() == 1) { // simple selector, not a combinator
			toReturn = getSimpleSelectorFromLessSelectorPart(lessSelector.getParts().get(0));
		} else { // combinator
			toReturn = getCombinator(lessSelector.getParts());
		}
		
		return toReturn;
	}

	private BaseSelector getCombinator(List<SelectorPart> parts) {

		if (parts.size() == 0)
			return null;
		
		// Don't touch the original list
		List<SelectorPart> partsCopy = new ArrayList<>(parts);

		// Get the right most selector. It is always a SimpleSelector
		SelectorPart lastPart = partsCopy.get(partsCopy.size() - 1);
		SimpleSelector rightHandSelector = getSimpleSelectorFromLessSelectorPart(lastPart);
		
		BaseSelector toReturn = rightHandSelector;
		
		
		partsCopy.remove(partsCopy.size() - 1);
		
		if (partsCopy.size() != 0) {
			BaseSelector leftHandSelector = getCombinator(partsCopy);
			switch (lastPart.getLeadingCombinator().getCombinator()) {
			//ADJACENT_SIBLING("+"), CHILD(">"), DESCENDANT("' '"), GENERAL_SIBLING("~"), HAT("^"), CAT("^^");
			case ADJACENT_SIBLING:
				toReturn = new AdjacentSiblingSelector(leftHandSelector, rightHandSelector);
				break;
			case CHILD:
				toReturn = new ChildSelector(leftHandSelector, rightHandSelector);
				break;
			case DESCENDANT:
				toReturn = new DescendantSelector(leftHandSelector, rightHandSelector);
				break;
			case GENERAL_SIBLING:
				toReturn = new SiblingSelector(leftHandSelector, rightHandSelector);
				break;
			case HAT:
			case CAT:
				// Not supported
				return null;
			}
			
			SelectorPart firstPart = parts.get(0);
			toReturn.setLineNumber(firstPart.getSourceLine());
			toReturn.setColumnNumber(firstPart.getSourceColumn());
			int startIndex = firstPart.getUnderlyingStructure().getTokenStartIndex();
			toReturn.setOffset(startIndex);
			toReturn.setLength(lastPart.getUnderlyingStructure().getTokenStopIndex() - startIndex);
		} 
		
		return toReturn;
		
	}

	private SimpleSelector getSimpleSelectorFromLessSelectorPart(SelectorPart selectorPart) {
		SimpleSelector simpleSelector = new SimpleSelector();
		for (ASTCssNode cssASTNode : selectorPart.getChilds()) {
			if (cssASTNode instanceof InterpolableName) {
				InterpolableName name = (InterpolableName)cssASTNode;
				simpleSelector.setSelectedElementName(name.getName());
			} else if (cssASTNode instanceof IdSelector) {
				IdSelector id = (IdSelector)cssASTNode;
				simpleSelector.setElementID(id.getName());
			} else if (cssASTNode instanceof CssClass) {
				CssClass className = (CssClass)cssASTNode;
				simpleSelector.addClassName(className.getName());
			} else if (cssASTNode instanceof SelectorAttribute) {
				SelectorAttribute attribute = (SelectorAttribute)cssASTNode;
				SelectorCondition condition = new SelectorCondition(attribute.getName());
				SelectorConditionType adaptedConditionType = null;
				switch (attribute.getOperator().getOperator()) {
				case NONE:
					adaptedConditionType = SelectorConditionType.HAS_ATTRIBUTE;
					break;
				case EQUALS:
					adaptedConditionType = SelectorConditionType.VALUE_EQUALS_EXACTLY;
					break;
				case INCLUDES:
					adaptedConditionType = SelectorConditionType.VALUE_CONTAINS_WORD_SPACE_SEPARATED;
					break;
				case PREFIXMATCH:
					adaptedConditionType = SelectorConditionType.VALUE_STARTS_WITH;
					break;
				case SPECIAL_PREFIX:
					adaptedConditionType = SelectorConditionType.VALUE_START_WITH_DASH_SEPARATED;
					break;
				case SUBSTRINGMATCH:
					adaptedConditionType = SelectorConditionType.VALUE_CONTAINS;
					break;
				case SUFFIXMATCH:
					adaptedConditionType = SelectorConditionType.VALUE_ENDS_WITH;
				}
				condition.setConditionType(adaptedConditionType);
				if (adaptedConditionType != SelectorConditionType.HAS_ATTRIBUTE)
					condition.setValue(attribute.getValue().toString());
				simpleSelector.addCondition(condition);
				
			} else if (cssASTNode instanceof com.github.sommeri.less4j.core.ast.PseudoClass) {
				
				com.github.sommeri.less4j.core.ast.PseudoClass pseudoClass = (com.github.sommeri.less4j.core.ast.PseudoClass) cssASTNode;
				
				PseudoClass adaptedPseudoClass = null;
				if ("not".equals(pseudoClass.getName().toLowerCase())) {
					com.github.sommeri.less4j.core.ast.Selector parameter = (com.github.sommeri.less4j.core.ast.Selector)pseudoClass.getParameter();
					adaptedPseudoClass = new NegationPseudoClass(getSelector(parameter));
				} else {
					adaptedPseudoClass =  new PseudoClass(pseudoClass.getName());
					if (pseudoClass.hasParameters()) {
						if (pseudoClass.getParameter() instanceof Nth) {
							adaptedPseudoClass.setValue(NthToString((Nth)pseudoClass.getParameter()));
						} else if ("lang".equals(pseudoClass.getName().toLowerCase())) {
							adaptedPseudoClass.setValue(pseudoClass.getParameter().toString());
						} else {
							throw new NotImplementedException("Unhandled parameter for pseudo class :" + pseudoClass.getParameter());
						}
					}
				}
				simpleSelector.addPseudoClass(adaptedPseudoClass);	
			
			} else if (cssASTNode instanceof com.github.sommeri.less4j.core.ast.PseudoElement) {
				com.github.sommeri.less4j.core.ast.PseudoElement pseudoElement = (com.github.sommeri.less4j.core.ast.PseudoElement) cssASTNode;
				PseudoElement adaptedPseudoElement = new PseudoElement(pseudoElement.getName());
				simpleSelector.addPseudoElement(adaptedPseudoElement);
			}
		}
		simpleSelector.setLineNumber(selectorPart.getSourceLine());
		simpleSelector.setColumnNumber(selectorPart.getSourceColumn());
		simpleSelector.setOffset(selectorPart.getUnderlyingStructure().getTokenStartIndex());
		simpleSelector.setLength(selectorPart.getUnderlyingStructure().getTokenStopIndex() - selectorPart.getUnderlyingStructure().getTokenStartIndex());
		return simpleSelector;
	}

	private String NthToString(Nth parameter) {
		String toReturn = "";
		if (parameter.getRepeater() != null)
			toReturn = parameter.getRepeater().toString();
		
		if (parameter.getMod() != null)
			toReturn += parameter.getMod().toString();
		
		return toReturn;
	}
}
