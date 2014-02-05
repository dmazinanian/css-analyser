package ca.concordia.cssanalyser.refactoring.dependencies;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.dom.DOMHelper;

public class DependencyDetector {
	
	private final Document document;
	private final StyleSheet styleSheet;

	public DependencyDetector(StyleSheet styleSheet, Document dom) {
		this.document = dom;
		this.styleSheet = styleSheet;
	}
	
	public DependencyDetector(StyleSheet styleSheet) {
		this.document = null;
		this.styleSheet = styleSheet;
	}
	
	public Document getDocument() {
		return document;
	}

	public StyleSheet getStyleSheet() {
		return styleSheet;
	}

	public ValueOverridingDependencyList findOverridingDependancies() {
	
		ValueOverridingDependencyList dependencies = new ValueOverridingDependencyList();

		// Find property overriding dependencies inside selectors
			
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
								ValueOverridingDependency newDependency = new InsideSelectorValueOverridingDependency(selector, d, declaration, d.getProperty());
								dependencies.add(newDependency);
							}
						}
					}
					correspondingDeclarations.add(declaration);
					propertyToDeclarationMapping.put(possibleIndividualProperty, correspondingDeclarations);
				}
			}
		}
		

		// Finding overriding dependencies across selectors 
		if (this.document != null) {

			// For each node, we find all the classes that select that node.
			Map<Node, List<BaseSelector>> nodeToSelectorsMapping = DOMHelper.getCSSClassesForDOMNodes(document, styleSheet);


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
			
			for (Node node : nodeToSelectorsMapping.keySet()) {
				List<BaseSelector> selectors = nodeToSelectorsMapping.get(node);
				if (selectors.size() > 1) {
					
					Map<String, Set<Entry>> propertyToEntryMapping = new HashMap<>();
					
					for (BaseSelector selector : selectors) {
						for (Declaration declaration : selector.getDeclarations()) {
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
										if (/*!oldDeclarationEntry.baseSelector.selectorEquals(selector) &&*/  
												!oldDeclarationEntry.declaration.declarationIsEquivalent(declaration)) {
											ValueOverridingDependency newDependency = dependencies.getDependency(oldDeclarationEntry.declaration, declaration);
											if (newDependency != null) {
												newDependency.addDependencyLabel(possiblyStyledProperty);
											} else {
												newDependency = new CrossSelectorValueOverridingDependency (
													oldDeclarationEntry.baseSelector, oldDeclarationEntry.declaration,
													selector, declaration,
													possiblyStyledProperty
												);
												dependencies.add(newDependency);
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
					
				}
			}

		}
		
		return dependencies;
	}
}
