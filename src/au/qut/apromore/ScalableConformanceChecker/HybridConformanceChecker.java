package au.qut.apromore.ScalableConformanceChecker;

import au.qut.apromore.importer.DecomposingTRImporter;
import au.qut.apromore.psp.Configuration;
import au.qut.apromore.psp.Synchronization;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;

import java.text.DecimalFormat;
import java.util.List;

public class HybridConformanceChecker
{
    private DecomposingTRImporter decompositions;
    private PNMatchInstancesRepResult alignmentResult;
    private int numThreads;
    private List<List<Synchronization>> alignmentsWithDifferentFormatting;

    public HybridConformanceChecker(String path, String log, String model) throws Exception
    {
        decompositions = new DecomposingTRImporter(path, model, log);
        computeAlignments();
    }

    public HybridConformanceChecker(DecomposingTRImporter importer) throws Exception
    {
        this.decompositions = importer;
        computeAlignmentsSilent();
    }

    public HybridConformanceChecker(DecomposingTRImporter importer, int numThreads) throws Exception
    {
        this.decompositions = importer;
        this.numThreads = numThreads;
        computeAlignmentsMT();
    }

    private void computeAlignmentsMT() throws Exception
    {
        DecimalFormat df2 = new DecimalFormat("#.###");
        if(decompositions.doDecomposition)
            System.out.println("Avg. Reduction per case: " + df2.format(decompositions.avgReduction) + "; Avg. S-Component RGSize vs. Original RGSize: " + decompositions.sumRGsize + " vs " + decompositions.modelFSM.totalSize);
        else
            System.out.println("Avg. Reduction per case: " + df2.format(decompositions.avgReduction) + "; Process model is not concurrent");
        if(decompositions.applyTRRule && decompositions.applySCompRule)
        {
            System.out.println("Applying TR-SComp");
            MTDecomposingTRConformanceChecker TRSComp = new MTDecomposingTRConformanceChecker(decompositions, numThreads);
            this.alignmentResult = TRSComp.alignmentResult;
        }
        else if(decompositions.applyTRRule)
        {
            System.out.println("Applying TR");
            decompositions.prepareTR();
            MultiThreadedTRConformanceChecker TR = new MultiThreadedTRConformanceChecker(decompositions.dafsa, decompositions.modelFSM, Integer.MAX_VALUE, numThreads);
            this.alignmentResult = TR.resOneOptimal();
        }
        else if(decompositions.applySCompRule)
        {
            System.out.println("Applying SComp ");
            MultiThreadedDecomposedConformanceChecker SComp = new MultiThreadedDecomposedConformanceChecker(decompositions.prepareSComp(),numThreads);
            this.alignmentResult = SComp.alignmentResult;
        }
        else
        {
            System.out.println("Applying Automata Conformance");
            decompositions.prepareAutomata();
            MultiThreadedConformanceChecker Automata = new MultiThreadedConformanceChecker(decompositions.dafsa,decompositions.modelFSM, Integer.MAX_VALUE,numThreads);
            this.alignmentResult = Automata.resOneOptimal;
        }
        //System.out.println(alignmentResult.getInfo());
    }

    private void computeAlignments() throws Exception
    {
        DecimalFormat df2 = new DecimalFormat("#.###");
        if(decompositions.doDecomposition)
            System.out.println("Avg. Reduction per case: " + df2.format(decompositions.avgReduction) + "; Avg. S-Component RGSize vs. Original RGSize: " + decompositions.sumRGsize + " vs " + decompositions.modelFSM.totalSize);
        else
            System.out.println("Avg. Reduction per case: " + df2.format(decompositions.avgReduction) + "; Process model is not concurrent");
        if(decompositions.applyTRRule && decompositions.applySCompRule)
        {
            System.out.println("Applying TR-SComp");
            DecomposingTRConformanceChecker TRSComp = new DecomposingTRConformanceChecker(decompositions);
            this.alignmentResult = TRSComp.alignmentResult;
        }
        else if(decompositions.applyTRRule)
        {
            System.out.println("Applying TR");
            decompositions.prepareTR();
            TRConformanceChecker TR = new TRConformanceChecker(decompositions.dafsa, decompositions.modelFSM, Integer.MAX_VALUE);
            this.alignmentResult = TR.resOneOptimal();
        }
        else if(decompositions.applySCompRule)
        {
            System.out.println("Applying SComp ");
            DecomposingConformanceChecker SComp = new DecomposingConformanceChecker(decompositions.prepareSComp());
            this.alignmentResult = SComp.alignmentResult;
        }
        else
        {
            System.out.println("Applying Automata Conformance");
            decompositions.prepareAutomata();
            ScalableConformanceChecker Automata = new ScalableConformanceChecker(decompositions.dafsa,decompositions.modelFSM, Integer.MAX_VALUE);
            this.alignmentResult = Automata.resOneOptimal();
        }
        //System.out.println(alignmentResult.getInfo());
    }

    private void computeAlignmentsSilent() throws Exception
    {
        //DecimalFormat df2 = new DecimalFormat("#.###");
        //if(decompositions.doDecomposition)
        //    System.out.println("Avg. Reduction per case: " + df2.format(decompositions.avgReduction) + "; Avg. S-Component RGSize vs. Original RGSize: " + decompositions.sumRGsize + " vs " + decompositions.modelFSM.totalSize);
        //else
        //    System.out.println("Avg. Reduction per case: " + df2.format(decompositions.avgReduction) + "; Process model is not concurrent");
        if(decompositions.applyTRRule && decompositions.applySCompRule)
        {
            //System.out.println("Applying TR-SComp");
            DecomposingTRConformanceChecker TRSComp = new DecomposingTRConformanceChecker(decompositions);
            this.alignmentResult = TRSComp.alignmentResult;
        }
        else if(decompositions.applyTRRule)
        {
            //System.out.println("Applying TR");
            decompositions.prepareTR();
            TRConformanceChecker TR = new TRConformanceChecker(decompositions.dafsa, decompositions.modelFSM, Integer.MAX_VALUE);
            this.alignmentResult = TR.resOneOptimal();
        }
        else if(decompositions.applySCompRule)
        {
            //System.out.println("Applying SComp ");
            DecomposingConformanceChecker SComp = new DecomposingConformanceChecker(decompositions.prepareSComp());
            this.alignmentResult = SComp.alignmentResult;
        }
        else
        {
            //System.out.println("Applying Automata Conformance");
            decompositions.prepareAutomata();
            ScalableConformanceChecker Automata = new ScalableConformanceChecker(decompositions.dafsa,decompositions.modelFSM, Integer.MAX_VALUE);
            this.alignmentResult = Automata.resOneOptimal();
        }
        //System.out.println(alignmentResult.getInfo());
    }

    public PNMatchInstancesRepResult getAlignments() throws Exception
    {
        if(alignmentResult==null) computeAlignments();
        return alignmentResult;
    }

    public List<List<Synchronization>> getAlignmentsWithDifferentFormatting() throws Exception
    {
        if(this.alignmentsWithDifferentFormatting==null)
        {
            List<Synchronization> alignmentWithDifferentFormatting;
            Synchronization sync;
            alignmentsWithDifferentFormatting = new FastList<>();
            PNMatchInstancesRepResult alignments = this.getAlignments();
            for(AllSyncReplayResult alignment : alignments)
            {
                alignmentWithDifferentFormatting=new FastList<>();
                alignmentsWithDifferentFormatting.add(alignmentWithDifferentFormatting);
                List<Object> alignmentLabels = alignment.getNodeInstanceLst().get(0);
                List<StepTypes> alignmentSteps = alignment.getStepTypesLst().get(0);
                for(int pos=0;pos<alignmentLabels.size();pos++)
                {
                    Configuration.Operation operation = mapStepTypeToOperation(alignmentSteps.get(pos));
                    int eventLog=-1;
                    int eventModel=-1;
                    String label = (String) alignmentLabels.get(pos);
                    if(operation==Configuration.Operation.MATCH)
                    {
                        eventLog=decompositions.globalInverseLabels.get(label);
                        eventModel=eventLog;
                    }
                    else if(operation== Configuration.Operation.LHIDE)
                    {
                        eventLog=decompositions.globalInverseLabels.get(label);
                    }
                    else
                    {
                        eventModel=decompositions.globalInverseLabels.get(label);
                    }
                    sync=new Synchronization(operation,eventLog,eventModel);
                    alignmentWithDifferentFormatting.add(sync);
                }
            }
        }
        return this.alignmentsWithDifferentFormatting;
    }

    public Configuration.Operation mapStepTypeToOperation(StepTypes stepType)
    {
        Configuration.Operation operation=null;
        if(stepType==StepTypes.LMGOOD)  operation=Configuration.Operation.MATCH;
        else if(stepType== StepTypes.L) operation= Configuration.Operation.LHIDE;
        else if(stepType==StepTypes.MREAL) operation= Configuration.Operation.RHIDE;
        return operation;
    }
}
