package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;
import filters.base.PrePass;
import filters.base.UInt;

import static filters.base.Filter.*;

/**
 * Adds / subtracts random values from each pixel channel.
 * Dependent on strength.
 */
public final class RandomNoise implements Filter<ImageRaster> {
	private static final List<PrePass<ImageRaster>> prePasses = Arrays.asList(
			(_source, _mask, _strength) -> {
				List<Object> precomputedConstants =  Arrays.asList(
						Integer.toUnsignedLong(ImageRaster.MAX_SAMPLE_VALUE),
						Integer.toUnsignedLong(ImageRaster.MIN_SAMPLE_VALUE),
						(int)(_strength * Long.divideUnsigned(Integer.toUnsignedLong(ImageRaster.MAX_SAMPLE_VALUE), 2))
						);
				return precomputedConstants;
			}
			);
	
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				long maxSampleULong = (long)_prePassData.get(prePasses.getFirst()).get(0);
				long minSampleULong = (long)_prePassData.get(prePasses.getFirst()).get(1);
				int rndStrength = (int)_prePassData.get(prePasses.getFirst()).get(2);
				
				long newRed, newGreen, newBlue;
				newRed = (_red + rndNoise(rndStrength));
				newRed = clamp_unsigned(newRed, minSampleULong, maxSampleULong);
				newGreen = (_green + rndNoise(rndStrength));
				newGreen = clamp_unsigned(newGreen, minSampleULong, maxSampleULong);
				newBlue = (_blue + rndNoise(rndStrength));
				newBlue = clamp_unsigned(newBlue, minSampleULong, maxSampleULong);
				
				return packPixelData((int)newRed, (int)newGreen, (int)newBlue);
			}
		);
	
	/**
	 * Returns a value between -(ampl/2) and ampl/2.
	 * @param max
	 * @return
	 */
	private static int rndNoise(int ampl) {
		return randomInt(ampl) - (ampl / 2);
	}
	
	@Override
	public List<PrePass<ImageRaster>> getPrePasses() {
		return prePasses;
	}
	
	@Override
	public List<PixelTransformer<ImageRaster>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Random noise";
	}
}
