package analyser.duplication;

import java.util.Set;

import CSSModel.selectors.Selector;


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
	
	public Set<Selector> getSelectors();

}
