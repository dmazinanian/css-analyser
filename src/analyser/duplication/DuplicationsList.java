package analyser.duplication;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DuplicationsList implements Iterable<Duplication> {

	private final List<Duplication> duplications;
	
	public DuplicationsList() {
		duplications = new ArrayList<>();
	}
	
	public void addDuplication (Duplication duplication) {
		duplications.add(duplication);
	}
	
	public int getSize() {
		return duplications.size();
	}

	@Override
	public Iterator<Duplication> iterator() {
		return duplications.iterator();
	}
	
		
}
