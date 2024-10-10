package filters.base;

import java.awt.image.BufferedImage;
import java.util.List;

@FunctionalInterface
public interface PrePass<ImgType> {
	public List<? extends Object> runPreProcessing(ImgType source,
			ImgType mask,
			double strength);
	
	public static final PrePass<BufferedImage> NULL_PRE_PASS
	= (source, mask, strength) -> {
		return null;
	};
}
