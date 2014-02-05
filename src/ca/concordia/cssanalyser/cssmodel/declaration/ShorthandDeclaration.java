package ca.concordia.cssanalyser.cssmodel.declaration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.declaration.value.ValueType;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;


/**
 * Representation of shorthand declarations
 * @author Davood Mazinanian
 *
 */
public class ShorthandDeclaration extends Declaration {

	private Map<String, Declaration> individualDeclarations;
	
	private static final Map<String, Set<String>> shorthandProperties = new HashMap<>();
	
	private boolean isVirtual = false;
	
	static {
		initializeShorthandsMap();
	}

	public ShorthandDeclaration(String propertyName, List<DeclarationValue> values, Selector belongsTo, int fileLineNumber, int fileColNumber, boolean important) {
		super(propertyName, values, belongsTo, fileLineNumber, fileColNumber, important);
		if (individualDeclarations == null)
			individualDeclarations =  new HashMap<>();
	}
	
	/**
	 * Sets the value indicating whether this shorthand declaration 
	 * is virtual
	 * (i.e. it has been added as an equivalent for a set of 
	 * individual declarations when finding type III duplication
	 * instances)
	 * @return
	 */
	public void isVirtual(boolean virtual) {
		this.isVirtual = virtual;
	}
	
	/**
	 * Shows whether this shorthand declaration is virtual
	 * (i.e. it has been added as an equivalent for a set of 
	 * individual declarations when finding type III duplication
	 * instances)
	 * @return
	 */
	public boolean isVirtual() {
		return this.isVirtual;
	}
	
	private static void initializeShorthandsMap() {
		
		//addShorthandProperty("animation", )
		
		addShorthandProperty("background", "background-image",
										   "background-repeat",
										   "background-attachement",
										   "background-origin",
										   "background-clip",
										   "background-position",
										   "background-size",
										   "background-color");
		
		addShorthandProperty("border", "border-color",
									   "border-width",
									   "border-style");
		
		addShorthandProperty("border-bottom", "border-bottom-color",
											  "border-bottom-width",
											  "border-bottom-style");
		
		addShorthandProperty("border-left", "border-left-color",
				  							"border-left-width",
				  							"border-left-style");
		
		addShorthandProperty("border-right", "border-right-color",
			  							     "border-right-width",
				  							 "border-right-style");
		
		addShorthandProperty("border-top", "border-top-color",
				  						   "border-top-width",
				  						   "border-top-style");
		
		addShorthandProperty("border-color", "border-left-color",
											 "border-right-color",
											 "border-top-color",
											 "border-bottom-color");
		
		addShorthandProperty("border-width", "border-left-width",
											 "border-right-width",
											 "border-top-width",
											 "border-bottom-width");
		
		addShorthandProperty("border-style", "border-left-style",
											 "border-right-style",
											 "border-top-style",
											 "border-bottom-style");
		
		addShorthandProperty("outline", "outline-color",
						     "outline-width",
						     "outline-style");
		
		//addShorthandProperty("border-image", );
		//addShorthandProperty("target", );
		
		addShorthandProperty("border-radius", "border-top-left-radius",
											  "border-top-right-radius",
											  "border-bottom-right-radius",
											  "border-bottom-left-radius");
		
		addShorthandProperty("list-style", "list-style-type",
										   "list-style-position",
										   "list-style-image");
		
		addShorthandProperty("margin", "margin-left",
									   "margin-right",
									   "margin-top",
									   "margin-bottom");
		
		addShorthandProperty("column-rule", "column-rule-style",
											"column-rule-color",
											"column-rule-width");
		
		addShorthandProperty("columns", "column-width",
										"column-count");
		
		addShorthandProperty("padding", "padding-left",
									    "padding-right",
									    "padding-top",
									    "padding-bottom");
		
		addShorthandProperty("transition", "transition-duration", 
										   "transition-timing-function",
										   "transition-delay", 
										   "transition-property");
		
		addShorthandProperty("font", "font-style",
									 "font-variant",
									 "font-weight",
									 "font-stretch",
									 "font-size",
									 "line-height",
									 "font-family");
	}
	
	private static void addShorthandProperty(String shorthandPropertyName, String... individualPropertyNames) {
		shorthandProperties.put(shorthandPropertyName, new HashSet<>(Arrays.asList(individualPropertyNames)));
	}

	/**
	 * Specifies whether a property is a shorthand or not.
	 * @param property
	 * @return
	 */
	public static boolean isShorthandProperty(String property) {
		property = getNonVendorProperty(property);
		return shorthandProperties.containsKey(property);
	}
	
	public static Set<String> getIndividualPropertiesForAShorthand(String shorthandProperty) {
		Set<String> result = new HashSet<>();
		Set<String> currentLevel = shorthandProperties.get(shorthandProperty);
		if (currentLevel != null) {
			result.addAll(currentLevel);
			for (String property : currentLevel)
				result.addAll(getIndividualPropertiesForAShorthand(property));
		}
		return result;
	}
	
	/**
	 * If a property could become a part of a shorthand property, this method returns
	 * those shorthand properties. For example, border-left-color could be a part of 
	 * border-color or border-left shorthand properties. So this method would return them.
	 * If not, the returned set is empty. 
	 * @param property
	 * @return
	 */
	// TODO: Maybe consider using a BiMap
	public static Set<String> getShorthandPropertyNames(String property) {
		String nonVendorproperty = getNonVendorProperty(property);
		String prefix = "";
		if (!property.equals(nonVendorproperty))
			prefix = property.substring(0, property.indexOf(nonVendorproperty));
		Set<String> toReturn = new HashSet<>();
		for (Entry<String, Set<String>> entry : shorthandProperties.entrySet()) {
			if (entry.getValue().contains(nonVendorproperty)) {
				toReturn.add(prefix + entry.getKey());
				// This method has to act recursively, to return border for border-left-width
				Set<String> recursiveProperties = getShorthandPropertyNames(entry.getKey());
				for (String s : recursiveProperties)
					toReturn.add(prefix + s);
			}
		}
		return toReturn;
	}
	
	/**
	 * Adds an individual declaration to the list of individual declarations
	 * for current declaration. 
	 * @param propertyName The name of the property as String
	 * @param values Values of the individual declaration.
	 */
	public void addIndividualDeclaration(String propertyName, DeclarationValue... values) {
		
		if (individualDeclarations == null) {
			individualDeclarations =  new HashMap<>();
		}
		
		List<DeclarationValue> valuesList = new ArrayList<>(Arrays.asList(values));
		// Because DeclarationFactory.getDeclaration() method only accepts a list of DeclarationValues
		
		Declaration individual = individualDeclarations.get(propertyName);
		
		if (isCommaSeparatedListOfValues && individual != null) {
			individual.getRealValues().add(new DeclarationValue(",", ValueType.SEPARATOR));
			for (DeclarationValue v : valuesList) {
				individual.getRealValues().add(v);
			}
		} else {
			individual = DeclarationFactory.getDeclaration(propertyName, valuesList, parentSelector, lineNumber, colNumber, isImportant);
		}
			
		addIndividualDeclaration(individual);
	}
	
	/**
	 * Adds an individual declaration to this shorthand declaration. 
	 * This method first clones the given declaration then 
	 * calls {@link DeclarationValue#setIsAMissingValue(boolean)} 
	 * method with <code>false</code> argument for each value of 
	 * cloned declaration. Then it adds it to the list of individual
	 * declarations of current shorthand declration.
	 * @param declaration
	 */
	public void addIndividualDeclaration(Declaration declaration) {
		
		if (individualDeclarations == null)
			individualDeclarations =  new HashMap<>();

		if (!isVirtual) {
		/*
		 * Copy, so if we are adding a real declaration, we don't want to
		 * modify it. 
		 * 
		 */
			declaration = declaration.clone();
			for (DeclarationValue v : declaration.declarationValues) {
				v.setIsAMissingValue(false);
			}
		}
		
		individualDeclarations.put(declaration.getProperty(), declaration);
		
	}
	
	/**
	 * Returns all individual declarations that constitute this shorthand.
	 * For example, for shorthand declaration <code>margin: 2px 4px;</code>
	 * this method returns:
	 * <ul>
	 * 	<li><code>margin-top: 2px;</code></li>
	 * 	<li><code>margin-right: 4px;</code></li>
	 * 	<li><code>margin-bottom: 2px;</code></li>
	 * 	<li><code>margin-left: 4px;</code></li>
	 * </ul>
	 * @return A collection of individual {@link Declaration}s
	 */
	public Collection<Declaration> getIndividualDeclarations() {
		return individualDeclarations.values();
	}
	
	/**
	 * Compares two shorthand declarations to see whether they
	 * are the same shorthand property and they have 
	 * the equal or equivalent set of individual properties. This 
	 * method is mainly used in finding type III duplications. 
	 * @param otherDeclaration
	 * @return True if the mentioned criteria are true;
	 */
	public boolean individualDeclarationsEquivalent(ShorthandDeclaration otherDeclaration) {
		
		if (individualDeclarations.size() != otherDeclaration.individualDeclarations.size())
			return false;
		
		if (!property.equals(otherDeclaration.property))
			return false;
		
		for (Entry<String, Declaration> entry : individualDeclarations.entrySet()) {
			Declaration otherIndividualDeclaration = otherDeclaration.individualDeclarations.get(entry.getKey());
			Declaration checkingIndividualDeclaration = entry.getValue();
			if (otherIndividualDeclaration != null && 
					(checkingIndividualDeclaration.declarationIsEquivalent(otherIndividualDeclaration) || 
					 checkingIndividualDeclaration.declarationEquals(otherIndividualDeclaration)
					)
				) {
				;
			} else {
				return false;
			}
		}
			
		return true;
	}

}