package CommandLineTool;

import au.qut.apromore.importer.TRImporter;
import main.ConformanceWrapperTR;
import main.ConformanceWrapperTRMT;
import main.FitnessWithTRreductionMain;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.*;

public class CommandLineMain
{
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
                        "AutomataConformance v1.2\n" +
                        "------------------------\n\n" +
                        "REQUIREMENTS:\n" +
                        "Java 8 or above\n\n" +
                        "WHAT IT DOES:\n" +
                        "AutomataConformance v1.2 is a business process conformance checking tool for identifying differences between a process model and an event log in a scalable way.\n" +
                "The differences are captured as alignments when synchronously traversing automata stuctures representing the process model and event log. As a result, the tool produces various alignment\n" +
                "statistics such as fitness and raw fitness costs. Optionally, the tool can output the alignments found.\n\n" +
                "HOW:\n" +
                "AutomataConformance v1.0 implements the conformance checking technique described in the paper\n" +
                " \"Scalable Conformance Checking of Business Processes\" by D. Reißner, R. Conforti, M. Dumas, M. La Rosa\n" +
                "and A. Armas-Cervantes.\n\n" +

                "AutomataConformance v1.1 implements a divide-and-conquer extension based on S-Component decomposition described in the paper\n" +
                "\"Scalable Alignment of Process Models and Event Logs: An Approach Based on Automata and S-Components\" by D. Reißner, A. Armas-Cervantes,\n" +
                "        R. Conforti, M. Dumas, D. Fahland and M. La Rosa\n\n" +
                "AutomataConformance v1.2 implements an additional extension based on tandem repeat reductions described in the paper\n" +
                "\"Efficient Conformance Checking using Alignment Computation with Tandem Repeats\" by D. Reißner, A. Armas-Cervantes and M. La Rosa\n\n" +
                "USAGE:\n" +
                "java -jar AutomataConformance.jar [folder] [modelfile] [logfile] [extension] [numberThreads]\n\n" +
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
                "1.0: initial version\n" +
                 "1.1: Added the implementation of the S-Components extension.\n" +
                 "1.2. Added the implementation of the Tandem repeats extenstion."
                );
                System.exit(1);
            }
            else if(args.length==3)
            {
                String[] args_new = {args[0],args[1],args[2],""};
                //String[] args_new = {"/Users/dreissner/Documents/Evaluations/TandemRepeatsPaper/public/IM/", "15.pnml", "15.xes.gz",""};
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<ConformanceWrapperTR> future = executor.submit(new ConformanceWrapperTR(args_new));
                String result;
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
                //System.out.println(result);
                DecimalFormat df2 = new DecimalFormat("#.###");
                System.out.println("Extension: " + confTask.type + ", Model: " + confTask.model + ", Time: " + confTask.time + " ms, Avg. Raw fitness cost: " + df2.format(confTask.cost) + ", Fitness: " + df2.format(confTask.fitness));
                printAlignments(confTask.path, confTask.alignresult, confTask.caseIDs);
                System.exit(0);
            }
            else if (args.length == 4) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<ConformanceWrapperTR> future = executor.submit(new ConformanceWrapperTR(args));
                String result;
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
                DecimalFormat df2 = new DecimalFormat("#.###");
                System.out.println("Extension: " + confTask.type + ", Model: " + confTask.model + ", Time: " + confTask.time + " ms, Avg. Raw fitness cost: " + df2.format(confTask.cost) + ", Fitness: " + df2.format(confTask.fitness));
                printAlignments(confTask.path, confTask.alignresult, confTask.caseIDs);
                System.exit(0);
            } else if (args.length > 4 && args.length < 8) {

                ConformanceWrapperTRMT confTask = null;
                String result;

                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<ConformanceWrapperTRMT> future = executor.submit(new ConformanceWrapperTRMT(args));
                try {
                    confTask = future.get(10, TimeUnit.MINUTES); //timeout is in 10 minutes
                } catch (TimeoutException e) {
                    System.err.println("Timeout");
                    List<Runnable> tasks = executor.shutdownNow();
                    result = args[3] + "," + args[1] + ",tout," + "," + ",\n";
                    future.cancel(true);
                    System.exit(1);
                }
                executor.shutdown();
                DecimalFormat df2 = new DecimalFormat("#.###");
                System.out.println("Extension: " + confTask.type + ", Model: " + confTask.model + ", Time: " + confTask.time + " ms, Avg. Raw fitness cost: " + df2.format(confTask.cost) + ", Fitness: " + df2.format(confTask.fitness));
                printAlignments(confTask.path, confTask.pnresult, confTask.caseIDs);
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void printAlignments(String path, PNMatchInstancesRepResult alignmentResult, UnifiedMap<Integer, String> caseIDs)
    {
        String fileName = path + "alignments" + ".xlsx";
        System.out.println("Do you want to export the alignments into excel file " + fileName + "?");
        System.out.print("Press y: ");
        char input = ' ';
        try {
            input = (char) System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(input!='y')
        {
            System.exit(1);
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet caseTypeSheet = workbook.createSheet("Alignment results per Case type");
        XSSFSheet alignmentSheet = workbook.createSheet("Alignment results per Case");

        Row row = caseTypeSheet.createRow(1);
        Cell cell = row.createCell(1);
        cell.setCellValue("Average Log Alignment Statistics per Case Type:");
        row = caseTypeSheet.createRow(2);
        String[] headlines = {  PNMatchInstancesRepResult.ORIGTRACELENGTH,
                                PNMatchInstancesRepResult.TIME,
                                PNMatchInstancesRepResult.RAWFITNESSCOST,
                                PNMatchInstancesRepResult.TRACEFITNESS};
        for(int i =0; i<headlines.length;i++)
        {
            cell = row.createCell(i+1);
            cell.setCellValue(headlines[i]);
        }
        row = caseTypeSheet.createRow(3);
        String[] results = {
                alignmentResult.getInfo().get(PNMatchInstancesRepResult.ORIGTRACELENGTH),
                alignmentResult.getInfo().get(PNMatchInstancesRepResult.TIME),
                alignmentResult.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST),
                alignmentResult.getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS)};
        for(int i =0; i<results.length;i++)
        {
            cell = row.createCell(i+1);
            cell.setCellValue(results[i]);
        }

        row = caseTypeSheet.createRow(5);
        cell = row.createCell(1);
        cell.setCellValue("Alignment statistics per case type:");
        row = caseTypeSheet.createRow(6);
        headlines = new String[]{"Case Type",
                                 "#Represented cases",
                                 PNMatchInstancesRepResult.ORIGTRACELENGTH,
                                 PNMatchInstancesRepResult.TIME,
                                 PNMatchInstancesRepResult.RAWFITNESSCOST,
                                 PNMatchInstancesRepResult.TRACEFITNESS,
                                 "alignment"};
        for(int i =0; i<headlines.length;i++)
        {
            cell = row.createCell(i+1);
            cell.setCellValue(headlines[i]);
        }

        row = alignmentSheet.createRow(1);
        headlines = new String[]{"Num.",
                	"Case Type",
                	"Case ID",
                	"Trace Index",
                    PNMatchInstancesRepResult.ORIGTRACELENGTH,
                    PNMatchInstancesRepResult.TIME,
                	PNMatchInstancesRepResult.RAWFITNESSCOST,
                	PNMatchInstancesRepResult.TRACEFITNESS,
                    "Alignment"};
        for(int i =0; i<headlines.length;i++)
        {
            cell = row.createCell(i+1);
            cell.setCellValue(headlines[i]);
        }

        int num=1, caseType =1;
        for(AllSyncReplayResult res : alignmentResult)
        {
            String alignment = FitnessWithTRreductionMain.printAlignment(res);
            for(int trace : res.getTraceIndex())
            {
                row=alignmentSheet.createRow(num+1);
                results = new String[]{
                                String.valueOf(num++),
                                String.valueOf(caseType),
                                caseIDs.get(trace),
                                String.valueOf(trace),
                                String.valueOf(res.getInfo().get(PNMatchInstancesRepResult.ORIGTRACELENGTH)),
                                String.valueOf(res.getInfo().get(PNMatchInstancesRepResult.TIME)),
                                String.valueOf(res.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST)),
                                String.valueOf(res.getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS)),
                                alignment
                };
                for(int i =0; i<results.length;i++)
                {
                    cell = row.createCell(i+1);
                    cell.setCellValue(results[i]);
                }
            }
            row=caseTypeSheet.createRow(6+caseType);
            results= new String[]
                    {
                            String.valueOf(caseType),
                            String.valueOf(res.getTraceIndex().size()),
                            String.valueOf(res.getInfo().get(PNMatchInstancesRepResult.ORIGTRACELENGTH)),
                            String.valueOf(res.getInfo().get(PNMatchInstancesRepResult.TIME)),
                            String.valueOf(res.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST)),
                            String.valueOf(res.getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS)),
                            alignment
                    };
            for(int i =0; i<results.length;i++)
            {
                cell = row.createCell(i+1);
                cell.setCellValue(results[i]);
            }
            caseType++;
        }

        try
        {
            FileOutputStream outputStream = new FileOutputStream(fileName);
            workbook.write(outputStream);
        } catch(Exception e){e.printStackTrace();}
        System.out.println("Export of alignment results is complete.");
    }
}
