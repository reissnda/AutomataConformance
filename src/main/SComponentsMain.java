package main;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.*;

import au.qut.apromore.importer.DecomposingConformanceImporter;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;

public class SComponentsMain
{
	/*public static void main(String[] args) throws Exception
	{
		System.out.println("Hello World");
		String path = "/Users/dreissner/Downloads";
		String model = "HybridModel0.2.pnml";
		ImportProcessModel importer = new ImportProcessModel();
		Object[] pnetAndMarking = importer.importPetriNetAndMarking(path + "/" + model);
		Automaton fsm = importer.createFSMfromPetrinet((Petrinet) pnetAndMarking[0], (Marking) pnetAndMarking[1], null, null);
		fsm.toDot(path + "/fsm.dot");
		*//*for(IntArrayList trace : fsm.cases)
		{
			System.out.println(trace);
		}*//*
	}*/

	public static void main(String[] args) throws Exception
	{
		//String path = "/Users/dreissner/Documents/Evaluations/S-Components/alignment/bpi12/";
		//String model = "bpi12.pnml";
		//String log = "bpi12.xes";
		//String path = "/Users/dreissner/Documents/Evaluations/SAP Model log pairs/";
		//String model = "1An_l51v.pnml";
		//String log = "1An_l51v_log0075.xes";
		//String path = "/Users/dreissner/Documents/Evaluations/SAP Model log pairs/evalSComp/";
		//String model = "1An_lbl5.pnml";
		//String log = "1An_lbl5_log_noise01.xes";
		//IntArrayList tr = new IntArrayList();
		//tr.addAll(13,13,13,13,13,13,13,13,13,13,13,13,1,1,1,1,1,1,1,12,12,13);
		//System.out.println("lenght: " + tr.size() + ", 13s: " + tr.count(x->x==13) + ", 1s: " + tr.count(x->x==1) +", 12s: " + tr.count(x->x==12));
		//String path = "/Users/dreissner/Documents/Evaluations/Adriano model-log pairs full/public/";
		//String model = "3.pnml";
		//String log = "3.xes.gz";
		//DecomposingConformanceImporter decomposer = new DecomposingConformanceImporter();
		//decomposer.importAndDecomposeModelAndLogForConformanceChecking(path, model, log);
		//XLog xLog = new ImportEventLog().importEventLog(path + log);

		/*FastList<IntArrayList> log = new FastList<IntArrayList>();
		IntArrayList arr = new IntArrayList();
		arr.addAll(1,2,3,4,5,6,7,8,9,0);
		log.add(arr);
		arr = new IntArrayList();
		arr.addAll(5,6,8,9,10);
		log.add(arr);

		XFactoryNaiveImpl xFac = new XFactoryNaiveImpl();
		XLog rescueLog = xFac.createLog();
		XExtension xtend = XConceptExtension.instance();
		XAttributeLiteral xAttrLiteral = null; //xFac.createAttributeLiteral("concept:name", "", xtend);
		for(IntArrayList ar : log) {
			XTrace xTr = xFac.createTrace();
			for (int ev : ar.toArray()) {
				xAttrLiteral = xFac.createAttributeLiteral("concept:name", ""+ev, xtend);
				XAttributeMap xAttr = xFac.createAttributeMap();
				xAttrLiteral.setValue("" + ev);
				xAttr.put("concept:name", xAttrLiteral);
				XEvent xEv = xFac.createEvent(xAttr);
				xTr.add(xEv);
			}
			rescueLog.add(xTr);
		}
		int i=1;
		for(XTrace trace : rescueLog)
		{
			System.out.println(i++ + ":");
			for(XEvent ev : trace)
				System.out.print(((XAttributeLiteral) ev.getAttributes().get("concept:name")).getValue() + ", ");
			System.out.println();
		}*/
		/*LongArrayList times = new LongArrayList();
		for(int i=0;i<5;i++) {
			long start = System.nanoTime();
			MultiThreadedConformanceChecker checker = new MultiThreadedConformanceChecker(args[0], args[1], args[2], 16);
			long time = System.nanoTime() - start;
			times.add(checker.timeOneOptimal);
		}
		System.out.println(times.average());*/
		/*LongArrayList times = new LongArrayList();
		DoubleArrayList costs = new DoubleArrayList();
		for(int i=0; i<5; i++)
		{
			DecomposingConformanceImporter decomposer = new DecomposingConformanceImporter();
			XLog xLog = new ImportEventLog().importEventLog(args[0] + args[2]);
			Object[] pnetAndM = new ImportProcessModel().importPetriNetAndMarking(args[0] + args[1]);
			//decomposer.importAndDecomposeModelAndLogForConformanceChecking(path, model, log);
			long start = System.nanoTime();
			decomposer.importAndDecomposeModelAndLogForConformanceChecking((Petrinet) pnetAndM[0],(Marking) pnetAndM[1], xLog);
			MultiThreadedDecomposedConformanceChecker pro =  new MultiThreadedDecomposedConformanceChecker(decomposer,1,8);
			times.add(TimeUnit.MILLISECONDS.convert(System.nanoTime()-start, TimeUnit.NANOSECONDS));
		}
		System.out.println(times.average());*/
		if(args[3].equals("Param"))
		{
			FastList<String> paths = new FastList<>();
			paths.add("/Users/dreissner/Documents/Evaluations/SComponentPaper/LfMf/public/im/");
			paths.add("/Users/dreissner/Documents/Evaluations/SComponentPaper/LfMf/public/sm/");
			paths.add("/Users/dreissner/Documents/Evaluations/SComponentPaper/LfMf/private/im/");
			paths.add("/Users/dreissner/Documents/Evaluations/SComponentPaper/LfMf/private/sm/");
			for(String path : paths)
			{
				IntArrayList datasets = null;
				if(path.contains("public"))
				{
					datasets = new IntArrayList();
					datasets.addAll(1,2,3,4,5,6,7,8,9,10,11,12);
				}
				else
				{
					datasets = new IntArrayList();
					datasets.addAll(1,2,3,4,6,7,9,10);
				}
				for(int dataset : datasets.toArray())
				{
					DecomposingConformanceImporter stats = new DecomposingConformanceImporter();
					stats.importAndDecomposeModelAndLogForConformanceChecking(path, dataset + ".pnml", dataset + ".xes.gz");
					stats.gatherStatistics();
					recordStatistics(path, dataset, stats);
				}
			}
			System.exit(0);
		}
		if(args.length==4) {
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Future<String> future = executor.submit(new ConformanceWrapperSComp(args));
			String result = null;
			try {
				result = future.get(10, TimeUnit.MINUTES); //timeout is in 2 seconds
			} catch (TimeoutException e) {
				System.err.println("Timeout");
				result = args[3] + "," + args[0] + "," + args[1] + "," + "," + "," + "," + args[2] + "," + ", time out" + "," + "," + "\n";
				future.cancel(true);
			}
			executor.shutdownNow();
			//String result = new ConformanceWrapperSComp(args).call();
			System.out.println(result);
			FileWriter pw = null;
			File eval = new File(args[0] + "evaluation.txt");
			if (!eval.exists()) {
				pw = new FileWriter(args[0] + "evaluation.txt", true);
				pw.append("Type,path,model,#SComps,#||,#Choice,log,log size,time,cost,#conflicts\n");
			}
			if (pw == null) pw = new FileWriter(args[0] + "evaluation.txt", true);
			pw.append(result);
			pw.close();
			System.exit(0);
		}
		else if(args.length > 4 && args.length < 8) {
			int numExecutions = Integer.parseInt(args[4]);
			LongArrayList times = new LongArrayList();
			DoubleArrayList costs = new DoubleArrayList();
			IntArrayList conflicts = new IntArrayList();
			ConformanceWrapperSCompMT confTask = null;
			String result = "";
			for (int i = 0; i < numExecutions; i++) {
				ExecutorService executor = Executors.newSingleThreadExecutor();
				Future<ConformanceWrapperSCompMT> future = executor.submit(new ConformanceWrapperSCompMT(args));
				try {
					confTask = future.get(10, TimeUnit.MINUTES); //timeout is in 10 minutes
				} catch (TimeoutException e) {
					System.err.println("Timeout");
					executor.shutdownNow();
					result = args[3] + "," + args[1] + "," + "," + "," + "," + args[2] + "," + ", time out" + "," + "," + "\n";
					future.cancel(true);
					recordResult(args[0], result);
					System.exit(1);
				}
				executor.shutdown();
				if(numExecutions >=3 && (i==0 || i== numExecutions-1)) continue;
				times.add(confTask.time);
				costs.add(confTask.cost);
				conflicts.add(confTask.conflicts);
				//System.out.println(confTask.result);
			}
			result = confTask.type + "," + confTask.model + "," + confTask.nSComps + "," + confTask.nParallel + "," + confTask.nChoice + "," + confTask.log + "," + confTask.logSize + "," + times.average() + "," + costs.average() + "," + conflicts.average() + "\n";
			System.out.println(result);
			recordResult(args[0], result);
			System.exit(0);
		}
	}

	private static void recordStatistics(String path, int dataset, DecomposingConformanceImporter stats) throws Exception
	{
		FileWriter pw = null;
		File statF = new File(path+"stats.txt");
		if(!statF.exists())
		{
			pw = new FileWriter(path + "stats.txt", true);
			pw.append("Path,Model,#Places,#Transitions,#Arcs,Size,#Choices,#Parallel,#RGNodes,#RGArcs,RGSize,#SComponents,@Places,&Places,@Transitions,&Transitions,@Arcs,&Arcs,@Size,&Size,@RGNodes,&RGNodes,@RGArcs,&RGArcs,@RGSize,&RGSize,@Choices,&Choices\n");
		}
		if(pw==null) pw = new FileWriter(path + "stats.txt", true);
		String result = path + "," + dataset + "," + stats.places + "," + stats.transitions + "," + stats.arcs + "," + stats.size + "," + stats.choice + "," + stats.parallel + "," + stats.rg_nodes + "," + stats.rg_arcs + "," + stats.rg_size + ",";
		if(stats.doDecomposition)
			result+= stats.sComponentFSMs.size() + "," +stats.scompPlaces.average() +"," + stats.scompPlaces.sum() + "," + stats.scompTransitions.average() + "," + stats.scompTransitions.sum() + ","
					+ stats.scompArcs.average() + "," + stats.scompArcs.sum() + "," + stats.scompSize.average() + "," + stats.scompSize.sum() + "," + stats.scompRGNodes.average() + "," + stats.scompRGNodes.sum() + ","
					+ stats.scompRGArcs.average() + "," + stats.scompRGArcs.sum() + "," + stats.scompRGSize.average() + "," + stats.scompRGSize.sum() + "," + stats.scompChoices.average() + "," + stats.scompChoices.sum() + "\n";
		else
			result+="1\n";
		pw.append(result);
		pw.close();

	}

	private static void recordResult(String path, String result) throws Exception
	{
		FileWriter pw = null;
		File eval = new File(path + "evaluation.txt");
		if(!eval.exists())
		{
			pw = new FileWriter(path + "evaluation.txt",true);
			pw.append("Type,model,#SComps,#||,#Choice,log,log size,time,cost,#conflicts\n");
		}
		if(pw ==null) pw = new FileWriter(path + "evaluation.txt",true);
		pw.append(result);
		pw.close();
	}
}
