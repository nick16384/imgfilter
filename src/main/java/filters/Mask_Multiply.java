package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;

import static filters.base.Filter.*;

/**
 * Multiplies source image ARGB values with mask values.
 */
public final class Mask_Multiply implements Filter<ImageRaster> {
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				int newRed = (int)(_red * ((double)_mask.getRedAt(_x, _y) / ImageRaster.MAX_SAMPLE_VALUE));
				int newGreen = (int)(_green * ((double)_mask.getGreenAt(_x, _y) / ImageRaster.MAX_SAMPLE_VALUE));
				int newBlue = (int)(_blue * ((double)_mask.getBlueAt(_x, _y) / ImageRaster.MAX_SAMPLE_VALUE));
				return packPixelData(newRed, newGreen, newBlue);
			}
		);
	
	@Override
	public List<PixelTransformer<ImageRaster>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Multiply mask";
	}
}
