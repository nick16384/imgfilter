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
 * Converts the image to grayscale. (RGBA values all have the same value per pixel)
 */
public final class Grayscale implements Filter<BufferedImage> {
	private static final List<PixelTransformer<BufferedImage>> mainPasses = Arrays.asList(
			(x, y, argb, prePassData, source, mask, strength) -> {
				int avg = (getRed(argb) + getGreen(argb) + getBlue(argb)) / 3;
				int redNew = avg;
				int greenNew = avg;
				int blueNew = avg;
				int alphaNew = avg;
				
				int modifiedRGB = toARGB(redNew, greenNew, blueNew, alphaNew);
				return modifiedRGB;
			}
		);
	
	@Override
	public List<PixelTransformer<BufferedImage>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Grayscale";
	}
}
