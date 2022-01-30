package org.processmining.plugins.neconformance.trees.ukkonen;

class ActivePoint<T,S extends Iterable<T>> {
	
	private Node<T,S> activeNode;
	private Edge<T,S> activeEdge;
	private int activeLength;
	private final Node<T,S> root;

	ActivePoint(Node<T,S> root) {
		activeNode = root;
		activeEdge = null;
		activeLength = 0;
		this.root = root;
	}

	void setPosition(Node<T,S> node, Edge<T,S> edge, int length) {
		activeNode = node;
		activeEdge = edge;
		activeLength = length;
	}

	void setEdge(Edge<T,S> edge) {
		activeEdge = edge;
	}

	void incrementLength() {
		activeLength++;
		resetActivePointToTerminal();
	}

	void decrementLength() {
		if (activeLength > 0)
			activeLength--;
		resetActivePointToTerminal();
	}

	boolean isRootNode() {
		return activeNode.equals(root) && activeEdge == null
				&& activeLength == 0;
	}

	boolean isNode() {
		return activeEdge == null && activeLength == 0;
	}

	Node<T,S> getNode() {
		return activeNode;
	}

	boolean isEdge() {
		return activeEdge != null;
	}

	Edge<T,S> getEdge() {
		return activeEdge;
	}

	int getLength() {
		return activeLength;
	}

	public void updateAfterInsert(Suffix<T,S> suffix) {
		if (activeNode == root && suffix.isEmpty()) {
			activeNode = root;
			activeEdge = null;
			activeLength = 0;
		} else if (activeNode == root) {
			Object item = suffix.getStart();
			activeEdge = root.getEdgeStarting(item);
			decrementLength();
			fixActiveEdgeAfterSuffixLink(suffix);
			if (activeLength == 0)
				activeEdge = null;
		} else if (activeNode.hasSuffixLink()) {
			activeNode = activeNode.getSuffixLink();
			findTrueActiveEdge();
			fixActiveEdgeAfterSuffixLink(suffix);
			if (activeLength == 0)
				activeEdge = null;
		} else{
			activeNode = root;
			findTrueActiveEdge();
			fixActiveEdgeAfterSuffixLink(suffix);
			if (activeLength == 0)
				activeEdge = null;
		}
	}

	private void fixActiveEdgeAfterSuffixLink(Suffix<T,S> suffix) {
		while (activeEdge != null && activeLength > activeEdge.getLength()) {
			activeLength = activeLength - activeEdge.getLength();
			activeNode = activeEdge.getTerminal();
			Object item = suffix.getItemXFromEnd(activeLength + 1);
			activeEdge = activeNode.getEdgeStarting(item);
		}
		resetActivePointToTerminal();
	}

	private void findTrueActiveEdge() {
		if (activeEdge != null) {
			Object item = activeEdge.getStartItem();
			activeEdge = activeNode.getEdgeStarting(item);
		}
	}

	private boolean resetActivePointToTerminal() {
		if (activeEdge != null && activeEdge.getLength() == activeLength
				&& activeEdge.isTerminating()) {
			activeNode = activeEdge.getTerminal();
			activeEdge = null;
			activeLength = 0;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "{" + activeNode.toString() + ", " + activeEdge + ", "
				+ activeLength + "}";
	}
}
