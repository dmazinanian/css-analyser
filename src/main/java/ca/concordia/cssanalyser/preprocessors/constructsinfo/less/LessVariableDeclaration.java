package ca.concordia.cssanalyser.preprocessors.constructsinfo.less;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.BinaryExpression;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.ParenthesesExpression;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.ast.VariableDeclaration;

import ca.concordia.cssanalyser.migration.topreprocessors.less.LessPrinter;


public class LessVariableDeclaration extends LessConstruct  {
	
	public enum VariableScope {
		Local, Global
	}

	public enum VariableType {
		OTHER, NUMBER, COLOR, OTHER_FUNCTION, IDENTIFIER, STRING,
		COLOR_FUNCTION, NUMBER_FUNCTION, STRING_FUNCTION, EXPRESSION_OF_VARIABLE, LITERAL_LIST
	}
	
	private static Set<String> colorFunctions = new HashSet<>(Arrays.asList(
		new String[] {
			"color",
			"rgb",
			"rgba",
			"hsl",
			"hsla",
			"hsv",
			"hsva",
			"saturate",
			"desaturate",
			"lighten",
			"darken",
			"fadein",
			"fadeout",
			"fade",
			"spin",
			"mix",
			"tint",
			"shade",
			"greyscale",
			"contrast",
			"multiply",
			"screen",
			"overlay",
			"softlight",
			"hardlight",
			"difference",
			"exclusion",
			"average",
			"negation"
		}
	));
	
	private static Set<String> numberFunctions = new HashSet<>(Arrays.asList(
		new String[] {
			"image-size",
			"image-width",
			"image-height",
			"convert",
			"unit",
			"length",
			"ceil",
			"floor",
			"percentage",
			"round",
			"sqrt",
			"abs",
			"sin",
			"asin",
			"cos",
			"acos",
			"tan",
			"atan",
			"pi",
			"pow",
			"mod",
			"min",
			"max",
			"hue",
			"saturation",
			"lightness",
			"hsvhue",
			"hsvsaturation",
			"hsvvalue",
			"red",
			"green",
			"blue",
			"alpha",
			"luma",
			"luminance"
		}
	));
	
	private static Set<String> stringFunctions = new HashSet<>(Arrays.asList(
		new String[] {
			"escape",
			"e",
			"%",
			"replace",
		}
	));

	private static LessPrinter printer = new LessPrinter();

	private final VariableDeclaration variableDeclaration;

	public LessVariableDeclaration(VariableDeclaration variableDeclaration, StyleSheet parentStyleSheet) {
		super(parentStyleSheet);
		this.variableDeclaration = variableDeclaration;
	}
	
	public VariableDeclaration getVariableDeclaration() {
		return variableDeclaration;
	}

	public int getSourceLine() {
		return variableDeclaration.getSourceLine();
	}
	
	public int getSourceColumn() {
		return variableDeclaration.getSourceColumn();
	}

	public String getVariableString() {
		return printer.getStringForNode(variableDeclaration);
	}

	public VariableType getVariableType() {
		VariableType type = VariableType.OTHER;
		Expression value = variableDeclaration.getValue();
		if (value instanceof NumberExpression) {
			type = VariableType.NUMBER;
		} else if (value instanceof FunctionExpression) {
			FunctionExpression functionExpression = (FunctionExpression)value;
			String functionName = functionExpression.getName();
			if (isColorFunction(functionName))
				type = VariableType.COLOR_FUNCTION;
			else if (isNumberFunction(functionName))
				type = VariableType.NUMBER_FUNCTION;
			else if (isStringFunction(functionName))
				type = VariableType.STRING_FUNCTION;
			else
				type = VariableType.OTHER_FUNCTION;
		} else if (value instanceof IdentifierExpression) {
			type = VariableType.IDENTIFIER;
		//} else if (variableDeclaration.getValue() instanceof BinaryExpression) {
			
		} else if (value instanceof CssString) {
			type = VariableType.STRING;
		} else if (value instanceof ColorExpression /*||
					value instanceof NamedColorExpression*/) {
			type = VariableType.COLOR;
		} else if (value instanceof ListExpression) {
			ListExpression listExpression = (ListExpression) value;
			List<ASTCssNode> allChilds = LessASTQueryHandler.getAllChilds(listExpression);
			for (ASTCssNode astCssNode : allChilds) {
				if (astCssNode instanceof Variable) {
					type = VariableType.EXPRESSION_OF_VARIABLE;
					break;
				}
			}
			if (type == VariableType.OTHER) {
				type = VariableType.LITERAL_LIST;
			}
		} else if (value instanceof BinaryExpression || value instanceof EscapedValue ||
				value instanceof Variable || value instanceof ParenthesesExpression) {
			type = VariableType.EXPRESSION_OF_VARIABLE;
		}
		
		return type;
	}

	private boolean isColorFunction(String valueString) {
		return colorFunctions.contains(valueString);
	}

	private boolean isNumberFunction(String valueString) {
		return numberFunctions.contains(valueString);
	}
	
	private boolean isStringFunction(String valueString) {
		return stringFunctions.contains(valueString);
	}

	public String getFunctionName() {
		if (variableDeclaration.getValue() instanceof FunctionExpression) {
			FunctionExpression functionExpression = (FunctionExpression)variableDeclaration.getValue();
			return functionExpression.getName();
		}
		return "";
	}

	public VariableScope getScope() {
		VariableScope scope = VariableScope.Local;
		if (variableDeclaration.getParent() instanceof StyleSheet)
			scope = VariableScope.Global;
		return scope;
	}
	
	@Override
	public int hashCode() {	
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getVariableString() == null) ? 0 : getVariableString().hashCode());
		result = prime * result + ((getStyleSheetPath() == null) ? 0 : getStyleSheetPath().hashCode());
		result = prime * result + variableDeclaration.getSourceLine();
		result = prime * result + variableDeclaration.getSourceColumn();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LessVariableDeclaration other = (LessVariableDeclaration) obj;
		
		if (getVariableString() == null) {
			if (other.getVariableString() != null)
				return false;
		} else if (!getVariableString().equals(other.getVariableString()))
			return false;
		if (getStyleSheetPath() == null) {
			if (other.getStyleSheetPath() != null)
				return false;
		} else if (!getStyleSheetPath().equals(other.getStyleSheetPath()))
			return false;
		if (this.variableDeclaration.getSourceLine() != other.variableDeclaration.getSourceLine())
			return false;
		if (this.variableDeclaration.getSourceColumn() != other.variableDeclaration.getSourceColumn())
			return false;
		return true;
	}
	
}
