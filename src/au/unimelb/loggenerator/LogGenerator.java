package au.unimelb.loggenerator;

import au.qut.apromore.importer.DecodeTandemRepeats;
import com.google.common.collect.HashBiMap;
import name.kazennikov.dafsa.IntDAFSAInt;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.*;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.io.FileOutputStream;
import java.util.*;

public class LogGenerator
{
    private final String conceptname = "concept:name";
    private final String lifecycle = "lifecycle:transition";

    public LogGenerator(Map<List<String>,Integer> tracesWithCounts, String file) throws Exception
    {
        XFactory xFac = new XFactoryNaiveImpl();
        XLog log = xFac.createLog();
        XTrace newTr;
        //XExtensionManager xEvMan = XExtensionManager.instance();
        //xEvMan.
        XExtension xtend = XConceptExtension.instance();
        XAttributeLiteral xAttrLiteral = xFac.createAttributeLiteral(conceptname, "", xtend);
        XAttributeLiteral xLc = xFac.createAttributeLiteral(lifecycle, "", xtend);
        for(List<String> trace : tracesWithCounts.keySet())
        {
            for(int i=0;i<tracesWithCounts.get(trace);i++) {
                newTr = xFac.createTrace();
                for (String activity : trace) {
                    XAttributeMap xAttr = xFac.createAttributeMap();
                    xAttrLiteral = xFac.createAttributeLiteral(conceptname, "", xtend);
                    xAttrLiteral.setValue(activity);
                    xLc.setValue("complete");
                    xAttr.put(conceptname, xAttrLiteral);
                    xAttr.put(lifecycle, xLc);
                    XEvent event = xFac.createEvent(xAttr);
                    newTr.add(event);
                }
                log.add(newTr);
            }
        }

        XesXmlGZIPSerializer serializer = new XesXmlGZIPSerializer();
        serializer.serialize(log,new FileOutputStream(file));


    }

    public static void main(String[] args) throws Exception
    {
        //generateRunningExampleLog();
        //System.out.println("Running example log export finished");
        //generateNegativeEventsLog();
        //generateAntiAlignmentsLog();
        //generateAntiAlignmentsRepetitiveLog();
        //System.out.println("Repetitive Log export finished");
        //generateAntiAlignmentsConcurrentLog();
        //System.out.println("Concurrent Log export finished");
        //generateAntiAlignmentsCompositeLog();
        //System.out.println("Composite log export finished");
        generateTRoverApproximationLog();
        System.out.println("Export of over-approximation log finished.");
        //generateTandemRepeatsRunningExampleLog();
        //System.out.println("Export of TR running example log finished");
    }

    private static void generateNegativeEventsLog() throws Exception
    {
        Map<List<String>,Integer> tracesWithCounts = new UnifiedMap<>();
        tracesWithCounts.put(FastList.newList(asList("a,c,d,e,k")),113);
        tracesWithCounts.put(FastList.newList(asList("a,b,i,g,h,j,k")),110);
        tracesWithCounts.put(FastList.newList(asList("a,b,g,i,h,j,k")),74);
        tracesWithCounts.put(FastList.newList(asList("a,b,g,h,i,j,k")),63);
        tracesWithCounts.put(FastList.newList(asList("a,c,d,f,c,d,e,k")),39);
        tracesWithCounts.put(FastList.newList(asList("a,c,d,f,b,i,g,h,j,k")),30);
        tracesWithCounts.put(FastList.newList(asList("a,c,d,f,b,g,i,h,j,k")),19);
        tracesWithCounts.put(FastList.newList(asList("a,c,d,f,b,g,h,i,j,k")),16);
        tracesWithCounts.put(FastList.newList(asList("a,c,d,f,c,d,f,b,g,h,i,j,k")),8);
        tracesWithCounts.put(FastList.newList(asList("a,c,d,f,c,d,f,c,d,e,k")),8);
        tracesWithCounts.put(FastList.newList(asList("a,c,d,f,c,d,f,b,i,g,h,j,k")),8);
        tracesWithCounts.put(FastList.newList(asList("a,c,d,f,c,d,f,b,g,i,h,j,k")),5);
        tracesWithCounts.put(FastList.newList(asList("a,c,d,f,c,d,f,c,d,f,b,i,g,h,j,k")),3);
        tracesWithCounts.put(FastList.newList(asList("a,c,d,f,c,d,f,c,d,f,c,d,e,k")),2);
        tracesWithCounts.put(FastList.newList(asList("a,c,d,f,c,d,f,c,d,f,b,g,h,i,j,k")),2);
        String file = "/Users/dreissner/Documents/Evaluations/PatternBasedGeneralizationPaper/QualitativeEvaluation/NegativeEventsDataset/artificial.xes.gz";
        new LogGenerator(tracesWithCounts,file);
    }

    private static void generateTRoverApproximationLog() throws Exception
    {
        Map<List<String>,Integer> tracesWithCounts = new UnifiedMap<>();
        //String repeatingSequenceLabels = "ABCDE";
        //Set<String> permutations = generatePerm(repeatingSequenceLabels);
        //System.out.println("#Permutations: " + permutations.size());
        //for(String permutation : permutations)
        //{
        //    tracesWithCounts.put(FastList.newList(asList(extendPermutation(permutation,4))),1);
        //}
        tracesWithCounts.put(FastList.newList(asList(extendPermutation("BCDA",7))),1);
        //tracesWithCounts.put(FastList.newList(asList(extendPermutation("BCDEFGHA",15))),1);
        //tracesWithCounts.put(FastList.newList(asList("A,A,A,A,A,A,B,B,B,B,B,B,B,C,C,C,C")),1);
        //tracesWithCounts.put(FastList.newList(asList("A,B,C,A,B,C,A,B,C")),1);
        //tracesWithCounts.put(FastList.newList(asList("A,B,C,A,B,C,A,B,C,A,B,C,A,B,C")),1);
        //tracesWithCounts.put(FastList.newList(asList("C,A,B,C,A,B,C,A,B,C,A,B,C,A,B")),1);
        //tracesWithCounts.put(FastList.newList(asList("B,C,A,B,C,A,B,C,A,B,C,A,B,C,A")),1);
        //tracesWithCounts.put(FastList.newList(asList("B,C,A,B,C,A,B,C,A,B,C,A,B,C,A,B,C,A,B,C,A,B,C,A,B,C,A,B,C,A")),1);
        //tracesWithCounts.put(FastList.newList(asList("D,D,D,D,D,E,E,E,E,E,E,E,F,F,F,F,F")),1);
        //tracesWithCounts.put(FastList.newList(asList("D,E,F,D,E,F,D,E,F,D,E,F")),1);
        //tracesWithCounts.put(FastList.newList(asList("F,D,E,F,D,E,F,D,E,F,D,E")),1);
        //tracesWithCounts.put(FastList.newList(asList("E,F,D,E,F,D,E,F,D,E,F,D")),1);
        //tracesWithCounts.put(FastList.newList(asList("E,F,D,E,F,D,E,F,D,E,F,D")),1);
        //tracesWithCounts.put(FastList.newList(asList("Z,V,W,X,Y,Z,V,W,X,Y,Z,V,W,X,Y,Z,V,W,X,Y,Z,V,W,X,Y,Z,V,W,X,Y")),1);
        //tracesWithCounts.put(FastList.newList(asList("Z,V,W,X,Y,Z,V,W,X,Y,Z,V,W,X,Y,Z,V,W,X,Y,Z,V,W,X,Y,Z,V,W,X,Y,Z,V,W,X,Y,Z,V,W,X,Y")),1);
        //tracesWithCounts.put(FastList.newList(asList("Z,V,W,X,Y,Z,V,W,X,Y,Z,V,W,X,Y,Z,V,W,X,Y,Z,V,W,X,Y,Z,V,W,X,Y")),1);
        //tracesWithCounts.put(FastList.newList(asList("Z,W,V,Y,X,Z,W,V,Y,X,Z,W,V,Y,X")),1);
        //tracesWithCounts.put(FastList.newList(asList("G,F,E,D,C,B,A,G,F,E,D,C,B,A")),1);
        //tracesWithCounts.put(FastList.newList(asList("G,F,E,D,C,B,A,G,F,E,D,C,B,A")),1);
        //tracesWithCounts.put(FastList.newList(asList("G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A")),1);
        //tracesWithCounts.put(FastList.newList(asList("G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A")),1);
        //tracesWithCounts.put(FastList.newList(asList("G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A")),1);
        //tracesWithCounts.put(FastList.newList(asList("G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A")),1);
        //tracesWithCounts.put(FastList.newList(asList("G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A")),1);
        //tracesWithCounts.put(FastList.newList(asList("G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A")),1);
        //tracesWithCounts.put(FastList.newList(asList("G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A")),1);
        //tracesWithCounts.put(FastList.newList(asList("G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A,G,F,E,D,C,B,A")),1);
        //tracesWithCounts.put(FastList.newList(asList("G,F,A,B,C,D,E,G,F,A,B,C,D,E,G,F,A,B,C,D,E,G,F,A,B,C,D,E,G,F,A,B,C,D,E,G,F,A,B,C,D,E,G,F,A,B,C,D,E,G,F,A,B,C,D,E,G,F,A,B,C,D,E,G,F,A,B,C,D,E")),1);
        //tracesWithCounts.put(FastList.newList(asList("G,F,E,B,C,D,A,G,F,E,B,C,D,A,G,F,E,B,C,D,A,G,F,E,B,C,D,A,G,F,E,B,C,D,A,G,F,E,B,C,D,A,G,F,E,B,C,D,A,G,F,E,B,C,D,A,G,F,E,B,C,D,A,G,F,E,B,C,D,A")),1);
        String file = "/Users/dreissner/Documents/Evaluations/TandemRepeatsPaper/OverApproximation/1.xes.gz";
        new LogGenerator(tracesWithCounts,file);
    }

    private static String extendPermutation(String permutation, int repetitions)
    {
        String result = "";
        String repeatingSequence="";
        for(int pos=0;pos<permutation.length();pos++)
        {
            repeatingSequence += permutation.charAt(pos) + ",";
        }
        //repeatingSequence=repeatingSequence.substring(0,repeatingSequence.length()-1);
        for(int rep=0;rep<repetitions;rep++)
        {
            result += repeatingSequence;
        }
        result=result.substring(0,result.length()-1);
        return result;
    }

    private static void generateAntiAlignmentsLog() throws Exception
    {
        Map<List<String>,Integer> tracesWithCounts = new UnifiedMap<>();
        tracesWithCounts.put(FastList.newList(asList("A,B,D,E,I")),1207);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,G,H,F,I")),145);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,D,H,F,I")),56);
        tracesWithCounts.put(FastList.newList(asList("A,C,H,D,F,I")),23);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,H,F,I")),28);
        String file = "/Users/dreissner/Documents/Evaluations/PatternBasedGeneralizationPaper/QualitativeEvaluation/AntiAlignmentsDataset/artificial.xes.gz";
        new LogGenerator(tracesWithCounts,file);
    }

    private static void generateAntiAlignmentsRepetitiveLog() throws Exception
    {
        Map<List<String>,Integer> tracesWithCounts = new UnifiedMap<>();
        //tracesWithCounts.put(FastList.newList(asList("A,B,D,E,I")),1000);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,H,G,H,G,H,D,H,H,H,F,I")),250);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,G,G,G,G,G,H,H,H,H,H,H,F,I")),250);
        //tracesWithCounts.put(FastList.newList(asList("A,C,H,H,H,H,H,D,F,I")),500);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,H,G,H,G,H,G,F,I")),250);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,D,D,D,D,D,H,F,I")),250);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,H,D,D,D,D,D,D,F,I")),250);
        String file = "/Users/dreissner/Documents/Evaluations/PatternBasedGeneralizationPaper/QualitativeEvaluation/AntiAlignmentsDataset/RepetitiveLog.xes.gz";
        new LogGenerator(tracesWithCounts,file);
    }

    private static void generateAntiAlignmentsConcurrentLog() throws Exception
    {
        Map<List<String>,Integer> tracesWithCounts = new UnifiedMap<>();
        //tracesWithCounts.put(FastList.newList(asList("A,B,D,E,I")),1000);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,G,H,F,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,D,H,F,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,H,D,G,F,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,H,G,F,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,H,D,F,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,H,G,D,F,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,H,G,F,D,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,H,F,D,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,F,H,G,D,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,F,H,D,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,F,G,H,D,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,F,D,H,G,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,F,H,G,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,H,F,D,G,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,F,H,D,G,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,H,F,G,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,H,D,F,G,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,D,F,H,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,G,F,H,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,F,G,D,H,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,F,D,H,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,F,G,H,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,F,D,G,H,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,H,F,G,D,I")),50);
        String file = "/Users/dreissner/Documents/Evaluations/PatternBasedGeneralizationPaper/QualitativeEvaluation/AntiAlignmentsDataset/ConcurrentLog.xes.gz";
        new LogGenerator(tracesWithCounts,file);
    }

    private static void generateAntiAlignmentsCompositeLog() throws Exception
    {
        Map<List<String>,Integer> tracesWithCounts = new UnifiedMap<>();
        tracesWithCounts.put(FastList.newList(asList("A,B,D,E,I")),1207);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,G,H,F,I")),145);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,D,H,F,I")),56);
        tracesWithCounts.put(FastList.newList(asList("A,C,H,D,F,I")),23);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,H,F,I")),28);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,H,G,H,G,H,D,H,H,H,F,I")),250);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,G,G,G,G,G,H,H,H,H,H,H,F,I")),250);
        //tracesWithCounts.put(FastList.newList(asList("A,C,H,H,H,H,H,D,F,I")),500);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,H,G,H,G,H,G,F,I")),250);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,D,D,D,D,D,H,F,I")),250);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,H,D,D,D,D,D,D,F,I")),250);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,G,H,F,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,D,H,F,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,H,D,G,F,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,H,G,F,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,H,D,F,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,H,G,D,F,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,H,G,F,D,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,H,F,D,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,F,H,G,D,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,F,H,D,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,F,G,H,D,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,F,D,H,G,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,F,H,G,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,H,F,D,G,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,F,H,D,G,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,H,F,G,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,H,D,F,G,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,D,F,H,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,G,F,H,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,F,G,D,H,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,G,F,D,H,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,D,F,G,H,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,F,D,G,H,I")),50);
        tracesWithCounts.put(FastList.newList(asList("A,C,H,F,G,D,I")),50);
        String file = "/Users/dreissner/Documents/Evaluations/PatternBasedGeneralizationPaper/QualitativeEvaluation/AntiAlignmentsDataset/CompositeLog.xes.gz";
        new LogGenerator(tracesWithCounts,file);
    }

    private static void generatePatternGeneralizationRunningExampleLog() throws Exception
    {
        Map<List<String>,Integer> tracesWithCounts = new UnifiedMap<>();
        tracesWithCounts.put(FastList.newList(asList("X,A,B,C")),1000);
        tracesWithCounts.put(FastList.newList(asList("X,A,C,B")),1000);
        tracesWithCounts.put(FastList.newList(asList("A,B,C")),200);
        tracesWithCounts.put(FastList.newList(asList("C,A,B")),200);
        tracesWithCounts.put(FastList.newList(asList("B,A,C")),200);
        tracesWithCounts.put(FastList.newList(asList("X,X,X,X,A,A,A,A,B,C")),1000);
        tracesWithCounts.put(FastList.newList(asList("X,X,A,X,X,A,X,B,C")),500);
        tracesWithCounts.put(FastList.newList(asList("X,A,X,A,X,A,C,B")),200);
        tracesWithCounts.put(FastList.newList(asList("X,A,X,A,X,A,X,A,X,A,C,B")),200);
        String file = "/Users/dreissner/Documents/Evaluations/PatternBasedGeneralizationPaper/RunningExample/RunningExampleLog.xes.gz";
        new LogGenerator(tracesWithCounts,file);
    }

    private static void generateTandemRepeatsRunningExampleLog() throws Exception
    {
        Map<List<String>,Integer> tracesWithCounts = new UnifiedMap<>();
        tracesWithCounts.put(FastList.newList(asList("A,B,C,C,C,C")),1);
        tracesWithCounts.put(FastList.newList(asList("A,B,D,E,E,F,B,D,E,E,F,B,D,E,E,F,B,C")),1);
        tracesWithCounts.put(FastList.newList(asList("A,B,D,F,B,D,F,B,D,F,B,D")),1);
        tracesWithCounts.put(FastList.newList(asList("A,B,D,F,B,D,F,B,D,F,B,D,F,B,D")),1);
        tracesWithCounts.put(FastList.newList(asList("A,B,D,F,B,D,F,B,D,F,B,D,F,B,D,F,B,D")),1);
        tracesWithCounts.put(FastList.newList(asList("A,D,B,F,D,B,F,D,B,F,D,B,F,B,C")),1);
        String file = "/Users/dreissner/Documents/Evaluations/TandemRepeatsPaper/RunningExample/1.xes.gz";
        new LogGenerator(tracesWithCounts,file);
    }

    private static List<String> asList(String listWithCommas)
    {
        String[] stringArray = listWithCommas.split(",");
        FastList<String> list = new FastList<>();
        for(int i=0;i<stringArray.length;i++)
        {
            list.add(stringArray[i]);
        }
        return list;
    }


    public static Set<String> generatePerm(String input)
    {
        Set<String> set = new HashSet<String>();
        if (input == "")
            return set;

        Character a = input.charAt(0);

        if (input.length() > 1)
        {
            input = input.substring(1);

            Set<String> permSet = generatePerm(input);

            for (String x : permSet)
            {
                for (int i = 0; i <= x.length(); i++)
                {
                    set.add(x.substring(0, i) + a + x.substring(i));
                }
            }
        }
        else
        {
            set.add(a + "");
        }
        return set;
    }

}
