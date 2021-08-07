package org.apromore.alignmentautomaton.PetriNet;

public class SynchronousTransition {

  private static int ID = 0;

  int id = ID++;

  boolean isVisible;

  String label;

  SynchronousNet.op operation;

  public SynchronousTransition(Transition eventTr, Transition processTr) {
    if (eventTr != null) {
      if (processTr == null) {
        operation = SynchronousNet.op.lhide;
      } else {
        operation = SynchronousNet.op.match;
      }
      isVisible = true;
      label = eventTr.label;
    } else {
      operation = SynchronousNet.op.rhide;
      isVisible = processTr.isVisible;
      label = processTr.label;
    }
  }

  public int id() {
    return this.id;
  }

  public String label() {
    return this.label;
  }

  public SynchronousNet.op operation() {
    return this.operation;
  }

  public String opAndLabel() {
    return "(" + this.operation + ", " + this.label + ")";
  }
}
