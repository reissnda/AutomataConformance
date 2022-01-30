package au.unimelb.evaluation;

import nl.tue.astar.AStarException;
import nl.tue.astar.impl.memefficient.MemoryEfficientAStarAlgorithm;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.AbstractPetrinetReplayer;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithoutILP;
import org.processmining.plugins.astar.petrinet.impl.*;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

import java.util.HashMap;
import java.util.Map;

public class AlignmentSetup {
	static {
		try {
			System.loadLibrary("lpsolve55");
			System.loadLibrary("lpsolve55j");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Petrinet net;
	private XLog log;
	private Map<Transition, Integer> costMOS;
	private Map<XEventClass, Integer> costMOT;
	TransEvClassMapping mapping;

	public AlignmentSetup(Petrinet net, XLog log) {

		this.net = net;
		this.log = log;
		costMOS = constructMOSCostFunction(net);
		XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
		costMOT = constructMOTCostFunction(net, log, eventClassifier);
		mapping = constructMapping(net, log, eventClassifier);

	}

	public AlignmentSetup(Petrinet net, XLog log, TransEvClassMapping mapping, Map<Transition, Integer> costMOS,
			Map<XEventClass, Integer> costMOT) {

		this.net = net;
		this.log = log;
		this.costMOS = costMOS;
		this.costMOT = costMOT;
		this.mapping = mapping;

	}

	public PNRepResult getAlignment(PluginContext context, Marking initialMarking, Marking finalMarking,
			boolean useILP, final int minCost) {

		AbstractPetrinetReplayer<?, ?> replayEngine;
		if (useILP) {
			replayEngine = new PetrinetReplayerWithILP() {
				@Override
				protected int getMinBoundMoveModel(IPNReplayParameter parameters, final int delta,
						final MemoryEfficientAStarAlgorithm<PHead, PILPTail> aStar, PILPDelegate delegateD)
						throws AStarException {
					return minCost;

				}

			};
		} else {
			replayEngine = new PetrinetReplayerWithoutILP() {
				@Override
				protected int getMinBoundMoveModel(IPNReplayParameter parameters, final int delta,
						final MemoryEfficientAStarAlgorithm<PHead, PNaiveTail> aStar, PNaiveDelegate delegateD)
						throws AStarException {
					return minCost;

				}

			};
		}

		CostBasedCompleteParam parameters = new CostBasedCompleteParam(costMOT, costMOS);

		parameters.setInitialMarking(initialMarking);
		parameters.setFinalMarkings(finalMarking);
		parameters.setGUIMode(false);
		parameters.setCreateConn(false);
		parameters.setNumThreads(1);
		//		parameters.setMaxNumOfStates(1000000);

		PNRepResult result = null;
		try {
			result = replayEngine.replayLog(context, net, log, mapping, parameters);

		} catch (AStarException e) {
			e.printStackTrace();
		}

		return result;
	}

	private Map<Transition, Integer> constructMOSCostFunction(Petrinet net) {
		Map<Transition, Integer> costMOS = new HashMap<Transition, Integer>();

		for (Transition t : net.getTransitions())
			if (t.isInvisible())
				costMOS.put(t, 0);
			else
				costMOS.put(t, 1);

		return costMOS;
	}

	private Map<XEventClass, Integer> constructMOTCostFunction(Petrinet net, XLog log, XEventClassifier eventClassifier) {
		Map<XEventClass, Integer> costMOT = new HashMap<XEventClass, Integer>();
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

		for (XEventClass evClass : summary.getEventClasses().getClasses()) {
			costMOT.put(evClass, 1);
		}

		return costMOT;
	}

	private TransEvClassMapping constructMapping(PetrinetGraph net, XLog log, XEventClassifier eventClassifier) {
		TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier, new XEventClass("DUMMY", 99999));

		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

		for (Transition t : net.getTransitions()) {
			for (XEventClass evClass : summary.getEventClasses().getClasses()) {
				String id = evClass.getId();

				if (t.getLabel().equals(id)) {
					mapping.put(t, evClass);
					break;
				}
			}

		}

		return mapping;
	}

}
