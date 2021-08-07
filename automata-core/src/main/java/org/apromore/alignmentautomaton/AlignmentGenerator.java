package org.apromore.alignmentautomaton;

import org.apromore.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.bpmn.Bpmn;

public interface AlignmentGenerator {

  AlignmentResult computeAlignment(Petrinet petriNet, Marking marking, XLog xLog);

  AlignmentResult computeAlignment(BPMNDiagram bpmn, XLog xLog);
}
