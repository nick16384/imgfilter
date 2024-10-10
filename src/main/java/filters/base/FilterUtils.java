package filters.base;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Random;

/**
 * Note to dev: Move content or at least rename filter utils.
 */
@Deprecated
public class FilterUtils {
	
	/**
	 * Scales a BufferedImage up / down to the desired width and height.
	 * Ideally, scales to the same size as the original image. However, in case of
	 * rounding errors:
	 * Guarantees, that the resulting image is at least as big as the original one.
	 * Cannot make sure that the resulting image is at least as small as the original one.
	 * Source:
	 * https://stackoverflow.com/questions/4216123/how-to-scale-a-bufferedimage
	 */
	public static <T extends BufferedImage> T scaleImageToSize(T original, int width, int height) {
		if (original == null)
			return null;
		BufferedImage after = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		AffineTransform transform = new AffineTransform();
		double scaleWidth = ((double)width / original.getWidth());
		double scaleHeight = ((double)height / original.getHeight());
		// + 1 guarantees minimum size to be the original size.
		if (original.getWidth() * scaleWidth < width)
			scaleWidth = (width + 1.0) / original.getWidth();
		if (original.getHeight() * scaleHeight < height)
			scaleHeight = (height + 1.0) / original.getHeight();
		transform.scale(scaleWidth, scaleHeight);
		AffineTransformOp scaleOp = 
		   new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
		return (T) scaleOp.filter(original, after);
	}
	
	public static <T extends BufferedImage> T deepCopy(T in) {
		if (in == null)
			return null;
		ColorModel cm = in.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = in.copyData(null);
		return (T) new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	
	// Int: 8 bytes alpha, 8 bytes red, 8 bytes green, 8 bytes blue
	// Bit masking for removing bytes from other channels
	// Bit shifting for moving bytes into the right position
	public static int toARGB(int red, int green, int blue, int alpha) {
		int withAlpha = (0x000000FF & alpha) << 24;
		int withRed = (0x000000FF & red) << 16;
		int withGreen = (0x000000FF & green) << 8;
		int withBlue = (0x000000FF & blue);
		// "|" operator must be used instead of "+". Might find out later why.
		return withAlpha | withRed | withGreen | withBlue;
	}
	public static int getAlpha(int argb) {
		return (0xFF000000 & argb) >> 24;
	}
	public static int getRed(int argb) {
		return (0x00FF0000 & argb) >> 16;
	}
	public static int getGreen(int argb) {
		return (0x0000FF00 & argb) >> 8;
	}
	public static int getBlue(int argb) {
		return (0x000000FF & argb) >> 0;
	}
	
	// Helper functions for filters
	public static int clamp(int num, int min, int max) {
		return Math.max(min, Math.min(max, num));
	}
	/**
	 * Clamps the input value between 0 and 255.
	 * Useful for color channels (values range from 0x00 to 0xFF)
	 */
	public static int clamp0255(int num) {
		return clamp(num, 0, 255);
	}
	public static int randomInt(int max) {
		return (int)(((double)new Random().nextInt() / Integer.MAX_VALUE) * max);
	}
}
