package org.processmining.plugins.neconformance.plugins.simple;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapperPanel;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.neconformance.metrics.impl.BehavioralRecallMetric;
import org.processmining.plugins.neconformance.metrics.impl.BehavioralWeightedPrecisionMetric;
import org.processmining.plugins.neconformance.models.ProcessReplayModel;
import org.processmining.plugins.neconformance.models.impl.PetrinetReplayModel;
import org.processmining.plugins.neconformance.negativeevents.AbstractNegativeEventInducer;
import org.processmining.plugins.neconformance.negativeevents.impl.LogTreeWeightedNegativeEventInducer;
import org.processmining.plugins.neconformance.trees.LogTree;
import org.processmining.plugins.neconformance.trees.ukkonen.UkkonenLogTree;

public class PetrinetEvaluatorPlugin {
	@Plugin(name = "Evaluate (Weighted) Conformance of Log on Petri Net (Simple)", 
			parameterLabels = { "Log", "Petri net", "Marking" }, 
			returnLabels = { "Evaluation Result" }, 
			returnTypes = { PetrinetEvaluatorResult.class }, 
			help = "Evaluate a log on petri net (fitness, precision)")
	@UITopiaVariant(affiliation = "KU Leuven", 
		author = "Seppe vanden Broucke", 
		email = "seppe.vandenbroucke@econ.kuleuven.be", 
		website = "http://econ.kuleuven.be")
	
	public static PetrinetEvaluatorResult main(UIPluginContext context, XLog log, Petrinet onet, Marking marking) {
		PetrinetEvaluatorResult result = new PetrinetEvaluatorResult();
		
		result.log = log;
		result.net = onet;	
		result.marking = marking;
		
		PetrinetLogMapperPanel mapperPanel = new PetrinetLogMapperPanel(log, result.net);
		InteractionResult ir = context.showWizard("Mapping", true, true, mapperPanel);
		if (!ir.equals(InteractionResult.FINISHED)) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		//result.mapper = mapperPanel.getMap();
		result.mapper.applyMappingOnTransitions();
		
		context.log("Making log tree and negative event inducer...");
		
		LogTree logTree = new UkkonenLogTree(log);
		XEventClasses eventClasses = XEventClasses.deriveEventClasses(XLogInfoImpl.STANDARD_CLASSIFIER, log);
		result.inducer = new LogTreeWeightedNegativeEventInducer(
				eventClasses,
				AbstractNegativeEventInducer.deriveStartingClasses(eventClasses, log),
				logTree);
		result.inducer.setReturnZeroEvents(false);
		result.inducer.setUseBothWindowRatios(false);
		result.inducer.setUseWeighted(true);
		
		context.log("Making replay models...");
		result.replayModels = new ArrayList<ProcessReplayModel<Transition, XEventClass, Marking>>();
		Marking initialMarking = PetrinetUtils.getInitialMarking(result.net, result.marking);
		for (int t = 0; t < log.size(); t++) {
			if (t % 10 == 0)
				context.log(" "+t+" / "+log.size());
			XTrace trace = log.get(t);
			ProcessReplayModel<Transition, XEventClass, Marking> replayModel = 
					new PetrinetReplayModel(result.net, initialMarking, result.mapper);
			replayModel.reset();
			List<XEventClass> classSequence = getTraceAsClassSequence(trace, result.inducer);
			replayModel.replay(classSequence);
			result.replayModels.add(replayModel);
		}
		
		PetrinetReplayModel replayModel = new PetrinetReplayModel(result.net, initialMarking, result.mapper);
		BehavioralRecallMetric br = new BehavioralRecallMetric(replayModel, result.inducer, log, true, true);
		BehavioralWeightedPrecisionMetric bp = new BehavioralWeightedPrecisionMetric(replayModel, result.inducer, log, true, true);
		result.behavioralRecallMetric = br;
		result.behavioralPrecisionMetric = bp;
		
		return result;
		
	}
	
	private static List<XEventClass> getTraceAsClassSequence(XTrace trace, AbstractNegativeEventInducer inducer) {
		List<XEventClass> sequence = new ArrayList<XEventClass>();
		for (XEvent event : trace)
			sequence.add(inducer.getClassAlphabet().getClassOf(event));
		return sequence;
	}

}
