package ca.concordia.cssanalyser.preprocessors.constructsinfo.less;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.DetachedRuleset;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
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
	private final LessASTQueryHandler queryHandler;
	private List<Selector> callingSelectors;

	public LessMixinCall(MixinReference reference, int numberOfMultiValuedArguments, StyleSheet styleSheet, LessASTQueryHandler queryHandler) {
		super(styleSheet);
		this.reference = reference;
		this.numberOfMultiValuedArguments = numberOfMultiValuedArguments;
		this.queryHandler = queryHandler;
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

	public List<Selector> getCallingSelectors() {

		if (callingSelectors == null) {

			List<Selector> selectorsToReturn = new ArrayList<>();

			List<LessMixinCall> mixinCallInfo = queryHandler.getMixinCallInfo();
			List<List<ASTCssNode>> parentsList = getParentRuleSets(mixinCallInfo, new HashSet<>());

			if (parentsList.size() == 0)
				return selectorsToReturn;

			for (List<ASTCssNode> parents : parentsList) {
				LessPrinter printer = new LessPrinter();
				ASTCssNode rootNode = parents.get(parents.size() - 1);

				try {
					String stringOfRootNode = printer.getStringForNode(rootNode);		
					StyleSheet lessStyleSheet = LessCSSParser.getLessStyleSheet(new LessSource.StringSource(stringOfRootNode));
					ca.concordia.cssanalyser.cssmodel.StyleSheet compileLESSStyleSheet = LessHelper.compileLESSStyleSheet(lessStyleSheet);
					if (compileLESSStyleSheet.getAllSelectors().iterator().hasNext()) {
						Selector callingSelector = compileLESSStyleSheet.getAllSelectors().iterator().next();
						callingSelector.removeDeclaration(callingSelector.getDeclarations().iterator().next());
						selectorsToReturn.add(callingSelector);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			callingSelectors = selectorsToReturn;
			
		}

		return callingSelectors;
	}

	private List<List<ASTCssNode>> getParentRuleSets(List<LessMixinCall> mixinCallInfo, Set<LessMixinCall> alreadyVisited) {
		
		List<List<ASTCssNode>> toReturn = new ArrayList<>();
		
		ASTCssNode parentStructure = getParentStructure();
		if (parentStructure == null) {
			return toReturn;
		}
		
		List<ASTCssNode> currentParentNodes = new ArrayList<>();
		
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
						return toReturn;
					}
				} else {
					clone.getBody().addMember(lastOne);
				}
				clone.getBody().configureParentToAllChilds();
				currentParentNodes.add(clone);
				lastOne = clone;
			} else if (parentStructure instanceof ReusableStructure) {
				ReusableStructure parentReusableStructure = (ReusableStructure) parentStructure;
				String mixinName = parentReusableStructure.getNamesAsStrings().toString();
				mixinName = mixinName.substring(1, mixinName.length() - 1);
				if (!this.getName().equals(mixinName)) {
					for (LessMixinCall lessMixinCall : mixinCallInfo) {
						if (!alreadyVisited.contains(lessMixinCall)) {
							// This is dangerous! FIXME
							if (lessMixinCall.getName().equals(mixinName)) {
								alreadyVisited.add(lessMixinCall);
								toReturn.addAll(lessMixinCall.getParentRuleSets(mixinCallInfo, alreadyVisited));
								alreadyVisited.remove(lessMixinCall);
							}
						}
					}
				}		
			}
			if (parentStructure.getParent() instanceof GeneralBody) {
				parentStructure = parentStructure.getParent().getParent();
			} else if (parentStructure.getParent() instanceof StyleSheet) {
				break;
			} else if (parentStructure.getParent() instanceof DetachedRuleset ||
					parentStructure.getParent() instanceof MixinReference) {
				return toReturn;
			} else {
				throw new RuntimeException("What is parent structure " + parentStructure.getParent());
			}
		} while (!(parentStructure instanceof StyleSheet));
		
		if (!currentParentNodes.isEmpty()) {
			if (toReturn.size() == 0) {
				toReturn.add(currentParentNodes);
			} else {
				// Prepend
				for (int i = 0; i < toReturn.size(); i++) {
					List<ASTCssNode> list = toReturn.get(i);
					List<ASTCssNode> newList = new ArrayList<>();
					GeneralBody body = ((RuleSet)list.get(0)).getBody();//((RuleSet)currentParentNodes.get(0)).getBody();
					body.removeAllMembers();
					body.addMember(currentParentNodes.get(currentParentNodes.size() - 1));
					body.configureParentToAllChilds();
					newList.addAll(currentParentNodes);
					newList.addAll(list);
					toReturn.set(i, newList);
				}
			}
		}
		
		return toReturn;
	}
}
