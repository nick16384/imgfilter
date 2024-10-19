package filters.base;

public enum RGBChannel {
	RED("Red"),
	GREEN("Green"),
	BLUE("Blue"),
	ALL("All");
	
	String displayName;
	private RGBChannel(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
}
