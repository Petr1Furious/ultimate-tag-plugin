package me._furiouspotato_.ultimatetag;

import java.util.Comparator;
import java.util.Map;

public class PlayerComparator implements Comparator<Map.Entry<String, TagPlayer>> {

	public int compare(Map.Entry<String, TagPlayer> a, Map.Entry<String, TagPlayer> b) {
		if (a.getValue().score > b.getValue().score) {
			return -1;
		}
		if (a.getValue().score > b.getValue().score) {
			return 1;
		}
		return 0;
	}
}
