package org.processmining.plugins.neconformance.trees.ukkonen;

public class SuffixTree<I,S extends Iterable<I>> {

	private final Node<I,S> root;
	private final Sequence<I,S> sequence;

	private Suffix<I,S> suffix;
	private final ActivePoint<I,S> activePoint;
	private int currentEnd = 0;
	private int insertsThisStep = 0;
	private Node<I,S> lastNodeInserted = null;

	public SuffixTree(){
		sequence = new Sequence<I, S>();
		root = new Node<I,S>(null, this.sequence, this);
		activePoint = new ActivePoint<I,S>(root);
	}
	
	public SuffixTree(S sequenceArray) {
		sequence = new Sequence<I, S>(sequenceArray);
		root = new Node<I,S>(null, this.sequence, this);
		activePoint = new ActivePoint<I,S>(root);
		suffix = new Suffix<I, S>(0, 0, this.sequence);
		extendTree(0,sequence.getLength());
	}
	
	public void add(S sequence){
		int start = currentEnd;
		this.sequence.add(sequence);
		suffix = new Suffix<I,S>(currentEnd,currentEnd,this.sequence);
		activePoint.setPosition(root, null, 0);
		extendTree(start, this.sequence.getLength());
	}

	private void extendTree(int from, int to) {
		for (int i = from; i < to; i++){
			suffix.increment();
			insertsThisStep = 0;
			insert(suffix);
			currentEnd++;
		}
	}	
	
	void insert(Suffix<I, S> suffix) {
		if (activePoint.isNode()) {
			Node<I, S> node = activePoint.getNode();
			node.insert(suffix, activePoint);
		} else if (activePoint.isEdge()) {
			Edge<I,S> edge = activePoint.getEdge();
			edge.insert(suffix, activePoint);
		}
	}

	int getCurrentEnd() {
		return currentEnd;
	}

	Node<I,S> getRoot() {
		return root;
	}

	void incrementInsertCount() {
		insertsThisStep++;
	}

	boolean isNotFirstInsert() {
		return insertsThisStep > 0;
	}

	Node<I,S> getLastNodeInserted() {
		return lastNodeInserted;
	}

	void setLastNodeInserted(Node<I,S> node) {
		lastNodeInserted = node;
	}

	void setSuffixLink(Node<I,S> node) {
		if (isNotFirstInsert()) {
			lastNodeInserted.setSuffixLink(node);
		}
		lastNodeInserted = node;
	}

	@Override
	public String toString() {
		return Utils.printTreeForGraphViz(this);
	}
	
	Sequence<I,S> getSequence(){
		return sequence;
	}
}