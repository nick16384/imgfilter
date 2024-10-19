package filters.base;

import java.util.List;

@FunctionalInterface
public interface PrePass<RasterType> {
	/**
	 * @apiNote Function input values should begin with an underscore symbol
	 * to differentiate them from function internal variables.
	 */
	public List<?> runPreProcessing(RasterType _source,
			RasterType _mask,
			double _strength);
	
	public static final PrePass<ImageRaster> NULL_PRE_PASS
	= (_source, _mask, _strength) -> {
		return null;
	};
}
