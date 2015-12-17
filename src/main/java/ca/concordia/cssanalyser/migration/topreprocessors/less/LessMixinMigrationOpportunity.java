package ca.concordia.cssanalyser.migration.topreprocessors.less;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorNode;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorType;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinDeclaration;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinParameter;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinParameterizedValue;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinValue;
import ca.concordia.cssanalyser.parser.ParseException;
import ca.concordia.cssanalyser.parser.less.LessCSSParser;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;

public class LessMixinMigrationOpportunity extends MixinMigrationOpportunity<com.github.sommeri.less4j.core.ast.StyleSheet> {
	
	public LessMixinMigrationOpportunity(Iterable<Selector> forSelectors, StyleSheet forStyleSheet) {
		super(forSelectors, forStyleSheet, PreprocessorType.LESS);
	}

	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		toReturn.append(getMixinSignature());
		toReturn.append(" {").append(System.lineSeparator());
		for (Iterator<MixinDeclaration> iterator = getAllMixinDeclarations().iterator(); iterator.hasNext(); ) {
			MixinDeclaration mixinDeclaration = iterator.next();
			toReturn.append("\t").append(mixinDeclaration.getPropertyName()).append(": ");
			List<MixinValue> mixinValues = new ArrayList<>();
			for (MixinValue mixinValue : mixinDeclaration.getMixinValues()) {
				mixinValues.add(mixinValue);
			}
			for (int i = 0; i < mixinValues.size(); i++) {
				toReturn.append(mixinValues.get(i));
				if (i < mixinValues.size() - 1 && !mixinValues.get(i + 1).toString().trim().equals(","))
					toReturn.append(" ");
			}
			if (iterator.hasNext())
				toReturn.append(";");
			toReturn.append(System.lineSeparator());
		}
		toReturn.append("}");
		return toReturn.toString();
	}

	@Override
	public String getMixinSignature() {
		StringBuilder toReturn = new StringBuilder();
		toReturn.append(this.getMixinName()).append("(");
		for(Iterator<MixinParameter> iterator = getParameters().iterator(); iterator.hasNext(); ) {
			toReturn.append("@").append(iterator.next().getName());
			if (iterator.hasNext())
				toReturn.append("; ");
		}
		toReturn.append(")");
		return toReturn.toString();
	}

	@Override
	public String getMixinReferenceString(Selector selector) {
		Map<MixinParameter, MixinParameterizedValue> paramToValMap = getParameterizedValues(selector);
		StringBuilder mixinReferenceStringBuilder = new StringBuilder(getMixinName());
		mixinReferenceStringBuilder.append("(");
		// Preserve the order of parameters
		for (Iterator<MixinParameter> paramterIterator = getParameters().iterator(); paramterIterator.hasNext(); ) {
			MixinParameter parameter = paramterIterator.next();
			MixinParameterizedValue value = paramToValMap.get(parameter);
			//mixinReferenceStringBuilder.append(parameter.toString()).append(": ");
			for (Iterator<DeclarationValue> declarationValueIterator = value.getForValues().iterator(); declarationValueIterator.hasNext(); ) {
				mixinReferenceStringBuilder.append(declarationValueIterator.next().getValue());
				if (declarationValueIterator.hasNext())
					mixinReferenceStringBuilder.append(", ");
			}
			if (paramterIterator.hasNext())
				mixinReferenceStringBuilder.append("; ");
		}
		mixinReferenceStringBuilder.append(");");
		return mixinReferenceStringBuilder.toString();
	}
	
	@Override
	public boolean preservesPresentation() {
		com.github.sommeri.less4j.core.ast.StyleSheet resultingLESSStyleSheet = this.apply();
		StyleSheet afterMigration;
		try {
			afterMigration = LessHelper.compileLESSStyleSheet(resultingLESSStyleSheet);
		} catch (Less4jException e) {
			String message = "Error in parsing compiling mixin opportunity." + System.lineSeparator() + e.getMessage();
			FileLogger.getLogger(this.getClass()).warn(message);
			return false;
		}
		/*
		 * Find each selector in the second StyleSheet,
		 * then see if the corresponding selectors style the same properties
		 * with the same values.
		 * We follow the simplistic way of finding the corresponding 
		 * selectors because a Mixin migration opportunity
		 * does not change the selector names and relative positions of them. 
		 */
		Set<Selector> checkedSelectorsIn2 = new HashSet<>(); // Don't map one selector two times. 
		for (Selector selector1 : getStyleSheet().getAllSelectors()) {
			boolean selectorFound = false;
			for (Selector selector2 : afterMigration.getAllSelectors()) {
				if (checkedSelectorsIn2.contains(selector2))
					continue;
				if (selector1.selectorEquals(selector2)) { // Selector names should be the same (including class names, ID, Pseudos, etc).
					checkedSelectorsIn2.add(selector2);
					// Now check if they style similarly
					Map<String, Declaration> individualDeclarations1 = new HashMap<>();
					for (Declaration declaration : selector1.getFinalStylingIndividualDeclarations()) {
						individualDeclarations1.put(declaration.getProperty(), declaration);
					}
					
					Map<String, Declaration> individualDeclarations2 = new HashMap<>();
					for (Declaration declaration : selector2.getFinalStylingIndividualDeclarations()) {
						individualDeclarations2.put(declaration.getProperty(), declaration);
					}
					
					for (String property : individualDeclarations1.keySet()) {
						if (!individualDeclarations2.containsKey(property) ||
								!individualDeclarations2.get(property).declarationEquals(individualDeclarations1.get(property)))
							return false;
					}
					selectorFound = true;
					break;
				}
			}
			if (!selectorFound)
				return false;
		}
		
		return true;
	}
	
	@Override
	public com.github.sommeri.less4j.core.ast.StyleSheet apply() {
		
		try {
			StyleSheet styleSheet = getStyleSheet();
			com.github.sommeri.less4j.core.ast.StyleSheet lessStyleSheet = LessCSSParser.getLessParserFromStyleSheet(styleSheet);
			
			LessPreprocessorNodeFinder nodeFinder = new LessPreprocessorNodeFinder(lessStyleSheet);

			// 1- Remove the declarations being parameterized
			List<PreprocessorNode<ASTCssNode>> nodesToBeRemoved = new ArrayList<>();
			for (Declaration declaration : getDeclarationsToBeRemoved()) {
				nodesToBeRemoved.addAll(getDeclarationNodesToBeRemoved(nodeFinder, declaration));
			}
			
			for (PreprocessorNode<ASTCssNode> node : nodesToBeRemoved) {
				node.getParent().deleteChild(node);
			}

			// 2- Add the necessary declarations to the involved selectors
			for (Declaration declaration : getDeclarationsToBeAdded()) {
				Selector selector = declaration.getSelector().getOriginalSelector();
				PreprocessorNode<ASTCssNode> selectorNode = nodeFinder.perform(selector.getLocationInfo().getOffset(), selector.getLocationInfo().getLength());
				String nodeString = declaration.toString();
				ASTCssNode resultingNode = LessHelper.getLessNodeFromLessString(nodeString);
				selectorNode.addChild(new LessPreprocessorNode(resultingNode));
			}
			
			// 3- Add the Mixin node
			com.github.sommeri.less4j.core.ast.StyleSheet root = LessCSSParser.getLessStyleSheet(new LessSource.StringSource(toString()));
			ASTCssNode mixin = root.getChilds().get(0);

			lessStyleSheet.getMembers().add(0, mixin);
				
			// 4- Add the Mixin call to the corresponding selectors
			for (Selector involvedSelector : getInvolvedSelectors()) {									
				String nodeString = getMixinReferenceString(involvedSelector);
				ASTCssNode resultingNode = LessHelper.getLessNodeFromLessString(nodeString);
				PreprocessorNode<ASTCssNode> node = nodeFinder.perform(involvedSelector.getLocationInfo().getOffset(), involvedSelector.getLocationInfo().getLength()); 
				node.addChild(new LessPreprocessorNode(resultingNode));
			}

			
			return lessStyleSheet;
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

	private List<PreprocessorNode<ASTCssNode>> getDeclarationNodesToBeRemoved(LessPreprocessorNodeFinder nodeFinder, Declaration declaration) {
		List<PreprocessorNode<ASTCssNode>> nodesToBeRemoved = new ArrayList<PreprocessorNode<ASTCssNode>>();
		if (declaration.isVirtualIndividualDeclarationOfAShorthand()) {
			ShorthandDeclaration parentShorthand = declaration.getParentShorthand();
			if (!(parentShorthand.isVirtual()))
				declaration = parentShorthand;
		} 
		PreprocessorNode<ASTCssNode> node = nodeFinder.perform(declaration.getLocationInfo().getOffset(), declaration.getLocationInfo().getLength()); 
		if (!node.isNull())
			nodesToBeRemoved.add(node);

		if (declaration instanceof ShorthandDeclaration) {
			ShorthandDeclaration shorthandDeclaration = (ShorthandDeclaration)declaration;
			if (shorthandDeclaration.isVirtual()) {
				for (Declaration d : shorthandDeclaration.getIndividualDeclarations()) {
					nodesToBeRemoved.addAll(getDeclarationNodesToBeRemoved(nodeFinder, d));
				}
			}
		}
		return nodesToBeRemoved;
	}
}
