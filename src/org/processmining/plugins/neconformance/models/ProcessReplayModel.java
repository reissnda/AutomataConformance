package org.processmining.plugins.neconformance.models;

import java.util.List;
import java.util.Set;

public interface ProcessReplayModel<M,L,S> {
	public static enum ReplayMove {
		// Say that model element x was executed and y is the current activity under consideration
									// Both model and log move
		BOTH_SYNCHRONOUS,			// Model element fired normally and corresponded with desired activity		(LMGOOD)
		BOTH_FORCED,				// Desired activity was fired in abnormal manner (e.g. forced transition)	(LMNOGOOD)
		
									// Model moves only (no move in log)
		MODELONLY_UNOBSERVABLE,		// The model element is invisible and was free-to-fire						(MINVI)
		MODELONLY_SKIPPED,			// The model element was not invisible and skipped logging in the trace		(MREAL)
		
									// Log moves only (no move in model)
		LOGONLY_INSERTED,			// Log moved without move in model (inserted in the trace)					(L)
	};
	
	public M getModelElement(int index);
	public L getTraceElement(int index);
	public S getInitialState();
	public S getModelState(int index);
	public Set<M> getExecutableModelElements(S state);
	public Set<L> getExecutableLogElements(S state);
	public Set<M> getOrphanedModelElements();
	public Set<L> getOrphanedLogElements();
	public boolean isExecutableModelElement(M element, S state);
	public boolean isExecutableLogElement(L element, S state);

	public void addReplayStep(ReplayMove move, M modelElement, L traceElement, S stateAfterStep);
	public int size();
	public void reset();
	public void replay(List<L> trace);
	public ReplayMove getReplayMove(int step);
	
	public ProcessReplayModel<M,L,S> copy();
	
}
