package org.apromore.alignmentautomaton;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import nl.tue.alignment.test.BasicCodeSnippet;
import org.apromore.alignmentautomaton.ScalableConformanceChecker.MultiThreadedConformanceChecker;
import org.apromore.alignmentautomaton.ScalableConformanceChecker.MultiThreadedDecomposedConformanceChecker;
import org.apromore.alignmentautomaton.automaton.Automaton;
import org.apromore.alignmentautomaton.importer.DecomposingConformanceImporter;
import org.apromore.alignmentautomaton.importer.ImportEventLog;
import org.apromore.alignmentautomaton.importer.ImportProcessModel;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import test.Alignments.AlignmentTest;

public class ConformanceWrapperSCompMT implements Callable<ConformanceWrapperSCompMT> {

  String path;

  String model;

  String log;

  String type;

  int numTraceThreads = 1;

  int numSCompThreads = 1;

  DecomposingConformanceImporter stats;

  public long time = 0;

  public double cost = 0;

  public int nSComps = 0;

  public int nParallel = 0;

  public int nChoice = 0;

  public int conflicts = 0;

  public int logSize = 0;

  public int nNonOpt = 0;

  public DoubleArrayList differences = new DoubleArrayList();

  public MultiThreadedDecomposedConformanceChecker pro = null;

  public MultiThreadedConformanceChecker checker = null;

  public PNMatchInstancesRepResult pnresult = null;

  public String result = "";

  private BasicCodeSnippet emeq_alignments;

  public ConformanceWrapperSCompMT(String[] args) {
    path = args[0];
    model = args[1];
    log = args[2];
    type = args[3];
    numTraceThreads = Integer.parseInt(args[5]);
    if (args.length == 7) {
      numSCompThreads = Integer.parseInt(args[6]);
    }
  }

  public ConformanceWrapperSCompMT call() throws Exception {
    if (type.equals("Param")) {
      //ImportProcessModel importer = new ImportProcessModel();
      //Automaton fsm = importer.createFSMfromPNMLFile(path + model, null, null);
      //System.out.println(importer.parallel + ", " + importer.choice);
      //System.out.println(fsm.elementaryCycles());
      stats = new DecomposingConformanceImporter();
      stats.importAndDecomposeModelAndLogForConformanceChecking(path, model, log);
      stats.gatherStatistics();
      return this;
    }

    if (type.equals("S-Components")) {
      DecomposingConformanceImporter decomposer = new DecomposingConformanceImporter();
      XLog xLog = new ImportEventLog().importEventLog(path + log);
      Object[] pnetAndM = new ImportProcessModel().importPetriNetAndMarking(path + model);
      //decomposer.importAndDecomposeModelAndLogForConformanceChecking(path, model, log);
      long start = System.nanoTime();
      decomposer
          .importAndDecomposeModelAndLogForConformanceChecking((Petrinet) pnetAndM[0], (Marking) pnetAndM[1], xLog);
      pro = new MultiThreadedDecomposedConformanceChecker(decomposer, numSCompThreads, numTraceThreads);
      time = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
      cost = Double.parseDouble(pro.alignmentResult.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST));
      nSComps = decomposer.sComponentFSMs.size();
      conflicts = pro.conflict;
      nParallel = decomposer.parallel;
      nChoice = decomposer.choice;
      logSize = (int) pro.logSize;
    } else if (type.equals("S-Components-with-compare")) {
      //System.out.println("Start");
      DecomposingConformanceImporter decomposer = new DecomposingConformanceImporter();
      XLog xLog = new ImportEventLog().importEventLog(path + log);
      Object[] pnetAndM = new ImportProcessModel().importPetriNetAndMarking(path + model);
      //decomposer.importAndDecomposeModelAndLogForConformanceChecking(path, model, log);
      long start = System.nanoTime();
      decomposer
          .importAndDecomposeModelAndLogForConformanceChecking((Petrinet) pnetAndM[0], (Marking) pnetAndM[1], xLog);
      pro = new MultiThreadedDecomposedConformanceChecker(decomposer, numSCompThreads, numTraceThreads);
      time = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
      //System.out.println("S-Components finished");
      cost = Double.parseDouble(pro.alignmentResult.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST));
      nSComps = decomposer.sComponentFSMs.size();
      conflicts = pro.conflict;
      nParallel = decomposer.parallel;
      nChoice = decomposer.choice;
      logSize = (int) pro.logSize;

      pnresult = pro.alignmentResult;
      PNRepResult repres = AlignmentTest.computeCost((PetrinetGraph) pnetAndM[0], xLog);
      //System.out.println("Alignments finished");
      for (SyncReplayResult res : repres) {
        for (AllSyncReplayResult res2 : pnresult) {
          if (res2.getTraceIndex().contains(res.getTraceIndex().first())) {
            double difference = Math.round(res2.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST)) - Math
                .round(res.getInfo().get(PNRepResult.RAWFITNESSCOST));
            if (difference > 0) {
              differences.add(difference);
              nNonOpt += res.getTraceIndex().size();
            }
            break;
          }
        }
      }
      //System.out.println("Compare finished");
    } else if (type.equals("BVD-Alignments")) {
      XLog xLog = new ImportEventLog().importEventLog(path + log);
      XEventNameClassifier classifier = new XEventNameClassifier();
      XLogInfo summary = XLogInfoFactory.createLogInfo(xLog, classifier);
      Object[] pnet_info = new ImportProcessModel().importPetriNetAndMarking(path + model);
      Petrinet pnet = (Petrinet) pnet_info[0];
      Marking initMarking = (Marking) pnet_info[1];
      emeq_alignments = new BasicCodeSnippet();
      Marking finalMarking = emeq_alignments.getFinalMarking(pnet);
      TransEvClassMapping mapping = emeq_alignments
          .constructMappingBasedOnLabelEquality(pnet, xLog, new XEventClass("DUMMY", 99999), classifier);
      long start = System.nanoTime();
      //BVDAlignmentTest.mainFileFolder(ReplayAlgorithm.Debug.STATS,60,model.substring(0,model.length()-5));
      PNRepResult res = emeq_alignments
          .doReplay(xLog, pnet, initMarking, finalMarking, summary.getEventClasses(), mapping, numTraceThreads);
      //System.out.println(res.getInfo());
      long alignmentTime = System.nanoTime() - start;
      cost = Double.parseDouble((String) res.getInfo().get(PNRepResult.RAWFITNESSCOST));
      time = TimeUnit.MILLISECONDS.convert(alignmentTime, TimeUnit.NANOSECONDS);
      //cost = BVDAlignmentTest
      //System.out.print("Time: " + time + "ms");
    } else if (type.equals("ILP-Alignments")) {
      XLog xLog = new ImportEventLog().importEventLog(path + log);
      Petrinet pnet = (Petrinet) new ImportProcessModel().importPetriNetAndMarking(path + model)[0];
      long start = System.nanoTime();
      AlignmentTest ilp_alignments = new AlignmentTest();
      PNRepResult res = ilp_alignments.computeCost(pnet, xLog, numTraceThreads);
      time = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
      cost = (Double) res.getInfo().get(PNRepResult.RAWFITNESSCOST);
    } else {
      Automaton dafsa = new ImportEventLog().convertLogToAutomatonFrom(path + log);
      //System.out.println("Log imported");
      long start = System.nanoTime();
      ImportProcessModel importer = new ImportProcessModel();
      Automaton fsm = importer.createFSMfromPNMLFile(path + model, dafsa.eventLabels(), dafsa.inverseEventLabels());
      //fsm.toDot(path + model.substring(0,model.length()-5) + ".dot");
      checker = new MultiThreadedConformanceChecker(dafsa, fsm, Integer.MAX_VALUE, numTraceThreads);
      time = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
      cost = Double.parseDouble(checker.resOneOptimal.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST));
      nSComps = 1;
      nParallel = importer.parallel;
      nChoice = importer.choice;
      logSize = (int) checker.logSize;
      pnresult = checker.resOneOptimal;
      //System.out.println(checker.resOneOptimal.getInfo());
    }
    result =
        type + "," + path + "," + model + "," + nSComps + "," + nParallel + "," + nChoice + "," + log + "," + logSize
            + "," + time + "," + cost + "," + conflicts + "\n";
    if (type.equals("S-Components-with-compare")) {
      if (!differences.isEmpty()) {
        result = result.substring(0, result.length() - 2) + ",";
        result += nNonOpt + "," + differences.size() + "," + differences.average() + "\n";
      }
    }
    return this;
  }
}
