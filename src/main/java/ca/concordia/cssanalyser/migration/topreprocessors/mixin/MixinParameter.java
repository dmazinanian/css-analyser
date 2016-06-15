package ca.concordia.cssanalyser.migration.topreprocessors.mixin;

import ca.concordia.cssanalyser.cssmodel.declaration.PropertyAndLayer;


public class MixinParameter implements MixinValue {
	
	private String parameterName;
	
	public MixinParameter(String parameterName, MixinDeclaration forDeclaration, PropertyAndLayer assignedTo) {
		this.parameterName = parameterName;
	}
	
	public String getName() {
		return parameterName;
	}
	
	public void setName(String name) {
		String mixinNamePattern = "[a-zA-Z_][a-zA-Z0-9_]*";
		if (!name.matches(mixinNamePattern)) {
			throw new IllegalArgumentException("Parameter name is invalid");
		}
		this.parameterName = name;
	}
	
	// FIXME: Less syntax
	@Override
	public String toString() {
		return String.format("@%s", parameterName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((parameterName == null) ? 0 : parameterName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MixinParameter other = (MixinParameter) obj;
		if (parameterName == null) {
			if (other.parameterName != null) {
				return false;
			}
		} else if (!parameterName.equals(other.parameterName)) {
			return false;
		}
		return true;
	}

}
