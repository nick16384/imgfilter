package filters;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import filters.base.Filter;
import filters.base.MultiPassFilterApplicator;
import filters.base.PixelTransformer;
import filters.base.PostProcessPixelTransformer;
import filters.base.PrePass;

import static filters.base.FilterUtils.*;

/**
 * Blurs by using the median value of surrounding pixels.
 */
public final class MedianBlur implements Filter<BufferedImage> {
	private static final List<PixelTransformer<BufferedImage>> mainPasses = Arrays.asList(
			(x, y, argb, prePassData, source, mask, strength) -> {
				int imgWidth = source.getWidth();
				int imgHeight = source.getHeight();
				
				// New colors are the median of their adjacent
				// colors, resulting in a blurred image (and removing noise).
				List<Integer> adjReds = new ArrayList<Integer>();
				List<Integer> adjGreens = new ArrayList<Integer>();
				List<Integer> adjBlues = new ArrayList<Integer>();
				List<Integer> adjAlphas = new ArrayList<Integer>();
				int delta = (int)(50.0 * strength);
				for (int dx = -delta; dx <= delta; dx++) {
					for (int dy = -delta; dy <= delta; dy++) {
						// Prevent out of range pixel coordinates
						int newX = clamp(x + dx, 0, imgWidth - 1);
						int newY = clamp(y + dy, 0, imgHeight - 1);
						
						int adjRGB = source.getRGB(newX, newY);
						adjReds.add(getRed(adjRGB));
						adjGreens.add(getGreen(adjRGB));
						adjBlues.add(getBlue(adjRGB));
						adjAlphas.add(getAlpha(adjRGB));
					}
				}
				adjReds.sort(Comparator.naturalOrder());
				adjGreens.sort(Comparator.naturalOrder());
				adjBlues.sort(Comparator.naturalOrder());
				adjAlphas.sort(Comparator.naturalOrder());
				
				// Get the median (middle value) in the list
				int newRed = adjReds.get((adjReds.size() - 1) / 2);
				int newGreen = adjGreens.get((adjGreens.size() - 1) / 2);
				int newBlue = adjBlues.get((adjBlues.size() - 1) / 2);
				int newAlpha = adjAlphas.get((adjAlphas.size() - 1) / 2);
				
				return toARGB(newRed, newGreen, newBlue, newAlpha);
			}
		);
	
	@Override
	public List<PixelTransformer<BufferedImage>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Square median blur (101 x 101)";
	}
}
