package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;

import static filters.base.Filter.*;

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
				int newRed = 0;
				int newGreen = 0;
				int newBlue = 0;
				int delta = (int)(50.0 * _strength);
				for (int dx = -delta; dx <= delta; dx++) {
					for (int dy = -delta; dy <= delta; dy++) {
						// Prevent out of range pixel coordinates
						int newX = clamp(_x + dx, 0, imgWidth - 1);
						int newY = clamp(_y + dy, 0, imgHeight - 1);
						
						newRed += _source.getRedAt(newX, newY);
						newGreen += _source.getGreenAt(newX, newY);
						newBlue += _source.getBlueAt(newX, newY);
					}
				}
				// For average divide by (2delta + 1) ^ 2. " + 1" is the current pixel itself.
				newRed /= ((2 * delta) + 1) * ((2 * delta) + 1);
				newGreen /= ((2 * delta) + 1) * ((2 * delta) + 1);
				newBlue /= ((2 * delta) + 1) * ((2 * delta) + 1);
				
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
