package ca.concordia.cssanalyser.migration.topreprocessors.less;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorCodePrinter;

import com.github.sommeri.less4j.core.NotACssException;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.AnonymousExpression;
import com.github.sommeri.less4j.core.ast.ArgumentDeclaration;
import com.github.sommeri.less4j.core.ast.BinaryExpression;
import com.github.sommeri.less4j.core.ast.BinaryExpressionOperator;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.CharsetDeclaration;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.Comment;
import com.github.sommeri.less4j.core.ast.CssClass;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.DetachedRuleset;
import com.github.sommeri.less4j.core.ast.Document;
import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.EmbeddedScript;
import com.github.sommeri.less4j.core.ast.EmptyExpression;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Extend;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FaultyNode;
import com.github.sommeri.less4j.core.ast.FixedMediaExpression;
import com.github.sommeri.less4j.core.ast.FixedNamePart;
import com.github.sommeri.less4j.core.ast.FontFace;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.IdSelector;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.Import;
import com.github.sommeri.less4j.core.ast.InlineContent;
import com.github.sommeri.less4j.core.ast.InterpolableName;
import com.github.sommeri.less4j.core.ast.InterpolableNamePart;
import com.github.sommeri.less4j.core.ast.InterpolatedMediaExpression;
import com.github.sommeri.less4j.core.ast.Keyframes;
import com.github.sommeri.less4j.core.ast.KeyframesName;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.ast.ListExpressionOperator;
import com.github.sommeri.less4j.core.ast.Media;
import com.github.sommeri.less4j.core.ast.MediaExpression;
import com.github.sommeri.less4j.core.ast.MediaExpressionFeature;
import com.github.sommeri.less4j.core.ast.MediaQuery;
import com.github.sommeri.less4j.core.ast.Medium;
import com.github.sommeri.less4j.core.ast.MediumModifier;
import com.github.sommeri.less4j.core.ast.NestedSelectorAppender;
import com.github.sommeri.less4j.core.ast.SignedExpression;
import com.github.sommeri.less4j.core.ast.MediumModifier.Modifier;
import com.github.sommeri.less4j.core.ast.MediumType;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.Name;
import com.github.sommeri.less4j.core.ast.NamedColorExpression;
import com.github.sommeri.less4j.core.ast.NamedExpression;
import com.github.sommeri.less4j.core.ast.Nth;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.Page;
import com.github.sommeri.less4j.core.ast.PageMarginBox;
import com.github.sommeri.less4j.core.ast.ParenthesesExpression;
import com.github.sommeri.less4j.core.ast.PseudoClass;
import com.github.sommeri.less4j.core.ast.PseudoElement;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SelectorAttribute;
import com.github.sommeri.less4j.core.ast.SelectorCombinator;
import com.github.sommeri.less4j.core.ast.SelectorOperator;
import com.github.sommeri.less4j.core.ast.SelectorPart;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.ast.Supports;
import com.github.sommeri.less4j.core.ast.SupportsCondition;
import com.github.sommeri.less4j.core.ast.SupportsConditionInParentheses;
import com.github.sommeri.less4j.core.ast.SupportsConditionNegation;
import com.github.sommeri.less4j.core.ast.SupportsLogicalCondition;
import com.github.sommeri.less4j.core.ast.SupportsLogicalOperator;
import com.github.sommeri.less4j.core.ast.SupportsQuery;
import com.github.sommeri.less4j.core.ast.SyntaxOnlyElement;
import com.github.sommeri.less4j.core.ast.UnicodeRangeExpression;
import com.github.sommeri.less4j.core.ast.UnknownAtRule;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.ast.VariableDeclaration;
import com.github.sommeri.less4j.core.ast.VariableNamePart;
import com.github.sommeri.less4j.core.ast.Viewport;
import com.github.sommeri.less4j.core.output.ExtendedStringBuilder;
import com.github.sommeri.less4j.utils.LastOfKindSet;
import com.github.sommeri.less4j.utils.PrintUtils;

public class LessPrinter implements PreprocessorCodePrinter<StyleSheet> {

	public static final String ERROR = "!#error#!";
	protected ExtendedStringBuilder builder; 

	@Override
	public String getString(StyleSheet styleSheet) {
		builder = new ExtendedStringBuilder();
		append(styleSheet);
		return builder.toString();
	}
	
	public String getStringForNode(ASTCssNode node) {
		builder = new ExtendedStringBuilder();
		append(node);
		return builder.toString();
	}

	public LessPrinter() {
		builder = new ExtendedStringBuilder();
	}

	private LessPrinter(LessPrinter toCopy) {
		builder = new ExtendedStringBuilder(toCopy.builder);
	}

	/**
	 * returns whether the output changed as a result of the operation
	 * 
	 * @param node
	 * @return
	 */
	public boolean append(ASTCssNode node) {
		// opening comments should not be docked directly in front of following
		// thing
		if (node == null || node.isSilent())
			return false;

		//appendComments(node.getOpeningComments(), true);
		boolean result = switchOnType(node);
		//appendComments(node.getTrailingComments(), false);
		return result;
	}

	public boolean switchOnType(ASTCssNode node) {
		switch (node.getType()) {
		case RULE_SET:
			return appendRuleset((RuleSet) node);

		case CSS_CLASS:
			return appendCssClass((CssClass) node);

		case PSEUDO_CLASS:
			return appendPseudoClass((PseudoClass) node);

		case PSEUDO_ELEMENT:
			return appendPseudoElement((PseudoElement) node);

		case NTH:
			return appendNth((Nth) node); // TODOsm: source map

		case SELECTOR:
			return appendSelector((Selector) node); 

		case SIMPLE_SELECTOR:
			return appendSimpleSelector((SimpleSelector) node);

		case SELECTOR_OPERATOR:
			return appendSelectorOperator((SelectorOperator) node); // TODOsm: source map

		case SELECTOR_COMBINATOR:
			return appendSelectorCombinator((SelectorCombinator) node); // TODOsm: source map

		case SELECTOR_ATTRIBUTE:
			return appendSelectorAttribute((SelectorAttribute) node); // TODOsm: source map

		case ID_SELECTOR:
			return appendIdSelector((IdSelector) node);

		case CHARSET_DECLARATION:
			return appendCharsetDeclaration((CharsetDeclaration) node);

		case FONT_FACE:
			return appendFontFace((FontFace) node);

		case NAMED_EXPRESSION:
			return appendNamedExpression((NamedExpression) node); // TODOsm: source map

		case BINARY_EXPRESSION:
			return appendComposedExpression((BinaryExpression) node); 

		case BINARY_EXPRESSION_OPERATOR:
			return appendBinaryExpressionOperator((BinaryExpressionOperator) node); // TODOsm: source map

		case LIST_EXPRESSION:
			return appendListExpression((ListExpression) node); 

		case LIST_EXPRESSION_OPERATOR:
			return appendListExpressionOperator((ListExpressionOperator) node); // TODOsm: source map

		case STRING_EXPRESSION:
			return appendCssString((CssString) node); // TODOsm: source map

		case EMPTY_EXPRESSION:
			return appendEmptyExpression((EmptyExpression) node); 

		case NUMBER:
			return appendNumberExpression((NumberExpression) node); // TODOsm: source map

		case IDENTIFIER_EXPRESSION:
			return appendIdentifierExpression((IdentifierExpression) node); // TODOsm: source map

		case UNICODE_RANGE_EXPRESSION:
			return appendUnicodeRangeExpression((UnicodeRangeExpression) node); // TODOsm: source map

		case COLOR_EXPRESSION:
			return appendColorExpression((ColorExpression) node); // TODOsm: source map

		case FUNCTION:
			return appendFunctionExpression((FunctionExpression) node); // TODOsm: source map

		case DECLARATION:
			return appendDeclaration((Declaration) node); // TODOsm: source map

		case MEDIA:
			return appendMedia((Media) node); 

		case MEDIA_QUERY:
			return appendMediaQuery((MediaQuery) node); // TODOsm: source map

		case MEDIUM:
			return appendMedium((Medium) node); // TODOsm: source map

		case MEDIUM_MODIFIER:
			return appendMediumModifier((MediumModifier) node); // TODOsm: source map

		case MEDIUM_TYPE:
			return appendMediumType((MediumType) node); // TODOsm: source map

		case FIXED_MEDIA_EXPRESSION:
			return appendMediaExpression((FixedMediaExpression) node); // TODOsm: source map

		case INTERPOLATED_MEDIA_EXPRESSION:
			return appendInterpolatedMediaExpression((InterpolatedMediaExpression) node); // TODOsm: source map

		case MEDIUM_EX_FEATURE:
			return appendMediaExpressionFeature((MediaExpressionFeature) node); // TODOsm: source map

		case STYLE_SHEET:
			return appendStyleSheet((StyleSheet) node); 

		case FAULTY_EXPRESSION:
			return appendFaultyExpression((FaultyExpression) node); // TODOsm: source map

		case FAULTY_NODE:
			return appendFaultyNode((FaultyNode) node); // TODOsm: source map

		case ESCAPED_VALUE:
			return appendEscapedValue((EscapedValue) node); // TODOsm: source map

		case EMBEDDED_SCRIPT:
			return appendEmbeddedScript((EmbeddedScript) node); // TODOsm: source map

		case KEYFRAMES:
			return appendKeyframes((Keyframes) node); // TODOsm: source map

		case KEYFRAMES_NAME:
			return appendKeyframesName((KeyframesName) node); // TODOsm: source map

		case UNKNOWN_AT_RULE:
			return appendUnknownAtRule((UnknownAtRule) node); // TODOsm: source map

		case DOCUMENT:
			return appendDocument((Document) node); // TODOsm: source map

		case VIEWPORT:
			return appendViewport((Viewport) node); // TODOsm: source map

		case GENERAL_BODY:
			return appendBodyOptimizeDuplicates((GeneralBody) node); 

		case PAGE:
			return appendPage((Page) node); // TODOsm: source map

		case PAGE_MARGIN_BOX:
			return appendPageMarginBox((PageMarginBox) node); // TODOsm: source map

		case NAME:
			return appendName((Name) node); // TODOsm: source map

		case IMPORT:
			return appendImport((Import) node); // TODOsm: source map

		case ANONYMOUS:
			return appendAnonymous((AnonymousExpression) node); // TODOsm: source map

		case SYNTAX_ONLY_ELEMENT:
			return appendSyntaxOnlyElement((SyntaxOnlyElement) node); // TODOsm: source map

		case SUPPORTS:
			return appendSupports((Supports) node); // TODOsm: source map

		case SUPPORTS_QUERY:
			return appendSupportsQuery((SupportsQuery) node); // TODOsm: source map

		case SUPPORTS_CONDITION_NEGATION:
			return appendSupportsConditionNegation((SupportsConditionNegation) node); // TODOsm: source map

		case SUPPORTS_CONDITION_PARENTHESES:
			return appendSupportsConditionParentheses((SupportsConditionInParentheses) node); // TODOsm: source map

		case SUPPORTS_CONDITION_LOGICAL:
			return appendSupportsConditionLogical((SupportsLogicalCondition) node); // TODOsm: source map

		case SUPPORTS_LOGICAL_OPERATOR:
			return appendSupportsLogicalOperator((SupportsLogicalOperator) node); // TODOsm: source map

		case INLINE_CONTENT:
			return appendInlineContent((InlineContent) node); // TODOsm: source map

		case DETACHED_RULESET:
			return appendDetachedRuleset((DetachedRuleset) node);
			
		case REUSABLE_STRUCTURE:
			return appendReusableStructure((ReusableStructure) node);
			
		case ARGUMENT_DECLARATION:
			return appendArgumentDeclaration((ArgumentDeclaration) node);
			
		case VARIABLE:
			return appendVariable((Variable) node); 
			
		case MIXIN_REFERENCE:
			return appendMixinReference((MixinReference) node);
			
		case NESTED_SELECTOR_APPENDER:
			return appendNestedSelesctorAppender((NestedSelectorAppender)node);
			
		case VARIABLE_DECLARATION:
			return appendVariableDeclaration((VariableDeclaration)node);

		case PARENTHESES_EXPRESSION:
			return appendParentheses((ParenthesesExpression)node);
			
		case EXTEND:
			return appendExtend((Extend) node);
			
		case SIGNED_EXPRESSION:
			return appendSignedExpression((SignedExpression) node);
			
		case INTERPOLABLE_NAME:
			return appendInterpolableName((InterpolableName) node);
			
		case ESCAPED_SELECTOR:
		case DETACHED_RULESET_REFERENCE:
		case INDIRECT_VARIABLE:
			throw new NotACssException(node);

		default:
			throw new IllegalStateException("Unknown: " + node.getType() + " " + node.getSourceLine() + ":" + node.getSourceColumn());
		}
	}

	private boolean appendInterpolableName(InterpolableName interpolableName) {
		for (InterpolableNamePart interpolableNamePart : interpolableName.getParts()) {
			if (interpolableNamePart instanceof FixedNamePart) {
				FixedNamePart fixedNamePart = (FixedNamePart) interpolableNamePart;
				builder.append(fixedNamePart.getName());
			} else if (interpolableNamePart instanceof VariableNamePart) {
				VariableNamePart variableNamePart = (VariableNamePart) interpolableNamePart;
				builder.append("@{" + variableNamePart.getName().substring(1) + "}");
			}
		}
		return true;
	}

	private boolean appendSignedExpression(SignedExpression node) {
		builder.append(node.getSign().toSymbol());
		append(node.getExpression());
		return true;
	}

	private boolean appendExtend(Extend node) {
		builder.append("&:extend(");
		append(node.getTarget());
		if (node.isAll()) {
			builder.ensureSeparator();
			builder.append("all");
		}
		builder.append(");");
		return true;
	}

	private boolean appendParentheses(ParenthesesExpression node) {
		builder.append("(");
		append(node.getEnclosedExpression());
		builder.append(")");
		return true;
	}

	private boolean appendVariableDeclaration(VariableDeclaration node) {
		append(node.getVariable());
		builder.append(":");
		builder.ensureSeparator();
		append(node.getValue());
		builder.append(";");
		return true;
	}

	private boolean appendNestedSelesctorAppender(NestedSelectorAppender node) {
		builder.append("&");
		return true;
	}

	private boolean appendMixinReference(MixinReference node) {
		builder.appendIgnoreNull(node.getFinalNameAsString());
		builder.append('(');
		for (Iterator<Expression> iterator = node.getPositionalParameters().iterator(); iterator.hasNext();) {
			Expression expression = iterator.next();
			append(expression);
			if (iterator.hasNext())
				builder.append("; ");
		}
		if (node.isImportant())
			builder.ensureSeparator().append("!important");
		builder.append(");");
		
		return true;
	}

	private boolean appendVariable(Variable node) {
		builder.append(node.getName());
		return true;
	}

	private boolean appendArgumentDeclaration(ArgumentDeclaration node) {
		builder.append(node.getVariable());
		if (node.hasDefaultValue()) {
			builder.append(":").ensureSeparator();
			append(node.getValue());
		}
		return true;
	}

	private boolean appendReusableStructure(ReusableStructure node) {
		for (Iterator<ReusableStructureName> reusableStructureNameIterator = node.getNames().iterator(); reusableStructureNameIterator.hasNext();) {
			ReusableStructureName reusableStructureName = reusableStructureNameIterator.next();
			for (Iterator<ElementSubsequent> namePartsIterator = reusableStructureName.getNameParts().iterator(); namePartsIterator.hasNext();) {
				ElementSubsequent elementSubsequent = namePartsIterator.next();
				append(elementSubsequent);
			}
		}
		builder.append("(");
		for (Iterator<ASTCssNode> paramsIterator = node.getParameters().iterator(); paramsIterator.hasNext(); ) {
			ASTCssNode paramNode = paramsIterator.next();
			append(paramNode);
			if (paramsIterator.hasNext())
				builder.append("; ");
		}
		builder.append(")");
		append(node.getBody());
		return true;
	}

	public boolean appendDetachedRuleset(DetachedRuleset node) {
		throw new NotACssException(node);
	}

	public boolean appendCommaSeparated(List<? extends ASTCssNode> values) {
		boolean result = false;
		Iterator<? extends ASTCssNode> names = values.iterator();
		if (names.hasNext()) {
			result |= append(names.next());
		}
		while (names.hasNext()) {
			builder.append(',');
			builder.ensureSeparator();
			append(names.next());
			result = true;
		}

		return result;
	}

	//TODO: what about source maps?
	private boolean appendInlineContent(InlineContent node) {
		builder.appendAsIs(node.getValue());
		return false;
	}

	private boolean appendSyntaxOnlyElement(SyntaxOnlyElement node) {
		builder.append(node.getSymbol());
		return true;
	}

	private boolean appendAnonymous(AnonymousExpression node) {
		builder.append(node.getValue());
		return true;
	}

	private boolean appendImport(Import node) {
		builder.append("@import").ensureSeparator();
		append(node.getUrlExpression());
		appendCommaSeparated(node.getMediums());
		builder.append(';');

		return true;
	}

	private boolean appendName(Name node) {
		builder.append(node.getName());
		return true;
	}

	private boolean appendPage(Page node) {
		builder.append("@page").ensureSeparator();
		if (node.hasName()) {
			append(node.getName());
			if (!node.hasDockedPseudopage())
				builder.ensureSeparator();
		}

		if (node.hasPseudopage()) {
			append(node.getPseudopage());
			builder.ensureSeparator();
		}

		appendBodySortDeclarations(node.getBody());
		return true;
	}

	private boolean appendPageMarginBox(PageMarginBox node) {
		append(node.getName());
		appendBodySortDeclarations(node.getBody());
		return true;
	}

	private boolean appendKeyframesName(KeyframesName node) {
		append(node.getName());
		return true;
	}

	private boolean appendKeyframes(Keyframes node) {
		builder.append(node.getDialect()).ensureSeparator();

		appendCommaSeparated(node.getNames());
		append(node.getBody());
		return true;
	}

	private boolean appendUnknownAtRule(UnknownAtRule node) {
		builder.append(node.getName()).ensureSeparator();

		appendCommaSeparated(node.getNames());

		append(node.getBody());
		append(node.getSemicolon());
		return true;
	}

	private boolean appendSupports(Supports node) {
		builder.append(node.getDialect()).ensureSeparator();
		append(node.getCondition());
		builder.ensureSeparator();
		append(node.getBody());
		return true;
	}

	private boolean appendSupportsConditionNegation(SupportsConditionNegation node) {
		append(node.getNegation());
		builder.ensureSeparator();
		append(node.getCondition());
		return true;
	}

	private boolean appendSupportsConditionParentheses(SupportsConditionInParentheses node) {
		append(node.getOpeningParentheses());
		append(node.getCondition());
		append(node.getClosingParentheses());
		return true;
	}

	private boolean appendSupportsConditionLogical(SupportsLogicalCondition node) {
		Iterator<SupportsLogicalOperator> operators = node.getLogicalOperators().iterator();
		Iterator<SupportsCondition> conditions = node.getConditions().iterator();

		append(conditions.next());

		while (operators.hasNext()) {
			builder.ensureSeparator();
			append(operators.next());
			builder.ensureSeparator();
			append(conditions.next());
		}
		return true;
	}

	private boolean appendSupportsLogicalOperator(SupportsLogicalOperator node) {
		if (node.getOperator() == null) {
			builder.append(ERROR);
		}
		builder.append(node.getOperator().getSymbol());
		return true;
	}

	private boolean appendSupportsQuery(SupportsQuery node) {
		append(node.getOpeningParentheses());
		append(node.getDeclaration());
		append(node.getClosingParentheses());
		return true;
	}

	private boolean appendDocument(Document node) {
		builder.append(node.getDialect()).ensureSeparator();

		appendCommaSeparated(node.getUrlMatchFunctions());
		append(node.getBody());
		return true;
	}

	private boolean appendViewport(Viewport node) {
		builder.append(node.getDialect());
		append(node.getBody());
		return true;
	}

	private boolean appendFaultyNode(FaultyNode node) {
		builder.append(ERROR);
		return true;
	}

	private boolean appendFaultyExpression(FaultyExpression node) {
		builder.append(ERROR);
		return true;
	}

	private boolean appendNth(Nth node) {
		switch (node.getForm()) {
		case EVEN:
			builder.append("even");
			return true;

		case ODD:
			builder.append("odd");
			return true;

		case STANDARD:
			if (node.getRepeater() != null)
				append(node.getRepeater());
			if (node.getMod() != null)
				append(node.getMod());

		}

		return true;
	}

	protected void appendComments(List<Comment> comments, boolean ensureSeparator) {
		if (comments == null || comments.isEmpty() )
			return;

		builder.ensureSeparator();

		for (Comment comment : comments) {
			String text = comment.getComment();
			if (text!=null)
				builder.append(text);
			if (comment.hasNewLine())
				builder.ensureNewLine();
		}

		if (ensureSeparator)
			builder.ensureSeparator();
	}

	public boolean appendFontFace(FontFace node) {
		builder.append("@font-face").ensureSeparator();
		append(node.getBody());

		return true;
	}

	public boolean appendCharsetDeclaration(CharsetDeclaration node) {
		builder.append("@charset").ensureSeparator();
		append(node.getCharset());
		builder.append(';');

		return true;
	}

	public boolean appendIdSelector(IdSelector node) {
		builder.append(node.getFullName());
		return true;
	}

	public boolean appendSelectorAttribute(SelectorAttribute node) {
		builder.append('[');
		builder.append(node.getName());
		append(node.getOperator());
		append(node.getValue());
		builder.append(']');

		return true;
	}

	private boolean appendSelectorOperator(SelectorOperator operator) {
		SelectorOperator.Operator realOperator = operator.getOperator();
		switch (realOperator) {
		case NONE:
			break;

		default:
			builder.append(realOperator.getSymbol());
		}
		return true;
	}

	public boolean appendPseudoClass(PseudoClass node) {
		builder.append(node.getFullName());
		if (node.hasParameters()) {
			builder.append('(');
			append(node.getParameter());
			builder.append(')');
		}

		return true;
	}

	public boolean appendPseudoElement(PseudoElement node) {
		builder.append(node.getFullName());
		return true;
	}

	public boolean appendStyleSheet(StyleSheet styleSheet) {
		appendComments(styleSheet.getOrphanComments(), false);
		appendAllChilds(styleSheet);
		return true;
	}

	public boolean appendRuleset(RuleSet ruleSet) {
		//if (ruleSet.hasEmptyBody())
		//	return false;

		appendSelectors(ruleSet.getSelectors());
		append(ruleSet.getBody());

		return true;
	}

	private boolean appendBodyOptimizeDuplicates(Body body) {
		if (body.isEmpty())
			return false;

		builder.ensureSeparator();
		append(body.getOpeningCurlyBrace());
		builder.increaseIndentationLevel();
		Iterable<LessPrinter> declarationsBuilders = collectUniqueBodyMembersStrings(body);
		for (LessPrinter miniBuilder : declarationsBuilders) {
			builder.ensureNewLine();
			append(miniBuilder);
		}

		appendComments(body.getOrphanComments(), false);
		builder.decreaseIndentationLevel();
		builder.ensureNewLine();
		append(body.getClosingCurlyBrace());

		return true;
	}

	private void append(LessPrinter miniBuilder) {
		builder.append(miniBuilder.builder);
	}

	private Iterable<LessPrinter> collectUniqueBodyMembersStrings(Body body) {
		// the same declaration must be printed only once
		LastOfKindSet<String, LessPrinter> declarationsStrings = new LastOfKindSet<String, LessPrinter>();
		for (ASTCssNode declaration : body.getMembers()) {
			LessPrinter miniPrinter = new LessPrinter(this);

			miniPrinter.append(declaration);
			//miniPrinter.builder.ensureNewLine();

			declarationsStrings.add(miniPrinter.toCss().toString(), miniPrinter);
		}

		return declarationsStrings;
	}

	public boolean appendDeclaration(Declaration declaration) {
		builder.appendIgnoreNull(declaration.getNameAsString());
		builder.append(':');
		builder.ensureSeparator();
		if (declaration.getExpression() != null)
			append(declaration.getExpression());

		if (declaration.isImportant())
			builder.ensureSeparator().append("!important");

		if (shouldHaveSemicolon(declaration))
			builder.appendIgnoreNull(";");

		return true;
	}

	private boolean shouldHaveSemicolon(Declaration declaration) {
		if (null == declaration.getParent() || declaration.getParent().getType() != ASTCssNodeType.SUPPORTS_QUERY)
			return true;

		return false;
	}

	private boolean appendMedia(Media node) {
		builder.append("@media");
		appendCommaSeparated(node.getMediums());
		appendBodySortDeclarations(node.getBody());

		return true;
	}

	private void appendBodySortDeclarations(Body node) {
		// this is sort of hack, bypass the usual append method
		appendComments(node.getOpeningComments(), true);

		builder.ensureSeparator();
		append(node.getOpeningCurlyBrace());
		builder.ensureNewLine().increaseIndentationLevel();

		Iterator<ASTCssNode> declarations = node.getDeclarations().iterator();
		List<ASTCssNode> notDeclarations = node.getNotDeclarations();
		while (declarations.hasNext()) {
			ASTCssNode declaration = declarations.next();
			append(declaration);
			if (declarations.hasNext() || notDeclarations.isEmpty())
				builder.ensureNewLine();
		}
		for (ASTCssNode body : notDeclarations) {
			boolean changedAnything = append(body);
			if (changedAnything)
				builder.ensureNewLine();
		}

		appendComments(node.getOrphanComments(), false);
		builder.decreaseIndentationLevel();
		append(node.getClosingCurlyBrace());

		// this is sort of hack, bypass the usual append method
		appendComments(node.getTrailingComments(), false);
	}

	public boolean appendMediaQuery(MediaQuery mediaQuery) {
		builder.ensureSeparator();
		append(mediaQuery.getMedium());
		boolean needSeparator = (mediaQuery.getMedium() != null);
		for (MediaExpression mediaExpression : mediaQuery.getExpressions()) {
			if (needSeparator) {
				builder.ensureSeparator().append("and");
			}
			append(mediaExpression);
			needSeparator = true;
		}

		return true;
	}

	public boolean appendMedium(Medium medium) {
		append(medium.getModifier());
		append(medium.getMediumType());

		return true;
	}

	public boolean appendMediumModifier(MediumModifier modifier) {
		Modifier kind = modifier.getModifier();
		switch (kind) {
		case ONLY:
			builder.ensureSeparator().append("only");
			break;

		case NOT:
			builder.ensureSeparator().append("not");
			break;

		case NONE:
			break;

		default:
			throw new IllegalStateException("Unknown modifier type.");
		}

		return true;
	}

	public boolean appendMediumType(MediumType medium) {
		builder.ensureSeparator().append(medium.getName());

		return true;
	}

	public boolean appendMediaExpression(FixedMediaExpression expression) {
		builder.ensureSeparator().append('(');
		append(expression.getFeature());
		if (expression.getExpression() != null) {
			builder.append(':');
			builder.ensureSeparator();
			append(expression.getExpression());
		}
		builder.append(')');
		return true;
	}

	private boolean appendInterpolatedMediaExpression(InterpolatedMediaExpression expression) {
		builder.ensureSeparator();
		return append(expression.getExpression());
	}

	public boolean appendMediaExpressionFeature(MediaExpressionFeature feature) {
		builder.append(feature.getFeature());
		return true;
	}

	public boolean appendNamedExpression(NamedExpression expression) {
		builder.append(expression.getName());
		builder.append('=');
		append(expression.getExpression());

		return true;
	}

	public boolean appendComposedExpression(BinaryExpression expression) {
		append(expression.getLeft());
		append(expression.getOperator());
		append(expression.getRight());

		return true;
	}

	public boolean appendListExpression(ListExpression expression) {
		Iterator<Expression> iterator = expression.getExpressions().iterator();
		if (!iterator.hasNext())
			return false;

		append(iterator.next());
		while (iterator.hasNext()) {
			append(expression.getOperator());
			append(iterator.next());
		}
		return true;
	}

	public boolean appendExpressionOperator(ListExpressionOperator operator) {
		ListExpressionOperator.Operator realOperator = operator.getOperator();
		switch (realOperator) {
		case COMMA:
			builder.append(realOperator.getSymbol());
			builder.ensureSeparator();
			break;

		case EMPTY_OPERATOR:
			builder.ensureSeparator();
			break;

		default:
			builder.append(realOperator.getSymbol());
		}

		return true;
	}

	public boolean appendBinaryExpressionOperator(BinaryExpressionOperator operator) {
		BinaryExpressionOperator.Operator realOperator = operator.getOperator();
		switch (realOperator) {
		// TODO this is a huge hack which goes around
		// "we do not parse fonts and less.js does" lack of feature
		// left here intentionally, so we can have correct unit test and can come
		// back to it later
//		case MINUS:
//			builder.ensureSeparator().append('-');
//			break;

		default:
			builder.append(realOperator.getSymbol());
		}

		return true;
	}

	public boolean appendListExpressionOperator(ListExpressionOperator operator) {
		ListExpressionOperator.Operator realOperator = operator.getOperator();
		switch (realOperator) {
		case COMMA:
			builder.append(realOperator.getSymbol());
			builder.ensureSeparator();
			break;

		case EMPTY_OPERATOR:
			builder.ensureSeparator();
			break;

		default:
			builder.append(realOperator.getSymbol());
		}

		return true;
	}

	public boolean appendCssString(CssString expression) {
		String quoteType = expression.getQuoteType();
		builder.append(quoteType).append(expression.getValue()).append(quoteType);

		return true;
	}

	public boolean appendEmptyExpression(EmptyExpression node) {
		return true;
	}

	public boolean appendEscapedValue(EscapedValue escaped) {
		builder.append("~\"" + escaped.getValue() + "\"");

		return true;
	}

	public boolean appendEmbeddedScript(EmbeddedScript escaped) {
		builder.append("~`" + escaped.getValue() + "`");

		return true;
	}

	public boolean appendIdentifierExpression(IdentifierExpression expression) {
		builder.append(expression.getValue());

		return true;
	}

	public boolean appendUnicodeRangeExpression(UnicodeRangeExpression expression) {
		builder.append(expression.getValue());

		return true;
	}

	protected boolean appendColorExpression(ColorExpression expression) {
		// if it is named color expression, write out the name
		if (expression instanceof NamedColorExpression) {
			NamedColorExpression named = (NamedColorExpression) expression;
			builder.append(named.getColorName());
		} else {
			//      cssAndSM.append(expression.getValue(), expression.getUnderlyingStructure());
			builder.append(expression.getValue());
		}

		return true;
	}

	private boolean appendFunctionExpression(FunctionExpression node) {
		builder.append(node.getName());
		builder.append('(');
		append(node.getParameter());
		builder.append(')');

		return true;
	}

	private boolean appendNumberExpression(NumberExpression node) {
		if (node.hasOriginalString()) {
			builder.append(node.getOriginalString());
		} else {
			if (node.hasExpliciteSign()) {
				if (0 < node.getValueAsDouble())
					builder.append('+');
				else
					builder.append('-');
			}
			builder.append(PrintUtils.formatNumber(node.getValueAsDouble()) + node.getSuffix());
		}

		return true;
	}

	public void appendSelectors(List<Selector> selectors) {
		selectors = filterSilent(selectors);
		// follow less.js formatting in special case - only one empty selector
		if (selectors.size() == 1 && isEmptySelector(selectors.get(0))) {
			builder.append(' ');
		}

		Iterator<Selector> iterator = selectors.iterator();
		while (iterator.hasNext()) {
			Selector selector = iterator.next();
			append(selector);

			if (iterator.hasNext())
				builder.append(',').newLine();
		}
	}

	private <T extends ASTCssNode> List<T> filterSilent(List<T> nodes) {
		List<T> result = new ArrayList<T>();
		for (T t : nodes) {
			if (!t.isSilent())
				result.add(t);
		}
		return result;
	}

	private boolean isEmptySelector(Selector selector) {
		if (selector.isCombined())
			return false;

		SelectorPart head = selector.getHead();
		if (head.getType() != ASTCssNodeType.SIMPLE_SELECTOR)
			return false;

		SimpleSelector simpleHead = (SimpleSelector) head;
		if (!simpleHead.isEmptyForm() || !simpleHead.isStar()) {
			return false;
		}

		if (simpleHead.hasSubsequent())
			return false;

		return true;
	}

	public boolean appendSelector(Selector selector) {
		for (SelectorPart part : selector.getParts()) {
			append(part);
		}
		return true;
	}

	private boolean appendSimpleSelector(SimpleSelector selector) {
		if (selector.hasLeadingCombinator())
			append(selector.getLeadingCombinator());

		appendSimpleSelectorHead(selector);
		appendSimpleSelectorTail(selector);
		return true;
	}

	private void appendSimpleSelectorTail(SimpleSelector selector) {
		List<ElementSubsequent> allChilds = selector.getSubsequent();
		for (ElementSubsequent astCssNode : allChilds) {
			append(astCssNode);
		}
	}

	private boolean appendCssClass(CssClass cssClass) {
		builder.append(".");
		if (!cssClass.isInterpolated()) {
			builder.append(cssClass.getName());
		} else {
			try {
				Field f = cssClass.getClass().getDeclaredField("name"); //NoSuchFieldException
				f.setAccessible(true);
				InterpolableName interpolableName = (InterpolableName) f.get(cssClass); //IllegalAccessException
				append(interpolableName);
			} catch (NoSuchFieldException | IllegalAccessException ex) {
				
			}
		}
		return true;
	}

	private void appendSimpleSelectorHead(SimpleSelector selector) {
		builder.ensureSeparator();
		if (!selector.isStar() || !selector.isEmptyForm()) {
			InterpolableName elementName = selector.getElementName();
			builder.appendIgnoreNull(elementName.getName());
		}
	}

	public boolean appendSelectorCombinator(SelectorCombinator combinator) {
		SelectorCombinator.Combinator realCombinator = combinator.getCombinator();
		switch (realCombinator) {
		case DESCENDANT:
			builder.ensureSeparator();
			break;

		default:
			builder.ensureSeparator().append(realCombinator.getSymbol());

		}

		return true;
	}

	private void appendAllChilds(ASTCssNode node) {
		List<? extends ASTCssNode> allChilds = node.getChilds();
		appendAll(allChilds);
	}

	private void appendAll(List<? extends ASTCssNode> all) {
		for (ASTCssNode kid : all) {
			if (append(kid))
				builder.ensureNewLine();
		}
	}

	public String toString() {
		return builder.toString();
	}

	public StringBuilder toCss() {
		return builder.toStringBuilder();
	}


}
