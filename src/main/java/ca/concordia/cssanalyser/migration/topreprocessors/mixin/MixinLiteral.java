package ca.concordia.cssanalyser.migration.topreprocessors.mixin;

import java.util.Collection;
import java.util.Iterator;

import ca.concordia.cssanalyser.cssmodel.declaration.PropertyAndLayer;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;

/**
 * Represents a list of values as a literal in the mixin
 * @author Davood Mazinanian
 *
 */
public class MixinLiteral extends MixinValue {
	
	private final Collection<DeclarationValue> forValues;
	
	public MixinLiteral(Collection<DeclarationValue> values, PropertyAndLayer assignedTo) {
		super(assignedTo);
		this.forValues = values;
	}
	
	public Iterable<DeclarationValue> getValues() {
		return forValues;
	}
	
	public boolean allValuesMissing() {
		for (DeclarationValue value : forValues)
			if (!value.isAMissingValue())
				return false;
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		for (Iterator<DeclarationValue> iterator = forValues.iterator(); iterator.hasNext(); ) {
			String valueString = iterator.next().getValue();
			if (assignedTo != null) {
				/*
				 * Special handling for / in border-radius
				 * This is necessary, as preprocessor compiler 
				 * does not distinguish this with binary operator
				 */
				String propertyName = assignedTo.getPropertyName();
				if ("border-radius-slash".equals(propertyName)) {
					valueString = "~'" + valueString + "'";
				}
			}
			toReturn.append(valueString);
			if (iterator.hasNext())
				toReturn.append(", ");
		}
		return toReturn.toString();
	}

}
