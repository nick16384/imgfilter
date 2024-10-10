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
 * Very similar to TurboColor2.0, but has a different channel modifier.
 * multiplicator range (0.5 to 1.5 instead of 0.0 to 2.0)
 */
public final class Brightness implements Filter<BufferedImage> {
	private static final List<PixelTransformer<BufferedImage>> mainPasses = Arrays.asList(
			(x, y, argb, prePassData, source, mask, strength) -> {
				double brightModifier = strength + 0.5;
				
				int newRed = (int)(getRed(argb) * brightModifier);
				newRed = clamp0255(newRed);
				int newGreen = (int)(getGreen(argb) * brightModifier);
				newGreen = clamp0255(newGreen);
				int newBlue = (int)(getBlue(argb) * brightModifier);
				newBlue = clamp0255(newBlue);
				int newAlpha = (int)(getAlpha(argb) * brightModifier);
				newAlpha = clamp0255(newAlpha);
				
				return toARGB(newRed, newGreen, newBlue, newAlpha);
			}
		);
	
	@Override
	public List<PixelTransformer<BufferedImage>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Brightness";
	}
}
