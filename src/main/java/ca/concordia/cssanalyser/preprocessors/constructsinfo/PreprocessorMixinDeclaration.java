package ca.concordia.cssanalyser.preprocessors.constructsinfo;

import java.util.Set;

public interface PreprocessorMixinDeclaration extends PreprocessorConstruct {

	Set<String> getPropertiesAtTheDeepestLevel(boolean includeNesting);

	String getMixinName();

	int getNumberOfParams();

	int getNumberOfDeclarations();

	int getNumberOfDeclarationsUsingParameters();

	int getNumberOfUniqueCrossBrowserDeclarations();
	
	int getNumberOfNonCrossBrowserDeclarations();
	
	int getNumberOfUniqueParametersUsedInMoreThanOneKindOfDeclaration();
	
	int getNumberOfDeclarationsHavingOnlyHardCodedValues();
	
	int getNumberOfUniqueParametersUsedInVendorSpecific();
	
	int getNumberOfVendorSpecificSharingParameter();
	
	int getNumberOfVariablesOutOfScopeAccessed();

}
