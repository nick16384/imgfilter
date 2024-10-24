package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;

import static filters.base.Filter.*;

/**
 * If channel value is below 128, it will be set to 0.
 * Otherwise, it becomes 255.
 */
public final class ZeroMaxContrast implements Filter<ImageRaster> {
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				int halfMaxSample = (int)Long.divideUnsigned(Integer.toUnsignedLong(ImageRaster.MAX_SAMPLE_VALUE), 2);
				int maxSample = ImageRaster.MAX_SAMPLE_VALUE;
				int minSample = ImageRaster.MIN_SAMPLE_VALUE;
				
				int newRed = _red < halfMaxSample ? minSample : maxSample;
				int newGreen = _green < halfMaxSample ? minSample : maxSample;
				int newBlue = _blue < halfMaxSample ? minSample : maxSample;
				
				return packPixelData(newRed, newGreen, newBlue);
			}
		);
	
	@Override
	public List<PixelTransformer<ImageRaster>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Min/Max contrast";
	}
}
