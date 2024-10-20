package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;
import filters.base.UInt;

import static filters.base.Filter.*;

/**
 * Adds / subtracts random values from each pixel channel.
 * Dependent on strength.
 */
public final class RandomNoise implements Filter<ImageRaster> {
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				int rndStrength = (int)(ImageRaster.MAX_SAMPLE_VALUE * _strength);
				int newRed, newGreen, newBlue;
				// FIXME: Add clamped addition
				newRed = (_red + randomInt(rndStrength));
				newGreen = (_green + randomInt(rndStrength));
				newBlue = (_blue + randomInt(rndStrength));
				/*newRed = clamp0MAX(_red + clamp(randomInt(rndStrength), -0x100, 0xFF));
				newGreen = clamp0MAX(_green + clamp(randomInt(rndStrength), -0x100, 0xFF));
				newBlue = clamp0MAX(_blue + clamp(randomInt(rndStrength), -0x100, 0xFF));*/
				
				return packPixelData(newRed, newGreen, newBlue);
			}
		);
	
	@Override
	public List<PixelTransformer<ImageRaster>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Random noise";
	}
}
