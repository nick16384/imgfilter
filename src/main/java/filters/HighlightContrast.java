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
 * Highlights regions of high contrast in magenta (RGB: 255, 0, 255).
 * Sensitivity threshold is set by strength.
 */
public final class HighlightContrast implements Filter<BufferedImage> {
	private static final List<PixelTransformer<BufferedImage>> mainPasses = Arrays.asList(
			(x, y, argb, prePassData, source, mask, strength) -> {
				List<Double> adjValues = new ArrayList<>();
				for (int dx = -3; dx <= 3; dx++) {
					for (int dy = -3; dy <= 3; dy++) {
						int nx = clamp(x + dx, 0, source.getWidth() - 1);
						int ny = clamp(y + dy, 0, source.getHeight() - 1);
						double avg = (getRed(source.getRGB(nx, ny))
										+ getRed(source.getRGB(nx, ny))
										+ getRed(source.getRGB(nx, ny))) / 3;
						adjValues.add(avg);
					}
				}
				adjValues.sort(Comparator.naturalOrder());
				double median = adjValues.get((adjValues.size() - 1) / 2);
				for (int dx = -3; dx <= 3; dx++) {
					for (int dy = -3; dy <= 3; dy++) {
						int nx = clamp(x + dx, 0, source.getWidth() - 1);
						int ny = clamp(y + dy, 0, source.getHeight() - 1);
						double avg = (getRed(source.getRGB(nx, ny))
								+ getRed(source.getRGB(nx, ny))
								+ getRed(source.getRGB(nx, ny))) / 3;
						if (Math.abs(avg - median) > (255.0 - (255.0 * strength)))
							return toARGB(255, 0, 255, getAlpha(argb));
					}
				}
				return argb;
			}
		);
	
	@Override
	public List<PixelTransformer<BufferedImage>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Contrast Highlight";
	}
}
