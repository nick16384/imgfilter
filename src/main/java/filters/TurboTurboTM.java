package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;

import static filters.base.Filter.*;
import static java.lang.Integer.*;

/**
 * A failed attempt at "Brightness".
 * Adjusts brightness but does not clamp values, meaning
 * over- / underflows are allowed.
 */
public final class TurboTurboTM implements Filter<ImageRaster> {
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				double brightModifier = _strength + 0.5;
				
				int newRed = (int)(toUnsignedLong(_red) * brightModifier);
				int newGreen = (int)(toUnsignedLong(_green) * brightModifier);
				int newBlue = (int)(toUnsignedLong(_blue) * brightModifier);
				
				return packPixelData(newRed, newGreen, newBlue);
			}
		);
	
	@Override
	public List<PixelTransformer<ImageRaster>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "TurboTurbo (TM)";
	}
}
