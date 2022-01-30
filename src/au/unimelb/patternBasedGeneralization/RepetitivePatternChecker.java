package au.unimelb.patternBasedGeneralization;

import au.qut.apromore.ScalableConformanceChecker.HybridConformanceChecker;
import au.qut.apromore.ScalableConformanceChecker.ReducedResult;
import au.qut.apromore.ScalableConformanceChecker.TRConformanceChecker;
import au.qut.apromore.automaton.Automaton;
import au.qut.apromore.importer.DecomposingTRImporter;
import au.qut.apromore.psp.Configuration;
import au.qut.apromore.psp.Synchronization;
import au.unimelb.pattern.ReducedTrace;
import au.unimelb.pattern.RepetitivePattern;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.BooleanArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

import java.util.List;

public class RepetitivePatternChecker
{
    private DecomposingTRImporter decompositions;
    ReducedTrace redTrace;
    Automaton reachGraph;
    private List<Synchronization> alignment;

    public RepetitivePatternChecker(ReducedTrace redTrace, Automaton reachGraph)
    {
        this.redTrace = redTrace;
        this.reachGraph = reachGraph;
        checkConformanceOfRepetitivePatterns();
    }

    public RepetitivePatternChecker(ReducedTrace redTrace, DecomposingTRImporter decompositions) throws Exception {
        this.redTrace = redTrace;
        this.decompositions=decompositions;
        checkConformanceOfRepetitivePatternsWithHybridApproach();
    }

    private void checkConformanceOfRepetitivePatternsWithHybridApproach() throws Exception {
        if(redTrace.getRepetitivePatterns().isEmpty()) return;
        DecomposingTRImporter decompositionsForReducedTrace = new DecomposingTRImporter(decompositions, redTrace);
        HybridConformanceChecker conformanceChecker = new HybridConformanceChecker(decompositionsForReducedTrace);
        this.alignment = conformanceChecker.getAlignmentsWithDifferentFormatting().get(0);
        for(RepetitivePattern repPattern : redTrace.getRepetitivePatterns())
        {
            checkPatternFulfillment(repPattern,alignment);
        }
    }

    private void checkConformanceOfRepetitivePatterns()
    {
        if(redTrace.getRepetitivePatterns().isEmpty()) return;
        ReducedResult res = TRConformanceChecker.calculateReducedAlignment(redTrace.getDecoder(),redTrace.getDafsa(),reachGraph);
        if(res.getFinalNode()==null)
            System.out.println("Problem");
        this.alignment = res.getFinalNode().configuration().sequenceSynchronizations();
        for(RepetitivePattern repPattern : redTrace.getRepetitivePatterns())
        {
            checkPatternFulfillment(repPattern,alignment);
        }
    }

    private void checkPatternFulfillment(RepetitivePattern repPattern, List<Synchronization> alignment)
    {
        List<Synchronization> partialAlignment = determineRelevantPartialAlignment(alignment, repPattern.getStartPos(),repPattern.getLength());
        BooleanArrayList labelRepeated = new BooleanArrayList();
        int fulfillment=0;
        //if(partialAlignment.size()==3)
        //    System.out.println("Problem");
        //partialAlignment = determineRelevantPartialAlignment(alignment, repPattern.getStartPos(),repPattern.getLength());
        for(int pos=0;pos<repPattern.getLength()/2;pos++)
        {
            if(partialAlignment.get(pos).operation()== Configuration.Operation.MATCH && partialAlignment.get(pos+repPattern.getLength()/2).operation()== Configuration.Operation.MATCH)
            {
                labelRepeated.add(true);
                fulfillment++;
            }
            else
            {
                labelRepeated.add(false);
            }
        }
        repPattern.setPartialFulfillment(fulfillment);
    }

    private List<Synchronization> determineRelevantPartialAlignment(List<Synchronization> alignment, int start, int length)
    {
        List<Synchronization> partialAlignment = new FastList<>();
        int trPos=0;
        IntArrayList trPositions = new IntArrayList();
        for(int pos=start;pos<start+length;pos++) trPositions.add(pos);
        for(Synchronization sync : alignment)
        {
            if(sync.operation()!= Configuration.Operation.RHIDE)
            {
                    if(trPositions.contains(++trPos)) partialAlignment.add(sync);
                    //trPos++;
            }
        }
        return partialAlignment;
    }

}
