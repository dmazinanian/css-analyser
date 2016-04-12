package ca.concordia.cssanalyser.preprocessors.constructsinfo;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.DetachedRuleset;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.StyleSheet;

import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessHelper;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessPrinter;
import ca.concordia.cssanalyser.parser.ParseException;
import ca.concordia.cssanalyser.parser.less.LessCSSParser;

public class LessMixinCall extends LessConstruct {

	private final int numberOfMultiValuedArguments;
	private final MixinReference reference;
	private LessMixinDeclaration mixinDeclaration;

	public LessMixinCall(MixinReference reference, int numberOfMultiValuedArguments, StyleSheet styleSheet) {
		super(styleSheet);
		this.reference = reference;
		this.numberOfMultiValuedArguments = numberOfMultiValuedArguments;
	}

	public int getNumberOfMultiValuedArguments() {
		return numberOfMultiValuedArguments;
	}

	public MixinReference getReference() {
		return reference;
	}
	
	public String getName() {
		return reference.getFinalNameAsString();
	}
	
	public int getNumberOfParameters() {
		return reference.getNamedParameters().size() + reference.getPositionalParameters().size();
	}

	public ASTCssNode getParentStructure() {
		return getReference().getParent().getParent();
	}

	public String getMixinCallHashString() {
		return String.format("%s(%s)", this.reference.getFinalNameAsString(), getNumberOfParameters());
	}
	
	@Override
	public String toString() {
		return getMixinCallHashString();
	}

	public void setMixinDeclaration(LessMixinDeclaration mixinDeclaration) {
		this.mixinDeclaration = mixinDeclaration; 
	}
	
	public LessMixinDeclaration getMixinDeclaration() {
		return this.mixinDeclaration;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getMixinCallHashString() == null) ? 0 : getMixinCallHashString().hashCode());
		result = prime * result + numberOfMultiValuedArguments;
		result = prime * result + ((getStyleSheetPath()  == null) ? 0 : getStyleSheetPath().hashCode());
		result = prime * result + reference.getSourceLine();
		result = prime * result + reference.getSourceColumn();
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
		LessMixinCall other = (LessMixinCall) obj;
		if (getMixinCallHashString() == null) {
			if (other.getMixinCallHashString() != null)
				return false;
		} else if (!getMixinCallHashString().equals(other.getMixinCallHashString()))
			return false;
		if (numberOfMultiValuedArguments != other.numberOfMultiValuedArguments)
			return false;
		if (getStyleSheetPath() == null) {
			if (other.getStyleSheetPath() != null)
				return false;
		} else if (!getStyleSheetPath().equals(other.getStyleSheetPath()))
			return false;
		if (this.reference == null) {
			if (other.reference != null)
				return false;
		} 
		if (this.reference != null) {
			if (this.reference.getSourceLine() != other.reference.getSourceLine())
				return false;
			if (this.reference.getSourceColumn() != other.reference.getSourceColumn())
				return false;
		}
		return true;
	}

	public Selector getCallingSelector() {

		List<RuleSet> parents = new ArrayList<>();

		ASTCssNode parentStructure = getParentStructure();
		if (parentStructure == null) {
			return null;
		}
		
		RuleSet lastOne = null;
		do {
			if (parentStructure instanceof RuleSet) {
				RuleSet clone = (RuleSet)parentStructure.clone();
				clone.getBody().removeAllMembers();
				if (lastOne == null) {
					try {
						clone.getBody().addMember(LessHelper.getLessNodeFromLessString("foo: bar;"));
					} catch (ParseException e) {
						e.printStackTrace();
						return null;
					}
				} else {
					clone.getBody().addMember(lastOne);
				}
				clone.configureParentToAllChilds();
				parents.add(clone);
				lastOne = clone;
			}
			if (parentStructure.getParent() instanceof GeneralBody) {
				parentStructure = parentStructure.getParent().getParent();
			} else if (parentStructure.getParent() instanceof StyleSheet) {
				break;
			} else if (parentStructure.getParent() instanceof DetachedRuleset ||
					parentStructure.getParent() instanceof MixinReference) {
				return null;
			} else {
				throw new RuntimeException("What is parent structure " + parentStructure.getParent());
			}
		} while (!(parentStructure instanceof StyleSheet));
		
		if (parents.size() == 0)
			return null;
		
		Selector selectorToReturn = null;
		
		LessPrinter printer = new LessPrinter();
		ASTCssNode rootNode = parents.get(parents.size() - 1);
		
		try {
			String stringOfRootNode = printer.getStringForNode(rootNode);		
			StyleSheet lessStyleSheet = LessCSSParser.getLessStyleSheet(new LessSource.StringSource(stringOfRootNode));
			ca.concordia.cssanalyser.cssmodel.StyleSheet compileLESSStyleSheet = LessHelper.compileLESSStyleSheet(lessStyleSheet);
			if (compileLESSStyleSheet.getAllSelectors().iterator().hasNext()) {
				selectorToReturn = compileLESSStyleSheet.getAllSelectors().iterator().next();
				selectorToReturn.removeDeclaration(selectorToReturn.getDeclarations().iterator().next());
			}
		} catch (ParseException | Less4jException e) {
			e.printStackTrace();
		}

		return selectorToReturn;
	}
}
