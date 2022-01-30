package au.unimelb.evaluation;

import org.deckfour.xes.model.XLog;
import org.processmining.antialignments.bruteforce.DistanceMetric;
import org.processmining.antialignments.ilp.AbstractILPCalculator;
import org.processmining.antialignments.ilp.antialignment.AntiAlignmentParameters;
import org.processmining.antialignments.ilp.antialignment.AntiAlignmentPlugin;
import org.processmining.antialignments.ilp.antialignment.AntiAlignmentValues;
import org.processmining.antialignments.ilp.antialignment.HeuristicAntiAlignmentAlgorithm;
import org.processmining.antialignments.ilp.util.AntiAlignments;
import org.processmining.framework.plugin.Progress;
import org.processmining.log.utils.XLogBuilder;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.importing.tpn.TpnImport;
import org.processmining.plugins.petrinet.importing.tpn.TpnParser;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class AntiAlignmentTask implements Callable<String>
{
	private XLog xLog;
	private Marking finalMarking;
	private Marking initMarking;
	private Petrinet pnet;
	private String model;
	private String log;

	public AntiAlignmentTask(String log,String model,Petrinet pnet, Marking initMarking, Marking finalMarking, XLog xLog)
	{
		this.log=log;
		this.model=model;
		this.pnet=pnet;
		this.initMarking =initMarking;
		this.finalMarking=finalMarking;
		this.xLog=xLog;
	}

	@Override
	public String call() throws Exception {
		long executionTime = System.nanoTime();
		AlignmentSetup alignmentAlgorithm = new AlignmentSetup(pnet, xLog);

		// Compute Alignments
		//ERROR Null Context
		PNRepResult alignments = alignmentAlgorithm.getAlignment(null, initMarking, finalMarking, false, 0);

		//return new AntiAlignmentPlugin().basicCodeStructureWithAlignments(null, net, initialMarking, finalMarking,
		//		xLog, alignments, alignmentAlgorithm.mapping, new AntiAlignmentParameters(5, 1.0, 2, 2.0));

		HeuristicAntiAlignmentAlgorithm algorithm = new HeuristicAntiAlignmentAlgorithm(pnet, initMarking,
				finalMarking, xLog, alignments, alignmentAlgorithm.mapping);

		AntiAlignments aa = algorithm.computeAntiAlignments(null, new AntiAlignmentParameters(5, 1.0, 2, 2.0));

		AntiAlignmentValues values = algorithm.computePrecisionAndGeneralization(aa);
		executionTime=System.nanoTime()-executionTime;
		String result=log+","+model+ ",AntiAlignments,"+ TimeUnit.MILLISECONDS.convert(executionTime,TimeUnit.NANOSECONDS) + "," +values.getGeneralization() +",\n";

		return result;
	}

	public static class MarkedNet {
		public final Petrinet net;
		public final Marking initialMarking;
		public final Marking finalMarking;

		public MarkedNet(Petrinet net, Marking initialMarking, Marking finalMarking) {
			this.net = net;
			this.initialMarking = initialMarking;
			this.finalMarking = finalMarking;

		}
	}

	private static final String FREQUENCYATTRIBUTE = "Frequency";

	public static void main(String[] args) throws IOException {
		AbstractILPCalculator.NAMES = true;

		// String path =
		// "D:\\Documents\\Dropbox\\Boudewijn Werk\\Research\\Papers\\"
		// +
		// "2016 Precision Generalization Josep\\Models for experiments\\ExampleBookWil\\";
		String path = "D:\\Documents\\Dropbox\\Boudewijn Werk\\Research\\Papers\\"
				+ "2016 Precision Generalization Josep\\Models for experiments\\ExampleBookWil\\";

		// path = "D:\\Documents\\CloudStation private\\Werk\\Reviews\\INS-D-16-665\\";

		//		Set<String> labels = new HashSet<String>();
		//		XLog log = loadLogFullTrace(path + "log.trace", labels);
		//		//
		//		OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(path + "log_whole.xez")));
		//		new XesXmlGZIPSerializer().serialize(log, out);
		//		out.close();
		//		System.exit(0);

		// String path =
		// "D:\\Documents\\Dropbox\\Boudewijn Werk\\Research\\Papers\\"
		// +
		// "2016 Precision Generalization Josep\\Models for experiments\\NastyExamples\\";

		// String path =
		// "D:\\Documents\\Dropbox\\Boudewijn Werk\\Research\\Papers\\"
		// +
		// "2016 Precision Generalization Josep\\Models for experiments\\RolePrecisionGeneralizationJoos\\";

		boolean verbose = false;

		double maxFactor = 1;

		// TODO: All experiments; go to 10;
		for (File g : new File(path).listFiles()) {
			if (g.getName().endsWith(".tr")) {
				String logfile = g.getName();

				if (!verbose) {
					System.out.println("Log: " + logfile);
					System.out.println("model\tFw\tP_t^u\tP_l^u\tP\tG_t^w\tG_l^w\tG");
				}
				for (File f : new File(path).listFiles()) {
					if (f.getName().endsWith(".tpn")) {
						String modelfile = f.getName();

						doExperimentForModelAndLog(path, modelfile, logfile, new DistanceMetric.Edit(), maxFactor,
								verbose);

					}
				}
				if (!verbose) {
					System.out.println();
				}

			}
		}
	}

	public static void doExperimentForModelAndLog(String path, String modelfile, String logfile, DistanceMetric metric,
			double maxFactor, boolean verbose) throws IOException {

		if (!verbose) {
			System.out.print(modelfile);
			System.out.print("\t");
		}

		MarkedNet markedNet = loadModel(path + modelfile, verbose);

		if (verbose) {
			System.out.println("Net loaded: " + markedNet.net.toString());
		}

		Set<String> labels = new HashSet<String>();
		for (Transition t : markedNet.net.getTransitions()) {
			if (!t.isInvisible()) {
				labels.add(t.getLabel());
			}
		}

		String fullLogFile = path + logfile;
		XLog xLog = loadLog(fullLogFile, labels);
		if (verbose) {
			System.out.println("Log loaded: " + logfile);
		}
		//		String xesLogName = fullLogFile.substring(0, fullLogFile.lastIndexOf('.')) + ".xez";
		//		FileOutputStream out = new FileOutputStream(new File(xesLogName));
		//		new XesXmlGZIPSerializer().serialize(xLog, out);
		//		out.close();

		//PNRepResult result = basicCodeStructureWithoutAlignments(markedNet.net, markedNet.initialMarking,
		//		markedNet.finalMarking, xLog);

		if (!verbose) {
			//System.out.println(result.getInfo());
		}

		//		int[] frequencies = new int[xLog.size()];
		//
		//		TObjectShortMap<String> label2short = new TObjectShortHashMap<>(
		//				(5 * labels.size()) / 4);
		//		String[] short2label = new String[labels.size()];
		//		short i = 0;
		//		for (String label : labels) {
		//			short2label[i] = label;
		//			label2short.put(label, i);
		//			i++;
		//		}
		//
		//		// We have a mapping from shorts to labels and vv
		//		// Prepare the log in a model efficient form
		//		short[][] log = new short[xLog.size()][];
		//		int t = 0;
		//		int max = 0;
		//		for (XTrace trace : xLog) {
		//			log[t] = new short[trace.size()];
		//			frequencies[t] = (int) ((XAttributeDiscrete) trace.getAttributes()
		//					.get(FREQUENCYATTRIBUTE)).getValue();
		//			int e = 0;
		//			for (XEvent event : trace) {
		//				log[t][e] = label2short.get(XConceptExtension.instance()
		//						.extractName(event));
		//				e++;
		//			}
		//			if (log[t].length > max) {
		//				max = log[t].length;
		//			}
		//			t++;
		//		}
		//		if (verbose) {
		//			System.out.println("Log converted: " + log.length + " traces...");
		//		}
		//
		//		// Let's align the log to the model.
		//		AlignmentSetup alignmentAlgorithm = new AlignmentSetup(markedNet.net,
		//				xLog);
		//		PNRepResult alignments = alignmentAlgorithm.getAlignment(
		//				markedNet.initialMarking, markedNet.finalMarking, true);
		//
		//		Iterator<SyncReplayResult> it = alignments.iterator();
		//		List[] firingSequences = new List[log.length];
		//		short[][] alignedLog = new short[log.length][];
		//		double unweightedFitness = 0;
		//		double weightedFitness = 0;
		//		int sumFreq = 0;
		//		while (it.hasNext()) {
		//			SyncReplayResult res = it.next();
		//			for (Integer trace : res.getTraceIndex()) {
		//				firingSequences[trace] = new ArrayList<Transition>();
		//				TShortList modelSeq = new TShortArrayList();
		//				for (Object nodeStep : res.getNodeInstance()) {
		//					if ((nodeStep instanceof Transition)) {
		//						if (!((Transition) nodeStep).isInvisible()) {
		//							modelSeq.add(label2short
		//									.get(((Transition) nodeStep).getLabel()));
		//						}
		//						firingSequences[trace].add(nodeStep);
		//					}
		//				}
		//				alignedLog[trace] = modelSeq.toArray();
		//				if (alignedLog[trace].length > max) {
		//					max = alignedLog[trace].length;
		//				}
		//				unweightedFitness += (Double) res.getInfo().get(
		//						PNRepResult.TRACEFITNESS);
		//				weightedFitness += frequencies[trace]
		//						* (Double) res.getInfo().get(PNRepResult.TRACEFITNESS);
		//				sumFreq += frequencies[trace];
		//			}
		//		}
		//		unweightedFitness /= log.length;
		//		weightedFitness /= sumFreq;
		//
		//		// FIXME: Here, we give the anti-alignment algorithm the aligned log.
		//		// Should we do that?
		//		// We get anti-alignments for each trace, where the maximum length
		//		// is bound to maxFactor * traceLength.
		//		// The maximum length of the anti-alignment for the log is bound
		//		// to maxFactor * (max)
		//		max *= 2;
		//		long start = System.nanoTime();
		//		AntiAlignments aa = new DepthFirstTraceSearch(markedNet, label2short)
		//				.getAntiAlignments(alignedLog, max, maxFactor, metric);
		//		long mid = System.nanoTime();
		//		AntiAlignmentFinder finder = new AntiAlignmentFinder(markedNet.net,
		//				markedNet.initialMarking, markedNet.finalMarking, label2short);
		//		AntiAlignments aa2 = finder
		//				.getAntiAlignment(alignedLog, max, maxFactor);
		//		long end = System.nanoTime();
		//
		//		if (verbose) {
		//			printEditDistances(alignedLog, aa);
		//			printEditDistances(alignedLog, aa2);
		//			System.out.println("Time: " + (mid - start) / 1000000.0 + " vs. "
		//					+ (end - mid) / 1000000.0);
		//		}
		//		// BVD: Comment this out to use the new finder.
		//		// aa = aa2;
		//
		//		// test for equality
		//
		//		log = alignedLog;
		//
		//		double[] weightedPrecision = computePrecision(aa, log, frequencies,
		//				max, maxFactor);
		//		double[] unweightedPrecision = computePrecision(aa, log, null, max,
		//				maxFactor);
		//
		//		// now for the generalization part.
		//		// compute the states that were visited by the aligned log, using the
		//		// firing sequences in the aligned log
		//		Map<Marking, TShortSet> statesVisitedPerTrace = getStatesVisitedPerTrace(
		//				markedNet.net, markedNet.initialMarking, firingSequences);
		//
		//		int[] count = new int[log.length + 1];
		//		double[] recDist = new double[log.length + 1];
		//
		//		int[] cAndRD = countNewStatesAndRecoveryDistance(markedNet.net,
		//				markedNet.initialMarking, aa.getAAFiringSequenceForLog(),
		//				statesVisitedPerTrace, (short) -1);
		//		count[log.length] = cAndRD[0];
		//		recDist[log.length] = cAndRD[1];
		//
		//		for (short tr = 0; tr < log.length; tr++) {
		//
		//			cAndRD = countNewStatesAndRecoveryDistance(markedNet.net,
		//					markedNet.initialMarking,
		//					aa.getAAFiringSequenceForLogWithoutTrace(tr),
		//					statesVisitedPerTrace, tr);
		//			count[tr] = cAndRD[0];
		//			recDist[tr] = cAndRD[1];
		//
		//		}
		//		if (verbose) {
		//			printAntiAlignments(modelfile, aa, log, frequencies, count,
		//					recDist, short2label, verbose);
		//		}
		//
		//		double[] weightedGeneralization = computeGeneralization(aa, log,
		//				frequencies, recDist, count, max, maxFactor);
		//		double[] unweightedGeneralization = computeGeneralization(aa, log,
		//				null, recDist, count, max, maxFactor);
		//		if (verbose) {
		//			System.out.println("F_w:   " + weightedFitness);
		//			System.out.println("F_u:   " + unweightedFitness);
		//			System.out.println("P_t^w: " + weightedPrecision[0]);
		//			System.out.println("P_t^u: " + unweightedPrecision[0]);
		//			System.out.println("G_t^w: " + weightedGeneralization[0]);
		//			System.out.println("G_t^u: " + unweightedGeneralization[0]);
		//			System.out.println("P_l^w: " + weightedPrecision[1]);
		//			System.out.println("P_l^u: " + unweightedPrecision[1]);
		//			System.out.println("G_l^w: " + weightedGeneralization[1]);
		//			System.out.println("G_l^u: " + unweightedGeneralization[1]);
		//		}
		//

	}

	protected static void printEditDistances(short[][] log, AntiAlignments aa) {
		DistanceMetric.Edit edit = new DistanceMetric.Edit();
		int min = Integer.MAX_VALUE;
		System.out.print("whole log: ");
		for (int tr = 0; tr < log.length; tr++) {
			int d = edit.getDistance(log[tr], aa.getAAForLog());
			if (d < min) {
				min = d;
			}
			System.out.print("d(" + tr + ")=" + d + ", ");
		}
		System.out.println("Dlog=" + min);
		for (int tti = 0; tti < log.length; tti++) {
			min = Integer.MAX_VALUE;
			int dt = 0;
			System.out.print("ignore " + tti + "   ");
			for (int tr = 0; tr < log.length; tr++) {
				int d = edit.getDistance(aa.getAAForLogWithoutTrace(tti), log[tr]);
				if (tr != tti && d < min) {
					min = d;
				}
				if (tr == tti) {
					dt = d;
				}
				System.out.print("d(" + tr + ")=" + d + ", ");
			}
			System.out.println("Dlog=" + min + ",Drem=" + dt);
		}
	}

	public static String toString(short[] sequence, String[] short2label) {
		String s = "<";
		int i;
		for (i = 0; i < sequence.length; i++) {
			s += short2label[sequence[i]];
			s += i == sequence.length - 1 ? "" : ",";
		}
		s += ">";

		return s;
	}

	public static XLog loadLog(String filename, Set<String> labels) throws IOException {
		File logFile = new File(filename);

		BufferedReader reader = new BufferedReader(new FileReader(logFile));
		XLogBuilder builder = new XLogBuilder();
		builder.startLog(filename);
		try {
			int t = 1;
			String line = reader.readLine();
			while (line != null) {
				String[] events = line.split(" ");
				int freq = Integer.parseInt(events[0]);

				for (int c = 0; c < freq; c++) {
					builder.addTrace("line " + t + "." + c);
					//					builder.addAttribute(FREQUENCYATTRIBUTE, freq);
					for (int i = 1; i < events.length; i++) {
						builder.addEvent(events[i]);
						labels.add(events[i]);
					}
				}
				t++;
				line = reader.readLine();
			}

		} finally {
			reader.close();
		}
		return builder.build();
	}

	public static XLog loadLogFull(String filename, Set<String> labels) throws IOException {
		File logFile = new File(filename);

		BufferedReader reader = new BufferedReader(new FileReader(logFile));
		XLogBuilder builder = new XLogBuilder();
		builder.startLog(filename);
		try {
			int t = 1;
			String line = reader.readLine();
			while (line != null) {
				String[] events = line.split(" ");
				int freq = Integer.parseInt(events[0]);
				for (int tr = 0; tr < freq; tr++) {
					builder.addTrace("line " + t + " copy " + tr);
					// builder.addAttribute(FREQUENCYATTRIBUTE, freq);
					for (int i = 1; i < events.length; i++) {
						builder.addEvent(events[i]);
						if (tr == 0) {
							labels.add(events[i]);
						}
					}
				}
				t++;
				line = reader.readLine();
			}

		} finally {
			reader.close();
		}
		return builder.build();
	}

	public static XLog loadLogFullTrace(String filename, Set<String> labels) throws IOException {
		File logFile = new File(filename);

		BufferedReader reader = new BufferedReader(new FileReader(logFile));
		XLogBuilder builder = new XLogBuilder();
		builder.startLog(filename);
		try {
			int t = 1;
			String line = reader.readLine();
			while (line != null) {
				String[] events = line.split(":")[1].split(",");
				builder.addTrace("line " + t);
				// builder.addAttribute(FREQUENCYATTRIBUTE, freq);
				for (int i = 0; i < events.length; i++) {
					if (!events[i].trim().isEmpty()) {
						builder.addEvent(events[i]);
						labels.add(events[i]);
					}
				}
				t++;
				line = reader.readLine();
			}

		} finally {
			reader.close();
		}
		return builder.build();
	}

	public static MarkedNet loadModel(String filename, boolean verbose) throws IOException {

		File pnFile = new File(filename);
		InputStream in = new BufferedInputStream(new FileInputStream(pnFile));
		Petrinet petrinet = PetrinetFactory.newPetrinet(filename);
		Marking initialMarking = null;

		TpnParser parser = new TpnParser(in);
		try {
			parser.start(petrinet);
			initialMarking = parser.getState();
			// context.getProvidedObjectManager().createProvidedObject("Initial
			// marking of "+p.getLabel(), state, context);

			// logEvents = new LogEvents();
			Iterator<? extends Transition> it = petrinet.getTransitions().iterator();

			while (it.hasNext()) {
				Transition t = it.next();
				String s = t.getLabel();

				String DELIM = "\\n";
				int i = s.indexOf(DELIM);
				if ((i == s.lastIndexOf(DELIM)) && (i > 0)) {

					String s2 = s.substring(i + DELIM.length(), s.length());

					if (s2.equals(TpnImport.INVISIBLE_EVENT_TYPE)) {
						t.setInvisible(true);
					}
				}
			}

			// p.Test("TpnImport");

			if (verbose) {
				System.out.println("TPN Import finished.");
			}
			// return new ConnectedObjects(c);
		} catch (Throwable x) {
			throw new IOException(x.getMessage());
		} finally {
			in.close();
		}
		Marking finalMarking = new Marking();
		for (Place p : petrinet.getPlaces()) {
			if (petrinet.getOutEdges(p).isEmpty()) {
				finalMarking.add(p);
			}
		}

		return new MarkedNet(petrinet, initialMarking, finalMarking);
	}

	public static void printAntiAlignments(String model, AntiAlignments aa, short[][] log, int[] frequencies,
			int[] newStateCount, double[] recDistances, String[] short2label, boolean printHeader) {
		// Depth first search concluded. Let's report:
		if (printHeader) {
			System.out
					.println("Model      \tLog           \tRemoved Trace\tLength\tFreq\tAnti Alignment\tDlog\tDrem\tnewS\tDrec\tFiring Sequence");
		}

		System.out.print(model);
		System.out.print("\t");
		System.out.print("Whole log\t\t\t\t");
		System.out.print(toString(aa.getAAForLog(), short2label));
		System.out.print("\t");
		System.out.print(aa.getAADistanceForLog());
		System.out.print("\t");
		System.out.print("0\t");
		System.out.print(newStateCount[log.length]);
		System.out.print("\t");
		System.out.print(recDistances[log.length]);
		System.out.print("\t");
		System.out.print(aa.getAAFiringSequenceForLog());
		System.out.println();

		for (int t = 0; t < aa.getLogLength(); t++) {
			System.out.print(model);
			System.out.print("\t");
			System.out.print("Trace " + t + " removed\t");
			System.out.print(toString(log[t], short2label));
			System.out.print("\t");
			System.out.print(log[t].length);
			System.out.print("\t");
			System.out.print(frequencies[t]);
			System.out.print("\t");
			if (aa.getAAForLogWithoutTrace(t) == null) {
				System.out.print("none");
			} else {
				System.out.print(toString(aa.getAAForLogWithoutTrace(t), short2label));
			}
			System.out.print("\t");
			System.out.print(aa.getAADistanceForLogWithoutTrace(t));
			System.out.print("\t");
			System.out.print(aa.getAADistanceToTrace(t));
			System.out.print("\t");
			System.out.print(newStateCount[t]);
			System.out.print("\t");
			System.out.print(recDistances[t]);
			System.out.print("\t");
			if (aa.getAAFiringSequenceForLogWithoutTrace(t) == null) {
				System.out.print("none");
			} else {
				System.out.print(aa.getAAFiringSequenceForLogWithoutTrace(t).toString());
			}
			System.out.println();

		}

	}

	public static double basicCodeStructureWithoutAlignments(Petrinet net, Marking initialMarking,
			Marking finalMarking, XLog xLog) {

		// Setup the alignmentAlgorithm
		AlignmentSetup alignmentAlgorithm = new AlignmentSetup(net, xLog);

		// Compute Alignments
		//ERROR Null Context
		PNRepResult alignments = alignmentAlgorithm.getAlignment(null, initialMarking, finalMarking, false, 0);

		//return new AntiAlignmentPlugin().basicCodeStructureWithAlignments(null, net, initialMarking, finalMarking,
		//		xLog, alignments, alignmentAlgorithm.mapping, new AntiAlignmentParameters(5, 1.0, 2, 2.0));

		HeuristicAntiAlignmentAlgorithm algorithm = new HeuristicAntiAlignmentAlgorithm(net, initialMarking,
				finalMarking, xLog, alignments, alignmentAlgorithm.mapping);

		AntiAlignments aa = algorithm.computeAntiAlignments(null, new AntiAlignmentParameters(5, 1.0, 2, 2.0));

		AntiAlignmentValues values = algorithm.computePrecisionAndGeneralization(aa);

		return values.getGeneralization();

	}

	/*public PNRepResult basicCodeStructureWithAlignments(Progress progress, Petrinet net, Marking initialMarking,
														Marking finalMarking, XLog xLog, PNRepResult alignments, TransEvClassMapping mapping,
														AntiAlignmentParameters parameters) {

		HeuristicAntiAlignmentAlgorithm algorithm = new HeuristicAntiAlignmentAlgorithm(net, initialMarking,
				finalMarking, xLog, alignments, mapping);

		AntiAlignments aa = algorithm.computeAntiAlignments(progress, parameters);

		AntiAlignmentValues values = algorithm.computePrecisionAndGeneralization(aa);

		return algorithm.getPNRepResult(aa, values, parameters);

	}*/
}
