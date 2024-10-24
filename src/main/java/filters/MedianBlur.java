package filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;
import filters.base.UInt;

import static filters.base.Filter.*;

/**
 * Blurs by using the median value of surrounding pixels.
 */
public final class MedianBlur implements Filter<ImageRaster> {
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				int imgWidth = _source.getWidth();
				int imgHeight = _source.getHeight();
				
				// New colors are the median of their adjacent
				// colors, resulting in a blurred image (and removing noise).
				List<Integer> adjReds = new ArrayList<Integer>();
				List<Integer> adjGreens = new ArrayList<Integer>();
				List<Integer> adjBlues = new ArrayList<Integer>();
				int delta = (int)(50.0 * _strength);
				for (int dx = -delta; dx <= delta; dx++) {
					for (int dy = -delta; dy <= delta; dy++) {
						// Prevent out of range pixel coordinates
						int newX = clamp_signed(_x + dx, 0, imgWidth - 1);
						int newY = clamp_signed(_y + dy, 0, imgHeight - 1);
						
						adjReds.add(_source.getRedAt(newX, newY));
						adjGreens.add(_source.getGreenAt(newX, newY));
						adjBlues.add(_source.getBlueAt(newX, newY));
					}
				}
				adjReds.sort(UInt.UINT_COMPARATOR);
				adjGreens.sort(UInt.UINT_COMPARATOR);
				adjBlues.sort(UInt.UINT_COMPARATOR);
				
				// Get the median (middle value) in the list
				int newRed = adjReds.get((adjReds.size() - 1) / 2);
				int newGreen = adjGreens.get((adjGreens.size() - 1) / 2);
				int newBlue = adjBlues.get((adjBlues.size() - 1) / 2);
				
				return packPixelData(newRed, newGreen, newBlue);
			}
		);
	
	@Override
	public List<PixelTransformer<ImageRaster>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Square median blur (101 x 101)";
	}
}
