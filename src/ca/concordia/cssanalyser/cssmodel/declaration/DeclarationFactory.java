package ca.concordia.cssanalyser.cssmodel.declaration;

import java.util.List;

import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;


/**
 * A factory class to return {@link Declaration} or {@link ShorthandDeclaration}
 * based on the property name
 * 
 * @author Davood Mazinanian
 *
 */
public class DeclarationFactory {

	/**
	 * Returns {@link Declaration} or {@link ShorthandDeclaration}, based on the 
	 * property name.
	 * 
	 * @param propertyName
	 * @param values
	 * @param belongsTo
	 * @param fileLineNumber
	 * @param fileColNumber
	 * @param important
	 * @return
	 */
	public static Declaration getDeclaration(String propertyName, List<DeclarationValue> values, Selector belongsTo, int fileLineNumber, int fileColNumber, boolean important) {
		if (ShorthandDeclaration.isShorthandProperty(propertyName))
			return new ShorthandDeclaration(propertyName, values, belongsTo, fileLineNumber, fileColNumber, important);
		else 
			return new Declaration(propertyName, values, belongsTo, fileLineNumber, fileColNumber, important);
	}
	
}
