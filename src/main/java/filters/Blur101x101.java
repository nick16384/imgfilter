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
 * Blur filter with square averaging. Max. square size in pixels: 101 x 101. Dependent on strength.
 */
public final class Blur101x101 implements Filter<BufferedImage> {
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
						newRed += getRed(adjRGB);
						newGreen += getGreen(adjRGB);
						newBlue += getBlue(adjRGB);
						newAlpha += getAlpha(adjRGB);
					}
				}
				// For average divide by (2delta + 1) ^ 2. " + 1" is the current pixel itself.
				newRed /= ((2 * delta) + 1) * ((2 * delta) + 1);
				newGreen /= ((2 * delta) + 1) * ((2 * delta) + 1);
				newBlue /= ((2 * delta) + 1) * ((2 * delta) + 1);
				newAlpha /= ((2 * delta) + 1) * ((2 * delta) + 1);
				
				return toARGB(newRed, newGreen, newBlue, newAlpha);
			}
		);
	
	@Override
	public List<PixelTransformer<BufferedImage>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Square avg. Blur (101 x 101)";
	}
}
