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
 * Averages ARGB values of source image and mask.
 */
public final class Mask_Avg implements Filter<BufferedImage> {
	private static final List<PixelTransformer<BufferedImage>> mainPasses = Arrays.asList(
			(x, y, argb, prePassData, source, mask, strength) -> {
				int newRed = (getRed(argb) + getRed(mask.getRGB(x, y))) / 2;
				int newGreen = (getGreen(argb) + getGreen(mask.getRGB(x, y))) / 2;
				int newBlue = (getBlue(argb) + getBlue(mask.getRGB(x, y))) / 2;
				return toARGB(newRed, newGreen, newBlue, getAlpha(argb));
			}
		);
	
	@Override
	public List<PixelTransformer<BufferedImage>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Average mask";
	}
}
