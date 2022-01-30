package au.unimelb.patternBasedGeneralization;

import au.qut.apromore.ScalableConformanceChecker.HybridConformanceChecker;
import au.qut.apromore.ScalableConformanceChecker.ScalableConformanceChecker;
import au.qut.apromore.automaton.Automaton;
import au.qut.apromore.importer.DecomposingTRImporter;
import au.qut.apromore.psp.Configuration;
import au.qut.apromore.psp.Synchronization;
import au.unimelb.partialorders.PartialOrder;

import au.unimelb.pattern.ConcurrentPattern;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntBooleanHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;
import sun.jvm.hotspot.utilities.IntArray;

import java.util.List;
import java.util.Map;

public class ConcurrentPatternChecker
{
    private DecomposingTRImporter decompositions;
    private boolean usePartialMatching = true;
    private PartialOrder partialorder;
    private Automaton reachabilityGraph;
    private List<List<Synchronization>> alignments;

    public ConcurrentPatternChecker(PartialOrder partialOrder, Automaton reachabilityGraph) throws Exception
    {
        this.partialorder = partialOrder;
        this.reachabilityGraph = reachabilityGraph;
        checkConformanceForConcurrentPatterns();
    }

    public ConcurrentPatternChecker(PartialOrder partialOrder, Automaton reachabilityGraph, boolean usePartialMatching) throws Exception
    {
        this.partialorder = partialOrder;
        this.usePartialMatching = usePartialMatching;
        this.reachabilityGraph = reachabilityGraph;
        checkConformanceForConcurrentPatterns();
    }

    public ConcurrentPatternChecker(PartialOrder partialOrder, DecomposingTRImporter decompositions, boolean usePartialMatching) throws Exception
    {
        this.partialorder = partialOrder;
        this.usePartialMatching = usePartialMatching;
        this.decompositions = decompositions;
        checkConformanceForConcurrentPatternsWithHybridApproach();
    }

    private void checkConformanceForConcurrentPatternsWithHybridApproach() throws Exception {
        if(!partialorder.representativeTraces().isEmpty()) {
            HybridConformanceChecker conformanceChecker = new HybridConformanceChecker(new DecomposingTRImporter(decompositions,partialorder));
            this.alignments=conformanceChecker.getAlignmentsWithDifferentFormatting();
            for (ConcurrentPattern concurrentPattern : partialorder.getConcurrentPatterns()) {
                if(usePartialMatching)
                    checkConcurrentPatternPartialMatchingBased(concurrentPattern);
                else
                    checkConcurrentPatternInterleavingsBased(concurrentPattern);
            }
        }
    }

    private void checkConformanceForConcurrentPatterns() throws Exception
    {
        if(!partialorder.representativeTraces().isEmpty()) {
            ScalableConformanceChecker conformanceChecker = new ScalableConformanceChecker(partialorder.getDAFSA(), this.reachabilityGraph);
            conformanceChecker.call();
            this.alignments = conformanceChecker.alignmentsWithDifferentFormatting;
            for (ConcurrentPattern concurrentPattern : partialorder.getConcurrentPatterns()) {
                if(usePartialMatching)
                    checkConcurrentPatternPartialMatchingBased(concurrentPattern);
                else
                    checkConcurrentPatternInterleavingsBased(concurrentPattern);
            }
        }
    }

    private void checkConcurrentPatternLabelBased(ConcurrentPattern concurrentPattern)
    {
        IntArrayList trPositions = concurrentPattern.tracePositions();
        IntHashSet concurrentLabels = concurrentPattern.getConcurrentLabels();
        List<List<Synchronization>> relevantPartialAlignments = determineRelevantPartialAlignments(trPositions);
        IntBooleanHashMap labelIsConcurrent = new IntBooleanHashMap();
        int numFulfilledConcurrencies = concurrentLabels.size();
        for(int concurrentLabel : concurrentLabels.toArray())
        {
            labelIsConcurrent.put(concurrentLabel,true);
        }
        for(List<Synchronization> partialAlignment : relevantPartialAlignments)
        {
            for(Synchronization sync : partialAlignment)
            {
                if(labelIsConcurrent.get(sync.eventLog()))
                    if(sync.operation()== Configuration.Operation.LHIDE)
                    {
                        labelIsConcurrent.put(sync.eventLog(), false);
                        numFulfilledConcurrencies--;
                    }
            }
            if(numFulfilledConcurrencies==0) break;
        }
        concurrentPattern.setPartialFulFillment(numFulfilledConcurrencies);
    }

    private void checkConcurrentPatternPartialMatchingBased(ConcurrentPattern concurrentPattern)
    {
        IntArrayList trPositions = concurrentPattern.tracePositions();
        //IntHashSet concurrentLabels = concurrentPattern.getConcurrentLabels();
        List<List<Synchronization>> relevantPartialAlignments = determineRelevantPartialAlignments(trPositions);
        double numMatches = 0;
        double numLHides = 0;
        for(List<Synchronization> partialAlignment : relevantPartialAlignments)
        {
            for(Synchronization sync : partialAlignment)
            {
                if(sync.operation()== Configuration.Operation.LHIDE)
                {
                    numLHides++;
                }
                else numMatches++;
            }
        }
        concurrentPattern.setFulfillment(numMatches / (numMatches+numLHides));
    }

    private void checkConcurrentPatternInterleavingsBased(ConcurrentPattern concurrentPattern)
    {
        IntArrayList trPositions = concurrentPattern.tracePositions();
        //IntHashSet concurrentLabels = concurrentPattern.getConcurrentLabels();
        List<List<Synchronization>> relevantPartialAlignments = determineRelevantPartialAlignments(trPositions);
        double numAllInterleavings = (double) relevantPartialAlignments.size();
        double numMatchingInterleavings=0;
        for(List<Synchronization> partialAlignment : relevantPartialAlignments)
        {
            boolean allMatches = true;
            for(Synchronization sync : partialAlignment)
            {
                if(sync.operation()== Configuration.Operation.LHIDE)
                {
                    allMatches=false;
                    break;
                }
            }
            if(allMatches) numMatchingInterleavings++;
        }
        concurrentPattern.setFulfillment(numMatchingInterleavings / numAllInterleavings);
    }

    private List<List<Synchronization>> determineRelevantPartialAlignments(IntArrayList trPositions)
    {
        List<List<Synchronization>> relevantPartialAlignments = new FastList<>();
        List<Synchronization> partialAlignment;
        int trPos=0;
        for(List<Synchronization> alignment : this.alignments)
        {
            partialAlignment = new FastList<>();
            trPos=0;
            for(Synchronization sync : alignment)
            {
                if(sync.operation()!=Configuration.Operation.RHIDE)
                {
                    if(trPositions.contains(trPos)) partialAlignment.add(sync);
                    trPos++;
                }
            }
            relevantPartialAlignments.add(partialAlignment);
        }
        return relevantPartialAlignments;
    }


}
