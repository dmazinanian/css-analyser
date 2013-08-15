package app;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.condition.NotRegexCondition;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.plugin.OnInvariantViolationPlugin;
import com.crawljax.plugins.crawloverview.CrawlOverview;

public class TestApp {

	public static void main(String[] args) throws IOException {
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor("http://gmail.com/");
		
			
		builder.addPlugin(new CrawlOverview());

		//System.getProperties().setProperty("webdriver.chrome.driver", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
		builder.setBrowserConfig(new BrowserConfiguration(BrowserType.FIREFOX, 1));
		
		builder.crawlRules().insertRandomDataInInputForms(false);
		//builder.crawlRules().clickDefaultElements();
		builder.crawlRules().setInputSpec(getInputSpecification());
		builder.crawlRules().dontClick("a");
		builder.crawlRules().click("input").underXPath("//*[@id='signIn']");
		
		CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
		
		crawljax.call();
		
	}

	private static InputSpecification getInputSpecification() {
		InputSpecification input = new InputSpecification();
		Form contactForm = new Form();
		contactForm.field("Email").setValues("dmazinanian");
		contactForm.field("Passwd").setValues("");
		input.setValuesInForm(contactForm).beforeClickElement("input").underXPath("//*[@id='signIn']");
		return input;
	}

}
