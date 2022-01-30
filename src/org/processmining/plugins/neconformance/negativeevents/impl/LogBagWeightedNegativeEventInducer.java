package org.processmining.plugins.neconformance.negativeevents.impl;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.neconformance.bags.LogBag;
import org.processmining.plugins.neconformance.negativeevents.AbstractNegativeEventInducer;
import org.processmining.plugins.neconformance.types.WeightedEventClass;

public class LogBagWeightedNegativeEventInducer extends AbstractNegativeEventInducer {
	private LogBag logBag;
	
	private boolean useWeighted = true;
	private int negWindowSize = -1;
	private int genWindowSize = -1;
	private boolean useBothWindowRatios = true;
	private boolean useWindowOccurrenceCut = false;
	private boolean returnZeroEvents = false;
	
	public LogBagWeightedNegativeEventInducer(XEventClasses classes, Set<XEventClass> startingClasses, LogBag logBag) {
		super(classes, startingClasses);
		this.logBag = logBag;
	}
	
	public Set<WeightedEventClass> getWeightedNegativeEvents(XTrace trace, int position, boolean isGeneralization) {
		int windowSize = (isGeneralization) ? genWindowSize : negWindowSize;
		Set<String> classBag = this.getEventBagBeforePosition(trace, position, windowSize, useWindowOccurrenceCut);
		String positiveClass = this.getEventClassAtPosition(trace, position);
		
		Set<WeightedEventClass> eventSet = new HashSet<WeightedEventClass>();
		for (XEventClass ec : classAlphabet.getClasses()) {
			if (ec.getId().equals(positiveClass))
				continue;
			
			double weight = 0D;
			double lenw = (double) classBag.size();
		
			if (classBag.size() == 0) {
				weight = ((isGeneralization && this.isStartingClass(ec)) 
						|| (!isGeneralization && !this.isStartingClass(ec))) ? 1D : 0D;
			} else if (isGeneralization) {
				double mws = (useBothWindowRatios) 
						? (double) logBag.getSmallestSharedSize(classBag, ec.getId())
						: (double) logBag.getLargestSharedBagSize(classBag, ec.getId());
				weight = (useWeighted)
						? (mws / lenw)
						: (mws >= lenw ? 1D : 0D);
			} else {
				double mws = (double) logBag.getLargestSharedBagSize(classBag, ec.getId());
				weight = (useWeighted)
						? ((lenw - mws) / lenw)
						: (mws >= lenw ? 0D : 1D);
			}
			
			if (weight > 0D || returnZeroEvents)
				eventSet.add(new WeightedEventClass(ec, weight));
			
		}
		
		return eventSet;
	}
	
	public Set<WeightedEventClass> getNegativeEvents(XTrace trace, int position) {
		return this.getWeightedNegativeEvents(trace, position, false);
	}

	public Set<WeightedEventClass> getGeneralizedEvents(XTrace trace, int position) {
		return this.getWeightedNegativeEvents(trace, position, true);
	}

	public LogBag getLogTree() {
		return logBag;
	}

	public void setLogBag(LogBag logBag) {
		this.logBag = logBag;
	}

	public boolean isUseWeighted() {
		return useWeighted;
	}

	public void setUseWeighted(boolean useWeighted) {
		this.useWeighted = useWeighted;
	}

	public int getNegWindowSize() {
		return negWindowSize;
	}

	public void setNegWindowSize(int negWindowSize) {
		this.negWindowSize = negWindowSize;
	}

	public int getGenWindowSize() {
		return genWindowSize;
	}

	public void setGenWindowSize(int genWindowSize) {
		this.genWindowSize = genWindowSize;
	}

	public boolean isUseBothWindowRatios() {
		return useBothWindowRatios;
	}

	public void setUseBothWindowRatios(boolean useBothWindowRatios) {
		this.useBothWindowRatios = useBothWindowRatios;
	}

	public boolean isUseWindowOccurrenceCut() {
		return useWindowOccurrenceCut;
	}

	public void setUseWindowOccurrenceCut(boolean useWindowOccurrenceCut) {
		this.useWindowOccurrenceCut = useWindowOccurrenceCut;
	}

	public boolean isReturnZeroEvents() {
		return returnZeroEvents;
	}

	public void setReturnZeroEvents(boolean returnZeroEvents) {
		this.returnZeroEvents = returnZeroEvents;
	}
}
