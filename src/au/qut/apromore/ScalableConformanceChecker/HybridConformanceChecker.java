package au.qut.apromore.ScalableConformanceChecker;

import au.qut.apromore.importer.DecomposingTRImporter;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;

import java.text.DecimalFormat;

public class HybridConformanceChecker
{
    private DecomposingTRImporter decompositions;
    private PNMatchInstancesRepResult alignmentResult;
    private int numThreads;

    public HybridConformanceChecker(String path, String log, String model) throws Exception
    {
        decompositions = new DecomposingTRImporter(path, model, log);
        computeAlignments();
    }

    public HybridConformanceChecker(DecomposingTRImporter importer) throws Exception
    {
        this.decompositions = importer;
        computeAlignments();
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

    public PNMatchInstancesRepResult getAlignments() throws Exception
    {
        if(alignmentResult==null) computeAlignments();
        return alignmentResult;
    }

}
