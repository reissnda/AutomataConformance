package au.unimelb.negativeEventsClasses;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

public class PetrinetUtils {
    public PetrinetUtils() {
    }

    public static boolean isInvisibleTransition(Transition t) {
        return t.getLabel().equals("") || t.isInvisible();
    }

    public static Set<String> getDistinctLabels(Petrinet petriNet) {
        Set<String> labels = new HashSet();
        Iterator var3 = petriNet.getTransitions().iterator();

        while(var3.hasNext()) {
            Transition transition = (Transition)var3.next();
            if (!isInvisibleTransition(transition)) {
                labels.add(transition.getLabel());
            }
        }

        return labels;
    }

    public static Set<Place> getPlacesBeforeTransition(Petrinet petriNet, Transition t) {
        Set<Place> places = new HashSet();
        Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> aInEdges = petriNet.getInEdges(t);
        Iterator var5 = aInEdges.iterator();

        while(var5.hasNext()) {
            PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> inEdge = (PetrinetEdge)var5.next();
            places.add((Place)inEdge.getSource());
        }

        return places;
    }

    public static Set<Place> getPlacesAfterTransition(Petrinet petriNet, Transition t) {
        Set<Place> places = new HashSet();
        Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> aOutEdges = petriNet.getOutEdges(t);
        Iterator var5 = aOutEdges.iterator();

        while(var5.hasNext()) {
            PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> outEdge = (PetrinetEdge)var5.next();
            places.add((Place)outEdge.getTarget());
        }

        return places;
    }

    public static Transition getTransitionsById(Petrinet petriNet, String id) {
        Iterator var3 = petriNet.getTransitions().iterator();

        while(var3.hasNext()) {
            Transition t = (Transition)var3.next();
            if (t.getId().toString().equals(id)) {
                return t;
            }
        }

        return null;
    }

    public static Set<Transition> getTransitionsByLabel(Petrinet petriNet, String label) {
        Set<Transition> transitions = new HashSet();
        Iterator var4 = petriNet.getTransitions().iterator();

        while(var4.hasNext()) {
            Transition t = (Transition)var4.next();
            if (t.getLabel().equals(label)) {
                transitions.add(t);
            }
        }

        return transitions;
    }

    public static Set<Place> getStartPlacesByLabel(Petrinet petriNet, String label) {
        Set<Transition> transitions = getTransitionsByLabel(petriNet, label);
        Set<Place> startPlaces = getStartPlaces(petriNet);
        Iterator var5 = transitions.iterator();

        while(var5.hasNext()) {
            Transition t = (Transition)var5.next();
            Set<Place> beforePlaces = getPlacesBeforeTransition(petriNet, t);
            beforePlaces.retainAll(startPlaces);
            if (beforePlaces.size() > 0) {
                return beforePlaces;
            }
        }

        return null;
    }

    public static Set<Place> getStartPlaces(Petrinet petriNet) {
        Set<Place> startSet = new HashSet();
        Iterator var3 = petriNet.getPlaces().iterator();

        while(var3.hasNext()) {
            Place p = (Place)var3.next();
            if (petriNet.getInEdges(p).size() == 0) {
                startSet.add(p);
            }
        }

        return startSet;
    }

    public static Set<Place> getEndPlaces(Petrinet petriNet) {
        Set<Place> endSet = new HashSet();
        Iterator var3 = petriNet.getPlaces().iterator();

        while(var3.hasNext()) {
            Place p = (Place)var3.next();
            if (petriNet.getOutEdges(p).size() == 0) {
                endSet.add(p);
            }
        }

        return endSet;
    }

    public static boolean isMarkingHasSinglePlace(Marking m, Set<Place> places) {
        Iterator var3 = places.iterator();

        while(var3.hasNext()) {
            Place p = (Place)var3.next();
            if (m.contains(p)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isMarkingHasAllPlaces(Marking m, Set<Place> places) {
        Iterator var3 = places.iterator();

        while(var3.hasNext()) {
            Place p = (Place)var3.next();
            if (!m.contains(p)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isMarkingOnlyHasPlaces(Marking m, Set<Place> places) {
        Iterator var3 = m.toList().iterator();

        while(var3.hasNext()) {
            Place p = (Place)var3.next();
            if (!places.contains(p)) {
                return false;
            }
        }

        return true;
    }

    public static Marking getInitialMarking(Petrinet petriNet) {
        return getInitialMarking(petriNet, (Marking)null);
    }

    public static Marking getInitialMarking(Petrinet petriNet, Marking initialMarking) {
        if (initialMarking == null) {
            initialMarking = new Marking();
            initialMarking.addAll(getStartPlaces(petriNet));
        }

        return initialMarking;
    }

    public static Marking getFinalMarking(Petrinet pnet)
    {
        Marking finalMarking = new Marking();
        for (Place p : pnet.getPlaces()) {
            if (pnet.getOutEdges(p).isEmpty()) {
                finalMarking.add(p);
            }
        }
        return finalMarking;
    }
}
