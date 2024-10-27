package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;

import static filters.base.Filter.*;

public class Saturation implements Filter<ImageRaster> {
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				
				double[] sourceHSV = RGBtoHSV(packPixelData(_red, _green, _blue));
				// Increase saturation depending on strength
				sourceHSV[1] *= (2 * _strength);
				// Clamp new saturation between 0.0 and 1.0
				sourceHSV[1] = Math.max(0.0, Math.min(1.0, sourceHSV[1]));
				
				return HSVtoRGB(sourceHSV);
			}
		);
	
	@Override
	public List<PixelTransformer<ImageRaster>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Saturation";
	}
}
