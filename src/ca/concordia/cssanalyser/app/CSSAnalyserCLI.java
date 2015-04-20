package ca.concordia.cssanalyser.app;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;

import ca.concordia.cssanalyser.analyser.CSSAnalyser;
import ca.concordia.cssanalyser.crawler.Crawler;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.io.IOHelper;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorMigrationOpportunitiesDetector;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessMigrationOpportunitiesDetector;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessMixinOpportunityApplier;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessPrinter;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
import ca.concordia.cssanalyser.parser.CSSParser;
import ca.concordia.cssanalyser.parser.CSSParserFactory;
import ca.concordia.cssanalyser.parser.CSSParserFactory.CSSParserType;
import ca.concordia.cssanalyser.parser.ParseException;
import ca.concordia.cssanalyser.parser.less.LessCSSParser;
import ca.concordia.cssanalyser.preprocessors.empiricalstudy.EmpiricalStudy;

import com.github.sommeri.less4j.LessSource.FileSource;

public class CSSAnalyserCLI {
	
	private static Logger LOGGER = FileLogger.getLogger(CSSAnalyserCLI.class);

	public static void main(String[] args) throws IOException {
		
		ParametersParser params = new ParametersParser(args);
		
		switch (params.getProgramMode()) {
		case CRAWL: {
			if (params.getOutputFolderPath() == null) {
				LOGGER.error("Please provide an output folder using --outfolder:out/folder.");
				return;
			} else if (params.getUrl() == null && params.getListOfURLsToAnalyzeFilePath() == null) {
				LOGGER.error("Please provide a url using --url:http://url/to/site or the file containing list of urls using --urlfile:path/to/url");
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
			
			break;
		}
		case FOLDER: {

			List<String> folders = getFolderPathsFromParameters(params);

			if (folders.size() == 0) {
				LOGGER.error("Please provide an input folder with --infolder:in/folder or list of folders using --foldersfile:path/to/file.");
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
			break;
		}
		case NODOM: {
			
			CSSAnalyser cssAnalyser = null;
			if (params.getInputFolderPath() != null) {
				try {
					cssAnalyser = new CSSAnalyser(params.getInputFolderPath());
				} catch (FileNotFoundException fnfe) {
					LOGGER.warn(fnfe.getMessage());
				}
			} else {
				LOGGER.error("Please provide an input folder with --infolder:in/folder");
				return;
			}
			cssAnalyser.analyse(params.getFPGrowthMinsup());
			break;
			
		}
		case DIFF: {
			throw new RuntimeException("Not yet implemented");
		}
		case PREP: {

				List<String> folders = getFolderPathsFromParameters(params);
				
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
										PreprocessorMigrationOpportunitiesDetector preprocessorOpportunities = new LessMigrationOpportunitiesDetector(styleSheet);
										List<MixinMigrationOpportunity> migrationOpportunities = preprocessorOpportunities.findMixinOpportunities();
										Collections.sort(migrationOpportunities, new Comparator<MixinMigrationOpportunity>() {
											@Override
											public int compare(MixinMigrationOpportunity o1, MixinMigrationOpportunity o2) {
												if (o1.getRank() == o2.getRank()) {
													return 1;
												}
												return Double.compare(o1.getRank(), o2.getRank());
											}
										});
										LessMixinOpportunityApplier applier = new LessMixinOpportunityApplier();
										for (MixinMigrationOpportunity migrationOpportunity : migrationOpportunities) {
											com.github.sommeri.less4j.core.ast.StyleSheet resultingLESSStyleSheet = applier.apply(migrationOpportunity, styleSheet);

											LessPrinter lessPrinter = new LessPrinter();										
											System.out.println(lessPrinter.getString(resultingLESSStyleSheet));
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
						PreprocessorMigrationOpportunitiesDetector preprocessorOpportunities = new LessMigrationOpportunitiesDetector(styleSheet);
						Iterable<MixinMigrationOpportunity> refactoringOpportunities = preprocessorOpportunities.findMixinOpportunities();
						System.out.println(refactoringOpportunities);
						
	
					} catch (ParseException e) {
	//
	//					e.printStackTrace();
	//
					}
				}
				else 
					LOGGER.error("No CSS file is provided.");
				break;
			}
			case EMPIRICAL_STUDY: {
				List<String> folders = getFolderPathsFromParameters(params);
				String outfolder = params.getOutputFolderPath();
				if (folders.size() > 0) {

					for (String folder : folders) {
						
						FileLogger.addFileAppender(outfolder + "/log.log", false);
						List<File> lessFiles = IOHelper.searchForFiles(folder, "less");

						boolean header = true;
						for (int i = 0; i < lessFiles.size(); i++) {
							File f = lessFiles.get(i);
							LOGGER.info(String.format("%3s%%: %s", (float)i / lessFiles.size() * 100, f.getAbsolutePath()));
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
					LOGGER.warn("No input folder is provided.");
				}
			}
			default:
		}		
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
