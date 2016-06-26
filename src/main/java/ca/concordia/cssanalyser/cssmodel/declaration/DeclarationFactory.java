package ca.concordia.cssanalyser.cssmodel.declaration;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;

import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.cssmodel.LocationInfo;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.declaration.value.ValueType;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;


/**
 * A factory class to return {@link Declaration} or {@link ShorthandDeclaration}
 * based on the property name
 * 
 * @author Davood Mazinanian
 *	
 */
public class DeclarationFactory {
	
	private static final Logger LOGGER = FileLogger.getLogger(DeclarationFactory.class);

	/**
	 * Returns {@link Declaration}, {@link ShorthandDeclaration} or {@link ShorthandDeclaration}, based on the property name.
	 * 
	 * @param propertyName
	 * @param values
	 * @param belongsTo
	 * @param important
	 * @param addMissingValues
	 * @param locationInfo
	 * @return
	 */
	public static Declaration getDeclaration(String propertyName, List<DeclarationValue> values, Selector belongsTo, boolean important, boolean addMissingValues, LocationInfo locationInfo) {
		if (MultiValuedDeclaration.isMultiValuedProperty(propertyName)) 
			return new MultiValuedDeclaration(propertyName, values, belongsTo, important, addMissingValues, locationInfo);
		if (ShorthandDeclaration.isShorthandProperty(propertyName))
			return new ShorthandDeclaration(propertyName, values, belongsTo, important, addMissingValues, locationInfo);
		else {
			DeclarationValue declarationValue = values.get(0);
			if (values.size() > 1) {
				String concatanated = "";
				for (Iterator<DeclarationValue> iterator = values.iterator(); iterator.hasNext();) {
					DeclarationValue dv = iterator.next();
					concatanated += dv.getValue();
					if (iterator.hasNext())
						concatanated += " ";
				}
				declarationValue = new DeclarationValue(concatanated, ValueType.OTHER);
				LOGGER.warn(String.format("Multiple values for single-valued property '%s' are given. All the values are concatenated to make a single value '%s'. Values are %s",
						propertyName, concatanated, values.toString()));
			}
			return new SingleValuedDeclaration(propertyName, declarationValue, belongsTo, important, locationInfo);
		}
	}
	
}
