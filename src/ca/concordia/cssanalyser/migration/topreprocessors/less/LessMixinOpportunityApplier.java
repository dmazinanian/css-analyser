package ca.concordia.cssanalyser.migration.topreprocessors.less;

import java.util.Iterator;
import java.util.Map;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorNode;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunityApplier;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinParameter;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinParameterizedValue;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
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


			// 1- Remove declarations from the original style sheet
			for (String property : opportunity.getProperties()) {
				for (Declaration declaration : opportunity.getDeclarationsForProperty(property)) {
					PreprocessorNode<ASTCssNode> node = nodeFinder.perform(declaration.getLocationInfo().getOffset(), declaration.getLocationInfo().getLenghth()); 
					node.getParent().deleteChild(node);
				}
			}
			
			// 2- Add the mixin node
			com.github.sommeri.less4j.core.ast.StyleSheet root = LessCSSParser.getLessStyleSheet(new LessSource.StringSource(opportunity.toString()));
			ASTCssNode mixin = root.getChilds().get(0);
			
			lessStyleSheet.getMembers().add(0, mixin);
			
			// 3- Add the mixin references to the corresponding selectors
			for (Selector s : opportunity.getInvolvedSelectors()) {
				Map<MixinParameter, MixinParameterizedValue> paramToValMap = opportunity.getParameterizedValues(s);
				if (paramToValMap.size() == 0) // If a selector does not play a role in differences
					continue;
				StringBuilder mixinReferenceStringBuilder = new StringBuilder(opportunity.getMixinName());
				mixinReferenceStringBuilder.append("(");
				// Preserve the order of parameters
				for (Iterator<MixinParameter> paramterIterator = opportunity.getParameters().iterator(); paramterIterator.hasNext(); ) {
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
				String mixinReferenceString = ".fake { " + mixinReferenceStringBuilder.toString() + "}" ;
				root = LessCSSParser.getLessStyleSheet(new LessSource.StringSource(mixinReferenceString));
				ASTCssNode mixinReference = root.getChilds().get(0).getChilds().get(1).getChilds().get(1); // :)
				PreprocessorNode<ASTCssNode> node = nodeFinder.perform(s.getLocationInfo().getOffset(), s.getLocationInfo().getLenghth()); 
				node.addChild(new LessPreprocessorNode(mixinReference));
			}

			
			return lessStyleSheet;
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	 
}
