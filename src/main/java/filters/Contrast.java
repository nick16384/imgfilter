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
 * [Work in progress] Increases image contrast with an adjustment curve
 * (less values in the middle range, more values in more extreme range)
 * TODO: Make middle of adjustment curve based on average pixel brightness. (Pre-pass: Histogram)
 */
public final class Contrast implements Filter<BufferedImage> {
	private static final PrePass<BufferedImage> histogramPrePass = (source, mask, strength) -> {
		double avgRed = 0;
		double avgGreen = 0;
		double avgBlue = 0;
		double avgAlpha = 0;
		int pixelCount = 0;
		for (int x = 0; x < source.getWidth(); x++) {
			for (int y = 0; y < source.getHeight(); y++) {
				int argb = source.getRGB(x, y);
				avgRed += getRed(argb);
				avgGreen += getGreen(argb);
				avgBlue += getBlue(argb);
				avgAlpha += getAlpha(argb);
				pixelCount++;
			}
		}
		avgRed /= pixelCount;
		avgGreen /= pixelCount;
		avgBlue /= pixelCount;
		avgAlpha /= pixelCount;
		
		System.out.println("[Histogram Pre-Pass] RGBA avg.: "
				+ avgRed + ", " + avgGreen + ", " + avgBlue + ", " + avgAlpha);
		
		return Arrays.asList(avgRed, avgGreen, avgBlue, avgAlpha);
	};
	private static final List<PrePass<BufferedImage>> prePasses =
			Arrays.asList(histogramPrePass);
	
	private static final List<PixelTransformer<BufferedImage>> mainPasses = Arrays.asList(
			(x, y, argb, prePassData, source, mask, strength) -> {
				int newRed = 0, newGreen = 0, newBlue = 0, newAlpha = 0;
				
				for (int i = 0; i < 4; i++) {
					int channelColor = 0;
					double channelAvg = 0;
					switch (i) {
					case 0: channelColor = getRed(argb); break;
					case 1: channelColor = getGreen(argb); break;
					case 2: channelColor = getBlue(argb); break;
					case 3: channelColor = getAlpha(argb); break;
					}
					
					// Transform old value into new value
					
					
					// Higher invDeviance ==> lower contrast
					int newChannelColor = (int) (channelColor *
							gammaTransform((double)channelColor / 255, strength));
					newChannelColor = clamp0255(newChannelColor);
					
					switch (i) {
					case 0: newRed = newChannelColor; break;
					case 1: newGreen = newChannelColor; break;
					case 2: newBlue = newChannelColor; break;
					case 3: newAlpha = newChannelColor; break;
					}
				}
				
				return toARGB(newRed, newGreen, newBlue, newAlpha);
			}
		);
	
	private static double gammaTransform(double in, double s) {
		return (in * Math.pow(Math.E, s*in)) / Math.pow(Math.E, s);
	}
	
	@Override
	public List<PrePass<BufferedImage>> getPrePasses() {
		return prePasses;
	}
	
	@Override
	public List<PixelTransformer<BufferedImage>> getMainPassTransformers() {
		return mainPasses;
	}
	
	@Override
	public String getName() {
		return "Contrast";
	}
}
