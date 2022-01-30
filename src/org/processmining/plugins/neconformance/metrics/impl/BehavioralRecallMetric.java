package org.processmining.plugins.neconformance.metrics.impl;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.neconformance.models.impl.PetrinetReplayModel;
import org.processmining.plugins.neconformance.negativeevents.AbstractNegativeEventInducer;

public class BehavioralRecallMetric extends AbstractBehavioralPetrinetMetric {

	public BehavioralRecallMetric(
			PetrinetReplayModel replayModel, AbstractNegativeEventInducer inducer, XLog log,
			boolean checkUnmapped, boolean useMultiTreadedCalculation) {
		super(replayModel, inducer, log, false, false, checkUnmapped, false, useMultiTreadedCalculation);
	}

	protected double getCalculatedValue() {
		if (truePositives == 0 && falseNegatives == 0)
			return 1d;
		return (truePositives) / (truePositives + falseNegatives);
	}
}
