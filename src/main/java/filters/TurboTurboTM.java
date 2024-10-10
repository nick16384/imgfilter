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
 * A failed attempt at "Brightness".
 * Adjusts brightness but does not clamp values, meaning
 * over- / underflows are allowed.
 */
public final class TurboTurboTM implements Filter<BufferedImage> {
	private static final List<PixelTransformer<BufferedImage>> mainPasses = Arrays.asList(
			(x, y, argb, prePassData, source, mask, strength) -> {
				double brightModifier = strength + 0.5;
				
				int newRed = (int)(getRed(argb) * brightModifier);
				int newGreen = (int)(getGreen(argb) * brightModifier);
				int newBlue = (int)(getBlue(argb) * brightModifier);
				int newAlpha = (int)(getAlpha(argb) * brightModifier);
				
				return toARGB(newRed, newGreen, newBlue, newAlpha);
			}
		);
	
	@Override
	public List<PixelTransformer<BufferedImage>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "TurboTurbo (TM)";
	}
}
