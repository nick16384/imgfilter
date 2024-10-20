package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;

import static filters.base.Filter.*;
import static filters.base.UInt.*;

/**
 * A sharpening filter.
 * Subtracts a blurred image from the original one to obtain a "detail" mask.
 * This detail mask is added to the original image to increase contrast of details.
 */
public final class Sharpening implements Filter<ImageRaster> {
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				
				double avgRed = 0;
				double avgGreen = 0;
				double avgBlue = 0;
				int delta = (int)(50.0 * _strength);
				for (int dx = -delta; dx <= delta; dx++) {
					int nx = clamp(_x + dx, 0, _source.getWidth() - 1);
					for (int dy = -delta; dy <= delta; dy++) {
						int ny = clamp(_y + dy, 0, _source.getHeight() - 1);
						
						avgRed += _source.getRedAt(nx, ny);
						avgGreen += _source.getGreenAt(nx, ny);
						avgBlue += _source.getBlueAt(nx, ny);
					}
				}
				avgRed /= ((2 * delta) + 1) * ((2 * delta) + 1);
				avgGreen /= ((2 * delta) + 1) * ((2 * delta) + 1);
				avgBlue /= ((2 * delta) + 1) * ((2 * delta) + 1);
				
				int detailRed = _red - (int)avgRed;
				int detailGreen = _green - (int)avgGreen;
				int detailBlue = _blue - (int)avgBlue;
				
				// Make mask a little darker to avoid overly bright images.
				detailRed -= 20;
				detailGreen -= 20;
				detailBlue -= 20;
				
				// FIXME: Add clamped additon
				int newRed = (_red + detailRed);
				int newGreen = (_green + detailGreen);
				int newBlue = (_blue + detailBlue);
				
				return packPixelData(newRed, newGreen, newBlue);
			}
		);
	
	@Override
	public List<PixelTransformer<ImageRaster>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Sharpening";
	}
}
