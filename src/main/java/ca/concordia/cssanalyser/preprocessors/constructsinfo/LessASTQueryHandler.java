package ca.concordia.cssanalyser.preprocessors.constructsinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ArgumentDeclaration;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Extend;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.Import;
import com.github.sommeri.less4j.core.ast.InterpolableName;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.ast.Media;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.NestedSelectorAppender;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.PseudoClass;
import com.github.sommeri.less4j.core.ast.PseudoElement;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.SelectorPart;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.ast.VariableDeclaration;

import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessPrinter;
import ca.concordia.cssanalyser.preprocessors.util.less.ImportInliner;


public class LessASTQueryHandler {
	
	private static Logger LOGGER = FileLogger.getLogger(LessASTQueryHandler.class);
	
	private StyleSheet lessStyleSheet;
	private final Map<LessImport, LessASTQueryHandler> importedStyleSheetsData;

	private LessASTQueryHandler(StyleSheet lessStyleSheet, Map<LessImport, LessASTQueryHandler> importedStyleSheetsData) {
		this.lessStyleSheet = lessStyleSheet;
		this.importedStyleSheetsData = importedStyleSheetsData;
		handleImports();
	}
	
	public LessASTQueryHandler(StyleSheet lessStyleSheet) {
		this(lessStyleSheet, new LinkedHashMap<>());
	}
	
	private void handleImports() {
		
		List<Import> allImports = ImportInliner.getAllImports(this.lessStyleSheet);
		for (Import importNode : allImports) {
			try {
				LessImport lessImport = new LessImport(importNode, this.lessStyleSheet);
				if (lessImport.couldFindURL()) {
					LessASTQueryHandler lessASTQueryHandler = new LessASTQueryHandler(lessImport.getImportedStyleSheet(), this.importedStyleSheetsData);
					importedStyleSheetsData.put(lessImport, lessASTQueryHandler);
				} else {
					LOGGER.warn(String.format("File %s not found for importing.", lessImport.getUrl()));
				}
			}
			catch (NotImplementedException niex) {
				niex.printStackTrace();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
	}

	public List<LessMixinCall> getMixinCallInfo() {
		
		Set<LessImport> importsToSkip = new HashSet<>();
		return getMixinCallInfo(importsToSkip);
		
	}
	
	private List<LessMixinCall> getMixinCallInfo(Set<LessImport> importsToSkip) {

		List<LessMixinCall> toReturn = new ArrayList<>();
		
		for (ASTCssNode node : getAllChilds(lessStyleSheet)) {

//			if (node instanceof RuleSet || node instanceof ReusableStructure) {
//
//				GeneralBody body = null;
//				if (node instanceof RuleSet)
//					body = ((RuleSet)node).getBody();
//				else
//					body = ((ReusableStructure)node).getBody();
//
//				if (body == null)
//					continue;

				//for (ASTCssNode child : body.getChilds()) {
					ASTCssNode child = node;
					if (child instanceof MixinReference) {
						MixinReference reference = (MixinReference)child;

						int numberOfMultiValuedArguments = 0;

						for (ASTCssNode ex : reference.getChilds()) {
							if (ex instanceof ListExpression) {
								++numberOfMultiValuedArguments;
							}
						}
						
						LessMixinCall lessMixinCall = new LessMixinCall(reference, numberOfMultiValuedArguments, this.lessStyleSheet);
						LessMixinDeclaration lessMixinDeclaration = getMixinDeclarationForMixinCall(lessMixinCall);
						lessMixinCall.setMixinDeclaration(lessMixinDeclaration);
						toReturn.add(lessMixinCall);

					}
				}
//			}
//		}
		
		for (LessImport lessImport : this.importedStyleSheetsData.keySet()) {
			if (!importsToSkip.contains(lessImport)) {
				importsToSkip.add(lessImport);
				toReturn.addAll(this.importedStyleSheetsData.get(lessImport).getMixinCallInfo(importsToSkip));
			}
		}
		
		return toReturn;
	
	}
	

	private LessMixinDeclaration getMixinDeclarationForMixinCall(LessMixinCall lessMixinCall) {
		
		List<LessMixinDeclaration> mixinDeclarationInfo = getMixinDeclarationInfo();
		for (LessMixinDeclaration declaration : mixinDeclarationInfo) {
			if (declaration.getMixinName().equals(lessMixinCall.getName())) {
				return declaration;
			}
		}
		
		List<LessSelector> selectorsInfo = getSelectorsInfo();
		for (LessSelector selectorInfo : selectorsInfo) {
			if (selectorInfo.getFullyQualifiedName().equals(lessMixinCall.getName()))
				return getLessMixinDeclarationFromReusableStructure(((RuleSet)selectorInfo.getNode()).convertToReusableStructure());
		}

		return null;
		
	}
	
	public List<LessMixinDeclaration> getAllMixinDeclarationsAndSelectorsCalledAsMixin() {
		
		Set<LessMixinDeclaration> allMixinDeclarations = new HashSet<>();
		
		allMixinDeclarations.addAll(getMixinDeclarationInfo());
		for (LessMixinCall lessMixinCall : getMixinCallInfo()) {
			if (lessMixinCall.getMixinDeclaration() != null) {
				allMixinDeclarations.add(lessMixinCall.getMixinDeclaration());
			}
		}
		
		return new ArrayList<>(allMixinDeclarations);
	}

	public List<LessMixinDeclaration> getMixinDeclarationInfo() {
		
		Set<LessImport> importsToSkip = new HashSet<>();
		return getMixinDeclarationInfo(importsToSkip);
		
	}
	
	public List<LessMixinDeclaration> getMixinDeclarationInfo(Set<LessImport> importsToSkip) {
		
		List<LessMixinDeclaration> toReturn = new ArrayList<>();
		
		for (ASTCssNode node : getAllChilds(lessStyleSheet)) {

			if (node instanceof ReusableStructure) {

				ReusableStructure reusableNode = (ReusableStructure)node;
				
				LessMixinDeclaration mixinDeclarationInfo = getLessMixinDeclarationFromReusableStructure(reusableNode);				
				
				toReturn.add(mixinDeclarationInfo);
				
			}
		}
		
		for (LessImport lessImport : this.importedStyleSheetsData.keySet()) {
			if (!importsToSkip.contains(lessImport)) {
				importsToSkip.add(lessImport);
				toReturn.addAll(this.importedStyleSheetsData.get(lessImport).getMixinDeclarationInfo(importsToSkip));
			}
		}
		
		return toReturn;
	
	}

	private LessMixinDeclaration getLessMixinDeclarationFromReusableStructure(ReusableStructure reusableNode) {
		LessMixinDeclaration mixinDeclarationInfo = new LessMixinDeclaration(reusableNode, this.lessStyleSheet);
		
		Map<String, Set<String>> propertyToCountMap = new HashMap<>();
		
		Set<String> parameters = new HashSet<>();
		for (ASTCssNode param : reusableNode.getParameters()) {
			if (param instanceof ArgumentDeclaration) {
				ArgumentDeclaration argumentDeclaration = (ArgumentDeclaration)param;
				parameters.add(argumentDeclaration.getVariable().getName());
			} else if (param instanceof IdentifierExpression) {
				IdentifierExpression identifierExpression = (IdentifierExpression) param;
				parameters.add(identifierExpression.getValue());
			} else if (param instanceof NumberExpression) {
				NumberExpression numberExpression = (NumberExpression) param;
				parameters.add(numberExpression.getOriginalString());
			} else {
				LOGGER.warn("Param is " + param.getClass().getName());
			}
		}

		if (reusableNode.getBody() != null)
			countVariablesInside(reusableNode.getBody(), propertyToCountMap, mixinDeclarationInfo, parameters);

		for (String parentAndProperty : propertyToCountMap.keySet()) {
			if (propertyToCountMap.get(parentAndProperty).size() > 1) {
				String property = parentAndProperty.split("\\\\")[1];
				// check if they are different
				boolean nonEqualFound = false;
				for (String p2 : propertyToCountMap.get(parentAndProperty)) {
					if (!property.equals(p2)) {
						nonEqualFound = true;
						break;
					}
				}
				if (nonEqualFound) {
					mixinDeclarationInfo.increaseNumberOfUniqueCrossBrowserDeclarations(1);
				} else {
					mixinDeclarationInfo.increaseNumberOfNonCrossBrowserDeclarations(1);
				}
			} else {
				String first = propertyToCountMap.get(parentAndProperty).iterator().next();
				if (!first.equals(Declaration.getNonVendorProperty(first)))
					mixinDeclarationInfo.increaseNumberOfUniqueCrossBrowserDeclarations(1);
				else
					mixinDeclarationInfo.increaseNumberOfNonCrossBrowserDeclarations(1);
			}
		}
		return mixinDeclarationInfo;
	}
	
	private void countVariablesInside(GeneralBody body, Map<String, Set<String>> propertyToCountMap, LessMixinDeclaration mixinDeclarationInfo, Set<String> parameters) {
		Map<String, Set<String>> parameterToDeclarationMap = new HashMap<>();
		for (com.github.sommeri.less4j.core.ast.Declaration declaration : getAllDeclarations(body)) {

			mixinDeclarationInfo.increaseNumberOfDeclarations(1);
			boolean useOfParameterFound = false,
					useOfVariableFound = false;
			Expression expression = declaration.getExpression();
			for (ASTCssNode c : getAllChilds(expression)) {
				if (c instanceof Variable) {
					Variable variable = (Variable)c;
					useOfVariableFound = true;
					if (parameters.contains(variable.getName())) {
						useOfParameterFound = true;
						Set<String> set = parameterToDeclarationMap.get(variable.getName());
						if (set == null) {
							set = new HashSet<>();
							parameterToDeclarationMap.put(variable.getName(), set);
						}
						set.add(declaration.getNameAsString());
					} else {
						boolean localFound = false;
						for (ASTCssNode astCssNode : getAllChilds(body)) {
							if (astCssNode instanceof VariableDeclaration) {
								VariableDeclaration variableDeclaration = (VariableDeclaration) astCssNode;
								if (variable.getName().equals(variableDeclaration.getVariable().getName())) {
									localFound = true;
									break;
								}
							}
						}
						if (!localFound) {
							mixinDeclarationInfo.increaseNumberOfVariablesOutOfScopeAccessed(1);
						}
					}
				}
			}
			if (useOfParameterFound)
				mixinDeclarationInfo.increaseNumberOfDeclarationsUsingParameters(1);
			else if (!useOfVariableFound)
				mixinDeclarationInfo.increaseNumberOfDeclarationsHavingOnlyHardCodedValues(1);
			
			String property = declaration.getNameAsString();
			String nonVendorProperty = Declaration.getNonVendorProperty(property);
			ASTCssNode parent = declaration.getParent();
			String parentName = String.format("%s<%s:%s>", parent.toString(), parent.getSourceLine(), parent.getSourceColumn()); 
			String parentAndProperty = parentName + "\\" + nonVendorProperty;
			if (propertyToCountMap.containsKey(parentAndProperty)) {
				propertyToCountMap.get(parentAndProperty).add(property);
			} else {
				Set<String> newSet = new HashSet<>();
				newSet.add(property);
				propertyToCountMap.put(parentAndProperty, newSet);
			}
		}
		Set<String> vendorSpecificSharingParams = new HashSet<>();
		for (String parameter : parameterToDeclarationMap.keySet()) {
			Set<String> set = parameterToDeclarationMap.get(parameter);
			Set<String> distinctProperties = new HashSet<>();
			for (String property : set) {
				String nonVendorProperty = Declaration.getNonVendorProperty(property);
				if (!nonVendorProperty.equals(property))
					vendorSpecificSharingParams.add(nonVendorProperty);
				distinctProperties.add(nonVendorProperty);
			}
			if (distinctProperties.size() > 1)
				mixinDeclarationInfo.increaseNumberOfUniqueParametersUsedInMoreThanOneKindOfDeclaration(1);
			if (vendorSpecificSharingParams.size() > 1)
				mixinDeclarationInfo.increaseNumberOfUniqueParametersUsedInVendorSpecific(1);
		}
		mixinDeclarationInfo.increaseNumberOfVendorSpecificSharingParameter(vendorSpecificSharingParams.size());
	}
	
	public static Set<com.github.sommeri.less4j.core.ast.Declaration> getAllDeclarations(GeneralBody body) {
		Set<com.github.sommeri.less4j.core.ast.Declaration> toReturn = new HashSet<>();
		for (ASTCssNode child : body.getChilds()) {
			if (child instanceof com.github.sommeri.less4j.core.ast.Declaration) {
				toReturn.add((com.github.sommeri.less4j.core.ast.Declaration) child);
			} else if (child instanceof GeneralBody) {
				toReturn.addAll(getAllDeclarations((GeneralBody)child));
			} else if (child instanceof RuleSet) {
				toReturn.addAll(getAllDeclarations(((RuleSet)child).getBody()));
			} else if (child instanceof ReusableStructure) {
				toReturn.addAll(getAllDeclarations(((ReusableStructure)child).getBody()));
			} else if (child instanceof Media) {
				toReturn.addAll(getAllDeclarations(((Media)child).getBody()));
			} else {
				// if (!(child instanceof SyntaxOnlyElement) && 
				// !(child instanceof VariableDeclaration) && 
				// !(child instanceof MixinReference) &&
				// !(child instanceof Extend))
			}
		}
		return toReturn;
	}
	
	
	public List<LessNesting> getNestingInfo() {
		List<LessNesting> lessNestingInfoList = new ArrayList<>();
		getNestingInfo("", lessStyleSheet, -1, lessNestingInfoList);
		return lessNestingInfoList;
	}

	private void getNestingInfo(String parentName, ASTCssNode cssNode, int level, List<LessNesting> lessNestingInfoList) {
		level++;
		for (ASTCssNode node : cssNode.getChilds()) {

			if (node instanceof RuleSet || node instanceof ReusableStructure || node instanceof Media) {

				GeneralBody body = null;
				String selectorName = getNodeName(node);
	
				if (node instanceof RuleSet) {
					RuleSet ruleSet = (RuleSet)node;
					body = ruleSet.getBody();
				} else if (node instanceof Media) {
					Media media = (Media)node;
					body = media.getBody();
				} else {
					ReusableStructure reusableStructure = (ReusableStructure)node;
					body = reusableStructure.getBody();
				}
				if (level == 0) {
					parentName = selectorName;
				} else { //if (level >= 1)
					LessNesting lessNestingInfo = new LessNesting(
							node.getSourceLine(),
							node.getSourceColumn(),
							parentName,
							selectorName,
							level,
							this.lessStyleSheet
							);
					lessNestingInfoList.add(lessNestingInfo);
				}
				if (body == null)
					continue;

				getNestingInfo(selectorName, body, level, lessNestingInfoList);
			}
		}
	}
	
	public List<LessVariableDeclaration> getVariableInformation() {

		return getVariableInformation(new HashSet<>()); 

	}
	
	private List<LessVariableDeclaration> getVariableInformation(Set<LessImport> importsToSkip) {

		List<LessVariableDeclaration> toReturn = new ArrayList<>();
		
		for (ASTCssNode node : getAllChilds(lessStyleSheet)) {
			if (node instanceof VariableDeclaration) {
				VariableDeclaration variableDeclaration = (VariableDeclaration) node;
				LessVariableDeclaration variableInformation = new LessVariableDeclaration(variableDeclaration, this.lessStyleSheet);
				toReturn.add(variableInformation);
			}
		}
		
		for (LessImport lessImport : this.importedStyleSheetsData.keySet()) {
			if (!importsToSkip.contains(lessImport)) {
				importsToSkip.add(lessImport);
				toReturn.addAll(this.importedStyleSheetsData.get(lessImport).getVariableInformation(importsToSkip));
			}
		}
		
		return toReturn;

	}
	
	public List<LessExtend> getExtendInfo() {
		return getExtendInfo(new HashSet<>());
	}
	
	private List<LessExtend> getExtendInfo(Set<LessImport> importsToSkip) {

		List<LessExtend> toReturn = new ArrayList<>();
		
		for (ASTCssNode node : getAllChilds(lessStyleSheet)) {
			if (node instanceof Extend) {
				Extend extend = (Extend) node;
				toReturn.add(new LessExtend(extend, this.lessStyleSheet));				
			}
		}
		
		for (LessImport lessImport : this.importedStyleSheetsData.keySet()) {
			if (!importsToSkip.contains(lessImport)) {
				importsToSkip.add(lessImport);
				toReturn.addAll(this.importedStyleSheetsData.get(lessImport).getExtendInfo(importsToSkip));
			}
		}
		
		return toReturn;
	}
	
	public List<LessSelector> getSelectorsInfo() {
		
		return getSelectorsInfo(new HashSet<>());
		
	}
	
	private List<LessSelector> getSelectorsInfo(Set<LessImport> importsToSkip) {
		
		ASTCssNode rootNode = lessStyleSheet;
		List<LessSelector> selectorsInfo = new ArrayList<>();
		List<String> parents = new ArrayList<>();
		getSelectorsInfo(rootNode, parents, selectorsInfo);
		
		for (LessImport lessImport : this.importedStyleSheetsData.keySet()) {
			if (!importsToSkip.contains(lessImport)) {
				importsToSkip.add(lessImport);
				selectorsInfo.addAll(this.importedStyleSheetsData.get(lessImport).getSelectorsInfo(importsToSkip));
			}
		}
		
		return selectorsInfo;
	}

	private void getSelectorsInfo(ASTCssNode rootNode, List<String> parents, List<LessSelector> selectorsInfo) {
		
		for (ASTCssNode node : rootNode.getChilds()) {
			GeneralBody body = null;
			int numberOfBaseSelectors = 0;
			int numberOfNestableSelectors = 0;
			String type = "";
			int numberOfDeclarations = 0;
			if (node instanceof RuleSet) {
				RuleSet ruleSet = (RuleSet)node;
				body = ruleSet.getBody();
				numberOfBaseSelectors = ruleSet.getSelectors().size();
				numberOfNestableSelectors = countNestableSelecors(ruleSet);
				if (ruleSet.getBody() != null) {
					for (ASTCssNode child : ruleSet.getBody().getChilds()) {
						if (child instanceof com.github.sommeri.less4j.core.ast.Declaration)
							numberOfDeclarations++;
					}
				}
				type = "RuleSet";
			} else if (node instanceof Media) {
				body = ((Media)node).getBody();
				type = "Media";
			}
			if (!"".equals(type)) {
				boolean hasNesting = false;
				for (ASTCssNode child : getAllChilds(node)) {
					if (child != node && (child instanceof RuleSet || child instanceof Media)) {
						hasNesting = true;
						break;
					}
				}

				String parentName = "";
				int parentLine = -1;
				String parentType = "";
				if (!(rootNode instanceof StyleSheet)) {
					ASTCssNode parent = rootNode.getParent();
					parentName = getNodeName(parent);
					parentLine = parent.getSourceLine();
					if (parent instanceof Media)
						parentType = "Media";
					else if (parent instanceof RuleSet)
						parentType = "RuleSet";
				}
				
				LessSelector selectorInfo = new LessSelector(node, parents, hasNesting, type, this.lessStyleSheet);
				selectorInfo.setParentInfo(parentName, parentLine, parentType);
				selectorInfo.setBodyInfo(numberOfBaseSelectors, numberOfNestableSelectors, numberOfDeclarations);
				selectorsInfo.add(selectorInfo);
								
				if (body != null) {
					List<String> currentParents = new ArrayList<>();
					currentParents.addAll(parents);
					currentParents.add(selectorInfo.getName());
					getSelectorsInfo(body, currentParents, selectorsInfo);
				}
				
			}
		}
	}

	private int countNestableSelecors(RuleSet ruleSetNode) {
		if (ruleSetNode.getSelectors().size() == 1) { // BaseSelector
			if (checkIfNestable(ruleSetNode.getSelectors().get(0)))
				return 1;
			else
				return 0;
		} else { // Grouping Selector
			int sum = 0;
			for (com.github.sommeri.less4j.core.ast.Selector s : ruleSetNode.getSelectors()) {
				if (checkIfNestable(s))
					sum++;
			}
			return sum;
		}
	}

	private boolean checkIfNestable(com.github.sommeri.less4j.core.ast.Selector selector) {
		// Combinator, PseudoClass or PseudoElement
		if (selector.getParts().size() > 1) {
			int partsCount = 0;
			for (SelectorPart part : selector.getParts()) {
				if (!(part instanceof NestedSelectorAppender))
					partsCount++;
			}
			return partsCount > 1;
		} else {
			boolean hasPseudo = false, hasOtherChilds = false;
			for (ASTCssNode child : selector.getChilds()) {
				if (child instanceof PseudoClass || child instanceof PseudoElement) {
					hasPseudo = true;
				} else {
					hasOtherChilds = true;
				}
			}
			if (hasPseudo && hasOtherChilds)
				return true;
		}
		return false;
	}

	
	public static List<ASTCssNode> getAllChilds(ASTCssNode child) {
		List<ASTCssNode> toReturn = new ArrayList<>();
		toReturn.add(child);
		for (ASTCssNode c : child.getChilds())
			toReturn.addAll(getAllChilds(c));
		return toReturn;
	}
	

	public static String getNodeName(ASTCssNode node) {
		LessPrinter printer = new LessPrinter();
		String selectorName = printer.getStringForNode(node);
		selectorName = selectorName.replaceAll("//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/", "");
		int firstCurly = selectorName.indexOf("{");
		try {
			if (selectorName.length() > 0 && firstCurly > 0) {
				selectorName = selectorName.substring(0, firstCurly);
			}
		} catch (StringIndexOutOfBoundsException oe) {

		}
		selectorName = selectorName.replace("{", "").replace("}", "");
		selectorName = selectorName.replaceAll("\r|\n|\r\n", "").trim();
		return selectorName;
	}

	public List<StyleSheet> getAllVisitedStyleSheets() {
		
		return getAllVisitedStyleSheet(new HashSet<>());
		
	}
	
	private List<StyleSheet> getAllVisitedStyleSheet(Set<LessImport> skipImports) {
		
		List<StyleSheet> toReturn = new ArrayList<>();
		toReturn.add(this.lessStyleSheet);
		
		for (LessImport lessImport : this.importedStyleSheetsData.keySet()) {
			if (!skipImports.contains(lessImport)) {
				skipImports.add(lessImport);
				toReturn.addAll(importedStyleSheetsData.get(lessImport).getAllVisitedStyleSheet(skipImports));
			}
		}
		
		return toReturn;
		
	}
	
	public List<LessEmbeddedScriptInfo> getEmbededScriptInfo() {
		
		return getEmbededScriptInfo(new HashSet<>());
		
	}
	
	private List<LessEmbeddedScriptInfo> getEmbededScriptInfo(Set<LessImport> importsToSkip) {
		
		List<LessEmbeddedScriptInfo> toReturn = new ArrayList<>();
		
		for (ASTCssNode astCssNode : getAllChilds(lessStyleSheet)) {
			if (astCssNode instanceof FunctionExpression) {
				FunctionExpression functionExpression = (FunctionExpression) astCssNode;
				if ("~`".equals(functionExpression.getName())) {
					toReturn.add(new LessEmbeddedScriptInfo(functionExpression, this.lessStyleSheet));
				}
			}
		}
		
		for (LessImport lessImport : this.importedStyleSheetsData.keySet()) {
			if (!importsToSkip.contains(lessImport)) {
				importsToSkip.add(lessImport);
				toReturn.addAll(this.importedStyleSheetsData.get(lessImport).getEmbededScriptInfo(importsToSkip));
			}
		}
		
		return toReturn;
		
	}
	
	public List<LessInterpolationInfo> getInterpolationsInfo() {
		
		return getInterpolationsInfo(new HashSet<>());
		
	}
	
	private List<LessInterpolationInfo> getInterpolationsInfo(Set<LessImport> importsToSkip) {
		
		List<LessInterpolationInfo> toReturn = new ArrayList<>();
		for (ASTCssNode node : getAllChilds(lessStyleSheet)) {
			if (node instanceof InterpolableName) {
				InterpolableName interpolableName = (InterpolableName) node;
				if (interpolableName.isInterpolated())
					toReturn.add(new LessInterpolationInfo(interpolableName, this.lessStyleSheet));				
			} else if (node instanceof CssString) {
				CssString cssString = (CssString) node;
				if (cssString.getValue().contains("@{"))
					toReturn.add(new LessInterpolationInfo(cssString, this.lessStyleSheet));
			} else if (node instanceof EscapedValue) {
				EscapedValue escapedValue = (EscapedValue) node;
				if (escapedValue.getValue().contains("@{"))
					toReturn.add(new LessInterpolationInfo(escapedValue, this.lessStyleSheet));				
			}
		}
		
		for (LessImport lessImport : this.importedStyleSheetsData.keySet()) {
			if (!importsToSkip.contains(lessImport)) {
				importsToSkip.add(lessImport);
				toReturn.addAll(this.importedStyleSheetsData.get(lessImport).getInterpolationsInfo(importsToSkip));
			}
		}
		
		return toReturn;
		
	}

}
