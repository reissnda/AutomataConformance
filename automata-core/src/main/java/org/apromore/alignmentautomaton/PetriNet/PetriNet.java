package org.apromore.alignmentautomaton.PetriNet;

import java.io.IOException;
import java.io.PrintWriter;
import static nl.tue.storage.hashing.impl.MurMur3HashCodeProvider.C1;
import static nl.tue.storage.hashing.impl.MurMur3HashCodeProvider.C2;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

public class PetriNet {

  String conceptname = "concept:name";

  FastList<Place> places = new FastList<Place>();

  FastList<Transition> transitions = new FastList<Transition>();

  UnifiedSet<String> alphabet = new UnifiedSet<>();

  IntObjectHashMap<IntHashSet> pOutEdges = new IntObjectHashMap<>();

  IntObjectHashMap<IntHashSet> pInEdges = new IntObjectHashMap<>();

  IntObjectHashMap<IntHashSet> tOutEdges = new IntObjectHashMap<>();

  IntObjectHashMap<IntHashSet> tInEdges = new IntObjectHashMap<>();

  DoubleMatrix2D tpFlows;

  DoubleMatrix2D ptFlows;

  DoubleMatrix2D incidenceMatrix;

  IntIntHashMap initialMarking, finalMarking;

  //Empty PetriNet
  public PetriNet() {
  }

  //Create Event net from a trace
  public PetriNet(XTrace trace) {
    ptFlows = DoubleFactory2D.sparse.make(trace.size() + 1, trace.size(), 0);
    tpFlows = DoubleFactory2D.sparse.make(trace.size() + 1, trace.size(), 0);
    places.add(new Place());
    for (int pos = 0; pos < trace.size(); pos++) {
      transitions
          .add(new Transition(((XAttributeLiteral) trace.get(pos).getAttributes().get(conceptname)).getValue(), true));
      places.add(new Place());
      addPTflow(pos, pos);
      addTPflow(pos, pos + 1);
    }
    calculateIncidenceMatrix();
    setInitialAndFinalMarking();
  }

  //create Event net from a decoded trace
  public PetriNet(IntArrayList trace, IntObjectHashMap<String> dictionary) {
    ptFlows = DoubleFactory2D.sparse.make(trace.size() + 1, trace.size(), 0);
    tpFlows = DoubleFactory2D.sparse.make(trace.size() + 1, trace.size(), 0);
    places.add(new Place());
    for (int pos = 0; pos < trace.size(); pos++) {
      transitions.add(new Transition(dictionary.get(trace.get(pos)), true));
      places.add(new Place());
      addPTflow(pos, pos);
      addTPflow(pos, pos + 1);
    }
    calculateIncidenceMatrix();
    setInitialAndFinalMarking();
  }

  public int addPlace(String label) {
    Place p = new Place(label);
    places.add(p);
    pOutEdges.put(p.id(), new IntHashSet());
    pInEdges.put(p.id(), new IntHashSet());
    return p.id;
  }

  public int addTransition(String label, boolean isVisible) {
    alphabet.add(label);
    Transition tr = new Transition(label, isVisible);
    transitions.add(tr);
    tOutEdges.put(tr.id(), new IntHashSet());
    tInEdges.put(tr.id(), new IntHashSet());
    return tr.id;
  }

  public void addPTflow(int placeID, int transitionID) {
    if (ptFlows == null) {
      ptFlows = DoubleFactory2D.sparse.make(this.places.size(), this.transitions.size(), 0);
    }
    ptFlows.set(placeID, transitionID, ptFlows.get(placeID, transitionID) + 1);
    IntHashSet set = null;
    pOutEdges.get(placeID).add(transitionID);
    tInEdges.get(transitionID).add(placeID);
  }

  public void addTPflow(int transitionID, int placeID) {
    if (tpFlows == null) {
      tpFlows = DoubleFactory2D.sparse.make(this.places.size(), this.transitions.size(), 0);
    }
    tpFlows.set(placeID, transitionID, tpFlows.get(placeID, transitionID) + 1);
    IntHashSet set = null;
    tOutEdges.get(transitionID).add(placeID);
    pInEdges.get(placeID).add(transitionID);
  }

  public void calculateIncidenceMatrix() {
    incidenceMatrix = DoubleFactory2D.sparse.make(this.places.size(), this.transitions.size(), 0);
    for (int transitionID = 0; transitionID < transitions.size(); transitionID++) {
      for (int placeID = 0; placeID < places.size(); placeID++) {
        incidenceMatrix
            .set(placeID, transitionID, tpFlows.get(placeID, transitionID) - ptFlows.get(placeID, transitionID));
      }
    }
  }

  public void setInitialAndFinalMarking() {
    setInitialMarking();
    setFinalMarking();
  }

  public void setInitialMarking() {
    initialMarking = new IntIntHashMap();
    for (int p = 0; p < places.size(); p++) {
      if (pInEdges.get(p).isEmpty()) {
        initialMarking.addToValue(p, 1);
      }
    }
  }

  public void setInitialMarking(IntIntHashMap initialMarking) {
    this.initialMarking = initialMarking;
  }

  public void setFinalMarking() {
    finalMarking = new IntIntHashMap();
    for (int p = 0; p < places.size(); p++) {
      if (!pOutEdges.containsKey(p)) {
        finalMarking.addToValue(p, 1);
      }
    }
  }

  public String getMarkingLabel(IntIntHashMap marking) {
    String markingLabel = "[";
    for (int key : marking.keySet().toArray()) {
      markingLabel += places.get(key).label() + " * " + marking.get(key) + ", ";
    }
    return markingLabel.substring(0, markingLabel.length() - 2) + "]";
  }

  public IntIntHashMap fire(int transition, IntIntHashMap marking) {
    if (!isEnabled(transition, marking)) {
      return null;
    }
    return fireUnchecked(transition, marking);
  }

  public IntIntHashMap fireUnchecked(int transition, IntIntHashMap marking) {
    IntIntHashMap newMarking = new IntIntHashMap(marking);
    for (int inPlace : tInEdges.get(transition).toArray()) {
      newMarking.addToValue(inPlace, -1);
    }
    for (int outPlace : tOutEdges.get(transition).toArray()) {
      newMarking.addToValue(outPlace, 1);
    }
    for (int key : newMarking.keySet().toArray()) {
      if (newMarking.get(key) == 0) {
        newMarking.remove(key);
      }
    }
    return newMarking;
  }

  public IntHashSet getEnabledTransitions(IntIntHashMap marking) {
    IntHashSet enabledTransitions = new IntHashSet();
    for (int place : marking.keySet().toArray()) {
      for (int outTr : pOutEdges.get(place).toArray()) {
        if (isEnabled(outTr, marking)) {
          enabledTransitions.add(outTr);
        }
      }
    }
    return enabledTransitions;
  }

  public boolean isEnabled(int transition, IntIntHashMap marking) {
    for (int prePlace : tInEdges.get(transition).toArray()) {
      if (marking.get(prePlace) < 1) {
        return false;
      }
    }
    return true;
  }

  public void toDot(PrintWriter pw) throws IOException {
    pw.println("digraph fsm {");
    pw.println("rankdir=LR;");
    pw.println("node [shape=circle,style=filled, fillcolor=white]");

    for (int p = 0; p < places.size(); p++) {
      Place pl = places.get(p);
      pw.printf("%d [label=\"%s\"];%n", pl.id(), pl.label());
    }

    for (int t = 0; t < transitions.size(); t++) {
      Transition tr = transitions.get(t);
      pw.printf("%d [label=\"%s\", shape=\"box\"];%n", tr.id() + places.size(), tr.label());
    }

    for (int p = 0; p < places.size(); p++) {
      if (pOutEdges.containsKey(p)) {
        for (int t : pOutEdges.get(p).toArray()) {
          pw.printf("%d -> %d;%n", places.get(p).id(), transitions.get(t).id() + places.size());
        }
      }
    }

    for (int t = 0; t < transitions.size(); t++) {
      if (tOutEdges.containsKey(t)) {
        for (int p : tOutEdges.get(t).toArray()) {
          pw.printf("%d -> %d;%n", transitions.get(t).id() + places.size(), places.get(p).id());
        }
      }
    }

    pw.println("}");
  }

  public void toDot(String fileName) throws IOException {
    PrintWriter pw = new PrintWriter(fileName);
    toDot(pw);
    pw.close();
  }

  public IntIntHashMap getInitialMarking() {
    return this.initialMarking;
  }

  public IntIntHashMap getFinalMarking() {
    return this.finalMarking;
  }

  public DoubleMatrix2D getIncidenceMatrix() {
    return this.incidenceMatrix;
  }

  public FastList<Place> getPlaces() {
    return this.places;
  }

  public FastList<Transition> getTransitions() {
    return this.transitions;
  }

  public static int compress(IntIntHashMap marking) {
    int[] data = new int[marking.size() * 2];
    int j = 0;
    for (int i = 0; i < marking.size(); i++) {
      data[j++] = marking.keySet().toArray()[i];
      data[j++] = marking.values().toArray()[i];
    }

    //data[2] = this.stateModelrep;
    int hash = 0;
    final int len = data.length;

    int k1;
    for (int i = 0; i < len - 1; i++) {
      // little endian load order
      k1 = data[i];
      k1 *= C1;

      k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
      k1 *= C2;

      hash ^= k1;

      hash = (hash << 13) | (hash >>> 19); // ROTL32(h1,13);
      hash = hash * 5 + 0xe6546b64;

    }

    // tail
    k1 = data[len - 1];
    k1 *= C1;
    k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
    k1 *= C2;
    hash ^= k1;

    // finalization
    hash ^= len;

    // fmix(h1);
    hash ^= hash >>> 16;
    hash *= 0x85ebca6b;
    hash ^= hash >>> 13;
    hash *= 0xc2b2ae35;
    hash ^= hash >>> 16;

    return hash;
  }
}
