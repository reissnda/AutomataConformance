package CommandLineTool;

import au.qut.apromore.importer.GeneralizationImporter;
import main.ConformanceWrapperTR;
import main.ConformanceWrapperTRMT;
import main.FitnessWithTRreductionMain;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.*;

public class PatternGeneralizationCommandLineTool
{
    private static String fileName = "PatternBasedGeneralizationEvaluationResults.csv";
    private static String fileNameBenchmark = "GeneralizationEvaluationBenchmark.csv";
    private static String fileNameLogStatistics = "LogStatistics.csv";
    private static String fileNameModelStatistics = "ModelStatistics.csv";

    public static void main(String[] args) {
        try {
            //#1 is path
            //#2 is the model file name
            //#3 is the log file name
            //#4 is the extension of the Automata conformance approach
            //#5 is number of cores for MT -> Hybrid?
            //Output Alignments?
            //Output PSP?
            if (args.length < 3) {

                System.out.println(
                        "------------------------\n" +
                                "PatternGeneralization v1.0\n" +
                                "------------------------\n\n" +
                                "REQUIREMENTS:\n" +
                                "Java 8 or above\n\n" +
                                "WHAT IT DOES:\n" +
                                "PatternGeneralization v1.0 computes the generalization measure between a process model and an event log in a scalable way.\n" +
                                "It particularly utilizes concurrent and repetitive patterns in the event log and compares them to control structures in the process model.\n" +
                                "As output, the tool will report the generalization value and the time used to compute it.\n" +
                                "Alternatively, the tool can output a breakdown of the generalization value per pattern type and the corresponding wheight of that pattern type.\n\n" +
                                "HOW:\n" +
                                "A concurrency oracle is used in tandem with partial orders to identify concurrent patterns in the log that are tested against parallel blocks in the process model.\n" +
                                "Tandem repeats are used with various trace reduction and extensions to define repetitive patterns in the log that are tested against loops in the process model.\n" +
                                "Each pattern is assigned a partial fulfilment value between 0 and 1.\n" +
                                " The generalization is then the average of pattern fulfilments weighted by the trace counts for which the patterns have been observed.\n" +
                                "PatternGeneralization v1.0 implements the quality measure described in the paper\n" +
                                " \"Determining Generalization in Process Mining:\n" +
                                "A Framework based on Event Log Patterns\" by D. Reißner, A. Armas-Cervantes and M. La Rosa\n\n" +
                                "USAGE:\n" +
                                "java -jar PatternGeneralization.jar [folder] [modelfile] [logfile] [extension] [numberThreads]\n\n" +
                                "Input : A process model in bpmn or pnml format and an event log in either xes or xes.gz format.\n" +
                                "Output: various alignment statistics such as fitness and raw fitness costs. Optionally, the tool can output the alignments found.\n\n" +
                                "PARAMETERS:\n" +
                                "[folder]      		: Folder where all input files are located and where the output file will be created. Please note, that the last delimiter is expected after the folder name, i.e. \"/path/dataset/\".\n" +
                                "[modelFile]    		: Process model including extension (.pnml or bpmn).\n" +
                                "[logFile]		: Event log including extension (either .xes or .xes.gz).\n" +
                                "[extension]		: (optional) Specify which of the following extensions should be applied to calculate conformance:\n" +
                                "-> \"Automata-Conformance\" is the standard technique from v1.0\n" +
                                "-> \"S-Components\" applies the first extension from v1.1\n" +
                                "-> \"TR-SComp\" applies the second extension from v1.2\n" +
                                "-> \"Hybrid approach\" determines which extension should be applied based on characteristics of the input event log and process model.\n" +
                                "This is the default parameter, if the extension is not specified.\n" +
                                "[numberThreads]		: (optional) The approach can be run multi-threaded by specifying the number of threads. Please not that this parameter can only use the maximal number of threads of your computer.\n" +
                                "If this parameter is not specified, the application will run single-threaded.\n\n" +
                                "Parameters [folder], [logfile] and [modelfile] and [extension] are compulsory.\n" +
                                "Parameters [extension] and [numberThreads] are optional, but expected in this order.\n" +
                                "The application will ask you, if you want to output alignments after the computation is finished. Press key y to output alignments into an excel sheet.\n\n" +
                                "Using the tool without any parameters will provide release information of the tool.\n\n" +
                                "OUTPUT:\n" +
                                "The tool will output the specified extension, the model name, the time used to calculate alignments, the average raw fitness cost per case and the trace fitness.\n" +
                                "If the user pressed y, the tool will output an excel file with name \"alignments.xlsx\" in the specified [folder] with the following characteristics:\n" +
                                "-> Sheet \"Alignment results per Case type\" contains the alignments and statistics for each case type (a.k.a. unique trace) and the number of represented cases.\n" +
                                "-> Sheet \"Alignment results per Case\" contains the alignments of each case.\n\n" +
                                "EXAMPLE:\n" +
                                "java -jar AutomataConformance.jar ./DemonstrationExample/ BPIC12IM.pnml BPIC12.xes.gz\n\n" +
                                "The dataset used for the evaluation of the papers can be found here:\n" +
                                "https://melbourne.figshare.com/articles/Public_benchmark_data-set_for_Conformance_Checking_in_Process_Mining/8081426\n\n" +
                                "ASSUMPTIONS:\n"+
                                "The input model should be formatted according to the pnml (http://www.pnml.org/) format.\n"+
                                "To check that the input model is correctly formatted, you can open the model using WoPeD.\n\n" +
                                "The tool accepts models formatted according to the bpmn (http://http://www.bpmn.org) format.\n" +
                                "The tool will internally convert the bpmn diagram to its equivalent Petri net representation.\n\n" +
                                "The input log should be formatted according to the xes or xes.gz (OpenXES, http://www.xes-standard.org/) format.\n" +
                                "To check that the input log is correctly formatted, you can open the log using the ProM toolkit.\n\n" +
                                "RELEASE NOTES:\n\n" +
                                "1.0: initial version\n"
                );
                System.exit(1);
            }
            else if(args.length>3)
            {
                String path = args[0];
                String log = args[1];
                String model = args[2];
                String approach = args[3];
                long timeLimit=10;
                TimeUnit timeLimitUnit=TimeUnit.MINUTES;
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
                //System.out.println("Extension: " + confTask.type + ", Model: " + confTask.model + ", Time: " + confTask.time + " ms, Avg. Raw fitness cost: " + df2.format(confTask.cost) + ", Fitness: " + df2.format(confTask.fitness));
                //printAlignments(confTask.path, confTask.alignresult, confTask.caseIDs);
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void performExperimentForPatternBasedGeneralization(String path, String log, String model, boolean useGlobal, boolean usePartialMatching,double noiseThreshold, float occurence, float balance, long timeLimit, TimeUnit timeLimitUnit) throws Exception
    {
        String result = "";
        GeneralizationImporter importer = new GeneralizationImporter();
        importer.importEventLogAndPetriNet(path+log,path+model);
        //System.out.println("Start experiment");
        ExecutorService executor = Executors.newCachedThreadPool();
        //PatternGeneralizationCommandLineTool tool = new PatternGeneralizationCommandLineTool(path+log,path+model,useGlobal,noiseThreshold,occurence,balance,false);
        au.unimelb.patternBasedGeneralization.PatternGeneralizationCommandLineTool tool = new au.unimelb.patternBasedGeneralization.PatternGeneralizationCommandLineTool(importer,useGlobal,usePartialMatching,noiseThreshold,occurence,balance,true);
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
}
