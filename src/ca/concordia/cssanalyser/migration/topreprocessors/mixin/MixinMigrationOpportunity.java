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

import ca.concordia.cssanalyser.analyser.duplication.items.Item;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.PropertyAndLayer;
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
public class MixinMigrationOpportunity extends PreprocessorMigrationOpportunity {
	
	private String mixinName;
	
	// Equivalent declarations only appear in the mixin's body
	private Map<String, Item> equivalentDelcarations = new LinkedHashMap<>();
	
	// Declarations having differences in their values are stored here. 
	private Map<String, List<Declaration>> declarationsWithDifferences = new LinkedHashMap<>();

	// Involved selectors in this mixin
	private Map<Selector, Collection<Declaration>> selectors = new HashMap<>();

	// List of parameters for this mixin
	private List<MixinParameter> parameters = new ArrayList<>(); 

	// The list of parameterized values in the call sites. 
	// Set of real declarations will be replaced by a call using mixinName and corresponding parameters
	private Map<Declaration, Set<MixinParameterizedValue>> parameterizedValues = new LinkedHashMap<>();
	
	private boolean shouldCalculateParameters = false;

	// The list of properties and MixinValues. The real mixin will be created based on this
	private Map<String, MixinDeclaration> mixinDeclarations = new LinkedHashMap<>();
	
	public Iterable<String> getProperties() {
		List<String> toReturn = new ArrayList<>();
		for (String property : declarationsWithDifferences.keySet())
			toReturn.add(property);
		for (String property : equivalentDelcarations.keySet())
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

	public Iterable<Declaration> getDeclarationsForProperty(String property) {
		if (declarationsWithDifferences.containsKey(property))
			return declarationsWithDifferences.get(property);
		else if (equivalentDelcarations.containsKey(property))
			return equivalentDelcarations.get(property);
		else
			return new ArrayList<>();
	}
	
	public void addEquivalentDeclarations(Item item) {
		String propertyName = item.getFirstDeclaration().getProperty();
		equivalentDelcarations.put(propertyName, item);
		for (Selector s : item.getSupport()) {
			Collection<Declaration> declarationsForThisSelector = selectors.get(s);
			if (declarationsForThisSelector == null) {
				declarationsForThisSelector = new ArrayList<>();
				selectors.put(s, declarationsForThisSelector);
			}
			declarationsForThisSelector.addAll(item);
		}
				
		MixinDeclaration declaration = new MixinDeclaration(propertyName, item.getFirstDeclaration());
		Declaration declarationWithMinChars = item.getDeclarationWithMinimumChars();
		for (PropertyAndLayer propertyAndLayer : declarationWithMinChars.getAllSetPropertyAndLayers()) {
			Collection<DeclarationValue> declarationValuesForStyleProperty = 
					declarationWithMinChars.getDeclarationValuesForStyleProperty(propertyAndLayer);
			declaration.addMixinValue(propertyAndLayer, new MixinLiteral(declarationValuesForStyleProperty, propertyAndLayer));
		}
		mixinDeclarations.put(propertyName, declaration);
	}

	public void addDeclarationsWithDifferences(Iterable<Declaration> declarations) {
		String forProperty = declarations.iterator().next().getProperty();
		List<Declaration> declarationsToBeAdded = new ArrayList<>();
		for (Declaration d : declarations) {
			declarationsToBeAdded.add(d);
			Collection<Declaration> declarationForThisDeclarationSelector = selectors.get(d.getSelector());
			if (declarationForThisDeclarationSelector == null)
				declarationForThisDeclarationSelector = new ArrayList<>();
			declarationForThisDeclarationSelector.add(d);
			selectors.put(d.getSelector(), declarationForThisDeclarationSelector);
		}
		declarationsWithDifferences.put(forProperty, declarationsToBeAdded);	
		shouldCalculateParameters = true;
	}
	
	private void calculateParameters() {
		
		if (!shouldCalculateParameters)
			return;
		
		List<MixinParameter> initialParameters = new ArrayList<>();
		parameterizedValues.clear();
		mixinDeclarations.clear();
		
		/*
		 * If a value (or a set of values) for a style property is different in at least on of the declarations
		 * which are different, it needs to be parameterized.
		 * So we add it as a difference, and then we will make a parameter for every difference
		 */
		
		// Map every property name to a list of differences in different declarations
		Map<String, List<StylePropertyValuesDifferenceInValues>> propertyNameToDifferencesMap = getDifferences();
		
		// Make a parameter for every difference, a literal for every value which is the same, and add the corresponding parameterized values for call sites
		for (String propertyName : declarationsWithDifferences.keySet()) {
			List<StylePropertyValuesDifferenceInValues> differencesForThisProperty = propertyNameToDifferencesMap.get(propertyName);
			Set<PropertyAndLayer> allSetPropertyAndLayers = getAllSetPropertyAndLayersForDeclarations(declarationsWithDifferences.get(propertyName));
			
			Declaration declarationWithMaxLayers = Collections.max(declarationsWithDifferences.get(propertyName), new Comparator<Declaration>() {
				@Override
				public int compare(Declaration o1, Declaration o2) {
					if (o1.getNumberOfValueLayers() == o2.getNumberOfValueLayers())
						return 1;
					return Integer.compare(o1.getNumberOfValueLayers(), o2.getNumberOfValueLayers());
				}
			});
			
			MixinDeclaration mixinDeclaration = new MixinDeclaration(propertyName, declarationWithMaxLayers);
			mixinDeclarations.put(propertyName, mixinDeclaration);
			
			// For each possible property and layer we will have a parameter or a literal
			for (PropertyAndLayer propertyAndLayer : allSetPropertyAndLayers) {
				
				MixinValue value = null;
				
				// If the property and layer is in the difference list, add a parameter. otherwise, add a literal value
				boolean differenceFoundForThisPropertyValueAndLayer = false;
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
						for (Declaration declaration : declarationsWithDifferences.get(propertyName)) {
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
				if (!differenceFoundForThisPropertyValueAndLayer) {
					// Need to add a literal
					// But if all values in all declarations are missing, ignore
					
					boolean allMissing = true;
					for (Declaration declaration : declarationsWithDifferences.get(propertyName)) {
						for (DeclarationValue v : declaration.getDeclarationValuesForStyleProperty(propertyAndLayer)) {
							if (!v.isAMissingValue()) {
								allMissing = false;
								break;
							}
						}
						if (!allMissing)
							break;
					}
					
					if (!allMissing) {
						Collection<DeclarationValue> declarationValuesForStyleProperty =
								declarationsWithDifferences.get(propertyName).get(0).
								getDeclarationValuesForStyleProperty(propertyAndLayer.getPropertyName(), propertyAndLayer.getPropertyLayer());
						value = new MixinLiteral(declarationValuesForStyleProperty, propertyAndLayer);
					} else {
						value = null;
					}
				}
				
				if (value != null)
					mixinDeclaration.addMixinValue(propertyAndLayer, value);
			}
		}
		
		// We have to minimize the parameters
		this.parameters = initialParameters;
		
		shouldCalculateParameters = false;
	}


	private Map<String, List<StylePropertyValuesDifferenceInValues>> getDifferences() {
		Map<String, List<StylePropertyValuesDifferenceInValues>> propertyNameToDifferencesMap = new LinkedHashMap<>();
		
		for (String declarationProperty : declarationsWithDifferences.keySet()) {
			
			// "declarations" includes all the declarations having the same property with different values
			List<Declaration> declarations = declarationsWithDifferences.get(declarationProperty);
			
			Set<PropertyAndLayer> visitedPropertyAndLayers = getAllSetPropertyAndLayersForDeclarations(declarations);
			
			for (PropertyAndLayer propertyAndLayer : visitedPropertyAndLayers) {
				StylePropertyValuesDifferenceInValues difference = new StylePropertyValuesDifferenceInValues(propertyAndLayer);
				// If the values are the same across are declarations, no need to add them
				boolean shouldAddDifference = false;
				Collection<DeclarationValue> groundTruth = null;
				for (Declaration declaration : declarations) {
					Collection<DeclarationValue> declarationValuesForThisDeclaration = 
							declaration.getDeclarationValuesForStyleProperty(propertyAndLayer);
					if (declarationValuesForThisDeclaration == null) {
						shouldAddDifference = true; 
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
				 if (propertyAndLayer.getPropertyName() == null || visitedPropertyAndLayers.contains(propertyAndLayer))
					 continue;
				 visitedPropertyAndLayers.add(propertyAndLayer);
			 }
		}
		return visitedPropertyAndLayers;
	}

	public Iterable<MixinDeclaration> getAllMixinDeclarations() {
		calculateParameters();
		return mixinDeclarations.values();
	}
	
	public Iterable<Selector> getInvolvedSelectors() {
		return selectors.keySet();
	}
	
	public Iterable<MixinParameter> getParameters() {
		calculateParameters();
		return parameters;
	}
	
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
	
}
