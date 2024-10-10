package filters;

import static java.util.Map.entry;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import filters.base.Filter;
import filters.base.MultiPassFilterApplicator;

/**
 * Contains a map from filter names to MultiPassFilters.
 */
public final class FiltersList {
	public static final HashMap<String, Filter<BufferedImage>> FILTERS_LIST =
			new HashMap<String, Filter<BufferedImage>>(
					Map.ofEntries(
							entry(new None().getName(),                new None()),
							entry(new Grayscale().getName(),           new Grayscale()),
							entry(new Inverted().getName(),            new Inverted()),
							entry(new Blur101x101().getName(),         new Blur101x101()),
							entry(new RandomNoise().getName(),         new RandomNoise()),
							entry(new Erase().getName(),               new Erase()),
							entry(new TurboColor2().getName(),         new TurboColor2()),
							entry(new Brightness().getName(),          new Brightness()),
							entry(new TurboTurboTM().getName(),        new TurboTurboTM()),
							entry(new ChannelShiftNoAlpha().getName(), new ChannelShiftNoAlpha()),
							entry(new MaxBlur().getName(),             new MaxBlur()),
							entry(new MedianBlur().getName(),          new MedianBlur()),
							entry(new Contrast().getName(),            new Contrast()),
							entry(new ZeroMaxContrast().getName(),     new ZeroMaxContrast()),
							entry(new HighlightContrast().getName(),   new HighlightContrast()),
							entry(new FlipLR().getName(),              new FlipLR()),
							entry(new FlipUD().getName(),              new FlipUD()),
							entry(new Sharpening().getName(),          new Sharpening())
							));
	
	// TODO: Make subclass MaskedMultiPassFilter
	public static final HashMap<String, Filter<BufferedImage>> MASK_FILTER_LIST =
			new HashMap<String, Filter<BufferedImage>>(
					Map.ofEntries(
							entry(new Mask_Add().getName(),      new Mask_Add()),
							entry(new Mask_Avg().getName(),      new Mask_Avg()),
							entry(new Mask_Multiply().getName(), new Mask_Multiply())
							));
	
	public static final Filter<BufferedImage> DEFAULT_FILTER = new None();
			//FILTERS_LIST.values().stream().findFirst().get();
	
	public static final Filter<BufferedImage> DEFAULT_MASK_FILTER = new Mask_Multiply();
	
	public static Filter<BufferedImage> fromString(String str) {
		return combineMaps(FILTERS_LIST, MASK_FILTER_LIST).get(str);
	}
	
	public static String toString(Filter<BufferedImage> filter) {
		return combineMaps(FILTERS_LIST, MASK_FILTER_LIST).entrySet()
	              .stream()
	              .filter(entry -> Objects.equals(entry.getValue(), filter))
	              .map(Map.Entry::getKey)
	              .findFirst()
	              .orElse(null);
	}
	
	private static <K, V> Map<K, V> combineMaps(Map<K, V> map1, Map<K, V> map2) {
		Stream<Entry<K, V>> combinedMapsStream =
				Stream.concat(map1.entrySet().stream(), map2.entrySet().stream());
		return combinedMapsStream.collect(
				  Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}