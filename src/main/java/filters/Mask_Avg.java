package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;

import static filters.base.Filter.*;

/**
 * Averages ARGB values of source image and mask.
 */
public final class Mask_Avg implements Filter<ImageRaster> {
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				int newRed = (_red + _mask.getRedAt(_x, _y)) / 2;
				int newGreen = (_green + _mask.getGreenAt(_x, _y)) / 2;
				int newBlue = (_blue + _mask.getBlueAt(_x, _y)) / 2;
				return packPixelData(newRed, newGreen, newBlue);
			}
		);
	
	@Override
	public List<PixelTransformer<ImageRaster>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Average mask";
	}
}
