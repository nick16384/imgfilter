package com.ImgFilter;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import filters.FiltersList;
import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.MultiPassFilterApplicator;
import filters.base.RGBChannel;

import static java.util.Map.entry;

/**
 * Note: Errors on linux might be fixed by additionally checking
 * file.getCanonicalFile() variants.
 */

public class ImgFilterFrontend {
	public static final ColorModel WRITE_OUT_COLOR_MODEL_24BIT =
			ColorModel.getRGBdefault();
			/*new ComponentColorModel(
					ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB),
					new int[] {8, 8, 8},
					false,
					false,
					ColorModel.OPAQUE,
					DataBuffer.TYPE_INT);*/
	public static final ColorModel WRITE_OUT_COLOR_MODEL_48BIT =
			new ComponentColorModel(
					ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB),
					new int[] {16, 16, 16},
					false,
					false,
					ColorModel.OPAQUE,
					DataBuffer.TYPE_INT);
	public static final ColorModel WRITE_OUT_COLOR_MODEL_72BIT =
			new ComponentColorModel(
					ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB),
					new int[] {24, 24, 24},
					false,
					false,
					ColorModel.OPAQUE,
					DataBuffer.TYPE_INT);
	public static final HashMap<ColorModel, String> AVAILABLE_WRITE_OUT_COLOR_MODEL_STRING_MAP =
			new HashMap<>(
					Map.ofEntries(
							entry(WRITE_OUT_COLOR_MODEL_24BIT, "24 bits per pixel"),
							entry(WRITE_OUT_COLOR_MODEL_48BIT, "[WIP] 48 bits per pixel"),
							entry(WRITE_OUT_COLOR_MODEL_72BIT, "[WIP] 72 bits per pixel"))
					);
	
	Filter<ImageRaster> currentFilter;
	private final File imgFile;
	private File maskFile;
	// Two images act as a double buffer:
	// Only the copy is being edited, while the live version is being read from as input.
	// This prevents new pixels affecting other ones (e.g. in the blur filter)
	private BufferedImage image;
	private List<BufferedImage> previousImages = new ArrayList<>();
	private BufferedImage mask;
	private int historyIndex = 0;
	public final int imgHeight;
	public final int imgWidth;
	
	/**
	 * Import an image file to a filter.
	 * Supported file extensions: .bmp, .jpg, .jpeg, .png
	 * @param imgIn Image
	 * @param mask Masking image used for some filters. Can be null without affecting other filters.
	 * A null mask will be interpreted as a black image.
	 * @throws IOException
	 */
	public ImgFilterFrontend(File imgIn, File maskIn) throws IOException {
		System.out.println("Input file: " + imgIn.getAbsolutePath());
		if (!imgIn.exists() || !imgIn.isFile())
			throw new IOException("Input file does not exist.");
		
		if (!ImageFileExtension.isValidImageFileExtension(imgIn))
			throw new IOException("Input file is not an image file. (File name suffix)");
		
		this.imgFile = imgIn;
		BufferedImage maybeIncompatibleImage = ImageIO.read(imgIn);
		this.image =
				ImageRaster.convertToCompatibleColorModel(
						maybeIncompatibleImage, ImageRaster.DEFAULT_COLOR_MODEL, true);
		
		imgHeight = image.getHeight();
		imgWidth = image.getWidth();
		System.out.println("CMIN: " + maybeIncompatibleImage.getColorModel());
		System.out.println("CM  : " + image.getColorModel());
		
		// Empty BufferedImage with 16-bit depth per pixel sample (48 bit per pixel)
		/*BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		ComponentColorModel colorModel = new ComponentColorModel(
		        ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB), false, false,
		        Transparency.OPAQUE, DataBuffer.TYPE_INT); // Maybe use TYPE_DOUBLE
		boolean isAlphaPremultiplied = emptyImage.isAlphaPremultiplied();
		WritableRaster raster = colorModel.createCompatibleWritableRaster(imgWidth, imgHeight);

		this.image = new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);*/
		
		importMaskFile(maskIn);
		
		previousImages.add(new ImageRaster(image).createDeepCopy().toBufferedImage());
	}
	/**
	 * See javadoc for {@code ImgFilter(imgIn, maskIn)}
	 */
	public ImgFilterFrontend(File imgIn) throws IOException {
		this(imgIn, null);
	}
	
	public void applyFilter(Filter<ImageRaster> filter,
		Filter<ImageRaster> maskOp,
		RGBChannel channel, double strength, int executorThreads) {
		// Remove every image after the current history index, since the timeline is going to be changed.
		for (int i = historyIndex + 1; i < previousImages.size(); i++)
			previousImages.remove(i);
				
		currentFilter = filter;
				
		GUIHelper.setActionsCount(historyIndex, previousImages.size() - (historyIndex + 1));
		System.out.println("Using filter type " + FiltersList.toString(filter));
		
		// Clamp strength value between 0.0 and 1.0
		strength = Math.max(0.0, Math.min(1.0, strength));
		
		Thread showProgressThread = enableShowProgressThread();
		
		try {
			BufferedImage newImage;
			ImageRaster imageRaster = new ImageRaster(image);
			ImageRaster maskRaster = new ImageRaster(mask);
					if (maskOp == null)
						newImage = MultiPassFilterApplicator.applyFilter(
								currentFilter, imageRaster, maskRaster, channel, strength, executorThreads)
								.toBufferedImage();
					else
						newImage = MultiPassFilterApplicator.applyFilter(
								currentFilter, imageRaster, maskRaster, maskOp, channel, strength, executorThreads)
								.toBufferedImage();
			image = newImage;
		} catch (RuntimeException re) {
			System.err.println("Non-caught error running filter. Stacktrace below:");
			re.printStackTrace();
		}
		
		previousImages.add(new ImageRaster(image).createDeepCopy().toBufferedImage());
		historyIndex = previousImages.size() - 1;
		showProgressThread.interrupt();
	}
	
	/**
	 * Applies a filter to the previously supplied image.
	 * @param filter The filter to use
	 * @param strength Filter strength (1.0 is max, 0.0 is none)
	 * @apiNote Filter strength setting is not effective on all filter types.
	 */
	public void applyFilter(Filter<ImageRaster> filter,
			RGBChannel channel, double strength,
			int executorThreads) {
		applyFilter(filter, null, channel, strength, executorThreads);
	}
	
	private Thread enableShowProgressThread() {
		Thread showProgressThread = new Thread(() -> {
			long lastLogTime = System.currentTimeMillis() - 1000;
			// Wait for filter applicator to start. When this thread gets interrupted before
			// actual logging happens, it means that the filter finished before any
			// logging occurred.
			boolean isFilterAlreadyFinished = false;
			while (!isProcessorRunning())
				try { Thread.sleep(50); } catch (InterruptedException ie) { isFilterAlreadyFinished = true; }
			
			while (isProcessorRunning() && !isFilterAlreadyFinished) {
				// Want logs to be displayed less often than the status bar to be updated.
				if (System.currentTimeMillis() - lastLogTime > 1000) {
					System.out.println("[" + System.currentTimeMillis()
					+ "] Group: " + MultiPassFilterApplicator.getCurrentPassGroup() + ", Pass: "
					+ MultiPassFilterApplicator.getCurrentPassNum() + "/"
					+ MultiPassFilterApplicator.getMaxPassesForCurrentGroup()
					+ ", Progress: " + (MultiPassFilterApplicator.getCurrentPassProgress() * 100) + "%");
					lastLogTime = System.currentTimeMillis();
				}
				GUIHelper.setFilterProgress(MultiPassFilterApplicator.getCurrentPassGroup(),
						MultiPassFilterApplicator.getCurrentPassNum(),
						MultiPassFilterApplicator.getMaxPassesForCurrentGroup(),
						MultiPassFilterApplicator.getCurrentPassProgress() * 100);
				try {
					Thread.sleep(200);
				} catch (InterruptedException ie) {
					break;
				}
			}
			GUIHelper.setFilterProgress(MultiPassFilterApplicator.getCurrentPassGroup(),
					MultiPassFilterApplicator.getCurrentPassNum(),
					MultiPassFilterApplicator.getMaxPassesForCurrentGroup(),
					MultiPassFilterApplicator.getCurrentPassProgress() * 100);
		});
		// If, for whatever reason this thread still runs after all processing is done
		// setting it as daemon will allow the JVM to shut down anyways without having to
		// wait indefinitely for this thread to stop.
		showProgressThread.setDaemon(true);
		showProgressThread.start();
		return showProgressThread;
	}
	
	// FIXME: Undo not working properly:
	// Apply filter twice
	// Undo once
	// Apply filter once
	// Undo:
	// Expected: Goes back one image
	// Actual: Stays at same image, second undo reverts two images
	public void undoLastAction() {
		System.out.println("Reverting last filter action. Left: " + historyIndex);
		if (historyIndex <= 0) {
			System.err.println("Cannot undo: No action done yet.");
			return;
		}
		historyIndex--;
		image = previousImages.get(historyIndex);
		GUIHelper.setActionsCount(historyIndex, previousImages.size() - (historyIndex + 1));
	}
	public void redoLastAction() {
		System.out.println("Redoing last undone action.");
		System.out.println("Size: " + previousImages.size() + ", Idx: " + historyIndex);
		if (historyIndex + 1 > previousImages.size() - 1) {
			System.err.println("Cannot redo: Already at latest state.");
			return;
		}
		historyIndex++;
		image = previousImages.get(historyIndex);
		GUIHelper.setActionsCount(historyIndex, previousImages.size() - (historyIndex + 1));
	}
	
	public void saveFileToFolder(File outFolder, ColorModel writeOutColorModel, ImageFileExtension extension) throws IOException {
		// Code for not overwriting files but instead finding a usable file name by appending
		// ascending numbers at the end of the file name.
		// Mostly obsolete when using file chooser GUI, because it asks for overwriting files.
		/*System.out.println("Attempting to find usable file name...");
		File outNew = null;
		for (int i = 1; outNew == null || outNew.exists(); i++) {
			String outNewPath = out.getAbsolutePath().substring(0, out.getAbsolutePath().length() - 4);
			outNewPath += "(" + i + ").bmp";
			outNew = new File(outNewPath);
		}*/
		
		System.out.println("Saving to folder: " + outFolder.getAbsolutePath());
		System.out.println("Internal file name: " + imgFile.getName());
		System.out.println("Using file extension: " + extension);
		
		// File system file separator string
		String fsep = FileSystems.getDefault().getSeparator();
		String outFileStr = outFolder.getAbsolutePath();
		if (!outFileStr.endsWith(fsep))
			outFileStr += fsep;
		outFileStr += imgFile.getName() + "_out";
		outFileStr += extension.getAllFileExtensions()[0];
		System.out.println("Saving to file: " + outFileStr);
		
		File outFile = new File(outFileStr);
		
		System.out.println("Creating empty file.");
		outFile.createNewFile();
		System.out.println("Created new empty file.");
		
		// TODO: Try writing with different color model (24 bit, 16 bit or 8 bit depending on user)
		System.out.println("Converting color models.");
		BufferedImage imageWithWriteOutColorModel =
				ImageRaster.convertToCompatibleColorModel(image, writeOutColorModel, false);
		System.out.println("Writing image.");
		
		System.out.println("Using extension \"" + extension.getAllFileExtensions()[0].substring(1) + "\"");
		
		boolean isWriteSuccessful = ImageIO.write(
						imageWithWriteOutColorModel,
						extension.getAllFileExtensions()[0].substring(1),
						outFile);
		if (!isWriteSuccessful)
			System.err.println("No appropriate image writer found.");
		else
			System.out.println("Saved.");
	}
	
	public File getImageFile() {
		return imgFile;
	}
	public File getMaskImageFile() {
		return maskFile;
	}
	public BufferedImage getMaskImage() {
		return mask;
	}
	
	/**
	 * Imports an image mask from an image file.
	 * If mask is null, a black image will be used.
	 * Note that the mask will be scaled automatically to the image being applied on.
	 * @param maskIn Mask image file
	 * @throws IOException If the image read operation still fails after basic file checks.
	 */
	public void importMaskFile(File maskIn) throws IOException {
		boolean useNullMask = false;
		if (maskIn == null)
			useNullMask = true;
		else if (maskIn != null && (!maskIn.exists() || !maskIn.isFile())) {
			System.err.println("Warning: Mask file does not exist. Using null mask.");
			useNullMask = true;
		}
		else if (maskIn != null && !ImageFileExtension.isValidImageFileExtension(maskIn)) {
			System.err.println("Warning: Mask file is not an image file. (File name suffix). Using null mask.");
			useNullMask = true;
		}
		
		this.maskFile = maskIn;
		if (useNullMask)
			this.mask = new ImageRaster(imgWidth, imgHeight).toBufferedImage();
		else
			this.mask = ImageIO.read(maskIn);
	}
	
	/**
	 * Returns the latest image version, being the one that is live edited.
	 * @return
	 */
	public final BufferedImage getLiveImage() {
		// Return internal image if...
		// 1. the filter is null (nothing has been applied yet)
		// 2. The live image from the filter is null (filter was initialized, but nothing was applied yet.)
		// 3. The current history index is before the current image's one (undo was requested)
		if (currentFilter == null || MultiPassFilterApplicator.getLiveImage() == null
				|| historyIndex < previousImages.size() - 1)
			return image;
		return MultiPassFilterApplicator.getLiveImage();
	}
	
	public boolean isProcessorRunning() {
		return MultiPassFilterApplicator.isAutoFilterRunning();
	}
	
	public void cancelProcessing() {
		MultiPassFilterApplicator.cancelProcessing();
	}
}
