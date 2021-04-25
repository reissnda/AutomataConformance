package org.apromore.alignmentautomaton.importer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix2D;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apromore.alignmentautomaton.automaton.Automaton;
import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.processmining.framework.util.collection.MultiSet;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.analysis.TComponentSet;
import org.processmining.models.graphbased.directed.petrinet.analysis.TransitionInvariantSet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.structuralanalysis.IncidenceMatrixFactory;
import org.processmining.plugins.petrinet.structuralanalysis.invariants.TransitionInvariantCalculator;

public class PrecisionImporter extends ImportProcessModel {

  private final String conceptname = "concept:name";

  //private Map<IntArrayList, Boolean> tracesContained;
  public BiMap<IntArrayList, IntArrayList> caseTracesMapping;

  //private IntObjectHashMap<String> traceIDtraceName;
  private final BiMap<Integer, IntArrayList> labelComponentsMapping = HashBiMap.create();

  public List<Automaton> tComponentFSMs = new FastList<Automaton>();

  public Set<Petrinet> tComponentNets = new HashSet<Petrinet>();

  public Map<Integer, String> caseIDs = new UnifiedMap<Integer, String>();

  public UnifiedMap<Integer, Automaton> componentDAFSAs = new UnifiedMap<Integer, Automaton>();

  private final UnifiedSet<IntArrayList> visited = new UnifiedSet<IntArrayList>();

  public XLog xLog;

  public UnifiedMap<IntArrayList, UnifiedMap<Integer, IntArrayList>> traceProjections = new UnifiedMap<IntArrayList, UnifiedMap<Integer, IntArrayList>>();

  public int minModelMoves;

  //private UnifiedMap<Integer, UnifiedMap<Integer, IntArrayList>> projectedLogs = new UnifiedMap<Integer, UnifiedMap<Integer, IntArrayList>>(); //TODO: ???
  private final UnifiedMap<Integer, BiMap<IntArrayList, IntArrayList>> projectedLogs = new UnifiedMap<Integer, BiMap<IntArrayList, IntArrayList>>();

  public void importAndDecomposeModelAndLogForConformanceChecking(String path, String model, String log)
      throws Exception {
    Object[] pnetAndMarking;
    if (model.endsWith(".bpmn")) {
      pnetAndMarking = this.importPetrinetFromBPMN(path + model);
    } else {
      pnetAndMarking = importPetriNetAndMarking(path + model);
    }
    xLog = new ImportEventLog().importEventLog(path + log);
    this.importAndDecomposeModelAndLogForConformanceChecking((Petrinet) pnetAndMarking[0], (Marking) pnetAndMarking[1],
        xLog);
  }

  public void importAndDecomposeModelAndLogForConformanceChecking(Petrinet pnet, Marking initM, XLog xLog)
      throws Exception {
    this.xLog = xLog;
    decomposePetriNetIntoTComponentAutomata(pnet, initM);
    int i = 1;
    for (Automaton fsm : this.tComponentFSMs) {
      fsm.toDot("/Users/daniel/Documents/workspace/paper_tests/Road Traffic/SymmetrieTests/net" + i + ".dot");
      i++;
    }
  }

  private void decomposePetriNetIntoTComponentAutomata(Petrinet pnet, Marking initM) throws Exception {
    TransitionInvariantCalculator calculator = new TransitionInvariantCalculator();
    TransitionInvariantSet invMarking = calculator.calculate(context, pnet);
    TComponentSet tComps = calculateTComponents(pnet, invMarking);

    int i = 0;
    //System.out.println();
    for (SortedSet<PetrinetNode> component : tComps) {
      Petrinet sCompNet = new PetrinetImpl("net" + i);
      for (PetrinetNode node : component) {
        if ((node.getAttributeMap().get(AttributeMap.SHAPE).getClass().getName()
            .equals("org.processmining.models.shapes.Rectangle"))) {
          Transition tr = null;
          for (Transition t : sCompNet.getTransitions()) {
            if (t.getLabel() == node.getLabel()) {
              tr = t;
              break;
            }
          }
          if (tr == null) {
            tr = sCompNet.addTransition(node.getLabel());
          }
          for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : node.getGraph().getOutEdges(node)) {
            if (component.contains(edge.getTarget())) {
              Place p = null;
              for (Place pl : sCompNet.getPlaces()) {
                if (pl.getLabel() == edge.getTarget().getLabel()) {
                  p = pl;
                  break;
                }
              }
              if (p == null) {
                p = sCompNet.addPlace(edge.getTarget().getLabel());
              }
              sCompNet.addArc(tr, p);
            }
          }
        } else {
          Place p = null;
          for (Place pl : sCompNet.getPlaces()) {
            if (pl.getLabel() == node.getLabel()) {
              p = pl;
              break;
            }
          }
          if (p == null) {
            p = sCompNet.addPlace(node.getLabel());
          }
          for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : node.getGraph().getOutEdges(node)) {
            if (component.contains(edge.getTarget())) {
              Transition tr = null;
              for (Transition t : sCompNet.getTransitions()) {
                if (t.getLabel() == edge.getTarget().getLabel()) {
                  tr = t;
                  break;
                }
              }
              if (tr == null) {
                tr = sCompNet.addTransition(edge.getTarget().getLabel());
              }
              sCompNet.addArc(p, tr);
            }
          }
        }
      }
      i++;
      Marking m = new Marking();
      for (Place p : initM) {
        for (Place p2 : sCompNet.getPlaces()) {
          if (p2.getLabel().equals(p.getLabel())) {
            m.add(p2);
          }
        }
      }
      Automaton fsm = createFSMfromPetrinet(sCompNet, m, globalInverseLabels.inverse(), globalInverseLabels);
      IntArrayList components;
      tComponentFSMs.add(fsm);
      //System.out.println(fsm.eventLabels());
      //System.out.println(globalInverseLabels.inverse());
      for (Integer event : fsm.eventLabels().keySet()) {
        if ((components = labelComponentsMapping.get(event)) == null) {
          components = new IntArrayList();
          labelComponentsMapping.put(event, components);
        }
        components.add(tComponentFSMs.size() - 1);
      }
      //sComponentFSMs.get(sComponentFSMs.size()-1).toDot("/Users/daniel/Documents/workspace/paper_tests/Sepsis/" + sCompNet.getLabel() + ".dot");
      //System.out.println(globalInverseLabels);
      //System.out.println(sComponentFSMs.get(sComponentFSMs.size()-1).eventLabels());
      //System.out.println(sComponentFSMs.get(sComponentFSMs.size()-1).inverseEventLabels());
      tComponentNets.add(sCompNet);
    }

  }

  private TComponentSet calculateTComponents(PetrinetGraph net, TransitionInvariantSet invMarking) {
    // initialization
    TComponentSet nodeMarking = new TComponentSet(); // to store final result

    // references
    DoubleMatrix2D incidenceMatrix = IncidenceMatrixFactory.getIncidenceMatrix(context, net);
    if (context != null && context.getProgress().isCancelled()) {
      return null;
    }

    ArrayList<Place> placeList = new ArrayList<Place>(net.getPlaces());
    ArrayList<Transition> transitionList = new ArrayList<Transition>(net.getTransitions());

    // for each transition invariants filter only the one with 1 or 0 as its member
    invariantLoop:
    for (MultiSet<Transition> set : invMarking) {

      if (context != null && context.getProgress().isCancelled()) {
        return null;
      }

      // for one set of invariant, check each element
      Set<Integer> transitionIndex = new HashSet<Integer>();
      for (PetrinetNode node : set) {
        if (set.occurrences(node) == 1) {
          transitionIndex.add(transitionList.indexOf(node));
        } else {
          continue invariantLoop;
        }
      }

      // until here, we have an invariant which only consists of 0 and 1

      // iterate the transitions, generate array of integer to dice incidenceMatrix
      int[] transitions = new int[transitionIndex.size()];
      Iterator<Integer> it = transitionIndex.iterator();
      int counter = 0;
      while (it.hasNext()) {
        transitions[counter] = it.next();
        counter++;
      }
      // iterate all of places, generate array of integer to dice incidenceMatrix
      int[] places = new int[placeList.size()];
      for (int i = 0; i < placeList.size(); i++) {
        places[i] = i;
      }

      // dice incidence matrix so that it only include necessary place and transitions
      DoubleMatrix2D tempIncidenceMatrix = incidenceMatrix.viewSelection(places, transitions);

      Set<Integer> placeIndex = new HashSet<Integer>(); // to store result for transition

      // for each rows on the diced incidence matrix, there can only be 2 nonzero value with the sum of 0
      IntArrayList tempPlace = new IntArrayList();
      DoubleArrayList tempPlaceValue = new DoubleArrayList();
      for (int i = 0; i < tempIncidenceMatrix.rows(); i++) {
        tempIncidenceMatrix.viewRow(i).getNonZeros(tempPlace, tempPlaceValue);

        // as only 1 ingoing and 1 outgoing arc is permitted, number of element should be 2
        // as there can only be 1 ingoing and 1 outgoing, addition should be 0
        if ((tempPlace.size() == 2) && (Double.compare(0.0, tempPlaceValue.get(0) + tempPlaceValue.get(1)) == 0)) {
          // add to transitionIndex
          placeIndex.add(i);
        } else if (tempPlace.size() != 0) {
          // this invariant has invalid transition inclusion. Continue to other invariants
          continue invariantLoop;
        }
      }

      // until here, we have our T-components, add all transition and places as a T-component
      SortedSet<PetrinetNode> result = new TreeSet<PetrinetNode>();
      // add all places
      for (int tempIndex : transitionIndex) {
        result.add(transitionList.get(tempIndex));
      }
      // add transition corresponds to each place
      for (int tempIndex : placeIndex) {
        result.add(placeList.get(tempIndex));
      }
      // add to final set
      nodeMarking.add(result);
    }

    return nodeMarking;
  }
}
