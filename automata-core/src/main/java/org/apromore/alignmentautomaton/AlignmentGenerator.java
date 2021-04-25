package org.apromore.alignmentautomaton;

import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

public interface AlignmentGenerator {

  AlignmentResult computeAlignment(Petrinet petriNet, Marking marking, XLog xLog);
}
