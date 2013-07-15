package CSSModel;

public class PseudoSelectionClass extends PseudoClass {

	public PseudoSelectionClass(String name) {
		super(name);
	}
	
	@Override
	public String toString() {
		return ":" + super.toString();
	}

}
