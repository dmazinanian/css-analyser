package ca.concordia.cssanalyser.analyser.duplication;

import java.util.Iterator;
import java.util.List;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;


public class TypeTwoDuplicationInstance extends TypeOneDuplicationInstance {

	public TypeTwoDuplicationInstance() {
		duplicationType = DuplicationInstanceType.TYPE_II;
	}
		
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (Iterator<Selector> iterator = forSelectors.iterator(); iterator.hasNext();) {
			Selector selector = iterator.next();
			s.append(selector);
			if (selector.getLocationInfo().getLineNumber() >= 0)
				s.append(String.format("(%s)", selector.getLocationInfo()));
			if (iterator.hasNext())
				s.append(", ");
		}
		StringBuilder string = new StringBuilder();
		string.append("For selectors ").append(s).append(": ");
		string.append(System.lineSeparator());
		for (List<Declaration> list : forDeclarations) {
			if (list == null || list.get(0) == null) 
				continue;
			string.append("\t[").append(list.get(0)).append("]  (or its equivalences) in the following places: ");
			string.append(System.lineSeparator());
			for (Declaration declaration : list)
				string.append("\t\t").append(declaration.getLocationInfo()).append(" :").append(declaration);
			string.append(System.lineSeparator()); 
		}
		return string.toString();
	}

}
