package filters;

import java.util.Arrays;
import java.util.List;

import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.PixelTransformer;
import filters.base.PrePass;

import static filters.base.Filter.*;

/**
 * [Work in progress] Increases image contrast with an adjustment curve
 * (less values in the middle range, more values in more extreme range)
 * TODO: Make middle of adjustment curve based on average pixel brightness. (Pre-pass: Histogram)
 */
public final class Contrast implements Filter<ImageRaster> {
	private static final PrePass<ImageRaster> histogramPrePass = (_source, _mask, _strength) -> {
		double avgRed = 0;
		double avgGreen = 0;
		double avgBlue = 0;
		int pixelCount = 0;
		for (int x = 0; x < _source.getWidth(); x++) {
			for (int y = 0; y < _source.getHeight(); y++) {
				avgRed += _source.getRedAt(x, y);
				avgGreen += _source.getGreenAt(x, y);
				avgBlue += _source.getBlueAt(x, y);
				pixelCount++;
			}
		}
		avgRed /= pixelCount;
		avgGreen /= pixelCount;
		avgBlue /= pixelCount;
		
		System.out.println("[Histogram Pre-Pass] RGBA avg.: "
				+ avgRed + ", " + avgGreen + ", " + avgBlue);
		
		return Arrays.asList(avgRed, avgGreen, avgBlue);
	};
	private static final List<PrePass<ImageRaster>> prePasses =
			Arrays.asList(histogramPrePass);
	
	private static final List<PixelTransformer<ImageRaster>> mainPasses = Arrays.asList(
			(_x, _y, _red, _green, _blue, _prePassData, _source, _mask, _strength) -> {
				int newRed = 0, newGreen = 0, newBlue = 0;
				
				for (int i = 0; i < 4; i++) {
					int channelColor = 0;
					double channelAvg = 0;
					switch (i) {
					case 0: channelColor = _red; break;
					case 1: channelColor = _green; break;
					case 2: channelColor = _blue; break;
					}
					
					// Transform old value into new value
					
					
					// Higher invDeviance ==> lower contrast
					double gammaTransformResult =
							gammaTransform((double)channelColor / ImageRaster.MAX_SAMPLE_VALUE, _strength);
					int newChannelColor = (int)(channelColor * gammaTransformResult);
					
					switch (i) {
					case 0: newRed = newChannelColor; break;
					case 1: newGreen = newChannelColor; break;
					case 2: newBlue = newChannelColor; break;
					}
				}
				
				return packPixelData(newRed, newGreen, newBlue);
			}
		);
	
	private static double gammaTransform(double in, double s) {
		return (in * Math.pow(Math.E, s*in)) / Math.pow(Math.E, s);
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
		return "Contrast";
	}
}
