package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;

import static filters.base.Filter.*;

/**
 * Interesting blur effect caused by not using the average neighbour value,
 * but rather the highest found.
 */
public final class MaxBlur implements Filter<ImageRaster> {
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
						
						int red = _source.getRedAt(newX, newY);
						newRed = red > newRed ? red : newRed;
						int green = _source.getGreenAt(newX, newY);
						newGreen = green > newGreen ? green : newGreen;
						int blue = _source.getBlueAt(newX, newY);
						newBlue = blue > newBlue ? blue : newBlue;
					}
				}
				
				return packPixelData(newRed, newGreen, newBlue);
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
