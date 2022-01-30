package org.processmining.plugins.neconformance.trees.suffixtree;

import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.neconformance.trees.LogTree;

public class SuffixLogTree implements LogTree {
	private XLog log;
	private XEventClassifier classifier;
	private int windowSize;
	
	private SuffixTree suffixTree;
	private int stateCounter;
	
	private final static String END_TRANSITION = "###ENDER###";
	
	public SuffixLogTree(XLog log) {
		this(log, XLogInfoImpl.STANDARD_CLASSIFIER, -1);
	}
	
	public SuffixLogTree(XLog log, XEventClassifier classifier) {
		this(log, classifier, -1);
	}
	
	public SuffixLogTree(XLog log, XEventClassifier classifier, int windowSize) {
		this.log = log;
		this.classifier = classifier;
		this.windowSize = windowSize;
		constructSuffixtree();
	}
	
	private void constructSuffixtree() {
		suffixTree = new SuffixTree();
		stateCounter = 0;
		
		for (int t = 0; t < log.size(); t++) {
			XTrace trace = log.get(t);
			addTraceToSuffixTree(trace);
		}
	}
	
	public void addTraceToSuffixTree(XTrace trace) {
		for (int startIndex = 0; startIndex < trace.size(); startIndex++) {
			int endIndex = (windowSize <= 0) ? 0 : startIndex - windowSize;
			if (endIndex < 0)
				endIndex = 0;
			SuffixTreeNode currentNode = suffixTree.getRootNode();
			for (int p = startIndex; p >= endIndex; p--) {
				String eventClassP = classifier.getClassIdentity(trace.get(p));
				SuffixTreeNode nextNode = suffixTree.getNodeOut(currentNode, eventClassP);
				if (nextNode == null)
					nextNode = suffixTree.addNode(
							currentNode,
							stateCounter++,
							eventClassP);
				currentNode = nextNode;
			}
			suffixTree.addNode(
						currentNode,
						stateCounter++,
						END_TRANSITION);
		}
	}
	
	public int getLongestMatchingWindowSize(List<String> window, String entryClass) {
		SuffixTreeNode currentNode = suffixTree.getNodeOut(suffixTree.getRootNode(), entryClass);
		if (currentNode == null) {
			System.err.println("Suffix tree could not enter entry class: "+entryClass+", returning 0");
			return 0;
		}
		int windowPos;
		for (windowPos = window.size()-1; windowPos >= 0; windowPos--) {
			SuffixTreeNode nextNode = suffixTree.getNodeOut(currentNode, window.get(windowPos));
			if (nextNode != null) { // Window still matches
				currentNode = nextNode;
				if (windowPos == 0)
					return window.size();
			} else { // Window no longer matches
				break;
			}
		}
		// The current window did not match
		return window.size() - windowPos - 1;
	}
	
	public int getShortestMatchingWindowSize(List<String> window, String entryClass) {
		SuffixTreeNode currentNode = suffixTree.getNodeOut(suffixTree.getRootNode(), entryClass);
		if (currentNode == null) {
			System.err.println("Suffix tree could not enter entry class: "+entryClass+", returning 0");
			return 0;
		}
		int windowPos;
		for (windowPos = window.size()-1; windowPos >= 0; windowPos--) {
			// Check if alternative windows could be found here
			if (suffixTree.nrNodesOut(currentNode) != 1) { // End of matching found here
				return window.size() - windowPos - 1;
			}
			
			// Continue onwards with matching
			SuffixTreeNode nextNode = suffixTree.getNodeOut(currentNode, window.get(windowPos));
			if (nextNode != null) { // Window still matches
				currentNode = nextNode;
				if (windowPos == 0)
					return window.size();
			} else { // Window no longer matches
				break;
			}
		}
		
		// The current window did not match
		return window.size() - windowPos - 1;
	}
	
	public SuffixTree getSuffixTree() {
		return suffixTree;
	}

	public void setSuffixTree(SuffixTree suffixTree) {
		this.suffixTree = suffixTree;
	}

	
}
