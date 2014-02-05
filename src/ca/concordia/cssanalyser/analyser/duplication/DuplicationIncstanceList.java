package ca.concordia.cssanalyser.analyser.duplication;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DuplicationIncstanceList implements Iterable<DuplicationInstance> {

	private final List<DuplicationInstance> duplications;
	
	public DuplicationIncstanceList() {
		duplications = new ArrayList<>();
	}
	
	public void addDuplication (DuplicationInstance duplication) {
		duplications.add(duplication);
	}
	
	public int getSize() {
		return duplications.size();
	}

	@Override
	public Iterator<DuplicationInstance> iterator() {
		return duplications.iterator();
	}
	
		
}
