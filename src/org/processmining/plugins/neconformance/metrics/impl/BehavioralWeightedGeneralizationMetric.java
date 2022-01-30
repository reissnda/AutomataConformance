package org.processmining.plugins.neconformance.metrics.impl;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.neconformance.models.impl.PetrinetReplayModel;
import org.processmining.plugins.neconformance.negativeevents.AbstractNegativeEventInducer;

public class BehavioralWeightedGeneralizationMetric extends AbstractBehavioralPetrinetMetric {

	public BehavioralWeightedGeneralizationMetric(
		PetrinetReplayModel replayModel, AbstractNegativeEventInducer inducer, XLog log,
		boolean checkUnmapped, boolean useMultiTreadedCalculation) {
		// checkUnmapped is not really used here... for now.
		super(replayModel, inducer, log, false, true, false, false, useMultiTreadedCalculation);
	}

	protected double getCalculatedValue() {
		if (allowedGeneralizations == 0 && disallowedGeneralizations == 0)
			return 1d;
		return (allowedGeneralizations) / (allowedGeneralizations + disallowedGeneralizations);
	}

}
