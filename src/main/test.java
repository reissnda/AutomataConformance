package main;

import au.qut.apromore.ScalableConformanceChecker.DecomposingTRConformanceChecker;
import au.qut.apromore.ScalableConformanceChecker.HybridConformanceChecker;
import au.qut.apromore.ScalableConformanceChecker.ScalableConformanceChecker;
import au.qut.apromore.ScalableConformanceChecker.TRConformanceChecker;
import au.qut.apromore.importer.DecomposingTRImporter;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;

public class test
{
    public static void main(String[] args) throws Exception
    {
        String path = "/Users/dreissner/Documents/Evaluations/TandemRepeatsPaper/public/IM/";
        String log = "15.xes.gz";
        String model = "15 .pnml";
        //ScalableConformanceChecker automata = new ScalableConformanceChecker(path,log,model,Integer.MAX_VALUE);
       // DecomposingTRImporter decompositions = new DecomposingTRImporter();
        //decompositions.importAndDecomposeModelAndLogForConformanceChecking(path, model, log);
        //DecomposingTRConformanceChecker TRSComp = new DecomposingTRConformanceChecker(decompositions);
        //TRConformanceChecker tr = new TRConformanceChecker(path,log,model,Integer.MAX_VALUE);
        //System.out.println(TRSComp.alignmentResult.getInfo());
        HybridConformanceChecker hybrid = new HybridConformanceChecker(path, log, model);
        for(AllSyncReplayResult res : hybrid.getAlignments())
        {
            int caseID = res.getTraceIndex().first();
            /*
            for(AllSyncReplayResult tres : tr.resOneOptimal())
            {
                if(tres.getTraceIndex().contains(caseID))
                {
                    if(tres.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST)>res.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST))
                    {
                        System.out.println(tres.getInfo());
                        System.out.println(res.getInfo());
                        System.out.println(printAlignment(tres));
                        System.out.println(printAlignment(res));
                    }
                }
            }*/

        }
    }

    public static String printAlignment(AllSyncReplayResult res)
    {
        String alignment = "[";
        for(int pos=0;pos<res.getNodeInstanceLst().get(0).size();pos++)
        {
            alignment += "(";
            if(res.getStepTypesLst().get(0).get(pos)== StepTypes.LMGOOD)
                alignment+= "M";
            else if(res.getStepTypesLst().get(0).get(pos)== StepTypes.L)
                alignment+="L";
            else alignment+="R";
            alignment+= "," + res.getNodeInstanceLst().get(0).get(pos) + "), ";
        }
        alignment = alignment.substring(0, alignment.length()-2) + "]";
        return alignment;
    }
}
