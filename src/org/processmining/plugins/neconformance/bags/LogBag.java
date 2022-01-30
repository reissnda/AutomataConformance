package org.processmining.plugins.neconformance.bags;

import java.util.Set;

public interface LogBag {
	public int getLargestSharedBagSize(Set<String> window, String entryClass);
	public int getSmallestSharedSize(Set<String> window, String entryClass);
}
