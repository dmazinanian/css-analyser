package ca.concordia.cssanalyser.migration.topreprocessors.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.PropertyAndLayer;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.migration.topreprocessors.DependenciesNotSatisfiableException;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorMigrationOpportunity;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorType;
import ca.concordia.cssanalyser.migration.topreprocessors.TransformationStatus;
import ca.concordia.cssanalyser.migration.topreprocessors.differences.StylePropertyValuesDifferenceInValues;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessMixinMigrationOpportunity;
import ca.concordia.cssanalyser.parser.CSSParserFactory;
import ca.concordia.cssanalyser.parser.CSSParserFactory.CSSParserType;
import ca.concordia.cssanalyser.parser.ParseException;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;

/**
 * Represents a Mixin refactoring opportunity in the preprocessor.
 * Every Mixin refactoring opportunity includes a collection of set of declarations which have the same property.
 * Some of them can have the same values, others will be parameterized based on the differences in the values.
 * @author Davood Mazinanian
 *
 */
public abstract class MixinMigrationOpportunity<T> extends PreprocessorMigrationOpportunity<T> {
	
	private String mixinName;
	
	// Declarations having differences in their values are stored here. 
	protected Map<String, List<Declaration>> realDeclarations = new LinkedHashMap<>();

	// List of parameters for this mixin
	private List<MixinParameter> parameters = new ArrayList<>(); 

	// The list of parameterized values in the call sites. 
	// Set of real declarations will be replaced by a call using mixinName and corresponding parameters
	private Map<Declaration, Set<MixinParameterizedValue>> parameterizedValues = new LinkedHashMap<>();
	
	private final Iterable<Selector> involvedSelectors; 
	
	private boolean shouldCalculateParameters = false;
	
	private final PreprocessorType preprocessorType;
	
	// Map every Declaration to a MixinDeclaration
	private Map<Declaration, MixinDeclaration> declarationToMixinDeclarationMapper = new HashMap<>();
	
	public MixinMigrationOpportunity(Iterable<Selector> forSelectors, StyleSheet forStyleSheet, PreprocessorType type) {
		super(forStyleSheet);
		this.involvedSelectors = forSelectors;
		preprocessorType = type;
	}

	// The list of properties and MixinValues. The real mixin will be created based on this
	private Map<String, MixinDeclaration> mixinDeclarations = new LinkedHashMap<>();
	
	public Iterable<String> getProperties() {
		return new ArrayList<>(realDeclarations.keySet());
	}
	
	public String getMixinName() {
		if (mixinName == null)
			return ".newMixin";
		if (!mixinName.startsWith("."))
			return "." + mixinName;
		return mixinName;
	}

	/**
	 * @param mixinName
	 * @throws IllegalArgumentException in case of an invalid name
	 */
	public void setMixinName(String mixinName) {
		String mixinNamePattern = "\\.[a-zA-Z_][a-zA-Z0-9_]*";
		if (!mixinName.matches(mixinNamePattern)) {
			throw new IllegalArgumentException("Mixin name is invalid");
		}
		// FIXME for repetitive names
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
		
		Set<String> propertiesInOrder = getPropertiesInOrder();
		
		// Make a parameter for every difference, a literal for every value which is the same, and add the corresponding parameterized values for call sites
		for (String propertyName : propertiesInOrder) {
			List<StylePropertyValuesDifferenceInValues> differencesForThisProperty = propertyNameToDifferencesMap.get(propertyName);
			List<Declaration> declarationsHavingTheSameProperty = realDeclarations.get(propertyName);
			Set<PropertyAndLayer> allSetPropertyAndLayers = getAllSetPropertyAndLayersForDeclarations(declarationsHavingTheSameProperty);
			
			Declaration declarationWithMaxLayers = getDeclarationWithMaxPropertyAndLayers(declarationsHavingTheSameProperty);
			
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
							// In case of repetitive names, prepend property name
							for (MixinParameter p : initialParameters) {
								if (p.getName().equals(parameterName)) {
									parameterName = propertyName.replace("-", "_") + "_" + parameterName;
									if (parameterName.startsWith("_"))
										parameterName = parameterName.substring(1); 
									break;
								}
							}
							MixinParameter parameter = new MixinParameter(parameterName, mixinDeclaration, propertyAndLayer);
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
								if (declarationValuesForThisPropertyAndLayer == null)
									FileLogger.getLogger(MixinMigrationOpportunity.class).warn(	
											String.format("NULL declaration value in %s for property and layer %s",
													declaration,
													propertyAndLayer));
								MixinParameterizedValue parameterizedValue = new MixinParameterizedValue(declaration, declarationValuesForThisPropertyAndLayer, parameter);
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
					
//					boolean theOnlyValuesOfThisDeclaration = 
//							getDeclarationWithMaxLayers(declarationsHavingTheSameProperty).getAllSetPropertyAndLayers().size() == 1;
					
//					boolean allMissing = true;
//					for (Declaration declaration : declarationsHavingTheSameProperty) {
//						Collection<DeclarationValue> declarationValuesForStyleProperty = declaration.getDeclarationValuesForStyleProperty(propertyAndLayer);
//						if (declarationValuesForStyleProperty != null) {
//							for (DeclarationValue v : declarationValuesForStyleProperty) {
//								if (!v.isAMissingValue()) {
//									allMissing = false;
//									break;
//								}
//							}
//							if (!allMissing)
//								break;
//						}
//					}
					
//					if (theOnlyValuesOfThisDeclaration || !allMissing) {
						Collection<DeclarationValue> declarationValuesForStyleProperty =
								getDeclarationWithMaxPropertyAndLayers(declarationsHavingTheSameProperty).
								getDeclarationValuesForStyleProperty(propertyAndLayer);
						value = new MixinLiteral(declarationValuesForStyleProperty);
//					} else {
//						value = null;
//					}
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
		mergeParameters();
		
		shouldCalculateParameters = false;
		
	}

	private Set<String> getPropertiesInOrder() /*throws DependenciesNotSatisfiableException*/ {
		
		List<Selector> selectors = new ArrayList<>();
		involvedSelectors.forEach(selector -> selectors.add(selector));
		
		Set<Declaration> declarations = new TreeSet<>(new Comparator<Declaration>() {
			@Override
			public int compare(Declaration o1, Declaration o2) {
				int declarationNumber1 = o1.getDeclarationNumber();
				int declarationNumber2 = o2.getDeclarationNumber();
				if (declarationNumber1 == declarationNumber2 && !o1.equals(o2)) {
					return -1;
				}
				return Integer.compare(declarationNumber1, declarationNumber2);
			}
		});
		
		Set<String> propertiesInOrder = new LinkedHashSet<>();
		Selector firstSelector = selectors.get(0);
		for (String property : realDeclarations.keySet()) {
			List<Declaration> list = realDeclarations.get(property);
			for (Declaration declaration : list) {
				if (declaration.getSelector().equals(firstSelector)) {
					declarations.add(declaration);
				}
			}
		}
		
		// Get the order from the first selector, if it is different in others, the change is not possible
		declarations.forEach(declaration -> propertiesInOrder.add(declaration.getProperty()));
		
		return propertiesInOrder;
	}

	/**
	 * @param declarationsHavingTheSameProperty
	 * @return
	 */
	private Declaration getDeclarationWithMaxPropertyAndLayers(List<Declaration> declarationsHavingTheSameProperty) {
		return Collections.max(declarationsHavingTheSameProperty, new Comparator<Declaration>() {
			@Override
			public int compare(Declaration o1, Declaration o2) {
				
				if (o1 instanceof ShorthandDeclaration && ((ShorthandDeclaration) o1).isVirtual())
					return -1;
				else if (o2 instanceof ShorthandDeclaration && ((ShorthandDeclaration)o2).isVirtual())
					return 1;
				
				if (o1.getNumberOfValueLayers() == o2.getNumberOfValueLayers())
					return Integer.compare(o1.getAllSetPropertyAndLayers().size(), o2.getAllSetPropertyAndLayers().size());
				return Integer.compare(o1.getNumberOfValueLayers(), o2.getNumberOfValueLayers());
			}
		});
	}

	private void mergeParameters() {
		
		Map<MixinParameter, List<MixinParameterizedValue>> mixinParameterToParameterizedValuesMap = new HashMap<>();
		for (Set<MixinParameterizedValue> parameterizedValuesForDeclaration : parameterizedValues.values()) {
			for (MixinParameterizedValue parameterizedValue : parameterizedValuesForDeclaration) {
				MixinParameter mixinParameter = parameterizedValue.getMixinParameter();
				List<MixinParameterizedValue> parameterizedValuesForParameter = new ArrayList<>();
				if (mixinParameterToParameterizedValuesMap.containsKey(mixinParameter)) {
					parameterizedValuesForParameter = mixinParameterToParameterizedValuesMap.get(mixinParameter);
				} else {
					mixinParameterToParameterizedValuesMap.put(mixinParameter, parameterizedValuesForParameter);
				}
				parameterizedValuesForParameter.add(parameterizedValue);
			}
		}
		
		Set<Integer> alreadyMerged = new HashSet<>();
		int lastParamIndex = 0;
		for (int i = 0; i < parameters.size(); i++) {
			if (alreadyMerged.contains(i))
				continue;
			List<Integer> candidatesForMerging = new ArrayList<>();
			candidatesForMerging.add(i);
			List<MixinParameterizedValue> list1 = mixinParameterToParameterizedValuesMap.get(parameters.get(i));
			for (int j = i + 1; j < parameters.size(); j++) {
				if (alreadyMerged.contains(j))
					continue;
				List<MixinParameterizedValue> list2 = mixinParameterToParameterizedValuesMap.get(parameters.get(j));
				// compare values between the two lists
				if (haveTheSameValues(list1, list2)) {
					candidatesForMerging.add(j);
				}
			}
			
			if (candidatesForMerging.size() > 1) {
				MixinParameter parameterToMergeWith = parameters.get(candidatesForMerging.get(0));
				String parameterName = parameterToMergeWith.getName();
				for (int j = 1; j < candidatesForMerging.size(); j++) {
					Integer candidateParameterForMergingIndex = candidatesForMerging.get(j);
					MixinParameter parameterToMerge = parameters.get(candidateParameterForMergingIndex);
					if (!"".equals(parameterName)) {
						parameterName = getLongestCommonPropertyName(parameterName, parameterToMerge.getName());
						if (parameterName.startsWith("_")) {
							parameterName = parameterName.substring(1, parameterName.length());
						}
						if (parameterName.endsWith("_")) {
							parameterName = parameterName.substring(0, parameterName.length() - 1);
						}
					}
					alreadyMerged.add(candidateParameterForMergingIndex);
					for (MixinParameterizedValue parameterizedValue : mixinParameterToParameterizedValuesMap.get(parameterToMerge)) {
						parameterizedValue.setMixinParameter(parameterToMergeWith);
					}
					
					for (MixinDeclaration declaration : mixinDeclarations.values()) {
						for (PropertyAndLayer propertyAndLayer : declaration.getAllSetPropertyAndLayers()) {
							if (parameterToMerge == declaration.getMixinValueForPropertyandLayer(propertyAndLayer)) {
								declaration.addMixinValue(propertyAndLayer, parameterToMergeWith);
								break;
							}
						}
					}
				}
				if (!"".equals(parameterName)) {
					parameterToMergeWith.setName(parameterName);
				} else {
					parameterToMergeWith.setName("arg" + lastParamIndex++);
				}
			}
			
		}
		
		List<MixinParameter> newMixinParameters = new ArrayList<>();
		for (int i = 0; i < parameters.size(); i++) {
			if (!alreadyMerged.contains(i)) {
				newMixinParameters.add(parameters.get(i));
			}
		}
		
		this.parameters = newMixinParameters;
	}

	private String getLongestCommonPropertyName(String property1, String property2) {
		BiMap<String, Character> partsToLettersMap = HashBiMap.create();
		String seq1 = getCharacterSequence(property1, partsToLettersMap);
		String seq2 = getCharacterSequence(property2, partsToLettersMap);
		String longestCommonSubsequence = getLongestCommonSubsequence(seq1, seq2);
		String longestCommonPropertyName = "";
		for (int i = 0; i < longestCommonSubsequence.length(); i++) {
			longestCommonPropertyName += partsToLettersMap.inverse().get(longestCommonSubsequence.charAt(i));
			if (i < longestCommonPropertyName.length() - 1) {
				longestCommonPropertyName += "_";
			}
		}
		return longestCommonPropertyName;
	}

	private String getCharacterSequence(String property, Map<String, Character> partsToLettersMap) {
		char currentChar = '@'; // Char before A
		for (Character chr : partsToLettersMap.values()) {
			if (chr > currentChar) {
				currentChar = chr;
			}
		}
		currentChar++;
		String parts1[] = property.split("_");
		String toReturn = "";
		for (int i = 0; i < parts1.length; i++) {
			String part = parts1[i];
			if (!partsToLettersMap.containsKey(part)) {
				partsToLettersMap.put(part, currentChar);
				currentChar++;
			}
			String letter = String.valueOf(partsToLettersMap.get(part));
			toReturn += letter;
		}
		return toReturn;
	}

	public String getLongestCommonSubsequence(String string1, String string2) {
        int l1 = string1.length();
        int l2 = string2.length();

        int[][] arr = new int[l1 + 1][l2 + 1];

        for (int i = l1 - 1; i >= 0; i--) {
            for (int j = l2 - 1; j >= 0; j--) {
                if (string1.charAt(i) == string2.charAt(j))
                    arr[i][j] = arr[i + 1][j + 1] + 1;
                else
                    arr[i][j] = Math.max(arr[i + 1][j], arr[i][j + 1]);
            }
        }

        int i = 0, j = 0;
        StringBuffer sb = new StringBuffer();
        while (i < l1 && j < l2) {
            if (string1.charAt(i) == string2.charAt(j)) {
                sb.append(string1.charAt(i));
                i++;
                j++;
            } else if (arr[i + 1][j] >= arr[i][j + 1])
                i++;
            else
                j++;
        }
        return sb.toString();
    }

	private boolean haveTheSameValues(List<MixinParameterizedValue> list1, List<MixinParameterizedValue> list2) {
	
		Map<Selector, Collection<DeclarationValue>> selectorToValuesMap1 = new HashMap<>();
		Map<Selector, Collection<DeclarationValue>> selectorToValuesMap2 = new HashMap<>();
		
		for (MixinParameterizedValue value : list1) {
			selectorToValuesMap1.put(value.getForDeclaration().getSelector(), value.getForValues());
		}
		
		for (MixinParameterizedValue value : list2) {
			selectorToValuesMap2.put(value.getForDeclaration().getSelector(), value.getForValues());
		}
		
		for (Selector selector : selectorToValuesMap1.keySet()) {
			Collection<DeclarationValue> collection1 = selectorToValuesMap1.get(selector);
			if (selectorToValuesMap2.containsKey(selector)) {
				Collection<DeclarationValue> collection2 = selectorToValuesMap2.get(selector);
				if ((collection1 == null && collection2 == null) || (collection1 != null && collection1.equals(collection2))) {
					
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
//			Collection<DeclarationValue> forValues1 = value1.getForValues();
//			for (MixinParameterizedValue value2 : list2) {
//				Collection<DeclarationValue> forValues2 = value2.getForValues();
//				if (!forValues1.equals(forValues2))
//					return false;
//			}

		return true;
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
					Collection<DeclarationValue> declarationValuesForThisStyleProperty = 
							declaration.getDeclarationValuesForStyleProperty(propertyAndLayer);
					if (declarationValuesForThisStyleProperty == null) {
						shouldAddDifference = true; 
					} else {
						if (groundTruth == null) {
							groundTruth = declarationValuesForThisStyleProperty;
						} else {
							if (groundTruth.size() != declarationValuesForThisStyleProperty.size()) {
								shouldAddDifference = true;
							} else {
								if (groundTruth instanceof List) { // Position is important
									List<DeclarationValue> toBeCheckedWithList = (List<DeclarationValue>)groundTruth;
									List<DeclarationValue> checkingList = (List<DeclarationValue>)declarationValuesForThisStyleProperty;
									for (int i = 0; i < toBeCheckedWithList.size(); i++) {
										if (!checkingList.get(i).equivalent(toBeCheckedWithList.get(i))) {
											shouldAddDifference = true;
											break;
										}
									}
								} else if (groundTruth instanceof Set) {
									Set<DeclarationValue> toBeCheckedWithSet = (Set<DeclarationValue>)groundTruth;
									Set<DeclarationValue> checkingSet = (Set<DeclarationValue>)declarationValuesForThisStyleProperty;
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
					difference.addDifference(declaration, declarationValuesForThisStyleProperty);
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
	 * sorted based on the position of declarations in the first selector
	 * @return
	 */
	public Iterable<MixinDeclaration> getAllMixinDeclarations() {
		calculateParameters();
		List<MixinDeclaration> toReturn = new ArrayList<>();
		for (String property : mixinDeclarations.keySet())
			toReturn.add(mixinDeclarations.get(property));
		return toReturn;
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
		for (Selector selector : involvedSelectors) {
			allDeclarations.addAll(getInvolvedDeclarations(selector));
		}
		return allDeclarations;
	}

	public abstract String getMixinReferenceString(Selector selector);

	public abstract TransformationStatus preservesPresentation();

	/**
	 * When we remove a shorthand because of a virtual individual, we should add
	 * the remaining individuals to the selector. This gives the list of such
	 * declarations
	 * @return
	 */
	public Iterable<Declaration> getDeclarationsToBeAdded() {
		Map<ShorthandDeclaration, Set<Declaration>> parentShortandsToIndividualsMap = new HashMap<>();
		for (String property : realDeclarations.keySet()) {
			List<Declaration> declarations = realDeclarations.get(property);
			for (Declaration declaration : declarations) {
				if (declaration.isVirtualIndividualDeclarationOfAShorthand()) {
					ShorthandDeclaration parentShorthand = declaration.getParentShorthand();
					while (parentShorthand != null && parentShorthand.isVirtualIndividualDeclarationOfAShorthand()) {
						parentShorthand = parentShorthand.getParentShorthand();
					} 
					if (parentShorthand != null) {
						Set<Declaration> individualsOfTheSameParent = parentShortandsToIndividualsMap.get(parentShorthand);
						if (individualsOfTheSameParent == null) {
							individualsOfTheSameParent = new HashSet<Declaration>();
							parentShortandsToIndividualsMap.put(parentShorthand, individualsOfTheSameParent);
						}
						individualsOfTheSameParent.add(declaration);
					}
				}
			}
		}
		
		List<Declaration> declarationsToBeAdded = new ArrayList<>();
		for (ShorthandDeclaration parentShorthand : parentShortandsToIndividualsMap.keySet()) {
			Set<Declaration> individualsToBeRemoved = parentShortandsToIndividualsMap.get(parentShorthand);
			for (Declaration individual : parentShorthand.getIndividualDeclarationsAtTheDeepestLevel()) {
				if (!individualsToBeRemoved.contains(individual) &&
						parentShorthand.getSelector() != null) {
					// Check if parent selector does not have this declaration, then add it
					boolean selectorAlreadyHasThisDeclaration = false;
					for (Declaration declaration : parentShorthand.getSelector().getDeclarations()) {
						if (declaration.declarationIsEquivalent(individual)) {
							selectorAlreadyHasThisDeclaration = true;
							break;
						}
					}
					if (!selectorAlreadyHasThisDeclaration) {
						declarationsToBeAdded.add(individual);
					}
				}
			}
		}
		return declarationsToBeAdded;
	}

	public PreprocessorType getPreprocessorType() {
		return preprocessorType;
	}
	
	public MixinDeclaration getMixinDeclarationForDeclaration(Declaration declaration) {
		return this.declarationToMixinDeclarationMapper.get(declaration);
	}
	
	public abstract String getMixinSignature();

	public int getNumberOfParameters() {
		calculateParameters();
		return parameters.size();
	}

	public int getNumberOfMixinDeclarations() {
		calculateParameters();
		return realDeclarations.keySet().size();
	}
	
	public int getNumberOfDeclarationsUsingParameters() {
		calculateParameters();
		int numberOfDeclarationsUsingParameters = 0;
		for (MixinDeclaration mixinDeclaration : mixinDeclarations.values()) {
			for (MixinValue mixinValue : mixinDeclaration.getMixinValues()) {
				if (mixinValue instanceof MixinParameter) {
					numberOfDeclarationsUsingParameters++;
				}
			}
		}
		return numberOfDeclarationsUsingParameters;
	}

	public int getNumberOfUniqueCrossBrowserDeclarations() {
		Set<String> uniqueCrossBrowserDeclarations = new HashSet<>();
		for (String property : realDeclarations.keySet()) {
			String nonVendorProperty = Declaration.getNonVendorProperty(property);
			if (!property.equals(nonVendorProperty)) {
				uniqueCrossBrowserDeclarations.add(nonVendorProperty);
			}
		}
		return uniqueCrossBrowserDeclarations.size();
	}

	public int getNumberOfNonCrossBrowserDeclarations() {
		Set<String> numberOfNonCrossBrowserDeclarations = new HashSet<>();
		for (String property : realDeclarations.keySet()) {
			String nonVendorProperty = Declaration.getNonVendorProperty(property);
			if (property.equals(nonVendorProperty)) {
				numberOfNonCrossBrowserDeclarations.add(property);
			}
		}
		return numberOfNonCrossBrowserDeclarations.size();
	}

	public int getNumberOfDeclarationsHavingOnlyHardCodedValues() {
		calculateParameters();
		int numberOfDeclarationsHavingOnlyHardCodedValues = mixinDeclarations.values().size();
		for (MixinDeclaration mixinDeclaration : mixinDeclarations.values()) {
			for (MixinValue mixinValue : mixinDeclaration.getMixinValues()) {
				if (mixinValue instanceof MixinParameter) {
					numberOfDeclarationsHavingOnlyHardCodedValues--;
					break;
				}
			}
		}
		return numberOfDeclarationsHavingOnlyHardCodedValues;
	}

	public int getNumberOfVendorSpecificSharingParameter() {
		calculateParameters();
		Set<String> vendorSpecificSharingParams = new HashSet<>();

		for (MixinParameter parameter : parameters) {
			for (MixinDeclaration mixinDeclaration : mixinDeclarations.values()) {
				for (MixinValue value : mixinDeclaration.getMixinValues()) {
					if (parameter == value) {
						String nonVendorProperty = Declaration.getNonVendorProperty(mixinDeclaration.getPropertyName());
						if (!nonVendorProperty.equals(mixinDeclaration.getPropertyName()))
							vendorSpecificSharingParams.add(nonVendorProperty);
						break;
					}
				}
			}
		}
		
		return vendorSpecificSharingParams.size();
	}

	public int getNumberOfUniqueParametersUsedInVendorSpecific() {
		calculateParameters();
		
		int numberOfUniqueParamsSharedInVendorSpecific = 0;
		
		for (MixinParameter parameter : parameters) {
			Map<String, Integer> vendorSpecificSharingParams = new HashMap<>();
			for (MixinDeclaration mixinDeclaration : mixinDeclarations.values()) {
				for (MixinValue value : mixinDeclaration.getMixinValues()) {
					if (parameter == value) {
						String nonVendorProperty = Declaration.getNonVendorProperty(mixinDeclaration.getPropertyName());
						int n = 0;
						if (vendorSpecificSharingParams.containsKey(nonVendorProperty)) {
							n = vendorSpecificSharingParams.get(nonVendorProperty);
						}
						vendorSpecificSharingParams.put(nonVendorProperty, n + 1);
						break;
					}
				}
			}
			for (String property : vendorSpecificSharingParams.keySet()) {
				if (vendorSpecificSharingParams.get(property) >= 2) {
					numberOfUniqueParamsSharedInVendorSpecific++;
					break;
				}
			}
		}
		return numberOfUniqueParamsSharedInVendorSpecific;
	}

	public int getNumberOfUniqueParametersUsedInMoreThanOneKindOfDeclaration() {
		calculateParameters();
		int uniqueParamsUsedInMoreThanOneKindOrDeclaration = 0;

		for (MixinParameter parameter : parameters) {
			Set<String> distinctProperties = new HashSet<>();
			for (MixinDeclaration mixinDeclaration : mixinDeclarations.values()) {
				for (MixinValue value : mixinDeclaration.getMixinValues()) {
					if (parameter == value) {
						String nonVendorProperty = Declaration.getNonVendorProperty(mixinDeclaration.getPropertyName());
						distinctProperties.add(nonVendorProperty);
						break;
					}
				}
			}
			if (distinctProperties.size() > 1)
				uniqueParamsUsedInMoreThanOneKindOrDeclaration++;
		}
		return uniqueParamsUsedInMoreThanOneKindOrDeclaration;
	}

	public Set<String> getPropertiesAtTheDeepestLevel() {
		calculateParameters();
		Set<String> propertiesInOpportunity = new HashSet<>();
		for (MixinDeclaration mixinDeclaration : getAllMixinDeclarations()) {
			String propertyName = mixinDeclaration.getPropertyName();
			if (ShorthandDeclaration.isShorthandProperty(propertyName)) {
				propertiesInOpportunity.addAll(ShorthandDeclaration.getIndividualPropertiesForAShorthand(propertyName));
			} else {
				propertiesInOpportunity.add(propertyName);
			}									
		}
		return propertiesInOpportunity;
	}
	
	public Set<Declaration> getInvolvedDeclarations(Selector selector) {
		Set<Declaration> involvedDeclarations = new HashSet<>();
		for (Declaration declaration : selector.getDeclarations()) {
			if (realDeclarations.containsKey(declaration.getProperty())) {
				List<Declaration> list = realDeclarations.get(declaration.getProperty());
				for (Declaration d : list) {
					if (d.equals(declaration)) {
						involvedDeclarations.add(declaration);
						break;		
					}
				}
//				involvedDeclarations.add(declaration);
			} else {
				// Check for possible shorthand
				Set<String> individuals = ShorthandDeclaration.getIndividualPropertiesForAShorthand(declaration.getProperty());
				if (individuals.size() > 0) {
					boolean declarationFound = false;
					for (String individual : individuals) {
						if (realDeclarations.containsKey(individual)) {
							List<Declaration> list = realDeclarations.get(individual);
							for (Declaration d : list) {
								if (d.getSelector().equals(selector) && d.isVirtualIndividualDeclarationOfAShorthand()) {
									ShorthandDeclaration parentShorthand = d.getParentShorthand();
									do {
										if (parentShorthand.equals(declaration)) {
											involvedDeclarations.add(declaration);
											declarationFound = true;
											break;
										}
										parentShorthand = parentShorthand.getParentShorthand();
									} while(parentShorthand != null);
									
									if (declarationFound) {
										break;		
									}
								}
							}
							if (declarationFound) {
								break;
							}
						}
					}
				}
			}
		}
		return involvedDeclarations;
	}

	/**
	 * Returns the declarations, in order, to satisfy dependencies.
	 * The mixin call is denoted by a fake declaration MIXIN: CALL;
	 * A null array means don't re-arrange!
	 * @param selector
	 * @return
	 * @throws Exception 
	 */
	public Declaration[] getMixinCallPosition(Selector selector) throws DependenciesNotSatisfiableException {
		
		CSSValueOverridingDependencyList intraSelectorOverridingDependencies = selector.getIntraSelectorOverridingDependencies();
		
		if (intraSelectorOverridingDependencies.size() == 0) {
			return null;
		}
		
		Solver solver = new Solver("Finding mixin call position problem");
		
		Declaration mixinFakeDeclaration = null;
		try {
			mixinFakeDeclaration =
				CSSParserFactory.getCSSParser(CSSParserType.LESS).parseCSSString(".selector {MIXIN:CALL}")
					.getAllSelectors().iterator().next()
					.getDeclarations().iterator().next();
		} catch (ParseException parseEx) {
			parseEx.printStackTrace();
		}
		
		Set<Declaration> involvedDeclarationsInMixin = getInvolvedDeclarations(selector);
		// Get them in order, we want to make the changes to ordering minimal
		Set<Declaration> otherDeclarations = new LinkedHashSet<Declaration>();
		
		for (Declaration declaration : selector.getDeclarations()) {
			if (!involvedDeclarationsInMixin.contains(declaration)) {
				otherDeclarations.add(declaration);
			}
		}
		
		/*
		 * The maximum number for variable domain is the number of declarations not being involved
		 * in the mixin plus one for mixin call
		 */
		int maximumNumber = otherDeclarations.size() + 1;
		
		Map<Declaration, IntVar> declarationsToVariablesMap = new HashMap<>();
		
		Declaration lastDeclaraion = null;
		declarationsToVariablesMap.put(mixinFakeDeclaration, VariableFactory.bounded(mixinFakeDeclaration.toString(), 1, maximumNumber, solver));
		for (Declaration declaration : otherDeclarations) {
			String variableName = declaration.toString();
			declarationsToVariablesMap.put(declaration, VariableFactory.bounded(variableName, 1, maximumNumber, solver));
			if (lastDeclaraion != null) {
				// Minimize changes
				solver.post(IntConstraintFactory.arithm(declarationsToVariablesMap.get(lastDeclaraion), "<", declarationsToVariablesMap.get(declaration)));
			}
			lastDeclaraion = declaration;
		}
		
		Set<Declaration> declarationsInvolvedInDependencies = new HashSet<>();
		// Add the constraints
		
		for (CSSValueOverridingDependency cssValueOverridingDependency : intraSelectorOverridingDependencies) {
			Declaration declaration1 = cssValueOverridingDependency.getDeclaration1();
			Declaration declaration2 = cssValueOverridingDependency.getDeclaration2();
			IntVar variable1 = null, variable2 = null;
			
			if (involvedDeclarationsInMixin.contains(declaration1) && otherDeclarations.contains(declaration2)) {
				variable1 = declarationsToVariablesMap.get(mixinFakeDeclaration);
				variable2 = declarationsToVariablesMap.get(declaration2);
				declarationsInvolvedInDependencies.add(declaration2);
			} else if (involvedDeclarationsInMixin.contains(declaration2) && otherDeclarations.contains(declaration1)) {
				variable1 = declarationsToVariablesMap.get(declaration1);
				variable2 = declarationsToVariablesMap.get(mixinFakeDeclaration);
				declarationsInvolvedInDependencies.add(declaration1);
			} else if (otherDeclarations.contains(declaration1) && otherDeclarations.contains(declaration2)) {
				variable1 = declarationsToVariablesMap.get(declaration1);
				variable2 = declarationsToVariablesMap.get(declaration2);
				declarationsInvolvedInDependencies.add(declaration1);
				declarationsInvolvedInDependencies.add(declaration2);
			}
			
			if (variable1 != null && variable2 != null) {
				solver.post(IntConstraintFactory.arithm(variable1, "<", variable2));
			}
		}
		
		IntVar[] allVars = new IntVar[declarationsToVariablesMap.size()];
		allVars = declarationsToVariablesMap.values().toArray(allVars);
		// "BC" = bound-consistency
		solver.post(IntConstraintFactory.alldifferent(allVars, "BC"));
		
		boolean result = solver.findSolution();
		
		if (result) {
			Declaration[] toReturn = new Declaration[declarationsToVariablesMap.size()];
			for (Entry<Declaration, IntVar> entry : declarationsToVariablesMap.entrySet()) {
				if (entry.getValue().getValue() == 0 || entry.getValue().getValue() - 1 >= toReturn.length) {
					throw new DependenciesNotSatisfiableException("Not solvable! Array index out of bound for declarations");
				} else {
					toReturn[entry.getValue().getValue() - 1] = entry.getKey();
				}
			}
			return toReturn;
		} else {
//			Set<String> set = new HashSet<>();
//			for (Declaration declaration : otherDeclarations) {
//				String p = declaration.getProperty();
//				if (set.contains(p)) {
//					throw new DependenciesNotSatisfiableException("Not solvable, duplicated properties!"); 
//				}
//				set.add(p);
//			}
			throw new DependenciesNotSatisfiableException("Not solvable!");
		}
	}
	
	public int getNumberOfIntraSelectorDependenciesInMixin() {
		return getNumberOfDependenciesAffectingMigration(false);
	}

	public int getNumberOfIntraSelectorDependenciesAffectingMixinCallPosition() {
		return getNumberOfDependenciesAffectingMigration(true);
	}
	
	private int getNumberOfDependenciesAffectingMigration(boolean affectingMixinCall) {
		int numberOfDependencies = 0;
		for (Selector selector : involvedSelectors) {
			CSSValueOverridingDependencyList intraSelectorOverridingDependencies = selector.getIntraSelectorOverridingDependencies();
			Set<Declaration> involvedDeclarations = getInvolvedDeclarations(selector);
			for (CSSValueOverridingDependency cssValueOverridingDependency : intraSelectorOverridingDependencies) {
				if (affectingMixinCall) {
					if (involvedDeclarations.contains(cssValueOverridingDependency.getDeclaration1()) && !involvedDeclarations.contains(cssValueOverridingDependency.getDeclaration2()) ||
							involvedDeclarations.contains(cssValueOverridingDependency.getDeclaration2()) && !involvedDeclarations.contains(cssValueOverridingDependency.getDeclaration1())) {
						numberOfDependencies++;
					}
				} else {
					if (involvedDeclarations.contains(cssValueOverridingDependency.getDeclaration1()) &&
							involvedDeclarations.contains(cssValueOverridingDependency.getDeclaration2())) {
						numberOfDependencies++;
					}
				}
			}
		}
		return numberOfDependencies ;
	}
	
	public MixinMigrationOpportunity<?> getSubOpportunity(Set<String> propertiesComingTogetherInAMixin, Set<Selector> forSelectors) {
		if (propertiesComingTogetherInAMixin.equals(this.getPropertiesAtTheDeepestLevel()) &&
				forSelectors.equals(getInvolvedSelectors())) {
			return this;
		}
		LessMixinMigrationOpportunity subOpportunity = new LessMixinMigrationOpportunity(forSelectors, getStyleSheet());
		for (String property : propertiesComingTogetherInAMixin) {
			
			List<Declaration> declarationsToAddToSubOpportunity = null;
			
			Set<String> shorthandPropertyNames = ShorthandDeclaration.getShorthandPropertyNames(property);
			if (shorthandPropertyNames.size() > 0) {
				List<String> toTest = new ArrayList<>();
				toTest.add(property);
				toTest.addAll(shorthandPropertyNames);
				for (String p : toTest) {
					List<Declaration> declarations = realDeclarations.get(p);
					if (declarations != null) {
						declarationsToAddToSubOpportunity = declarations;
						break;
					}
				}
			} else {
				declarationsToAddToSubOpportunity = realDeclarations.get(property);
			}
			
			if (declarationsToAddToSubOpportunity != null) {
				List<Declaration> declarationsInTheSelectors = new ArrayList<>();
				for (Declaration declaration : declarationsToAddToSubOpportunity) {
					if (forSelectors.contains(declaration.getSelector())) {
						declarationsInTheSelectors.add(declaration);
					}
				}
				if (declarationsInTheSelectors.size() > 0)
					subOpportunity.addDeclarationsWithTheSameProperty(declarationsInTheSelectors);
			} else {
				FileLogger.getLogger(LessMixinMigrationOpportunity.class).warn(String.format("Declaration %s not found", property));
			}
		}
		return subOpportunity;
	}
}
