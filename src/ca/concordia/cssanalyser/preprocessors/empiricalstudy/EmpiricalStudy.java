package ca.concordia.cssanalyser.preprocessors.empiricalstudy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.concordia.cssanalyser.app.CSSAnalyserCLI;
import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.io.IOHelper;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessPrinter;
import ca.concordia.cssanalyser.parser.ParseException;
import ca.concordia.cssanalyser.parser.less.LessCSSParser;

import com.github.sommeri.less4j.LessSource.FileSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ArgumentDeclaration;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Extend;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.ast.Media;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.NamedColorExpression;
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

public class EmpiricalStudy {
	
	public static void writeStringToFile(String string, String path, boolean append) {
		IOHelper.writeStringToFile(string.replace("#", "\\#"), path, append);
	}

	private StyleSheet lessStyleSheet;
	
	public EmpiricalStudy(StyleSheet styleSheet) {
		this.lessStyleSheet = styleSheet;
	}

	public void writeMixinCallsInfoToFile(String path, boolean header) {

		if (header) {
			EmpiricalStudy.writeStringToFile("WebSite|File|MixinName|NumberOfArgumentsPassed|NumberOfMultiValuedArguments" + System.lineSeparator(), path, false);
		}

		for (ASTCssNode node : getAllChilds(lessStyleSheet)) {

			if (node instanceof RuleSet || node instanceof ReusableStructure) {

				GeneralBody body = null;
				if (node instanceof RuleSet)
					body = ((RuleSet)node).getBody();
				else
					body = ((ReusableStructure)node).getBody();

				if (body == null)
					continue;

				for (ASTCssNode child : body.getChilds()) {
					if (child instanceof MixinReference) {
						MixinReference reference = (MixinReference)child;

						String mixinName = reference.getFinalNameAsString();

						int numberOfMultiValuedArguments = 0;

						for (ASTCssNode ex : reference.getChilds()) {
							if (ex instanceof ListExpression) {
								++numberOfMultiValuedArguments;
							}
						}

						String webSiteName = this.lessStyleSheet.getSource().toString().split(",")[0];
						EmpiricalStudy.writeStringToFile(String.format("%s|%s|%s|%s|%s%s", 
								webSiteName,
								this.lessStyleSheet.getSource().toString(), 
								mixinName,
								reference.getNumberOfDeclaredParameters(),
								numberOfMultiValuedArguments,
								System.lineSeparator()
								), path, true);

					}
				}
			}
		}
	}

	// Can you believe it?!
	static class ReturnValues {
		private int numberOfDeclarations = 0, 
				numberOfDeclarationsUsingParameters = 0,
				numberOfNonCrossBrowserDeclarations = 0,
				numberOfUniqueCrossBrowserDeclarations = 0,
				numberOfUniqueParametersUsedInMoreThanOneKindOfDeclaration = 0,
				numberOfDeclarationsHavingOnlyHardCodedValues = 0,
				numberOfUniqueParametersUsedInVendorSpecific = 0,
				numberOfVendorSpecificSharingParameter = 0;
		
	}

	public void writeMixinDeclarationsInfoToFile(String path, boolean header) {
		if (header) {
			EmpiricalStudy.writeStringToFile("Website|File|MixinName|Parameters|Declarations|DeclarationsUsingParams|CrossBrowserDeclarations|"
					+ "NonCrossBrowserDeclarations|UniqueParametersUsedInMoreThanOneKindOfDeclaration|DeclarationsHavingOnlyHardCoded|ParametersReusedInVendorSpecific|VendorSpecificSharingParam" + System.lineSeparator(), path, false);
		}
		for (ASTCssNode node : getAllChilds(lessStyleSheet)) {

			if (node instanceof ReusableStructure) {

				ReturnValues returnValues = new ReturnValues();

				ReusableStructure reusableNode = (ReusableStructure)node;

				int numberOfParams = reusableNode.getParameters().size();

				Map<String, Set<String>> propertyToCountMap = new HashMap<>();
				
				Set<String> parameters = new HashSet<>();
				for (ASTCssNode param : reusableNode.getParameters()) {
					if (!(param instanceof ArgumentDeclaration))
						System.out.println(param.getClass().getName());
					else {
						ArgumentDeclaration argumentDeclaration = (ArgumentDeclaration)param;
						parameters.add(argumentDeclaration.getVariable().getName());
					}
				}

				countVariablesInside(reusableNode.getBody(), propertyToCountMap, returnValues, parameters);

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
							returnValues.numberOfUniqueCrossBrowserDeclarations++;
						} else {
							returnValues.numberOfNonCrossBrowserDeclarations++;
						}
					} else {
						String first = propertyToCountMap.get(parentAndProperty).iterator().next();
						if (!first.equals(Declaration.getNonVendorProperty(first)))
							returnValues.numberOfUniqueCrossBrowserDeclarations++;
						else
							returnValues.numberOfNonCrossBrowserDeclarations++;
					}
				}
				String filePath = this.lessStyleSheet.getSource().toString();
				String webSite = filePath.split(",")[0];
				String mixinName = reusableNode.getNamesAsStrings().toString();
				mixinName = mixinName.substring(1, mixinName.length() - 1);
				EmpiricalStudy.writeStringToFile(String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s%s",
						webSite,
						filePath,
						mixinName,
						numberOfParams,
						returnValues.numberOfDeclarations,
						returnValues.numberOfDeclarationsUsingParameters,
						returnValues.numberOfUniqueCrossBrowserDeclarations, 
						returnValues.numberOfNonCrossBrowserDeclarations,
						returnValues.numberOfUniqueParametersUsedInMoreThanOneKindOfDeclaration,
						returnValues.numberOfDeclarationsHavingOnlyHardCodedValues,
						returnValues.numberOfUniqueParametersUsedInVendorSpecific,
						returnValues.numberOfVendorSpecificSharingParameter,
						System.lineSeparator()), path, true);
			}
		}
	}

	private void countVariablesInside(GeneralBody body, Map<String, Set<String>> propertyToCountMap, ReturnValues returnValues, Set<String> parameters) {
		Map<String, Set<String>> parameterToDeclarationMap = new HashMap<>();
		for (com.github.sommeri.less4j.core.ast.Declaration declaration : getAllDeclarations(body)) {

			returnValues.numberOfDeclarations++;
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
					}
				}
			}
			if (useOfParameterFound)
				returnValues.numberOfDeclarationsUsingParameters++;
			else if (!useOfVariableFound)
				returnValues.numberOfDeclarationsHavingOnlyHardCodedValues++;
			
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
				returnValues.numberOfUniqueParametersUsedInMoreThanOneKindOfDeclaration++;
			if (vendorSpecificSharingParams.size() > 1)
				returnValues.numberOfUniqueParametersUsedInVendorSpecific++;
		}
		returnValues.numberOfVendorSpecificSharingParameter += vendorSpecificSharingParams.size();
	}

	private Set<com.github.sommeri.less4j.core.ast.Declaration> getAllDeclarations(GeneralBody body) {
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
				//				if (!(child instanceof SyntaxOnlyElement) && 
				//						!(child instanceof VariableDeclaration) && 
				//						!(child instanceof MixinReference) &&
				//						!(child instanceof Extend))
				//					System.out.println();
			}
		}
		return toReturn;
	}

	public void writeNestingInfoToFile(String path, boolean header) {
		if (header) {

			EmpiricalStudy.writeStringToFile("WebSite|File|Line|Parent|Name|Level" + System.lineSeparator(), path, false);
		}

		getNestingInfo(path, "", lessStyleSheet, -1);

	}

	public void writeFileSizeInfoToFile(String path, boolean header) {
		if (header) {
			EmpiricalStudy.writeStringToFile("WebSite|File|Size" + System.lineSeparator(), path, false);
		}
		String cssFilePath = lessStyleSheet.getSource().toString();
		long fileSize = (new File(cssFilePath)).length();

		EmpiricalStudy.writeStringToFile(String.format("%s|%s|%s%s", 
				cssFilePath.split(",")[0], 
				cssFilePath, 
				fileSize,
				System.lineSeparator()), path, true);

	}

	private void getNestingInfo(String path, String parent, ASTCssNode cssNode, int level) {
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
					parent = selectorName;
				} else { //if (level >= 1)
					StringBuilder toWrite = new StringBuilder();
					String filePath = lessStyleSheet.getSource().toString();
					toWrite.append(filePath.split(",")[0]).append("|")
					.append(filePath).append("|")
					.append(node.getSourceLine()).append("|")
					.append(parent).append("|")
					.append(selectorName).append("|")
					.append(level)
					.append(System.lineSeparator());
					EmpiricalStudy.writeStringToFile(toWrite.toString(), path, true);
				}
				if (body == null)
					continue;

				getNestingInfo(path, selectorName, body, level);
			}
		}
	}

	private String getNodeName(ASTCssNode node) {
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

	private List<ASTCssNode> getAllChilds(ASTCssNode child) {
		List<ASTCssNode> toReturn = new ArrayList<>();
		toReturn.add(child);
		for (ASTCssNode c : child.getChilds())
			toReturn.addAll(getAllChilds(c));
		return toReturn;
	}

	public void writeVariableInformation(String path, boolean header) {
		if (header) {	
			EmpiricalStudy.writeStringToFile("WebSite|File|Line|Variable|Type|Scope" + System.lineSeparator(), path, false);
		}
		String filePath = lessStyleSheet.getSource().toString();
		String webSite = filePath.split(",")[0];
		LessPrinter printer = new LessPrinter();
		for (ASTCssNode node : getAllChilds(lessStyleSheet)) {
			if (node instanceof VariableDeclaration) {
				VariableDeclaration variableDeclaration = (VariableDeclaration) node;
				String scope = "Local";
				if (variableDeclaration.getParent() instanceof StyleSheet)
					scope = "Global";
				
				String stringForNode = printer.getStringForNode(variableDeclaration);
				String type = "Other";
				if (variableDeclaration.getValue() instanceof NumberExpression) {
					type = "Number";
				} else if (variableDeclaration.getValue() instanceof FunctionExpression) {
					String valueString = printer.getStringForNode(variableDeclaration.getValue());
					if (valueString.startsWith("rgb") | valueString.startsWith("hsl"))
						type = "Color";
					else
						type = "Function";
				} else if (variableDeclaration.getValue() instanceof IdentifierExpression) {
					type = "Identifier";
				//} else if (variableDeclaration.getValue() instanceof BinaryExpression) {
					
				} else if (variableDeclaration.getValue() instanceof CssString) {
					type = "String";
				} else if (variableDeclaration.getValue() instanceof ColorExpression ||
							variableDeclaration.getValue() instanceof NamedColorExpression) {
					type = "Color";
				}
				
				EmpiricalStudy.writeStringToFile(String.format("%s|%s|%s|%s|%s|%s%s", 
						webSite, 
						filePath,
						variableDeclaration.getSourceLine(), 
						stringForNode,
						type,
						scope,
						System.lineSeparator()), 
						path, true);
			}
		}

	}

	public void writeExtendInfo(String path, boolean header) {
		if (header) {	
			EmpiricalStudy.writeStringToFile("WebSite|File|Line|Target|isAll" + System.lineSeparator(), path, false);
		}
		String filePath = lessStyleSheet.getSource().toString();
		String webSite = filePath.split(",")[0];
		LessPrinter printer = new LessPrinter();
		for (ASTCssNode node : getAllChilds(lessStyleSheet)) {
			if (node instanceof Extend) {
				Extend extend = (Extend) node;
				String targetSelectorString = printer.getStringForNode(extend.getTarget());
				EmpiricalStudy.writeStringToFile(String.format("%s|%s|%s|%s|%s%s", 
						webSite, 
						filePath,
						extend.getSourceLine(),
						targetSelectorString, 
						extend.isAll(),
						System.lineSeparator()), path, true);
			}
		}
	}

	public void writeSelectorsInfoToFile(String path, boolean header) {
		if (header) {	
			EmpiricalStudy.writeStringToFile("WebSite|File|Line|Name|NumberOfBaseSelectors|NumberOfNestableSelectors|HasNesting|Parent|ParentLine|ParentType|NumberOfDeclarations|Type|Level" + System.lineSeparator(), path, false);
		}
		ASTCssNode rootNode = lessStyleSheet;
		String filePath = lessStyleSheet.getSource().toString();
		getSelectorsInfo(path, rootNode, filePath, 0);
	}

	private void getSelectorsInfo(String outpuFilePath, ASTCssNode rootNode, String filePath, int level) {
		String webSite = filePath.split(",")[0];
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
					parentName = getNodeName(rootNode.getParent());
					parentLine = rootNode.getParent().getSourceLine();
					if (rootNode.getParent() instanceof Media)
						parentType = "Media";
					else if (rootNode.getParent() instanceof RuleSet)
						parentType = "RuleSet";
				}
				
				String nodeName = getNodeName(node);
				int nodeSourceLine = node.getSourceLine();
				
				EmpiricalStudy.writeStringToFile(String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s%s", 
						webSite, 
						filePath,
						nodeSourceLine,
						nodeName,
						numberOfBaseSelectors,
						numberOfNestableSelectors,
						hasNesting,
						parentName,
						parentLine,
						parentType,
						numberOfDeclarations,
						type,
						level,
						System.lineSeparator()), outpuFilePath, true);
				
				if (body != null)
					getSelectorsInfo(outpuFilePath, body, filePath, level+1);
				
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

	public static void doEmpiricalStudy(List<String> folders, String outfolder) {

		if (folders.size() > 0) {
	
			for (String folder : folders) {
				
				FileLogger.addFileAppender(outfolder + "/log.log", false);
				List<File> lessFiles = IOHelper.searchForFiles(folder, "less");
	
				boolean header = true;
				for (int i = 0; i < lessFiles.size(); i++) {
					File f = lessFiles.get(i);
					CSSAnalyserCLI.LOGGER.info(String.format("%3s%%: %s", (float)i / lessFiles.size() * 100, f.getAbsolutePath()));
					EmpiricalStudy empiricalStudy;
					try {
						empiricalStudy = new EmpiricalStudy(LessCSSParser.getLessStyleSheet(new FileSource(f)));
						empiricalStudy.writeVariableInformation(outfolder + "/less-variableDeclarationsInfo.txt", header);
						empiricalStudy.writeMixinDeclarationsInfoToFile(outfolder + "/less-mixinDeclarationInfo.txt", header);
						empiricalStudy.writeExtendInfo(outfolder + "/less-extendInfo.txt", header);
						empiricalStudy.writeMixinCallsInfoToFile(outfolder + "/less-mixinCallsInfo.txt", header);
						empiricalStudy.writeFileSizeInfoToFile(outfolder + "/less-fileSizes.txt", header);
						empiricalStudy.writeSelectorsInfoToFile(outfolder + "/less-selectorsInfo.txt", header);
						header = false;
					} catch (ParseException e) {
						e.printStackTrace();
					}
	
				}
	
			}
		} else {
			CSSAnalyserCLI.LOGGER.warn("No input folder is provided.");
		}
	}
}
