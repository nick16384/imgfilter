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
 * If channel value is below 128, it will be set to 0.
 * Otherwise, it becomes 255.
 */
public final class ZeroMaxContrast implements Filter<BufferedImage> {
	private static final List<PixelTransformer<BufferedImage>> mainPasses = Arrays.asList(
			(x, y, argb, prePassData, source, mask, strength) -> {
				int newRed = getRed(argb) < 128 ? 0 : 255;
				int newGreen = getGreen(argb) < 128 ? 0 : 255;
				int newBlue = getBlue(argb) < 128 ? 0 : 255;
				int newAlpha = getAlpha(argb) < 128 ? 0 : 255;
				
				return toARGB(newRed, newGreen, newBlue, newAlpha);
			}
		);
	
	@Override
	public List<PixelTransformer<BufferedImage>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Min/Max contrast";
	}
}
