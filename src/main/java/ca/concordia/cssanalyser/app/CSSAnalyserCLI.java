package ca.concordia.cssanalyser.app;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import ca.concordia.cssanalyser.analyser.CSSAnalyser;
import ca.concordia.cssanalyser.crawler.Crawler;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.io.CSVColumns;
import ca.concordia.cssanalyser.io.IOHelper;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessHelper;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessMigrationOpportunitiesDetector;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessMixinMigrationOpportunity;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessPrinter;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinDeclaration;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
import ca.concordia.cssanalyser.parser.CSSParser;
import ca.concordia.cssanalyser.parser.CSSParserFactory;
import ca.concordia.cssanalyser.parser.CSSParserFactory.CSSParserType;
import ca.concordia.cssanalyser.parser.ParseException;
import ca.concordia.cssanalyser.parser.less.LessCSSParser;
import ca.concordia.cssanalyser.parser.less.ModifiedLessFileSource;
import ca.concordia.cssanalyser.preprocessors.constructsinfo.LessASTQueryHandler;
import ca.concordia.cssanalyser.preprocessors.constructsinfo.LessMixinDeclaration;
import ca.concordia.cssanalyser.preprocessors.empiricalstudy.EmpiricalStudy;
import ca.concordia.cssanalyser.preprocessors.util.less.ImportInliner;

public class CSSAnalyserCLI {

	public static Logger LOGGER = FileLogger.getLogger(CSSAnalyserCLI.class);

	public static void main(String[] args) throws IOException {

		ParametersParser params = new ParametersParser(args);

		switch (params.getProgramMode()) {
		case CRAWL:
			doCloneRefactoringInCrawlMode(params);
			break;
		case FOLDER:
			doCloneRefactoringInFolderMode(params);
			break;
		case NODOM:
			doCloneRefactoringInNoDomMode(params);
			break;
		case DIFF:
			throw new RuntimeException("Not yet implemented");
		case MIXIN_MIGRATION_EMPIRICAL:
			doMixinsExtractionEmpiricalStudy(params);
			break;
		case PREP:
			doPreprocessorMigration(params);
			break;
		case EMPIRICAL_STUDY:
			doLessEmpiricalStudy(params);
			break;
		case INLINE_IMPORTS:
			doInlineImports(params);
			break;
		default:
		}		
	}
	
	interface ProcessLessFiles {
		void process(int percentage, String website, String pathToMainFile);
	}

	private static void processLessFiles(ParametersParser params, ProcessLessFiles intrface) {
		List<String> folders = getFolderPathsFromParameters(params);
		String outfolder = params.getOutputFolderPath();

		if (folders.size() > 0) {
			
			for (String folder : folders) {
				
				FileLogger.addFileAppender(outfolder + "/log.log", false);
				List<File> listOfFilesContainingMainFiles = IOHelper.searchForFiles(folder, "mainfiles.txt", true);

				for (int mainFileIndex = 0; mainFileIndex < listOfFilesContainingMainFiles.size(); mainFileIndex++) {

					File mainFilesPathsFile = listOfFilesContainingMainFiles.get(mainFileIndex);
					String website = mainFilesPathsFile.getParentFile().getName();

					LOGGER.info(String.format("%3s%%: %s", Math.round((float)(mainFileIndex + 1)/ listOfFilesContainingMainFiles.size() * 100), mainFilesPathsFile.getAbsolutePath()));
					try {
					
						String[] mainFilesRelativePaths = IOHelper.readFileToString(mainFilesPathsFile.getAbsolutePath()).split("\n");
						List<String> filesToConsider = new ArrayList<>();
						for (String mainFileRelativePath : mainFilesRelativePaths) {
							mainFileRelativePath = mainFileRelativePath.replace("\r", "");
							if (!"".equals(mainFileRelativePath)) {
								String absolutePathToMainLessFile = mainFilesPathsFile.getParentFile().getAbsolutePath() + File.separator + mainFileRelativePath;
								filesToConsider.add(absolutePathToMainLessFile);
							}
						}
						for (String pathToLessFile : filesToConsider) {
							intrface.process(Math.round((float)(mainFileIndex + 1)/ listOfFilesContainingMainFiles.size() * 100), website, pathToLessFile);
						}
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

	private static void doInlineImports(ParametersParser params) throws IOException {
		String inputFile = params.getFilePath();
		File file = new File(inputFile);
		if (file.exists()) {
			ImportInliner.replaceImports(inputFile, false);
		} else {
			LOGGER.error("File %s not found.", file.getCanonicalPath());
		}
	}

	private static void doLessEmpiricalStudy(ParametersParser params) {
		List<String> folders = CSSAnalyserCLI.getFolderPathsFromParameters(params);
		String outfolder = params.getOutputFolderPath();
		EmpiricalStudy.doEmpiricalStudy(folders, outfolder);
	}

	private static void doCloneRefactoringInNoDomMode(ParametersParser params) throws IOException {
		CSSAnalyser cssAnalyser = null;
		if (params.getInputFolderPath() != null) {
			try {
				cssAnalyser = new CSSAnalyser(params.getInputFolderPath());
			} catch (FileNotFoundException fnfe) {
				LOGGER.warn(fnfe.getMessage());
			}
		} else {
			LOGGER.error("Please provide an input folder with --in-folder \"in/folder\"");
			return;
		}
		cssAnalyser.analyse(params.getFPGrowthMinsup());
	}

	private static void doCloneRefactoringInFolderMode(ParametersParser params) throws IOException {
		List<String> folders = getFolderPathsFromParameters(params);

		if (folders.size() == 0) {
			LOGGER.error("Please provide an input folder with --in-folder \"in/folder\" or list of folders using --folders-file \"path/to/file\".");
		} else {

			for (String folder : folders) {
				List<File> allStatesFiles = IOHelper.searchForFiles(folder + "crawljax/doms", "html");	
				if (allStatesFiles.size() == 0) {
					LOGGER.warn("No HTML file found in " + folder + "crawljax/doms, skipping this folder");
				} else {
					for (File domStateHtml : allStatesFiles) {

						String stateName = domStateHtml.getName();
						// Remove .html
						String correspondingCSSFolderName = stateName.substring(0, stateName.length() - 5);

						try {

							CSSAnalyser cssAnalyser = new CSSAnalyser(domStateHtml.getAbsolutePath(), folder + "css/" + correspondingCSSFolderName);
							cssAnalyser.analyse(params.getFPGrowthMinsup());

						} catch (FileNotFoundException fnfe) {
							LOGGER.warn(fnfe.getMessage());
						}

					}
				}
			}
		}
	}

	private static void doCloneRefactoringInCrawlMode(ParametersParser params) throws IOException {
		if (params.getOutputFolderPath() == null) {
			LOGGER.error("Please provide an output folder using --out-folder \"out/folder\".");
			return;
		} else if (params.getUrl() == null && params.getListOfURLsToAnalyzeFilePath() == null) {
			LOGGER.error("Please provide a url using --url \"http://url/to/site\" or the file containing list of urls using --urls-file \"path/to/url\"");
			return;
		}

		List<String> urls = new ArrayList<>();

		if (params.getListOfURLsToAnalyzeFilePath() != null) {
			urls.addAll(params.getURLs());
		} else {
			urls.add(params.getUrl());
		}

		for (String currentUrl : urls) {

			String outputFolderPath = params.getOutputFolderPath() + currentUrl.replaceFirst("http[s]?://", "").replaceFirst("file://", "").replace("/", "_").replace(":", "_") + "/";
			// Make sure to configure ca.concordia.cssanalyser.crawler in Crawler class
			Crawler crawler = new Crawler(currentUrl, outputFolderPath);
			crawler.start();

			// Get all ca.concordia.cssanalyser.dom states in outputFolder/crawljax/doms		
			List<File> allStatesFiles = IOHelper.searchForFiles(outputFolderPath + "crawljax/doms", "html");	
			for (File domStateHtml : allStatesFiles) {

				String stateName = domStateHtml.getName();
				// Remove .html
				String correspondingCSSFolderName = stateName.substring(0, stateName.length() - 5);

				try {

					CSSAnalyser cssAnalyser = new CSSAnalyser(domStateHtml.getAbsolutePath(), outputFolderPath + "css/" + correspondingCSSFolderName);
					cssAnalyser.analyse(params.getFPGrowthMinsup());

				} catch (FileNotFoundException fnfe) {
					LOGGER.warn(fnfe.getMessage());
				}

			}

		}
	}

	private static void doPreprocessorMigration(ParametersParser params) {
		List<String> folders = getFolderPathsFromParameters(params);
		String outFolder = params.getOutputFolderPath();
		LessPrinter lessPrinter = new LessPrinter();
		
		if (folders.size() > 0) {

			for (String folder : folders) {
				List<File> allStatesFiles = IOHelper.searchForFiles(folder + "crawljax/doms", "html");	
				if (allStatesFiles.size() == 0) {
					LOGGER.warn("No HTML file found in " + folder + "crawljax/doms, skipping this folder");
				} else {
					for (File domStateHtml : allStatesFiles) {

						String stateName = domStateHtml.getName();
						// Remove .html
						String correspondingCSSFolderName = stateName.substring(0, stateName.length() - 5);

						FileLogger.addFileAppender(folder + "css/log.log", false);
						List<File> cssFiles = IOHelper.searchForFiles(folder + "css/" + correspondingCSSFolderName, "css");

						for (File f : cssFiles) {
							try {
								CSSParser parser = CSSParserFactory.getCSSParser(CSSParserType.LESS);
								StyleSheet styleSheet = parser.parseExternalCSS(f.getAbsolutePath());
								LessMigrationOpportunitiesDetector preprocessorOpportunities = new LessMigrationOpportunitiesDetector(styleSheet);
								List<LessMixinMigrationOpportunity> migrationOpportunities = preprocessorOpportunities.findMixinOpportunities(true);
								Collections.sort(migrationOpportunities, new Comparator<LessMixinMigrationOpportunity>() {
									@Override
									public int compare(LessMixinMigrationOpportunity o1, LessMixinMigrationOpportunity o2) {
										if (o1.getRank() == o2.getRank()) {
											return 1;
										}
										return Double.compare(o1.getRank(), o2.getRank());
									}
								});
								int i = 0;
								for (MixinMigrationOpportunity<com.github.sommeri.less4j.core.ast.StyleSheet> migrationOpportunity : migrationOpportunities) {

									boolean preservesPresentation = migrationOpportunity.preservesPresentation();
									if (!preservesPresentation) {
										LOGGER.warn("The following migration opportunity do not preserve the presentation:");
									}
									String path = outFolder + f.getName() + "migrated" + ++i + ".less";
									IOHelper.writeStringToFile(lessPrinter.getString(migrationOpportunity.apply()), path);
									LOGGER.info("Created Mixin {}, new file has been written to {}", migrationOpportunity.getMixinName(), path);
								}

							}
							catch (ParseException e) {
								LOGGER.warn("Parse exception in parsing " + f.getAbsolutePath());
							}
						}

					}
				}
			}

		} else if (null != params.getFilePath() && !"".equals(params.getFilePath())) {
			try {

				CSSParser parser = CSSParserFactory.getCSSParser(CSSParserType.LESS);
				StyleSheet styleSheet = parser.parseExternalCSS(params.getFilePath());
				LessMigrationOpportunitiesDetector preprocessorOpportunities = new LessMigrationOpportunitiesDetector(styleSheet);
				Iterable<LessMixinMigrationOpportunity> refactoringOpportunities = preprocessorOpportunities.findMixinOpportunities(true);
				System.out.println(refactoringOpportunities);

			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		else 
			LOGGER.error("No CSS file is provided.");
	}

	private static void doMixinsExtractionEmpiricalStudy(ParametersParser params) throws IOException {

		String outfolder = params.getOutputFolderPath();
		int maxDeclarations = params.getMaxDeclarations();
		int maxParameters = params.getMaxParameters();
		int maxCalls = params.getMaxCalls();
		
		LOGGER.info("Max Mixin calls {}, Max Declarations {}, Max Parameters {}", maxCalls, maxDeclarations, maxParameters);
		
		String opportunitiesCsvOutputPath = outfolder + String.format("/migrationOpportunities[maxDecls%s, maxParams%s, maxCalls%s].csv", maxDeclarations, maxParameters, maxCalls);
		CSVColumns fileColumns = new CSVColumns("WebSite", "File", "Parameters", "Declarations", "DeclarationsUsingParams",
				"CrossBrowserDeclarations", "NonCrossBrowserDeclarations", "UniqueParametersUsedInMoreThanOneKindOfDeclaration",
				"DeclarationsHavingOnlyHardCoded", "ParametersReusedInVendorSpecific", "VendorSpecificSharingParam",
				/*"GlobalVarsAccessed",*/ "NameOfTheMappedMixinName", "MappedMixinFile", /*"PreservesPresentation",*/
				"Support", "ExactProperties", "ExactSelectors", "NumbefOfInvolvedSelectors", "NumberOfDependenciesInMixin", "NumberOfDependenciesAffectingMixinCallPosition");
		IOHelper.writeStringToFile(fileColumns.getHeader(true), opportunitiesCsvOutputPath);
		
		String cssFilesCSVOutputPath = outfolder + String.format("/cssFiles[maxDecls%s, maxParams%s, maxCalls%s].csv", maxDeclarations, maxParameters, maxCalls);
		CSVColumns cssFileColumns = new CSVColumns("WebSite", "File", "MixinsToConsider", "Selectors", "Declarations", "MigrationOpportunities", "NonOverlapping");
		IOHelper.writeStringToFile(cssFileColumns.getHeader(true), cssFilesCSVOutputPath);
		
		String mixinsToConsiderCSVPath = outfolder + "/mixinsToConsider.csv";
		CSVColumns mixinsCSVColumns = new CSVColumns("WebSite", "File", "MixinName", "Parameters", "Declarations", "DeclarationsUsingParams",
				"CrossBrowserDeclarations", "NonCrossBrowserDeclarations", "UniqueParametersUsedInMoreThanOneKindOfDeclaration", 
				"DeclarationsHavingOnlyHardCoded", "ParametersReusedInVendorSpecific", "VendorSpecificSharingParam",
				"GlobalVarsAccessed", "MixinCalls");
		
		IOHelper.writeStringToFile(mixinsCSVColumns.getHeader(true), mixinsToConsiderCSVPath, false);
		
		
		String differencesPath = outfolder + String.format("/differences.txt[maxDecls%s, maxParams%s, maxCalls%s].csv", maxDeclarations, maxParameters, maxCalls);
		
		processLessFiles(params, (percentage, website, pathToLessFile) -> {

			LOGGER.info(String.format("%3s%%: Parsing %s", percentage, pathToLessFile));

			try {
				// Get the less style sheet
				com.github.sommeri.less4j.core.ast.StyleSheet styleSheet = LessCSSParser.getLessStyleSheet(new ModifiedLessFileSource(new File(pathToLessFile)));

				LOGGER.info(String.format("%3s%%: Finding mixins in %s", percentage, pathToLessFile));
				
				LessASTQueryHandler lessASTQueryHandler = new LessASTQueryHandler(styleSheet);
				Map<LessMixinDeclaration, Set<Selector>> mixinCallsMap = lessASTQueryHandler.getMixinDeclarationsAndSelectorsTheyWereCalledIn();
				
				List<LessMixinDeclaration> mixinsToConsider = mixinCallsMap.keySet().stream()
						.filter(mixinDeclaration -> mixinDeclaration.getPropertiesAtTheDeepestLevel(false).size() > 0 && mixinCallsMap.get(mixinDeclaration).size() >= 2)
						.collect(Collectors.toList());

				if (mixinsToConsider.size() == 0) {
					LOGGER.warn("No mixin found in {} that was called more than once", website);
				} else {
					
					int numberOfMixinsToConsider = mixinsToConsider.size();
					
					writeMixinsToFile(mixinsToConsiderCSVPath, website, mixinCallsMap, mixinsCSVColumns);

					LOGGER.info(String.format("%3s%%: Compiling %s", percentage, pathToLessFile));
	
					// Compile the less style sheet to CSS
					StyleSheet compiled = LessHelper.compileLESSStyleSheet(styleSheet);
					/*
					 * Format the style sheet.
					 * This is really important to keep the location information consistent,
					 * as we will use the toString of the StyleSheet object
					 * in order to re-parse the style sheet in the later phases.
					 */
					LOGGER.info(String.format("%3s%%: Parsing the resuling style sheet compiled from %s", percentage, pathToLessFile));

					compiled = CSSParserFactory.getCSSParser(CSSParserType.LESS).parseCSSString(compiled.toString());

					LOGGER.info(String.format("%3s%%: Getting migration opportunities for %s", percentage, pathToLessFile));

					LessMigrationOpportunitiesDetector preprocessorOpportunityDetector = new LessMigrationOpportunitiesDetector(compiled);
					// Get mixin opportunities, without subsumed
					List<LessMixinMigrationOpportunity> migrationOpportunities = preprocessorOpportunityDetector.findMixinOpportunities(true);
					
					if (maxDeclarations > 1) {
						migrationOpportunities = migrationOpportunities.stream()
								.filter(migrationOpportunity -> ((Collection<MixinDeclaration>)migrationOpportunity.getAllMixinDeclarations()).size() <= maxDeclarations)
								.collect(Collectors.toList());
					}
					
//					if (maxParameters >= 0) {
//						migrationOpportunities = migrationOpportunities.stream()
//								.filter(migrationOpportunity -> migrationOpportunity.getNumberOfParameters() <= maxParameters)
//								.collect(Collectors.toList());
//					}
					
					if (maxCalls > 2) {
						migrationOpportunities = migrationOpportunities.stream()
								.filter(migrationOpportunity -> ((Collection<Selector>)migrationOpportunity.getInvolvedSelectors()).size() <= maxCalls)
								.collect(Collectors.toList());
					}
					
					int numberOfMigrationOpportunities = migrationOpportunities.size();
					int numberOfSelectors = ((Set<Selector>)compiled.getAllSelectors()).size();
					int numberOfDeclarations = (compiled.getAllDeclarations()).size();
					LOGGER.info(String.format("Found %s migration opportunities", numberOfMigrationOpportunities)) ;
					Set<LessMixinDeclaration> foundRealMixins = new HashSet<>();
					
					if (maxParameters >= 0) {
						numberOfMigrationOpportunities = 0;
					}
					
					int numberOfNonOverlappingOpportunities = 0;
					List<Set<String>> alreadySeenProperties = new ArrayList<>();
					List<Set<Selector>> alreadySeenSelectors = new ArrayList<>(); 
					for (LessMixinMigrationOpportunity migrationOpportunity : migrationOpportunities) {
						
						if (maxParameters >= 0 && migrationOpportunity.getNumberOfParameters() <= maxParameters) {
							numberOfMigrationOpportunities++;
						}
						
						Set<String> propertiesInOpportunity = migrationOpportunity.getPropertiesAtTheDeepestLevel();
						Set<Selector> involvedSelectors = (Set<Selector>)migrationOpportunity.getInvolvedSelectors();
						boolean hasOverlap = false;
						for (int i = 0; i < alreadySeenProperties.size(); i++) {
							Set<String> properties = new HashSet<>(propertiesInOpportunity);
							Set<Selector> selectors = new HashSet<>(involvedSelectors);
							properties.retainAll(alreadySeenProperties.get(i));
							selectors.retainAll(alreadySeenSelectors.get(i));
							if (properties.size() > 0 && selectors.size() > 0) {
								hasOverlap = true;
								break;
							}
						}
						
						if (!hasOverlap) {
							numberOfNonOverlappingOpportunities++;
						}
						
						alreadySeenProperties.add(propertiesInOpportunity);
						alreadySeenSelectors.add(involvedSelectors);
						
						
						String mixinMappedToName = "";
						String mixinMappedToFile = "";
						boolean exactProperties = false; 
						boolean exactSelectors = false;
						for (LessMixinDeclaration realLessMixinDeclaration : mixinsToConsider) {
							exactProperties = false; 
							exactSelectors = false;
							Set<String> propertiesInRealMixin = realLessMixinDeclaration.getPropertiesAtTheDeepestLevel(false);
							if (propertiesInOpportunity.containsAll(propertiesInRealMixin)) {
								Set<BaseSelector> realMixinCalledInSelectors = getBaseSelectorsFromSelectors(mixinCallsMap.get(realLessMixinDeclaration));
								Set<BaseSelector> opportunityCalledInSelectors = getBaseSelectorsFromSelectors(migrationOpportunity.getInvolvedSelectors());
								Set<Selector> selectorsIntersection = new HashSet<>();
								for (Selector realMixinCalledIn : realMixinCalledInSelectors) {
									boolean selectorFound = false;
									for (Selector selectorInvolvedForOpportunity : opportunityCalledInSelectors) {
										if (!selectorsIntersection.contains(selectorInvolvedForOpportunity) && 
												realMixinCalledIn.selectorEquals(selectorInvolvedForOpportunity, false)) {
												selectorsIntersection.add(selectorInvolvedForOpportunity);
												selectorFound = true;
												break;
										}
										if (selectorFound) {
											break;
										}
									}
								}
								if (selectorsIntersection.size() == realMixinCalledInSelectors.size()) {
									
									if (maxParameters > -1 && migrationOpportunity.getNumberOfParameters() > maxParameters) {
										MixinMigrationOpportunity<?> subOpportunity = migrationOpportunity.getSubOpportunity(propertiesInRealMixin, selectorsIntersection);
										if (subOpportunity.getNumberOfParameters() > maxParameters) {	
											continue;
										} else {
											numberOfMigrationOpportunities++;
										}
									}
									
									exactProperties = propertiesInRealMixin.equals(propertiesInOpportunity);
									exactSelectors = selectorsIntersection.size() == involvedSelectors.size();
									mixinMappedToName = realLessMixinDeclaration.toString();
									mixinMappedToFile = realLessMixinDeclaration.getStyleSheetPath();
									foundRealMixins.add(realLessMixinDeclaration);
								}
							}
						}			

						boolean preservesPresentation = migrationOpportunity.preservesPresentation();
						if (!preservesPresentation) {
							StringBuilder builder = new StringBuilder();
							builder.append(pathToLessFile).append(System.lineSeparator());
							builder.append(migrationOpportunity.getInvolvedSelectors()).append(System.lineSeparator());
							builder.append(migrationOpportunity.toString()).append(System.lineSeparator());
							builder.append("-------------").append(System.lineSeparator()).append(System.lineSeparator());
							IOHelper.writeStringToFile(builder.toString(), outfolder + "/notpreserving.txt", true);
						}

						String row = String.format(fileColumns.getRowFormat(true),
								website,
								pathToLessFile,
								migrationOpportunity.getNumberOfParameters(),
								migrationOpportunity.getNumberOfMixinDeclarations(),
								migrationOpportunity.getNumberOfDeclarationsUsingParameters(),
								migrationOpportunity.getNumberOfUniqueCrossBrowserDeclarations(), 
								migrationOpportunity.getNumberOfNonCrossBrowserDeclarations(),
								migrationOpportunity.getNumberOfUniqueParametersUsedInMoreThanOneKindOfDeclaration(),
								migrationOpportunity.getNumberOfDeclarationsHavingOnlyHardCodedValues(),
								migrationOpportunity.getNumberOfUniqueParametersUsedInVendorSpecific(),
								migrationOpportunity.getNumberOfVendorSpecificSharingParameter(),
								/*migrationOpportunity.getNumberOfVariablesOutOfScopeAccessed(),*/
								mixinMappedToName,
								mixinMappedToFile,
								/*preservesPresentation,*/
								migrationOpportunity.getRank(),
								exactProperties,
								exactSelectors,
								((Set<Selector>)migrationOpportunity.getInvolvedSelectors()).size(),
								migrationOpportunity.getNumberOfIntraSelectorDependenciesInMixin(),
								migrationOpportunity.getNumberOfIntraSelectorDependenciesAffectingMixinCallPosition()
								);

						IOHelper.writeStringToFile(row.replace("#", "\\#"), opportunitiesCsvOutputPath, true);	
						
						//System.out.println((new LessPrinter()).getString(migrationOpportunity.apply()));
					}
					mixinsToConsider.removeAll(foundRealMixins);
					if (mixinsToConsider.size() > 0) {
						IOHelper.writeStringToFile(pathToLessFile + ":" + mixinsToConsider.toString() + "\n", differencesPath, true);
					}
					
					String cssRow = String.format(cssFileColumns.getRowFormat(true),
							website,
							pathToLessFile,
							numberOfMixinsToConsider,
							numberOfSelectors,
							numberOfDeclarations,
							numberOfMigrationOpportunities,
							numberOfNonOverlappingOpportunities);
					
					IOHelper.writeStringToFile(cssRow.replace("#", "\\#"), cssFilesCSVOutputPath, true);

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		});

	}

	private static void writeMixinsToFile(String path, String website, Map<LessMixinDeclaration, Set<Selector>> mixinsToConsider, CSVColumns columns) {

		for (LessMixinDeclaration mixinDeclarationInfo : mixinsToConsider.keySet()) {
			Set<Selector> mixinCalls = mixinsToConsider.get(mixinDeclarationInfo);
			if (mixinCalls.size() >= 2 && mixinDeclarationInfo.getPropertiesAtTheDeepestLevel(false).size() > 0) {
				IOHelper.writeStringToFile(
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
								mixinDeclarationInfo.getNumberOfVariablesOutOfScopeAccessed(),
								mixinCalls.size()
								)
						, path, true);

			}
		}

	}

	private static Set<BaseSelector> getBaseSelectorsFromSelectors(Iterable<Selector> setOfSelectors) {
		Set<BaseSelector> baseSelectorsToReturn = new HashSet<>();
		for (Selector selector : setOfSelectors) {
			if (selector instanceof GroupingSelector) {
				baseSelectorsToReturn.addAll((Set<BaseSelector>)((GroupingSelector)selector).getBaseSelectors());
			} else if (selector instanceof BaseSelector) {
				baseSelectorsToReturn.add((BaseSelector)selector);
			}
		}
		return baseSelectorsToReturn;
	}

	private static List<String> getFolderPathsFromParameters(ParametersParser params) {
		List<String> folders = new ArrayList<>();

		if (params.getInputFolderPath() != null)
			folders.add(params.getInputFolderPath());
		else if (params.getListOfFoldersPathsToBeAnayzedFile() != null) {
			folders.addAll(params.getFoldersListToBeAnalyzed());
		} else {
			return new ArrayList<>();
		}
		return folders;
	}
	
}
