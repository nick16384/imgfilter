package com.ImgFilter;

import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamHelper {
	/**
	 * Returns the corresponding key to a value in a Map<K, V>.
	 * If the key is not found, null is returned.
	 * @param <K>
	 * @param <V>
	 * @param map
	 * @param searchValue
	 * @return
	 */
	public static <K, V> K getKeyFromValue(Map<K, V> map, V searchValue) {
		return map.entrySet()
				.stream()
				.filter(entry -> Objects.equals(entry.getValue(), searchValue))
				.map(Map.Entry::getKey)
				.findFirst()
				.orElse(null);
	}
	
	/**
	 * Combines two separate Maps of the same mapping types into one.
	 * @param <K>
	 * @param <V>
	 * @param map1
	 * @param map2
	 * @return
	 */
	public static <K, V> Map<K, V> combineMaps(Map<K, V> map1, Map<K, V> map2) {
		Stream<Entry<K, V>> combinedMapsStream =
				Stream.concat(map1.entrySet().stream(), map2.entrySet().stream());
		return combinedMapsStream.collect(
				  Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}
