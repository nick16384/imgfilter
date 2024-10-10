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
 * A sharpening filter.
 * Subtracts a blurred image from the original one to obtain a "detail" mask.
 * This detail mask is added to the original image to increase contrast of details.
 */
public final class Sharpening implements Filter<BufferedImage> {
	private static final List<PixelTransformer<BufferedImage>> mainPasses = Arrays.asList(
			(x, y, argb, prePassData, source, mask, strength) -> {
				
				double avgRed = 0;
				double avgGreen = 0;
				double avgBlue = 0;
				int delta = (int)(50.0 * strength);
				for (int dx = -delta; dx <= delta; dx++) {
					int nx = clamp(x + dx, 0, source.getWidth() - 1);
					for (int dy = -delta; dy <= delta; dy++) {
						int ny = clamp(y + dy, 0, source.getHeight() - 1);
						
						int adjRGB = source.getRGB(nx, ny);
						avgRed += getRed(adjRGB);
						avgGreen += getGreen(adjRGB);
						avgBlue += getBlue(adjRGB);
					}
				}
				avgRed /= ((2 * delta) + 1) * ((2 * delta) + 1);
				avgGreen /= ((2 * delta) + 1) * ((2 * delta) + 1);
				avgBlue /= ((2 * delta) + 1) * ((2 * delta) + 1);
				
				int detailRed = getRed(argb) - (int)avgRed;
				int detailGreen = getGreen(argb) - (int)avgGreen;
				int detailBlue = getBlue(argb) - (int)avgBlue;
				
				// Make mask a little darker to avoid overly bright images.
				detailRed -= 20;
				detailGreen -= 20;
				detailBlue -= 20;
				
				int newRed = clamp0255(getRed(argb) + detailRed);
				int newGreen = clamp0255(getGreen(argb) + detailGreen);
				int newBlue = clamp0255(getBlue(argb) + detailBlue);
				
				return toARGB(newRed, newGreen, newBlue, getAlpha(argb));
			}
		);
	
	@Override
	public List<PixelTransformer<BufferedImage>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Sharpening";
	}
}
