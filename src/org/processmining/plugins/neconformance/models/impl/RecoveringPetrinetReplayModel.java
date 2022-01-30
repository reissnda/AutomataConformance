package org.processmining.plugins.neconformance.models.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.neconformance.utils.PetrinetReplayUtils;
import au.unimelb.negativeEventsClasses.PetrinetLogMapper;

public class RecoveringPetrinetReplayModel extends PetrinetReplayModel {

	protected boolean greedyInvisibles;
	
	public RecoveringPetrinetReplayModel(Petrinet net, Marking initialMarking, PetrinetLogMapper mapping) {
		super(net, initialMarking, mapping);
	}
	
	public RecoveringPetrinetReplayModel(RecoveringPetrinetReplayModel toClone) {
		this(toClone.getPetrinet(), toClone.getInitialState(), toClone.getMapping());
		this.setGreedyInvisibles(toClone.isGreedyInvisibles());
	}
	
	public void reset() {
		super.reset();
		this.greedyInvisibles = false;
	}

	public void replay(List<XEventClass> trace) {
		this.reset();
		
		int currentSequencePosition = 0;
		while (currentSequencePosition < trace.size()) {
			XEventClass currentClass = trace.get(currentSequencePosition);
			XEventClass nextClass = (currentSequencePosition < trace.size() - 1)
					? trace.get(currentSequencePosition + 1)
					: null;
			
			Set<Transition> MT = new HashSet<Transition>();
			Set<Transition> ET = new HashSet<Transition>();
			Set<Transition> VT = new HashSet<Transition>();
			Set<Transition> IT = new HashSet<Transition>();
			Set<Transition> CET = new HashSet<Transition>();
			Set<Transition> NET = new HashSet<Transition>();
			
			MT = PetrinetReplayUtils.getMappedTransitions(mapping, currentClass);
			ET = PetrinetReplayUtils.getEnabledTransitions(net, currentMarking);
			VT = PetrinetReplayUtils.getVisibleTransitions(net, mapping);
			IT = PetrinetReplayUtils.getInvisibleTransitions(net, mapping);
			CET = filterOnNextTaskEnabler(net.getTransitions(), currentClass);
			NET = filterOnNextTaskEnabler(net.getTransitions(), nextClass);
			
			ReplayMove move = null;
			Transition t = null;
			XEventClass c = null;
			boolean f = false;
			Transition r;
			
			if ((r = getRandomTransitionFromIntersect(MT, ET, NET)) != null) {
				t = r;
				c = currentClass;
				move = ReplayMove.BOTH_SYNCHRONOUS;
			} else if ((r = getRandomTransitionFromIntersect(MT, ET)) != null) {
				t = r;
				c = currentClass;
				move = ReplayMove.BOTH_SYNCHRONOUS;
			} else if ((r = getRandomTransitionFromIntersect(IT, ET, CET)) != null) {
				t = r;
				c = null;
				move = ReplayMove.MODELONLY_UNOBSERVABLE;
			} else if ((r = getRandomTransitionFromIntersect(MT, NET)) != null) {
				t = r;
				c = currentClass;
				move = ReplayMove.BOTH_FORCED;
			} else if ((r = getRandomTransitionFromIntersect(MT)) != null) {
				t = r;
				c = currentClass;
				move = ReplayMove.BOTH_FORCED;
			} else if ((r = getRandomTransitionFromIntersect(VT, ET, CET)) != null) {
				t = r;
				c = null;
				move = ReplayMove.MODELONLY_SKIPPED;
			} else if ((r = getRandomTransitionFromIntersect(IT, CET)) != null) {
				t = r;
				c = null;
				move = ReplayMove.BOTH_FORCED;
			} else if ((r = getRandomTransitionFromIntersect(VT, CET)) != null) {
				t = r;
				c = null;
				move = ReplayMove.MODELONLY_SKIPPED;
			} else {
				t = null;
				c = currentClass;
				move = ReplayMove.LOGONLY_INSERTED;
			} 
			
			Marking m = currentMarking;
			if (t != null)
				m = PetrinetReplayUtils.getMarkingAfterFire(net, m, t);
			if (c != null)
				currentSequencePosition++;
			this.addReplayStep(move, t, c, m);
			this.transitionIsForced.add(f);
			this.currentMarking = m;
		} // end for
	}
	
	@SafeVarargs
	private final Transition getRandomTransitionFromIntersect(Set<Transition>... choices) {
		Set<Transition> total = choices[0];
		for (Set<Transition> c : choices)
			total.retainAll(c);
		if (total.size() == 0) return null;
		return getRandomTransition(total);
	}
	
	private Transition getRandomTransition(Set<Transition> choices) {
		int index = generator.nextInt(choices.size());
		return (Transition) choices.toArray()[index];
	}

	private Set<Transition> filterOnNextTaskEnabler(Collection<Transition> collection, XEventClass eclass) {
		HashSet<Transition> filtered = new HashSet<Transition>();
		if (eclass == null) {
			return filtered;
		}
		
		for (Transition t : collection) {
			Marking nextState = PetrinetReplayUtils.getMarkingAfterFire(net, currentMarking, t);
			Set<Transition> nextTaskEnabledTransitions =  PetrinetReplayUtils.getEnabledMappedTransitions(net, nextState, mapping, eclass);
			
			if (nextTaskEnabledTransitions == null || nextTaskEnabledTransitions.size() > 0)
				filtered.add(t);
		}
		
		return filtered;
	}

	public boolean isGreedyInvisibles() {
		return greedyInvisibles;
	}

	public void setGreedyInvisibles(boolean greedyInvisibles) {
		this.greedyInvisibles = greedyInvisibles;
	}
	
}
