package ca.concordia.cssanalyser.analyser.duplication;

import java.util.HashSet;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.selectors.Selector;


public abstract class TypeFourDuplicationInstance implements DuplicationInstance {

	protected Set<Selector> identicalSelectors;
	
	public TypeFourDuplicationInstance() {
		identicalSelectors = new HashSet<>();
	}
	
	public void addSelector(Selector selector) {
		identicalSelectors.add(selector);
	}
	

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Selector selector : identicalSelectors) {
			sb.append(selector);
			sb.append(": (");
			sb.append(selector.getLocationInfo());
			sb.append(")\n");
		}
		return sb.toString();
	}
	
	@Override
	public Set<Selector> getSelectors() {
		return identicalSelectors;
	}
	
	public static class TypeIVADuplication extends TypeFourDuplicationInstance {

		@Override
		public DuplicationInstanceType getType() {
			return DuplicationInstanceType.TYPE_IV_A;
		}
		
		@Override
		public String toString() {
			String toReturn = "These selectors are identical:\n";
			return toReturn + super.toString();
		}
		
		
	}
	
	public static class TypeIVBDuplication extends TypeFourDuplicationInstance {

		@Override
		public DuplicationInstanceType getType() {
			return DuplicationInstanceType.TYPE_IV_B;
		}
		
		@Override
		public String toString() {
			String toReturn = "These selectors select the same elements in DOM:\n";
			return toReturn + super.toString();
		}
		
		
	}

}
