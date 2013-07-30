package CSSModel;

public class DeclarationValue {
	
	private final String value;
	//private final short type;
	//private int hashCode = -1;
	
	public DeclarationValue(String value) {
		this.value = value;
	//	this.type = type;
	}
	
	public String getValue() {
		return value;
	}
	
	//public short getType() {
	//	return type;
	//}

	@Override
	public int hashCode() {
		//if (hashCode == -1) {
			final int prime = 31;
			int hashCode = 1;
			//result = prime * result + type;
			hashCode = prime * hashCode + ((value == null) ? 0 : value.hashCode());
		//}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		//if (type != other.type)
		//	return false;
		//if (value == null) {
		//	if (other.value != null)
		//		return false;
		//} else if (!value.equals(other.value))*/
		DeclarationValue other = (DeclarationValue) obj;
		return (value.equals(other.value));
	}
	
	@Override
	public String toString() {
		return value;
	}
}
