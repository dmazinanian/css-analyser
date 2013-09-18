package analyser.duplication;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import CSSModel.declaration.Declaration;
import CSSModel.selectors.Selector;

/**
 * 
 * @author Davood Mazinanian
 *
 */
public class Item implements Set<Declaration>, Cloneable {

	private final Set<Declaration> declarations;
	private final Set<Declaration> virtualDeclarations = new HashSet<>();
	private final Set<Selector> support;
	private ItemSet paretnItemSet;
	
	/**
	 * Creates an empty Item
	 */
	public Item() {
		declarations = new HashSet<>();
		support = new HashSet<>();
	}
	
	
	/**
	 * Creates new Item using the given {@link Declaration} object.
	 * @param declaration
	 */
	public Item(Declaration declaration) {
		this();
		declarations.add(declaration);
		support.add(declaration.getSelector());
	}
	
	/**
	 * Creates the item and points its 
	 * declarations set to the given <code>Set<Declaration></code>
	 * @param declarations
	 */
	public Item(Set<Declaration> declarations) {
		this.declarations = declarations;
		support = new HashSet<>();
		for (Declaration d : declarations)
			support.add(d.getSelector());
	}
	
	@Override
	public boolean add(Declaration e) {
		return add(e, false);
	}
	
	public boolean add(Declaration e, boolean isVirtual) {
		virtualDeclarations.add(e);
		boolean supportsChanged = support.add(e.getSelector());
		boolean declarationsChanged = declarations.add(e);
		if (paretnItemSet != null && supportsChanged)
			paretnItemSet.rebuildSupport();
		return declarationsChanged;
	}

	@Override
	public boolean addAll(Collection<? extends Declaration> c) {
		for (Declaration d : c)
			support.add(d.getSelector());
		return declarations.addAll(c);
	}

	@Override
	public void clear() {
		declarations.clear();
		support.clear();
	}

	@Override
	public boolean contains(Object o) {
		return declarations.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return declarations.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return declarations.isEmpty();
	}

	@Override
	public Iterator<Declaration> iterator() {
		return declarations.iterator();
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof Declaration)
		{
			Declaration d = (Declaration) o;
			support.remove(d.getSelector());
		}
		rebuildSupport();
		return declarations.remove(o);
	}

	private void rebuildSupport() {
		for (Declaration d : declarations) {
			support.add(d.getSelector());
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = declarations.removeAll(c);
		rebuildSupport();
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = retainAll(c);
		rebuildSupport();
		return changed;
	}

	@Override
	public int size() {
		return declarations.size();
	}

	@Override
	public Object[] toArray() {
		return declarations.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return declarations.toArray(a);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		for (Declaration d : declarations)
			sb.append(d.toString() + ", ");
		if (sb.length() > 2)
			sb.delete(sb.length() - 2, sb.length());
		sb.append("} : {");
		for (Selector s : support)
			sb.append(s.toString() + ", ");
		if (sb.length() > 2)
			sb.delete(sb.length() - 2, sb.length());
		sb.append("}");
		
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		return declarations.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return declarations.hashCode();
	}
	
	@Override
	protected Item clone() {
		return new Item(new HashSet<Declaration>(declarations));
	}
	
	public Set<Selector> getSupport() {
		return support;
	}

	/**
	 * Returns the first declaration in the set
	 * which is used as the representative of the set
	 * @return
	 */
	public Declaration getFirstDeclaration() {
		if (size() > 0) {
			Declaration nonVirtualDeclaration = null;
			for (Declaration d : declarations) {
				nonVirtualDeclaration = d; 
				if (virtualDeclarations.contains(nonVirtualDeclaration))
					nonVirtualDeclaration = null;
				else
					break;
			}
			return nonVirtualDeclaration;  
		}
		else
			return null;
	}

	/**
	 * Sets the parent itemset which contains this item
	 * @param itemSet
	 */
	public void setParentItemSet(ItemSet itemSet) {
		paretnItemSet = itemSet;
	}
	
	/**
	 * Returns current imte's container itemset
	 * @return
	 */
	public ItemSet getParentItemSet() {
		return paretnItemSet;
	}
}
