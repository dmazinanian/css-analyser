package ca.concordia.cssanalyser.cssmodel.declaration;

import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class SingleValuedDeclaration extends Declaration {
	
	protected DeclarationValue declarationValue;

	public SingleValuedDeclaration(String propertyName, DeclarationValue declrationValue, Selector belongsTo, int offset, int length, boolean important) {
		super(propertyName, belongsTo, offset, length, important);
		this.declarationValue = declrationValue;
		// For single-valued declarations, the style property is set to the declaration property
		// See doc for DeclarationValue#setCorrespondingStyleProperty
		declarationValue.setCorrespondingStyleProperty(this.getProperty());
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
		return new SingleValuedDeclaration(property, declarationValue.clone(), parentSelector, offset, length, isImportant);
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
			result = prime * result + offset;
			result = prime * result +  prime * declarationValue.hashCode();
			result = prime * result + (isCommaSeparatedListOfValues ? 1231 : 1237);
			result = prime * result + (isImportant ? 1231 : 1237);
			result = prime * result + length;
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
		if (length != other.length)
			return false;
		if (offset != other.offset)
			return false;
		if (isCommaSeparatedListOfValues != other.isCommaSeparatedListOfValues)
			return false;
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
		if (declarationValue == null) {
			if (other.declarationValue != null)
				return false;
		} else if (!declarationValue.equals(other.declarationValue))
			return false;
		return true;
	}

	
	/**
	 * checks whether two declaration have a set of identical or equivalent values, based 
	 * on the <code>checkEquivalent</code> parameter.
	 * @param otherDeclaration Declaration to be checked with
	 * @param onlyCheckEquality If true, only the equality would be checked. 
	 * If this parameter is true, the method would only rely on {@link DeclarationValue.euqals()}.
	 * Otherwise, if values are of type {@link DeclarationEquivalentValue}, it uses
	 * their {@link DeclarationEquivalentValue#equivalent()} methods to check their equivalency.
	 * In this case, this method also considers missing values. 
	 * @return True if both declarations have identical  set of values,
	 * (or equivalent set of values, based on <code>checkEquivalent</code> parameter) that is:
	 * <ol>
	 * 	<li>The number of values for both are the same,</li>
	 * 	<li>For every value in this declaration, there must be a value in other declaration
	 * 		which is either equivalent or identical, based on <code>checkEquivalent</code> parameter.</li>
	 * </ol>
	 */
	// TODO: This method has not well implemented ??
//	private boolean valuesEquivalent(Declaration otherDeclaration, boolean onlyCheckEquality) {
//		
//		if (declarationValues.size() != otherDeclaration.declarationValues.size()) 
//			return false;
//		/* 
//		 * In most cases, we don't consider the order of values. However, sometimes
//		 * we need them to be considered, like when we are using numeric values. 
//		 * For example, for background-position, we read: 
//		 * "Note that a pair of keywords can be reordered while a combination of keyword and length 
//		 * or percentage cannot. So ‘center left’ is valid while ‘50% left’ is not."
//		 * <http://www.w3.org/TR/css3-background/#the-background-position>
//		 * In general this happens for a limited list of properties, which have more than
//		 * one value, and they must have more than one value which is not a keyword. 
//		 * So first we find the non-keyword values in the declaration.
//		 * 
//		 */
//		
//		List<DeclarationValue> allValues = getRealValues(); 
//		List<DeclarationValue> otherAllValues = otherDeclaration.getRealValues(); 
//	
//		if (allValues.size() != otherAllValues.size() ||
//				(onlyCheckEquality && numberOfMissingValues != otherDeclaration.numberOfMissingValues))
//			return false;
//				
//		int numberOfValuesForWhichOrderIsImportant = 0;
//		for (DeclarationValue v : allValues)
//			if (!v.isKeyword() || "inherit".equals(v.getValue()) || "none".equals(v.getValue()) || NamedColorsHelper.getRGBAColor(v.getValue()) != null)
//				numberOfValuesForWhichOrderIsImportant++;
//		
//		boolean[] checkedValues = new boolean[allValues.size()];
//		
//		for (int i = 0; i < allValues.size(); i++) {
//			
//			DeclarationValue currentValue = allValues.get(i);
//			if (onlyCheckEquality && currentValue.isAMissingValue())
//				continue;
//			
//			boolean orderIsNotImportant = currentValue.isKeyword() || numberOfValuesForWhichOrderIsImportant == 1;
//			if (orderIsNotImportant) {
//				boolean valueFound = false;
//				for (int k = 0; k < otherAllValues.size(); k++) {
//					
//					if (checkedValues[k])
//						continue;
//					
//					DeclarationValue checkingValue = otherAllValues.get(k);
//					
//					if (checkingValue == null || (onlyCheckEquality && checkingValue.isAMissingValue()))
//						continue;
//					
//					if ((!onlyCheckEquality && currentValue.equivalent(checkingValue)) ||
//						(onlyCheckEquality && currentValue.equals(checkingValue))) {
//						/*
//						 * Removing the checking value is necessary for special cases like
//						 * background-position: 0px 0px VS background-position: 0px 10px
//						 */
//						checkedValues[k] = true;
//						valueFound = true;
//						break;
//					}
//				}
//				
//				if (!valueFound)
//					return false;
//
//			} else {
//				
//				// Non-keyword values should appear at the same position in the other declaration
//				DeclarationValue checkingValue = otherAllValues.get(i);
//
//				if (checkedValues[i] || checkingValue == null || (onlyCheckEquality && checkingValue.isAMissingValue()))
//					return false;
//				
//				if ((!onlyCheckEquality && currentValue.equivalent(checkingValue)) ||
//						(onlyCheckEquality && currentValue.equals(checkingValue)))
//					checkedValues[i] = true;
//				else
//					return false;
//
//			}
//		}
//
//		return true;
//	}
	 
}
