package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;

import static filters.base.Filter.*;
import static filters.base.UnsignedIntOperations.*;

/**
 * Multiplies channel values with 0.0 to 2.0 depending on strength.
 * Similar to "Erase" but also takes original channel value into account.
 * Only useful when using channel specific filter.
 * Note: Original TurboColor has been replaced by "Erase"
 */
public final class TurboColor2 implements Filter<ImageRaster> {
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				int str = (int)(2.0 * _strength);
				return packPixelData(
						safe_mul(str, _red),
						safe_mul(str, _green),
						safe_mul(str, _blue));
			}
		);
	
	@Override
	public List<PixelTransformer<ImageRaster>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "TurbuColor2.0 (TM)";
	}
}
