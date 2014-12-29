package ca.concordia.cssanalyser.migration.topreprocessors.less;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.declaration.value.ValueType;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinDeclaration;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinLiteral;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinParameter;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinParameterizedValue;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinValue;

public class LessMixinMigrationOpportunity extends MixinMigrationOpportunity {
	
	public LessMixinMigrationOpportunity(Iterable<Selector> forSelectors) {
		super(forSelectors);
	}

	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		toReturn.append(this.getMixinName()).append("(");
		for(Iterator<MixinParameter> iterator = getParameters().iterator(); iterator.hasNext(); ) {
			toReturn.append("@").append(iterator.next().getName());
			if (iterator.hasNext())
				toReturn.append("; ");
		}
		toReturn.append(") {").append(System.lineSeparator());
		for (Iterator<MixinDeclaration> iterator = getAllMixinDeclarations().iterator(); iterator.hasNext(); ) {
			MixinDeclaration mixinDeclaration = iterator.next();
			toReturn.append("\t").append(mixinDeclaration.getPropertyName()).append(": ");
			// Get the declaration with the highest number of layers and get all the values from that
			Declaration declarationWithHighestNumberOfLayers = mixinDeclaration.getReferenceDeclaration();
			// values includes all DeclarationValue objects of the declaration with the highest number of values
			List<DeclarationValue> values = new ArrayList<>();
			for (DeclarationValue v : declarationWithHighestNumberOfLayers.getDeclarationValues())
				values.add(v);
			Set<Integer> checkedValuesIndices = new HashSet<>(); 
			for (int i = 0; i < values.size(); i++) {
				DeclarationValue value = values.get(i);
				if (checkedValuesIndices.contains(i))
					continue;
				boolean valueAdded = false;
				if (value.getCorrespondingStyleProperty() != null) {
					MixinValue mixinValue = mixinDeclaration.getMixinValueForPropertyandLayer(value.getCorrespondingStylePropertyAndLayer());
					if (mixinValue != null) {
						if (!(mixinValue instanceof MixinLiteral && ((MixinLiteral) mixinValue).allValuesMissing())) {
							toReturn.append(mixinValue);
						} 
						// Check all the values related to this style property, so we skip them in other runs
						for (int j = 0; j < values.size(); j++) {
							if (!checkedValuesIndices.contains(j) &&
									value.getCorrespondingStylePropertyAndLayer().equals(values.get(j).getCorrespondingStylePropertyAndLayer())) {
								checkedValuesIndices.add(j);
								/*
								 * Try to remove the separator (comma) related to this property and layer
								 * If the next value is a separator and the value after the separator has 
								 * the same property and value, the separator should be removed
								 */
								if (j <= values.size() - 3 && 
										values.get(j + 1).getType() == ValueType.SEPARATOR &&
										value.getCorrespondingStylePropertyAndLayer().equals(values.get(j + 2).getCorrespondingStylePropertyAndLayer())) {
									checkedValuesIndices.add(j + 1);								
								}
							}
						}
						valueAdded = true;
					}
				} else { 
					toReturn.append(value);
					valueAdded = true;
				}
				if (valueAdded && i <= values.size() - 2 && values.get(i + 1).getType() != ValueType.SEPARATOR)
					toReturn.append(" ");
			}
			if (iterator.hasNext())
				toReturn.append(";");
			toReturn.append(System.lineSeparator());
		}
		toReturn.append("}");
		return toReturn.toString();
	}

	@Override
	public String getMixinReferenceString(Selector selector) {
		Map<MixinParameter, MixinParameterizedValue> paramToValMap = getParameterizedValues(selector);
		StringBuilder mixinReferenceStringBuilder = new StringBuilder(getMixinName());
		mixinReferenceStringBuilder.append("(");
		// Preserve the order of parameters
		for (Iterator<MixinParameter> paramterIterator = getParameters().iterator(); paramterIterator.hasNext(); ) {
			MixinParameter parameter = paramterIterator.next();
			MixinParameterizedValue value = paramToValMap.get(parameter);
			//mixinReferenceStringBuilder.append(parameter.toString()).append(": ");
			for (Iterator<DeclarationValue> declarationValueIterator = value.getForValues().iterator(); declarationValueIterator.hasNext(); ) {
				mixinReferenceStringBuilder.append(declarationValueIterator.next().getValue());
				if (declarationValueIterator.hasNext())
					mixinReferenceStringBuilder.append(", ");
			}
			if (paramterIterator.hasNext())
				mixinReferenceStringBuilder.append("; ");
		}
		mixinReferenceStringBuilder.append(");");
		return mixinReferenceStringBuilder.toString();
	}	
}
