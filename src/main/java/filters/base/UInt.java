package filters.base;

import java.util.Comparator;

public class UInt {
	public static final int MAX_VALUE = 0xFFFFFFFF;
	public static final int MIN_VALUE = 0x00000000;
	
	public static final Comparator<Long> ULONG_COMPARATOR =
			(x, y) -> { return Long.compareUnsigned(x, y); };
	public static final Comparator<Integer> UINT_COMPARATOR =
			(x, y) -> {
				long xL = Integer.toUnsignedLong(x);
				long yL = Integer.toUnsignedLong(y);
				return ULONG_COMPARATOR.compare(xL, yL);
				};
	
	/**
	 * Casts a ULong to a UInt the safe way
	 * (ensuring positive sign by and-masking off all bytes that wouldn't fit into 32 bits either way)
	 * @param ulong
	 * @return
	 */
	public static int cast_ulong_uint(long ulong) {
		return (int)(ulong & 0x00000000_FFFFFFFFFl);
	}
}
