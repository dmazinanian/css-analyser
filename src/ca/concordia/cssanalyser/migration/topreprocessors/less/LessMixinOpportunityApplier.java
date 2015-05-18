package ca.concordia.cssanalyser.migration.topreprocessors.less;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorNode;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunityApplier;
import ca.concordia.cssanalyser.parser.ParseException;
import ca.concordia.cssanalyser.parser.less.LessCSSParser;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;

public class LessMixinOpportunityApplier implements MixinMigrationOpportunityApplier<com.github.sommeri.less4j.core.ast.StyleSheet> {

	@Override
	public com.github.sommeri.less4j.core.ast.StyleSheet apply(MixinMigrationOpportunity opportunity, StyleSheet styleSheet) {
		
		try {
			
			com.github.sommeri.less4j.core.ast.StyleSheet lessStyleSheet = LessCSSParser.getLessParserFromStyleSheet(styleSheet);
			
			LessPreprocessorNodeFinder nodeFinder = new LessPreprocessorNodeFinder(lessStyleSheet);

			// 1- Remove the declarations being parameterized
			List<PreprocessorNode<ASTCssNode>> nodesToBeRemoved = new ArrayList<>();
			for (Declaration declaration : opportunity.getDeclarationsToBeRemoved()) {
				nodesToBeRemoved.addAll(getDeclarationNodesToBeRemoved(nodeFinder, declaration));
			}
			
			for (PreprocessorNode<ASTCssNode> node : nodesToBeRemoved) {
				node.getParent().deleteChild(node);
			}
			
			/*
			 * If you are removing a shorthand because of a virtual individual,
			 * add the remaining individuals to the selector 
			 */
			Map<ShorthandDeclaration, Set<Declaration>> parentShortandsToIndividualsMap = new HashMap<>();
			for (Declaration declaration : opportunity.getDeclarationsToBeRemoved()) {
				if (declaration.isVirtualIndividualDeclarationOfAShorthand()) {
					ShorthandDeclaration parentShorthand = declaration.getParentShorthand();
					//if (!parentShorthand.isVirtual()) {
						Set<Declaration> individualsOfTheSameParent = parentShortandsToIndividualsMap.get(parentShorthand);
						if (individualsOfTheSameParent == null) {
							individualsOfTheSameParent = new HashSet<Declaration>();
							parentShortandsToIndividualsMap.put(parentShorthand, individualsOfTheSameParent);
						}
						individualsOfTheSameParent.add(declaration);
					//}
				}
			}
			
			List<Declaration> declarationsToBeAdded = new ArrayList<>();
			for (ShorthandDeclaration parentShorthand : parentShortandsToIndividualsMap.keySet()) {
				Set<Declaration> individualsToBeRemoved = parentShortandsToIndividualsMap.get(parentShorthand);
				for (Declaration individual : parentShorthand.getIndividualDeclarations()) {
					if (!individualsToBeRemoved.contains(individual) &&
							!parentShorthand.getSelector().getOriginalSelector().containsDeclaration(individual)) {
						declarationsToBeAdded.add(individual);
					}
				}
			}
			
			for (Declaration declaration : declarationsToBeAdded) {
				Selector selector = declaration.getSelector().getOriginalSelector();
				PreprocessorNode<ASTCssNode> selectorNode = nodeFinder.perform(selector.getLocationInfo().getOffset(), selector.getLocationInfo().getLength());
				String nodeString = declaration.toString();
				ASTCssNode resultingNode = LessHelper.getLessNodeFromLessString(nodeString);
				selectorNode.addChild(new LessPreprocessorNode(resultingNode));
			}
			
			// 2- Add the Mixin node
			com.github.sommeri.less4j.core.ast.StyleSheet root = LessCSSParser.getLessStyleSheet(new LessSource.StringSource(opportunity.toString()));
			ASTCssNode mixin = root.getChilds().get(0);

			lessStyleSheet.getMembers().add(0, mixin);
				
			// 3- Add the Mixin call to the corresponding selectors
			for (Selector involvedSelector : opportunity.getInvolvedSelectors()) {									
				String nodeString = opportunity.getMixinReferenceString(involvedSelector);
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
