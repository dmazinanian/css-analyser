package ca.concordia.cssanalyser.migration.topreprocessors.differences;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.PropertyAndLayer;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;

/**
 * Represents a difference in the values of a set of declarations
 * for a specific style property.
 * @author Davood Mazinanian
 *
 */
public class StylePropertyValuesDifferenceInValues extends StylePropertyValuesDifference {

	
	private Map<Declaration, Collection<DeclarationValue>> valuesBeingDifferent = new HashMap<>();
	
	public StylePropertyValuesDifferenceInValues(PropertyAndLayer forStylePropertyAndLayer) {
		super(forStylePropertyAndLayer);
	}
	
	public void addDifference(Declaration forDeclaration, Collection<DeclarationValue> values) {
		this.valuesBeingDifferent.put(forDeclaration, values);
	}
	
	public Iterable<Declaration> getDeclarations() {
		return valuesBeingDifferent.keySet();
	}
	
	public Collection<DeclarationValue> getValuesForDeclaration(Declaration declaration) {
		return valuesBeingDifferent.get(declaration);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Style property: ").append(getForStylePropertyAndLayer()).append(System.lineSeparator());
		for (Iterator<Entry<Declaration, Collection<DeclarationValue>>> iterator = valuesBeingDifferent.entrySet().iterator(); iterator.hasNext(); ) {
			Entry<Declaration, Collection<DeclarationValue>> entry = iterator.next();
			builder.append(entry.getValue()).append(" <").append(entry.getKey()).append(">");
			if (iterator.hasNext())
				builder.append(System.lineSeparator());
		}
		return builder.toString();
	}

}
