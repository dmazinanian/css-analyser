package ca.concordia.cssanalyser.parser.less;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.DeclarationFactory;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValueFactory;
import ca.concordia.cssanalyser.cssmodel.declaration.value.ValueType;
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
import com.github.sommeri.less4j.core.ast.BinaryExpression;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.ColorExpression.ColorWithAlphaExpression;
import com.github.sommeri.less4j.core.ast.CssClass;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.IdSelector;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.InterpolableName;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.ast.ListExpressionOperator.Operator;
import com.github.sommeri.less4j.core.ast.NamedColorExpression;
import com.github.sommeri.less4j.core.ast.Nth;
import com.github.sommeri.less4j.core.ast.NumberExpression;
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
		
		StyleSheet styleSheet = new StyleSheet();
		styleSheet.setPath(lessStyleSheet.getSource().toString());
			
 		for (ASTCssNode node : lessStyleSheet.getChilds()) {
 			
 			if (node instanceof RuleSet) {
 				
 				RuleSet ruleSetNode = (RuleSet)node;
 				Selector selector = null;
 				
 				if (ruleSetNode.getSelectors().size() == 1) { // One selector, this is a base selector
 					
 					selector = getBaseSelectorFromLessSelector(ruleSetNode.getSelectors().get(0));
 					
 				} else { // More than 1 selector, this is a grouping selector
 					GroupingSelector grouping = new GroupingSelector();

 					for (com.github.sommeri.less4j.core.ast.Selector lessSelector : ruleSetNode.getSelectors()) {
 						grouping.add(getBaseSelectorFromLessSelector(lessSelector));					
 					}
 					
 					com.github.sommeri.less4j.core.ast.Selector firstSelector = ruleSetNode.getSelectors().get(0);
 					com.github.sommeri.less4j.core.ast.Selector lastSelector = ruleSetNode.getSelectors().get(ruleSetNode.getSelectors().size() - 1);

 					grouping.setLineNumber(firstSelector.getSourceLine());
 					grouping.setColumnNumber(firstSelector.getSourceColumn());
 					grouping.setOffset(firstSelector.getUnderlyingStructure().getTokenStartIndex());
 					grouping.setLength(lastSelector.getUnderlyingStructure().getTokenStopIndex() - firstSelector.getUnderlyingStructure().getTokenStartIndex());
 					
 					selector = grouping;
 				}
 				
 				// Handle declarations
 				for (ASTCssNode declarationNode : ruleSetNode.getBody().getDeclarations()) {
 					
 					if (declarationNode instanceof com.github.sommeri.less4j.core.ast.Declaration) {
 						
 						com.github.sommeri.less4j.core.ast.Declaration lessDeclaration = (com.github.sommeri.less4j.core.ast.Declaration)declarationNode;  
 						
 						String property = lessDeclaration.getNameAsString();
 						List<DeclarationValue> values;

 						if (lessDeclaration.getExpression() != null) { // If a declaration does not have a value, happened in some cases
 							values = getListOfDeclarationValuesFromLessExpression(property, lessDeclaration.getExpression());
 						} else {
 							values  = new ArrayList<>();
 							values.add(new DeclarationValue("", ValueType.OTHER));
 						}

 						Declaration declaration = DeclarationFactory.getDeclaration(
 								property, values, selector, declarationNode.getSourceLine(), 
 								declarationNode.getSourceColumn(), lessDeclaration.isImportant(), true);
 						selector.addDeclaration(declaration);
 						
 					} else {
 						throw new RuntimeException("What is that?" + declarationNode);
 					}
 				}
 				styleSheet.addSelector(selector);
 			} else if (node instanceof com.github.sommeri.less4j.core.ast.Media) {
 				throw new NotImplementedException("Media not yet implemented");
 			}
		}
		
		System.out.println(styleSheet.toString());
		return styleSheet;
	}

	private List<DeclarationValue> getListOfDeclarationValuesFromLessExpression(String property, Expression expression) {
		
		List<DeclarationValue> values = new ArrayList<>();
		
		if (expression instanceof ListExpression) {
			ListExpression listExpression = (ListExpression)expression;
			for (Iterator<Expression> iterator = listExpression.getExpressions().iterator(); iterator.hasNext();) {
				
				Expression expr = iterator.next();
				
				List<DeclarationValue> vals = getListOfDeclarationValuesFromLessExpression(property, expr);
				values.addAll(vals);
				if (listExpression.getOperator() != null) {
					
					if (listExpression.getOperator().getOperator() ==  Operator.COMMA) {
						if (iterator.hasNext())
							values.add(DeclarationValueFactory.getDeclarationValue(property, ",", ValueType.SEPARATOR));
					} else if (listExpression.getOperator().getOperator() ==  Operator.EMPTY_OPERATOR) {
						// Do nothing
					} else {
						throw new RuntimeException("Operator = " + listExpression.getOperator());
					}
					
				} else {
					throw new RuntimeException("Operator = " + listExpression.getOperator());
				}
			}
				
		}  else if (expression instanceof NumberExpression) {

			NumberExpression numberExpression = (NumberExpression)expression;

			switch(numberExpression.getDimension()) {
				case ANGLE:
					values.add(DeclarationValueFactory.getDeclarationValue(property, numberExpression.getOriginalString(), ValueType.ANGLE));
				case EMS:
				case EXS:
				case LENGTH:
					values.add(DeclarationValueFactory.getDeclarationValue(property, numberExpression.getOriginalString(), ValueType.LENGTH));
					break;
				case FREQ:
					values.add(DeclarationValueFactory.getDeclarationValue(property, numberExpression.getOriginalString(), ValueType.FREQUENCY));
					break;
				case NUMBER:
					if (numberExpression.getOriginalString().indexOf(".") > -1)
						values.add(DeclarationValueFactory.getDeclarationValue(property, DeclarationValueFactory.formatFloat(numberExpression.getValueAsDouble()), ValueType.REAL));
					else
						values.add(DeclarationValueFactory.getDeclarationValue(property, DeclarationValueFactory.formatFloat(numberExpression.getValueAsDouble()), ValueType.INTEGER));
					break;
				case PERCENTAGE:
					values.add(DeclarationValueFactory.getDeclarationValue(property, numberExpression.getOriginalString(), ValueType.PERCENTAGE));
					break;
				case REPEATER:
					throw new RuntimeException("What is " + property + ":" + numberExpression.getOriginalString());
				case TIME:
					values.add(DeclarationValueFactory.getDeclarationValue(property, numberExpression.getOriginalString(), ValueType.TIME));
					break;
				case UNKNOWN:
					if ("turn".equals(numberExpression.getSuffix().toLowerCase()))
						values.add(DeclarationValueFactory.getDeclarationValue(property, numberExpression.getOriginalString(), ValueType.ANGLE));
					else
						throw new RuntimeException("What is " + property + ":" + numberExpression.getOriginalString());
				default:
					break;
			}

		} else if (expression instanceof NamedColorExpression) {
			NamedColorExpression  namedExpression = (NamedColorExpression)expression;
			values.add(DeclarationValueFactory.getDeclarationValue(property, namedExpression.getColorName(), ValueType.IDENT));
		} else if (expression instanceof ColorWithAlphaExpression) {
			ColorWithAlphaExpression colorWithAlpha = (ColorWithAlphaExpression)expression;
			throw new RuntimeException(colorWithAlpha.toString());
		}else if (expression instanceof IdentifierExpression) {
			IdentifierExpression identifier = (IdentifierExpression)expression; 
			values.add(DeclarationValueFactory.getDeclarationValue(property, identifier.getValue(), ValueType.IDENT));
		} else if (expression instanceof ColorExpression) {
			ColorExpression colorExpression = (ColorExpression) expression;
			values.add(DeclarationValueFactory.getDeclarationValue(property, colorExpression.getValue(), ValueType.COLOR)); 
		} else if (expression instanceof FunctionExpression) {

			FunctionExpression function =  (FunctionExpression)expression;
			String functionName = function.getName();
			if ("rgb".equals(functionName) || "hsl".equals(functionName) ||
					"rgba".equals(functionName) || "hsla".equals(functionName)) {

				String functionString = getFunctionStringFromLessFunctionExpression(function);

				values.add(DeclarationValueFactory.getDeclarationValue(property, functionString, ValueType.COLOR));

			} else if(functionName.equals("url")) {
				if (function.getParameter().getChilds().get(1) instanceof CssString) {
					String url = "url('" + ((CssString)function.getParameter().getChilds().get(1)).getValue() + "')"; 
					values.add(DeclarationValueFactory.getDeclarationValue(property, url, ValueType.URL));
				} else {
					throw new RuntimeException("What is that?" + expression);
				}
			} else {

				String functionString = getFunctionStringFromLessFunctionExpression(function);
				values.add(DeclarationValueFactory.getDeclarationValue(property, functionString, ValueType.FUNCTION));
			}
		} else if (expression instanceof CssString) {
			values.add(DeclarationValueFactory.getDeclarationValue(property, "'" + ((CssString)expression).getValue() + "'", ValueType.STRING));
		} else if (expression instanceof BinaryExpression) {
			BinaryExpression binary = (BinaryExpression)expression;
			values.addAll(getListOfDeclarationValuesFromLessExpression(property, binary.getLeft()));
			values.add(DeclarationValueFactory.getDeclarationValue(property, binary.getOperator().toString(), ValueType.OPERATOR));
			values.addAll(getListOfDeclarationValuesFromLessExpression(property, binary.getRight()));
		}
		
		return values;
	}

	protected String getFunctionStringFromLessFunctionExpression(
			FunctionExpression function) {
		String functionString = function.getName() + "(" ;
		// First child is the operator
		for (int i = 1; i < function.getParameter().getChilds().size(); i++) {
			functionString += function.getParameter().getChilds().get(i);
			if (i < function.getParameter().getChilds().size() -1) {
				functionString += ", ";
			}
		}
		functionString += ")";
		return functionString;
	}

	/**
	 * Converts a Less4j Selector which only has one child to a BaseSelector.
	 * A Less4j Selector might be a combinator or a simple selector. 
	 * @param lessSelector
	 * @return
	 */
	private BaseSelector getBaseSelectorFromLessSelector(com.github.sommeri.less4j.core.ast.Selector lessSelector) {
		
		BaseSelector toReturn = null;
		
		// In the Less selector, every part is a simple selector 
		if (lessSelector.getParts().size() == 1) { // simple selector, not a combinator
			
			toReturn = getSimpleSelectorFromLessSelectorPart(lessSelector.getParts().get(0));
			
		} else { // combinator
			
			toReturn = getCombinatorFromLessSelectorParts(lessSelector.getParts());
			
		}
		
		return toReturn;
	}

	private BaseSelector getCombinatorFromLessSelectorParts(List<SelectorPart> parts) {

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
			
			BaseSelector leftHandSelector = getCombinatorFromLessSelectorParts(partsCopy);
			
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
					adaptedPseudoClass = new NegationPseudoClass(getBaseSelectorFromLessSelector(parameter));
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
		switch (parameter.getForm()) {
		case EVEN:
			toReturn = "even";
			break;
		case ODD:
			toReturn = "odd";
		case STANDARD:
			if (parameter.getRepeater() != null)
				toReturn = parameter.getRepeater().toString();
			
			if (parameter.getMod() != null)
				toReturn += parameter.getMod().toString();
			break;
		}
		return toReturn;
	}
}
