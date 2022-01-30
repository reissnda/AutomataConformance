package org.processmining.plugins.neconformance.trees.ukkonen;

import java.util.Collections;
import java.util.List;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.kutoolbox.utils.LogUtils;
import org.processmining.plugins.neconformance.trees.LogTree;

/**
 * A suffix log tree implementation using Ukkonen's algorithm.
 * The code used in this package was adapted from: https://github.com/maxgarfinkel/suffixTree
 * Thanks to maxgarfinkel for the implementation of Ukkonen trees.
 */

public class UkkonenLogTree implements LogTree {
	private XLog log;
	private XEventClassifier classifier;
	private SuffixTree<String, List<String>> suffixTree;
	
	public UkkonenLogTree(XLog log) {
		this(log, XLogInfoImpl.STANDARD_CLASSIFIER);
	}
		
	public UkkonenLogTree(XLog log, XEventClassifier classifier) {
		this.log = log;
		this.classifier = classifier;
		this.suffixTree = new SuffixTree<String, List<String>>();
		constructSuffixtree();
	}
	
	private void constructSuffixtree() {
		for (int t = 0; t < log.size(); t++) {
			XTrace trace = log.get(t);
			addTraceToSuffixTree(trace);
		}
	}
	
	public void addTraceToSuffixTree(XTrace trace) {
		List<String> traceSuffixed = LogUtils.getTraceEventClassSequence(trace, classifier);
		Collections.reverse(traceSuffixed); // Reverse the trace
		suffixTree.add(traceSuffixed);
	}
	
	public int getLongestMatchingWindowSize(List<String> window, String entryClass) {
		Cursor<String, List<String>> cursor = new Cursor<String, List<String>>(suffixTree);
		if (!cursor.proceedTo(entryClass)) {
			System.err.println("Ukkonen tree could not enter entry class: "+entryClass+", returning 0");
			return 0;
		}
		int windowPos;
		for (windowPos = window.size() - 1; windowPos >= 0; windowPos--) { // Go through window in reverse
			if (cursor.proceedTo(window.get(windowPos))) {
				if (windowPos == 0)
					return window.size();
			} else {
				break;
			}
		}
		
		// The current window did not match
		return window.size() - windowPos - 1;
	}
	
	public int getShortestMatchingWindowSize(List<String> window, String entryClass) {
		Cursor<String, List<String>> cursor = new Cursor<String, List<String>>(suffixTree);
		if (!cursor.proceedTo(entryClass)) {
			System.err.println("Ukkonen tree could not enter entry class: "+entryClass+", returning 0");
			return 0;
		}
		int windowPos;
		for (windowPos = window.size() - 1; windowPos >= 0; windowPos--) { // Go through window in reverse
			// Check if alternative window could be found here
			if (!cursor.hasSingleContinuation()) {
				return window.size() - windowPos - 1;
			}
			
			// Continue onwards
			if (cursor.proceedTo(window.get(windowPos))) {
				if (windowPos == 0)
					return window.size();
			} else {
				break;
			}
		}
		
		// The current window did not match
		return window.size() - windowPos - 1;
	}
		
}
