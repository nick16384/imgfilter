package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;
import filters.base.UInt;

import static filters.base.Filter.*;
import static filters.base.UInt.*;

/**
 * Interesting blur effect caused by not using the average neighbour value,
 * but rather the highest found.
 */
public final class MaxBlur implements Filter<ImageRaster> {
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				int imgWidth = _source.getWidth();
				int imgHeight = _source.getHeight();
				
				// New colors are the highest of their surrounding (adjacent)
				// colors, resulting in a bright, blocky blurred image.
				long newRed = 0;
				long newGreen = 0;
				long newBlue = 0;
				int delta = (int)(50.0 * _strength);
				for (int dx = -delta; dx <= delta; dx++) {
					for (int dy = -delta; dy <= delta; dy++) {
						// Prevent out of range pixel coordinates
						int newX = clamp_signed(_x + dx, 0, imgWidth - 1);
						int newY = clamp_signed(_y + dy, 0, imgHeight - 1);
						
						long red = _source.getRedAt_UL(newX, newY);
						newRed = ULONG_COMPARATOR.compare(red, newRed) > 0 ? red : newRed;
						long green = _source.getGreenAt_UL(newX, newY);
						newGreen = ULONG_COMPARATOR.compare(green, newGreen) > 0 ? green : newGreen;
						long blue = _source.getBlueAt_UL(newX, newY);
						newBlue = ULONG_COMPARATOR.compare(blue, newBlue) > 0 ? blue : newBlue;
					}
				}
				
				return packPixelData(cast_ulong_uint(newRed), cast_ulong_uint(newGreen), cast_ulong_uint(newBlue));
			}
		);
	
	@Override
	public List<PixelTransformer<ImageRaster>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Square MaxBlur (101 x 101)";
	}
}
