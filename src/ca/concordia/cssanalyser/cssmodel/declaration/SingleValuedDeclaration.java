package ca.concordia.cssanalyser.cssmodel.declaration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.LocationInfo;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class SingleValuedDeclaration extends Declaration {
	
	protected DeclarationValue declarationValue;

	public SingleValuedDeclaration(String propertyName, DeclarationValue declrationValue, Selector belongsTo, boolean important, LocationInfo locationInfo) {
		super(propertyName, belongsTo, important, locationInfo);
		this.declarationValue = declrationValue;
		// For single-valued declarations, the style property is set to the declaration property
		// See doc for DeclarationValue#setCorrespondingStyleProperty
		declarationValue.setCorrespondingStyleProperty(this.getProperty(), 1);
	}

	@Override
	protected boolean valuesEquivalent(Declaration otherDeclaration) {
		
		if (!(otherDeclaration instanceof SingleValuedDeclaration))
			throw new RuntimeException("This method cannot be called on a declaration rather than SingleValuedDeclaration.");
		
		SingleValuedDeclaration otherSingleValuedDeclaration = (SingleValuedDeclaration)otherDeclaration;
		
		return declarationValue.equivalent(otherSingleValuedDeclaration.getValue());

	}

	
	public DeclarationValue getValue() {
		return declarationValue;
	}

	@Override
	public Declaration clone() {
		return new SingleValuedDeclaration(property, declarationValue.clone(), parentSelector, isImportant, locationInfo);
	}

	@Override
	protected boolean valuesEqual(Declaration otherDeclaration) {
		if (!(otherDeclaration instanceof SingleValuedDeclaration))
			throw new RuntimeException("This method cannot be called on a declaration rather than SingleValuedDeclaration.");
		
		SingleValuedDeclaration otherSingleValuedDeclaration = (SingleValuedDeclaration)otherDeclaration;
		
		return declarationValue.equals(otherSingleValuedDeclaration.getValue());

	}
	
	@Override
	public String toString() {	
		return String.format("%s: %s", property, declarationValue);
	}
	
	int hashCode = -1;
	@Override
	public int hashCode() {
		// Only calculate the hashCode once
		if (hashCode == -1) {
			final int prime = 31;
			int result = 1;
			result = prime * result + locationInfo.hashCode();
			result = prime * result +  prime * declarationValue.hashCode();
			result = prime * result + (isImportant ? 1231 : 1237);
			result = prime * result + 0;
			result = prime * result
					+ ((parentSelector == null) ? 0 : parentSelector.hashCode());
			result = prime * result
					+ ((property == null) ? 0 : property.hashCode());
			hashCode = result;
		}
		return hashCode;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SingleValuedDeclaration other = (SingleValuedDeclaration) obj;
		if (isImportant != other.isImportant)
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		if (parentSelector == null) {
			if (other.parentSelector != null)
				return false;
		} else if (!parentSelector.equals(other.parentSelector))
			return false;
		if (locationInfo == null) {
			if (other.locationInfo != null)
				return false;
		} else if (!locationInfo.equals(other.locationInfo))
			return false;
		if (declarationValue == null) {
			if (other.declarationValue != null)
				return false;
		} else if (!declarationValue.equals(other.declarationValue))
			return false;
		return true;
	}

	@Override
	public Map<String, DeclarationValue> getPropertyToValuesMap() {
		Map<String, DeclarationValue> toReturn = new HashMap<>();
		toReturn.put(property, declarationValue);
		return toReturn;
	}
	
	@Override
	public Collection<String> getStyleProperties() {
		Set<String> toReturn = new HashSet<>();
		toReturn.add(property);
		return toReturn;
	}

	@Override
	public Iterable<DeclarationValue> getDeclarationValues() {
		List<DeclarationValue> toReturn = new ArrayList<>();
		toReturn.add(declarationValue);
		return toReturn;
	}

	@Override
	public int getNumberOfValueLayers() {
		return 1;
	}

	@Override
	public Collection<DeclarationValue> getDeclarationValuesForStyleProperty(String styleProperty, int forLayer) {
		List<DeclarationValue> values = new ArrayList<>();
		if (property.equals(styleProperty)) {
			values.add(declarationValue);
		}
		return values;
	}
	
	@Override
	public Set<PropertyAndLayer> getAllSetPropertyAndLayers() {
		Set<PropertyAndLayer> allSetPropertyAndLayers = new HashSet<>();
		allSetPropertyAndLayers.add(new PropertyAndLayer(this.property, 1));
		return allSetPropertyAndLayers;
	}
	 
}
