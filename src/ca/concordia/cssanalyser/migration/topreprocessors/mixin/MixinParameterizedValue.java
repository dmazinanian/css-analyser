package ca.concordia.cssanalyser.migration.topreprocessors.mixin;

import java.util.Collection;
import java.util.Iterator;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;

/**
 * Represents a value in the call site for a mixin
 * @author Davood Mazinanian
 *
 */
public class MixinParameterizedValue {
	
	private MixinParameter mixinParameter;
	private Collection<DeclarationValue> forValues;
	private Declaration forDeclaration;
	
	public MixinParameterizedValue(Declaration forDeclaration, Collection<DeclarationValue> forValues, MixinParameter mixinParameter) {
		this.mixinParameter = mixinParameter;
		this.forDeclaration = forDeclaration;
		this.forValues = forValues;
	}

	public MixinParameter getMixinParameter() {
		return mixinParameter;
	}

	public Declaration getForDeclaration() {
		return forDeclaration;
	}

	public Collection<DeclarationValue> getForValues() {
		return forValues;
	}
	
	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		toReturn.append(mixinParameter).append(" = ");
		for (Iterator<DeclarationValue> iterator = forValues.iterator(); iterator.hasNext(); ) {
			toReturn.append(iterator.next().getValue());
			if (iterator.hasNext())
				toReturn.append(", ");
		}
		toReturn.append(" in ").append(forDeclaration);
		toReturn.append(" <");
		toReturn.append(forValues.iterator().next().getCorrespondingStyleProperty());
		toReturn.append(", ");
		toReturn.append(forValues.iterator().next().getCorrespondingStyleLayer());
		toReturn.append(">");
		return toReturn.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((forDeclaration == null) ? 0 : forDeclaration.hashCode());
		result = prime * result
				+ ((forValues == null) ? 0 : forValues.hashCode());
		result = prime * result
				+ ((mixinParameter == null) ? 0 : mixinParameter.hashCode());
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
		MixinParameterizedValue other = (MixinParameterizedValue) obj;
		if (forDeclaration == null) {
			if (other.forDeclaration != null) {
				return false;
			}
		} else if (!forDeclaration.equals(other.forDeclaration)) {
			return false;
		}
		if (forValues == null) {
			if (other.forValues != null) {
				return false;
			}
		} else if (!forValues.equals(other.forValues)) {
			return false;
		}
		if (mixinParameter == null) {
			if (other.mixinParameter != null) {
				return false;
			}
		} else if (!mixinParameter.equals(other.mixinParameter)) {
			return false;
		}
		return true;
	}
	
}
