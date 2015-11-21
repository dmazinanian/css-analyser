package ca.concordia.cssanalyser.migration.topreprocessors.differences;

import ca.concordia.cssanalyser.cssmodel.declaration.PropertyAndLayer;

public abstract class StylePropertyValuesDifference {
	
	private final PropertyAndLayer forStylePropertyAndLayer;
	
	public StylePropertyValuesDifference(PropertyAndLayer forPropertyAndLayer) {
		this.forStylePropertyAndLayer = forPropertyAndLayer;
	}
	
	public PropertyAndLayer getForStylePropertyAndLayer() {
		return forStylePropertyAndLayer;
	}
	
	
	
}
