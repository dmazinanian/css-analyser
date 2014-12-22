package ca.concordia.cssanalyser.migration.topreprocessors.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.concordia.cssanalyser.analyser.duplication.items.Item;
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
public class MixinMigrationOpportunity extends PreprocessorMigrationOpportunity {
	
	private String mixinName;
	
	// Equivalent declarations only appear in the mixin's body
	private Map<String, Collection<Declaration>> equivalentDelcarations = new LinkedHashMap<>();
	
	// Declarations having differences in their values are stored here. 
	private Map<String, List<Declaration>> declarationsWithDifferences = new LinkedHashMap<>();

	// List of parameters for this mixin
	private List<MixinParameter> parameters = new ArrayList<>(); 

	// The list of parameterized values in the call sites. 
	// Set of real declarations will be replaced by a call using mixinName and corresponding parameters
	private Map<Declaration, Set<MixinParameterizedValue>> parameterizedValues = new LinkedHashMap<>();
	
	private final Iterable<Selector> involvedSelectors; 
	
	private boolean shouldCalculateParameters = false;
	
	public MixinMigrationOpportunity(Iterable<Selector> forSelectors) {
		this.involvedSelectors = forSelectors;
	}

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
	
	public void addEquivalentDeclarations(String forProperty, Collection<Declaration> declarations) {
		equivalentDelcarations.put(forProperty, declarations);
		
		MixinDeclaration declaration = new MixinDeclaration(forProperty, getFirstNonVirtualDeclaration(declarations));
		Declaration declarationWithMinChars = getDeclarationWithMinimumChars(declarations);
		for (PropertyAndLayer propertyAndLayer : declarationWithMinChars.getAllSetPropertyAndLayers()) {
			Collection<DeclarationValue> declarationValuesForStyleProperty = 
					declarationWithMinChars.getDeclarationValuesForStyleProperty(propertyAndLayer);
			declaration.addMixinValue(propertyAndLayer, new MixinLiteral(declarationValuesForStyleProperty, propertyAndLayer));
		}
		mixinDeclarations.put(forProperty, declaration);
	}
	
	private Declaration getDeclarationWithMinimumChars(Collection<Declaration> declarations) {
		if (declarations instanceof Item)
			return ((Item) declarations).getDeclarationWithMinimumChars();
		else {
			return Collections.min(declarations, new Comparator<Declaration>() {
				@Override
				public int compare(Declaration o1, Declaration o2) {
					if (o1.toString().length() == o2.toString().length())
						return 1;
					return Integer.compare(o1.toString().length(), o2.toString().length());
				}
				
			});
		}
	}


	private Declaration getFirstNonVirtualDeclaration(Collection<Declaration> declarations) {
		if (declarations instanceof Item)
			return ((Item) declarations).getFirstDeclaration();
		else {
			for (Declaration declaration : declarations) {
				if (declaration instanceof ShorthandDeclaration && ((ShorthandDeclaration)declaration).isVirtual())
					continue;
				return declaration;
			}
		}
		return declarations.iterator().next();
	}


	public void addEquivalentDeclarations(Item item) {
		String propertyName = item.getFirstDeclaration().getProperty();
		addEquivalentDeclarations(propertyName, item);
	}

	public void addDeclarationsWithDifferences(String forProperty, Iterable<Declaration> declarations) {
		List<Declaration> declarationsToBeAdded = new ArrayList<>();
		for (Declaration d : declarations) {
			declarationsToBeAdded.add(d);
		}
		declarationsWithDifferences.put(forProperty, declarationsToBeAdded);	
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
		for (String propertyName : declarationsWithDifferences.keySet()) {
			List<StylePropertyValuesDifferenceInValues> differencesForThisProperty = propertyNameToDifferencesMap.get(propertyName);
			List<Declaration> declarationsWithDifferencesHavingTheSameProperty = declarationsWithDifferences.get(propertyName);
			Set<PropertyAndLayer> allSetPropertyAndLayers = getAllSetPropertyAndLayersForDeclarations(declarationsWithDifferencesHavingTheSameProperty);
			
			Declaration declarationWithMaxLayers = Collections.max(declarationsWithDifferencesHavingTheSameProperty, new Comparator<Declaration>() {
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
							for (Declaration declaration : declarationsWithDifferencesHavingTheSameProperty) {
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
				}
				if (!differenceFoundForThisPropertyValueAndLayer) {
					// Need to add a literal
					// But if all values in all declarations are missing, ignore
					
					boolean allMissing = true;
					for (Declaration declaration : declarationsWithDifferencesHavingTheSameProperty) {
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
								declarationsWithDifferencesHavingTheSameProperty.get(0).
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
				 if (propertyAndLayer.getPropertyName() == null)
					 continue;
				 visitedPropertyAndLayers.add(propertyAndLayer);
			 }
		}
		return visitedPropertyAndLayers;
	}

	/**
	 * Get all the mixin declarations (real declarations plus parameterized ones)
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

	/**
	 * Returns a set, containing all the declarations of a selector being parameterized using this opportunity
	 * or declarations that are equal among all involved selectors
	 * @param selector
	 * @return
	 */
	public Set<Declaration> getDeclarationsInvolved(Selector selector) {
		calculateParameters();
		Set<Declaration> toReturn = new HashSet<>();
		for (Declaration declaration : parameterizedValues.keySet()) {
			if (declaration.getSelector().equals(selector)) {
				toReturn.add(declaration);
			}
		}
		for (Collection<Declaration> declarations : equivalentDelcarations.values()) {
			for (Declaration declaration : declarations) {
				if (declaration.getSelector().equals(selector)) {
					/*
					 * Handle virtual shorthand declarations.
					 * Instead of returning the virtual shorthand declaration, return the corresponding declarations 
					 */
					if (declaration instanceof ShorthandDeclaration && ((ShorthandDeclaration) declaration).isVirtual()) {
						for (Declaration individual : ((ShorthandDeclaration) declaration).getIndividualDeclarations()) {
							for (Declaration declarationInTheSelector : selector.getDeclarations()) {
								if (individual.getProperty().equals(declarationInTheSelector.getProperty())) {
									toReturn.add(declarationInTheSelector);
									break;
								}
							}
						}
					} else {
						toReturn.add(declaration);
					}
				}
			}
		}
		return toReturn;
	}
	
}
