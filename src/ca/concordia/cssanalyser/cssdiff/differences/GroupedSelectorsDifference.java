/**
 * 
 */
package ca.concordia.cssanalyser.cssdiff.differences;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;

/**
 * Two selectors are grouped in one selector in the new stylesheet
 * @author Davood Mazinanian
 */
public class GroupedSelectorsDifference extends Difference {

	public GroupedSelectorsDifference(StyleSheet styleSheet1,
			StyleSheet styleSheet2) {
		super(styleSheet1, styleSheet2);
	}

}
