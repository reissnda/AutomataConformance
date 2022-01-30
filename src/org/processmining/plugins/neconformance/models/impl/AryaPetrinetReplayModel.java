package org.processmining.plugins.neconformance.models.impl;

import java.util.List;

import com.raffaeleconforti.context.FakePluginContext;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.EvClassLogPetrinetConnectionFactoryUI;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.kutoolbox.utils.LogUtils;
import org.processmining.plugins.neconformance.models.ProcessReplayModel;
import org.processmining.plugins.neconformance.utils.PetrinetReplayUtils;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedprefix.CostBasedPrefixAlg;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedprefix.CostBasedPrefixParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import au.unimelb.negativeEventsClasses.PetrinetLogMapper;
import au.unimelb.negativeEventsClasses.PetrinetUtils;


public class AryaPetrinetReplayModel extends PetrinetReplayModel {

	protected XEventClasses eventClasses;
	protected IPNReplayAlgorithm chosenAlgorithm;
	private PNRepResult aryaReplayResult;
	
	public AryaPetrinetReplayModel(Petrinet net, XEventClasses eventClasses, 
			Marking initialMarking, PetrinetLogMapper mapping) {
		super(net, initialMarking, mapping);
		this.eventClasses = eventClasses;
		this.chosenAlgorithm = new CostBasedPrefixAlg();
	}
	
	public AryaPetrinetReplayModel(AryaPetrinetReplayModel toClone) {
		this(toClone.getPetrinet(), toClone.getEventClasses(), toClone.getInitialState(), toClone.getMapping());
		
	}
	
	public void reset() {
		super.reset();
		aryaReplayResult = null;
		currentMarking = new Marking();
		currentMarking.addAll(initialState.toList());
	}

	public void replay(List<XEventClass> trace) {
		this.reset();
		
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog log = LogUtils.newLog("Dummy Log");
		XTrace xtrace = factory.createTrace();
		for (XEventClass ec : trace)
			xtrace.add(makeEvent(ec));
		log.add(xtrace);
		
		TransEvClassMapping transEvMapping = getTransEvClassMapping(mapping, net);
		
	//	IPNReplayParameter parameters = new CostBasedCompleteParam(
	//			eventClasses.getClasses(),
	//			transEvMapping.getDummyEventClass(),
	//			net.getTransitions());
		IPNReplayParameter parameters = new CostBasedPrefixParam();
		parameters.setCreateConn(false);
		parameters.setGUIMode(false);
		parameters.setInitialMarking(this.getInitialState());
		parameters.setFinalMarkings(new Marking[] {PetrinetUtils.getFinalMarking(net)});
		((CostBasedPrefixParam) parameters).setInappropriateTransFireCost(1);
        
		boolean satisfied = chosenAlgorithm.isAllReqSatisfied(null, net, log, transEvMapping, parameters);
		
		PNLogReplayer replayer = new PNLogReplayer();
		try {
			aryaReplayResult = replayer.replayLog(new FakePluginContext(), net, log, transEvMapping, chosenAlgorithm, parameters);
		} catch (Exception e) {
			aryaReplayResult = null;
			System.err.println("WARNING: Arya failed to replay");
			e.printStackTrace();
			return;
		}
		
		if (aryaReplayResult == null) {
			System.err.println("WARNING: Arya failed to replay (no exception message available)");
			System.err.println("Satisfaction state: "+satisfied);
			return;
		}
		
		SyncReplayResult syncResult = aryaReplayResult.iterator().next();
		for (int i = 0; i < syncResult.getStepTypes().size(); i++) {
			ReplayMove move = null;
			Transition t = null;
			XEventClass c = null;
			
			StepTypes stepType = syncResult.getStepTypes().get(i);
			switch (stepType) {
			case LMGOOD:
				t = (Transition) syncResult.getNodeInstance().get(i);
				c = mapping.get(t);
		    	move = ReplayMove.BOTH_SYNCHRONOUS;
				break;
			case LMNOGOOD:
				t = (Transition) syncResult.getNodeInstance().get(i);
				c = mapping.get(t);
		    	move = ReplayMove.BOTH_FORCED;
				break;
			case MINVI:
				t = (Transition) syncResult.getNodeInstance().get(i);
		    	c = null;
				move = ReplayMove.MODELONLY_UNOBSERVABLE;
				break;
		    case MREAL:
		    	t = (Transition) syncResult.getNodeInstance().get(i);
		    	c = null;
		    	move = ReplayMove.MODELONLY_SKIPPED;
				break;
			case L:
				t = null;
				String s = syncResult.getNodeInstance().get(i).toString();
				c = mapping.getEventClass(s);
				move = ReplayMove.LOGONLY_INSERTED;
				break;
		    default:
				break;
			}
			
			Marking m = currentMarking;
			if (t != null)
				m = PetrinetReplayUtils.getMarkingAfterFire(net, m, t);
			this.addReplayStep(move, t, c, m);
			this.currentMarking = m;
		}          
		
	}
	
	private XEvent makeEvent(XEventClass eventClass) {
		XEvent event = new XEventImpl();
		String[] keys = this.eventClasses.getClassifier().getDefiningAttributeKeys();
		String[] values = eventClass.toString().split("\\+");
		for (int i = 0; i < keys.length; i++)
			event.getAttributes().put(keys[i], new XAttributeLiteralImpl(keys[i], values[i]));
		return event;
	}
	
	private TransEvClassMapping getTransEvClassMapping(PetrinetLogMapper mapping, Petrinet net) {
		TransEvClassMapping transEvClassMapping = new TransEvClassMapping(
				mapping.getEventClassifier(),
				EvClassLogPetrinetConnectionFactoryUI.DUMMY);

		for (Transition transition : net.getTransitions()) {
			if (mapping.get(transition) == null) {
				transEvClassMapping.put(transition, EvClassLogPetrinetConnectionFactoryUI.DUMMY);
			} else if (mapping.get(transition).equals(PetrinetLogMapper.BLOCKING_CLASS)) {
				transEvClassMapping.put(transition, EvClassLogPetrinetConnectionFactoryUI.DUMMY);
			} else {
				transEvClassMapping.put(transition, mapping.get(transition));
			}
		}

		return transEvClassMapping;
	}

	public XEventClasses getEventClasses() {
		return eventClasses;
	}

	public IPNReplayAlgorithm getChosenAlgorithm() {
		return chosenAlgorithm;
	}

	public void setChosenAlgorithm(IPNReplayAlgorithm chosenAlgorithm) {
		this.chosenAlgorithm = chosenAlgorithm;
	}

	public PNRepResult getAryaReplayResult() {
		return aryaReplayResult;
	}
	
	public ProcessReplayModel<Transition, XEventClass, Marking> copy() {
		return new AryaPetrinetReplayModel(this);
	}
}
