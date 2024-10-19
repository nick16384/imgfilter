package filters.base;

import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

public class ImageRaster extends WritableRaster {
	/**
	 * Default color model with linear RGB space, no alpha and 32 bits per channel. (96 bits per pixel)
	 */
	public static final ColorModel DEFAULT_COLOR_MODEL =
			new ComponentColorModel(
					ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB),
					null/*new int[] {32, 32, 32}*/,
					false,
					false,
					ColorModel.OPAQUE,
					DataBuffer.TYPE_INT);
	/**
	 * Highest possible value for pixel channel values (e.g. 255 for 8 bits per channel).
	 * Note that the use of an unsigned integer would be much more appropriate here, but
	 * since java doesn't implement this feature, there would be a lot of headaches when treating
	 * ints as uints, so this implementation sticks with Integer.MIN_VALUE as minimum sample
	 * value and Integer.MAX_VALUE as maximum value.
	 */
	public static final int MAX_SAMPLE_VALUE = 0x7FFFFFFF;
	/**
	 * Lowest possible value for pixel channel values (e.g. -256 for 8 bits per channel).
	 * Note that the use of an unsigned integer would be much more appropriate here, but
	 * since java doesn't implement this feature, there would be a lot of headaches when treating
	 * ints as uints, so this implementation sticks with Integer.MIN_VALUE as minimum sample
	 * value and Integer.MAX_VALUE as maximum value.
	 */
	public static final int MIN_SAMPLE_VALUE = 0xFFFFFFFF;
	public static final int INDEX_SAMPLE_RED = 0;
	public static final int INDEX_SAMPLE_GREEN = 1;
	public static final int INDEX_SAMPLE_BLUE = 2;
	
	public ImageRaster(BufferedImage fromImage) {
		this(createCompatibleImage(fromImage).getRaster());
	}
	
	private ImageRaster(WritableRaster fromRaster) {
		super(fromRaster.getSampleModel(),
				fromRaster.getDataBuffer(),
				new Point(0, 0));
	}
	
	public ImageRaster(int width, int height) {
		this(new BufferedImage(
				DEFAULT_COLOR_MODEL,
				DEFAULT_COLOR_MODEL.createCompatibleWritableRaster(width, height),
				false,
				null));
	}
	
	public int[] getPixelRGBAt(int x, int y) {
		return this.getPixel(x, y, new int[3]);
	}
	public int getRedAt(int x, int y) {
		// TODO: Maybe use getSample(x, y, band)
		return getPixelRGBAt(x, y)[INDEX_SAMPLE_RED];
	}
	public int getGreenAt(int x, int y) {
		return getPixelRGBAt(x, y)[INDEX_SAMPLE_GREEN];
	}
	public int getBlueAt(int x, int y) {
		return getPixelRGBAt(x, y)[INDEX_SAMPLE_BLUE];
	}
	
	public void setPixelRGBAt(int x, int y, int[] newPixelData) {
		this.setPixel(x, y, newPixelData);
	}
	public void setRedAt(int x, int y, int newValue) {
		int[] pixelValues = getPixelRGBAt(x, y);
		pixelValues[INDEX_SAMPLE_RED] = newValue;
		setPixelRGBAt(x, y, pixelValues);
	}
	public void setGreenAt(int x, int y, int newValue) {
		int[] pixelValues = getPixelRGBAt(x, y);
		pixelValues[INDEX_SAMPLE_GREEN] = newValue;
		setPixelRGBAt(x, y, pixelValues);
	}
	public void setBlueAt(int x, int y, int newValue) {
		int[] pixelValues = getPixelRGBAt(x, y);
		pixelValues[INDEX_SAMPLE_BLUE] = newValue;
		setPixelRGBAt(x, y, pixelValues);
	}
	
	public ImageRaster createDeepCopy() {
		final WritableRaster copiedRaster =
				new BufferedImage(DEFAULT_COLOR_MODEL,
				this, false, null)
				.copyData(null);
		
		return new ImageRaster(copiedRaster);
	}
	
	/**
	 * Returns a scaled variant of the current raster using the default
	 * rescaling operation {@code AffineTransformOp.TYPE_BILINEAR}.
	 * See {@code createRescaledRaster(targetWidth, targetHeight, transformOp)}
	 * for more info about rescaling.
	 * @param targetWidth
	 * @param targetHeight
	 * @return
	 */
	public ImageRaster createRescaledRaster(int targetWidth, int targetHeight) {
		return this.createRescaledRaster(targetWidth, targetHeight, AffineTransformOp.TYPE_BILINEAR);
	}
	
	/**
	 * Scales a this raster up / down to the desired width and height.
	 * Ideally, scales to the same size as the original raster. However, in case of
	 * rounding errors:
	 * Guarantees, that the resulting raster is at least as big as the original one.
	 * Cannot make sure that the resulting raster is at least as small as the original one.
	 * Source (modified):
	 * https://stackoverflow.com/questions/4216123/how-to-scale-a-bufferedimage
	 */
	public ImageRaster createRescaledRaster(int targetWidth, int targetHeight, int transformOp) {
		AffineTransform transform = new AffineTransform();
		double scaleWidth = ((double)targetWidth / this.getWidth());
		double scaleHeight = ((double)targetHeight / this.getHeight());
		// + 1 guarantees minimum size to be the original size.
		if (this.getWidth() * scaleWidth < targetWidth)
			scaleWidth = (targetWidth + 1.0) / this.getWidth();
		if (this.getHeight() * scaleHeight < targetHeight)
			scaleHeight = (targetHeight + 1.0) / this.getHeight();
		transform.scale(scaleWidth, scaleHeight);
		AffineTransformOp scaleOp = 
				new AffineTransformOp(transform, transformOp);
		ImageRaster rescaledRaster = new ImageRaster(targetWidth, targetHeight);
		scaleOp.filter(this, rescaledRaster);
		return rescaledRaster;
	}
	
	public BufferedImage toBufferedImage() {
		return new BufferedImage(DEFAULT_COLOR_MODEL, this, false, null);
	}
	
	/**
	 * Copies pixel data from one image to another with a different ColorModel.
	 * @param source
	 * @return
	 */
	// TODO: Add multithreading
	// TODO: Move this method to a more appropriate place.
	public static BufferedImage createCompatibleImage(BufferedImage source) {
		int w = source.getWidth();
		int h = source.getHeight();
		
		BufferedImage result = new BufferedImage(
				DEFAULT_COLOR_MODEL,
				DEFAULT_COLOR_MODEL.createCompatibleWritableRaster(w, h),
				false,
				null);
		/*Graphics2D g = result.createGraphics();
		g.drawRenderedImage(source, new AffineTransform()); //or some other drawImage function
		
		g.dispose();*/
		// Manual copy, method above does not work
		int numSourceComponents = source.getColorModel().getNumComponents();
		System.out.println("Copy source components: " + numSourceComponents);
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int[] sourcePixel = source.getRaster().getPixel(x, y, new int[numSourceComponents]);
				int[] sourcePixelConverted = new int[numSourceComponents];
				
				long maxSample8Bit = 65536;
				long maxSample32Bit = MAX_SAMPLE_VALUE * 2;
				// Iterate over each color component (usually red, green and blue)
				for (int i = 0; i < numSourceComponents; i++) {
					long sourceSampleUnsigned = sourcePixel[i] + (Integer.MAX_VALUE / 2);
					double sampleFrac = (double)sourceSampleUnsigned / (double)maxSample8Bit;
					long sourceSampleConvertedUnsigned = (long)(maxSample32Bit * sampleFrac);
					sourceSampleConvertedUnsigned -= (Integer.MAX_VALUE / 2);
					int sourceSampleConverted = (int)sourceSampleConvertedUnsigned;
					
					double wFrac = (double)x / w;
					sourcePixelConverted[i] = (int)(wFrac * 0x00FFFFFF);
				}
				
				//result.getRaster().setPixel(x, y, new int[] {0x00000001, 0x90000000, 0x8000000});
				result.getRaster().setPixel(x, y, sourcePixelConverted);
			}
		}

		return result;
	}
}
