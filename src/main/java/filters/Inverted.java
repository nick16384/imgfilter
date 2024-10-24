package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;

import static filters.base.Filter.*;

/**
 * Inverts the image (RGBA values are multiplied with -1)
 */
public final class Inverted implements Filter<ImageRaster> {
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				int newRed = (int)(ImageRaster.MAX_SAMPLE_VALUE_ULONG - _red);
				int newGreen = (int)(ImageRaster.MAX_SAMPLE_VALUE_ULONG - _green);
				int newBlue = (int)(ImageRaster.MAX_SAMPLE_VALUE_ULONG - _blue);
				return packPixelData(newRed, newGreen, newBlue);
			}
		);
	
	@Override
	public List<PixelTransformer<ImageRaster>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Inverted";
	}
}
