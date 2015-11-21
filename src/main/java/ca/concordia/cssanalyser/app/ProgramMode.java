package ca.concordia.cssanalyser.app;

/**
 * Application running modes
 * @author Davood Mazinanian
 */
enum ProgramMode {
	/** Find CSS files in a folder (resulted from a 
	 * previous crawling) and do the analysis 
	 */
	FOLDER,
	/** 
	 * Crawl the web pages and capture CSS files
	 */
	CRAWL, 
	/** 
	 * Find CSS files inside a given folder and analyze 
	 * without considering DOM states
	 */
	NODOM, 
	/**
	 * DIFF two given CSS files
	 */
	DIFF,
	/** Find preprocessor migration opportunities */
	PREP, 
	/** Perform empirical study */
	EMPIRICAL_STUDY, 
	/** Inline imports in the given less files */
	INLINE_IMPORTS
}
