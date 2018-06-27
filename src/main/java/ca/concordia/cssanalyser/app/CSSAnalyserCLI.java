package ca.concordia.cssanalyser.app;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import ca.concordia.cssanalyser.analyser.CSSAnalyser;
import ca.concordia.cssanalyser.crawler.Crawler;
import ca.concordia.cssanalyser.csshelper.CSSPropertyCategory;
import ca.concordia.cssanalyser.csshelper.CSSPropertyCategoryHelper;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.cssmodel.selectors.SimpleSelector;
import ca.concordia.cssanalyser.io.CSVColumns;
import ca.concordia.cssanalyser.io.IOHelper;
import ca.concordia.cssanalyser.migration.topreprocessors.TransformationStatus;
import ca.concordia.cssanalyser.migration.topreprocessors.TransformationStatus.TransformationStatusEntry;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessHelper;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessMigrationOpportunitiesDetector;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessMixinMigrationOpportunity;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessPrinter;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinDeclaration;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
import ca.concordia.cssanalyser.migration.topreprocessors.sass.SassHelper;
import ca.concordia.cssanalyser.parser.CSSParser;
import ca.concordia.cssanalyser.parser.CSSParserFactory;
import ca.concordia.cssanalyser.parser.CSSParserFactory.CSSParserType;
import ca.concordia.cssanalyser.parser.ParseException;
import ca.concordia.cssanalyser.parser.less.LessCSSParser;
import ca.concordia.cssanalyser.parser.less.ModifiedLessFileSource;
import ca.concordia.cssanalyser.preprocessors.constructsinfo.PreprocessorMixinDeclaration;
import ca.concordia.cssanalyser.preprocessors.constructsinfo.less.LessASTQueryHandler;
import ca.concordia.cssanalyser.preprocessors.constructsinfo.less.LessMixinDeclaration;
import ca.concordia.cssanalyser.preprocessors.constructsinfo.sass.SassMixinDeclaration;
import ca.concordia.cssanalyser.preprocessors.empiricalstudy.EmpiricalStudy;
import ca.concordia.cssanalyser.preprocessors.util.less.ImportInliner;

public class CSSAnalyserCLI {

	public static Logger LOGGER = FileLogger.getLogger(CSSAnalyserCLI.class);

	public static void main(String[] args) throws Exception {

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
		case MIXIN_MIGRATION_EMPIRICAL_LESS:
			doMixinsExtractionEmpiricalStudyLess(params);
			break;
		case MIXIN_MIGRATION_EMPIRICAL_SASS:
			doMixinsExtractionEmpiricalStudySass(params);
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
		case MIXIN_RANKING:
			doRankingTraining(params);
		default:
		}		
	}
	
	interface ProcessPreprocessorFiles {
		void process(int percentage, String website, String pathToMainFile);
	}

	private static void processPreprocessorFiles(String folder, ProcessPreprocessorFiles intrface) {
		//List<String> folders = getFolderPathsFromParameters(params);
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
						String absolutePathToMainPreprocessorFile = mainFilesPathsFile.getParentFile().getAbsolutePath() + File.separator + mainFileRelativePath;
						filesToConsider.add(absolutePathToMainPreprocessorFile);
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

									boolean preservesPresentation = migrationOpportunity.preservesPresentation().isOK();
									if (!preservesPresentation) {
										LOGGER.warn("The following migration opportunity does not preserve the presentation:");
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
	
	private static Function<String, StyleSheet> lessStyleSheetCompilerFunction = (pathToLessFile) -> {
		com.github.sommeri.less4j.core.ast.StyleSheet styleSheet;
		try {
			styleSheet = getLessStyleSheetFromPath(pathToLessFile);	
			return LessHelper.compileLESSStyleSheet(styleSheet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	};
	
	private static Function<String, StyleSheet> sassStyleSheetCompilerFunction = pathToSassFile -> {
		try {
			return SassHelper.compileStyleSheetOnWindowsUsingSass(pathToSassFile);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	};
	
	private static void doMixinsExtractionEmpiricalStudyLess(ParametersParser params) {
		
		BiFunction<String, String, Map<? extends PreprocessorMixinDeclaration, Set<Selector>>> getMixinDeclarationsAndCallsFunction = (website, pathToLessFile) -> {		
					
			com.github.sommeri.less4j.core.ast.StyleSheet styleSheet = getLessStyleSheetFromPath(pathToLessFile);
			
			LessASTQueryHandler lessASTQueryHandler = new LessASTQueryHandler(styleSheet);
			
			return lessASTQueryHandler.getMixinDeclarationsAndSelectorsTheyWereCalledIn();
			
		};
		
		doMixinsExtractionEmpiricalStudy(params, getMixinDeclarationsAndCallsFunction, lessStyleSheetCompilerFunction);
	}

	private static com.github.sommeri.less4j.core.ast.StyleSheet getLessStyleSheetFromPath(String pathToLessFile) {
		com.github.sommeri.less4j.core.ast.StyleSheet styleSheet;
		try {
			styleSheet = LessCSSParser.getLessStyleSheet(new ModifiedLessFileSource(new File(pathToLessFile)));
			return styleSheet;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static void doMixinsExtractionEmpiricalStudySass(ParametersParser params) {
		
		String outputPath = params.getInputFolderPath();
		
		Map<String, List<PreprocessorMixinDeclaration>> websitesToMixinDeclarationsMap = new HashMap<>();
		
		websitesToMixinDeclarationsMap.putAll(readSassOutputFile(outputPath + "/libraries-scss-mixinProperties.txt"));
		websitesToMixinDeclarationsMap.putAll(readSassOutputFile(outputPath + "/websites-sass-mixinProperties.txt"));
		websitesToMixinDeclarationsMap.putAll(readSassOutputFile(outputPath + "/websites-scss-mixinProperties.txt"));
		
		//mixinDeclarationsToCalledSelectorsLibrariesMap.get(null);

		
		BiFunction<String, String, Map<? extends PreprocessorMixinDeclaration, Set<Selector>>> getMixinDeclarationsAndCallsFunction = (website, pathToSassFile) -> {
			
			Map<PreprocessorMixinDeclaration, Set<Selector>> mixinDeclarationsToCalledSelectorsMap = new HashMap<>();
			
			List<PreprocessorMixinDeclaration> mixinDeclarationsForThisWebsite = websitesToMixinDeclarationsMap.get(website);
			
			if (mixinDeclarationsForThisWebsite != null) {
			
				for (PreprocessorMixinDeclaration mixinDeclaration : mixinDeclarationsForThisWebsite) {
					SassMixinDeclaration sassMixinDeclaration = (SassMixinDeclaration)mixinDeclaration;
					Set<Selector> selectorsForThisMixin = new HashSet<>();
					for (int i = 0; i < sassMixinDeclaration.getNumberOfCalls(); i++) {
						SimpleSelector selector = new SimpleSelector();
						selector.addClassName("dummySelector" + i);
						selectorsForThisMixin.add(selector);
					}
					mixinDeclarationsToCalledSelectorsMap.put(sassMixinDeclaration, selectorsForThisMixin);
				}
				
			}
			
			return mixinDeclarationsToCalledSelectorsMap;
			
		};
		
		doMixinsExtractionEmpiricalStudy(params, getMixinDeclarationsAndCallsFunction, sassStyleSheetCompilerFunction);
	}


	private static Map<String, List<PreprocessorMixinDeclaration>> readSassOutputFile(String path) {
		Map<String, List<PreprocessorMixinDeclaration>> toReturn = new HashMap<>(); 
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
			String line;
			while ((line = reader.readLine()) != null) {
				if (!"".equals(line)) {
					String[] headerLine = line.split("\\|");
					String website = headerLine[0];
					String styleSheetPath = headerLine[1];
					String mixinName = headerLine[2];
					int params = Integer.parseInt(headerLine[3]);
					int numberOfCalls = Integer.parseInt(headerLine[4]);
					int numberOfProperties = Integer.parseInt(headerLine[5]);
					SassMixinDeclaration sassMixinDeclaration = new SassMixinDeclaration(mixinName, styleSheetPath, params, numberOfCalls);
					for (int i = 0; i < numberOfProperties; i++) {
						sassMixinDeclaration.addProperty(reader.readLine());
					}
					List<PreprocessorMixinDeclaration> mixinsForThisWebsite = toReturn.get(website);
					if (mixinsForThisWebsite == null) {
						mixinsForThisWebsite = new ArrayList<>();
					}
					mixinsForThisWebsite.add(sassMixinDeclaration);
					toReturn.put(website, mixinsForThisWebsite);
				}
			}
			reader.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return toReturn;
	}
	
	private static void doRankingTraining(ParametersParser params) {

		String outfolder = params.getOutputFolderPath();
		String lessPath = params.getInputFolderPath() + "/less-projects";
		String sassPath = params.getInputFolderPath() + "/sass-projects";
		int maxDeclarations = params.getMaxDeclarations();
		int maxParameters = params.getMaxParameters();
		int maxCalls = params.getMaxCalls();
		
		LOGGER.info("Max Mixin calls {}, Max Declarations {}, Max Parameters {}", maxCalls, maxDeclarations, maxParameters);
		
		String opportunitiesCsvOutputPath = outfolder + String.format("/migrationOpportunities[maxDecls%s, maxParams%s, maxCalls%s].csv", maxDeclarations, maxParameters, maxCalls);
		CSVColumns fileColumns = new CSVColumns("WebSite", "File", "Parameters", "Declarations", "DeclarationsUsingParams",
				"UniqueCrossBrowserDeclarations", "NonCrossBrowserDeclarations", "UniqueParametersUsedInMoreThanOneKindOfDeclaration",
				"DeclarationsHavingOnlyHardCoded", "ParametersReusedInVendorSpecific", "VendorSpecificSharingParam", "CrossBrowserDeclarations",
				"ContainsSupport", "ExactSupport",
				"NumbefOfInvolvedSelectors", "NumberOfDependenciesInMixin", "NumberOfDependenciesAffectingMixinCallPosition",
				"NumberOfPropertyCategories");
		IOHelper.writeStringToFile(fileColumns.getHeader(true), opportunitiesCsvOutputPath);
		
		String timeOutputPath = outfolder + "/time.csv";
		CSVColumns timeColumns = new CSVColumns("WebSite", "File", "OpportunitiesDetectionNanoTime", "OpportunitiesMilliTime");
		IOHelper.writeStringToFile(timeColumns.getHeader(true), timeOutputPath);
		
		
		List<PreprocessorMixinDeclaration> allRealMixinDeclarations = new ArrayList<>();
		
		readSassOutputFile(sassPath + "/libraries-scss-mixinProperties.txt").values().stream().forEach(list -> list.forEach(mixinDeclaraiton -> allRealMixinDeclarations.add(mixinDeclaraiton)));
		readSassOutputFile(sassPath + "/websites-sass-mixinProperties.txt").values().stream().forEach(list -> list.forEach(mixinDeclaraiton -> allRealMixinDeclarations.add(mixinDeclaraiton)));
		readSassOutputFile(sassPath + "/websites-scss-mixinProperties.txt").values().stream().forEach(list -> list.forEach(mixinDeclaraiton -> allRealMixinDeclarations.add(mixinDeclaraiton)));
		
		
		processPreprocessorFiles(lessPath, (percentage, website, pathToLessFile) -> {
			com.github.sommeri.less4j.core.ast.StyleSheet styleSheet = getLessStyleSheetFromPath(pathToLessFile);
			LessASTQueryHandler lessASTQueryHandler = new LessASTQueryHandler(styleSheet);
			LOGGER.info("Getting Mixins in {}", pathToLessFile);
			Map<LessMixinDeclaration, Set<Selector>> lessMixinDeclarationsAndSelectorsTheyWereCalledIn = lessASTQueryHandler.getMixinDeclarationsAndSelectorsTheyWereCalledIn();
			lessMixinDeclarationsAndSelectorsTheyWereCalledIn.keySet().forEach(mixinDeclaration -> allRealMixinDeclarations.add(mixinDeclaration));
		});
		
		Map<String, Function<String, StyleSheet>> lessAndSass = new HashMap<>();
		lessAndSass.put(sassPath, sassStyleSheetCompilerFunction);
		lessAndSass.put(lessPath, lessStyleSheetCompilerFunction);
		
		for (String rootPath : lessAndSass.keySet()) {
			
			Function<String, StyleSheet> styleSheetCompilerFunction = lessAndSass.get(rootPath);

			processPreprocessorFiles(rootPath, (percentage, website, pathToPreprocessorFile) -> {

				try {
					
					LOGGER.info(String.format("%3s%%: Compiling %s", percentage, pathToPreprocessorFile));

					// Compile the preprocessor style sheet to CSS
					StyleSheet compiled = styleSheetCompilerFunction.apply(pathToPreprocessorFile);
					/*
					 * Format the style sheet.
					 * This is really important to keep the location information consistent,
					 * as we will use the toString of the StyleSheet object
					 * in order to re-parse the style sheet in the later phases.
					 */
					LOGGER.info(String.format("%3s%%: Parsing the resuling style sheet compiled from %s", percentage, pathToPreprocessorFile));

					compiled = CSSParserFactory.getCSSParser(CSSParserType.LESS).parseCSSString(compiled.toString());

					LOGGER.info(String.format("%3s%%: Getting migration opportunities for %s", percentage, pathToPreprocessorFile));

					LessMigrationOpportunitiesDetector preprocessorOpportunityDetector = new LessMigrationOpportunitiesDetector(compiled);
					
					long startNanoTime = System.nanoTime();
					long startMilliTime = System.currentTimeMillis();
					// Get mixin opportunities, with subsumed
					List<LessMixinMigrationOpportunity> migrationOpportunities = preprocessorOpportunityDetector.findMixinOpportunities(false);
					long endNanoTime = System.nanoTime();
					long endMilliTime = System.currentTimeMillis();
					
					long nanoDifferenceTime = endNanoTime - startNanoTime;
					long milliDifferenceTime = endMilliTime - startMilliTime;
					
					String timeRow = String.format(timeColumns.getRowFormat(true), 
							website,
							pathToPreprocessorFile,
							nanoDifferenceTime,
							milliDifferenceTime);
					
					IOHelper.writeStringToFile(timeRow.replace("#", "\\#"), timeOutputPath, true);
					
					LOGGER.info("It took {} ({}) seconds", nanoDifferenceTime / 1000000000, milliDifferenceTime / 1000);
					
					for (int mixinIndex = 0; mixinIndex < migrationOpportunities.size(); mixinIndex++) {
						LessMixinMigrationOpportunity migrationOpportunity = migrationOpportunities.get(mixinIndex);
						if (mixinIndex % 1000 == 0) {
							LOGGER.info("Trying to find a matching mixin ({} of {})", mixinIndex + 1, migrationOpportunities.size()) ;
						}
						int exactSupport = 0;
						int containsSupport = 0;
						Set<String> opportunityPropertiesAtTheDeepestLevel = migrationOpportunity.getPropertiesAtTheDeepestLevel();
						for (PreprocessorMixinDeclaration realMixinDeclaration : allRealMixinDeclarations) {
							Set<String> realMmixinPropertiesAtTheDeepestLevel = realMixinDeclaration.getPropertiesAtTheDeepestLevel(false);
							if (opportunityPropertiesAtTheDeepestLevel.containsAll(realMmixinPropertiesAtTheDeepestLevel)) {
								containsSupport++;
								if (realMmixinPropertiesAtTheDeepestLevel.equals(opportunityPropertiesAtTheDeepestLevel)) {
									exactSupport++;
								}
							}
						}
						String row = String.format(fileColumns.getRowFormat(true),
								website,
								pathToPreprocessorFile,
								migrationOpportunity.getNumberOfParameters(),
								migrationOpportunity.getNumberOfMixinDeclarations(),
								migrationOpportunity.getNumberOfDeclarationsUsingParameters(),
								migrationOpportunity.getNumberOfUniqueCrossBrowserDeclarations(), 
								migrationOpportunity.getNumberOfNonCrossBrowserDeclarations(),
								migrationOpportunity.getNumberOfUniqueParametersUsedInMoreThanOneKindOfDeclaration(),
								migrationOpportunity.getNumberOfDeclarationsHavingOnlyHardCodedValues(),
								migrationOpportunity.getNumberOfUniqueParametersUsedInVendorSpecific(),
								migrationOpportunity.getNumberOfVendorSpecificSharingParameter(),
								migrationOpportunity.getNumberOfCrossBrowserProperties(),
								containsSupport,
								exactSupport,
								((Set<Selector>)migrationOpportunity.getInvolvedSelectors()).size(),
								migrationOpportunity.getNumberOfIntraSelectorDependenciesInMixin(),
								migrationOpportunity.getNumberOfIntraSelectorDependenciesAffectingMixinCallPosition(),
								getPropertyCategories(opportunityPropertiesAtTheDeepestLevel).size()
								);
						
						IOHelper.writeStringToFile(row.replace("#", "\\#"), opportunitiesCsvOutputPath, true);	
					}
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
			});
			
		};
				
	}
	

	private static void doMixinsExtractionEmpiricalStudy(ParametersParser params, 
			BiFunction<String, String, Map<? extends PreprocessorMixinDeclaration, Set<Selector>>> mixinCallsMapFunctoin,
			Function<String, StyleSheet> styleSheetCompilerFunction) {

		String outfolder = params.getOutputFolderPath();
		int maxDeclarations = params.getMaxDeclarations();
		int maxParameters = params.getMaxParameters();
		int maxCalls = params.getMaxCalls();
		
		FileLogger.addFileAppender(outfolder + "/log.log", false);
		
		LOGGER.info("Max Mixin calls {}, Max Declarations {}, Max Parameters {}", maxCalls, maxDeclarations, maxParameters);
		
		String opportunitiesCsvOutputPath = outfolder + String.format("/migrationOpportunities[maxDecls%s, maxParams%s, maxCalls%s].csv", maxDeclarations, maxParameters, maxCalls);
		CSVColumns fileColumns = new CSVColumns("WebSite", "File", "Parameters", "Declarations", "DeclarationsUsingParams",
				"CrossBrowserDeclarations", "NonCrossBrowserDeclarations", "UniqueParametersUsedInMoreThanOneKindOfDeclaration",
				"DeclarationsHavingOnlyHardCoded", "ParametersReusedInVendorSpecific", "VendorSpecificSharingParam",
				/*"GlobalVarsAccessed",*/ "NameOfTheMappedMixinName", "MappedMixinFile", /*"PreservesPresentation",*/
				"Support", "ExactProperties", "ExactSelectors",
				"NumbefOfInvolvedSelectors", "NumberOfDependenciesInMixin", "NumberOfDependenciesAffectingMixinCallPosition",
				"NumberOfPropertyCategories");
		IOHelper.writeStringToFile(fileColumns.getHeader(true), opportunitiesCsvOutputPath);
		
		String cssFilesCSVOutputPath = outfolder + String.format("/cssFiles[maxDecls%s, maxParams%s, maxCalls%s].csv", maxDeclarations, maxParameters, maxCalls);
		CSVColumns cssFileColumns = new CSVColumns("WebSite", "File", "MixinsToConsider", "Selectors", "Declarations", "MigrationOpportunities", "NonOverlapping");
		IOHelper.writeStringToFile(cssFileColumns.getHeader(true), cssFilesCSVOutputPath);
		
		String mixinsToConsiderCSVPath = outfolder + "/mixinsToConsider.csv";
		CSVColumns mixinsCSVColumns = new CSVColumns("WebSite", "File", "MixinName", "Parameters", "Declarations", "DeclarationsUsingParams",
				"CrossBrowserDeclarations", "NonCrossBrowserDeclarations", "UniqueParametersUsedInMoreThanOneKindOfDeclaration", 
				"DeclarationsHavingOnlyHardCoded", "ParametersReusedInVendorSpecific", "VendorSpecificSharingParam",
				"GlobalVarsAccessed", "MixinCalls",
				"NumberOfPropertyCategories");
		
		IOHelper.writeStringToFile(mixinsCSVColumns.getHeader(true), mixinsToConsiderCSVPath, false);
		
		String timeOutputPath = outfolder + "/time.csv";
		
		CSVColumns timeColumns = new CSVColumns("WebSite", "File", "OpportunitiesDetectionNanoTime", "OpportunitiesMilliTime");
		IOHelper.writeStringToFile(timeColumns.getHeader(true), timeOutputPath);
		
		
		String differencesPath = outfolder + String.format("/differences.txt[maxDecls%s, maxParams%s, maxCalls%s].csv", maxDeclarations, maxParameters, maxCalls);
		
		processPreprocessorFiles(params.getInputFolderPath(), (percentage, website, pathToLessFile) -> {

			LOGGER.info(String.format("%3s%%: Parsing %s", percentage, pathToLessFile));

			try {
				
				LOGGER.info(String.format("%3s%%: Finding mixins in %s", percentage, pathToLessFile));
				
				Map<? extends PreprocessorMixinDeclaration, Set<Selector>> mixinCallsMap = mixinCallsMapFunctoin.apply(website, pathToLessFile); 
				
				List<PreprocessorMixinDeclaration> mixinsToConsider = mixinCallsMap.keySet().stream()
						.filter(mixinDeclaration -> mixinDeclaration.getPropertiesAtTheDeepestLevel(false).size() > 0 
								&& mixinCallsMap.get(mixinDeclaration).size() >= 2)
						.collect(Collectors.toList());
				
				if (mixinsToConsider.size() == 0) {
					LOGGER.warn("No mixin found in {} that was called more than once", website);
				} else {
					
					int numberOfMixinsToConsider = mixinsToConsider.size();
					
					writeMixinsToFile(mixinsToConsiderCSVPath, website, mixinCallsMap, mixinsCSVColumns);
					LOGGER.info(String.format("%3s%%: Compiling %s", percentage, pathToLessFile));
	
					// Compile the preprocessor style sheet to CSS
					StyleSheet compiled = styleSheetCompilerFunction.apply(pathToLessFile);
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
					long startNanoTime = System.nanoTime();
					long startMilliTime = System.currentTimeMillis();
					// Get mixin opportunities, with subsumed
					List<LessMixinMigrationOpportunity> migrationOpportunities = preprocessorOpportunityDetector.findMixinOpportunities(true);
					
					long endNanoTime = System.nanoTime();
					long endMilliTime = System.currentTimeMillis();
					
					long nanoDifferenceTime = endNanoTime - startNanoTime;
					long milliDifferenceTime = endMilliTime - startMilliTime;
					
					String timeRow = String.format(timeColumns.getRowFormat(true), 
							website,
							pathToLessFile,
							nanoDifferenceTime,
							milliDifferenceTime);
					
					IOHelper.writeStringToFile(timeRow.replace("#", "\\#"), timeOutputPath, true);
					
					LOGGER.info("It took {} ({}) seconds", nanoDifferenceTime / 1000000000, milliDifferenceTime / 1000);
										
					if (maxDeclarations > 1) {
						migrationOpportunities = migrationOpportunities.stream()
								.filter(migrationOpportunity -> ((Collection<MixinDeclaration>)migrationOpportunity.getAllMixinDeclarations()).size() <= maxDeclarations)
								.collect(Collectors.toList());
					}
					
					if (maxParameters >= 0) {
						migrationOpportunities = migrationOpportunities.stream()
								.filter(migrationOpportunity -> migrationOpportunity.getNumberOfParameters() <= maxParameters)
								.collect(Collectors.toList());
					}
					
					if (maxCalls > 2) {
						migrationOpportunities = migrationOpportunities.stream()
								.filter(migrationOpportunity -> ((Collection<Selector>)migrationOpportunity.getInvolvedSelectors()).size() <= maxCalls)
								.collect(Collectors.toList());
					}
					
					int numberOfMigrationOpportunities = migrationOpportunities.size();
					LOGGER.info(String.format("Found %s migration opportunities", numberOfMigrationOpportunities));

					int numberOfSelectors = ((Set<Selector>)compiled.getAllSelectors()).size();
					int numberOfDeclarations = (compiled.getAllDeclarations()).size();
					
					Set<PreprocessorMixinDeclaration> foundRealMixins = new HashSet<>();
					
					if (maxParameters >= 0) {
						numberOfMigrationOpportunities = 0;
					}
					
					int numberOfNonOverlappingOpportunities = 0;
					List<Set<String>> alreadySeenProperties = new ArrayList<>();
					List<Set<Selector>> alreadySeenSelectors = new ArrayList<>(); 
					for (int mixinIndex = 0; mixinIndex < numberOfMigrationOpportunities; mixinIndex++) {
						LessMixinMigrationOpportunity migrationOpportunity = migrationOpportunities.get(mixinIndex);
						if (mixinIndex % 1000 == 0) {
							LOGGER.info("Trying to find a matching mixin ({} of {})", mixinIndex + 1, numberOfMigrationOpportunities) ;
						}
						if (maxParameters >= 0 && migrationOpportunity.getNumberOfParameters() <= maxParameters) {
							numberOfMigrationOpportunities++;
						}
						
						Set<String> propertiesInOpportunity = migrationOpportunity.getPropertiesAtTheDeepestLevel();
						Set<Selector> involvedSelectors = (Set<Selector>)migrationOpportunity.getInvolvedSelectors();
/*						boolean hasOverlap = false;
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
						alreadySeenSelectors.add(involvedSelectors);*/
						
						
						String mixinMappedToName = "";
						String mixinMappedToFile = "";
						boolean exactProperties = false; 
						boolean exactSelectors = false;
						for (PreprocessorMixinDeclaration realMixinDeclaration : mixinsToConsider) {
							exactProperties = false; 
							exactSelectors = false;
							Set<String> propertiesInRealMixin = realMixinDeclaration.getPropertiesAtTheDeepestLevel(false);
							// The opportunity should contain all the properties of the real mixin
							if (propertiesInOpportunity.containsAll(propertiesInRealMixin)) { // (propertiesInOpportunity.equals(propertiesInRealMixin)) {
								//Set<BaseSelector> realMixinCalledInSelectors = getBaseSelectorsFromSelectors(mixinCallsMap.get(realLessMixinDeclaration));
								//Set<BaseSelector> opportunityCalledInSelectors = getBaseSelectorsFromSelectors(migrationOpportunity.getInvolvedSelectors());
								//Set<Selector> selectorsIntersection = new HashSet<>();
								// The opportunity should be in the same selectors as the real mixin (or more)
								/*for (Selector realMixinCalledIn : realMixinCalledInSelectors) {
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
								}*/
								// Is the opportunity called in at least the same places as the real corresponding mixin?
								//if (selectorsIntersection.size() > 0 /*== realMixinCalledInSelectors.size()*/) {
									exactProperties = propertiesInRealMixin.equals(propertiesInOpportunity);
									exactSelectors = /*realMixinCalledInSelectors.size()*/ mixinCallsMap.get(realMixinDeclaration).size() == involvedSelectors.size();
									mixinMappedToName = realMixinDeclaration.toString();
									mixinMappedToFile = realMixinDeclaration.getStyleSheetPath();
									foundRealMixins.add(realMixinDeclaration);
								/*}*/ /*else {
									LOGGER.info(realLessMixinDeclaration + " is called in other places that this opportunity");
									LOGGER.info("mixin is called in " + mixinCallsMap.get(realLessMixinDeclaration));
									LOGGER.info("Opportunity is called in " + migrationOpportunity.getInvolvedSelectors());
								}*/
									if (exactProperties) {
										break;
									}
							}
						}			

						TransformationStatus transformationStatus = migrationOpportunity.preservesPresentation();
						if (!transformationStatus.isOK()) {
							StringBuilder builder = new StringBuilder();
							builder.append(pathToLessFile).append(System.lineSeparator());
							builder.append(migrationOpportunity.getInvolvedSelectors()).append(System.lineSeparator());
							builder.append(migrationOpportunity.toString()).append(System.lineSeparator());
							List<TransformationStatusEntry> statusEntries = transformationStatus.getStatusEntries();
							for (TransformationStatusEntry entry : statusEntries) {
								builder.append(entry.toString()).append(System.lineSeparator());
							}
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
								migrationOpportunity.getNumberOfIntraSelectorDependenciesAffectingMixinCallPosition(),
								getPropertyCategories(migrationOpportunity.getPropertiesAtTheDeepestLevel()).size()
								);
						
						if (getPropertyCategories(migrationOpportunity.getPropertiesAtTheDeepestLevel()).size() == 0) {
							//System.out.println();
							//getPropertyCategories(migrationOpportunity.getPropertiesAtTheDeepestLevel());
						}

						IOHelper.writeStringToFile(row.replace("#", "\\#"), opportunitiesCsvOutputPath, true);	

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
	
	private static Set<CSSPropertyCategory> getPropertyCategories(Iterable<String> properties) {
		Set<CSSPropertyCategory> categories = new HashSet<>();
		for (String property : properties) {
			categories.add(CSSPropertyCategoryHelper.getCSSCategoryOfProperty(Declaration.getNonVendorProperty(Declaration.getNonHackedProperty(property))));
		}
		return categories;
	}

	private static void writeMixinsToFile(String path, String website, Map<? extends PreprocessorMixinDeclaration, Set<Selector>> mixinsToConsider, CSVColumns columns) {

		for (PreprocessorMixinDeclaration mixinDeclarationInfo : mixinsToConsider.keySet()) {
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
								mixinCalls.size(),
								getPropertyCategories(mixinDeclarationInfo.getPropertiesAtTheDeepestLevel(false)).size()
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
