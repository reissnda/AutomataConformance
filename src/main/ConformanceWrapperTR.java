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
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import test.Alignments.AlignmentTest;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class ConformanceWrapperTR implements Callable<ConformanceWrapperTR>
{
    public String path;
    public String model;
    String log;
    public String type;
    DecomposingConformanceImporter stats;
    public long time =0;
    public double cost = 0;
    public double fitness = 0;
    public double fitnessProblemsSolved;
    public long memory=0;
    public UnifiedMap<Integer, String> caseIDs;
    public DoubleArrayList differences = new DoubleArrayList();
    public TRConformanceChecker pro = null;
    public ScalableConformanceChecker checker = null;
    public PNMatchInstancesRepResult alignresult = null;
    public String result = "";
    private BasicCodeSnippet emeq_alignments;
    private AlignmentTest alignments;

    public ConformanceWrapperTR(String[] args)
    {
        path = args[0];
        model = args[1];
        log = args[2];
        type = args[3];
    }

    public ConformanceWrapperTR call() throws Exception
    {
        if(type.equals("S-Components"))
        {
            DecomposingConformanceImporter decomposer = new DecomposingConformanceImporter();
            XLog xLog = new ImportEventLog().importEventLog(path + log);
            Object[] pnetAndM = new ImportProcessModel().importPetriNetAndMarking(path + model);
            //decomposer.importAndDecomposeModelAndLogForConformanceChecking(path, model, log);
            Runtime runtime = Runtime.getRuntime();
            System.gc();
            long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
            //System.out.println("Used Memory before: " + usedMemoryBefore/1000000);
            long start = System.nanoTime();
            decomposer.importAndDecomposeModelAndLogForConformanceChecking((Petrinet) pnetAndM[0],(Marking) pnetAndM[1], xLog);
            DecomposingConformanceChecker checker =  new DecomposingConformanceChecker(decomposer);
            time = TimeUnit.MILLISECONDS.convert(System.nanoTime()-start, TimeUnit.NANOSECONDS);
            System.gc();
            long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
            memory=(usedMemoryAfter-usedMemoryBefore)/1000000;
            //System.out.println("Memory increased:" + memory);
            cost = Double.parseDouble(checker.alignmentResult.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST));
            fitness = Double.parseDouble(checker.alignmentResult.getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS));
            //nSComps=decomposer.sComponentFSMs.size();
            //conflicts = pro.conflict;
            //nParallel = decomposer.parallel;
            //nChoice = decomposer.choice;
            //logSize = (int) pro.logSize;
            fitnessProblemsSolved = checker.alignmentResult.size();
            alignresult = checker.alignmentResult;
            caseIDs = (UnifiedMap) decomposer.caseIDs;
        }
        else if(type.equals("TR"))
        {
            //System.out.println("Start");
            TRImporter importer = new TRImporter(path, log, model);
            //XLog xLog = new ImportEventLog().importEventLog(path + log);
            //Object[] pnetAndM = new ImportProcessModel().importPetriNetAndMarking(path + model);
            //decomposer.importAndDecomposeModelAndLogForConformanceChecking(path, model, log);
            Runtime runtime = Runtime.getRuntime();
            System.gc();
            long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
            //System.out.println("Used Memory before: " + usedMemoryBefore/1000000);
            long start = System.nanoTime();
            importer.createAutomata();
            pro =  new TRConformanceChecker(importer.logAutomaton, importer.modelAutomaton, Integer.MAX_VALUE);
            time = TimeUnit.MILLISECONDS.convert(System.nanoTime()-start, TimeUnit.NANOSECONDS);
            System.gc();
            long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
            memory=(usedMemoryAfter-usedMemoryBefore)/1000000;
            //System.out.println("Memory increased:" + memory);
            //System.out.println("S-Components finished");
            cost = Double.parseDouble(pro.resOneOptimal().getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST));
            fitness = Double.parseDouble(pro.resOneOptimal().getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS));
            fitnessProblemsSolved = pro.fitnessProblemsSolved;
            alignresult = pro.resOneOptimal();
            caseIDs = (UnifiedMap) importer.logAutomaton.caseIDs;
        }
        else if(type.equals("TR-SComp"))
        {
            DecomposingTRImporter importer = new DecomposingTRImporter();
            XLog xLog = new ImportEventLog().importEventLog(path + log);
            Object[] pnetAndM = new ImportProcessModel().importPetriNetAndMarking(path + model);
            Runtime runtime = Runtime.getRuntime();
            System.gc();
            long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
            //System.out.println("Used Memory before: " + usedMemoryBefore/1000000);
            long start = System.nanoTime();
            //importer.createAutomata();
            importer.importAndDecomposeModelAndLogForConformanceChecking((Petrinet) pnetAndM[0], (Marking) pnetAndM[1],xLog);
            DecomposingTRConformanceChecker checker = new DecomposingTRConformanceChecker(importer);
            time = TimeUnit.MILLISECONDS.convert(System.nanoTime()-start, TimeUnit.NANOSECONDS);
            System.gc();
            long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
            memory=(usedMemoryAfter-usedMemoryBefore)/1000000;
            //System.out.println("Memory increased:" + memory);
            //System.out.println("S-Components finished");
            cost = Double.parseDouble(checker.alignmentResult.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST));
            fitness = Double.parseDouble(checker.alignmentResult.getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS));
            fitnessProblemsSolved = checker.alignmentResult.size();
            alignresult = checker.alignmentResult;
            caseIDs = (UnifiedMap) importer.caseIDs;
        }
        else if(type.equals("BVD-Alignments"))
        {
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
            XLogInfo summary = XLogInfoFactory.createLogInfo(xLog,classifier);
            emeq_alignments = new BasicCodeSnippet();
            Marking finalMarking = emeq_alignments.getFinalMarking(pnet);
            TransEvClassMapping mapping = emeq_alignments.constructMappingBasedOnLabelEquality(pnet,xLog, new XEventClass("DUMMY", 99999), classifier);
            //BVDAlignmentTest.mainFileFolder(ReplayAlgorithm.Debug.STATS,60,model.substring(0,model.length()-5));
            PNRepResult res = emeq_alignments.doReplay(xLog,pnet,initMarking,finalMarking,summary.getEventClasses(),mapping, 1);
            //System.out.println(res.getInfo());
            long alignmentTime = System.nanoTime()-start;
            cost = Double.parseDouble((String) res.getInfo().get(PNRepResult.RAWFITNESSCOST));
            time = TimeUnit.MILLISECONDS.convert(alignmentTime,TimeUnit.NANOSECONDS);
            System.gc();
            long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
            memory=(usedMemoryAfter-usedMemoryBefore)/1000000;
            //System.out.println("Memory increased:" + memory);
            fitnessProblemsSolved = Double.parseDouble((String) res.getInfo().get(PNMatchInstancesRepResult.NUMALIGNMENTS));;
            //cost = BVDAlignmentTest
            //System.out.print("Time: " + time + "ms");
        }
        else if(type.equals("ILP-Alignments"))
        {
            XLog xLog = new ImportEventLog().importEventLog(path + log);
            Petrinet pnet = (Petrinet) new ImportProcessModel().importPetriNetAndMarking(path + model)[0];
            Runtime runtime = Runtime.getRuntime();
            System.gc();
            long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
            //System.out.println("Used Memory before: " + usedMemoryBefore/1000000);
            long start = System.nanoTime();
            alignments = new AlignmentTest();
            PNRepResult res = alignments.computeCost(pnet, xLog,true);
            time=TimeUnit.MILLISECONDS.convert(System.nanoTime()-start,TimeUnit.NANOSECONDS);
            System.gc();
            long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
            memory=(usedMemoryAfter-usedMemoryBefore)/1000000;
            //System.out.println("Memory increased:" + memory);
            cost= (Double) res.getInfo().get(PNRepResult.RAWFITNESSCOST);
            fitnessProblemsSolved = res.size();
        }
        else if(type.equals("LP-Alignments"))
        {
            XLog xLog = new ImportEventLog().importEventLog(path + log);
            Petrinet pnet = (Petrinet) new ImportProcessModel().importPetriNetAndMarking(path + model)[0];
            Runtime runtime = Runtime.getRuntime();
            System.gc();
            long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
            //System.out.println("Used Memory before: " + usedMemoryBefore/1000000);
            long start = System.nanoTime();
            alignments = new AlignmentTest();
            PNRepResult res = alignments.computeCost(pnet, xLog,false);
            time=TimeUnit.MILLISECONDS.convert(System.nanoTime()-start,TimeUnit.NANOSECONDS);
            System.gc();
            long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
            memory=(usedMemoryAfter-usedMemoryBefore)/1000000;
            //System.out.println("Memory increased:" + memory);
            cost= (Double) res.getInfo().get(PNRepResult.RAWFITNESSCOST);
            fitnessProblemsSolved = res.size();
        }
        else if(type.equals("Automata-Conformance"))
        {
            Automaton dafsa = new ImportEventLog().convertLogToAutomatonFrom(path + log);
            //System.out.println("Log imported");
            Runtime runtime = Runtime.getRuntime();
            System.gc();
            long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
            //System.out.println("Used Memory before: " + usedMemoryBefore/1000000);
            long start = System.nanoTime();
            ImportProcessModel importer = new ImportProcessModel();
            Automaton fsm = importer.createFSMfromPNMLFile(path + model, dafsa.eventLabels(), dafsa.inverseEventLabels());
            //fsm.toDot(path + model.substring(0,model.length()-5) + ".dot");
            checker = new ScalableConformanceChecker(dafsa,fsm,Integer.MAX_VALUE);
            time = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            System.gc();
            long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
            memory=(usedMemoryAfter-usedMemoryBefore)/1000000;
            //System.out.println("Memory increased:" + memory);
            cost = Double.parseDouble(checker.resOneOptimal().getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST));
            alignresult = checker.resOneOptimal();
            fitness = Double.parseDouble(checker.resOneOptimal().getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS));
            fitnessProblemsSolved = Double.parseDouble((String) alignresult.getInfo().get(PNMatchInstancesRepResult.NUMALIGNMENTS));
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
            //cost = Double.parseDouble(hybrid.getAlignments().getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST));
            System.gc();
            long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
            memory=(usedMemoryAfter-usedMemoryBefore)/1000000;
            //System.out.println("Memory increased:" + memory);
            cost=-1;
            //fitness = Double.parseDouble(hybrid.getAlignments().getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS));
            fitness=-1;
            //fitnessProblemsSolved = hybrid.getAlignments().size();
            //caseIDs = (UnifiedMap) decomposer.caseIDs;
        } else
        {
            type = "Hybrid approach";
            DecomposingTRImporter importer = new DecomposingTRImporter();
            XLog xLog = new ImportEventLog().importEventLog(path + log);
            Object[] pnetAndM = new ImportProcessModel().importPetriNetAndMarking(path + model);
            Runtime runtime = Runtime.getRuntime();
            System.gc();
            long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
            //System.out.println("Used Memory before: " + usedMemoryBefore/1000000);
            long start = System.nanoTime();
            //importer.createAutomata();
            importer.importAndDecomposeModelAndLogForConformanceChecking((Petrinet) pnetAndM[0], (Marking) pnetAndM[1],xLog);
            HybridConformanceChecker checker = new HybridConformanceChecker(importer);
            time = TimeUnit.MILLISECONDS.convert(System.nanoTime()-start, TimeUnit.NANOSECONDS);
            System.gc();
            long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
            memory=(usedMemoryAfter-usedMemoryBefore)/1000000;
            //System.out.println("Memory increased:" + memory);
            //System.out.println("S-Components finished");
            cost = Double.parseDouble(checker.getAlignments().getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST));
            fitness = Double.parseDouble(checker.getAlignments().getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS));
            //fitnessProblemsSolved = checker.alignmentResult.size();
            alignresult = checker.getAlignments();
            caseIDs = (UnifiedMap) importer.caseIDs;
        }
        result = type +"," + model + "," + time + "," +  cost + "," + fitness + "\n";
        /*if(type.equals("S-Components-with-compare"))
        {
            if(!differences.isEmpty()) {
                result = result.substring(0, result.length() - 2) + ",";
                result += nNonOpt + "," + differences.size() + "," + differences.average() + "\n";
            }
        }*/
        return this;
    }
}
