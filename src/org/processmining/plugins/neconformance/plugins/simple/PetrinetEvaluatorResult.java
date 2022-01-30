package org.processmining.plugins.neconformance.plugins.simple;


import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.neconformance.metrics.impl.BehavioralRecallMetric;
import org.processmining.plugins.neconformance.metrics.impl.BehavioralWeightedPrecisionMetric;
import org.processmining.plugins.neconformance.models.ProcessReplayModel;
import org.processmining.plugins.neconformance.negativeevents.impl.LogTreeWeightedNegativeEventInducer;
import au.unimelb.negativeEventsClasses.PetrinetLogMapper;

public class PetrinetEvaluatorResult {
	public LogTreeWeightedNegativeEventInducer inducer;
	public XLog log;
	public Petrinet net;	
	public PetrinetLogMapper mapper;
	public List<ProcessReplayModel<Transition, XEventClass, Marking>> replayModels;
	public BehavioralRecallMetric behavioralRecallMetric;
	public BehavioralWeightedPrecisionMetric behavioralPrecisionMetric;
	public Marking marking;
}
