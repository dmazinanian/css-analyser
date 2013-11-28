package ca.concordia.cssanalyser.analyser.duplication;

import java.util.HashSet;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.selectors.Selector;


public abstract class TypeIVDuplication implements Duplication {

	protected Set<Selector> identicalSelectors;
	
	public TypeIVDuplication() {
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
			sb.append(selector.getLineNumber() + ", " + selector.getColumnNumber());
			sb.append(")\n");
		}
		return sb.toString();
	}
	
	@Override
	public Set<Selector> getSelectors() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static class TypeIVADuplication extends TypeIVDuplication {

		@Override
		public DuplicationType getType() {
			return DuplicationType.TYPE_IV_A;
		}
		
		@Override
		public String toString() {
			String toReturn = "These selectors are identical:\n";
			return toReturn + super.toString();
		}
		
		public Set<Selector> getIdenticalSelectors() {
			return identicalSelectors;
		}
		
	}
	
	public static class TypeIVBDuplication extends TypeIVDuplication {

		@Override
		public DuplicationType getType() {
			return DuplicationType.TYPE_IV_B;
		}
		
		@Override
		public String toString() {
			String toReturn = "These selectors select the same elements in DOM:\n";
			return toReturn + super.toString();
		}
		
		public Set<Selector> getEquivalentSelectors() {
			return identicalSelectors;
		}
		
	}

}
