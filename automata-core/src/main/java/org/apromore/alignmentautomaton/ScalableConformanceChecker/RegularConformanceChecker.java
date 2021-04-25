package org.apromore.alignmentautomaton.ScalableConformanceChecker;

import java.util.PriorityQueue;
import org.apromore.alignmentautomaton.automaton.Automaton;
import org.apromore.alignmentautomaton.automaton.Transition;
import org.apromore.alignmentautomaton.psp.Node;
import org.apromore.alignmentautomaton.psp.PSP;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntDoubleHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;

public class RegularConformanceChecker {

  Automaton logAutomaton = null;

  Automaton modelAutomaton = null;

  PSP psp;

  PriorityQueue<Node> queue = null;

  IntDoubleHashMap visited = null;

  Node currentNode = null;

  IntArrayList trace = null;

  int traceLength = -1;

  double actMin = -1;

  public RegularConformanceChecker(Automaton logAutomaton, Automaton modelAutomaton) {
    this.logAutomaton = logAutomaton;
    this.modelAutomaton = modelAutomaton;
    this.psp = new PSP(logAutomaton, modelAutomaton);
    this.calcOneOptimalForLog();
  }

  public void calcOneOptimalForLog() {
    for (IntIntHashMap finalConfig : logAutomaton.configCasesMapping().keySet()) {
      for (IntArrayList trace : logAutomaton.configCasesMapping().get(finalConfig)) {
        calcOneOptimalForTrace(trace);
      }
    }
  }

  public void calcOneOptimalForTrace(IntArrayList traceLabels) {
    queue = new PriorityQueue();
    visited = new IntDoubleHashMap();
    currentNode = psp.sourceNode();
    queue.offer(currentNode);
    trace = traceLabels;
    traceLength = trace.size();
    actMin = traceLength + modelAutomaton.minNumberOfModelMoves();
    boolean isOptimal = false;
    while (true) {
      currentNode = queue.poll();
      if (isOptimal()) {
        isOptimal = true;
        break;
      }
      queueMoves();
      if (queue.isEmpty() || queue.peek().weight() >= actMin) {
        break;
      }
    }

  }

  public boolean isOptimal() {
    return currentNode.stLog().isFinal() && currentNode.stModel().isFinal() && (currentNode.tracePosition
        == traceLength);
  }

  public void queueMoves() {
    int expectedTraceLabel = trace.get(currentNode.tracePosition);
    int trLogLabel = -1, trModelLabel = -1;
    for (Transition trLog : currentNode.stLog().outgoingTransitions()) {
      trLogLabel = trLog.eventID();
      if (trLogLabel == expectedTraceLabel) {
        for (Transition trModel : currentNode.stModel().outgoingTransitions()) {
          trModelLabel = trModel.eventID();
          if (trLogLabel == trModelLabel) {
            queueMove(trLog, trModel);
          }
        }
        queueMove(trLog, null);
      }
    }
    for (Transition trModel : currentNode.stModel().outgoingTransitions()) {
      queueMove(null, trModel);
    }
  }

  public void queueMove(Transition trLog, Transition trModel) {
    Node potentialNode = new Node(currentNode, trLog, trModel);
    if (potentialNode.weight() < actMin) {
      if (visited.containsKey(potentialNode.hashCode())) {
        if (visited.get(potentialNode.hashCode()) <= potentialNode.weight()) {
          return;
        }
      }
      queue.offer(potentialNode);
    }
  }
}
