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
					new int[] {32, 32, 32},
					false,
					false,
					ColorModel.OPAQUE,
					DataBuffer.TYPE_INT);
	/**
	 * Highest possible value for pixel channel values (e.g. 511 for 8 bits per channel).
	 * Note that the usual signed int of Java is treated like an unsigned int here.
	 */
	public static final int MAX_SAMPLE_VALUE = 0xFFFFFFFF;
	public static final long MAX_SAMPLE_VALUE_ULONG = Integer.toUnsignedLong(MAX_SAMPLE_VALUE);
	/**
	 * Lowest possible value for pixel channel values (e.g. 0 for 8 bits per channel).
	 * Note that the usual signed int of Java is treated like an unsigned int here.
	 */
	public static final int MIN_SAMPLE_VALUE = 0x00000000;
	public static final long MIN_SAMPLE_VALUE_ULONG = Integer.toUnsignedLong(MIN_SAMPLE_VALUE);
	public static final int INDEX_SAMPLE_RED = 0;
	public static final int INDEX_SAMPLE_GREEN = 1;
	public static final int INDEX_SAMPLE_BLUE = 2;
	
	public ImageRaster(BufferedImage fromImage) {
		this(convertToCompatibleColorModel(fromImage, DEFAULT_COLOR_MODEL).getRaster());
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
	public long getRedAt_UL(int x, int y) {
		return Integer.toUnsignedLong(getRedAt(x, y));
	}
	public int getGreenAt(int x, int y) {
		return getPixelRGBAt(x, y)[INDEX_SAMPLE_GREEN];
	}
	public long getGreenAt_UL(int x, int y) {
		return Integer.toUnsignedLong(getGreenAt(x, y));
	}
	public int getBlueAt(int x, int y) {
		return getPixelRGBAt(x, y)[INDEX_SAMPLE_BLUE];
	}
	public long getBlueAt_UL(int x, int y) {
		return Integer.toUnsignedLong(getBlueAt(x, y));
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
		System.out.println("SW: " + scaleWidth + ", SH: " + scaleHeight);
		if (scaleWidth == 1.0 && scaleHeight == 1.0)
			return this.createDeepCopy();
		
		transform.scale(scaleWidth, scaleHeight);
		AffineTransformOp scaleOp = 
				new AffineTransformOp(transform, transformOp);
		return new ImageRaster(scaleOp.filter(this, null));
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
	public static BufferedImage convertToCompatibleColorModel(BufferedImage source, ColorModel newModel) {
		int w = source.getWidth();
		int h = source.getHeight();
		
		BufferedImage result = new BufferedImage(
				newModel,
				newModel.createCompatibleWritableRaster(w, h),
				false,
				null);
		/*Graphics2D g = result.createGraphics();
		g.drawRenderedImage(source, new AffineTransform()); //or some other drawImage function
		
		g.dispose();*/
		// Manual copy, method above does not work
		int numSourceComponents = source.getColorModel().getNumComponents();
		int numDestComponents = newModel.getNumComponents();
		int sourceBitsPerChannel =
				source.getColorModel().getPixelSize() / numSourceComponents;
		int destBitsPerChannel = 
				newModel.getPixelSize() / numDestComponents;
		int bitsPerChannelDifference =
				destBitsPerChannel - sourceBitsPerChannel;
		System.out.println("Copy components: " + numSourceComponents + " -> " + numDestComponents);
		System.out.println("Copy bits/channel: " + sourceBitsPerChannel + " -> " + destBitsPerChannel);
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int[] sourcePixel = source.getRaster().getPixel(x, y, new int[numSourceComponents]);
				int[] sourcePixelConverted = new int[numDestComponents];
				
				// Iterate over each color component (usually red, green and blue)
				for (int i = 0; i < numDestComponents; i++) {
					int sourceSample = sourcePixel[i];
					sourceSample *= (int)Math.pow(2, bitsPerChannelDifference);
					sourcePixelConverted[i] = sourceSample;
				}
				
				//result.getRaster().setPixel(x, y, new int[] {0x00000001, 0x90000000, 0x8000000});
				result.getRaster().setPixel(x, y, sourcePixelConverted);
			}
		}

		return result;
	}
}
