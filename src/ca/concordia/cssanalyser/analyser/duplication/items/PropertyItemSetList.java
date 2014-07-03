package ca.concordia.cssanalyser.analyser.duplication.items;

public class PropertyItemSetList extends ItemSetList {
	// Use template method design pattern
	@Override
	protected String getRepresentativeItemString(Item item) {
		return item.getFirstDeclaration().getProperty();
	}
}
