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
 * Highlights regions of high contrast in magenta (RGB: 255, 0, 255).
 * Sensitivity threshold is set by strength.
 */
public final class HighlightContrast implements Filter<ImageRaster> {
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				List<Double> adjValues = new ArrayList<>();
				for (int dx = -3; dx <= 3; dx++) {
					for (int dy = -3; dy <= 3; dy++) {
						int nx = clamp_signed(_x + dx, 0, _source.getWidth() - 1);
						int ny = clamp_signed(_y + dy, 0, _source.getHeight() - 1);
						double avg = Long.divideUnsigned(_source.getRedAt_UL(nx, ny)
										+ _source.getGreenAt_UL(nx, ny)
										+ _source.getBlueAt_UL(nx, ny), 3);
						adjValues.add(avg);
					}
				}
				adjValues.sort(Comparator.naturalOrder());
				double median = adjValues.get((adjValues.size() - 1) / 2);
				for (int dx = -3; dx <= 3; dx++) {
					for (int dy = -3; dy <= 3; dy++) {
						int nx = clamp_signed(_x + dx, 0, _source.getWidth() - 1);
						int ny = clamp_signed(_y + dy, 0, _source.getHeight() - 1);
						double avg = Long.divideUnsigned(_source.getRedAt_UL(nx, ny)
								+ _source.getGreenAt_UL(nx, ny)
								+ _source.getBlueAt_UL(nx, ny), 3);
						
						// Pixel is determined to have high contrast (sharp edge) when the difference
						// between the median of surrounding pixels and the average is greater than
						// a certain value dependent on strength.
						if (Math.abs(avg - median) >
								((double)ImageRaster.MAX_SAMPLE_VALUE_ULONG - ((double)ImageRaster.MAX_SAMPLE_VALUE_ULONG * _strength)))
							return packPixelData(ImageRaster.MAX_SAMPLE_VALUE, 0, ImageRaster.MAX_SAMPLE_VALUE);
					}
				}
				return packPixelData(_red, _green, _blue);
			}
		);
	
	@Override
	public List<PixelTransformer<ImageRaster>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Contrast Highlight";
	}
}
