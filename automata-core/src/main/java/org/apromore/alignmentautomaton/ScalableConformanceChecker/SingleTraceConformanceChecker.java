package org.apromore.alignmentautomaton.ScalableConformanceChecker;

import java.util.Map;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apromore.alignmentautomaton.automaton.Automaton;
import org.apromore.alignmentautomaton.automaton.State;
import org.apromore.alignmentautomaton.automaton.Transition;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;

public class SingleTraceConformanceChecker {

  Automaton modelAutomaton;

  AllSyncReplayResult res;

  //double cost=0;
  //Alignment for a single trace
  public SingleTraceConformanceChecker(IntArrayList trace, Automaton modelAutomaton,
      BiMap<Integer, String> labelMapping, BiMap<String, Integer> inverseLabelMapping, Map<Integer, String> caseIDs) {
    BiMap<Integer, State> states = HashBiMap.create();
    BiMap<Integer, Transition> transitions = HashBiMap.create();
    UnifiedMap<IntArrayList, IntArrayList> caseTracesMapping = new UnifiedMap<>();
    caseTracesMapping.put(trace, IntArrayList.newListWith(1));
    int initialState = 0;
    IntHashSet finalStates = new IntHashSet();
    Automaton dafsa = null;
    int id = 0;
    states.put(id, new State(id++, true, true));
    for (int pos = 0; pos < trace.size(); pos++) {
      states.put(pos + 1, new State(pos + 1, false, pos == trace.size() - 1));
      Transition tr = new Transition(states.get(pos), states.get(pos + 1), trace.get(pos));
      transitions.put(tr.eventID(), tr);
    }
    dafsa = new Automaton(states, labelMapping, inverseLabelMapping, transitions, initialState, finalStates,
        caseTracesMapping, caseIDs);
    ScalableConformanceChecker checker = new ScalableConformanceChecker(dafsa, modelAutomaton, Integer.MAX_VALUE);
    res = checker.traceAlignmentsMapping.get(trace);
    //System.out.println("Cost: " + res.getInfo().get(PNRepResult.RAWFITNESSCOST));
  }
}
