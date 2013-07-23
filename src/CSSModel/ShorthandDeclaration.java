/**
 * 
 */
package CSSModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is another representation for shorthand properties.
 * A shorthand property is kept along with all its constituting 
 * properties. Default values are added as well. So, when comparing 
 * two declarations, we will have even more equal values,
 * cause margin: 2px would be equal to margin: 2px 2px and margin: 2px 2px 2px 2px
 *    
 * @author Davood Mazinanian
 *
 */
public class ShorthandDeclaration extends Declaration {

	private final Set<Declaration> constitutiveDeclarations;
	
	/**
	 * @param property
	 * @param values
	 * @param belongsTo
	 * @param important
	 */
	public ShorthandDeclaration(String property, List<DeclarationValue> values,
			Selector belongsTo, boolean important) {
		this(property, values, belongsTo, -1, -1, important); 
	}

	/**
	 * @param property
	 * @param values
	 * @param belongsTo
	 * @param fileLineNumber
	 * @param fileColNumber
	 * @param important
	 */
	public ShorthandDeclaration(String property, List<DeclarationValue> values,
			Selector belongsTo, int fileLineNumber, int fileColNumber,
			boolean important) {
		super(property, values, belongsTo, fileLineNumber, fileColNumber,
				important);
		constitutiveDeclarations = new HashSet<>();
		extractConstiutiveDeclarations();
	}

	private void extractConstiutiveDeclarations() {
		switch (property) {
		/*
		 * W3C:
		 * 	[ <bg-layer> , ]* <final-bg-layer>
		 *  <bg-layer> : <background-image> [(<background-size>)]? <background-repeat>? <background-position>? <background-attachment>? [<background-clip> <background-origin>?]?
		 *  <final-bg-layer> : <background-image> || (<background-size>) || <background-repeat> || <background-position> || <background-attachment> 
		 *  					|| [<background-clip> <background-origin>?] || <background-color>
		 */
		case "background":  
			
			break;

		default:
			break;
		}
		
	}

}
