package org.processmining.plugins.neconformance.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.unimelb.negativeEventsClasses.PetrinetLogMapper;
import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.PetrinetSemantics;
import org.processmining.models.semantics.petrinet.impl.PetrinetSemanticsFactory;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;

public class PetrinetReplayUtils extends PetrinetUtils {
	public static boolean isInvisibleTransition(Transition t, PetrinetLogMapper m) {
		return isInvisibleTransition(t) || m.transitionIsInvisible(t);
	}

	public static void setInvisibleTransitions(Petrinet petriNet, PetrinetLogMapper m) {
		for (Transition t : petriNet.getTransitions()) {
			t.setInvisible(isInvisibleTransition(t, m));
		}
	}
	
	public static Marking getMarkingAfterFire(Petrinet net, Marking current, Transition fired) {
		PetrinetSemantics semantics = PetrinetSemanticsFactory.regularPetrinetSemantics(Petrinet.class);
		semantics.initialize(net.getTransitions(), current);
		
		// Take weight into account
		Marking requiredMarking = new Marking();
		Set<Place> beforePlaces = new HashSet<Place>();
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> aInEdges = net.getInEdges(fired);
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> inEdge : aInEdges) {
			Place before = (Place) inEdge.getSource();
			beforePlaces.add(before);
			int weight = ((Arc) inEdge).getWeight();
			requiredMarking.add(before, weight);
		}
		
		Marking forcedMarking = new Marking(current);
		for (Place p : beforePlaces) {
			int missing = requiredMarking.occurrences(p) - forcedMarking.occurrences(p);
			if (missing > 0)
				forcedMarking.add(p, missing);
		}
		semantics.setCurrentState(forcedMarking);
		
		try {
			semantics.executeExecutableTransition(fired);
		} catch (IllegalTransitionException e) {
			System.err.println("DANGER! Illegal transition exception occurred when trying to derive next state!");
			System.err.println("Current marking is:");
			System.err.println(current);
			System.err.println("All before places are:");
			System.err.println(beforePlaces);
			System.err.println("Required marking is:");
			System.err.println(requiredMarking);
			System.err.println("Constructed marking is:");
			System.err.println(forcedMarking);
			System.err.println("Transition was:");
			System.err.println(fired);
			System.err.println("I'm putting the output places in the current marking...");
			Marking newMarking = current;
			newMarking.addAll(PetrinetReplayUtils.getPlacesAfterTransition(net, fired));
			return newMarking;
		}
		
		Marking newMarking = semantics.getCurrentState();
		
		return newMarking;
	}
	
	public static Set<Transition> getEnabledTransitions(Petrinet net, Marking current) {
		PetrinetSemantics semantics = PetrinetSemanticsFactory.regularPetrinetSemantics(Petrinet.class);
		semantics.initialize(net.getTransitions(), current);
		Set<Transition> fireables = new HashSet<Transition>(semantics.getExecutableTransitions());
		return fireables;
	}
	
	public static Set<Transition> getMappedTransitions(PetrinetLogMapper mapping, XEventClass task) {
		return new HashSet<Transition>(mapping.getTransitionsForActivity(task.toString()));
	}

	public static Set<Transition> getEnabledMappedTransitions(Petrinet net, Marking current, PetrinetLogMapper mapping, XEventClass task) {
		Collection<Transition> allTransitions = PetrinetReplayUtils.getMappedTransitions(mapping, task);
		allTransitions.retainAll(PetrinetReplayUtils.getEnabledTransitions(net, current));
		return new HashSet<Transition>(allTransitions);
	}
	
	public static Set<Transition> getEnabledInvisibleTransitions(Petrinet net, Marking current, PetrinetLogMapper mapping) {
		Collection<Transition> enabled = PetrinetReplayUtils.getEnabledTransitions(net, current);
		HashSet<Transition> invisibles = new HashSet<Transition>();
		for (Transition t : enabled) {
			if (PetrinetReplayUtils.isInvisibleTransition(t, mapping))
				invisibles.add(t);
		}
		return invisibles;
	}

	public static Set<Transition> getInvisibleTransitions(Petrinet net, PetrinetLogMapper mapping) {
		HashSet<Transition> invisibles = new HashSet<Transition>();
		for (Transition t : net.getTransitions()) {
			if (PetrinetReplayUtils.isInvisibleTransition(t, mapping))
				invisibles.add(t);
		}
		return invisibles;
	}
	
	public static Set<Transition> getVisibleTransitions(Petrinet net, PetrinetLogMapper mapping) {
		HashSet<Transition> visibles = new HashSet<Transition>();
		for (Transition t : net.getTransitions()) {
			if (!PetrinetReplayUtils.isInvisibleTransition(t, mapping))
				visibles.add(t);
		}
		return visibles;
	}
		
	public static List<Transition> getInvisibleTaskReachabilityPaths(
			Petrinet petriNet,
			PetrinetLogMapper mapping,
			XEventClass task,
			Marking marking) {
		Marking initialMarking = getInitialMarking(petriNet, marking);
		return getInvisibleTaskReachabilityPaths(
				petriNet,
				mapping,
				task,
				initialMarking,
				50,
				true, true, false);
	}
	
	public static List<Transition> getInvisibleTaskReachabilityPaths(
			Petrinet petriNet,
			PetrinetLogMapper mapping,
			XEventClass task,
			Marking marking,
			int maxStates,
			boolean useImmediate,
			boolean useOne,
			boolean useBFS) {
		
		// Is the task reachable now?
		if (useImmediate && getEnabledMappedTransitions(petriNet, marking, mapping, task).size() > 0)
			return new ArrayList<Transition>();
		
		// Is the task reachable in one step -- quick optimization
		List<Transition> single = getSingleInvisibleTaskReachabilityPath(petriNet, mapping, task, marking);
		if (useOne && single != null)
			return single;
		
		// Check complete exploration -- limited number of states
		return getInvisibleTaskReachabilityPath(petriNet, mapping, task, marking, maxStates, useBFS);
		
		//return null;
	}

	public static List<Transition> getInvisibleTaskReachabilityPath(
			Petrinet petriNet,
			PetrinetLogMapper mapping,
			XEventClass task,
			Marking marking,
			int maxStates,
			boolean useBFS) {
		Set<Marking> visitedMarkings = new HashSet<Marking>();
		visitedMarkings.add(marking);
		List<Marking> queuedMarkings = new ArrayList<Marking>();
		queuedMarkings.add(marking);
		Map<Marking, List<Transition>> pathToMarking = new HashMap<Marking, List<Transition>>();
		pathToMarking.put(marking, new ArrayList<Transition>());
		
		int statesChecked = 0;
		
		while (queuedMarkings.size() > 0) {
			statesChecked++;
			if (maxStates > 0 && statesChecked > maxStates) {
				//System.err.println("* Max state exploration limit ("+maxStates+") reached: "+task.toString());
				return null;
			}
			
			Marking markingTodo = queuedMarkings.remove(0);

			// Is the task enabled in this state?
			if (getEnabledMappedTransitions(petriNet, markingTodo, mapping, task).size() > 0)
				return pathToMarking.get(markingTodo);
			
			// Try other states
			Set<Transition> fireableTransitions = getEnabledInvisibleTransitions(petriNet, markingTodo, mapping);
			for (Transition fire : fireableTransitions) {
				Marking after = getMarkingAfterFire(petriNet, markingTodo, fire);
				if (!visitedMarkings.contains(after)) {
					if (useBFS) {
						queuedMarkings.add(after); // BFS: append
					} else {
						queuedMarkings.add(0, after); // DFS: prepend
					}
					List<Transition> newPath = new ArrayList<Transition>(pathToMarking.get(markingTodo));
					newPath.add(fire);
					pathToMarking.put(after, newPath);
					visitedMarkings.add(after);
				}
			}
	
		}
		
		return null;
	}

	public static List<Transition> getSingleInvisibleTaskReachabilityPath(
			Petrinet petriNet,
			PetrinetLogMapper mapping,
			XEventClass task,
			Marking marking) {
		List<Transition> path = new ArrayList<Transition>();
		
		Set<Transition> fireableTransitions = getEnabledInvisibleTransitions(petriNet, marking, mapping);
		for (Transition fire : fireableTransitions) {
			Marking after = getMarkingAfterFire(petriNet, marking, fire);
			if (getEnabledMappedTransitions(petriNet, after, mapping, task).size() > 0) {
				path.add(fire);
				return path;
			}
		}
		return null;
	}

	public static boolean isTaskReachableByInvisiblePaths(Petrinet net,
			PetrinetLogMapper mapping, XEventClass element, Marking state) {
		return getInvisibleTaskReachabilityPaths(net, mapping, element, state) != null;
	}
	
}
