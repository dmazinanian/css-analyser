package ca.concordia.cssanalyser.migration.topreprocessors.less;

import java.util.ArrayList;
import java.util.List;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
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

			for (Selector involvedSelector : opportunity.getInvolvedSelectors()) {
				
				List<PreprocessorNode<ASTCssNode>> nodesToBeRemoved = new ArrayList<>();
				// 1- Remove the declarations being parameterized
				for (Declaration declaration : opportunity.getDeclarationsToBeRemoved(involvedSelector)) {
					PreprocessorNode<ASTCssNode> node = nodeFinder.perform(declaration.getLocationInfo().getOffset(), declaration.getLocationInfo().getLenghth()); 
					if (!node.isNull())
						nodesToBeRemoved.add(node);
				}
				
				for (PreprocessorNode<ASTCssNode> node : nodesToBeRemoved) {
					node.getParent().deleteChild(node);
				}
				
			}
			
			// 2- Add the mixin node
			com.github.sommeri.less4j.core.ast.StyleSheet root = LessCSSParser.getLessStyleSheet(new LessSource.StringSource(opportunity.toString()));
			ASTCssNode mixin = root.getChilds().get(0);

			lessStyleSheet.getMembers().add(0, mixin);
				
			for (Selector involvedSelector : opportunity.getInvolvedSelectors()) {						
				// 3- Add the mixin call to the corresponding selectors
				
				String mixinReferenceString = ".fake { " + opportunity.getMixinReferenceString(involvedSelector) + "}" ;
				root = LessCSSParser.getLessStyleSheet(new LessSource.StringSource(mixinReferenceString));
				ASTCssNode mixinReference = root.getChilds().get(0).getChilds().get(1).getChilds().get(1); // :)
				PreprocessorNode<ASTCssNode> node = nodeFinder.perform(involvedSelector.getLocationInfo().getOffset(), involvedSelector.getLocationInfo().getLenghth()); 
				// Well, you are adding it at the end, which may not be correct!
				node.addChild(new LessPreprocessorNode(mixinReference));
			}

			
			return lessStyleSheet;
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	 
}
