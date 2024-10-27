package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;
import filters.base.UInt;

import static filters.base.Filter.*;
import static java.lang.Integer.*;

/**
 * Decreases color parts, esp. where they stand in high contrast to the other channels
 */
public class ReducePartial implements Filter<ImageRaster> {
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				
				long channelAvgL = toUnsignedLong(_red) + toUnsignedLong(_green) + toUnsignedLong(_blue);
				channelAvgL = Long.divideUnsigned(channelAvgL, 3);
				int channelAvg = UInt.cast_ulong_uint(channelAvgL);
				
				int chColor;
				for (int i = 0; i < 3; i++) {
					switch (i) {
					case 0: chColor = _red;
					case 1: chColor = _green;
					case 2: chColor = _blue;
					
					// Signed
					long diff = Math.abs(channelAvgL - Integer.toUnsignedLong(chColor));
					chColor -= (int)(_strength * diff);
					if (chColor < 0)
						chColor = ImageRaster.MIN_SAMPLE_VALUE;
					
					switch (i) {
					case 0: _red = chColor;
					case 1: _green = chColor;
					case 2: _blue = chColor;
					}
					}
				}
				return packPixelData(_red, _green, _blue);
			}
		);
	
	@Override
	public List<PixelTransformer<ImageRaster>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "[WIP] Reduce Partial";
	}
}
