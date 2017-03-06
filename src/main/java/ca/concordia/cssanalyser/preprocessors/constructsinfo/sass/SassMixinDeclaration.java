package ca.concordia.cssanalyser.preprocessors.constructsinfo.sass;

import java.util.HashSet;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.preprocessors.constructsinfo.PreprocessorMixinDeclaration;

public class SassMixinDeclaration implements PreprocessorMixinDeclaration {
	
	private String mixinName;
	private Set<String> properties = new HashSet<>();
	private String styleSheetPath;
	private int numberOfCalls;
	private int numberOfParameters;
	
	private int numberOfDeclarationsUsingParameters = 0,
			numberOfNonCrossBrowserDeclarations = 0,
			numberOfUniqueCrossBrowserDeclarations = 0,
			numberOfUniqueParametersUsedInMoreThanOneKindOfDeclaration = 0,
			numberOfDeclarationsHavingOnlyHardCodedValues = 0,
			numberOfUniqueParametersUsedInVendorSpecific = 0,
			numberOfVendorSpecificSharingParameter = 0,
			numberOfVariablesOutOfScopeAccessed;

	public SassMixinDeclaration(String mixinName, String styleSheetPath, int params, int numberOfCalls) {
		this.mixinName = mixinName;
		this.styleSheetPath = styleSheetPath;
		this.numberOfParameters = params;
		this.numberOfCalls = numberOfCalls;	
	}
	
	public void addProperty(String property) {
		properties.add(property);
	}
	
	public String getMixinName() {
		return mixinName;
	}

	public int getNumberOfDeclarations() {
		return properties.size();
	}

	public int getNumberOfDeclarationsUsingParameters() {
		return numberOfDeclarationsUsingParameters;
	}

	public int getNumberOfUniqueCrossBrowserDeclarations() {
		return numberOfUniqueCrossBrowserDeclarations;
	}

	public int getNumberOfNonCrossBrowserDeclarations() {
		return numberOfNonCrossBrowserDeclarations;
	}

	public int getNumberOfUniqueParametersUsedInMoreThanOneKindOfDeclaration() {
		return numberOfUniqueParametersUsedInMoreThanOneKindOfDeclaration;
	}

	public int getNumberOfDeclarationsHavingOnlyHardCodedValues() {
		return numberOfDeclarationsHavingOnlyHardCodedValues;
	}

	public int getNumberOfUniqueParametersUsedInVendorSpecific() {
		return numberOfUniqueParametersUsedInVendorSpecific;
	}

	public int getNumberOfVendorSpecificSharingParameter() {
		return numberOfVendorSpecificSharingParameter;
	}

	public void increaseNumberOfUniqueCrossBrowserDeclarations(int i) {
		this.numberOfUniqueCrossBrowserDeclarations += i;
	}

	public void increaseNumberOfNonCrossBrowserDeclarations(int i) {
		this.numberOfNonCrossBrowserDeclarations += i;
	}

	public void increaseNumberOfDeclarations(int i) {
		
	}

	public void increaseNumberOfDeclarationsUsingParameters(int i) {
		this.numberOfDeclarationsUsingParameters += i;
	}

	public void increaseNumberOfDeclarationsHavingOnlyHardCodedValues(int i) {
		this.numberOfDeclarationsHavingOnlyHardCodedValues += i;
	}

	public void increaseNumberOfUniqueParametersUsedInMoreThanOneKindOfDeclaration(int i) {
		this.numberOfUniqueParametersUsedInMoreThanOneKindOfDeclaration += i;
	}

	public void increaseNumberOfUniqueParametersUsedInVendorSpecific(int i) {
		this.numberOfUniqueParametersUsedInVendorSpecific += i;
	}

	public void increaseNumberOfVendorSpecificSharingParameter(int i) {
		this.numberOfVendorSpecificSharingParameter += i;
	}
	
	public int getNumberOfVariablesOutOfScopeAccessed() {
		return numberOfVariablesOutOfScopeAccessed;
	}

	public void increaseNumberOfVariablesOutOfScopeAccessed(int i) {
		this.numberOfVariablesOutOfScopeAccessed += i;
	}

	
	public Set<String> getPropertiesAtTheDeepestLevel(boolean includeNesting) {
		Set<String> propertiesToReturn = new HashSet<>();
		for (String property : properties) {
			if (ShorthandDeclaration.isShorthandProperty(property)) {
				propertiesToReturn.addAll(ShorthandDeclaration.getIndividualPropertiesForAShorthand(property));
			} else {
				propertiesToReturn.add(property);
			}
		}
		// remove existing shorthands
		for (String property : new HashSet<>(propertiesToReturn)) {
			if (ShorthandDeclaration.isShorthandProperty(property) &&
					propertiesToReturn.containsAll(ShorthandDeclaration.getIndividualPropertiesForAShorthand(property)))
				propertiesToReturn.remove(property);
		}
		return propertiesToReturn;
	}

	@Override
	public String getStyleSheetPath() {
		return styleSheetPath;
	}

	@Override
	public int getNumberOfParams() {
		return this.numberOfParameters;
	}

	public int getNumberOfCalls() {
		return numberOfCalls;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mixinName == null) ? 0 : mixinName.hashCode());
		result = prime * result + numberOfCalls;
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((styleSheetPath == null) ? 0 : styleSheetPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SassMixinDeclaration other = (SassMixinDeclaration) obj;
		if (mixinName == null) {
			if (other.mixinName != null)
				return false;
		} else if (!mixinName.equals(other.mixinName))
			return false;
		if (numberOfCalls != other.numberOfCalls)
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (styleSheetPath == null) {
			if (other.styleSheetPath != null)
				return false;
		} else if (!styleSheetPath.equals(other.styleSheetPath))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return getMixinHashString();
	}
	
	public String getMixinHashString() {
		return String.format("%s(%s)",getMixinName(), getNumberOfParams());
	}
	
}
