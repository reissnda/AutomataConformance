package org.processmining.plugins.neconformance.trees;

import java.util.List;

public interface LogTree {
	public int getLongestMatchingWindowSize(List<String> window, String entryClass);
	public int getShortestMatchingWindowSize(List<String> window, String entryClass);
}
