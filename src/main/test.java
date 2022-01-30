package main;

import au.qut.apromore.ScalableConformanceChecker.DecomposingTRConformanceChecker;
import au.qut.apromore.ScalableConformanceChecker.HybridConformanceChecker;
import au.qut.apromore.ScalableConformanceChecker.ScalableConformanceChecker;
import au.qut.apromore.ScalableConformanceChecker.TRConformanceChecker;
import au.qut.apromore.importer.DecomposingTRImporter;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;

import static nl.tue.storage.hashing.impl.MurMur3HashCodeProvider.C1;
import static nl.tue.storage.hashing.impl.MurMur3HashCodeProvider.C2;

public class test
{
    public static void main(String[] args) throws Exception
    {
        IntHashSet setA = IntHashSet.newSetWith(0,1,2);
        IntHashSet setB = IntHashSet.newSetWith(1,2);
        System.out.println(setA.equals(setB));
        System.out.println(setA.hashCode() + " =? " + setB.hashCode());
        System.out.println(compress(setA) + " =? " + compress(setB));
        UnifiedSet<Integer> setA2 = UnifiedSet.newSetWith(0,1,2);
        UnifiedSet<Integer> setB2 = UnifiedSet.newSetWith(1,2);
        System.out.println(setA2.equals(setB2));
        System.out.println(setA2.hashCode() + " =? " + setB2.hashCode());


        /*String path = "/Users/dreissner/Documents/Evaluations/TandemRepeatsPaper/public/IM/";
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
            }*//*

        }*/
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

    /** Returns the MurmurHash3_x86_32 hash. */
    protected static int compress(IntHashSet set) {

        int[] data = set.toArray();

        //data[2] = this.stateModelrep;
        int hash =0;
        final int len = data.length;

        int k1;
        for (int i = 0; i < len - 1; i++) {
            // little endian load order
            k1 = data[i];
            k1 *= C1;

            k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
            k1 *= C2;

            hash ^= k1;

            hash = (hash << 13) | (hash >>> 19); // ROTL32(h1,13);
            hash = hash * 5 + 0xe6546b64;

        }

        // tail
        k1 = data[len - 1];
        k1 *= C1;
        k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
        k1 *= C2;
        hash ^= k1;

        // finalization
        hash ^= len;

        // fmix(h1);
        hash ^= hash >>> 16;
        hash *= 0x85ebca6b;
        hash ^= hash >>> 13;
        hash *= 0xc2b2ae35;
        hash ^= hash >>> 16;

        return hash;
    }
}
