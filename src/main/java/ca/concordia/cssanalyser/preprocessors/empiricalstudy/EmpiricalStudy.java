package ca.concordia.cssanalyser.preprocessors.empiricalstudy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.StyleSheet;

import ca.concordia.cssanalyser.app.CSSAnalyserCLI;
import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.io.IOHelper;
import ca.concordia.cssanalyser.parser.ParseException;
import ca.concordia.cssanalyser.parser.less.LessCSSParser;
import ca.concordia.cssanalyser.parser.less.ModifiedLessFileSource;
import ca.concordia.cssanalyser.preprocessors.constructsinfo.LessASTQueryHandler;
import ca.concordia.cssanalyser.preprocessors.constructsinfo.LessEmbeddedScriptInfo;
import ca.concordia.cssanalyser.preprocessors.constructsinfo.LessExtend;
import ca.concordia.cssanalyser.preprocessors.constructsinfo.LessInterpolationInfo;
import ca.concordia.cssanalyser.preprocessors.constructsinfo.LessMixinCall;
import ca.concordia.cssanalyser.preprocessors.constructsinfo.LessMixinDeclaration;
import ca.concordia.cssanalyser.preprocessors.constructsinfo.LessNesting;
import ca.concordia.cssanalyser.preprocessors.constructsinfo.LessSelector;
import ca.concordia.cssanalyser.preprocessors.constructsinfo.LessVariableDeclaration;

public class EmpiricalStudy {
	
	private static class CSVColumns {
		
		private static final String SEPARATOR = "|";
		
		private final String[] columns;
		
		public CSVColumns(String... columns) {
			this.columns = columns;
		}

		public String getHeader(boolean addLineSeparator) {
			StringBuilder toReturn = new StringBuilder();
			for (int i = 0; i < columns.length; i++) {
				toReturn.append(columns[i]);
				if (i < columns.length - 1) {
					toReturn.append(SEPARATOR);
				} else { 
					if (addLineSeparator) {
						toReturn.append(System.lineSeparator());
					}
				}
			}
			return toReturn.toString();
		}
		
		public String getRowFormat(boolean addLineSeparator) {
			StringBuilder toReturn = new StringBuilder();
			for (int i = 0; i < columns.length; i++) {
				toReturn.append("%s");
				if (i < columns.length - 1) {
					toReturn.append(SEPARATOR);
				} else { 
					if (addLineSeparator) {
						toReturn.append(System.lineSeparator());
					}
				}
			}
			return toReturn.toString();
		}
		
	}

	public static void writeStringToFile(String string, String path, boolean append) {
		IOHelper.writeStringToFile(string.replace("#", "\\#"), path, append);
	}

	private List<StyleSheet> lessStyleSheets = new ArrayList<>();
	private List<LessASTQueryHandler> queryHandlers = new ArrayList<>();
	private final String website;
	
	public EmpiricalStudy(String website, List<String> pathToLessFiles) throws ParseException {
		for (String pathToLessFile : pathToLessFiles) {
			StyleSheet styleSheet = LessCSSParser.getLessStyleSheet(new ModifiedLessFileSource(new File(pathToLessFile)));
			this.lessStyleSheets.add(styleSheet);
			this.queryHandlers.add(new LessASTQueryHandler(styleSheet));
		}
		this.website = website;
	}

	public void writeMixinCallsInfoToFile(String path, boolean header) {
		
		CSVColumns columns = new CSVColumns("WebSite" , "File", "MixinName", "NumberOfArgumentsPassed", "NumberOfMultiValuedArguments", 
				"MixinDeclarationFile", "MixinDeclarationName");

		if (header) {
			writeStringToFile(columns.getHeader(true), path, false);
		}
		
		Set<LessMixinCall> alreadyVisitedMixinCalls = new HashSet<>();
		
		for (int i = 0; i < this.lessStyleSheets.size(); i++) {
			
			LessASTQueryHandler queryHandler = this.queryHandlers.get(i);
			
			for (LessMixinCall lessMixinCallInfo : queryHandler.getMixinCallInfo()) {
				if (!alreadyVisitedMixinCalls.contains(lessMixinCallInfo)) {
					LessMixinDeclaration mixinDeclaration = lessMixinCallInfo.getMixinDeclaration();
					writeStringToFile(
							String.format(columns.getRowFormat(true), 
								website,
								lessMixinCallInfo.getStyleSheetPath(), 
								lessMixinCallInfo.getName(),
								lessMixinCallInfo.getNumberOfParameters(),
								lessMixinCallInfo.getNumberOfMultiValuedArguments(),
								mixinDeclaration != null ? mixinDeclaration.getStyleSheetPath() : "",
								mixinDeclaration != null ? mixinDeclaration.getMixinName() : ""
							),
							path, true);
					
					alreadyVisitedMixinCalls.add(lessMixinCallInfo);
				}
			}
		} 

	}


	public void writeMixinDeclarationsInfoToFile(String path, boolean header) {
		
		CSVColumns columns = new CSVColumns("WebSite", "File", "MixinName", "Parameters", "Declarations", "DeclarationsUsingParams",
				"CrossBrowserDeclarations", "NonCrossBrowserDeclarations", "UniqueParametersUsedInMoreThanOneKindOfDeclaration", 
				"DeclarationsHavingOnlyHardCoded", "ParametersReusedInVendorSpecific", "VendorSpecificSharingParam",
				"GlobalVarsAccessed");
		
		if (header) {
			writeStringToFile(columns.getHeader(true), path, false);
		}

		Set<LessMixinDeclaration> alreadyVisitedMixins = new HashSet<>();

		for (int i = 0; i < this.lessStyleSheets.size(); i++) {
			
			LessASTQueryHandler queryHandler = this.queryHandlers.get(i);
			
			List<LessMixinDeclaration> mixinDeclarationsInfo = queryHandler.getAllMixinDeclarationsAndSelectorsCalledAsMixin();//queryHandler.getMixinDeclarationInfo();
			
			for (LessMixinDeclaration mixinDeclarationInfo : mixinDeclarationsInfo) {
				if (!alreadyVisitedMixins.contains(mixinDeclarationInfo)) {
					writeStringToFile(
						String.format(columns.getRowFormat(true),
							website,
							mixinDeclarationInfo.getStyleSheetPath(),
							mixinDeclarationInfo.getMixinName(),
							mixinDeclarationInfo.getNumberOfParams(),
							mixinDeclarationInfo.getNumberOfDeclarations(),
							mixinDeclarationInfo.getNumberOfDeclarationsUsingParameters(),
							mixinDeclarationInfo.getNumberOfUniqueCrossBrowserDeclarations(), 
							mixinDeclarationInfo.getNumberOfNonCrossBrowserDeclarations(),
							mixinDeclarationInfo.getNumberOfUniqueParametersUsedInMoreThanOneKindOfDeclaration(),
							mixinDeclarationInfo.getNumberOfDeclarationsHavingOnlyHardCodedValues(),
							mixinDeclarationInfo.getNumberOfUniqueParametersUsedInVendorSpecific(),
							mixinDeclarationInfo.getNumberOfVendorSpecificSharingParameter(),
							mixinDeclarationInfo.getNumberOfVariablesOutOfScopeAccessed()
						)
						, path, true);
					
					alreadyVisitedMixins.add(mixinDeclarationInfo);
				}
			}
		}

	}

	public void writeNestingInfoToFile(String path, boolean header) {
		
		if (header) {
			writeStringToFile("WebSite|File|Line|Parent|Name|Level" + System.lineSeparator(), path, false);
		}
		
		Set<LessNesting> alreadyVisitedNestings = new HashSet<>();
		
		for (int i = 0; i < this.lessStyleSheets.size(); i++) {

			LessASTQueryHandler queryHandler = this.queryHandlers.get(i);
		
			for (LessNesting nestingInfo : queryHandler.getNestingInfo()) {

				if (!alreadyVisitedNestings.contains(nestingInfo)) {

					StringBuilder toWrite = new StringBuilder();
					toWrite.append(website).append("|")
					.append(nestingInfo.getStyleSheet().getSource().toString()).append("|")
					.append(nestingInfo.getSourceLine()).append("|")
					.append(nestingInfo.getParentName()).append("|")
					.append(nestingInfo.getSelectorName()).append("|")
					.append(nestingInfo.getLevel())
					.append(System.lineSeparator());
					writeStringToFile(toWrite.toString(), path, true);
					
					alreadyVisitedNestings.add(nestingInfo);

				}

			}
		}
	}


	public void writeVariableInformation(String path, boolean header) {
		
		CSVColumns columns = new CSVColumns("WebSite" , "File" , "Line" , "Variable" , "Type" , "Scope");

		if (header) {	
			writeStringToFile(columns.getHeader(true), path, false);
		}

		Set<LessVariableDeclaration> alreadyVisitedVariables = new HashSet<>();

		for (int i = 0; i < this.lessStyleSheets.size(); i++) {
			
			LessASTQueryHandler queryHandler = this.queryHandlers.get(i);

			for (LessVariableDeclaration variableInfo : queryHandler.getVariableInformation()) {

				if (!alreadyVisitedVariables.contains(variableInfo)) {

					writeStringToFile(
						String.format(columns.getRowFormat(true), 
							website, 
							variableInfo.getStyleSheet().getSource().toString(),
							variableInfo.getSourceLine(), 
							variableInfo.getVariableString().replace("|", "{???}"),
							variableInfo.getVariableType(),
							variableInfo.getScope()
						), 
						path, true);

					alreadyVisitedVariables.add(variableInfo);

				}

			}

		}
	}

	public void writeExtendInfo(String path, boolean header) {
		
		CSVColumns columns = new CSVColumns("WebSite" , "File" , "Line" , "Target" , "isAll");
		
		if (header) {	
			writeStringToFile(columns.getHeader(true), path, false);
		}
		
		Set<LessExtend> alreadyVisitedExtends = new HashSet<>();

		for (int i = 0; i < this.lessStyleSheets.size(); i++) {
			
			LessASTQueryHandler queryHandler = this.queryHandlers.get(i);
		
			for (LessExtend extendInfo : queryHandler.getExtendInfo()) {
	
				if (!alreadyVisitedExtends.contains(extendInfo)) {

					writeStringToFile(
						String.format(columns.getRowFormat(true), 
							website, 
							extendInfo.getStyleSheet().getSource().toString(),
							extendInfo.getSourceLine(),
							extendInfo.getTargetSelectorName(), 
							extendInfo.isAll()
						),
						path, true);
					
					alreadyVisitedExtends.add(extendInfo);
					
				}
	 
			}
			
		}
	}

	public void writeSelectorsInfoToFile(String path, boolean header) {
		
		CSVColumns columns = new CSVColumns("WebSite", "File", "Line", "Name", "NumberOfBaseSelectors", 
				"NumberOfNestableSelectors", "HasNesting", "Parent",
				"ParentLine", "ParentType", "NumberOfDeclarations", "Type", "Level");
		
		if (header) {	
			writeStringToFile(columns.getHeader(true), path, false);
		}
		
		Set<LessSelector> alreadyVisitedSelectors = new HashSet<>();

		for (int i = 0; i < this.lessStyleSheets.size(); i++) {

			LessASTQueryHandler queryHandler = this.queryHandlers.get(i);

			for (LessSelector selectorInfo : queryHandler.getSelectorsInfo()) {

				if (!alreadyVisitedSelectors.contains(selectorInfo)) {

					writeStringToFile(
						String.format(columns.getRowFormat(true), 
							website, 
							selectorInfo.getStyleSheet().getSource().toString(),
							selectorInfo.getSourceLine(),
							selectorInfo.getName(),
							selectorInfo.getNumberOfBaseSelectors(),
							selectorInfo.getNumberOfNestableSelectors(),
							selectorInfo.hasNesting(),
							selectorInfo.getParentName(),
							selectorInfo.getParentLine(),
							selectorInfo.getParentType(),
							selectorInfo.getNumberOfDeclarations(),
							selectorInfo.getType(),
							selectorInfo.getLevel()
						),
						path, true);

					alreadyVisitedSelectors.add(selectorInfo);

				}
			}

		}
	}
	
	private void writeEmbeddedScriptInfo(String path, boolean header) {
		
		CSVColumns columns = new CSVColumns("WebSite" , "File", "Param");
		
		if (header) {
			writeStringToFile(columns.getHeader(true), path, false);
		}
		
		Set<LessEmbeddedScriptInfo> alreadyVisitedEmbeddedScriptsInfoObjects = new HashSet<>();

		for (int i = 0; i < this.lessStyleSheets.size(); i++) {

			LessASTQueryHandler lessASTQueryHandler = this.queryHandlers.get(i);
			List<LessEmbeddedScriptInfo> embededScriptInfo = lessASTQueryHandler.getEmbededScriptInfo();

			for (LessEmbeddedScriptInfo lessEmbeddedScriptInfo : embededScriptInfo) {
				if (!alreadyVisitedEmbeddedScriptsInfoObjects.contains(lessEmbeddedScriptInfo)) {
					writeStringToFile(
						String.format(columns.getRowFormat(true), 
								this.website,
								lessEmbeddedScriptInfo.getStyleSheetPath(),
								lessEmbeddedScriptInfo.getParameterAsString()
								),
						path, true
						);

					alreadyVisitedEmbeddedScriptsInfoObjects.add(lessEmbeddedScriptInfo);
				}
			}
		}
	}

	public void writeFileSizeInfoToFile(String path, boolean header) {
		
		CSVColumns columns = new CSVColumns("WebSite" , "File" , "Size");
		
		if (header) {
			writeStringToFile(columns.getHeader(true), path, false);
		}
		
		for (int i = 0; i < this.lessStyleSheets.size(); i++) {
			
			LessASTQueryHandler lessASTQueryHandler = this.queryHandlers.get(i);
			
			List<StyleSheet> allVisitedStyleSheets = lessASTQueryHandler.getAllVisitedStyleSheets();
			
			for (StyleSheet styleSheet : allVisitedStyleSheets) {

				String cssFilePath = styleSheet.getSource().toString();
				long fileSize = (new File(cssFilePath)).length();

				writeStringToFile(
					String.format(columns.getRowFormat(true), 
						website, 
						cssFilePath, 
						fileSize
					), path, true);
			}
			
		}
		
	}
	
	public void writeInterpolationsInfoToFile(String path, boolean header) {
		
		CSVColumns columns = new CSVColumns("WebSite", "File", "Line", "VariableUsed");
		
		if (header) {
			writeStringToFile(columns.getHeader(true), path, false);
		}
		
		Set<LessInterpolationInfo> alreadyVisitedInterpolations = new HashSet<>();
		
		for (int i = 0; i < this.lessStyleSheets.size(); i++) {
			
			LessASTQueryHandler lessASTQueryHandler = this.queryHandlers.get(i);
			
			List<LessInterpolationInfo> interpolationsInfoList = lessASTQueryHandler.getInterpolationsInfo();
			
			for (LessInterpolationInfo interpolationInfo : interpolationsInfoList) {
				
				if (!alreadyVisitedInterpolations.contains(interpolationInfo)) {

					writeStringToFile(
						String.format(columns.getRowFormat(true), 
							website, 
							interpolationInfo.getStyleSheetPath(), 
							interpolationInfo.getLine(),
							interpolationInfo.getInterpolableNameAsString()
						), path, true);
					
					alreadyVisitedInterpolations.add(interpolationInfo);
				
				}
			}
			
		}
		
	}
	
	public static void doEmpiricalStudy(List<String> folders, String outfolder) {

		if (folders.size() > 0) {
	
			for (String folder : folders) {
				
				FileLogger.addFileAppender(outfolder + "/log.log", false);
				List<File> listOfFilesContainingMainFiles = IOHelper.searchForFiles(folder, "mainfiles.txt", true);
	
				boolean header = true;
				for (int i = 0; i < listOfFilesContainingMainFiles.size(); i++) {
					File mainFilesPathsFile = listOfFilesContainingMainFiles.get(i);
					try {
						CSSAnalyserCLI.LOGGER.info(String.format("%3s%%: %s", Math.round((float)(i + 1)/ listOfFilesContainingMainFiles.size() * 100), mainFilesPathsFile.getAbsolutePath()));
						String[] mainFilesRelativePaths = IOHelper.readFileToString(mainFilesPathsFile.getAbsolutePath()).split("\n");
						String website = mainFilesPathsFile.getParentFile().getName();
						List<String> filesToConsider = new ArrayList<>();
						for (String mainFileRelativePath : mainFilesRelativePaths) {
							mainFileRelativePath = mainFileRelativePath.replace("\r", "");
							String absolutePathToMainLessFile = mainFilesPathsFile.getParentFile().getAbsolutePath() + File.separator + mainFileRelativePath;
							filesToConsider.add(absolutePathToMainLessFile);
						}
						EmpiricalStudy empiricalStudy = new EmpiricalStudy(website, filesToConsider);
						empiricalStudy.writeMixinCallsInfoToFile(outfolder + "/less-mixinCallsInfo.txt", header);
						empiricalStudy.writeMixinDeclarationsInfoToFile(outfolder + "/less-mixinDeclarationInfo.txt", header);
						empiricalStudy.writeVariableInformation(outfolder + "/less-variableDeclarationsInfo.txt", header);
						empiricalStudy.writeExtendInfo(outfolder + "/less-extendInfo.txt", header);
						empiricalStudy.writeFileSizeInfoToFile(outfolder + "/less-fileSizes.txt", header);
						empiricalStudy.writeSelectorsInfoToFile(outfolder + "/less-selectorsInfo.txt", header);
						empiricalStudy.writeEmbeddedScriptInfo(outfolder + "/less-embeddedScriptsInfo.txt", header);
						empiricalStudy.writeInterpolationsInfoToFile(outfolder + "/less-interpolationsInfo.txt", header);
						header = false;
					} catch (IOException | ParseException e) {
						e.printStackTrace();
					}
				}
	
			}
		} else {
			CSSAnalyserCLI.LOGGER.warn("No input folder is provided.");
		}
	}
}
