package ca.concordia.cssanalyser.refactoring.dependencies;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.dom.DOMNodeWrapper;

public class CSSDependencyDetector {
	
	private final Document document;
	private final StyleSheet styleSheet;

	public CSSDependencyDetector(StyleSheet styleSheet, Document dom) {
		this.document = dom;
		this.styleSheet = styleSheet;
	}
	
	public CSSDependencyDetector(StyleSheet styleSheet) {
		this.document = null;
		this.styleSheet = styleSheet;
	}
	
	public Document getDocument() {
		return document;
	}

	public StyleSheet getStyleSheet() {
		return styleSheet;
	}

	/**
	 * Returns all the value overriding dependencies in a CSS
	 * It doesn't take into account the specificity of selectors.
	 * @return
	 */
	public CSSValueOverridingDependencyList findOverridingDependancies() {
	
		CSSValueOverridingDependencyList dependencies = new CSSValueOverridingDependencyList();
		
		Map<Integer, CSSValueOverridingDependency> dependenciesSpecialHashMapper = new HashMap<>();		

		// Finding overriding dependencies across selectors 
		if (this.document == null) {
			// Find property overriding dependencies inside selectors
			findIntraSelectorDependencies(dependencies, dependenciesSpecialHashMapper);
		} else {
			findInterSelectorDependencies(dependencies, dependenciesSpecialHashMapper);

		}
		
		return dependencies;
	}

	private void findInterSelectorDependencies(
			CSSValueOverridingDependencyList dependencies,
			Map<Integer, CSSValueOverridingDependency> dependenciesSpecialHashMapper) {
		// For each node, we find all the classes that select that node.
		Map<DOMNodeWrapper, List<BaseSelector>> nodeToSelectorsMapping = styleSheet.getCSSClassesForDOMNodes(document);


		// We have to keep track of the base selector in which the declaration exists
		// For grouping selectors, the declaration's selector 
		class Entry {
			private Declaration declaration;
			private BaseSelector baseSelector;
			public Entry(BaseSelector baseSelector, Declaration declaration) {
				this.declaration = declaration;
				this.baseSelector = baseSelector;
			}
			@Override
			public String toString() {
				return this.baseSelector + "$" + this.declaration;
			}
		}
		
		for (DOMNodeWrapper node : nodeToSelectorsMapping.keySet()) {
			List<BaseSelector> selectorsForCurrentNode = nodeToSelectorsMapping.get(node);
			//if (selectorsForCurrentNode.size() > 1) {

				// Maps every property to a set of declarations in the stylesheet
				Map<String, Set<Entry>> propertyToEntryMapping = new HashMap<>();

				for (BaseSelector selector : selectorsForCurrentNode) {
					for (Declaration declaration : selector.getDeclarations()) {
						
//						if (declaration instanceof ShorthandDeclaration && ((ShorthandDeclaration)declaration).isVirtual())
//							continue;
						
						// We find all possible properties which could be styled using this declaration 
						Set<String> possiblyStyledPropertiesSet = new HashSet<>();
						// The first possible property is the real property of the declaration.
						possiblyStyledPropertiesSet.add(declaration.getProperty());
						// But if the declaration is a shorthand, it can style many other sub-properties
						if (declaration instanceof ShorthandDeclaration) {
							possiblyStyledPropertiesSet.addAll(ShorthandDeclaration.getIndividualPropertiesForAShorthand(declaration.getProperty()));
						}

						for (String possiblyStyledProperty : possiblyStyledPropertiesSet) {
							// See if another declaration is already styling this property (so this new declaration will override the old one)
							Set<Entry> declarationsStylingThisProperty;
							if (propertyToEntryMapping.containsKey(possiblyStyledProperty)) {
								// In this case, we create one dependency from each of those declarations to the current declaration
								declarationsStylingThisProperty = propertyToEntryMapping.get(possiblyStyledProperty);

								for (Entry oldDeclarationEntry : declarationsStylingThisProperty) {
									if (!oldDeclarationEntry.declaration.declarationIsEquivalent(declaration, true)) {
										/*!oldDeclarationEntry.baseSelector.selectorEquals(selector) &&*/  

										// Check to see whether such a dependency is there already
										
										int specialHashCode = CSSValueOverridingDependency.getSpecialHashCode(
												oldDeclarationEntry.baseSelector, oldDeclarationEntry.declaration,
												selector, declaration);
										CSSValueOverridingDependency newDependency = dependenciesSpecialHashMapper.get(specialHashCode);
										if (newDependency != null) {
											newDependency.addDependencyLabel(possiblyStyledProperty);
										} else {
											if (oldDeclarationEntry.baseSelector == selector) {
												// Intra-selector dependency
												newDependency = new CSSIntraSelectorValueOverridingDependency(
														selector,
														oldDeclarationEntry.declaration,
														declaration,
														possiblyStyledProperty);
											} else {
												// if specificities of selectors are the same, then there is a value overriding dependency
												if (oldDeclarationEntry.baseSelector.getSpecificity() == selector.getSpecificity()) {
													newDependency = new CSSInterSelectorValueOverridingDependency(
															oldDeclarationEntry.baseSelector,
															oldDeclarationEntry.declaration,
															selector, declaration,
															possiblyStyledProperty);
												} else {
													
												}
											}

											if (newDependency != null) {
												dependencies.add(newDependency);
												dependenciesSpecialHashMapper.put(newDependency.getSpecialHashCode(), newDependency);
											}
										}

									}
								}

							} else { // If there is no such a declaration for current possibly styled property
								declarationsStylingThisProperty = new HashSet<>();
							}

							// Add current declaration as a styling declaration for current property
							declarationsStylingThisProperty.add(new Entry(selector, declaration));
							propertyToEntryMapping.put(possiblyStyledProperty, declarationsStylingThisProperty);

						}
					}
				}

			//}
		}
	}

	private void findIntraSelectorDependencies(CSSValueOverridingDependencyList dependencies, Map<Integer, CSSValueOverridingDependency> dependenciesSpecialHashMapper) {
		for (BaseSelector selector : this.styleSheet.getAllBaseSelectors()) {
			Map<String, Set<Declaration>> propertyToDeclarationMapping = new HashMap<>();
			for (Declaration declaration : selector.getDeclarations()) {
				Set<String> possiblyStyledProperties;
				if (declaration instanceof ShorthandDeclaration) {
					possiblyStyledProperties = ShorthandDeclaration.getIndividualPropertiesForAShorthand(declaration.getProperty());
				} else {
					possiblyStyledProperties = new HashSet<>();
				}
				possiblyStyledProperties.add(declaration.getProperty());
				
				for (String possibleIndividualProperty : possiblyStyledProperties) {
					Set<Declaration> correspondingDeclarations = propertyToDeclarationMapping.get(possibleIndividualProperty);
					if (correspondingDeclarations == null) {
						correspondingDeclarations = new HashSet<>();
					} else {
						for (Declaration d : correspondingDeclarations) {
							if (!d.declarationEquals(declaration)) {
								CSSValueOverridingDependency newDependency = 
										dependenciesSpecialHashMapper.get(
												CSSValueOverridingDependency.getSpecialHashCode(selector, d, selector, declaration));
								if (newDependency != null) {
									newDependency.addDependencyLabel(possibleIndividualProperty);
								} else {
									newDependency = new CSSIntraSelectorValueOverridingDependency(selector, d, declaration, possibleIndividualProperty);
									dependencies.add(newDependency);
									dependenciesSpecialHashMapper.put(newDependency.getSpecialHashCode(), newDependency);
								}
								
							}
						}
					}
					correspondingDeclarations.add(declaration);
					propertyToDeclarationMapping.put(possibleIndividualProperty, correspondingDeclarations);
				}
			}
		}
	}
}
