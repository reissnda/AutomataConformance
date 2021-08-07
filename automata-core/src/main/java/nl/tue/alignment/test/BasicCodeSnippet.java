package nl.tue.alignment.test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import nl.tue.alignment.Replayer;
import nl.tue.alignment.ReplayerParameters;
import nl.tue.alignment.TraceReplayTask;
import nl.tue.alignment.Utils;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.jbpt.petri.Flow;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.io.PNMLSerializer;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResultImpl;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class BasicCodeSnippet {

  static {
    try {
      System.loadLibrary("lpsolve55");
      System.loadLibrary("lpsolve55j");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public ExecutorService service = null;

//  public static void main(String[] args) throws Exception {
//    String petrinetFile = "C:\\temp\\alignment\\test2\\test2";
//    String logFile = "C:\\temp\\alignment\\test2\\test2";
//    BasicCodeSnippet confChecker = new BasicCodeSnippet();
//    Petrinet net = confChecker.constructNet(petrinetFile + ".pnml");
//    Marking initialMarking = confChecker.getInitialMarking(net);
//    Marking finalMarking = confChecker.getFinalMarking(net);
//
//    XLog log;
//    XEventClassifier eventClassifier;
//
//    if (new File(logFile + ".mxml").exists()) {
//      XMxmlParser parser = new XMxmlParser();
//      eventClassifier = XLogInfoImpl.STANDARD_CLASSIFIER;
//      log = parser.parse(new File(logFile + ".mxml")).get(0);
//    } else {
//      XesXmlParser parser = new XesXmlParser();
//      eventClassifier = new XEventNameClassifier();
//      log = parser.parse(new File(logFile + ".xes")).get(0);
//    }
//
//    XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
//    TransEvClassMapping mapping = confChecker
//        .constructMappingBasedOnLabelEquality(net, log, dummyEvClass, eventClassifier);
//    XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);
//    XEventClasses classes = summary.getEventClasses();
//
//    confChecker.doReplay(log, net, initialMarking, finalMarking, classes, mapping);
//
//  }

  public PNRepResult doReplay(XLog log, Petrinet net, Marking initialMarking, Marking finalMarking,
      XEventClasses classes, TransEvClassMapping mapping, int numThreads) {

    int nThreads = numThreads;
    int costUpperBound = Integer.MAX_VALUE;

    ReplayerParameters parameters = new ReplayerParameters.Default(nThreads, costUpperBound, Debug.NONE);
    Replayer replayer = new Replayer(parameters, net, initialMarking, finalMarking, classes, mapping, false);

    // timeout per trace in milliseconds
    int timeoutMilliseconds = 1000 * 1000;
    // preprocessing time to be added to the statistics if necessary
    long preProcessTimeNanoseconds = 0;

    FastList<SyncReplayResult> results = new FastList<>();
    PNRepResult res = new PNRepResultImpl(results);
    double cost = 0;
    int fitting = 0;
    int nonfitting = 0;

    service = Executors.newFixedThreadPool(parameters.nThreads);

    @SuppressWarnings("unchecked") FastList<Future<TraceReplayTask>> futures = new FastList<Future<TraceReplayTask>>();
    FastList<String> traceLabels;
    ObjectIntHashMap<FastList<String>> uniqueTraces = new ObjectIntHashMap<>();
    int fut = 0;

    for (int i = 0; i < log.size(); i++) {
      // Setup the trace replay task
      XTrace trace = log.get(i);
      traceLabels = new FastList<>();
      for (XEvent event : trace) {
        traceLabels.add(((XAttributeLiteral) event.getAttributes().get("concept:name")).getValue());
      }
      if (!uniqueTraces.keySet().contains(traceLabels)) {
        TraceReplayTask task = new TraceReplayTask(replayer, parameters, log.get(i), i, timeoutMilliseconds,
            parameters.maximumNumberOfStates, preProcessTimeNanoseconds);
        // submit for execution
        uniqueTraces.put(traceLabels, 1);
        futures.add(service.submit(task));
      } else {
        uniqueTraces.put(traceLabels, uniqueTraces.get(traceLabels) + 1);
      }
    }
    // initiate shutdown and wait for termination of all submitted tasks.
    service.shutdown();

    // obtain the results one by one.

    //for (int i = 0; i < log.size(); i++) {
    for (int i = 0; i < futures.size(); i++) {
      TraceReplayTask result;
      try {
        result = futures.get(i).get();
      } catch (Exception e) {
        // execution os the service has terminated.
        assert false;
        System.exit(1);
        throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
      }

      switch (result.getResult()) {
        case DUPLICATE:
          assert false; // cannot happen in this setting
          throw new RuntimeException("Result cannot be a duplicate in per-trace computations.");
        case FAILED:
          // internal error in the construction of synchronous product or other error.
          throw new RuntimeException("Error in alignment computations");
        case SUCCESS:
          // process succcesful execution of the replayer
          SyncReplayResult replayResult = result.getSuccesfulResult();
          results.add(replayResult);
          traceLabels = new FastList();
          for (int j = 0; j < replayResult.getStepTypes().size(); j++) {
            if (replayResult.getStepTypes().get(j) == StepTypes.LMGOOD
                || replayResult.getStepTypes().get(j) == StepTypes.L) {
              traceLabels.add(((String) replayResult.getNodeInstance().get(j)));
            }
          }
          cost += (replayResult.getInfo().get(PNRepResult.RAWFITNESSCOST) * uniqueTraces.get(traceLabels));
          int exitCode = replayResult.getInfo().get(Replayer.TRACEEXITCODE).intValue();
          if ((exitCode & Utils.OPTIMALALIGNMENT) == Utils.OPTIMALALIGNMENT) {
            // Optimal alignment found.
            fitting++;
          } else if ((exitCode & Utils.FAILEDALIGNMENT) == Utils.FAILEDALIGNMENT) {
            // failure in the alignment. Error code shows more details.
            nonfitting++;
          }
          if ((exitCode & Utils.ENABLINGBLOCKEDBYOUTPUT) == Utils.ENABLINGBLOCKEDBYOUTPUT) {
            // in some marking, there were too many tokens in a place, blocking the addition of more tokens. Current upper limit is 128
          }
          if ((exitCode & Utils.COSTFUNCTIONOVERFLOW) == Utils.COSTFUNCTIONOVERFLOW) {
            // in some marking, the cost function went through the upper limit of 2^24
          }
          if ((exitCode & Utils.HEURISTICFUNCTIONOVERFLOW) == Utils.HEURISTICFUNCTIONOVERFLOW) {
            // in some marking, the heuristic function went through the upper limit of 2^24
          }
          if ((exitCode & Utils.TIMEOUTREACHED) == Utils.TIMEOUTREACHED) {
            // alignment failed with a timeout
          }
          if ((exitCode & Utils.STATELIMITREACHED) == Utils.STATELIMITREACHED) {
            // alignment failed due to reacing too many states.
          }
          if ((exitCode & Utils.COSTLIMITREACHED) == Utils.COSTLIMITREACHED) {
            // no optimal alignment found with cost less or equal to the given limit.
          }
          if ((exitCode & Utils.CANCELED) == Utils.CANCELED) {
            // user-cancelled.
          }

          break;
      }

    }
    //System.out.println("Fitting:     " + fitting);
    //System.out.println("Non-fitting: " + nonfitting);
    res.addInfo(PNRepResult.RAWFITNESSCOST, "" + cost / log.size());
    res.addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS, "" + fitting);
    return res;
  }

  public PNRepResult doReplay(XLog log, Petrinet net, Marking initialMarking, Marking finalMarking,
      XEventClasses classes, TransEvClassMapping mapping) {

    int nThreads = 1;
    int costUpperBound = Integer.MAX_VALUE;

    ReplayerParameters parameters = new ReplayerParameters.Default(nThreads, costUpperBound, Debug.NONE);
    Replayer replayer = new Replayer(parameters, net, initialMarking, finalMarking, classes, mapping, false);

    // timeout per trace in milliseconds
    int timeoutMilliseconds = 1000 * 1000;
    // preprocessing time to be added to the statistics if necessary
    long preProcessTimeNanoseconds = 0;

    FastList<SyncReplayResult> results = new FastList<>();
    PNRepResult res = new PNRepResultImpl(results);
    double cost = 0;
    int fitting = 0;
    int nonfitting = 0;

    ExecutorService service = Executors.newFixedThreadPool(parameters.nThreads);

    @SuppressWarnings("unchecked") Future<TraceReplayTask>[] futures = new Future[log.size()];

    for (int i = 0; i < log.size(); i++) {
      // Setup the trace replay task
      TraceReplayTask task = new TraceReplayTask(replayer, parameters, log.get(i), i, timeoutMilliseconds,
          parameters.maximumNumberOfStates, preProcessTimeNanoseconds);

      // submit for execution
      futures[i] = service.submit(task);
    }
    // initiate shutdown and wait for termination of all submitted tasks.
    service.shutdown();

    // obtain the results one by one.

    for (int i = 0; i < log.size(); i++) {

      TraceReplayTask result;
      try {
        result = futures[i].get();
      } catch (Exception e) {
        // execution os the service has terminated.
        assert false;
        throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
      }

      switch (result.getResult()) {
        case DUPLICATE:
          assert false; // cannot happen in this setting
          throw new RuntimeException("Result cannot be a duplicate in per-trace computations.");
        case FAILED:
          // internal error in the construction of synchronous product or other error.
          throw new RuntimeException("Error in alignment computations");
        case SUCCESS:
          // process succcesful execution of the replayer
          SyncReplayResult replayResult = result.getSuccesfulResult();
          results.add(replayResult);
          cost += replayResult.getInfo().get(PNRepResult.RAWFITNESSCOST);
          int exitCode = replayResult.getInfo().get(Replayer.TRACEEXITCODE).intValue();
          if ((exitCode & Utils.OPTIMALALIGNMENT) == Utils.OPTIMALALIGNMENT) {
            // Optimal alignment found.
            fitting++;
          } else if ((exitCode & Utils.FAILEDALIGNMENT) == Utils.FAILEDALIGNMENT) {
            // failure in the alignment. Error code shows more details.
            nonfitting++;
          }
          if ((exitCode & Utils.ENABLINGBLOCKEDBYOUTPUT) == Utils.ENABLINGBLOCKEDBYOUTPUT) {
            // in some marking, there were too many tokens in a place, blocking the addition of more tokens. Current upper limit is 128
          }
          if ((exitCode & Utils.COSTFUNCTIONOVERFLOW) == Utils.COSTFUNCTIONOVERFLOW) {
            // in some marking, the cost function went through the upper limit of 2^24
          }
          if ((exitCode & Utils.HEURISTICFUNCTIONOVERFLOW) == Utils.HEURISTICFUNCTIONOVERFLOW) {
            // in some marking, the heuristic function went through the upper limit of 2^24
          }
          if ((exitCode & Utils.TIMEOUTREACHED) == Utils.TIMEOUTREACHED) {
            // alignment failed with a timeout
          }
          if ((exitCode & Utils.STATELIMITREACHED) == Utils.STATELIMITREACHED) {
            // alignment failed due to reacing too many states.
          }
          if ((exitCode & Utils.COSTLIMITREACHED) == Utils.COSTLIMITREACHED) {
            // no optimal alignment found with cost less or equal to the given limit.
          }
          if ((exitCode & Utils.CANCELED) == Utils.CANCELED) {
            // user-cancelled.
          }

          break;
      }

    }
    //System.out.println("Fitting:     " + fitting);
    //System.out.println("Non-fitting: " + nonfitting);
    res.addInfo(PNRepResult.RAWFITNESSCOST, "" + cost / log.size());
    return res;
  }

  public TransEvClassMapping constructMappingBasedOnLabelEquality(PetrinetGraph net, XLog log, XEventClass dummyEvClass,
      XEventClassifier eventClassifier) {
    TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier, dummyEvClass);

    XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

    for (Transition t : net.getTransitions()) {
      boolean mapped = false;
      for (XEventClass evClass : summary.getEventClasses().getClasses()) {
        String id = evClass.getId();

        if (t.getLabel().equals(id)) {
          mapping.put(t, evClass);
          mapped = true;
          break;
        }
      }

      if (!mapped && !t.isInvisible()) {
        mapping.put(t, dummyEvClass);
      }

    }

    return mapping;
  }

  public Petrinet constructNet(String netFile) {
    PNMLSerializer PNML = new PNMLSerializer();
    NetSystem sys = PNML.parse(netFile);

    //System.err.println(sys.getMarkedPlaces());

    //		int pi, ti;
    //		pi = ti = 1;
    //		for (org.jbpt.petri.Place p : sys.getPlaces())
    //			p.setName("p" + pi++);
    //		for (org.jbpt.petri.Transition t : sys.getTransitions())
    //				t.setName("t" + ti++);

    Petrinet net = PetrinetFactory.newPetrinet(netFile);

    // places
    Map<org.jbpt.petri.Place, Place> p2p = new HashMap<org.jbpt.petri.Place, Place>();
    for (org.jbpt.petri.Place p : sys.getPlaces()) {
      Place pp = net.addPlace(p.toString());
      p2p.put(p, pp);
    }

    // transitions
    Map<org.jbpt.petri.Transition, Transition> t2t = new HashMap<org.jbpt.petri.Transition, Transition>();
    for (org.jbpt.petri.Transition t : sys.getTransitions()) {
      Transition tt = net.addTransition(t.getLabel());
      if (t.isSilent() || t.getLabel().startsWith("tau") || t.getLabel().equals("t2") || t.getLabel().equals("t8") || t
          .getLabel().equals("complete")) {
        tt.setInvisible(true);
      }
      t2t.put(t, tt);
    }

    // flow
    for (Flow f : sys.getFlow()) {
      if (f.getSource() instanceof org.jbpt.petri.Place) {
        net.addArc(p2p.get(f.getSource()), t2t.get(f.getTarget()));
      } else {
        net.addArc(t2t.get(f.getSource()), p2p.get(f.getTarget()));
      }
    }

    // add unique start node
    if (sys.getSourceNodes().isEmpty()) {
      Place i = net.addPlace("START_P");
      Transition t = net.addTransition("");
      t.setInvisible(true);
      net.addArc(i, t);

      for (org.jbpt.petri.Place p : sys.getMarkedPlaces()) {
        net.addArc(t, p2p.get(p));
      }

    }

    return net;
  }

  public Marking getFinalMarking(PetrinetGraph net) {
    Marking finalMarking = new Marking();

    for (Place p : net.getPlaces()) {
      if (net.getOutEdges(p).isEmpty()) {
        finalMarking.add(p);
      }
    }

    return finalMarking;
  }

  private Marking getInitialMarking(PetrinetGraph net) {
    Marking initMarking = new Marking();

    for (Place p : net.getPlaces()) {
      if (net.getInEdges(p).isEmpty()) {
        initMarking.add(p);
      }
    }

    return initMarking;
  }

}
