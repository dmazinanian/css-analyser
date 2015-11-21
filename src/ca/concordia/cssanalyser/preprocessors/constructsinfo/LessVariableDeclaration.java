package ca.concordia.cssanalyser.preprocessors.constructsinfo;

import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.NamedColorExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.ast.VariableDeclaration;

import ca.concordia.cssanalyser.migration.topreprocessors.less.LessPrinter;


public class LessVariableDeclaration extends LessConstruct  {
	
	public enum VariableScope {
		Local, Global
	}

	public enum VariableType {
		Other, Number, Color, Function, Identifier, String

	}

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
		VariableType type = VariableType.Other;
		if (variableDeclaration.getValue() instanceof NumberExpression) {
			type = VariableType.Number;
		} else if (variableDeclaration.getValue() instanceof FunctionExpression) {
			String valueString = printer.getStringForNode(variableDeclaration.getValue());
			if (valueString.startsWith("rgb") | valueString.startsWith("hsl"))
				type = VariableType.Color;
			else
				type = VariableType.Function;
		} else if (variableDeclaration.getValue() instanceof IdentifierExpression) {
			type = VariableType.Identifier;
		//} else if (variableDeclaration.getValue() instanceof BinaryExpression) {
			
		} else if (variableDeclaration.getValue() instanceof CssString) {
			type = VariableType.String;
		} else if (variableDeclaration.getValue() instanceof ColorExpression ||
					variableDeclaration.getValue() instanceof NamedColorExpression) {
			type = VariableType.Color;
		}
		
		return type;
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
