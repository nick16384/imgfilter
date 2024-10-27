package filters.base;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public interface Filter<RasterType extends ImageRaster> {
	public default List<PrePass<RasterType>> getPrePasses() {
		return new ArrayList<>();
	}
	
	public default List<PixelTransformer<RasterType>> getMainPassTransformers() {
		return new ArrayList<>();
	}
	
	public String getName();
	
	// Below are some helper functions for filters
	// They have no specific use for this class itself.

	static int randomInt(int max) {
		return (int)(((double)new Random().nextInt() / Integer.MAX_VALUE) * max);
	}
	
	static int clamp_signed(int num, int min, int max) {
		return Math.max(min, Math.min(max, num));
	}
	static long clamp_signed(long num, long min, long max) {
		return Math.max(min, Math.min(max, num));
	}
	
	static int clamp_unsigned(int num, int min, int max) {
		if (UInt.UINT_COMPARATOR.compare(num, min) < 0) return min;
		else if (UInt.UINT_COMPARATOR.compare(num, max) > 0) return max;
		else return num;
	}
	static long clamp_unsigned(long num, long min, long max) {
	    if (UInt.ULONG_COMPARATOR.compare(num, min) < 0) return min;
	    else if (UInt.ULONG_COMPARATOR.compare(num, max) > 0) return max;
	    else return num;
	}
	
	// Code & information source:
	// https://www.geeksforgeeks.org/program-change-rgb-color-model-hsv-color-model/
	/**
	 * Returns an array with the color components converted to HSV/HSB.
	 * @param Input RGB array in the usual format used for filters.
	 * @return a double array with corresponding HSV values (each between 0.0 and 1.0)
	 */
	public static double[] RGBtoHSV(int[] rgb) {
		// Change RGB values to a double between 0.0 and 1.0
        double r = (double)Integer.toUnsignedLong(rgb[0]) / (double)ImageRaster.MAX_SAMPLE_VALUE_ULONG; 
        double g = (double)Integer.toUnsignedLong(rgb[1]) / (double)ImageRaster.MAX_SAMPLE_VALUE_ULONG; 
        double b = (double)Integer.toUnsignedLong(rgb[2]) / (double)ImageRaster.MAX_SAMPLE_VALUE_ULONG;
        
        // Maximum of r,g,b
        double cmax = Math.max(r, Math.max(g, b));
        // Minimum of r,g,b
        double cmin = Math.min(r, Math.min(g, b));
        double diff = cmax - cmin;
        double h = -1, s = -1;
        
        if (cmax == cmin) 
            h = 0;
        
        else if (cmax == r) 
            h = (60 * ((g - b) / diff) + 360) % 360;
        
        else if (cmax == g) 
            h = (60 * ((b - r) / diff) + 120) % 360; 
        
        else if (cmax == b) 
            h = (60 * ((r - g) / diff) + 240) % 360;
        
        // Division necessary to keep hue between 0.0 and 1.0
        h /= 360;
        
        
        if (cmax == 0) 
            s = 0;
        
        else
            s = (diff / cmax);
        
        double v = cmax; 
        
        return new double[] { h,s,v };
	}
	// Code source:
	// https://stackoverflow.com/questions/7896280/converting-from-hsv-hsb-in-java-to-rgb-without-using-java-awt-color-disallowe
	/**
	 * Converts HSV back to RGB. For more info see: {@code RGBtoHSV()}
	 */
	public static int[] HSVtoRGB(double[] hsv) {
		double hue = hsv[0];
		double saturation = hsv[1];
		double value = hsv[2];
		
		int h = (int)(hue * 6);
	    double f = hue * 6 - h;
	    double p = value * (1 - saturation);
	    double q = value * (1 - f * saturation);
	    double t = value * (1 - (1 - f) * saturation);
	    
	    double red = 0, green = 0, blue = 0;

	    switch (h) {
	      case 0: red = value; green = t; blue = p; break;
	      case 1: red = q; green = value; blue = p; break;
	      case 2: red = p; green = value; blue = t; break;
	      case 3: red = p; green = q; blue = value; break;
	      case 4: red = t; green = p; blue = value; break;
	      case 5: red = value; green = p; blue = q; break;
	      default:
	    	  System.err.println("Error during HSV to RGB conversion; Input was " + hue + ", " + saturation + ", " + value);
	    	  throw new RuntimeException("Error during conversion from HSV to RGB. See initial values above.");
	    }
	    
	    int redInt = UInt.cast_ulong_uint((long)(red * (double)ImageRaster.MAX_SAMPLE_VALUE_ULONG));
	    int greenInt = UInt.cast_ulong_uint((long)(green * (double)ImageRaster.MAX_SAMPLE_VALUE_ULONG));
	    int blueInt = UInt.cast_ulong_uint((long)(blue * (double)ImageRaster.MAX_SAMPLE_VALUE_ULONG));
	    
	    return new int[] { redInt, greenInt, blueInt };
	}
	
	public static int[] packPixelData(int red, int green, int blue) {
		return new int[] { red, green, blue };
	}
}
