package main;

import au.qut.apromore.importer.GeneralizationImporter;
import au.qut.apromore.importer.ImportEventLog;
import au.qut.apromore.importer.ImportProcessModel;
import au.unimelb.evaluation.AntiAlignmentTask;
import au.unimelb.evaluation.NegativeEventsTask;
import au.unimelb.negativeEventsClasses.PetrinetLogMapper;
import au.unimelb.patternBasedGeneralization.PatternBasedGeneralizationMeasure;
import au.unimelb.patternBasedGeneralization.PatternGeneralizationCommandLineTool;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PatternGeneralization
{
    private static String fileName = "PatternBasedGeneralizationEvaluationResults.csv";
    private static String fileNameBenchmark = "GeneralizationEvaluationBenchmark.csv";
    private static String fileNameLogStatistics = "LogStatistics.csv";
    private static String fileNameModelStatistics = "ModelStatistics.csv";

    public static void mainBla(String[] args) throws Exception
    {
        String path = "/Users/dreissner/Documents/Evaluations/PatternBasedGeneralizationPaper/QualitativeEvaluation/AntiAlignmentsDataset/";
        String log = "artificial.xes.gz";
        String model = "2.pnml";
        //System.out.println(performNEGeneralization(path,log,model));
    }

    public static void main(String[] args) throws Exception
    {
        //qualitativeEvaluation();
        //quantitativeEvaluation();
        String path = args[0];
        String log = args[1];
        String model = args[2];
        String approach = args[3];
        long timeLimit=10;
        TimeUnit timeLimitUnit=TimeUnit.MINUTES;
        if(approach.toLowerCase().equals("patternbasedgeneralization"))
        {
            boolean useGlobal=true;
            double noiseThreshold=0.02;
            float occurence=0.55f;
            float balance=0.1f;
            boolean usePartialMatching=true;
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
                timeLimit = Long.parseLong(args[7]);
                timeLimitUnit = TimeUnit.valueOf(args[8]);
            }
            else if(!useGlobal && args.length>=10)
            {
                timeLimit = Long.parseLong(args[8]);
                timeLimitUnit = TimeUnit.valueOf(args[9]);
            }
            performExperimentForPatternBasedGeneralization(path,log,model,useGlobal,usePartialMatching,noiseThreshold,occurence,balance, timeLimit, timeLimitUnit);
        }
        else if(approach.equals("AntiAlignmentsGeneralization"))
        {
            if(args.length>=6)
            {
                timeLimit = Long.parseLong(args[4]);
                timeLimitUnit = TimeUnit.valueOf(args[5]);
            }
            performAntiAlignmentExperiment(path,log,model,timeLimit,timeLimitUnit);
        }
        else if(approach.equals("NegativeEventsGeneralization"))
        {
            if(args.length>=6)
            {
                timeLimit = Long.parseLong(args[4]);
                timeLimitUnit = TimeUnit.valueOf(args[5]);
            }
            performNegativeEventsExperiment(path,log,model,timeLimit,timeLimitUnit);
        }
        else if(approach.equals("LogStatistics"))
        {
            boolean useGlobal=true;
            double noiseThreshold=0.02;
            float occurence=0.5f;
            float balance=0.1f;
            if(args.length>=5)
            {
                useGlobal = args[4].toLowerCase().equals("global");
            }
            if(useGlobal && args.length>=6)
            {
                noiseThreshold=Double.parseDouble(args[5]);
            }
            else if(!useGlobal && args.length>=7)
            {
                occurence=Float.parseFloat(args[5]);
                balance=Float.parseFloat(args[6]);
            }
            if(useGlobal && args.length>=8)
            {
                timeLimit = Long.parseLong(args[6]);
                timeLimitUnit = TimeUnit.valueOf(args[7]);
            }
            else if(!useGlobal && args.length>=9)
            {
                timeLimit = Long.parseLong(args[7]);
                timeLimitUnit = TimeUnit.valueOf(args[8]);
            }
            gatherLogStatistics(path,log,useGlobal,noiseThreshold,occurence,balance, timeLimit, timeLimitUnit);
        }
        else if(approach.equals("ModelStatistics"))
        {
            ImportProcessModel importer = new ImportProcessModel();
            recordModelStatistics(path,importer.importPetrinetForStatistics(path + model));
        }
        System.exit(0);
    }

    private static void performNegativeEventsExperiment(String path, String log, String model, long timeLimit, TimeUnit timeLimitUnit) throws Exception
    {
        String result = "";
        ImportProcessModel importProcessModel = new ImportProcessModel();
        Object[] pnetAndMarking = importProcessModel.importPetriNetAndMarking(path+model);
        ImportEventLog importEventLog = new ImportEventLog();
        XLog xLog = importEventLog.importEventLog(path+log);
        ExecutorService executor = Executors.newCachedThreadPool();
        NegativeEventsTask negativeEventsTask = new NegativeEventsTask(log,model,xLog,(Petrinet) pnetAndMarking[0],(Marking) pnetAndMarking[1]);
        Future<String> future = executor.submit(negativeEventsTask);
        try
        {
            result = future.get(timeLimit,timeLimitUnit);
        } catch(Exception timeout)
        {
            result = log + "," + model + ",NegativeEvents,t/out\n";
            future.cancel(true);
        }
        executor.shutdownNow();
        //double antiAlignmentResult = AntiAlignmentTask.basicCodeStructureWithoutAlignments((Petrinet) pnetAndMarking[0],(Marking) pnetAndMarking[1],(Marking) pnetAndMarking[2],xLog);
        //double neResult = getNEGeneralization(xLog,(Petrinet) pnetAndMarking[0],(Marking) pnetAndMarking[1]);
        //System.out.println(result);
        recordResultBenchmark(path,result);
    }

    private static void performAntiAlignmentExperiment(String path, String log, String model, long timeLimit, TimeUnit timeLimitUnit) throws Exception
    {
        String result = "";
        ImportProcessModel importProcessModel = new ImportProcessModel();
        Object[] pnetAndMarking = importProcessModel.importPetriNetAndMarking(path+model);
        ImportEventLog importEventLog = new ImportEventLog();
        XLog xLog = importEventLog.importEventLog(path+log);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AntiAlignmentTask antiAlignmentTask = new AntiAlignmentTask(log,model,(Petrinet) pnetAndMarking[0],(Marking) pnetAndMarking[1],(Marking) pnetAndMarking[2],xLog);
        Future<String> future = executor.submit(antiAlignmentTask);
        try
        {
            result = future.get(timeLimit,timeLimitUnit);
        } catch(Exception timeout)
        {
            result = log + "," + model + ",AntiAlignments,t/out\n";
            future.cancel(true);
        }
        executor.shutdownNow();
        //double antiAlignmentResult = AntiAlignmentTask.basicCodeStructureWithoutAlignments((Petrinet) pnetAndMarking[0],(Marking) pnetAndMarking[1],(Marking) pnetAndMarking[2],xLog);
        //double neResult = getNEGeneralization(xLog,(Petrinet) pnetAndMarking[0],(Marking) pnetAndMarking[1]);
        //System.out.println(result);
        recordResultBenchmark(path,result);
    }

    private static void recordResultBenchmark(String path, String result) throws Exception
    {
        String headlines = "Log,Model,approach,Execution time,generalization\n";
        System.out.println(headlines);
        System.out.println(result);
        FileWriter pw = null;
        File statF = new File(path+fileNameBenchmark);
        if(!statF.exists())
        {
            pw = new FileWriter(path + fileNameBenchmark, true);
            pw.append(headlines);
        }
        if(pw==null) pw = new FileWriter(path + fileNameBenchmark, true);
        pw.append(result);
        pw.close();
    }

    private static void performExperimentForPatternBasedGeneralization(String path, String log, String model, boolean useGlobal, boolean usePartialMatching,double noiseThreshold, float occurence, float balance, long timeLimit, TimeUnit timeLimitUnit) throws Exception
    {
        String result = "";
        GeneralizationImporter importer = new GeneralizationImporter();
        importer.importEventLogAndPetriNet(path+log,path+model);
        //System.out.println("Start experiment");
        ExecutorService executor = Executors.newCachedThreadPool();
        //PatternGeneralizationCommandLineTool tool = new PatternGeneralizationCommandLineTool(path+log,path+model,useGlobal,noiseThreshold,occurence,balance,false);
        PatternGeneralizationCommandLineTool tool = new PatternGeneralizationCommandLineTool(importer,useGlobal,usePartialMatching,noiseThreshold,occurence,balance,true);
        Future<String> future = executor.submit(tool);
        //long time = System.nanoTime();
        try
        {
            result = future.get(timeLimit,timeLimitUnit);
            //time=System.nanoTime()-time;
        } catch(Exception timeout)
        {
            //System.out.println(timeout.getMessage());
            result = log + "," + model +",PatternBasedGeneralization," + useGlobal + "," + usePartialMatching + ",t/out\n";
            future.cancel(true);
        }
        executor.shutdownNow();
        //tool.call();
        //result=tool.getResult();
        recordPatternBasedGeneralizationResult(path,result);
    }

    private static void recordPatternBasedGeneralizationResult(String path,String result) throws Exception
    {
        String headlines = "Log,model,approach,GlobalOracle,UsePartialMatching,Execution time,Initial trace count, trace count Concurrent pattern, generalization concurrent pattern,trace count repetitive pattern,generalization repetitive pattern, overall trace count,overall generalization\n";
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

    private static void gatherLogStatistics(String path, String log, boolean useGlobal, double noiseThreshold, float occurence, float balance, long timeLimit, TimeUnit timeLimitUnit) throws Exception
    {
        String result = "";

        ExecutorService executor = Executors.newCachedThreadPool();
        GeneralizationImporter importer = new GeneralizationImporter(path,log,useGlobal,noiseThreshold,occurence,balance);
        Future<String> future = executor.submit(importer);
        //long time = System.nanoTime();
        try
        {
            result = future.get(timeLimit,timeLimitUnit);
            //time=System.nanoTime()-time;
        } catch(Exception timeout)
        {
            //System.out.println(timeout.getMessage());
            result = importer.statistics + "t/out\n";
            future.cancel(true);
        }
        executor.shutdownNow();
        recordLogStatistics(path, result);
    }

    private static void recordLogStatistics(String path,String result) throws Exception
    {
        String headlines = "Log,#Events,Unq,#Traces,Unq,#TRs,#Extended Traces,#Repetitive Patterns,labels,#Trace count,#Conc pair,#Partial Orders,#ConcUnqTraces,#Conc patterns,#Trace Count,#Contexts,Avg Conc Pairs,#Partial Orders,#ConcUnqTrace,#Conc Patterns,#Trace Count\n";
        System.out.println(headlines);
        System.out.println(result);
        FileWriter pw = null;
        File statF = new File(path+fileNameLogStatistics);
        if(!statF.exists())
        {
            pw = new FileWriter(path + fileNameLogStatistics, true);
            pw.append(headlines);
        }
        if(pw==null) pw = new FileWriter(path + fileNameLogStatistics, true);
        pw.append(result);
        pw.close();
    }

    private static void recordModelStatistics(String path, String result) throws Exception
    {
        String headlines = "Model,Size,Places,Transitions,Choices,Parallel\n";
        System.out.println(headlines);
        System.out.println(result);
        FileWriter pw = null;
        File statF = new File(path+fileNameModelStatistics);
        if(!statF.exists())
        {
            pw = new FileWriter(path + fileNameModelStatistics, true);
            pw.append(headlines);
        }
        if(pw==null) pw = new FileWriter(path + fileNameModelStatistics, true);
        pw.append(result);
        pw.close();
    }

    private static void quantitativeEvaluation() throws Exception
    {
        System.out.println("Public IM Dataset");
        String path="/Users/dreissner/Documents/evaluations/PatternBasedGeneralizationPaper/QuantitativeEvaluation/IM/";
        String log;
        String model;
        for(int i=1;i<=17;i++)
        {
            log= i + ".xes.gz";
            model=i + ".pnml";
            performExperiment(path,log,model);
        }
        System.out.println("Public SM Dataset");
        path="/Users/dreissner/Documents/evaluations/PatternBasedGeneralizationPaper/QuantitativeEvaluation/SM/";
        for(int i=1;i<=17;i++)
        {
            log= i + ".xes.gz";
            model=i + ".pnml";
            performExperiment(path,log,model);
        }
    }

    public static void qualitativeEvaluation() throws Exception
    {
        System.out.println("Anti-Alignments Dataset");
        String path="/Users/dreissner/Documents/Evaluations/PatternBasedGeneralizationPaper/QualitativeEvaluation/AntiAlignmentsDataset/";
        String log="artificial.xes.gz";
        //String model="";
        String[] models = new String[]{"1.pnml","2.pnml","3.pnml","4.pnml","5.pnml","6.pnml","7.pnml","8.pnml","12.pnml"};
        for(String model : models) {
            performExperiment(path,log,model);
        }
        System.out.println("Negative Events Dataset");
        path="/Users/dreissner/Documents/Evaluations/PatternBasedGeneralizationPaper/QualitativeEvaluation/NegativeEventsDataset/";
        models = new String[]{"1.pnml","2.pnml","3.pnml","4.pnml","5.pnml","6.pnml"};
        for(String model : models) {
            performExperiment(path,log,model);
        }
    }

    public static void performExperiment(String path, String log, String model) throws Exception
    {
        ImportProcessModel importProcessModel = new ImportProcessModel();
        Object[] pnetAndMarking = importProcessModel.importPetriNetAndMarking(path+model);
        ImportEventLog importEventLog = new ImportEventLog();
        XLog xLog = importEventLog.importEventLog(path+log);
        PatternBasedGeneralizationMeasure generalizationGlobalBasic = new PatternBasedGeneralizationMeasure(path + log, path + model, true, false);
        PatternBasedGeneralizationMeasure generalizationLocalBasic = new PatternBasedGeneralizationMeasure(path + log, path + model, false, false);
        PatternBasedGeneralizationMeasure generalizationGlobalTransitive = new PatternBasedGeneralizationMeasure(path + log, path + model, true, true);
        PatternBasedGeneralizationMeasure generalizationLocalTransitive = new PatternBasedGeneralizationMeasure(path + log, path + model, false, true);
        double antiAlignmentResult = AntiAlignmentTask.basicCodeStructureWithoutAlignments((Petrinet) pnetAndMarking[0],(Marking) pnetAndMarking[1],(Marking) pnetAndMarking[2],xLog);
        //double neResult = getNEGeneralization(xLog,(Petrinet) pnetAndMarking[0],(Marking) pnetAndMarking[1]);
        String result=model+","+ antiAlignmentResult +"," +generalizationLocalBasic.generalization+","+generalizationLocalTransitive.generalization +","+generalizationGlobalBasic.generalization + ","+generalizationGlobalTransitive.generalization + "\n";
        System.out.println(result);
        recordResult(path,result);
    }

    public static double performNEGeneralization(String path, String log, String model) throws Exception
    {
        ImportProcessModel importProcessModel = new ImportProcessModel();
        Object[] pnetAndMarking = importProcessModel.importPetriNetAndMarking(path+model);
        ImportEventLog importEventLog = new ImportEventLog();
        XLog xLog = importEventLog.importEventLog(path+log);
        return getNEGeneralization(xLog,(Petrinet) pnetAndMarking[0],(Marking) pnetAndMarking[1]);
    }

    public static double getNEGeneralization(XLog log, Petrinet onet, Marking marking) throws Exception {
        XEventClassifier classifier = new XEventNameClassifier();
        PetrinetLogMapper mapper = PetrinetLogMapper.getStandardMap(log,onet);// new PetrinetLogMapper(classifier, XEventClasses.deriveEventClasses(classifier, log).getClasses(), onet.getTransitions());
        //double neGeneralization = PetrinetEvaluatorPlugin.getMetricValue(log, onet, marking, mapper, 0, 1, true, true, true, 10, 10, true, false, true, false, "generalization");
        double neGeneralization = NegativeEventsTask.getMetricValue(log, onet, marking, mapper, 1, 1, true, true, true, 20, 20, true, false, true, false, "generalization");
        return neGeneralization;
    }

    private static void recordResult(String path, String result) throws Exception
    {
        FileWriter pw = null;
        File eval = new File(path + "evaluation.txt");
        if(!eval.exists())
        {
            pw = new FileWriter(path + "evaluation.txt",true);
            pw.append("Model,Description,Negative Events,Anti-Alignments,Local Basic,Local Transitive,Global Basic,Global Transitive\n");
        }
        if(pw ==null) pw = new FileWriter(path + "evaluation.txt",true);
        pw.append(result);
        pw.close();
    }
}
