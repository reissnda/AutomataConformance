package org.apromore.alignmentautomaton.ScalableConformanceChecker;

import org.apromore.alignmentautomaton.psp.Node;

public class ReducedResult {

  private final Node finalNode;

  private final double numStates;

  private final double queuedStates;

  private final double time;

  public ReducedResult(Node finalNode, double numStates, double queuedStates, double time) {
    this.finalNode = finalNode;
    this.numStates = numStates;
    this.queuedStates = queuedStates;
    this.time = time;
  }

  public Node getFinalNode() {
    return this.finalNode;
  }

  public double getNumStates() {
    return this.numStates;
  }

  public double getNumQueuedStates() {
    return this.queuedStates;
  }

  public double getTime() {
    return this.time;
  }
}
