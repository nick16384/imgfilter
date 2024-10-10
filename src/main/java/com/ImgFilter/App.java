package com.ImgFilter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;


public class App {
	public static final boolean LOG_DEBUG_PIXEL_TRANSFORM = false;
	public static final int ARGS_INDEX_INPUT_IMAGE_FILE = 0;
	public static final int ARGS_INDEX_OUTPUT_IMAGE_FILE = 2;
	public static final int ARGS_INDEX_MASK_IMAGE_FILE = 1;
	
	public static final String ARG_HELP = "--help";
	public static final String ARG_DISABLE_MASK = "--disable-mask";
	private static boolean isMaskDisabled = false;
	private static boolean saveOnExit = false;
	
	private static String[] argsMain;
	public static String[] getArgs() {
		return argsMain;
	}
	
	protected static ImgFilterFrontend filterFrontend;
	
	// TODO: Check undo / redo with cancel
	// TODO: Add support for 16-bit images (new constant for lowest/highest channel values)
	// TODO: 16-bit support with long instead of int?
	// TODO: Replace pre-mask with post mask: original [mask_op] mask = output
	// TODO: Check saving of 16-bit files
	
	public static void main(String[] args) {
		argsMain = args;
		if (args.length == 1 && args[0].equals(ARG_HELP)) {
			System.out.println("Usage: [ThisFile].jar <Args>\n"
					+ "Usage 2: [ThisFile].jar <ImageIn> <MaskIn / --disable-mask> <ImageOut>.bmp"
					+ "--help \t Get this help\n"
					+ "--disable-mask \t Disable masking (use null mask); Has to be the 2nd argument.");
			System.exit(0);
		}
		if (args.length < 3 || args.length > 3) {
			System.err.println("Expected exactly 3 arguments: Input, Mask, Output.bmp");
			System.out.println("Use \"--help\" for help.");
			System.exit(1);
		}
		if (Arrays.asList(args).contains(ARG_DISABLE_MASK))
			isMaskDisabled = true;
		
		importImage(args[ARGS_INDEX_INPUT_IMAGE_FILE], args[ARGS_INDEX_MASK_IMAGE_FILE]);
		
		new Thread(() -> {
			GUIHelper.launchGUI();
			System.out.println("Exiting...");
			// System.exit() call required, as some thread running
			// in the background are not daemons yet.
			System.exit(0);
		}).start();
	}
    
    public static void importImage(String imagePath, String maskPath) {
    	File imgFile = new File(imagePath);
		System.out.println("Using image path: " + imgFile.getAbsolutePath());
		if (!imgFile.exists()) {
			System.err.println("Input image file does not exist.");
			System.exit(1);
		}
		
		System.out.println("Image file: " + imagePath + ", Mask file: " + maskPath);
		
		System.out.println("Attempting to parse image...");
		try {
			filterFrontend = new ImgFilterFrontend(imgFile);
			importMaskImage(maskPath);
		} catch (IOException ioe) {
			System.err.println("Error parsing image");
			ioe.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Image width: " + filterFrontend.imgWidth);
		System.out.println("Image height: " + filterFrontend.imgHeight);
    }
    
    public static void importMaskImage(String maskPath) throws IOException {
    	if (maskPath == null || maskPath.isBlank() || isMaskDisabled) {
    		System.err.println("Not using mask file: No mask file supplied");
    		return;
    	}
    	File maskFile = new File(maskPath);
    	filterFrontend.importMaskFile(maskFile);
    }
    
    public static boolean isMaskDisabled() {
    	return isMaskDisabled;
    }
    public static void setMaskDisabled(boolean newValue) {
    	isMaskDisabled = newValue;
    }
    public static boolean isSaveOnExit() {
    	return saveOnExit;
    }
    public static void setSaveOnExit(boolean newSaveOnExit) {
    	saveOnExit = newSaveOnExit;
    }
}