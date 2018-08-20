package ca.concordia.cssanalyser.parser.less;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.core.ast.*;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.FileSource;
import com.github.sommeri.less4j.LessSource.URLSource;
import com.github.sommeri.less4j.core.ast.ColorExpression.ColorWithAlphaExpression;
import com.github.sommeri.less4j.core.ast.ListExpressionOperator.Operator;

import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.cssmodel.LocationInfo;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.DeclarationFactory;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValueFactory;
import ca.concordia.cssanalyser.cssmodel.declaration.value.ValueType;
import ca.concordia.cssanalyser.cssmodel.media.MediaFeatureExpression;
import ca.concordia.cssanalyser.cssmodel.media.MediaQuery;
import ca.concordia.cssanalyser.cssmodel.media.MediaQuery.MediaQueryPrefix;
import ca.concordia.cssanalyser.cssmodel.media.MediaQueryList;
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
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessPreprocessorNodeFinder;
import ca.concordia.cssanalyser.parser.ParseException;

/**
 * Adapts a Less StyleSheet object to a CSSAnalyser StyleSheet object 
 * @author Davood Mazinanian
 *
 */
public class LessStyleSheetAdapter {
	
	private static final String IMPORTANT = "!important";

	private static Logger LOGGER = FileLogger.getLogger(LessStyleSheetAdapter.class);
	
	private final ASTCssNode lessStyleSheet;
	
	public LessStyleSheetAdapter(ASTCssNode lessStyleSheet) {
		this.lessStyleSheet = lessStyleSheet;
	}

	private void adapt(StyleSheet ourStyleSheet) {
		
 		List<? extends ASTCssNode> nodes = lessStyleSheet.getChilds();
		addSelectorsToStyleSheetFromLessASTNodes(ourStyleSheet, nodes);
		
	}

	private void addSelectorsToStyleSheetFromLessASTNodes(StyleSheet styleSheet, List<? extends ASTCssNode> nodes) {
		addSelectorsToStyleSheetFromLessASTNodes(styleSheet, nodes, null);
	}
	
	private void addSelectorsToStyleSheetFromLessASTNodes(StyleSheet styleSheet, List<? extends ASTCssNode> nodes, MediaQueryList mediaQueries) {
		
		for (ASTCssNode node : nodes) {
 			
 			if (node instanceof RuleSet) {
 				
 				RuleSet ruleSetNode = (RuleSet)node;
 				Selector selector = getSelectorFromLessRuleSet(ruleSetNode);
 				if (mediaQueries != null)
 					selector.addMediaQueryList(mediaQueries);
 				styleSheet.addSelector(selector);
 				
 			} else if (node instanceof com.github.sommeri.less4j.core.ast.Media) {
 				
 				com.github.sommeri.less4j.core.ast.Media lessMedia = (com.github.sommeri.less4j.core.ast.Media)node;
 				MediaQueryList mediaQueryList = getMediaQueryListFromLessMedia(lessMedia);
 				addSelectorsToStyleSheetFromLessASTNodes(styleSheet, lessMedia.getBody().getMembers(), mediaQueryList);
 				
 			}
 	
		}
		
	}
		
	private MediaQueryList getMediaQueryListFromLessMedia(com.github.sommeri.less4j.core.ast.Media lessMedia) {
		
		// x and y, z and t is a list of media queries containing two media queries
		MediaQueryList mediaQueryList = new MediaQueryList();
		
		for (com.github.sommeri.less4j.core.ast.MediaQuery lessMediaQuery : lessMedia.getMediums()) {
			MediaQueryPrefix prefix = null;
			String mediumType = "";
			if (lessMediaQuery.getMedium() != null) {
				if (lessMediaQuery.getMedium().getModifier() != null) {
					switch (lessMediaQuery.getMedium().getModifier().getModifier()) {
					case NOT:
						prefix = MediaQueryPrefix.NOT;
						break;
					case ONLY:
						prefix = MediaQueryPrefix.ONLY;
						break;
					case NONE:
					default:
						prefix = null;
						break;
					}
				}
				
				if (lessMediaQuery.getMedium().getMediumType() != null)
					mediumType = lessMediaQuery.getMedium().getMediumType().getName();
			}
			MediaQuery query = new MediaQuery(prefix, mediumType);
			for (MediaExpression lessMediaExpression : lessMediaQuery.getExpressions()) {
				try {
					String feature = "";
					String expression = "";
					if (lessMediaExpression instanceof FixedMediaExpression) {
						FixedMediaExpression fixedMediaExpression = (FixedMediaExpression)lessMediaExpression;
						if (fixedMediaExpression.getExpression() != null) {
							// Lets re-use a method that we already have, in an ugly manner
							List<DeclarationValue> values = getListOfDeclarationValuesFromLessExpression("fake",  fixedMediaExpression.getExpression());
							for (DeclarationValue value : values)
								expression += value;
						}
						feature = fixedMediaExpression.getFeature().getFeature();
					} else if (lessMediaExpression instanceof InterpolatedMediaExpression) {
						throw new RuntimeException("What is " + lessMediaExpression);
					}
					MediaFeatureExpression featureExpression = new MediaFeatureExpression(feature, expression);
					featureExpression.setLocationInfo(LessPreprocessorNodeFinder.getLocationInfoForLessASTCssNode(lessMediaExpression));
					query.addMediaFeatureExpression(featureExpression);
				} catch (ParseException ex) {
					LOGGER.warn(String.format("Ignored media expression %s", lessMediaExpression.toString()));
				}
			}
			query.setLocationInfo(LessPreprocessorNodeFinder.getLocationInfoForLessASTCssNode(lessMediaQuery));
			mediaQueryList.addMediaQuery(query);
		}
		
		mediaQueryList.setLocationInfo(LessPreprocessorNodeFinder.getLocationInfoForLessASTCssNode(lessMedia));
		
		return mediaQueryList;
	}
	
	public Selector getSelectorFromLessRuleSet(RuleSet ruleSetNode) {
		Selector selector = null;
		
		if (ruleSetNode.getSelectors().size() == 1) { // One selector, this is a base selector
			
			selector = getBaseSelectorFromLessSelector(ruleSetNode.getSelectors().get(0));
			
		} else { // More than 1 selector, this is a grouping selector
			GroupingSelector grouping = new GroupingSelector();

			for (com.github.sommeri.less4j.core.ast.Selector lessSelector : ruleSetNode.getSelectors()) {
				BaseSelector baseSelectorFromLessSelector = getBaseSelectorFromLessSelector(lessSelector);
				grouping.add(baseSelectorFromLessSelector);
				baseSelectorFromLessSelector.setParentGroupSelector(grouping);
			}
			
			selector = grouping;
		}
		
		selector.setLocationInfo(LessPreprocessorNodeFinder.getLocationInfoForLessASTCssNode(ruleSetNode));

		// Handle declarations
		addDeclarationsToSelectorFromLessRuleSetNode(ruleSetNode, selector);

		return selector;
	}
	
	private void addDeclarationsToSelectorFromLessRuleSetNode(RuleSet ruleSetNode, Selector selector) {
		if (ruleSetNode.getBody() != null) {
			for (ASTCssNode declarationNode : ruleSetNode.getBody().getDeclarations()) {
				Declaration declaration = getDeclarationFromLessDeclaration(declarationNode);
				if (declaration != null)
					selector.addDeclaration(declaration);
			}
		}
	}

	public Declaration getDeclarationFromLessDeclaration(ASTCssNode declarationNode) {
		Declaration declaration = null;		
		if (declarationNode instanceof com.github.sommeri.less4j.core.ast.Declaration) {
			
			try {
				com.github.sommeri.less4j.core.ast.Declaration lessDeclaration = (com.github.sommeri.less4j.core.ast.Declaration)declarationNode;  
				
				String property = lessDeclaration.getNameAsString();
				List<DeclarationValue> values;

				if (lessDeclaration.getExpression() != null) { 
					values = getListOfDeclarationValuesFromLessExpression(property, lessDeclaration.getExpression());
				} else { // If a declaration does not have a value, happened in some cases
					values  = new ArrayList<>();
					values.add(new DeclarationValue("", ValueType.OTHER));
				}
				boolean isImportant = false;
				DeclarationValue declarationValue = values.get(values.size() - 1);
				if (declarationValue.getValue().endsWith(IMPORTANT)) {
					isImportant = true;
					if (declarationValue.getValue().equals(IMPORTANT)) {
						values.remove(values.size() - 1);
					} else {
						String value = declarationValue.getValue().replace("!important", "");
						values.set(values.size() - 1, DeclarationValueFactory.getDeclarationValue(property, value, declarationValue.getType()));
					}
				}
				if (values.size() == 0) {
					LOGGER.warn(String.format("No CSS values could be found for property %s at line %s, column %s", property, 
							lessDeclaration.getSourceLine(), lessDeclaration.getSourceColumn()));
				} else {
					declaration = DeclarationFactory.getDeclaration(
							property, values, null, isImportant, true, LessPreprocessorNodeFinder.getLocationInfoForLessASTCssNode(declarationNode));
				}

			} catch (Exception ex) {
				LOGGER.warn("Could not read " + declarationNode + "; " + ex);
			}
				
		} else {
			throw new RuntimeException("What is that?" + declarationNode);
		}
		return declaration;
	}

	private List<DeclarationValue> getListOfDeclarationValuesFromLessExpression(String property, Expression expression) throws ParseException {

		List<DeclarationValue> values = new ArrayList<>();

		if (expression instanceof ListExpression) {
			ListExpression listExpression = (ListExpression) expression;
			for (Iterator<Expression> iterator = listExpression.getExpressions().iterator(); iterator.hasNext(); ) {

				Expression expr = iterator.next();
				List<DeclarationValue> vals = getListOfDeclarationValuesFromLessExpression(property, expr);
				values.addAll(vals);
				if (listExpression.getOperator() != null) {

					if (listExpression.getOperator().getOperator() == Operator.COMMA) {
						if (iterator.hasNext()) {
							DeclarationValue value = DeclarationValueFactory.getDeclarationValue(property, ",", ValueType.SEPARATOR);
							value.setLocationInfo(LessPreprocessorNodeFinder.getLocationInfoForLessASTCssNode(listExpression.getOperator()));
							values.add(value);
						}
					} else if (listExpression.getOperator().getOperator() == Operator.EMPTY_OPERATOR) {
						// Do nothing
					} else {
						throw new RuntimeException("Operator = " + listExpression.getOperator());
					}

				} else {
					throw new RuntimeException("Operator = " + listExpression.getOperator());
				}
			}

		} else if (expression instanceof BinaryExpression) {

			BinaryExpression binary = (BinaryExpression) expression;

			values.addAll(getListOfDeclarationValuesFromLessExpression(property, binary.getLeft()));

			// Operator
			DeclarationValue operator = DeclarationValueFactory.getDeclarationValue(property, binary.getOperator().toString(), ValueType.OPERATOR);
			operator.setLocationInfo(LessPreprocessorNodeFinder.getLocationInfoForLessASTCssNode(binary.getOperator()));
			values.add(operator);

			values.addAll(getListOfDeclarationValuesFromLessExpression(property, binary.getRight()));

		} else if (expression instanceof NamedExpression) {

			NamedExpression namedExpression = (NamedExpression) expression;
			values.add(DeclarationValueFactory.getDeclarationValue(property, namedExpression.getName(), ValueType.IDENT));
			values.add(DeclarationValueFactory.getDeclarationValue(property, "=", ValueType.OPERATOR));

			values.addAll(getListOfDeclarationValuesFromLessExpression(property, namedExpression.getExpression()));

		} else if (expression instanceof ParenthesesExpression) {

			ParenthesesExpression parenthesesExpression = (ParenthesesExpression) expression;
			DeclarationValue leftParenthesis = DeclarationValueFactory.getDeclarationValue(property, "(", ValueType.SEPARATOR);
			leftParenthesis.setLocationInfo(LessPreprocessorNodeFinder.getLocationInfoForLessASTCssNode(parenthesesExpression));
			values.add(leftParenthesis);
			values.addAll(getListOfDeclarationValuesFromLessExpression(property, parenthesesExpression.getEnclosedExpression()));
			DeclarationValue rightParenthesis = DeclarationValueFactory.getDeclarationValue(property, ")", ValueType.SEPARATOR);
			rightParenthesis.setLocationInfo(LessPreprocessorNodeFinder.getLocationInfoForLessASTCssNode(parenthesesExpression));
			values.add(rightParenthesis);

		} else if (expression instanceof SignedExpression) {

			SignedExpression signedExpression = (SignedExpression) expression;
			DeclarationValue sign = DeclarationValueFactory.getDeclarationValue(property, signedExpression.getSign().toSymbol(), ValueType.OPERATOR);
			sign.setLocationInfo(LessPreprocessorNodeFinder.getLocationInfoForLessASTCssNode(signedExpression));
			values.addAll(getListOfDeclarationValuesFromLessExpression(property, signedExpression.getExpression()));

		} else {
			
			values.add(getSingleValueFromLessValueExpression(property, expression));
			
		}
		
		return values;
	}

	private DeclarationValue getSingleValueFromLessValueExpression(String property, Expression expression) throws ParseException {
		
		DeclarationValue value = null; 
		
		if (expression instanceof NumberExpression) {

			NumberExpression numberExpression = (NumberExpression)expression;
			value = getDeclarationValueFromLessNumberExpression(property, numberExpression);

		} else if (expression instanceof NamedColorExpression) {

			NamedColorExpression namedExpression = (NamedColorExpression)expression;
			value = DeclarationValueFactory.getDeclarationValue(property, namedExpression.getColorName(), ValueType.COLOR);

		} else if (expression instanceof ColorWithAlphaExpression) {
			
			if (expression instanceof NamedColorWithAlphaExpression) {
				
				NamedColorWithAlphaExpression namedColorWithAlphaExpression = (NamedColorWithAlphaExpression) expression;
				value = DeclarationValueFactory.getDeclarationValue(property, namedColorWithAlphaExpression.getColorName(), ValueType.IDENT);
				
			} else {
				
				ColorWithAlphaExpression colorWithAlpha = (ColorWithAlphaExpression)expression;
				value = DeclarationValueFactory.getDeclarationValue(property, colorWithAlpha.toString(), ValueType.COLOR);
				
			}

		} else if (expression instanceof IdentifierExpression) {

			IdentifierExpression identifier = (IdentifierExpression)expression; 
			String valueString = identifier.getValue();
			if (valueString == null)
				valueString = "";
			value = DeclarationValueFactory.getDeclarationValue(property, valueString, ValueType.IDENT);

		} else if (expression instanceof ColorExpression) {

			ColorExpression colorExpression = (ColorExpression) expression;
			value = DeclarationValueFactory.getDeclarationValue(property, colorExpression.getValue(), ValueType.COLOR);

		} else if (expression instanceof FunctionExpression) {

			FunctionExpression function =  (FunctionExpression)expression;
			String functionName = function.getName();
			if ("rgb".equals(functionName) || "hsl".equals(functionName) || "rgba".equals(functionName) || "hsla".equals(functionName)) {

				String functionString = getFunctionStringFromLessFunctionExpression(property, function);
				value = DeclarationValueFactory.getDeclarationValue(property, functionString, ValueType.COLOR);

			} else if (functionName.equals("url")) {

				if (function.getParameter().getChilds().get(1) instanceof CssString) {
					String url = "url('" + ((CssString)function.getParameter().getChilds().get(1)).getValue() + "')"; 
					value = DeclarationValueFactory.getDeclarationValue(property, url, ValueType.URL);
				} else {
					throw new RuntimeException("What is that?" + expression);
				}
			} else {
				
				String functionString = getFunctionStringFromLessFunctionExpression(property, function);
				value = DeclarationValueFactory.getDeclarationValue(property, functionString, ValueType.FUNCTION);

			}
		} else if (expression instanceof CssString) {
			
			value = DeclarationValueFactory.getDeclarationValue(property, "'" + ((CssString)expression).getValue() + "'", ValueType.STRING);
			
		} else if (expression instanceof AnonymousExpression) {
			AnonymousExpression anonymousExpression = (AnonymousExpression) expression; 
			value = DeclarationValueFactory.getDeclarationValue(property, anonymousExpression.getValue(), ValueType.OTHER);
		} else if (expression instanceof EscapedValue) {
			EscapedValue escapedValue = (EscapedValue) expression;
			value = DeclarationValueFactory.getDeclarationValue(property, escapedValue.getValue(), ValueType.OTHER);
		} else {
			throw new RuntimeException("What is that?" + expression);
		}
		
		value.setLocationInfo(LessPreprocessorNodeFinder.getLocationInfoForLessASTCssNode(expression));
		
		return value;
	}

	private DeclarationValue getDeclarationValueFromLessNumberExpression(String property, NumberExpression numberExpression) throws ParseException {
		
		DeclarationValue value = null;
		
		String originalString = numberExpression.getOriginalString();
		switch(numberExpression.getDimension()) {
			case ANGLE:
				value = DeclarationValueFactory.getDeclarationValue(property, originalString, ValueType.ANGLE);
			case EMS:
			case EXS:
			case LENGTH:
				value = DeclarationValueFactory.getDeclarationValue(property, originalString, ValueType.LENGTH);
				break;
			case FREQ:
				value = DeclarationValueFactory.getDeclarationValue(property, originalString, ValueType.FREQUENCY);
				break;
			case NUMBER:
				if (originalString.indexOf(".") > -1)
					value = DeclarationValueFactory.getDeclarationValue(property, DeclarationValueFactory.formatDouble(numberExpression.getValueAsDouble()), ValueType.REAL);
				else
					value = DeclarationValueFactory.getDeclarationValue(property, DeclarationValueFactory.formatDouble(numberExpression.getValueAsDouble()), ValueType.INTEGER);
				break;
			case PERCENTAGE:
				value = DeclarationValueFactory.getDeclarationValue(property, originalString, ValueType.PERCENTAGE);
				break;
			case REPEATER:
				throw new RuntimeException("What is " + property + ":" + originalString);
			case TIME:
				value = DeclarationValueFactory.getDeclarationValue(property, originalString, ValueType.TIME);
				break;
			case UNKNOWN:
			String suffix = numberExpression.getSuffix().toLowerCase();
			if ("turn".equals(suffix)) {
					value = DeclarationValueFactory.getDeclarationValue(property, originalString, ValueType.ANGLE);
				} else if ("rem".equals(suffix) || "vw".equals(suffix) || "vh".equals(suffix) || "vmin".equals(suffix)) {
					value = DeclarationValueFactory.getDeclarationValue(property, originalString, ValueType.PERCENTAGE);
				} else {
					value = DeclarationValueFactory.getDeclarationValue(property, originalString, ValueType.OTHER);
//					throw new ParseException("What is " + property + ":" + originalString);
				}
			default:
				break;
		}
		
		value.setLocationInfo(LessPreprocessorNodeFinder.getLocationInfoForLessASTCssNode(numberExpression));
		return value;
	}

	private String getFunctionStringFromLessFunctionExpression(String property, FunctionExpression function) throws ParseException {		
		String functionName = function.getName();
		StringBuilder functionString = new StringBuilder(functionName);
		functionString.append("(");
		Expression parameter = function.getParameter();
		List<DeclarationValue> values = getListOfDeclarationValuesFromLessExpression(property, parameter);		
		for (Iterator<DeclarationValue> iterator = values.iterator(); iterator.hasNext(); ) {
			DeclarationValue value = iterator.next();
			if ("".equals(value.getValue()))
				throw new ParseException(String.format("Could not parse one of the parameters for function %s at <%s:%s>", functionName, function.getSourceLine(), function.getSourceColumn()));

			if (value.getType() != ValueType.SEPARATOR && !functionString.toString().endsWith("(")) {
				functionString.append(" ");
			}
			functionString.append(value.getValue());
		}
		functionString.append(")");
		return functionString.toString();
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
		
		partsCopy.remove(partsCopy.size() - 1);
		
		if (partsCopy.size() >= 1) {
			if (partsCopy.get(partsCopy.size() - 1) instanceof NestedSelectorAppender) {
				rightHandSelector.setSelectedElementName("&");
				partsCopy.remove(partsCopy.size() - 1);
			}
		}
		
		BaseSelector baseSelectorToReturn = rightHandSelector;
		
		if (partsCopy.size() != 0) {
			
			BaseSelector leftHandSelector = getCombinatorFromLessSelectorParts(partsCopy);
			
			if (lastPart.getLeadingCombinator() != null) {
				switch (lastPart.getLeadingCombinator().getCombinatorType()) {
				//ADJACENT_SIBLING("+"), CHILD(">"), DESCENDANT("' '"), GENERAL_SIBLING("~"), HAT("^"), CAT("^^");
				case ADJACENT_SIBLING:
					baseSelectorToReturn = new AdjacentSiblingSelector(leftHandSelector, rightHandSelector);
					break;
				case CHILD:
					baseSelectorToReturn = new ChildSelector(leftHandSelector, rightHandSelector);
					break;
				case DESCENDANT:
					baseSelectorToReturn = new DescendantSelector(leftHandSelector, rightHandSelector);
					break;
				case GENERAL_SIBLING:
					baseSelectorToReturn = new SiblingSelector(leftHandSelector, rightHandSelector);
					break;
				case NAMED:
				case HAT:
				case CAT:
					// Not supported
					return null;
				default:
					break;
				}
			} else {
				baseSelectorToReturn = new AdjacentSiblingSelector(leftHandSelector, rightHandSelector);
			}
			
			int lineNumber = leftHandSelector.getSelectorNameLocationInfo().getLineNumber();
			int colNumber = leftHandSelector.getSelectorNameLocationInfo().getColumnNumber();
			int offset = leftHandSelector.getSelectorNameLocationInfo().getOffset();
			int length = rightHandSelector.getSelectorNameLocationInfo().getOffset() + rightHandSelector.getSelectorNameLocationInfo().getLength() - offset;
			
			LocationInfo selectorNameLocationInfo = new LocationInfo(lineNumber, colNumber, offset, length);
			baseSelectorToReturn.setSelectorNameLocationInfo(selectorNameLocationInfo);
		} 
		
		return baseSelectorToReturn;
		
	}

	private SimpleSelector getSimpleSelectorFromLessSelectorPart(SelectorPart selectorPart) {
		
		SimpleSelector simpleSelector = new SimpleSelector();
		
		if (selectorPart instanceof NestedSelectorAppender) {
			simpleSelector.setSelectedElementName("&");
		} else {

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
		}
			
		simpleSelector.setSelectorNameLocationInfo(LessPreprocessorNodeFinder.getLocationInfoForLessASTCssNode(selectorPart));
		
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

	public StyleSheet getAdaptedStyleSheet() {
		StyleSheet ourStyleSheet = new StyleSheet();
		LessSource source = lessStyleSheet.getSource();
		if ((source instanceof FileSource || source instanceof URLSource) && source.getURI() != null) {
			ourStyleSheet.setPath(source.getURI().toString());
		} else if (source instanceof ModifiedLessFileSource) {
			ourStyleSheet.setPath(((ModifiedLessFileSource) source).getInputFile().getAbsolutePath());
		}
		adapt(ourStyleSheet);
		return ourStyleSheet;
	}
	
	public ASTCssNode getLessStyleSheet() {
		return lessStyleSheet;
	}
	
	
	
}
