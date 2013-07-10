package CSSModel;

public abstract class AtomicSelector extends Selector {

	private GroupedSelectors parentGroupSelector;

	public AtomicSelector() {
		this(null, -1, -1);
	}

	public AtomicSelector(GroupedSelectors parent) {
		this(parent, -1, -1);
	}

	public AtomicSelector(int line, int coloumn) {
		this(null, line, coloumn);
	}

	public AtomicSelector(GroupedSelectors parent, int line, int coloumn) {
		super(line, coloumn);
		parentGroupSelector = parent;
	}

	public void setParentGroupSelector(GroupedSelectors newGroup) {
		parentGroupSelector = newGroup;
	}

	public GroupedSelectors getParentGroupSelector() {
		return parentGroupSelector;
	}
}
