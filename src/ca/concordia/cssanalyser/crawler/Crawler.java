package ca.concordia.cssanalyser.crawler;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import ca.concordia.cssanalyser.crawler.plugin.CSSCatcher;

import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.plugins.crawloverview.CrawlOverview;


/**
 * Uses Crawljax in order to crawl the web page
 * @author Davood Mazinanian
 *
 */
public class Crawler {
	
	private final String websiteURI;
	private final String outputFolder;
	
	public Crawler(String URI, String outputFolderPath) {
		websiteURI = URI;
		outputFolder = outputFolderPath;
	}
	
	/**
	 * Starts crawling the given website using Crawljax and 
	 * CSSCatcher plugin.
	 */
	public void start() {
		
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(websiteURI);
		
		CSSCatcher cssCatcher = new CSSCatcher();
		cssCatcher.setOutputFolder(outputFolder + "css/");
		
		builder.addPlugin(new CrawlOverview());
		builder.addPlugin(cssCatcher);

		configureCrawljax(builder);
		
		CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
		
		crawljax.call();
		
	}
	
	/**
	 * Set Crawljax configuration here
	 * @param builder
	 */
	private void configureCrawljax(CrawljaxConfigurationBuilder builder) {
		
		//builder.crawlRules().clickDefaultElements();
		//builder.crawlRules().dontClick("input").withAttribute("value", "I don't recognize");
		//builder.crawlRules().click("input").withAttribute("type", "submit");
		//builder.crawlRules().dontClick("a").underXPath("//*[@id='pageFooter']");
		//builder.crawlRules().dontClick("a").underXPath("//*[@id='content']/div/div[2]");
		//System.getProperties().setProperty("webdriver.chrome.driver", "chromedriver.exe");
		//builder.setBrowserConfig(new BrowserConfiguration(BrowserType.CHROME, 2));w
		//builder.setBrowserConfig(new BrowserConfiguration(BrowserType.FIREFOX, 2));
		builder.crawlRules().insertRandomDataInInputForms(false);
		builder.crawlRules().clickElementsInRandomOrder(true);
		builder.crawlRules().crawlFrames(true);
		

		//builder.crawlRules().dontClick("*");
		//InputSpecification input = new InputSpecification();
		// when Crawljax encouters a form element with the id or name "q" enter "Crawljax"
//		inputSpecification.setValuesInForm(loginForm).beforeClickElement("input").withAttribute("type", "submit").withAttribute("value", "Log In");
		//String uname = "";
		//String passwd = "";
		//input.field("username").setValue(uname);
		//input.field("passwd").setValue(passwd);
		//builder.crawlRules().setInputSpec(input);
		//builder.crawlRules().click("button");
		
		builder.setOutputDirectory(new File(outputFolder + "/crawljax"));
						
		builder.setMaximumDepth(1);
		builder.setMaximumStates(2);
		
	}


	/**
	 * Returns hrefs of all initial CSSs of a web site
	 * @return
	 */
	public Collection<String> getInitialCSSHrefs() {
		
		Set<String> allHrefs = new HashSet<>();

		// Code adapted from seleniumhq
		
		// Create a new instance of the Firefox driver
		// Notice that the remainder of the code relies on the interface,
		// not the implementation.
		WebDriver driver = new FirefoxDriver();

		// And now use this to visit Google
		driver.get(websiteURI);
		// Alternatively the same thing can be done like this
		// driver.navigate().to("http://www.google.com");

		// Check the title of the page
		// System.out.println("Page title is: " + driver.getTitle());

		// Google's search is rendered dynamically with JavaScript.
		// Wait for the page to load, timeout after 10 seconds
		// (new WebDriverWait(driver, 10)).until(new
		// ExpectedCondition<Boolean>() {
		// public Boolean apply(WebDriver d) {
		// return d.getTitle().toLowerCase().startsWith("cheese!");
		// }
		// });

		JavascriptExecutor js = (JavascriptExecutor) driver;

		int styleSheetLength = Integer.valueOf(js.executeScript("return document.styleSheets.length").toString());
		for (int i = 0; i < styleSheetLength; i++) {
			Object hrefObj = js.executeScript("return document.styleSheets[" + i + "].href");
			if (hrefObj == null)
				continue;
			String href = hrefObj.toString();
			if (href != null) {
				allHrefs.add(href);
			}
		}

		// Close the browser
		driver.quit();
		
		return allHrefs;
	}
}
