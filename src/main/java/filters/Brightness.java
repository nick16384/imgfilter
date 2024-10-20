package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;

import static filters.base.Filter.*;

/**
 * Very similar to TurboColor2.0, but has a different channel modifier.
 * multiplicator range (0.5 to 1.5 instead of 0.0 to 2.0)
 */
public final class Brightness implements Filter<ImageRaster> {
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				double brightModifier = _strength + 0.5;
				
				int newRed = (int)(_red * brightModifier);
				int newGreen = (int)(_green * brightModifier);
				int newBlue = (int)(_blue * brightModifier);
				
				return packPixelData(newRed, newGreen, newBlue);
			}
		);
	
	@Override
	public List<PixelTransformer<ImageRaster>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Brightness";
	}
}
