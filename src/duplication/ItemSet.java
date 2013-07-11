package duplication;

import java.util.Iterator;
import java.util.Set;

import CSSModel.Declaration;
import CSSModel.Selector;

/**
 * This class keeps the data of a itemset, in addition to its support 
 * In our definition, every itemset is a set of declarations and
 * support means the number of selectors that have all these declarations.
 * In fact, instead of keeping the support as a pure percentage or number of supports,
 * we keep the selectors for further uses. 
 * 
 * @author Davood Mazinanian
 *
 */
public class ItemSet implements Iterable<Declaration> {
	
	private Set<Declaration> itemsetField;
	private Set<Selector> selectors;
	
	public ItemSet() {
		this(null, null); // Never pass null. I know.
	}
	
	public ItemSet(Set<Declaration> declarations, Set<Selector> selectorsList) {
		itemsetField = declarations;
		selectors = selectorsList;
	}
	
	public int getSupport() {
		return selectors.size();
	}
	
	
	public Set<Declaration> getItemSet() {
		return itemsetField;
	}
	
	public void setItemSet(Set<Declaration> itemset) {
		itemsetField = itemset;
	}
	
	public void setSelectorList(Set<Selector> s) {
		selectors = s;
	}
	
	public Set<Selector> getSelectors() {
		return selectors;
	}

	@Override
	public Iterator<Declaration> iterator() {
		return itemsetField.iterator();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof ItemSet))
			return false;

		ItemSet otherObj = (ItemSet)obj; 
		if (itemsetField.size() != otherObj.itemsetField.size())
			return false;
		return itemsetField.containsAll(otherObj.itemsetField);
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		for (Declaration d : itemsetField)
			result += d.hashCode();
		return result;
	}
}
