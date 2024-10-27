package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;
import filters.base.UInt;

import static filters.base.Filter.*;
import static java.lang.Integer.*;

/**
 * A sharpening filter.
 * Subtracts a blurred image from the original one to obtain a "detail" mask.
 * This detail mask is added to the original image to increase contrast of details.
 */
public final class Sharpening implements Filter<ImageRaster> {
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				
				long avgRed = 0;
				long avgGreen = 0;
				long avgBlue = 0;
				
				int[] curRGB = new int[3];
				int delta = (int)(50.0 * _strength);
				for (int dx = -delta; dx <= delta; dx++) {
					int nx = clamp_signed(_x + dx, 0, _source.getWidth() - 1);
					for (int dy = -delta; dy <= delta; dy++) {
						int ny = clamp_signed(_y + dy, 0, _source.getHeight() - 1);
						
						// Calling getPixelRGBAt() once and then converting to unsigned longs
						// gives a huge performance boost over calling get[Color]At_UL() for each channel.
						curRGB = _source.getPixelRGBAt(nx, ny);
						avgRed += toUnsignedLong(curRGB[0]);
						avgGreen += toUnsignedLong(curRGB[1]);
						avgBlue += toUnsignedLong(curRGB[2]);
					}
				}
				int pixelCountI = ((2 * delta) + 1) * ((2 * delta) + 1);
				long pixelCount = toUnsignedLong(pixelCountI);
				avgRed = Long.divideUnsigned(avgRed, pixelCount);
				avgGreen = Long.divideUnsigned(avgGreen, pixelCount);
				avgBlue = Long.divideUnsigned(avgBlue, pixelCount);
				
				// Detail values are SIGNED!
				long detailRed = toUnsignedLong(_red) - avgRed;
				long detailGreen = toUnsignedLong(_green) - avgGreen;
				long detailBlue = toUnsignedLong(_blue) - avgBlue;
				
				// FIXME: Fix these values or take them out entirely
				// Make mask a little darker to avoid overly bright images.
				//detailRed -= 20;
				//detailGreen -= 20;
				//detailBlue -= 20;
				
				long newRed = toUnsignedLong(_red) + detailRed;
				newRed = clamp_signed(newRed, ImageRaster.MIN_SAMPLE_VALUE_ULONG, ImageRaster.MAX_SAMPLE_VALUE_ULONG);
				long newGreen = toUnsignedLong(_green) + detailGreen;
				newGreen = clamp_signed(newGreen, ImageRaster.MIN_SAMPLE_VALUE_ULONG, ImageRaster.MAX_SAMPLE_VALUE_ULONG);
				long newBlue = toUnsignedLong(_blue) + detailBlue;
				newBlue = clamp_signed(newBlue, ImageRaster.MIN_SAMPLE_VALUE_ULONG, ImageRaster.MAX_SAMPLE_VALUE_ULONG);
				
				
				// TODO: Add packPixelData() with ulong as input. Internally, use UInt.cast_ulong_uint()
				return packPixelData(UInt.cast_ulong_uint(newRed), UInt.cast_ulong_uint(newGreen), UInt.cast_ulong_uint(newBlue));
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
