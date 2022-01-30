package main;

import au.qut.apromore.ScalableConformanceChecker.*;
import au.qut.apromore.automaton.Automaton;
import au.qut.apromore.importer.*;
import nl.tue.alignment.test.BasicCodeSnippet;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import test.Alignments.AlignmentTest;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class ConformanceWrapperTRMT implements Callable<ConformanceWrapperTRMT>
{
    public String path;
    public String model;
    String log;
    public String type;
    int numTraceThreads=1;
    int numSCompThreads=1;
    DecomposingConformanceImporter stats;
    public long time =0;
    public long memory=0;
    public long TwithoutConflict=0;
    public double cost = 0;
    public double fitness = 0;
    public double fitnessProblemsSolved;
    public UnifiedMap<Integer, String> caseIDs;
    public DoubleArrayList differences = new DoubleArrayList();
    public MultiThreadedTRConformanceChecker pro = null;
    public MultiThreadedConformanceChecker checker = null;
    public PNMatchInstancesRepResult pnresult = null;
    public String result = "";
    private MultiThreadedDecomposedConformanceChecker scomp=null;
    private MTDecomposingTRConformanceChecker tr_scomp = null;
    private BasicCodeSnippet emeq_alignments;
    private AlignmentTest alignments;

    public ConformanceWrapperTRMT(String[] args)
    {
        path = args[0];
        model = args[1];
        log = args[2];
        type = args[3];
        numTraceThreads = Integer.parseInt(args[5]);
        if(args.length==7) numSCompThreads = Integer.parseInt(args[6]);
    }

    public ConformanceWrapperTRMT call() throws Exception
    {
        try {
            if (type.equals("S-Components")) {
                DecomposingConformanceImporter decomposer = new DecomposingConformanceImporter();
                XLog xLog = new ImportEventLog().importEventLog(path + log);
                Object[] pnetAndM = new ImportProcessModel().importPetriNetAndMarking(path + model);
                //decomposer.importAndDecomposeModelAndLogForConformanceChecking(path, model, log);
                Runtime runtime = Runtime.getRuntime();
                System.gc();
                long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
                //System.out.println("Used Memory before: " + usedMemoryBefore/1000000);
                long start = System.nanoTime();
                decomposer.importAndDecomposeModelAndLogForConformanceChecking((Petrinet) pnetAndM[0], (Marking) pnetAndM[1], xLog);
                scomp = new MultiThreadedDecomposedConformanceChecker(decomposer, numSCompThreads, numTraceThreads);
                time = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
                System.gc();
                long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
                memory=(usedMemoryAfter-usedMemoryBefore)/1000000;
                //System.out.println("Memory increased:" + memory);
                cost = Double.parseDouble(scomp.alignmentResult.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST));
                fitness = Double.parseDouble(scomp.alignmentResult.getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS));
                //nSComps=decomposer.sComponentFSMs.size();
                //conflicts = pro.conflict;
                //nParallel = decomposer.parallel;
                //nChoice = decomposer.choice;
                //logSize = (int) pro.logSize;
                fitnessProblemsSolved = scomp.alignmentResult.size();
                TwithoutConflict = scomp.TwithoutConflict;
                caseIDs = (UnifiedMap) decomposer.caseIDs;
            } else if (type.equals("TR")) {
                ///System.out.println("Start");
                TRImporter importer = new TRImporter(path, log, model);
                Runtime runtime = Runtime.getRuntime();
                System.gc();
                long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
                //System.out.println("Used Memory before: " + usedMemoryBefore/1000000);
                long start = System.nanoTime();
                importer.createAutomata();
                pro = new MultiThreadedTRConformanceChecker(importer.logAutomaton, importer.modelAutomaton, Integer.MAX_VALUE, numTraceThreads);
                time = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
                System.gc();
                long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
                memory=(usedMemoryAfter-usedMemoryBefore)/1000000;
                //System.out.println("Memory increased:" + memory);
                cost = Double.parseDouble(pro.resOneOptimal().getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST));
                fitness = Double.parseDouble(pro.resOneOptimal().getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS));
                fitnessProblemsSolved = pro.fitnessProblemsSolved;
                caseIDs = (UnifiedMap) importer.logAutomaton.caseIDs;
            } else if (type.equals("TR-SComp")) {
                XLog xLog = new ImportEventLog().importEventLog(path + log);
                Object[] pnetAndM = new ImportProcessModel().importPetriNetAndMarking(path + model);
                DecomposingTRImporter decomposer = new DecomposingTRImporter();
                Runtime runtime = Runtime.getRuntime();
                System.gc();
                long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
                //System.out.println("Used Memory before: " + usedMemoryBefore/1000000);
                long start = System.nanoTime();
                decomposer.importAndDecomposeModelAndLogForConformanceChecking((Petrinet) pnetAndM[0], (Marking) pnetAndM[1], xLog);
                tr_scomp = new MTDecomposingTRConformanceChecker(decomposer, numTraceThreads, numSCompThreads);
                time = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
                System.gc();
                long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
                memory=(usedMemoryAfter-usedMemoryBefore)/1000000;
                //System.out.println("Memory increased:" + memory);
                cost = Double.parseDouble(tr_scomp.alignmentResult.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST));
                fitness = Double.parseDouble(tr_scomp.alignmentResult.getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS));
                fitnessProblemsSolved = tr_scomp.alignmentResult.size();
                TwithoutConflict = tr_scomp.TwithoutConflict;
                caseIDs = (UnifiedMap) decomposer.caseIDs;
            } else if (type.equals("BVD-Alignments")) {
                XLog xLog = new ImportEventLog().importEventLog(path + log);
                Object[] pnet_info = new ImportProcessModel().importPetriNetAndMarking(path + model);
                Petrinet pnet = (Petrinet) pnet_info[0];
                Marking initMarking = (Marking) pnet_info[1];
                Runtime runtime = Runtime.getRuntime();
                System.gc();
                long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
                //System.out.println("Used Memory before: " + usedMemoryBefore/1000000);
                long start = System.nanoTime();
                XEventNameClassifier classifier = new XEventNameClassifier();
                XLogInfo summary = XLogInfoFactory.createLogInfo(xLog, classifier);
                emeq_alignments = new BasicCodeSnippet();
                Marking finalMarking = emeq_alignments.getFinalMarking(pnet);
                TransEvClassMapping mapping = emeq_alignments.constructMappingBasedOnLabelEquality(pnet, xLog, new XEventClass("DUMMY", 99999), classifier);
                //BVDAlignmentTest.mainFileFolder(ReplayAlgorithm.Debug.STATS,60,model.substring(0,model.length()-5));
                PNRepResult res = emeq_alignments.doReplay(xLog, pnet, initMarking, finalMarking, summary.getEventClasses(), mapping, numTraceThreads);
                //System.out.println(res.getInfo());
                long alignmentTime = System.nanoTime() - start;
                System.gc();
                long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
                memory=(usedMemoryAfter-usedMemoryBefore)/1000000;
                //System.out.println("Memory increased:" + memory);
                cost = Double.parseDouble((String) res.getInfo().get(PNRepResult.RAWFITNESSCOST));
                time = TimeUnit.MILLISECONDS.convert(alignmentTime, TimeUnit.NANOSECONDS);
                fitnessProblemsSolved = Double.parseDouble((String) res.getInfo().get(PNMatchInstancesRepResult.NUMALIGNMENTS));
                //cost = BVDAlignmentTest
                //System.out.print("Time: " + time + "ms");
            } else if (type.equals("ILP-Alignments")) {
                XLog xLog = new ImportEventLog().importEventLog(path + log);
                Petrinet pnet = (Petrinet) new ImportProcessModel().importPetriNetAndMarking(path + model)[0];
                Runtime runtime = Runtime.getRuntime();
                System.gc();
                long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
                //System.out.println("Used Memory before: " + usedMemoryBefore/1000000);
                long start = System.nanoTime();
                alignments = new AlignmentTest();
                PNRepResult res = alignments.computeCost(pnet, xLog, numTraceThreads, true);
                time = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
                System.gc();
                long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
                memory=(usedMemoryAfter-usedMemoryBefore)/1000000;
                //System.out.println("Memory increased:" + memory);
                cost = (Double) res.getInfo().get(PNRepResult.RAWFITNESSCOST);
                fitnessProblemsSolved = res.size();
            } else if (type.equals("LP-Alignments")) {
                XLog xLog = new ImportEventLog().importEventLog(path + log);
                Petrinet pnet = (Petrinet) new ImportProcessModel().importPetriNetAndMarking(path + model)[0];
                Runtime runtime = Runtime.getRuntime();
                System.gc();
                long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
                //System.out.println("Used Memory before: " + usedMemoryBefore/1000000);
                long start = System.nanoTime();
                alignments = new AlignmentTest();
                PNRepResult res = alignments.computeCost(pnet, xLog, numTraceThreads, false);
                time = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
                System.gc();
                long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
                memory=(usedMemoryAfter-usedMemoryBefore)/1000000;
                //System.out.println("Memory increased:" + memory);
                cost = (Double) res.getInfo().get(PNRepResult.RAWFITNESSCOST);
                fitnessProblemsSolved = res.size();
            }
            else if (type.equals("Automata-Conformance")) {
                Automaton dafsa = new ImportEventLog().convertLogToAutomatonFrom(path + log);
                ImportProcessModel importer = new ImportProcessModel();
                //System.out.println("Log imported");
                Runtime runtime = Runtime.getRuntime();
                System.gc();
                long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
                //System.out.println("Used Memory before: " + usedMemoryBefore/1000000);
                long start = System.nanoTime();
                Automaton fsm = importer.createFSMfromPNMLFile(path + model, dafsa.eventLabels(), dafsa.inverseEventLabels());
                //fsm.toDot(path + model.substring(0,model.length()-5) + ".dot");
                checker = new MultiThreadedConformanceChecker(dafsa, fsm, Integer.MAX_VALUE, numTraceThreads);
                time = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                System.gc();
                long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
                memory=(usedMemoryAfter-usedMemoryBefore)/1000000;
                //System.out.println("Memory increased:" + memory);
                cost = Double.parseDouble(checker.resOneOptimal.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST));
                fitness = Double.parseDouble(checker.resOneOptimal.getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS));
                pnresult = checker.resOneOptimal;
                fitnessProblemsSolved = Double.parseDouble((String) pnresult.getInfo().get(PNMatchInstancesRepResult.NUMALIGNMENTS));
                //System.out.println(checker.resOneOptimal.getInfo());
            }
            else if(type.equals("MeasurePreprocessingTimes"))
            {
                XLog xLog = new ImportEventLog().importEventLog(path + log);
                Object[] pnetAndM = new ImportProcessModel().importPetriNetAndMarking(path + model);
                DecomposingTRImporter decomposer = new DecomposingTRImporter();
                Runtime runtime = Runtime.getRuntime();
                System.gc();
                long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
                //System.out.println("Used Memory before: " + usedMemoryBefore/1000000);
                long start = System.nanoTime();
                decomposer.importAndDecomposeModelAndLogForConformanceChecking((Petrinet) pnetAndM[0], (Marking) pnetAndM[1], xLog);
                //HybridConformanceChecker hybrid = new HybridConformanceChecker(decomposer, numTraceThreads);
                time = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
                System.gc();
                long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
                memory=(usedMemoryAfter-usedMemoryBefore)/1000000;
                //System.out.println("Memory increased:" + memory);
                //cost = Double.parseDouble(hybrid.getAlignments().getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST));
                cost=-1;
                //fitness = Double.parseDouble(hybrid.getAlignments().getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS));
                fitness=-1;
                //fitnessProblemsSolved = hybrid.getAlignments().size();
                //caseIDs = (UnifiedMap) decomposer.caseIDs;
            } else
            {
                type = "Hybrid approach";
                XLog xLog = new ImportEventLog().importEventLog(path + log);
                Object[] pnetAndM = new ImportProcessModel().importPetriNetAndMarking(path + model);
                DecomposingTRImporter decomposer = new DecomposingTRImporter();
                Runtime runtime = Runtime.getRuntime();
                System.gc();
                long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
                //System.out.println("Used Memory before: " + usedMemoryBefore/1000000);
                long start = System.nanoTime();
                decomposer.importAndDecomposeModelAndLogForConformanceChecking((Petrinet) pnetAndM[0], (Marking) pnetAndM[1], xLog);
                HybridConformanceChecker hybrid = new HybridConformanceChecker(decomposer, numTraceThreads);
                time = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
                System.gc();
                long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
                memory=(usedMemoryAfter-usedMemoryBefore)/1000000;
                //System.out.println("Memory increased:" + memory);
                cost = Double.parseDouble(hybrid.getAlignments().getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST));
                fitness = Double.parseDouble(hybrid.getAlignments().getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS));
                fitnessProblemsSolved = hybrid.getAlignments().size();
                caseIDs = (UnifiedMap) decomposer.caseIDs;
            }
            result = type + ", " + model + ", " + time + " ms, " + cost + ", " + fitness + "\n";
            //if (type.equals("TR-SComp") || type.equals("S-Components"))
            //    result = result.substring(0, result.length() - 1) + "," + TwithoutConflict + "\n";
        /*if(type.equals("S-Components-with-compare"))
        {
            if(!differences.isEmpty()) {
                result = result.substring(0, result.length() - 2) + ",";
                result += nNonOpt + "," + differences.size() + "," + differences.average() + "\n";
            }
        }*/

        } catch (InterruptedException exception)
        {
            if(type.equals("Automata-Conformance"))
            {
                checker.executor.shutdownNow();
            }
            else if(type.equals("TR"))
            {
                pro.executor.shutdownNow();
            }
            else if(type.equals("BVD-Alignments"))
            {
                emeq_alignments.service.shutdownNow();
            }
        }
        return this;
    }
}
