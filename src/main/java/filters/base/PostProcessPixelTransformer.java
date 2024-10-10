package filters.base;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;

@FunctionalInterface
public interface PostProcessPixelTransformer<ImgType> {
	public int apply(int x, int y, int argb,
			HashMap<PrePass<ImgType>, List<? extends Object>> preProcessingData,
			ImgType original, ImgType source, ImgType mask,
			double strength);
	
	public static final PostProcessPixelTransformer<BufferedImage> NULL_POST_TRANSFORMER
	= (x, y, argb, prePassData, original, source, mask, strength) -> {
		return argb;
	};
}
