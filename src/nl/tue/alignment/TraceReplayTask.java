package nl.tue.alignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import gnu.trove.map.TObjectIntMap;
import nl.tue.alignment.Utils.Statistic;
import nl.tue.alignment.algorithms.ReplayAlgorithm;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;
import nl.tue.alignment.algorithms.implementations.AStar;
import nl.tue.alignment.algorithms.implementations.AStarLargeLP;
import nl.tue.alignment.algorithms.implementations.Dijkstra;
import nl.tue.alignment.algorithms.syncproduct.SyncProduct;
import nl.tue.astar.Trace;
import nl.tue.astar.util.ilp.LPMatrixException;

public class TraceReplayTask implements Callable<TraceReplayTask> {

	public static enum TraceReplayResult {
		FAILED, DUPLICATE, SUCCESS
	}

	// transient fields. Cleared after call();
	private transient Replayer replayer;
	private transient XTrace trace;
	private transient ReplayerParameters parameters;
	private transient SyncProduct product;
	private transient ReplayAlgorithm algorithm;

	// Available after "call()"
	private int original;
	private final int traceIndex;
	private SyncReplayResult srr;
	private TraceReplayResult result;
	private int traceLogMoveCost;

	// internal variables
	private int[] alignment;
	private int maximumNumberOfStates;
	private int[] eventsWithErrors;
	private final long preProcessTimeNanoseconds;
	private final int timeoutMilliseconds;

	public TraceReplayTask(Replayer replayer, ReplayerParameters parameters, int timeoutMilliseconds,
			int maximumNumberOfStates, long preProcessTimeNanoseconds, int... eventsWithErrors) {
		this.replayer = replayer;
		this.parameters = parameters;
		this.maximumNumberOfStates = maximumNumberOfStates;
		this.preProcessTimeNanoseconds = preProcessTimeNanoseconds;
		this.trace = XFactoryRegistry.instance().currentDefault().createTrace();
		XConceptExtension.instance().assignName(trace, "Empty");
		this.traceIndex = -1;
		this.timeoutMilliseconds = timeoutMilliseconds;
		this.eventsWithErrors = eventsWithErrors;
		Arrays.sort(eventsWithErrors);
	}

	@Deprecated
	public TraceReplayTask(Replayer replayer, ReplayerParameters parameters, int timeoutMilliseconds,
			int maximumNumberOfStates, int... eventsWithErrors) {
		this(replayer, parameters, timeoutMilliseconds, maximumNumberOfStates, 0, eventsWithErrors);
	}

	public TraceReplayTask(Replayer replayer, ReplayerParameters parameters, XTrace trace, int traceIndex,
			int timeoutMilliseconds, int maximumNumberOfStates, long preProcessTimeNanoseconds,
			int... eventsWithErrors) {
		this.replayer = replayer;
		this.parameters = parameters;
		this.trace = trace;
		this.traceIndex = traceIndex;
		this.timeoutMilliseconds = timeoutMilliseconds;
		this.maximumNumberOfStates = maximumNumberOfStates;
		this.preProcessTimeNanoseconds = preProcessTimeNanoseconds;
		this.eventsWithErrors = eventsWithErrors;
		Arrays.sort(eventsWithErrors);

	}

	@Deprecated
	public TraceReplayTask(Replayer replayer, ReplayerParameters parameters, XTrace trace, int traceIndex,
			int timeoutMilliseconds, int maximumNumberOfStates, int... eventsWithErrors) {
		this(replayer, parameters, trace, traceIndex, timeoutMilliseconds, maximumNumberOfStates, 0, eventsWithErrors);
	}

	private int getTraceCost(XTrace trace) {
		int cost = 0;
		for (XEvent e : trace) {
			cost += replayer.getCostLM(replayer.getEventClass(e));
		}
		return cost;
	}

	public TraceReplayTask call() throws LPMatrixException {
		if (replayer == null) {
			return this;
		}

		Trace traceAsList = this.replayer.factory.getTrace(trace, parameters.partiallyOrderEvents);
		this.traceLogMoveCost = getTraceCost(trace);

		if (this.replayer.mergeDuplicateTraces) {
			synchronized (this.replayer.trace2FirstIdenticalTrace) {
				original = this.replayer.trace2FirstIdenticalTrace.get(traceAsList);
				if (original < 0) {
					this.replayer.trace2FirstIdenticalTrace.put(traceAsList, traceIndex);
				}
			}
		} else {
			original = -1;
		}
		if (original < 0) {
			//			System.out.println("Starting trace: " + traceIndex);
			List<Transition> transitionList = new ArrayList<Transition>();
			long pt;
			synchronized (this.replayer.factory) {
				long startSP = System.nanoTime();
				product = this.replayer.factory.getSyncProduct(trace, transitionList, parameters.partiallyOrderEvents);
				long endSP = System.nanoTime();
				// compute the pre-process time based on the fixed overhead, plus time for this replayer...
 				pt = preProcessTimeNanoseconds + (endSP - startSP);
			}

			if (product != null) {
				if (parameters.debug == Debug.DOT) {
					Utils.toDot(product, ReplayAlgorithm.Debug.getOutputStream());
				}

				algorithm = getAlgorithm(product);

				algorithm.putStatistic(Statistic.PREPROCESSTIME, (int) (pt / 1000));
				algorithm.putStatistic(Statistic.CONSTRAINTSETSIZE, replayer.getConstraintSetSize());

				alignment = algorithm.run(this.replayer.getProgress(), timeoutMilliseconds, maximumNumberOfStates,
						parameters.costUpperBound);

				if (parameters.debug == Debug.DOT) {
					Utils.toDot(product, alignment, ReplayAlgorithm.Debug.getOutputStream());
				}
				TObjectIntMap<Statistic> stats = algorithm.getStatistics();

				srr = toSyncReplayResult(product, stats, alignment, trace, traceIndex, transitionList);
				this.replayer.getProgress().inc();
				result = TraceReplayResult.SUCCESS;
			} else {
				this.replayer.getProgress().inc();
				result = TraceReplayResult.FAILED;
			}
		} else {
			this.replayer.getProgress().inc();
			result = TraceReplayResult.DUPLICATE;
		}

		replayer = null;
		trace = null;
		parameters = null;
		algorithm = null;
		product = null;

		return this;
	}

	private SyncReplayResult toSyncReplayResult(SyncProduct product, TObjectIntMap<Statistic> statistics,
			int[] alignment, XTrace trace, int traceIndex, List<Transition> transitionList) {
		List<Object> nodeInstance = new ArrayList<>(alignment.length);
		List<StepTypes> stepTypes = new ArrayList<>(alignment.length);
		int mm = 0, lm = 0, smm = 0, slm = 0;
		for (int i = 0; i < alignment.length; i++) {
			int t = alignment[i];
			if (product.getTypeOf(t) == SyncProduct.LOG_MOVE) {
				nodeInstance.add((String) ((XAttributeLiteral) trace.get(product.getEventOf(t)).getAttributes().get("concept:name")).getValue());
				stepTypes.add(StepTypes.L);
				lm += product.getCost(t);
			} else {
				nodeInstance.add(transitionList.get(t).getLabel());
				if (product.getTypeOf(t) == SyncProduct.MODEL_MOVE) {
					stepTypes.add(StepTypes.MREAL);
					mm += product.getCost(t);
				} else if (product.getTypeOf(t) == SyncProduct.SYNC_MOVE) {
					stepTypes.add(StepTypes.LMGOOD);
					smm += replayer.getCostMM(transitionList.get(t));
					slm += replayer.getCostLM(replayer.classes.getClassOf(trace.get(product.getEventOf(t))));
				} else if (product.getTypeOf(t) == SyncProduct.TAU_MOVE) {
					stepTypes.add(StepTypes.MINVI);
					mm += product.getCost(t);
				}
			}
		}

		SyncReplayResult srr = new SyncReplayResult(nodeInstance, stepTypes, traceIndex);
		srr.addInfo(PNRepResult.RAWFITNESSCOST, 1.0 * statistics.get(Statistic.COST));
		srr.addInfo(PNRepResult.TIME,
				(statistics.get(Statistic.PREPROCESSTIME) + statistics.get(Statistic.TOTALTIME)) / 1000.0);
		srr.addInfo(PNRepResult.QUEUEDSTATE, 1.0 * statistics.get(Statistic.QUEUEACTIONS));
		if (lm + slm == 0) {
			srr.addInfo(PNRepResult.MOVELOGFITNESS, 1.0);
		} else {
			srr.addInfo(PNRepResult.MOVELOGFITNESS, 1.0 - (1.0 * lm) / (lm + slm));
		}
		if (mm + smm == 0) {
			srr.addInfo(PNRepResult.MOVEMODELFITNESS, 1.0);
		} else {
			srr.addInfo(PNRepResult.MOVEMODELFITNESS, 1.0 - (1.0 * mm) / (mm + smm));
		}
		srr.addInfo(PNRepResult.NUMSTATEGENERATED, 1.0 * statistics.get(Statistic.MARKINGSREACHED));
		srr.addInfo(PNRepResult.ORIGTRACELENGTH, 1.0 * trace.size());
		srr.addInfo(Replayer.TRACEEXITCODE, new Double(statistics.get(Statistic.EXITCODE)));
		srr.addInfo(Replayer.MEMORYUSED, new Double(statistics.get(Statistic.MEMORYUSED)));
		srr.addInfo(Replayer.PREPROCESSTIME, (statistics.get(Statistic.PREPROCESSTIME)) / 1000.0);
		srr.addInfo(Replayer.HEURISTICSCOMPUTED, (double) statistics.get(Statistic.HEURISTICSCOMPUTED));
		srr.setReliable(statistics.get(Statistic.EXITCODE) == Utils.OPTIMALALIGNMENT);
		return srr;
	}

	private ReplayAlgorithm getAlgorithm(SyncProduct product) throws LPMatrixException {
		switch (parameters.algorithm) {
			case ASTAR :
				return new AStar(product, parameters.moveSort, parameters.queueSort, parameters.preferExact, //
						parameters.useInt, parameters.debug);
			case INCREMENTALASTAR :
				if (parameters.initialSplits > 0 && eventsWithErrors.length == 0) {
					return new AStarLargeLP(product, parameters.moveSort, parameters.useInt, parameters.initialSplits,
							parameters.debug);
				} else {
					return new AStarLargeLP(product, parameters.moveSort, parameters.useInt, parameters.debug,
							eventsWithErrors);
				}
			case DIJKSTRA :
				return new Dijkstra(product, parameters.moveSort, parameters.queueSort, parameters.debug);
		}
		assert false;
		return null;
	}

	public TraceReplayResult getResult() {
		return result;
	}

	public SyncReplayResult getSuccesfulResult() {
		return srr;
	}

	public int getOriginalTraceIndex() {
		return original;
	}

	public int getTraceIndex() {
		return traceIndex;
	}

	public int getTraceLogMoveCost() {
		return traceLogMoveCost;
	}
}