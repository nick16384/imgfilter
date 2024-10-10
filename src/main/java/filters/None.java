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
 * Filter that does nothing. For testing purposes only.
 */
public final class None implements Filter<BufferedImage> {
	private static final List<PixelTransformer<BufferedImage>> mainPasses = Arrays.asList(
			(x, y, argb, prePassData, source, mask, strength) -> {
				return argb;
			}
		);
	
	private static final List<PostProcessPixelTransformer<BufferedImage>> postPasses = Arrays.asList(
			(x, y, argb, prePassData, original, source, mask, strength) -> {
				return argb;
			}
		);
	
	@Override
	public List<PixelTransformer<BufferedImage>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public List<PostProcessPixelTransformer<BufferedImage>> getPostPassTransformers() {
		return postPasses;
	}
	
	@Override
	public String getName() {
		return "[Nothing]";
	}
}
