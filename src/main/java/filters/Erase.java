package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;
import filters.base.UInt;

import static filters.base.Filter.*;

/**
 * Erases all values from all channels and replaces them with
 * a value between MIN_SAMPLE_VALUE and MAX_SAMPLE_VALUE dependent on strength.
 * Only useful in combination with a channel selector.
 */
public final class Erase implements Filter<ImageRaster> {
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				int newVal = UInt.cast_ulong_uint((long)(_strength * ImageRaster.MAX_SAMPLE_VALUE_ULONG));
				return packPixelData(newVal, newVal, newVal);
			}
		);
	
	@Override
	public List<PixelTransformer<ImageRaster>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Erase";
	}
}
