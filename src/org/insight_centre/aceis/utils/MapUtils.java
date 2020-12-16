package org.insight_centre.aceis.utils;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author feng
 * 
 *         provides the sort-by-value function for Map<EventPattern,Double> to rank event patterns based on their qos
 *         utility from high to low
 */
public class MapUtils {
	public static void main(String[] args) {
		Random random = new Random(System.currentTimeMillis());
		Map<String, Double> testMap = new HashMap<String, Double>();
		for (int i = 0; i < 10; ++i) {
			testMap.put("SomeString" + random.nextInt(), Math.random());
		}

		testMap = MapUtils.sortByValue(testMap);
		// Assert.assertEquals(1000, testMap.size());

		// Double previous = null;
		for (Entry<String, Double> entry : testMap.entrySet()) {
			// Assert.assertNotNull(entry.getValue());
			// if (previous != null) {
			// Assert.assertTrue(entry.getValue() >= previous);
			// }
			// previous = entry.getValue();
			System.out.println(entry);
		}
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Entry<K, V>> list = new LinkedList<Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Entry<K, V>>() {
			public int compare(Entry<K, V> o1, Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	static Map sortByValue2(Map map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Entry) (o1)).getValue()).compareTo(((Entry) (o2)).getValue());
			}
		});

		Map result = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Entry entry = (Entry) it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}