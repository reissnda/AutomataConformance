package org.processmining.plugins.neconformance.models.impl;

import java.util.List;

import org.processmining.plugins.neconformance.models.ProcessReplayModel;

public class TraceReplayRunnable<M, L, S> implements Runnable {
	
	private ProcessReplayModel<M, L, S> replayModel;
	private List<L> classSequence;

	public TraceReplayRunnable(ProcessReplayModel<M, L, S> replayModel, List<L> classSequence) {
		this.replayModel = replayModel;
		this.classSequence = classSequence;
	}
	
	public void run() {
		this.replayModel.reset();
		this.replayModel.replay(classSequence);
	}

	public ProcessReplayModel<M, L, S> getReplayModel() {
		return this.replayModel;
	}
}
