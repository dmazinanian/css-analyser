package ca.concordia.cssanalyser.migration.topreprocessors.mixin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.PropertyAndLayer;

public class MixinDeclaration {
	
	private String propertyName;
	private Map<PropertyAndLayer, MixinValue> mixinValues = new LinkedHashMap<>();
	private Declaration referenceDeclaration;

	public MixinDeclaration(String propertyName, Declaration referenceDeclaration) {
		this.propertyName = propertyName;
		this.referenceDeclaration = referenceDeclaration;
	}
	
	public void addMixinValue(PropertyAndLayer propertyAndLayer, MixinValue value) {
		mixinValues.put(propertyAndLayer, value);
	}
	
	public Declaration getReferenceDeclaration() {
		return this.referenceDeclaration;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	public MixinValue getMixinValueForPropertyandLayer(PropertyAndLayer propertyAndLayer) {
		return mixinValues.get(propertyAndLayer);
	}
	
	public Set<PropertyAndLayer> getAllSetPropertyAndLayers() {
		return mixinValues.keySet();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(propertyName).append(System.lineSeparator());
		Map<Integer, List<PropertyAndLayer>> allLayers = new HashMap<>();
		for (PropertyAndLayer propertyAndLayer : mixinValues.keySet()) {
			List<PropertyAndLayer> propertyAndLayersForThislayer = allLayers.get(propertyAndLayer.getPropertyLayer());
			if (propertyAndLayersForThislayer == null) {
				propertyAndLayersForThislayer = new ArrayList<>();
				allLayers.put(propertyAndLayer.getPropertyLayer(), propertyAndLayersForThislayer);
			}
			propertyAndLayersForThislayer.add(propertyAndLayer);
		}
		
		for (int i = 1; i <= allLayers.size(); i++) {
			builder.append("(Layer ").append(i).append(") ");
			for (Iterator<PropertyAndLayer> propAndLayersIterator = allLayers.get(i).iterator(); propAndLayersIterator.hasNext(); ) {
				PropertyAndLayer propAndLayer = propAndLayersIterator.next();
				builder.append(propAndLayer.getPropertyName()).append(": ").append(mixinValues.get(propAndLayer));
				if (propAndLayersIterator.hasNext())
					builder.append(", ");
			}
			if (i < allLayers.size())
				builder.append(System.lineSeparator());
		}
		
		return builder.toString();
	}
	
}
