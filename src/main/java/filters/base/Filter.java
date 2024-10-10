package filters.base;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface Filter<ImgType extends BufferedImage> {
	public default List<PrePass<ImgType>> getPrePasses() {
		return new ArrayList<>();
	}
	
	public default List<PixelTransformer<ImgType>> getMainPassTransformers() {
		return new ArrayList<>();
	}
	
	public default List<PostProcessPixelTransformer<ImgType>> getPostPassTransformers() {
		return new ArrayList<>();
	}
	
	public String getName();
}
