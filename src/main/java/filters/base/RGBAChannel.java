package filters.base;

public enum RGBAChannel {
	RED("Red"),
	GREEN("Green"),
	BLUE("Blue"),
	ALPHA("Alpha"),
	
	ALL_EXCEPT_ALPHA("All except Alpha"),
	ALL("All");
	
	String displayName;
	private RGBAChannel(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
}
