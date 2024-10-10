package filters.base;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;

@FunctionalInterface
public interface PixelTransformer<ImgType> {
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
	 */
	public int apply(int x, int y, int argb,
			HashMap<PrePass<ImgType>, List<? extends Object>> preProcessingData,
			ImgType source, ImgType mask,
			double strength);
	
	public static final PixelTransformer<BufferedImage> NULL_TRANSFORMER
	= (x, y, argb, prePassData, source, mask, strength) -> {
		return argb;
	};
}
