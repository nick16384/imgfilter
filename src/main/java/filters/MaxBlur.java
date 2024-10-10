package filters;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.MultiPassFilterApplicator;
import filters.base.PixelTransformer;
import filters.base.PostProcessPixelTransformer;
import filters.base.PrePass;

import static filters.base.FilterUtils.*;

/**
 * Interesting blur effect caused by not using the average neighbour value,
 * but rather the highest found.
 */
public final class MaxBlur implements Filter<BufferedImage> {
	private static final List<PixelTransformer<BufferedImage>> mainPasses = Arrays.asList(
			(x, y, argb, prePassData, source, mask, strength) -> {
				int imgWidth = source.getWidth();
				int imgHeight = source.getHeight();
				
				// New colors are the average of their surrounding (adjacent)
				// colors, resulting in a blurred image.
				int newRed = 0;
				int newGreen = 0;
				int newBlue = 0;
				int newAlpha = 0;
				int delta = (int)(50.0 * strength);
				for (int dx = -delta; dx <= delta; dx++) {
					for (int dy = -delta; dy <= delta; dy++) {
						// Prevent out of range pixel coordinates
						int newX = clamp(x + dx, 0, imgWidth - 1);
						int newY = clamp(y + dy, 0, imgHeight - 1);
						
						int adjRGB = source.getRGB(newX, newY);
						newRed = getRed(adjRGB) > newRed ? getRed(adjRGB) : newRed;
						newGreen = getGreen(adjRGB) > newGreen ? getGreen(adjRGB) : newGreen;
						newBlue = getBlue(adjRGB) > newBlue ? getBlue(adjRGB) : newBlue;
						newAlpha = getAlpha(adjRGB) > newAlpha ? getAlpha(adjRGB) : newAlpha;
					}
				}
				
				return toARGB(newRed, newGreen, newBlue, newAlpha);
			}
		);
	
	@Override
	public List<PixelTransformer<BufferedImage>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Square MaxBlur (101 x 101)";
	}
}
