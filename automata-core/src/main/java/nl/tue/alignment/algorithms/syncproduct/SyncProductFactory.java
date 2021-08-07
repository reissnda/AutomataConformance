package nl.tue.alignment.algorithms.syncproduct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.TByteList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import nl.tue.astar.Trace;
import nl.tue.astar.util.LinearTrace;
import nl.tue.astar.util.PartiallyOrderedTrace;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

public class SyncProductFactory {

  private static class MapWrap<K> {

    private final Map<K, Integer> map1;

    private final TObjectIntMap<K> map2;

    private final int defaultValue;

    private MapWrap(Map<K, Integer> map1, TObjectIntMap<K> map2, int defaultValue) {
      this.map1 = map1;
      this.map2 = map2;
      this.defaultValue = defaultValue;
    }

    public MapWrap(Map<K, Integer> map1, int defaultValue) {
      this(map1, null, defaultValue);
    }

    public MapWrap(TObjectIntMap<K> map2, int defaultValue) {
      this(null, map2, defaultValue);
    }

    public MapWrap(int defaultValue) {
      this(null, null, defaultValue);
    }

    public int get(K key) {
      if (map1 != null && map1.containsKey(key)) {
        return map1.get(key);
      } else if (map2 != null && map2.containsKey(key)) {
        return map2.get(key);
      } else {
        return defaultValue;
      }
    }
  }

  private static class StringList {

    private String[] list;

    int size = 0;

    public StringList(int capacity) {
      list = new String[capacity];
    }

    public void add(String s) {
      ensureCapacity(size);
      list[size] = s;
      size++;
    }

    private void ensureCapacity(int insertAt) {
      if (list.length <= insertAt) {
        list = Arrays.copyOf(list, list.length * 2);
      }
    }

    public void trunctate(int size) {
      this.size = size;
      Arrays.fill(list, size, list.length, null);
    }

    public String get(int index) {
      return list[index];
    }

    public int size() {
      return size;
    }

    public String[] asArray() {
      return Arrays.copyOf(list, size);
    }

    public String toString() {

      int iMax = size - 1;
      if (iMax == -1) {
        return "[]";
      }

      StringBuilder b = new StringBuilder();
      b.append('[');
      for (int i = 0; ; i++) {
        b.append(list[i]);
        if (i == iMax) {
          return b.append(']').toString();
        }
        b.append(", ");
      }
    }
  }

  private final int transitions;

  private final TIntList t2mmCost;

  private final TIntList t2smCost;

  private final TObjectIntMap<Object> t2id;

  private final StringList t2name;

  private final List<int[]> t2input;

  private final List<int[]> t2output;

  private final TIntList t2eid;

  private final TByteList t2type;

  private final Transition[] t2transition;

  private final int classCount;

  private final int[] c2lmCost;

  private final TIntObjectMap<TIntSet> c2t;

  private final int places;

  private final StringList p2name;

  private final byte[] initMarking;

  private final byte[] finMarking;

  private final XEventClasses classes;

  private final TObjectIntMap<XEventClass> c2id;

  private final TIntList t2move;

  public SyncProductFactory(Petrinet net, XEventClasses classes, TObjectIntMap<XEventClass> c2id,
      TransEvClassMapping map, Marking initialMarking, Marking finalMarking) {
    this(net, classes, c2id, map, new MapWrap<Transition>(1), new MapWrap<XEventClass>(1), //
        new MapWrap<Transition>(0), initialMarking, finalMarking);
  }

  public SyncProductFactory(Petrinet net, XEventClasses classes, TObjectIntMap<XEventClass> c2id,
      TransEvClassMapping map, Map<Transition, Integer> mapTrans2Cost, Map<XEventClass, Integer> mapEvClass2Cost,
      Map<Transition, Integer> mapSync2Cost, Marking initialMarking, Marking finalMarking) {
    this(net, classes, c2id, map, new MapWrap<>(mapTrans2Cost, 1), new MapWrap<>(mapEvClass2Cost, 1), //
        new MapWrap<>(mapSync2Cost, 0), initialMarking, finalMarking);
  }

  public SyncProductFactory(Petrinet net, XEventClasses classes, TObjectIntMap<XEventClass> c2id,
      TransEvClassMapping map, TObjectIntMap<Transition> mapTrans2Cost, TObjectIntMap<XEventClass> mapEvClass2Cost,
      TObjectIntMap<Transition> mapSync2Cost, Marking initialMarking, Marking finalMarking) {
    this(net, classes, c2id, map, new MapWrap<>(mapTrans2Cost, 1), new MapWrap<>(mapEvClass2Cost, 1), //
        new MapWrap<>(mapSync2Cost, 0), initialMarking, finalMarking);
  }

  public SyncProductFactory(Petrinet net, XEventClasses classes, TObjectIntMap<XEventClass> c2id,
      TransEvClassMapping map, Map<Transition, Integer> mapTrans2Cost, Map<XEventClass, Integer> mapEvClass2Cost,
      Marking initialMarking, Marking finalMarking) {
    this(net, classes, c2id, map, new MapWrap<>(mapTrans2Cost, 1), new MapWrap<>(mapEvClass2Cost, 1), //
        new MapWrap<Transition>(0), initialMarking, finalMarking);
  }

  public SyncProductFactory(Petrinet net, XEventClasses classes, TObjectIntMap<XEventClass> c2id,
      TransEvClassMapping map, TObjectIntMap<Transition> mapTrans2Cost, TObjectIntMap<XEventClass> mapEvClass2Cost,
      Marking initialMarking, Marking finalMarking) {
    this(net, classes, c2id, map, new MapWrap<>(mapTrans2Cost, 1), new MapWrap<>(mapEvClass2Cost, 1), //
        new MapWrap<Transition>(0), initialMarking, finalMarking);
  }

  public static TObjectIntMap<XEventClass> createClass2ID(XEventClasses classes) {
    TObjectIntHashMap<XEventClass> c2id = new TObjectIntHashMap<>(classes.size(), 0.75f, -1);
    int id = 0;
    for (XEventClass clazz : classes.getClasses()) {
      c2id.put(clazz, id++);
    }
    return c2id;
  }

  private SyncProductFactory(Petrinet net, XEventClasses classes, TObjectIntMap<XEventClass> c2id,
      TransEvClassMapping map, MapWrap<Transition> mapTrans2Cost, MapWrap<XEventClass> mapEvClass2Cost,
      MapWrap<Transition> mapSync2Cost, Marking initialMarking, Marking finalMarking) {

    this.c2id = c2id;
    this.classes = classes;

    // find the highest class number
    int mx = 0;
    TObjectIntIterator<XEventClass> its = c2id.iterator();
    while (its.hasNext()) {
      its.advance();
      if (mx < its.value()) {
        mx = its.value();
      }
    }
    mx++;

    this.classCount = mx;
    c2lmCost = new int[classCount];
    c2t = new TIntObjectHashMap<>(this.classCount);
    for (XEventClass clazz : classes.getClasses()) {
      c2lmCost[c2id.get(clazz)] = mapEvClass2Cost.get(clazz);
    }

    transitions = net.getTransitions().size();
    t2mmCost = new TIntArrayList(transitions * 2);
    t2smCost = new TIntArrayList(transitions * 2);
    t2eid = new TIntArrayList(transitions * 2);
    t2type = new TByteArrayList(transitions * 2);
    t2name = new StringList(transitions * 2);
    t2input = new ArrayList<>(transitions * 2);
    t2output = new ArrayList<>(transitions * 2);
    t2transition = new Transition[transitions];
    t2move = new TIntArrayList(transitions * 2);

    places = net.getPlaces().size();
    p2name = new StringList(places * 2);
    TObjectIntMap<Place> p2id = new TObjectIntHashMap<>(net.getPlaces().size(), 0.75f, -1);
    t2id = new TObjectIntHashMap<>(net.getTransitions().size(), 0.75f, -1);

    // build list of move_model transitions
    Integer cost;
    Iterator<Transition> it = net.getTransitions().iterator();
    while (it.hasNext()) {
      Transition t = it.next();
      t2id.put(t, t2name.size());
      t2transition[t2name.size()] = t;
      t2move.add(t2name.size());

      // update mapping from event class to transitions
      XEventClass clazz = map.get(t);
      if (clazz != null) {
        TIntSet set = c2t.get(c2id.get(clazz));
        if (set == null) {
          set = new TIntHashSet(3);
          c2t.put(c2id.get(clazz), set);
        }
        set.add(t2name.size());
      }

      cost = mapTrans2Cost.get(t);
      t2mmCost.add(cost);
      cost = mapSync2Cost.get(t);
      t2smCost.add(cost);
      t2name.add(t.getLabel());
      t2eid.add(SyncProduct.NOEVENT);
      t2type.add(t.isInvisible() ? SyncProduct.TAU_MOVE : SyncProduct.MODEL_MOVE);

      TIntList input = new TIntArrayList(2 * net.getInEdges(t).size());
      int i = 0;
      for (PetrinetEdge<?, ?> e : net.getInEdges(t)) {
        Place p = (Place) e.getSource();
        int id = p2id.get(p);
        if (id == -1) {
          id = p2id.size();
          p2id.put(p, id);
          p2name.add(p.getLabel());
        }
        for (int w = 0; w < ((Arc) e).getWeight(); w++) {
          input.add(id);
          i++;
        }
      }
      t2input.add(input.toArray());

      TIntList output = new TIntArrayList(2 * net.getOutEdges(t).size());
      i = 0;
      for (PetrinetEdge<?, ?> e : net.getOutEdges(t)) {
        Place p = (Place) e.getTarget();
        int id = p2id.get(p);
        if (id == -1) {
          id = p2id.size();
          p2id.put(p, id);
          p2name.add(p.getLabel());
        }
        for (int w = 0; w < ((Arc) e).getWeight(); w++) {
          output.add(id);
          i++;
        }
      }
      t2output.add(output.toArray());
    }

    initMarking = new byte[p2name.size()];
    for (Place p : initialMarking) {
      int id = p2id.get(p);
      if (id >= 0) {
        initMarking[id]++;
      }
    }

    finMarking = new byte[p2name.size()];
    for (Place p : finalMarking) {
      int id = p2id.get(p);
      if (id >= 0) {
        finMarking[id]++;
      }
    }

  }

  public synchronized SyncProduct getSyncProductForEmptyTrace(List<Transition> transitionList) {
    return getLinearSyncProduct(new LinearTrace("Empty", 0), transitionList);
  }

  public Trace getTrace(XTrace xTrace, boolean partiallyOrderSameTimestamp) {
    String traceLabel = XConceptExtension.instance().extractName(xTrace);
    if (traceLabel == null) {
      traceLabel = "XTrace@" + Integer.toHexString(xTrace.hashCode());
    }

    if (partiallyOrderSameTimestamp) {
      return getPartiallyOrderedTrace(xTrace, traceLabel);
    } else {
      return getLinearTrace(xTrace, traceLabel);
    }
  }

  public synchronized SyncProduct getSyncProduct(XTrace xTrace, List<Transition> transitionList,
      boolean partiallyOrderSameTimestamp) {
    String traceLabel = XConceptExtension.instance().extractName(xTrace);
    if (traceLabel == null) {
      traceLabel = "XTrace@" + Integer.toHexString(xTrace.hashCode());
    }
    if (partiallyOrderSameTimestamp) {
      PartiallyOrderedTrace trace = getPartiallyOrderedTrace(xTrace, traceLabel);
      // Do the ranking on this trace.
      return getPartiallyOrderedSyncProduct(trace, transitionList);
    } else {
      return getLinearSyncProduct(getLinearTrace(xTrace, traceLabel), transitionList);
    }

  }

  private SyncProduct getLinearSyncProduct(LinearTrace trace, List<Transition> transitionList) {
    transitionList.clear();
    // for this trace, compute the log-moves
    // compute the sync moves
    for (int e = 0; e < trace.getSize(); e++) {
      //			XEventClass clazz = classes.getClassOf(trace.get(e));
      int cid = trace.get(e); // c2id.get(clazz);
      // add a place
      p2name.add("pe_" + e);
      // add log move
      t2name.add("e" + e + "(" + cid + ")");//clazz.toString());
      t2mmCost.add(c2lmCost[cid]);
      t2eid.add(e);
      t2type.add(SyncProduct.LOG_MOVE);
      t2move.add(transitions + e);

      TIntSet set = c2t.get(cid);
      if (set != null) {
        TIntIterator it = set.iterator();
        while (it.hasNext()) {
          // add sync move
          int t = it.next();
          t2name.add(t2name.get(t) + " + e" + e + "(" + cid + ")");
          t2mmCost.add(t2smCost.get(t));
          t2eid.add(e);
          t2type.add(SyncProduct.SYNC_MOVE);
          t2move.add(transitions + trace.getSize() + t);
        }
      }
    }
    if (trace.getSize() > 0) {
      p2name.add("pe_" + trace.getSize());
    }

    SyncProductImpl product = new SyncProductImpl(trace.getLabel(), //label
        this.classCount, // number of classes
        t2name.asArray(), //transition labels
        p2name.asArray(), // place labels
        t2eid.toArray(), //event numbers
        t2type.toArray(), //types
        t2move.toArray(), //moves
        t2mmCost.toArray());

    int t = 0;
    for (; t < transitions; t++) {
      // first the model moves
      product.setInput(t, t2input.get(t));
      product.setOutput(t, t2output.get(t));
      transitionList.add(t2transition[t]);
    }

    for (int e = 0; e < trace.getSize(); e++) {
      // then the log moves
      //			XEventClass clazz = classes.getClassOf(trace.get(e));
      int cid = trace.get(e);// c2id.get(clazz);
      product.setInput(t, places + e);
      product.setOutput(t, places + e + 1);
      transitionList.add(null);
      t++;

      TIntSet set = c2t.get(cid);
      if (set != null) {
        TIntIterator it = set.iterator();
        while (it.hasNext()) {
          // add sync move
          int t2 = it.next();
          product.setInput(t, t2input.get(t2));
          product.setOutput(t, t2output.get(t2));

          product.addToInput(t, (places + e));
          product.addToOutput(t, (places + e + 1));

          transitionList.add(t2transition[t2]);
          t++;
        }
      }
    }

    product.setInitialMarking(Arrays.copyOf(initMarking, p2name.size()));
    product.setFinalMarking(Arrays.copyOf(finMarking, p2name.size()));
    if (trace.getSize() > 0) {
      product.addToInitialMarking(places);
      product.addToFinalMarking(places + trace.getSize());
    }

    // trim to size;
    p2name.trunctate(places);
    t2name.trunctate(transitions);
    t2mmCost.remove(transitions, t2mmCost.size() - transitions);
    t2eid.remove(transitions, t2eid.size() - transitions);
    t2type.remove(transitions, t2type.size() - transitions);
    t2move.remove(transitions, t2move.size() - transitions);

    return product;
  }

  private LinearTrace getLinearTrace(XTrace xTrace, String label) {
    LinearTrace trace = new LinearTrace(label, xTrace.size());
    for (int e = 0; e < xTrace.size(); e++) {
      XEventClass clazz = classes.getClassOf(xTrace.get(e));
      trace.set(e, c2id.get(clazz));
    }

    return trace;

  }

  private SyncProduct getPartiallyOrderedSyncProduct(PartiallyOrderedTrace trace, List<Transition> transitionList) {
    transitionList.clear();

    int[] e2t = new int[trace.getSize()];

    // for this trace, compute the log-moves
    // compute the sync moves
    for (int e = 0; e < trace.getSize(); e++) {
      //			XEventClass clazz = classes.getClassOf(trace.get(e));
      int cid = trace.get(e); // c2id.get(clazz);
      int[] predecessors = trace.getPredecessors(e);
      if (predecessors == null) {
        // initial place
        // add a place
        p2name.add("p_init-" + e);
      } else {
        for (int pi = 0; pi < predecessors.length; pi++) {
          // add a place
          p2name.add("p_" + predecessors[pi] + "-" + e);
        }
      }

      e2t[e] = t2name.size();
      // add log move
      t2name.add("e" + e + "(" + cid + ")");//clazz.toString());
      t2mmCost.add(c2lmCost[cid]);
      t2eid.add(e);
      t2type.add(SyncProduct.LOG_MOVE);
      t2move.add(transitions + e);

      TIntSet set = c2t.get(cid);
      if (set != null) {
        TIntIterator it = set.iterator();
        while (it.hasNext()) {
          // add sync move
          int t = it.next();
          t2name.add(t2name.get(t) + ",e" + e + "(" + cid + ")");
          t2mmCost.add(t2smCost.get(t));
          t2eid.add(e);
          t2type.add(SyncProduct.SYNC_MOVE);
          t2move.add((transitions + trace.getSize() + t));
        }
      }

    }

    int[] ranks = new int[t2eid.size()];
    Arrays.fill(ranks, SyncProduct.NORANK);
    SyncProductImpl product = new SyncProductImpl(trace.getLabel(), //label
        this.classCount, //number of event classes
        t2name.asArray(), //transition labels
        p2name.asArray(), // place labels
        t2eid.toArray(), //event numbers
        ranks, // ranks
        t2type.toArray(), //types
        t2move.toArray(), //moves
        t2mmCost.toArray());

    int t = 0;
    for (; t < transitions; t++) {
      // first the model moves
      product.setInput(t, t2input.get(t));
      product.setOutput(t, t2output.get(t));
      transitionList.add(t2transition[t]);
    }

    product.setInitialMarking(Arrays.copyOf(initMarking, p2name.size()));
    product.setFinalMarking(Arrays.copyOf(finMarking, p2name.size()));

    // TODO: Handle the sync product ranking properly. Currently, a random sequence
    // of events is ranked and the assumption is that events are ordered, i.e. that
    // the predecessors of the event at index e are a index < e .

    int minRank = SyncProduct.NORANK;
    int p = places;
    for (int e = 0; e < trace.getSize(); e++) {
      int cid = trace.get(e); // c2id.get(clazz);

      int[] predecessors = trace.getPredecessors(e);
      if (predecessors == null) {
        // initial place
        // add a place
        if (minRank == SyncProduct.NORANK) {
          product.setRankOf(e2t[e], ++minRank);
        }
        product.setInput(e2t[e], p);
        product.addToInitialMarking(p);
        p++;
      } else {
        for (int pi = 0; pi < predecessors.length; pi++) {
          // add a place
          if (product.getRankOf(e2t[predecessors[pi]]) == minRank) {
            product.setRankOf(e2t[e], ++minRank);
          }
          product.addToInput(e2t[e], p);
          product.addToOutput(e2t[predecessors[pi]], p);
          p++;
        }

      }
      transitionList.add(null);

      TIntSet set = c2t.get(cid);
      if (set != null) {
        TIntIterator it = set.iterator();
        while (it.hasNext()) {
          // add sync move
          int t2 = it.next();

          transitionList.add(t2transition[t2]);
        }
      }
    }
    for (int e = 0; e < trace.getSize(); e++) {
      int cid = trace.get(e); // c2id.get(clazz);
      t++;
      TIntSet set = c2t.get(cid);
      if (set != null) {
        TIntIterator it = set.iterator();
        while (it.hasNext()) {
          // add sync move
          int t2 = it.next();
          product.setInput(t, t2input.get(t2));
          product.setOutput(t, t2output.get(t2));

          product.addToInput(t, product.getInput(e2t[e]));
          product.addToOutput(t, product.getOutput(e2t[e]));

          t++;
        }
      }
    }

    // trim to size;
    p2name.trunctate(places);
    t2name.trunctate(transitions);
    t2mmCost.remove(transitions, t2mmCost.size() - transitions);
    t2eid.remove(transitions, t2eid.size() - transitions);
    t2type.remove(transitions, t2type.size() - transitions);
    t2move.remove(transitions, t2move.size() - transitions);

    return product;
  }

  private PartiallyOrderedTrace getPartiallyOrderedTrace(XTrace xTrace, String label) {
    int s = xTrace.size();
    int[] idx = new int[s];

    TIntList activities = new TIntArrayList(s);
    List<int[]> predecessors = new ArrayList<int[]>();
    Date lastTime = null;
    TIntList pre = new TIntArrayList();
    int previousIndex = -1;
    int currentIdx = 0;
    for (int i = 0; i < s; i++) {
      XEvent event = xTrace.get(i);
      int act = c2id.get(classes.getClassOf(event));
      //			int act = delegate.getActivityOf(trace, i);
      idx[i] = currentIdx;
      Date timestamp = XTimeExtension.instance().extractTimestamp(event);

      activities.add(act);

      if (lastTime == null) {
        // first event
        predecessors.add(null);
      } else if (timestamp.equals(lastTime)) {
        // timestamp is the same as the last event.
        if (previousIndex >= 0) {
          predecessors.add(new int[] { previousIndex });
        } else {
          predecessors.add(null);
        }
      } else {
        // timestamp is different from the last event.
        predecessors.add(pre.toArray());
        previousIndex = idx[i - 1];
        pre = new TIntArrayList();
      }
      pre.add(currentIdx);
      lastTime = timestamp;
      currentIdx++;

    }

    PartiallyOrderedTrace result;
    // predecessors[i] holds all predecessors of event at index i
    result = new PartiallyOrderedTrace(label, activities.toArray(), predecessors.toArray(new int[0][]));
    return result;

  }
}
