package org.processmining.plugins.neconformance.negativeevents;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.kutoolbox.utils.LogUtils;
import org.processmining.plugins.neconformance.types.WeightedEventClass;

public abstract class AbstractNegativeEventInducer implements NegativeEventInducer {
	protected XEventClasses classAlphabet;
	protected Set<XEventClass> startingClasses;
	
	protected AbstractNegativeEventInducer(XEventClasses classes, Set<XEventClass> startingClasses) {
		this.classAlphabet = classes;
		this.startingClasses = startingClasses;
	}
	
	public String getEventClassAtPosition(XTrace trace, int position) {
		return classAlphabet.getClassifier().getClassIdentity(trace.get(position));
	}
	
	public List<String> getEventWindowBeforePosition(XTrace trace, int position, int windowSize, boolean stopAtNextOccurrence) {
		List<String> fullSequence = LogUtils.getTraceEventClassSequence(trace, classAlphabet.getClassifier());
		int startPos = position - windowSize;
		if (windowSize < 0 || startPos < 0) startPos = 0;
		if (stopAtNextOccurrence) {
			for (int checkPos = position-1; checkPos >= startPos; checkPos--) {
				if (fullSequence.get(checkPos).equals(fullSequence.get(position))) {
					startPos = checkPos;
					break;
				}
			}
		}
		return new ArrayList<String>(fullSequence.subList(startPos, position));
	}
	
	public Set<String> getEventBagBeforePosition(XTrace trace, int position, int windowSize, boolean stopAtNextOccurrence) {
		return new HashSet<String>(getEventWindowBeforePosition(trace, position, windowSize, stopAtNextOccurrence));
	}
	
	public abstract Set<WeightedEventClass> getNegativeEvents(XTrace trace, int position);
	public abstract Set<WeightedEventClass> getGeneralizedEvents(XTrace trace, int position);

	public XEventClasses getClassAlphabet() {
		return classAlphabet;
	}
	
	public boolean isStartingClass(XEventClass clazz) {
		return this.startingClasses.contains(clazz);
	}
	
	public static Set<XEventClass> deriveStartingClasses(XEventClasses classAlphabet, XLog log) {
		Set<XEventClass> starters = new HashSet<XEventClass>();
		for (XTrace t : log)
			if (t.size() > 0)
				starters.add(classAlphabet.getClassOf(t.get(0)));
		return starters;
	}
}
