package main;

import au.qut.apromore.ScalableConformanceChecker.ScalableConformanceChecker;
import au.qut.apromore.ScalableConformanceChecker.TRConformanceChecker;
import au.qut.apromore.automaton.Automaton;
import au.qut.apromore.importer.*;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.concurrent.*;

public class FitnessWithTRreductionMain
{
    //Main method for finding cost over-approximations
    /*public static void main(String[] args) throws Exception
    {
        String path = "/Users/dreissner/Documents/Evaluations/TandemRepeatsPaper/OverApproximation/";
        String log = "1.xes.gz";
        String model = "1.pnml";
        //String path = "/Users/dreissner/Documents/Paper tests/S-Components Paper/TandemRepeatsTest";
        //String log = "TRtest1.xes";
        //String model = "TRtest1.pnml";
        ImportEventLog logImporter = new ImportEventLog();
        //TRConformanceChecker fitness = new TRConformanceChecker(path, log, model, Integer.MAX_VALUE);
        Automaton logAutomatonWithTRReductions = logImporter.convertLogToAutomatonWithTRFrom(path + "/" + log);
        Automaton modelAutomaton = new ImportProcessModel().createAutomatonFromPNMLorBPMNFile(path + "/" + model, logAutomatonWithTRReductions.eventLabels(), logAutomatonWithTRReductions.inverseEventLabels());
        TRConformanceChecker fitness = new TRConformanceChecker(logAutomatonWithTRReductions, modelAutomaton, Integer.MAX_VALUE);
        Automaton logAutomaton = logImporter.createDAFSAfromLogFile(path+"/"+log);
        modelAutomaton = new ImportProcessModel().createAutomatonFromPNMLorBPMNFile(path + "/" + model, logAutomaton.eventLabels(), logAutomaton.inverseEventLabels());
        ScalableConformanceChecker fitness2 = new ScalableConformanceChecker(logAutomaton, modelAutomaton, Integer.MAX_VALUE);

        int numProblemTraces=0;
        AllSyncReplayResult resTr;
        AllSyncReplayResult res = null;
        int caseID = -1;
        double costDifferenceMax=0;
        AllSyncReplayResult resTrMaxDifference=null;
        AllSyncReplayResult resMaxDifference=null;
        String traceLabelsMax=null;
        //for(DecodeTandemRepeats decoder : fitness.traceAlignmentsMapping.keySet())
        for(IntArrayList trace : fitness.traceAlignmentsMapping.keySet())
        {
            //resTr = fitness.traceAlignmentsMapping.get(decoder);
            resTr = fitness.traceAlignmentsMapping.get(trace);
            if(!resTr.isReliable())
            {
                numProblemTraces++;
                System.out.println("TR result : " + resTr.getInfo());
                System.out.println("Trace: " + trace);
                //System.out.println("Trace: " + decoder.trace());
                //System.out.println("Reduced Trace: " + decoder.reducedTrace());
                //System.out.println("Adjusted costs: " + decoder.adjustedCost());
                //System.out.println("Adjusted RHIDE Costs : " + decoder.adjustedRHIDECost());
                System.out.println("TR Alingment : " + printAlignment(resTr) );
                System.out.println("-------------------------------------------------------------------------------------------------------");
            }
            caseID = resTr.getTraceIndex().first();
            for(AllSyncReplayResult tempRes : fitness2.resOneOptimal())
            {
                if(tempRes.getTraceIndex().contains(caseID)) {
                    res = tempRes;
                    break;
                }
            }
            //res = fitness2.traceAlignmentsMapping.get(decoder.trace());
            double resTrCost = resTr.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST);
            double resCost = res.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST);
            //if(resTrCost!=resCost)
            {
                numProblemTraces++;
                System.out.println("TR result : " + resTr.getInfo());
                System.out.println("Regular result : " + res.getInfo());
                String traceLabels = "";
                for(int pos=0;pos<trace.size();pos++) traceLabels+= logAutomaton.eventLabels().get(trace.get(pos)) + ",";
                traceLabels = "[" + traceLabels.substring(0,traceLabels.length()-1) + "]";
                System.out.println("Trace: " + traceLabels);
                //System.out.println("Trace: " + decoder.trace());
                //System.out.println("Reduced Trace: " + decoder.reducedTrace());
                //System.out.println("Adjusted costs: " + decoder.adjustedCost());
                //System.out.println("Adjusted RHIDE Costs : " + decoder.adjustedRHIDECost());
                System.out.println("TR Alingment : " + printAlignment(resTr) );
                System.out.println("Rg Alignment : " + printAlignment(res));
                System.out.println("-------------------------------------------------------------------------------------------------------");
            }
            double costDifference = resTrCost-resCost;
            if(costDifference>costDifferenceMax)
            {
                String traceLabels = "";
                for(int pos=0;pos<trace.size();pos++) traceLabels+= logAutomaton.eventLabels().get(trace.get(pos)) + ",";
                traceLabels = "[" + traceLabels.substring(0,traceLabels.length()-1) + "]";
                costDifferenceMax=costDifference;
                resTrMaxDifference=resTr;
                resMaxDifference = res;
                traceLabelsMax=traceLabels;
            }

        }
        System.out.println("Time TR : " + fitness.timeOneOptimal + " ms");
        System.out.println("Time Rg : " + fitness2.timeOneOptimal + " ms");
        System.out.println("Result TR : " + fitness.resOneOptimal().getInfo());
        System.out.println("Result Rg : " + fitness2.resOneOptimal().getInfo());
        System.out.println("#Problem traces : " + numProblemTraces);
        System.out.println("Max cost difference: " + costDifferenceMax);
        System.out.println("TR result : " + resTrMaxDifference.getInfo());
        System.out.println("Regular result : " + resMaxDifference.getInfo());
        System.out.println("Trace: " + traceLabelsMax);
        System.out.println("TR Alingment : " + printAlignment(resTrMaxDifference) );
        System.out.println("Rg Alignment : " + printAlignment(resMaxDifference));
        System.out.println("-------------------------------------------------------------------------------------------------------");
    }*/

    public static void main(String[] args) throws Exception
    {
        try {
            if (args.length < 4) {
                System.out.println("At least four args required");
                System.exit(1);
            }
            String path = args[0];
            String model = args[1];
            String log = args[2];

            if (args[3].equals("Statistics")) {
                TRImporter stats = new TRImporter(path, log, model);
                stats.recordStatistics();
                System.exit(0);
            }
            if (args.length == 4) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<ConformanceWrapperTR> future = executor.submit(new ConformanceWrapperTR(args));
                String result = null;
                ConformanceWrapperTR confTask = null;
                try {
                    confTask = future.get(10, TimeUnit.MINUTES);
                } catch (TimeoutException e) {
                    System.err.println("Timeout");
                    result = args[3] + "," + args[0] + ",tout," + "," + ",\n";
                    future.cancel(true);
                    System.exit(1);
                }
                executor.shutdownNow();
                //String result = new ConformanceWrapperSComp(args).call();
                result = confTask.result;
                System.out.println(result);
                recordResult(args[0], result);
                System.exit(0);
            } else if (args.length == 5) {
                int numExecutions = Integer.parseInt(args[4]);
                LongArrayList times = new LongArrayList();
                LongArrayList memories = new LongArrayList();
                LongArrayList woutConflictTimes = new LongArrayList();
                DoubleArrayList costs = new DoubleArrayList();
                DoubleArrayList fitnessProblemsSolved = new DoubleArrayList();
                ConformanceWrapperTR confTask = null;
                String result = "";
                for (int i = 0; i < numExecutions; i++) {
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Future<ConformanceWrapperTR> future = executor.submit(new ConformanceWrapperTR(args));
                    try {
                        confTask = future.get(10, TimeUnit.MINUTES); //timeout is in 10 minutes
                    } catch (TimeoutException e) {
                        System.err.println("Timeout");
                        List<Runnable> tasks = executor.shutdownNow();

                        result = args[3] + "," + args[1] + ",tout," + "," + ",\n";
                        future.cancel(true);
                        recordResult(args[0], result);
                        System.exit(1);
                    }
                    executor.shutdown();
                if(confTask.time > 600000)
                {
                    times.add(confTask.time);
                    costs.add(confTask.cost);
                    memories.add(confTask.memory);
                    fitnessProblemsSolved.add(confTask.fitnessProblemsSolved);
                    //if(args[3].equals("TR-SComp") || args[3].equals("S-Components")) woutConflictTimes.add(confTask.TwithoutConflict);
                    break;
                }
                    if (numExecutions >= 3 && (i == 0 || i == numExecutions - 1)) continue;
                    times.add(confTask.time);
                    costs.add(confTask.cost);
                    memories.add(confTask.memory);
                    fitnessProblemsSolved.add(confTask.fitnessProblemsSolved);
                    //if(args[3].equals("TR-SComp") || args[3].equals("S-Components")) woutConflictTimes.add(confTask.TwithoutConflict);
                    //System.out.println(confTask.result);
                }
                result = confTask.type + "," + confTask.model + "," + times.average() + "," + costs.average() + "," + fitnessProblemsSolved.average() + "," + memories.average() + "\n";
                //if (args[3].equals("TR-SComp") || args[3].equals("S-Components")) result = result.substring(0, result.length() - 1) + "," + woutConflictTimes.average() + "\n";
                System.out.println(result);
                recordResult(args[0], result);
                System.exit(0);
            }
            else if (args.length > 5 && args.length < 8) {
                int numExecutions = Integer.parseInt(args[4]);
                LongArrayList times = new LongArrayList();
                LongArrayList memories = new LongArrayList();
                LongArrayList woutConflictTimes = new LongArrayList();
                DoubleArrayList costs = new DoubleArrayList();
                DoubleArrayList fitnessProblemsSolved = new DoubleArrayList();
                ConformanceWrapperTRMT confTask = null;
                String result = "";
                for (int i = 0; i < numExecutions; i++) {
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Future<ConformanceWrapperTRMT> future = executor.submit(new ConformanceWrapperTRMT(args));
                    try {
                        confTask = future.get(10, TimeUnit.MINUTES); //timeout is in 10 minutes
                    } catch (TimeoutException e) {
                        System.err.println("Timeout");
                        List<Runnable> tasks = executor.shutdownNow();

                        result = args[3] + "," + args[1] + ",tout," + "," + ",\n";
                        future.cancel(true);
                        recordResult(args[0], result);
                        System.exit(1);
                    }
                    executor.shutdown();
                    if (confTask.time > 600000) {
                        times.add(confTask.time);
                        costs.add(confTask.cost);
                        fitnessProblemsSolved.add(confTask.fitnessProblemsSolved);
                        if (args[3].equals("TR-SComp") || args[3].equals("S-Components"))
                            woutConflictTimes.add(confTask.TwithoutConflict);
                        break;
                    }
                    if (numExecutions >= 3 && (i == 0 || i == numExecutions - 1)) continue;
                    times.add(confTask.time);
                    costs.add(confTask.cost);
                    fitnessProblemsSolved.add(confTask.fitnessProblemsSolved);
                    if (args[3].equals("TR-SComp") || args[3].equals("S-Components"))
                        woutConflictTimes.add(confTask.TwithoutConflict);
                    //System.out.println(confTask.result);
                }
                result = confTask.type + "," + confTask.model + "," + times.average() + "," + costs.average() + "," + fitnessProblemsSolved.average() + "," + memories.average() + "\n";
                if (args[3].equals("TR-SComp") || args[3].equals("S-Components"))
                    result = result.substring(0, result.length() - 1) + "," + woutConflictTimes.average() + "\n";
                System.out.println(result);
                recordResult(args[0], result);
                System.exit(0);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static String printAlignment(AllSyncReplayResult res)
    {
        String printAlignment = "[";
        for(int pos=0; pos < res.getNodeInstanceLst().get(0).size(); pos++)
        {
            printAlignment+="("+res.getStepTypesLst().get(0).get(pos) + "," + res.getNodeInstanceLst().get(0).get(pos) + "), ";
        }
        printAlignment=printAlignment.substring(0,printAlignment.length()-2)+"]";
        return  printAlignment;
    }



    private static void recordResult(String path, String result) throws Exception
    {
        FileWriter pw = null;
        File eval = new File(path + "evaluation.txt");
        if (!eval.exists()) {
            pw = new FileWriter(path + "evaluation.txt", true);
            pw.append("type,model,time,cost,#problems solved,memory(mb)\n");
        }
        if (pw == null) pw = new FileWriter(path + "evaluation.txt", true);
        pw.append(result);
        pw.close();
    }
}

