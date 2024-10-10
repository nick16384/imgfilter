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
 * Multiplies source image ARGB values with mask values.
 */
public final class Mask_Multiply implements Filter<BufferedImage> {
	private static final List<PixelTransformer<BufferedImage>> mainPasses = Arrays.asList(
			(x, y, argb, prePassData, source, mask, strength) -> {
				int newRed = (int)(getRed(argb) * ((double)getRed(mask.getRGB(x, y)) / 255));
				int newGreen = (int)(getGreen(argb) * ((double)getGreen(mask.getRGB(x, y)) / 255));
				int newBlue = (int)(getBlue(argb) * ((double)getBlue(mask.getRGB(x, y)) / 255));
				return toARGB(newRed, newGreen, newBlue, getAlpha(argb));
			}
		);
	
	@Override
	public List<PixelTransformer<BufferedImage>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Multiply mask";
	}
}
