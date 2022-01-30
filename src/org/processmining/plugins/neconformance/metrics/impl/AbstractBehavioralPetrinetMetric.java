package org.processmining.plugins.neconformance.metrics.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.groupedlog.GroupedXLog;
import org.processmining.plugins.neconformance.metrics.AbstractConformanceMetric;
import org.processmining.plugins.neconformance.models.ProcessReplayModel;
import org.processmining.plugins.neconformance.models.ProcessReplayModel.ReplayMove;
import org.processmining.plugins.neconformance.models.impl.PetrinetReplayModel;
import org.processmining.plugins.neconformance.models.impl.TraceReplayRunnable;
import org.processmining.plugins.neconformance.negativeevents.AbstractNegativeEventInducer;
import org.processmining.plugins.neconformance.types.WeightedEventClass;

public abstract class AbstractBehavioralPetrinetMetric extends AbstractConformanceMetric{
	
	protected double truePositives, falsePositives;
	protected double trueNegatives, falseNegatives;
	protected double allowedGeneralizations, disallowedGeneralizations;
	
	protected boolean checkNegatives, checkGeneralization;
	protected boolean checkUnmappedRecall, checkUnmappedPrecision;
	
	protected boolean useMultiThreadedCalculation;
	
	protected AbstractNegativeEventInducer inducer;
	protected GroupedXLog groupedLog;
	
	public AbstractBehavioralPetrinetMetric(
			ProcessReplayModel<?,?,?> replayModel, AbstractNegativeEventInducer inducer, XLog log,
			boolean checkNegatives, boolean checkGeneralization,
			boolean checkUnmappedRecall, boolean checkUnmappedPrecision,
			boolean useMultiTreadedCalculation) {
		super(replayModel);
		this.groupedLog = new GroupedXLog(log);
		this.inducer = inducer;
		this.checkNegatives = checkNegatives;
		this.checkGeneralization = checkGeneralization;
		this.checkUnmappedRecall = checkUnmappedRecall;
		this.checkUnmappedPrecision = checkUnmappedPrecision;
		this.useMultiThreadedCalculation = useMultiTreadedCalculation;
	}

	public void reset() {
		truePositives = 0;
		falsePositives = 0;
		trueNegatives = 0;
		falseNegatives = 0;
		allowedGeneralizations = 0;
		disallowedGeneralizations = 0;
	}
	
	public double getValue() {
		reset();
		
		BlockingQueue<Runnable> worksQueue = null;
		ThreadPoolExecutor executor = null;
		List<ReplayTask> taskList = new ArrayList<ReplayTask>();
		
		if (useMultiThreadedCalculation) {
			worksQueue = new ArrayBlockingQueue<Runnable>(10);
			executor = new ThreadPoolExecutor(10, 	// core size
					20, 	// max size
					1, 		// keep alive time
					TimeUnit.MINUTES, 	// keep alive time units
					worksQueue 			// the queue to use
			);
		}
		
		for (int t = 0; t < groupedLog.size(); t++){
			XTrace trace = groupedLog.get(t).get(0);
			int times = groupedLog.get(t).size();
			if (!useMultiThreadedCalculation) {
				evaluateTrace(trace, times);
			} else {
				List<XEventClass> classSequence = getTraceAsClassSequence(trace);
				@SuppressWarnings("unchecked")
				ProcessReplayModel<Transition, XEventClass, Marking> clonedModel = 
						(ProcessReplayModel<Transition, XEventClass, Marking>) replayModel.copy();
				taskList.add(new ReplayTask(
						new TraceReplayRunnable<Transition, XEventClass, Marking>(clonedModel, classSequence),
						trace, times));
			}
		}
		
		if (useMultiThreadedCalculation) {
			for (int i = 0; i < taskList.size(); i++) {
				while (executor.getQueue().remainingCapacity() == 0);
				executor.execute(taskList.get(i).runnable);
			}
			executor.shutdown();
			try {
				while (!executor.awaitTermination(10, TimeUnit.SECONDS));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			for (ReplayTask task : taskList) {
				this.evaluateTrace(task.runnable.getReplayModel(), task.trace, task.times);
			}
		}
		return getCalculatedValue();
	}

	protected abstract double getCalculatedValue();
	
	protected void evaluateTrace(XTrace trace, int times) {
		replayModel.reset();
		List<XEventClass> classSequence = getTraceAsClassSequence(trace);
		((PetrinetReplayModel)replayModel).replay(classSequence);
		
		this.evaluateTrace(replayModel, trace, times);
	}
	
	protected void evaluateTrace(ProcessReplayModel<?, ?, ?> replayModel, XTrace trace, int times) {
		Set<Transition> orphanedModelElements = ((PetrinetReplayModel)this.replayModel).getOrphanedModelElements();
		
		for (int step = 0; step < replayModel.size(); step++) {
			ReplayMove type = replayModel.getReplayMove(step);
			switch (type) {
			case BOTH_SYNCHRONOUS:
				truePositives += 1d * times;
				break;
			case BOTH_FORCED:
				falseNegatives += 1d * times;
				break;
			case LOGONLY_INSERTED:
				if (checkUnmappedRecall)
					falseNegatives += 1d * times;
				break;
			case MODELONLY_SKIPPED:
				// No event in the log
				break;
			case MODELONLY_UNOBSERVABLE:
				// Do nothing for invisible
				break;
			default:
				break;
			}
			
			// Check negatives and generalizations at this point
			if (!checkNegatives && !checkGeneralization)
				continue;
			
			// Determine where we are in the trace
			int latestNonInvisibleStep;
			int currentPositionInTrace = getTracePositionForReplayStep(replayModel, step);
			for (latestNonInvisibleStep = step-1; latestNonInvisibleStep >= 0; latestNonInvisibleStep--) {
				if (!replayModel.getReplayMove(latestNonInvisibleStep).equals(ReplayMove.MODELONLY_UNOBSERVABLE))
					break;
			}
	
			
			// Generalization and precision checking is based on synchronous movements...
			if (type.equals(ReplayMove.BOTH_SYNCHRONOUS) 
					|| type.equals(ReplayMove.BOTH_FORCED)) {		
				Set<XEventClass> executables = (latestNonInvisibleStep < 0) 
						? ((PetrinetReplayModel)replayModel).getExecutableLogElements(
								(Marking) replayModel.getInitialState())
						: ((PetrinetReplayModel)replayModel).getExecutableLogElements(
								(Marking) replayModel.getModelState(latestNonInvisibleStep));
				Set<Transition> modelExecutables = (latestNonInvisibleStep < 0) 
						? ((PetrinetReplayModel)replayModel).getExecutableModelElements(
								(Marking) replayModel.getInitialState())
						: ((PetrinetReplayModel)replayModel).getExecutableModelElements(
								(Marking) replayModel.getModelState(latestNonInvisibleStep));
				
				if (checkNegatives) {
					Set<WeightedEventClass> negativeevents = inducer.getNegativeEvents(trace, currentPositionInTrace);
					for (WeightedEventClass ec : negativeevents) {
						if (executables.contains(ec.eventClass))
							falsePositives += ec.weight * times; 
						else
							trueNegatives += ec.weight * times;
					}
					if (checkUnmappedPrecision) {
						for (Transition tr : orphanedModelElements) {
							if (modelExecutables.contains(tr))
								falsePositives += 1D * times;
						}
					}
				}
				if (checkGeneralization) {
					Set<WeightedEventClass> generalizedevents = inducer.getGeneralizedEvents(trace, currentPositionInTrace);
					for (WeightedEventClass ec : generalizedevents) {
						if (executables.contains(ec.eventClass))
							allowedGeneralizations += ec.weight * times;
						else
							disallowedGeneralizations += ec.weight * times;
					}
				}
			}
		}
	}
	
	private int getTracePositionForReplayStep(ProcessReplayModel<?, ?, ?> replayModel,
			int replayStep) {
		int positionInTrace = -1;
		for (int step = 0; step <= replayStep; step++) {
			ReplayMove type = replayModel.getReplayMove(step);
			if ( type.equals(ReplayMove.BOTH_SYNCHRONOUS) 
					|| type.equals(ReplayMove.BOTH_FORCED) 
					|| type.equals(ReplayMove.LOGONLY_INSERTED) ) {
				positionInTrace++;
			}
		}
		return positionInTrace;
	}
		
	private List<XEventClass> getTraceAsClassSequence(XTrace trace) {
		List<XEventClass> sequence = new ArrayList<XEventClass>();
		for (XEvent event : trace)
			sequence.add(inducer.getClassAlphabet().getClassOf(event));
		return sequence;
	}

}


class ReplayTask {
	public TraceReplayRunnable<Transition, XEventClass, Marking> runnable;
	public XTrace trace;
	public int times;

	public ReplayTask(TraceReplayRunnable<Transition, XEventClass, Marking> runnable, XTrace trace, int times) {
		this.runnable = runnable;
		this.trace = trace;
		this.times = times;
	}
}
