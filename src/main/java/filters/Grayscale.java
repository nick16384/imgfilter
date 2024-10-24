package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;

import static filters.base.Filter.*;
import static java.lang.Integer.*;

/**
 * Converts the image to grayscale. (RGBA values all have the same value per pixel)
 */
public final class Grayscale implements Filter<ImageRaster> {
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				long componentSum = toUnsignedLong(_red) + toUnsignedLong(_green) + toUnsignedLong(_blue);
				int avg = (int)Long.divideUnsigned(componentSum, 3);
				int redNew = avg;
				int greenNew = avg;
				int blueNew = avg;
				
				int[] modifiedRGB = packPixelData(redNew, greenNew, blueNew);
				return modifiedRGB;
			}
		);
	
	@Override
	public List<PixelTransformer<ImageRaster>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Grayscale";
	}
}
