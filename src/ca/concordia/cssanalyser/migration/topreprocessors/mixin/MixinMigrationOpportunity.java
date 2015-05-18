package ca.concordia.cssanalyser.migration.topreprocessors.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.PropertyAndLayer;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorMigrationOpportunity;
import ca.concordia.cssanalyser.migration.topreprocessors.differences.StylePropertyValuesDifferenceInValues;

/**
 * Represents a Mixin refactoring opportunity in the preprocessor.
 * Every Mixin refactoring opportunity includes a collection of set of declarations which have the same property.
 * Some of them can have the same values, others will be parameterized based on the differences in the values.
 * @author Davood Mazinanian
 *
 */
public abstract class MixinMigrationOpportunity extends PreprocessorMigrationOpportunity {
	
	private String mixinName;
	
	// Declarations having differences in their values are stored here. 
	private Map<String, List<Declaration>> realDeclarations = new LinkedHashMap<>();

	// List of parameters for this mixin
	private List<MixinParameter> parameters = new ArrayList<>(); 

	// The list of parameterized values in the call sites. 
	// Set of real declarations will be replaced by a call using mixinName and corresponding parameters
	private Map<Declaration, Set<MixinParameterizedValue>> parameterizedValues = new LinkedHashMap<>();
	
	private final Iterable<Selector> involvedSelectors; 
	
	private boolean shouldCalculateParameters = false;
	
	// Map every Declaration to a MixinDeclaration
	private Map<Declaration, MixinDeclaration> declarationToMixinDeclarationMapper = new HashMap<>();
	
	public MixinMigrationOpportunity(Iterable<Selector> forSelectors, StyleSheet forStyleSheet) {
		super(forStyleSheet);
		this.involvedSelectors = forSelectors;
	}

	// The list of properties and MixinValues. The real mixin will be created based on this
	private Map<String, MixinDeclaration> mixinDeclarations = new LinkedHashMap<>();
	
	public Iterable<String> getProperties() {
		List<String> toReturn = new ArrayList<>();
		for (String property : realDeclarations.keySet())
			toReturn.add(property);
		return toReturn;
	}
	
	public String getMixinName() {
		if (mixinName == null)
			return ".newMixin";
		if (!mixinName.startsWith("."))
			return "." + mixinName;
		return mixinName;
	}

	public void setMixinName(String mixinName) {
		this.mixinName = mixinName;
	}
	
	
	public void addDeclarationsWithTheSameProperty(List<Declaration> declarations) {
		String forProperty = declarations.get(0).getProperty();
		List<Declaration> declarationsToBeAdded = new ArrayList<>();
		for (Declaration d : declarations) {
			declarationsToBeAdded.add(d);
		}
		realDeclarations.put(forProperty, declarationsToBeAdded);	
		shouldCalculateParameters = true;
	}
	
	private void calculateParameters() {
		
		if (!shouldCalculateParameters)
			return;
		
		List<MixinParameter> initialParameters = new ArrayList<>();
		parameterizedValues.clear();
		
		/*
		 * If a value (or a set of values) for a style property is different in at least on of the declarations
		 * which are different, it needs to be parameterized.
		 * So we add it as a difference, and then we will make a parameter for every difference
		 */
		
		// Map every property name to a list of differences in different declarations
		Map<String, List<StylePropertyValuesDifferenceInValues>> propertyNameToDifferencesMap = getDifferences();
		
		// Make a parameter for every difference, a literal for every value which is the same, and add the corresponding parameterized values for call sites
		for (String propertyName : realDeclarations.keySet()) {
			List<StylePropertyValuesDifferenceInValues> differencesForThisProperty = propertyNameToDifferencesMap.get(propertyName);
			List<Declaration> declarationsHavingTheSameProperty = realDeclarations.get(propertyName);
			Set<PropertyAndLayer> allSetPropertyAndLayers = getAllSetPropertyAndLayersForDeclarations(declarationsHavingTheSameProperty);
			
			Declaration declarationWithMaxLayers = Collections.max(declarationsHavingTheSameProperty, new Comparator<Declaration>() {
				@Override
				public int compare(Declaration o1, Declaration o2) {
					
					if (o1 instanceof ShorthandDeclaration && ((ShorthandDeclaration) o1).isVirtual())
						return -1;
					else if (o2 instanceof ShorthandDeclaration && ((ShorthandDeclaration)o2).isVirtual())
						return 1;
					
					if (o1.getNumberOfValueLayers() == o2.getNumberOfValueLayers())
						return 1;
					return Integer.compare(o1.getNumberOfValueLayers(), o2.getNumberOfValueLayers());
				}
			});
			
			MixinDeclaration mixinDeclaration = new MixinDeclaration(propertyName, declarationWithMaxLayers, declarationsHavingTheSameProperty);
			
			// For each possible property and layer we will have a parameter or a literal
			for (PropertyAndLayer propertyAndLayer : allSetPropertyAndLayers) {

				MixinValue value = null;

				// If the property and layer is in the difference list, add a parameter. otherwise, add a literal value
				boolean differenceFoundForThisPropertyValueAndLayer = false;
				if (differencesForThisProperty != null) {
					for (StylePropertyValuesDifferenceInValues difference : differencesForThisProperty) {
						if (difference.getForStylePropertyAndLayer().equals(propertyAndLayer)) {
							// Found, add a parameter
							differenceFoundForThisPropertyValueAndLayer = true;
							// Parameter name will be style property
							String parameterName = propertyAndLayer.getPropertyName().replace("-", "_");
							// In the case of multiple layers, add layer number to the parameter name
							if (declarationWithMaxLayers.getNumberOfValueLayers() > 1)
								parameterName += "_" + propertyAndLayer.getPropertyLayer();
							MixinParameter parameter = new MixinParameter(parameterName, propertyAndLayer);
							initialParameters.add(parameter);
							value = parameter;

							// Add parameterized values for real declarations
							for (Declaration declaration : declarationsHavingTheSameProperty) {
								Set<MixinParameterizedValue> setOfParameterizedValues = parameterizedValues.get(declaration);
								if (setOfParameterizedValues == null) {
									setOfParameterizedValues = new HashSet<>();
									parameterizedValues.put(declaration, setOfParameterizedValues);	
								}

								Collection<DeclarationValue> declarationValuesForThisPropertyAndLayer = 
										declaration.getDeclarationValuesForStyleProperty(propertyAndLayer);
								MixinParameterizedValue parameterizedValue = new MixinParameterizedValue(declaration, declarationValuesForThisPropertyAndLayer , parameter);
								setOfParameterizedValues.add(parameterizedValue);
							}

							break;
						}
					}
				} else { // Difference does not exist at all!
					differenceFoundForThisPropertyValueAndLayer = false;
				}

				if (!differenceFoundForThisPropertyValueAndLayer) {
					// Need to add a literal
					// But if all values in all declarations are missing, ignore,
					// Except if the values we are adding are the only values of this declaration!
					
					boolean theOnlyValuesOfThisDeclaration = 
							declarationsHavingTheSameProperty.get(0).getAllSetPropertyAndLayers().size() == 1;
					
					boolean allMissing = true;
					for (Declaration declaration : declarationsHavingTheSameProperty) {
						Collection<DeclarationValue> declarationValuesForStyleProperty = declaration.getDeclarationValuesForStyleProperty(propertyAndLayer);
						if (declarationValuesForStyleProperty != null) {
							for (DeclarationValue v : declarationValuesForStyleProperty) {
								if (!v.isAMissingValue()) {
									allMissing = false;
									break;
								}
							}
							if (!allMissing)
								break;
						}
					}
					
					if (theOnlyValuesOfThisDeclaration || !allMissing) {
						Collection<DeclarationValue> declarationValuesForStyleProperty =
								declarationsHavingTheSameProperty.get(0).
								getDeclarationValuesForStyleProperty(propertyAndLayer);
						value = new MixinLiteral(declarationValuesForStyleProperty, propertyAndLayer);
					} else {
						value = null;
					}
				}
				
				if (value != null)
					mixinDeclaration.addMixinValue(propertyAndLayer, value);
			}
			
			mixinDeclarations.put(propertyName, mixinDeclaration);
			for (Declaration declaration : declarationsHavingTheSameProperty) {
				declarationToMixinDeclarationMapper.put(declaration, mixinDeclaration);
			}

		}
				
		// We have to minimize the parameters
		this.parameters = initialParameters;
		
		shouldCalculateParameters = false;
		
	}

	private Map<String, List<StylePropertyValuesDifferenceInValues>> getDifferences() {
		
		Map<String, List<StylePropertyValuesDifferenceInValues>> propertyNameToDifferencesMap = new LinkedHashMap<>();
		
		for (String declarationProperty : realDeclarations.keySet()) {
			
			// "declarations" includes all the declarations having the same property with different values
			List<Declaration> declarations = realDeclarations.get(declarationProperty);
			
			Set<PropertyAndLayer> allSetPropertyAndLayers = getAllSetPropertyAndLayersForDeclarations(declarations);
			
			for (PropertyAndLayer propertyAndLayer : allSetPropertyAndLayers) {
				StylePropertyValuesDifferenceInValues difference = new StylePropertyValuesDifferenceInValues(propertyAndLayer);
				// If the values are the same across are declarations, no need to add them
				boolean shouldAddDifference = false;
				Collection<DeclarationValue> groundTruth = null;
				for (Declaration declaration : declarations) {
					Collection<DeclarationValue> declarationValuesForThisDeclaration = 
							declaration.getDeclarationValuesForStyleProperty(propertyAndLayer);
					if (declarationValuesForThisDeclaration == null) {
						//shouldAddDifference = true; 
					} else {
						if (groundTruth == null) {
							groundTruth = declarationValuesForThisDeclaration;
						} else {
							if (groundTruth.size() != declarationValuesForThisDeclaration.size()) {
								shouldAddDifference = true;
							} else {
								if (groundTruth instanceof List) { // Position is important
									List<DeclarationValue> toBeCheckedWithList = (List<DeclarationValue>)groundTruth;
									List<DeclarationValue> checkingList = (List<DeclarationValue>)declarationValuesForThisDeclaration;
									for (int i = 0; i < toBeCheckedWithList.size(); i++) {
										if (!checkingList.get(i).equivalent(toBeCheckedWithList.get(i))) {
											shouldAddDifference = true;
											break;
										}
									}
								} else if (groundTruth instanceof Set) {
									Set<DeclarationValue> toBeCheckedWithSet = (Set<DeclarationValue>)groundTruth;
									Set<DeclarationValue> checkingSet = (Set<DeclarationValue>)declarationValuesForThisDeclaration;
									Set<DeclarationValue> checkedValues = new HashSet<>();
									for (DeclarationValue value : toBeCheckedWithSet) {
										boolean valueFound = false;
										for (DeclarationValue checkingValue : checkingSet) {
											if (!checkedValues.contains(checkedValues) && value.equivalent(checkingValue)) {
												checkedValues.add(checkingValue);
												valueFound = true;
												break;
											}
										}
										if (!valueFound) {
											shouldAddDifference = true;
											break;
										}
											
									}
								}
							}
						}
					}
					difference.addDifference(declaration, declarationValuesForThisDeclaration);
				}
				if (shouldAddDifference) {
					List<StylePropertyValuesDifferenceInValues> differences = propertyNameToDifferencesMap.get(declarationProperty);
					if (differences == null) {
						differences = new ArrayList<>();
						propertyNameToDifferencesMap.put(declarationProperty, differences);
					}
					differences.add(difference);
				}
			}			
		}
		return propertyNameToDifferencesMap;
	}

	private Set<PropertyAndLayer> getAllSetPropertyAndLayersForDeclarations(List<Declaration> declarations) {
		Set<PropertyAndLayer> visitedPropertyAndLayers = new HashSet<>(); 
				
		for (Declaration declaration : declarations) {
			 for (PropertyAndLayer propertyAndLayer : declaration.getAllSetPropertyAndLayers()) {
				 if (propertyAndLayer.getPropertyName() == null)
					 continue;
				 visitedPropertyAndLayers.add(propertyAndLayer);
			 }
		}
		return visitedPropertyAndLayers;
	}

	/**
	 * Get all the mixin declarations (real declarations plus parameterized ones)
	 * sorted based on the mixin declaration number (if set)
	 * @return
	 */
	public Iterable<MixinDeclaration> getAllMixinDeclarations() {
		calculateParameters();
		return mixinDeclarations.values();
	}
	
	/**
	 * Returns all the selectors involved in this migration opportunity
	 * @return
	 */
	public Iterable<Selector> getInvolvedSelectors() {
		return involvedSelectors;
	}
	
	/**
	 * Returns all the parameters for the mixin being created using this opportunity
	 * @return
	 */
	public Iterable<MixinParameter> getParameters() {
		calculateParameters();
		return parameters;
	}
	
	/**
	 * Returns a map, which maps MixinParameters to MixinParameterizedValues for the given selector.
	 * This information can be used in making mixin calls
	 * @param forSelector
	 * @return
	 */
	public Map<MixinParameter, MixinParameterizedValue> getParameterizedValues(Selector forSelector) {
		calculateParameters();
		Map<MixinParameter, MixinParameterizedValue> toReturn = new LinkedHashMap<>();
		for (Declaration d : parameterizedValues.keySet()) {
			if (d.getSelector().equals(forSelector)) {
				Set<MixinParameterizedValue> pramsVals = parameterizedValues.get(d);
				for (MixinParameterizedValue pv : pramsVals) {
					toReturn.put(pv.getMixinParameter(), pv);
				}
			}
		}
		return toReturn;
	}
	
	public Iterable<Declaration> getDeclarationsToBeRemoved() {
		calculateParameters();
		Set<Declaration> allDeclarations = new HashSet<>();
		for (String property : realDeclarations.keySet())
			allDeclarations.addAll(realDeclarations.get(property));
		return allDeclarations;
	}

	public abstract String getMixinReferenceString(Selector selector);

	public abstract boolean preservesPresentation();
	
}
