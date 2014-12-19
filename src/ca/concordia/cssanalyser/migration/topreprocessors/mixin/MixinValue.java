package ca.concordia.cssanalyser.migration.topreprocessors.mixin;

import ca.concordia.cssanalyser.cssmodel.declaration.PropertyAndLayer;

public abstract class MixinValue {
	
	protected final PropertyAndLayer assignedTo;
	
	public MixinValue(PropertyAndLayer assinedTo) {
		this.assignedTo = assinedTo;
	}
	
	public PropertyAndLayer getAssignedTo() {
		return this.assignedTo;
	}
}
