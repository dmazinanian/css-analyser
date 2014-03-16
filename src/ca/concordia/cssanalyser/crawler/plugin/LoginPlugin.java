package ca.concordia.cssanalyser.crawler.plugin;

import org.openqa.selenium.ElementNotVisibleException;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.plugin.PreStateCrawlingPlugin;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.core.state.StateVertex;
import com.google.common.collect.ImmutableList;

public class LoginPlugin implements PreStateCrawlingPlugin {

	@Override
	public void preStateCrawling(CrawlerContext arg0, ImmutableList<CandidateElement> arg1, StateVertex arg2) {
		
		
		//arg0.getBrowser().input(new Identification(), arg1)
		if (arg0.getBrowser().getCurrentUrl().contains("https://accounts.google.com/ServiceLogin?")) {
			EmbeddedBrowser browser = arg0.getBrowser();
			browser.input(new Identification(How.id, "Email"), "");
			browser.input(new Identification(How.id, "Passwd"), "");
			try {
				browser.fireEventAndWait(new Eventable(new Identification(How.id, "signIn"), EventType.click));
			} catch (ElementNotVisibleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

	 

}
