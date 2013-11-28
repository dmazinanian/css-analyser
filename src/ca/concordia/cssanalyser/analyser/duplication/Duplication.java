package ca.concordia.cssanalyser.analyser.duplication;

import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.selectors.Selector;



/**
 * This interface represents duplications. Every duplication is simply a list
 * of duplication occurrences.
 * 
 * @author Davood Mazinanian
 * 
 */
public interface Duplication {

	/**
	 * Gets the type of duplication
	 * @return
	 */
	public DuplicationType getType();
	
	/**
	 * The set of selectors for which the duplications happened
	 * @return
	 */
	
	public Set<Selector> getSelectors();

}
