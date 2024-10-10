package filters;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.MultiPassFilterApplicator;
import filters.base.PixelTransformer;
import filters.base.PostProcessPixelTransformer;
import filters.base.PrePass;

import static filters.base.FilterUtils.*;

/**
 * Adds / subtracts random values from each pixel channel.
 * Dependent on strength.
 */
public final class RandomNoise implements Filter<BufferedImage> {
	private static final List<PixelTransformer<BufferedImage>> mainPasses = Arrays.asList(
			(x, y, argb, prePassData, source, mask, strength) -> {
				int rndStrength = (int)(255 * strength);
				int newRed, newGreen, newBlue, newAlpha;
				// Clamp values to prevent over-/underflow of channel brightness
				newRed = clamp0255(getRed(argb) + clamp(randomInt(rndStrength), -0x100, 0xFF));
				newGreen = clamp0255(getGreen(argb) + clamp(randomInt(rndStrength), -0x100, 0xFF));
				newBlue = clamp0255(getBlue(argb) + clamp(randomInt(rndStrength), -0x100, 0xFF));
				newAlpha = clamp0255(getAlpha(argb) + clamp(randomInt(rndStrength), -0x100, 0xFF));
				
				return toARGB(newRed, newGreen, newBlue, newAlpha);
			}
		);
	
	@Override
	public List<PixelTransformer<BufferedImage>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Random noise";
	}
}
