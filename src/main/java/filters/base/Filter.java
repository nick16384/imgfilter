package filters.base;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public interface Filter<RasterType extends ImageRaster> {
	public default List<PrePass<RasterType>> getPrePasses() {
		return new ArrayList<>();
	}
	
	public default List<PixelTransformer<RasterType>> getMainPassTransformers() {
		return new ArrayList<>();
	}
	
	public String getName();
	
	// Below are some helper functions for filters
	// They have no specific use for this class itself.

	static int randomInt(int max) {
		return (int)(((double)new Random().nextInt() / Integer.MAX_VALUE) * max);
	}
	
	static int clamp_signed(int num, int min, int max) {
		return Math.max(min, Math.min(max, num));
	}
	static long clamp_signed(long num, long min, long max) {
		return Math.max(min, Math.min(max, num));
	}
	
	static int clamp_unsigned(int num, int min, int max) {
		if (UInt.UINT_COMPARATOR.compare(num, min) < 0) return min;
		else if (UInt.UINT_COMPARATOR.compare(num, max) > 0) return max;
		else return num;
	}
	static long clamp_unsigned(long num, long min, long max) {
	    if (UInt.ULONG_COMPARATOR.compare(num, min) < 0) return min;
	    else if (UInt.ULONG_COMPARATOR.compare(num, max) > 0) return max;
	    else return num;
	}
	
	/*static long clamp(long num, long min, long max) {
		return Math.max(min, Math.min(max, num));
	}*/
	
	public static int[] packPixelData(int red, int green, int blue) {
		return new int[] { red, green, blue };
	}
}
