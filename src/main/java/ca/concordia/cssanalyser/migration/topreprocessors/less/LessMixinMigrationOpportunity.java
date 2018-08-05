package ca.concordia.cssanalyser.migration.topreprocessors.less;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.RuleSet;

import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.cssmodel.LocationInfo;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.MultiValuedDeclaration;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorNode;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorType;
import ca.concordia.cssanalyser.migration.topreprocessors.TransformationStatus;
import ca.concordia.cssanalyser.migration.topreprocessors.TransformationStatus.TransformationStatusFlag;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinDeclaration;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinParameter;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinParameterizedValue;
import ca.concordia.cssanalyser.parser.ParseException;
import ca.concordia.cssanalyser.parser.less.LessCSSParser;

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
			toReturn.append("\t");
			toReturn.append(mixinDeclaration.getMixinDeclarationString());
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
		boolean mustAddSemiColon = false;
		mixinReferenceStringBuilder.append("(");
		// Preserve the order of parameters
		for (Iterator<MixinParameter> paramterIterator = getParameters().iterator(); paramterIterator.hasNext(); ) {
			MixinParameter parameter = paramterIterator.next();
			MixinParameterizedValue value = paramToValMap.get(parameter);
			//mixinReferenceStringBuilder.append(parameter.toString()).append(": ");
			if (value.getForValues() != null) {
				for (Iterator<DeclarationValue> declarationValueIterator = value.getForValues().iterator(); declarationValueIterator.hasNext(); ) {
					String declarationValue = declarationValueIterator.next().getValue();
					if ((declarationValue.contains("\\") || declarationValue.contains("/")) &&
							!(declarationValue.startsWith("'") && declarationValue.endsWith("'")) &&
							!(declarationValue.replace(" ", "").startsWith("url("))) {
						declarationValue = escapeValue(declarationValue);
					}
					mixinReferenceStringBuilder.append(declarationValue);
					if (declarationValueIterator.hasNext()) {
						if (MultiValuedDeclaration.isCommaSeparated(value.getForDeclaration().getProperty())) {
							mixinReferenceStringBuilder.append(", ");
							mustAddSemiColon = true;
						} else {
							mixinReferenceStringBuilder.append(" ");
						}
					}
				}
			} else {
				mixinReferenceStringBuilder.append("''");
			}
			if (mustAddSemiColon || paramterIterator.hasNext()) {
				mixinReferenceStringBuilder.append("; ");
			}
		}
		mixinReferenceStringBuilder.append(");");
		return mixinReferenceStringBuilder.toString();
	}
	
	private String escapeValue(String value) {
		return "~'" + value + "'";
	}

	@Override
	public TransformationStatus preservesPresentation() {
		TransformationStatus statusToReturn = new TransformationStatus();
		com.github.sommeri.less4j.core.ast.StyleSheet resultingLESSStyleSheet = this.apply();
		StyleSheet afterMigration = null;
		String codeBefore = getStyleSheet().toString();
		String codeAfter = new LessPrinter().getString(resultingLESSStyleSheet);

		try {
			afterMigration = LessHelper.compileLESSStyleSheet(resultingLESSStyleSheet, true); // Avoid re-reading from physical file
		} catch (Exception e) {
			String msg = "Error in compiling the resulting style sheet after applying mixin migration opportunity." 
					+ System.lineSeparator() + e.getMessage();
			statusToReturn.addStatusEntry(codeBefore, codeAfter, TransformationStatusFlag.FATAL, msg);
		}
		if (afterMigration == null || "".equals(afterMigration.toString())) {
			String msg = "Resulting StyleSheet is empty, possible transpilation errors.";
			statusToReturn.addStatusEntry(codeBefore, codeAfter, TransformationStatusFlag.FATAL, msg);
		}

		if (statusToReturn.isOK()) {
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
				Iterable<Declaration> selector1FinalStylingIndividualDeclarations = selector1.getFinalStylingIndividualDeclarations();
				if (selector1FinalStylingIndividualDeclarations.iterator().hasNext()) { // Skip empty selectors, because less will not print them
					boolean selectorFound = false;
					for (Selector selector2 : afterMigration.getAllSelectors()) {
						if (checkedSelectorsIn2.contains(selector2))
							continue;
						if (selector1.selectorEquals(selector2)) { // Selector names should be the same (including class names, ID, Pseudos, etc).
							checkedSelectorsIn2.add(selector2);
							// Now check if they style similarly
							Map<String, Declaration> individualDeclarations1 = new HashMap<>();

							for (Declaration declaration : selector1FinalStylingIndividualDeclarations) {
								individualDeclarations1.put(declaration.getProperty(), declaration);
							}

							Map<String, Declaration> individualDeclarations2 = new HashMap<>();
							for (Declaration declaration : selector2.getFinalStylingIndividualDeclarations()) {
								individualDeclarations2.put(declaration.getProperty(), declaration);
							}

							for (String property : individualDeclarations1.keySet()) {
								if (!individualDeclarations2.containsKey(property)) {
									String msg = "Declaration not found for " + property;
									statusToReturn.addStatusEntry(codeBefore, codeAfter, TransformationStatusFlag.FATAL, msg);
								} else {
									Declaration declaration2 = individualDeclarations2.get(property);
									Declaration declaration1 = individualDeclarations1.get(property);
									if (!declaration2.declarationIsEquivalent(declaration1)) {
										String msg = String.format("Declarations are not equivalent: %s AND %s ", declaration1, declaration2);
										statusToReturn.addStatusEntry(codeBefore, codeAfter, TransformationStatusFlag.FATAL, msg);
										if ((declaration2.isImportant() && !declaration1.isImportant()) ||
												(!declaration2.isImportant() && declaration1.isImportant())) {
											msg = String.format("!important is different: %s AND %s ", declaration1, declaration2);
											statusToReturn.addStatusEntry(codeBefore, codeAfter, TransformationStatusFlag.FATAL, msg);
											break; // fail fast, we could continue
										}
									}
								}
							}
							selectorFound = true;
							break;
						}
					}
					if (!selectorFound) {
						String msg = "PRESENTATION: Selector not found: " + selector1;
						statusToReturn.addStatusEntry(codeBefore, codeAfter, TransformationStatusFlag.FATAL, msg);
					}
				}
			}
		}

		return statusToReturn;
	}
	
	@Override
	public com.github.sommeri.less4j.core.ast.StyleSheet apply() {
		
		try {
			StyleSheet styleSheet = getStyleSheet();
			com.github.sommeri.less4j.core.ast.StyleSheet lessStyleSheet = LessCSSParser.getLessParserFromStyleSheet(styleSheet);
			
			LessPreprocessorNodeFinder nodeFinder = new LessPreprocessorNodeFinder(lessStyleSheet.clone());

			// 1- Remove the declarations being parameterized
			List<PreprocessorNode<ASTCssNode>> nodesToBeRemoved = new ArrayList<>();
			for (Declaration declaration : getDeclarationsToBeRemoved()) {
				nodesToBeRemoved.add(nodeFinder.perform(declaration.getLocationInfo().getOffset(), declaration.getLocationInfo().getLength()));
			}
			
			for (PreprocessorNode<ASTCssNode> node : nodesToBeRemoved) {
				node.getParent().deleteChild(node);
			}

			// 2- Add the necessary declarations to the involved selectors
			for (Declaration declaration : getDeclarationsToBeAdded()) {
				Selector selector = declaration.getSelector();
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
				String mixinCallString = getMixinReferenceString(involvedSelector);
				ASTCssNode mixinCallNode = LessHelper.getLessNodeFromLessString(mixinCallString);
				RuleSet ruleSetNode = 
						(RuleSet)nodeFinder.perform(involvedSelector.getLocationInfo().getOffset(), involvedSelector.getLocationInfo().getLength()).getRealNode();
				try {
					Declaration[] positionsMap = getMixinCallPosition(involvedSelector);

					if (positionsMap == null) { // don't touch anything, just add the call to the end
						ruleSetNode.getBody().addMember(mixinCallNode);
					} else {

						// Rearrange declarations inside the selector to satisfy dependencies
						Map<LocationInfo, com.github.sommeri.less4j.core.ast.Declaration> declarations = new HashMap<>();
						// Remove all the declarations
						for (ASTCssNode declarationNode : ruleSetNode.getBody().membersByType(ASTCssNodeType.DECLARATION)) {
							LocationInfo locationInfo = LessPreprocessorNodeFinder.getLocationInfoForLessASTCssNode(declarationNode);
							declarations.put(locationInfo, (com.github.sommeri.less4j.core.ast.Declaration)declarationNode);
							ruleSetNode.getBody().removeMember(declarationNode);
						}

						for (int i = 0; i < positionsMap.length; i++) {
							Declaration declaration = positionsMap[i];
							ASTCssNode nodeToAdd = null;
							if ("MIXIN".equals(declaration.getProperty().toUpperCase())) {
								nodeToAdd  = mixinCallNode;
							} else {
								nodeToAdd = declarations.get(declaration.getLocationInfo()); 
							}
							if (nodeToAdd != null)
								ruleSetNode.getBody().addMember(nodeToAdd);	
							else 
								throw new Exception ("Couldn't find declaration " + declaration.toString());
						}
						ruleSetNode.getBody().configureParentToAllChilds();
					}
				} catch (Exception ex){
					FileLogger.getLogger(LessMixinMigrationOpportunity.class).warn(ex.getMessage() + "\n" + this.toString());
					ruleSetNode.getBody().addMember(mixinCallNode);
				}
			}
						
			return lessStyleSheet;
			
		} catch (ParseException e) {
			
			FileLogger.getLogger(LessMixinMigrationOpportunity.class).warn(e.getMessage());
		}
		
		return null;
		
	}
	
}
