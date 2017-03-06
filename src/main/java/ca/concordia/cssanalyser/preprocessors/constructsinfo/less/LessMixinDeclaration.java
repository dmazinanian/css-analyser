package ca.concordia.cssanalyser.preprocessors.constructsinfo.less;

import java.util.HashSet;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.StyleSheet;

import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.preprocessors.constructsinfo.PreprocessorMixinDeclaration;

public class LessMixinDeclaration extends LessConstruct implements PreprocessorMixinDeclaration {
	
	private final ReusableStructure reusableNode;
	
	private int numberOfDeclarations = 0, 
			numberOfDeclarationsUsingParameters = 0,
			numberOfNonCrossBrowserDeclarations = 0,
			numberOfUniqueCrossBrowserDeclarations = 0,
			numberOfUniqueParametersUsedInMoreThanOneKindOfDeclaration = 0,
			numberOfDeclarationsHavingOnlyHardCodedValues = 0,
			numberOfUniqueParametersUsedInVendorSpecific = 0,
			numberOfVendorSpecificSharingParameter = 0,
			numberOfVariablesOutOfScopeAccessed;

	public LessMixinDeclaration(ReusableStructure reusableStructure, StyleSheet parentStyleSheet) {
		super(parentStyleSheet);
		this.reusableNode = reusableStructure;
		
	}
	
	public  ReusableStructure getReusableNode() {
		return this.reusableNode;
	}

	public String getMixinName() {
		String mixinName = reusableNode.getNamesAsStrings().toString();
		mixinName = mixinName.substring(1, mixinName.length() - 1);
		return mixinName;
	}

	public int getNumberOfParams() {
		return reusableNode.getParameters().size();
	}

	public int getNumberOfDeclarations() {
		return numberOfDeclarations;
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
		this.numberOfDeclarations += i;
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

	public Set<Declaration> getDeclarations() {
		return getDeclarations(true);
	}
	
	public Set<Declaration> getDeclarations(boolean includeNesting) {
		return LessASTQueryHandler.getAllDeclarations(this.reusableNode.getBody(), includeNesting);
	}

	public String getMixinHashString() {
		return String.format("%s(%s)", getMixinNodeName(), reusableNode.getParameters().size());
	}

	private String getMixinNodeName() {
		String nodeName = LessASTQueryHandler.getNodeName(reusableNode);
		nodeName = nodeName.substring(0, nodeName.indexOf('('));
		return nodeName;
	}

	public Set<String> getPropertiesAtTheDeepestLevel(boolean includeNesting) {
		Set<String> propertiesToReturn = new HashSet<>();
		Set<Declaration> declarations = getDeclarations(includeNesting);
		for (Declaration declaration : declarations) {
			String property = declaration.getNameAsString();
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getMixinHashString() == null) ? 0 : getMixinHashString().hashCode());
		result = prime * result + ((getStyleSheetPath() == null) ? 0 : getStyleSheetPath().hashCode());
		result = prime * result + reusableNode.getSourceLine();
		result = prime * result + reusableNode.getSourceColumn();
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
		LessMixinDeclaration other = (LessMixinDeclaration) obj;
		
		if (getMixinHashString() == null) {
			if (other.getMixinHashString() != null)
				return false;
		} else if (!getMixinHashString().equals(other.getMixinHashString()))
			return false;
		if (getStyleSheetPath() == null) {
			if (other.getStyleSheetPath() != null)
				return false;
		} else if (!getStyleSheetPath().equals(other.getStyleSheetPath()))
			return false;
		if (reusableNode.getSourceColumn() != other.reusableNode.getSourceColumn())
			return false;
		if (reusableNode.getSourceLine() != other.reusableNode.getSourceLine())
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return getMixinHashString();
	}
	 
}
