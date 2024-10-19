package filters.base;

/**
 * Contains useful methods for treating java "int"s as unsigned integers
 * and using math operations without overflows.
 */
public class UnsignedIntOperations {

	/**
	 * Adds two numbers without over- / underflow. Values that would
	 * undergo over- / underflow are clamped between Integer.MAX_VALUE and Integer.MIN_VALUE.
	 */
	public static int safe_add(int num1, int num2) {
		long num1L = (long)num1;
		long num2L = (long)num2;
		long res = num1L + num2L;
		res &= 0x00000000_FFFFFFFFl;
		long resWithoutCarry = num1L ^ num2L;
		resWithoutCarry &= 0x00000000_FFFFFFFF;
		
		//boolean num1MSB = (num1 >> 31) == 1 ? true : false;
		//boolean num2MSB = (num2 >> 31) == 1 ? true : false;
		boolean resMSB = (res >> 63) == 1 ? true : false;
		boolean resWithoutCarryMSB = (resWithoutCarry >> 63) == 1 ? true : false;
		
		if (resMSB != resWithoutCarryMSB) {
			int halfIntMax = Integer.MAX_VALUE / 2;
			if (num1 > halfIntMax || num2 > halfIntMax)
				return Integer.MAX_VALUE;
			else
				return Integer.MIN_VALUE;
		}
		return num1 + num2;
		
		// Highest possible uint: 0xFFFFFFFF_FFFFFFFF, lowest: 0x00000000_00000000
		// Highest possible int: 0x7FFFFFFF_FFFFFFFF, lowest: 0xFFFFFFFF_FFFFFFFF
	}
	
	public static int safe_mul(int num1, int num2) {
		long num1L = (long)num1;
		long num2L = (long)num2;
		long res = num1L * num2L;
		res &= 0xFFFFFFFF_00000000l;
		int resUpper = (int)(res >> 32);
		
		// Overflow into positive direction
		if (resUpper > 0)
			return Integer.MAX_VALUE;
		// Overflow into negative direction
		else if (resUpper < 0)
			return Integer.MIN_VALUE;
		
		return (int)res;
	}
	
	// FIXME: Implement this. Basic implementation of now allows overflows.
	// Keep in mind: doubles have a weird bit structure
	public static double safe_mul(int num1, double num2) {
		return num1 * num2;
	}
}
