package au.qut.apromore.ScalableConformanceChecker;

import au.qut.apromore.automaton.Automaton;
import au.qut.apromore.importer.ImportEventLog;
import au.qut.apromore.importer.ImportProcessModel;
import au.qut.apromore.psp.*;
import com.google.common.collect.HashBiMap;
import lpsolve.LpSolve;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class MultiThreadedConformanceChecker implements Callable<MultiThreadedConformanceChecker>
{
    public int component;
    private Automaton logAutomaton;
    private Automaton modelAutomaton;
    private PSP psp;
    private Map<IntArrayList, Set<Node>> prefixMemorizationTable;
    private IntObjectHashMap<Set<Configuration>> suffixMemorizationTable;
    public PNMatchInstancesRepResult resOneOptimal = new PNMatchInstancesRepResult(new FastList<AllSyncReplayResult>());
    private PNMatchInstancesRepResult resAllOptimal;
    private Map<IntArrayList, AllSyncReplayResult> caseReplayResultMapping = new UnifiedMap<>();
    public UnifiedMap<IntArrayList, AllSyncReplayResult> traceAlignmentsMapping = new UnifiedMap<IntArrayList, AllSyncReplayResult>();
    public int cost = 0;
    //public LongIntHashMap statePruning;
    IntIntHashMap visited = new IntIntHashMap();
    public long preperationLog;
    public long preperationModel;
    public long timeOneOptimal;
    public long timeAllOptimal;
    private int qStates=1;
    private int nThreads;
    private boolean useVisited = true;
    private static LpSolve lp;
    public double logSize;
    private double epsilon = 0.0000000;
    public ExecutorService executor = null;

    public MultiThreadedConformanceChecker(int component, Automaton logAutomaton, Automaton modelAutomaton, int numThreads)
    {
        this.logAutomaton = logAutomaton;
        this.modelAutomaton = modelAutomaton;
        this.nThreads = numThreads;
    }

    public MultiThreadedConformanceChecker(int component, Automaton logAutomaton, Automaton modelAutomaton)
    {
        this.logAutomaton = logAutomaton;
        this.modelAutomaton = modelAutomaton;
        this.nThreads = 1;
    }

    public MultiThreadedConformanceChecker call()
    {
        long start = System.nanoTime();
        psp = new PSP(this.logAutomaton,this.modelAutomaton);
        calculateOneOptimalAlignments(Integer.MAX_VALUE);
        this.timeOneOptimal = TimeUnit.MILLISECONDS.convert(System.nanoTime()-start, TimeUnit.NANOSECONDS);
        return this;
    }

    public MultiThreadedConformanceChecker(String path, String model, String log, int numThreads) throws Exception
    {
        Automaton logAutomaton = new ImportEventLog().createDAFSAfromLogFile(path + log);
        Automaton modelAutomaton = new ImportProcessModel().createAutomatonFromPNMLorBPMNFile(path+model,logAutomaton.eventLabels(), logAutomaton.inverseEventLabels());
        this.nThreads = numThreads;
        long start = System.currentTimeMillis();
        this.logAutomaton = logAutomaton;
        //System.out.println(logAutomaton.eventLabels());
        this.modelAutomaton = modelAutomaton;
        //System.out.println(modelAutomaton.eventLabels());
        Node source = new Node(logAutomaton.source(), modelAutomaton.source(),
                new Configuration(new IntArrayList(), new IntIntHashMap(), new IntArrayList(), new IntIntHashMap(), new FastList<au.qut.apromore.psp.Synchronization>(),
                        new IntArrayList(), new IntArrayList()), 0);
        source.configuration().logIDs().add(logAutomaton.sourceID()); source.configuration().modelIDs().add(modelAutomaton.sourceID());
        psp = new PSP(HashBiMap.create(), new UnifiedSet<Arc>(), source.hashCode(), logAutomaton, modelAutomaton);
        psp.nodes().put(source.hashCode(), source);
        calculateOneOptimalAlignments(Integer.MAX_VALUE);
        //calculateAllOptimalAlignments(stateLimit);
        this.timeOneOptimal = System.currentTimeMillis() - start;
    }

    public MultiThreadedConformanceChecker(Automaton logAutomaton, Automaton modelAutomaton, int stateLimit, int numThreads)
    {
        this.nThreads = numThreads;
        long start = System.currentTimeMillis();
        this.logAutomaton = logAutomaton;
        //System.out.println(logAutomaton.eventLabels());
        this.modelAutomaton = modelAutomaton;
        psp = new PSP(logAutomaton,modelAutomaton);
        calculateOneOptimalAlignments(stateLimit);
        //calculateAllOptimalAlignments(stateLimit);
        this.timeOneOptimal = System.currentTimeMillis() - start;
    }

    private void calculateOneOptimalAlignments(int stateLimit)
    {
        long start = System.nanoTime();
        double time = 0;
        double rawFitnessCost = 0;
        double numStates = 0;
        double numAlignments = 0;
        double traceFitness = 0;
        double moveModelFitness = 0;
        double moveLogFitness = 0;
        double traceLength = 0;
        double queuedStates = 0;
        logSize =  0;
        for(IntArrayList trace : logAutomaton.caseTracesMapping.keySet())
        {
            logSize += logAutomaton.caseTracesMapping.get(trace).size();
        }

        if(this.nThreads>1) {
            executor = Executors.newFixedThreadPool(this.nThreads);
            //ExecutorService executor = Executors.newCachedThreadPool();
            modelAutomaton.setMinNumberOfModelMoves((int) new SingleTraceConformanceChecker(new IntArrayList(), modelAutomaton, logAutomaton.eventLabels(), logAutomaton.inverseEventLabels(), logAutomaton.caseIDs).res.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST).intValue());
            FastList<Future<CheckConformanceCallable>> futures = new FastList<>();
            for (IntIntHashMap finalConfiguration : logAutomaton.configCasesMapping().keySet()) {
                for (IntArrayList trace : logAutomaton.configCasesMapping().get(finalConfiguration)) {
                    CheckConformanceCallable confTask = new CheckConformanceCallable(psp, logAutomaton, modelAutomaton, trace, finalConfiguration, stateLimit, useVisited);
                    futures.add(executor.submit(confTask));
                }
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
                try {
                    executor.awaitTermination(10, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                }
            }
            for (Future<CheckConformanceCallable> fut : futures) {
                CheckConformanceCallable confResult = null;
                try {
                    confResult = fut.get();
                } catch (Exception e) {
                    System.out.println(e.getCause());
                    e.printStackTrace();
                    System.exit(1);
                }
                this.resOneOptimal.add(confResult.result);
                this.useVisited = confResult.useVisited;
                IntArrayList traces = new IntArrayList();
                traces.addAll(logAutomaton.caseTracesMapping.get(confResult.traceLabels).toArray());
                this.caseReplayResultMapping.put(traces, confResult.result);
                this.traceAlignmentsMapping.put(confResult.traceLabels, confResult.result);
            }
        }
        else
        {
            for (IntIntHashMap finalConfiguration : logAutomaton.configCasesMapping().keySet()) {
                for (IntArrayList trace : logAutomaton.configCasesMapping().get(finalConfiguration)) {
                    CheckConformanceCallable confTask = new CheckConformanceCallable(psp, logAutomaton, modelAutomaton, trace, finalConfiguration, stateLimit, useVisited);
                    confTask.calculatePartiallySynchronizedPathWithLeastSkips();
                    this.resOneOptimal.add(confTask.result);
                    this.useVisited = confTask.useVisited;
                    IntArrayList traces = new IntArrayList();
                    traces.addAll(logAutomaton.caseTracesMapping.get(confTask.traceLabels).toArray());
                    this.caseReplayResultMapping.put(traces, confTask.result);
                    this.traceAlignmentsMapping.put(confTask.traceLabels, confTask.result);
                }
            }
        }
        int count = 0;

        for(AllSyncReplayResult result : this.resOneOptimal)
        {

            if(result.isReliable())
            {
                count++;
                time += (result.getInfo().get(PNMatchInstancesRepResult.TIME) * result.getTraceIndex().size());
                rawFitnessCost += (result.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST) * result.getTraceIndex().size());
                numStates += (result.getInfo().get(PNMatchInstancesRepResult.NUMSTATES) * result.getTraceIndex().size());
                numAlignments += result.getInfo().get(PNMatchInstancesRepResult.NUMALIGNMENTS);
                traceFitness += (result.getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS) * result.getTraceIndex().size());
                moveModelFitness += (result.getInfo().get(PNRepResult.MOVEMODELFITNESS) * result.getTraceIndex().size());
                moveLogFitness += (result.getInfo().get(PNRepResult.MOVELOGFITNESS) * result.getTraceIndex().size());
                traceLength += (result.getInfo().get(PNMatchInstancesRepResult.ORIGTRACELENGTH) * result.getTraceIndex().size());
                queuedStates += (result.getInfo().get(PNMatchInstancesRepResult.QUEUEDSTATE) * result.getTraceIndex().size());
            }
        }
        cost = (int) rawFitnessCost;
        this.resOneOptimal.addInfo(PNMatchInstancesRepResult.TIME, "" + (time / logSize));
        this.resOneOptimal.addInfo(PNMatchInstancesRepResult.RAWFITNESSCOST, "" + (rawFitnessCost / logSize));
        this.resOneOptimal.addInfo(PNMatchInstancesRepResult.NUMSTATES, "" + (numStates / logSize));
        this.resOneOptimal.addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS, "" + numAlignments);
        this.resOneOptimal.addInfo(PNMatchInstancesRepResult.TRACEFITNESS, "" +  (traceFitness / logSize));
        this.resOneOptimal.addInfo(PNRepResult.MOVEMODELFITNESS, "" + (moveModelFitness  / logSize));
        this.resOneOptimal.addInfo(PNRepResult.MOVELOGFITNESS, "" + (moveLogFitness / logSize));
        this.resOneOptimal.addInfo(PNRepResult.ORIGTRACELENGTH, "" + traceLength / logSize);
        this.resOneOptimal.addInfo(PNMatchInstancesRepResult.QUEUEDSTATE, "" + queuedStates / logSize);
        this.timeOneOptimal =TimeUnit.MILLISECONDS.convert(System.nanoTime() - start,TimeUnit.NANOSECONDS);
        //System.out.println(this.resOneOptimal.getInfo());
    }
}
