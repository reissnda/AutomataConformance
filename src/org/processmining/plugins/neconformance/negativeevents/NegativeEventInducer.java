package org.processmining.plugins.neconformance.negativeevents;

import java.util.Set;

import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.neconformance.types.WeightedEventClass;

public interface NegativeEventInducer {
	public Set<WeightedEventClass> getNegativeEvents(XTrace trace, int position);
	public Set<WeightedEventClass> getGeneralizedEvents(XTrace trace, int position);
}
