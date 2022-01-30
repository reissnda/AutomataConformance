package org.processmining.plugins.neconformance.models.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.neconformance.models.AbstractProcessReplayModel;
import org.processmining.plugins.neconformance.models.ProcessReplayModel;
import org.processmining.plugins.neconformance.utils.PetrinetReplayUtils;
import au.unimelb.negativeEventsClasses.PetrinetLogMapper;

public class PetrinetReplayModel extends AbstractProcessReplayModel<Transition, XEventClass, Marking> {

	protected Petrinet net;
	protected Marking currentMarking;
	protected au.unimelb.negativeEventsClasses.PetrinetLogMapper mapping;
	protected Random generator;
	
	protected List<Boolean> transitionIsForced;
	
	public PetrinetReplayModel(Petrinet net, Marking initialMarking, PetrinetLogMapper mapping) {
		super(mapping.getTransitions(), mapping.getEventClasses(), PetrinetUtils.getInitialMarking(net, initialMarking));
		this.net = net;
		this.mapping = mapping;
	}
	
	public PetrinetReplayModel(PetrinetReplayModel toClone) {
		this(toClone.getPetrinet(), toClone.getInitialState(), toClone.getMapping());
	}
	
	public void reset() {
		super.reset();
		currentMarking = new Marking(initialState);
		transitionIsForced = new ArrayList<Boolean>();
		this.generator = new Random();
	}

	public void replay(List<XEventClass> trace) {
		this.reset();
		
		int currentSequencePosition = 0;
		while (currentSequencePosition < trace.size()) {
			XEventClass currentClass = trace.get(currentSequencePosition);
			Set<Transition> enabledCandidates = new HashSet<Transition>();
			Set<Transition> allCandidates = new HashSet<Transition>();
			Set<Transition> filteredEnabledCandidates = new HashSet<Transition>();
			Set<Transition> filteredForcedCandidates = new HashSet<Transition>();
			
			XEventClass nextClass = (currentSequencePosition < trace.size() - 1)
					? trace.get(currentSequencePosition + 1)
					: null;

			enabledCandidates = PetrinetReplayUtils.getEnabledMappedTransitions(net, currentMarking, mapping, currentClass);
			allCandidates = PetrinetReplayUtils.getMappedTransitions(mapping, currentClass);
			filteredEnabledCandidates = filterOnNextTaskEnabler(enabledCandidates, currentClass, nextClass);
			filteredForcedCandidates = filterOnNextTaskEnabler(allCandidates, currentClass, nextClass);
			
			List<Transition> invisiblePath = 
					PetrinetReplayUtils.getInvisibleTaskReachabilityPaths(net, mapping, currentClass, currentMarking);
			
			ReplayMove move = null;
			Transition t = null;
			XEventClass c = null;
			boolean f = false;
			if (enabledCandidates.size() > 0) {
				if (filteredEnabledCandidates.size() > 0)
					t = getRandomTransition(filteredEnabledCandidates);
				else
					t = getRandomTransition(enabledCandidates);		
				c = currentClass;
				move = ReplayMove.BOTH_SYNCHRONOUS;
			} else if (invisiblePath != null) {
				// Add all transitions until last one
				for (int i = 0; i < invisiblePath.size() - 1; i++) {
					Marking invm = currentMarking;
					invm = PetrinetReplayUtils.getMarkingAfterFire(net, invm, invisiblePath.get(i));
					this.addReplayStep(ReplayMove.MODELONLY_UNOBSERVABLE, invisiblePath.get(i), null, invm);
					this.transitionIsForced.add(false);
					this.currentMarking = invm;
				}
				t = invisiblePath.get(invisiblePath.size()-1);
				c = null;
				move = ReplayMove.MODELONLY_UNOBSERVABLE;
			} else if (allCandidates.size() > 0) {
				if (filteredForcedCandidates.size() > 0)
					t = getRandomTransition(filteredForcedCandidates);
				else
					t = getRandomTransition(allCandidates);		
				c = currentClass;
				move = ReplayMove.BOTH_FORCED;
				f = true;
			} else {
				// Move the log only
				// Event was probably not mapped
				t = null;
				c = currentClass;
				move = ReplayMove.LOGONLY_INSERTED;
			} 
			
			Marking m = currentMarking;
			if (t != null) // Get new marking
				m = PetrinetReplayUtils.getMarkingAfterFire(net, m, t);
			if (c != null)
				currentSequencePosition++;
			this.addReplayStep(move, t, c, m);
			this.transitionIsForced.add(f);
			this.currentMarking = m;
		} // end for
	}
	
	private Transition getRandomTransition(Set<Transition> choices) {
		int index = generator.nextInt(choices.size());
		return (Transition) choices.toArray()[index];
	}

	public PetrinetLogMapper getMapping() {
		return mapping;
	}

	public Petrinet getPetrinet() {
		return net;
	}

	private Set<Transition> filterOnNextTaskEnabler(Set<Transition> fireCandidates,
			XEventClass currentClass, XEventClass nextClass) {
		HashSet<Transition> filtered = new HashSet<Transition>();
		
		for (Transition t : fireCandidates) {
			Set<Transition> nextTaskEnabledTransitions = null;
			Marking nextState = PetrinetReplayUtils.getMarkingAfterFire(net, currentMarking, t);
			
			if (PetrinetReplayUtils.isInvisibleTransition(t, mapping))
				nextTaskEnabledTransitions = 
					PetrinetReplayUtils.getEnabledMappedTransitions(net, nextState, mapping, currentClass);
			else if(nextClass != null && !PetrinetReplayUtils.isInvisibleTransition(t, mapping))
				nextTaskEnabledTransitions = 
					PetrinetReplayUtils.getEnabledMappedTransitions(net, nextState, mapping, nextClass);
			
			if (nextTaskEnabledTransitions == null || nextTaskEnabledTransitions.size() > 0)
				filtered.add(t);
		}
		
		return filtered;
	}

	public boolean isExecutableModelElement(Transition element, Marking state) {
		return PetrinetReplayUtils.getEnabledTransitions(net, state).contains(element);
	}
	
	public boolean isExecutableLogElement(XEventClass element, Marking state) {
		return PetrinetReplayUtils.isTaskReachableByInvisiblePaths(net, mapping, element, state);		
	}

	public Set<Transition> getOrphanedModelElements() {
		Set<Transition> orphans = new HashSet<Transition>();
		for (Transition tr : mapping.getTransitions()) {
			if (!mapping.transitionIsInvisible(tr) && !mapping.transitionHasEvent(tr))
				orphans.add(tr);
		}
		return orphans;
	}

	public Set<XEventClass> getOrphanedLogElements() {
		Set<XEventClass> orphans = new HashSet<XEventClass>();
		for (XEventClass ec : mapping.getEventClasses()) {
			if (!mapping.eventHasTransition(ec))
				orphans.add(ec);
		}
		return orphans;
	}

	public ProcessReplayModel<Transition, XEventClass, Marking> copy() {
		return new PetrinetReplayModel(this);
	}
	
}
