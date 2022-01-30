package org.processmining.plugins.neconformance.bags.naieve;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.neconformance.bags.LogBag;

public class NaieveLogBag implements LogBag {
	private XLog log;
	private XEventClassifier classifier;
	private int windowSize;
	
	List<XEventClass> eventClasses;
	Map<String, Set<BitSet>> bagsPerClass;

	public NaieveLogBag(XLog log) {
		this(log, XLogInfoImpl.STANDARD_CLASSIFIER, -1);
	}
	
	public NaieveLogBag(XLog log, XEventClassifier classifier) {
		this(log, classifier, -1);
	}
	
	public NaieveLogBag(XLog log, XEventClassifier classifier, int windowSize) {
		this.log = log;
		this.classifier = classifier;
		this.windowSize = windowSize;
		constructLookupTable();
	}
	
	private void constructLookupTable() {
		eventClasses = new ArrayList<XEventClass>(XEventClasses.deriveEventClasses(classifier, log).getClasses());
		bagsPerClass = new HashMap<String, Set<BitSet>>();
		for (XEventClass ec : eventClasses)
			bagsPerClass.put(ec.toString(), new HashSet<BitSet>());
		
		for (int t = 0; t < log.size(); t++) {
			XTrace trace = log.get(t);
			for (int startIndex = 0; startIndex < trace.size(); startIndex++) {
				String eventClassP = classifier.getClassIdentity(trace.get(startIndex));
				BitSet eventBag = getEventBagBeforePosition(trace, startIndex);
				bagsPerClass.get(eventClassP).add(eventBag);
			}
		}
	}

	public int getLargestSharedBagSize(Set<String> window, String entryClass) {
		BitSet windowBag = getEventBag(window);
		int largestShared = -1;
		for (BitSet eventBag : bagsPerClass.get(entryClass)) {
			BitSet andWindowBag = (BitSet) windowBag.clone();
			andWindowBag.and(eventBag);
			int shared = andWindowBag.cardinality();
			if (shared > largestShared)
				largestShared = shared;
			if (largestShared == window.size())
				return largestShared;
		}
		return largestShared;
	}

	public int getSmallestSharedSize(Set<String> window, String entryClass) {
		BitSet windowBag = getEventBag(window);
		int smallestShared = -1;
		for (BitSet eventBag : bagsPerClass.get(entryClass)) {
			BitSet andWindowBag = (BitSet) windowBag.clone();
			andWindowBag.and(eventBag);
			int shared = andWindowBag.cardinality();
			if (shared < smallestShared || smallestShared == -1)
				smallestShared = shared;
			if (smallestShared == 0)
				return smallestShared;
		}
		return smallestShared;
	}
	
	private BitSet getEventBagBeforePosition(XTrace trace, int position) {
		Set<String> eventBag = new HashSet<String>();
		int startPos = position - windowSize;
		if (windowSize < 0 || startPos < 0) startPos = 0;
		for (int checkPos = position-1; checkPos >= startPos; checkPos--) {
			String eventClassP = classifier.getClassIdentity(trace.get(checkPos));
			eventBag.add(eventClassP);
		}

		return getEventBag(eventBag);
	}

	private BitSet getEventBag(Set<String> window) {
		BitSet bitBag = new BitSet(eventClasses.size());
		for (int i = 0; i < eventClasses.size(); i++) {
			if (window.contains(eventClasses.get(i).toString())) {
				bitBag.set(i, true);
			}
		}
		
		return bitBag;
	}

	
}
