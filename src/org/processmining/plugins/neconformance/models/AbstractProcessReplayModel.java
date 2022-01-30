package org.processmining.plugins.neconformance.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractProcessReplayModel<M,L,S> implements ProcessReplayModel<M,L,S> {
	
	protected List<ReplayMove> replayMoveSequence = new ArrayList<ReplayMove>();
	protected Set<M> modelElements = new HashSet<M>();
	protected Set<L> logElements = new HashSet<L>();
	protected List<M> modelElementSequence = new ArrayList<M>();
	protected List<L> traceElementSequence = new ArrayList<L>();
	protected List<S> stateSequence = new ArrayList<S>();
	protected S initialState;
	
	protected AbstractProcessReplayModel(Collection<M> modelElements, Collection<L> logElements, S initialState) {
		this.modelElements = new HashSet<M>(modelElements);
		this.logElements = new HashSet<L>(logElements);
		this.initialState = initialState;
	}
	
	protected AbstractProcessReplayModel(AbstractProcessReplayModel<M, L, S> toClone) {
		this(toClone.getModelElements(), toClone.getLogElements(), toClone.getInitialState());
	}

	public int size() {
		return replayMoveSequence.size();
	}
	
	public ReplayMove getReplayMove(int index) {
		return replayMoveSequence.get(index);
	}
	
	public M getModelElement(int index) {
		return modelElementSequence.get(index);
	}
	
	public L getTraceElement(int index) {
		return traceElementSequence.get(index);
	}
	
	public S getModelState(int index) {
		if (index == -1)
			return getInitialState();
		return stateSequence.get(index);
	}
	
	public S getInitialState() {
		return initialState;
	}

	public Set<M> getModelElements() {
		return modelElements;
	}

	public Set<L> getLogElements() {
		return logElements;
	}
	
	public void addReplayStep(ReplayMove move, M modelElement, L traceElement, S stateAfterStep) {
		replayMoveSequence.add(move);
		modelElementSequence.add(modelElement);
		traceElementSequence.add(traceElement);
		stateSequence.add(stateAfterStep);
	}
	
	public void reset() {
		replayMoveSequence = new ArrayList<ReplayMove>();
		modelElementSequence = new ArrayList<M>();
		traceElementSequence = new ArrayList<L>();
		stateSequence = new ArrayList<S>();
	}

	public Set<M> getExecutableModelElements(S state) {
		Set<M> elements = new HashSet<M>();
		for (M element : this.getModelElements()) {
			if (this.isExecutableModelElement(element, state))
				elements.add(element);
		}
		return elements;
	}

	public Set<L> getExecutableLogElements(S state) {
		Set<L> elements = new HashSet<L>();
		for (L element : this.getLogElements()) {
			if (this.isExecutableLogElement(element, state))
				elements.add(element);
		}
		return elements;
	}

	public abstract boolean isExecutableModelElement(M element, S state);
	public abstract boolean isExecutableLogElement(L element, S state);
	
	public abstract void replay(List<L> trace);

}
