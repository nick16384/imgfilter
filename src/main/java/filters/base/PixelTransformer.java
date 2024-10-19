package filters.base;

import java.util.HashMap;
import java.util.List;

@FunctionalInterface
public interface PixelTransformer<RasterType> {
	/**
	 * Pixel transformer function, which takes in various parameters and
	 * returns a newly determined pixel value.<br>
	 * Parameters: <br>
	 * * (Marked with asterisk: Does not affect all filters)<br>
	 * [Per-pixel] X coordinate*<br>
	 * [Per-pixel] Y coordinate*<br>
	 * [Per-pixel] Current RGB value<br>
	 * [Global] Source image<br>
	 * [Global] Masking image*<br>
	 * [Global] Filter strength (0.0 - 1.0)*<br>
	 * @return rgb [Per-pixel] Resulting RGB value
	 * @apiNote Function input values should begin with an underscore symbol
	 * to differentiate them from function internal variables.
	 */
	public int[] apply(int _x, int _y, int _red, int _green, int _blue,
			HashMap<PrePass<RasterType>, List<? extends Object>> _preProcessingData,
			RasterType _source, RasterType _mask,
			double _strength);
	
	public static final PixelTransformer<ImageRaster> NULL_TRANSFORMER
	= (_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
		return Filter.packPixelData(_red, _green, _blue);
	};
}
