package au.unimelb.patternBasedGeneralization;

import au.qut.apromore.importer.GeneralizationImporter;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.*;

public class PatternGeneralizationCommandLineTool implements Callable<String>
{
    private boolean usePartialMatching=true;
    private boolean useGeneralizationMeasure;
    private boolean useGlobal;
    private double noiseThreshold;
    private float occurence;
    private float balance;
    private GeneralizationImporter patterns;

    public String getResult() {
        return result;
    }

    private String result="";

    public PatternGeneralizationCommandLineTool(GeneralizationImporter importer, boolean useGlobal, double noiseThreshold, float occurence, float balance, boolean useGeneralizationMeasure)
    {
        this.patterns = importer;
        this.useGlobal = useGlobal;
        this.noiseThreshold=noiseThreshold;
        this.occurence=occurence;
        this.balance=balance;
        this.useGeneralizationMeasure=useGeneralizationMeasure;
    }

    public PatternGeneralizationCommandLineTool(GeneralizationImporter importer, boolean useGlobal, boolean usePartialMatching, double noiseThreshold, float occurence, float balance, boolean useGeneralizationMeasure)
    {
        this.patterns = importer;
        this.useGlobal = useGlobal;
        this.usePartialMatching=usePartialMatching;
        this.noiseThreshold=noiseThreshold;
        this.occurence=occurence;
        this.balance=balance;
        this.useGeneralizationMeasure=useGeneralizationMeasure;
    }

    public static void main(String[] args) throws Exception
    {
        String path = args[0];
        String log = args[1];
        String model = args[2];
        String mode="GeneralizationMeasure";
        boolean useGeneralization=true;
        boolean useGlobal=true;
        boolean usePartialMatching = true;
        double noiseThreshold=0.02;
        float occurence=0.5f;
        float balance=0.1f;
        long time = 10;
        TimeUnit timeUnit = TimeUnit.MINUTES;
        if(args.length>=4)
        {
            mode = args[3];
        }
        if(args.length>=5)
        {
            useGlobal = args[4].toLowerCase().equals("global");
        }
        if(args.length>=6)
        {
            usePartialMatching=args[5].equalsIgnoreCase("PartialMatching");
        }
        if(useGlobal && args.length>=7)
        {
            noiseThreshold=Double.parseDouble(args[6]);
        }
        else if(!useGlobal && args.length>=8)
        {
            occurence=Float.parseFloat(args[6]);
            balance=Float.parseFloat(args[7]);
        }
        if(useGlobal &&args.length>=9)
        {
            time = Long.parseLong(args[7]);
            timeUnit = TimeUnit.valueOf(args[8]);
        }
        else if(!useGlobal && args.length>=10)
        {
            time = Long.parseLong(args[8]);
            timeUnit = TimeUnit.valueOf(args[9]);
        }
        GeneralizationImporter importer = new GeneralizationImporter();
        importer.importEventLogAndPetriNet(path+log,path+model);
        if(mode.toLowerCase().equals("measure")||mode.toLowerCase().equals("generalization")||mode.toLowerCase().equals("patternbasedgeneralization"))
        {
            useGeneralization=true;
        }
        else useGeneralization=false;
        ExecutorService executor = Executors.newCachedThreadPool();
        PatternGeneralizationCommandLineTool tool = new PatternGeneralizationCommandLineTool(path+log,path+model,useGlobal,usePartialMatching,noiseThreshold,occurence,balance,useGeneralization);
        Future<String> future = executor.submit(tool);
        String result = "";
        try
        {
            result = future.get(time,timeUnit);
        } catch(TimeoutException timeout)
        {
            result = log+",t/out";
            future.cancel(true);
        }
        executor.shutdownNow();
        recordResult(path,result,useGeneralization);
    }

    private static void recordResult(String path, String fileName, String result, boolean useGeneralization) throws Exception
    {
        if(useGeneralization)
        {
            String headlines = "Log,Model,approach,Execution time,Initial trace count, trace count Concurrent pattern, generalization concurrent pattern,trace count repetitive pattern,generalization repetitive pattern, overall trace count,overall generalization\n";
            System.out.println(headlines);
            System.out.println(result);
            FileWriter pw = null;
            File statF = new File(path+fileName);
            if(!statF.exists())
            {
                pw = new FileWriter(path + fileName, true);
                pw.append(headlines);
            }
            if(pw==null) pw = new FileWriter(path + fileName, true);
            pw.append(result);
            pw.close();
        }
        else
        {
            String headlines = "Path, Log File, #Events, #Unique Events, #Traces, #Unique Traces, #Times TR Considered, " +
                    "#Unique Traces after TR, Original Trace length average, (median,max), " +
                    "Reduction length average, (median,max), Reduced Trace Lengths average, (median,max), " +
                    "#TR touples average, (max,sum), #Repetitions average, (max, sum)\n";
            //String result = path + "," + log + "," + events + "," + logAutomaton.eventLabels().size() + "," + nTraces + "," + this.uniqueTraces + "," + this.nTRconsidered + "," +
            //        this.uniqueTracesAfterTR + "," + this.OriginalTraceLengths.average() + "," + this.OriginalTraceLengths.median() + "," + this.OriginalTraceLengths.max() + "," +
            //        this.TRReductionLength.average() + "," + this.TRReductionLength.median() + "," + this.TRReductionLength.max() + "," +
            //        this.TRReducedTraceLengths.average() + "," + this.TRReducedTraceLengths.median() + "," + this.TRReducedTraceLengths.max() + "," +
            //        this.nTRrepeatTpls.average() + "," + this.nTRrepeatTpls.average() + "," + this.nTRrepeatTpls.sum() + "," +
            //        this.nTRrepsList.average() + "," + this.nTRrepsList. max() + "," + this.nTRrepsList.sum() +"\n";
            System.out.println(headlines);
            System.out.println(result);
            FileWriter pw = null;
            File statF = new File(path+fileName);
            if(!statF.exists())
            {
                pw = new FileWriter(path + fileName, true);
                pw.append(headlines);
            }
            if(pw==null) pw = new FileWriter(path + fileName, true);
            pw.append(result);
            pw.close();
        }
    }

    private static void recordResult(String path, String result, boolean useGeneralization) throws Exception
    {
        if(useGeneralization)
        {
            recordResult(path,"PatternBasedGeneralization.csv",result,useGeneralization);
        }
        else
        {
            recordResult(path,"GeneralizationStats.csv",result,useGeneralization);
        }
    }

    public PatternGeneralizationCommandLineTool(String logFile, String modelFile, boolean useGlobal, double noiseThreshold, float occurence, float balance, boolean useGeneralizationMeasure) throws Exception
    {
        this.patterns=new GeneralizationImporter();
        patterns.importEventLogAndPetriNet(logFile,modelFile);
        this.useGlobal=useGlobal;
        this.noiseThreshold=noiseThreshold;
        this.occurence=occurence;
        this.balance=balance;
        this.useGeneralizationMeasure=useGeneralizationMeasure;
    }

    public PatternGeneralizationCommandLineTool(String logFile, String modelFile, boolean useGlobal, boolean usePartialMatching, double noiseThreshold, float occurence, float balance, boolean useGeneralizationMeasure) throws Exception
    {
        this.patterns=new GeneralizationImporter();
        patterns.importEventLogAndPetriNet(logFile,modelFile);
        this.usePartialMatching=usePartialMatching;
        this.useGlobal=useGlobal;
        this.noiseThreshold=noiseThreshold;
        this.occurence=occurence;
        this.balance=balance;
        this.useGeneralizationMeasure=useGeneralizationMeasure;
    }

    @Override
    public String call() throws Exception {
        result = "";
        if(useGeneralizationMeasure) {
            PatternBasedGeneralizationMeasure measure = null;
            if (useGlobal)
                measure = new PatternBasedGeneralizationMeasure(patterns, noiseThreshold, usePartialMatching);
            else
                measure = new PatternBasedGeneralizationMeasure(patterns, occurence, balance, usePartialMatching);
            //return measure.result;
            result= measure.result;
        }
        else
        {
            GeneralizationImporter importer = new GeneralizationImporter();
            result = importer.collectLogStatisticsGlobal();
        }
        return result;
    }
}
