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
 * Multiplies channel values with 0.0 to 2.0 depending on strength.
 * Similar to "Erase" but also takes original channel value into account.
 * Only useful when using channel specific filter.
 * Note: Original TurboColor has been replaced by "Erase"
 */
public final class TurboColor2 implements Filter<BufferedImage> {
	private static final List<PixelTransformer<BufferedImage>> mainPasses = Arrays.asList(
			(x, y, argb, prePassData, source, mask, strength) -> {
				return toARGB(
						clamp0255((int)(2.0 * strength * getRed(argb))),
						clamp0255((int)(2.0 * strength * getGreen(argb))),
						clamp0255((int)(2.0 * strength * getBlue(argb))),
						clamp0255((int)(2.0 * strength * getAlpha(argb))));
			}
		);
	
	@Override
	public List<PixelTransformer<BufferedImage>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "TurbuColor2.0 (TM)";
	}
}
