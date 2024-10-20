package filters.base;

/**
 * TL;DR I was tired of handling Java's ints as uints with alle the conversions
 * necessary to correctly do arithmetic operations, etc. <br>
 * So I made my own class, which represents an unsigned integer and has
 * appropriate operations implemented.
 */
public class UInt {
	public static final int MAX_VALUE = 0xFFFFFFFF;
	public static final int MIN_VALUE = 0x00000000;
	
	int _theUInt;
	
	/**
	 * Initializes a new UInt with default value 0
	 */
	public UInt() {
		this._theUInt = 0;
	}
	
	/**
	 * Initialized a UInt with the specified value. Note that the int
	 * specified must already be in the UInt format!
	 */
	public UInt(int initialValue) {
		this._theUInt = initialValue;
	}
	
	public int intValue() {
		return this._theUInt;
	}
	
	public long get() {
		return (this._theUInt);
	}
	
	
}
