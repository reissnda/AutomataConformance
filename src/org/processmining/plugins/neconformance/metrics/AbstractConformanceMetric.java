package org.processmining.plugins.neconformance.metrics;

import org.processmining.plugins.neconformance.models.ProcessReplayModel;

public abstract class AbstractConformanceMetric implements ConformanceMetric {
	protected ProcessReplayModel<?, ?, ?> replayModel;
	
	public AbstractConformanceMetric(ProcessReplayModel<?, ?, ?> replayModel) {
		this.replayModel = replayModel;
	}

	public abstract double getValue();
}
