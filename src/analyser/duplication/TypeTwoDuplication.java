package analyser.duplication;

import java.util.List;

import CSSModel.declaration.Declaration;
import CSSModel.selectors.Selector;

public class TypeTwoDuplication extends TypeOneDuplication {

	public TypeTwoDuplication() {
		duplicationType = DuplicationType.TYPE_II;
	}
		
	@Override
	public String toString() {
		String s = "";
		for (Selector selector : forSelectors) {
			s += selector;
			if (selector.getLineNumber() >= 0)
				s += String.format(" (%s : %s)", selector.getLineNumber(), selector.getColumnNumber());
			s += ", ";
		}
		s = s.substring(0, s.length() - 2); // Remove the last comma and space
		String string = "For selectors " + s + ": \n";
		for (List<Declaration> list : forDeclarations) {
			if (list == null || list.get(0) == null) 
				continue;
			string += "\t[" + list.get(0) + "]  (or its equivalences) in the following places: \n";
			for (Declaration declaration : list)
				string += "\t\t" + declaration.getLineNumber() + " : " + declaration.getColumnNumber() + " :" + declaration + "\n"; 
		}
		return string;
	}

}
