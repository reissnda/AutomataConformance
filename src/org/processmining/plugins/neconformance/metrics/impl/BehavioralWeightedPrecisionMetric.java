package org.processmining.plugins.neconformance.metrics.impl;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.neconformance.models.impl.PetrinetReplayModel;
import org.processmining.plugins.neconformance.negativeevents.AbstractNegativeEventInducer;

public class BehavioralWeightedPrecisionMetric extends AbstractBehavioralPetrinetMetric {

	public BehavioralWeightedPrecisionMetric(
			PetrinetReplayModel replayModel, AbstractNegativeEventInducer inducer, XLog log,
			boolean checkUnmapped, boolean useMultiTreadedCalculation) {
			super(replayModel, inducer, log, true, false, false, checkUnmapped, useMultiTreadedCalculation);
	}

	protected double getCalculatedValue() {
		if (truePositives == 0 && falsePositives == 0)
			return 1d;
		return (truePositives) / (truePositives + falsePositives);
	}

}
