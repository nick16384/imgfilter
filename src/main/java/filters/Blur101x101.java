package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;

import static filters.base.Filter.*;
import static java.lang.Integer.*;
import static java.lang.Long.*;

/**
 * Blur filter with square averaging. Max. square size in pixels: 101 x 101. Dependent on strength.
 */
public final class Blur101x101 implements Filter<ImageRaster> {
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				int imgWidth = _source.getWidth();
				int imgHeight = _source.getHeight();
				
				// New colors are the average of their surrounding (adjacent)
				// colors, resulting in a blurred image.
				long redSum = 0x0;
				long greenSum = 0x0;
				long blueSum = 0x0;
				int delta = (int)(50.0 * _strength);
				for (int dx = -delta; dx <= delta; dx++) {
					for (int dy = -delta; dy <= delta; dy++) {
						// Prevent out of range pixel coordinates
						int newX = clamp_signed(_x + dx, 0, imgWidth - 1);
						int newY = clamp_signed(_y + dy, 0, imgHeight - 1);
						
						redSum += _source.getRedAt_UL(newX, newY);
						greenSum += _source.getGreenAt_UL(newX, newY);
						blueSum += _source.getBlueAt_UL(newX, newY);
					}
				}
				// For average divide by (2delta + 1) ^ 2. " + 1" is the current pixel itself.
				int pixelCountI = ((2 * delta) + 1) * ((2 * delta) + 1);
				long pixelCount = toUnsignedLong(pixelCountI);
				
				int newRed = (int)divideUnsigned(redSum, pixelCount);
				int newGreen = (int)divideUnsigned(greenSum, pixelCount);
				int newBlue = (int)divideUnsigned(blueSum, pixelCount);
				
				return packPixelData(newRed, newGreen, newBlue);
			}
		);
	
	@Override
	public List<PixelTransformer<ImageRaster>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Square avg. Blur (101 x 101)";
	}
}
