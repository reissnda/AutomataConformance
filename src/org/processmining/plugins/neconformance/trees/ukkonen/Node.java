package org.processmining.plugins.neconformance.trees.ukkonen;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class Node<T,S extends Iterable<T>> implements Iterable<Edge<T,S>> {
	private final Map<T, Edge<T,S>> edges = new HashMap<T, Edge<T,S>>();
	private final Edge<T,S> incomingEdge;
	private Set<SequenceTerminal<S>> sequenceTerminals = new HashSet<SequenceTerminal<S>>();
	private final Sequence<T,S> sequence;
	private final SuffixTree<T,S> tree;
	private Node<T,S> link = null;

	Node(Edge<T,S> incomingEdge, Sequence<T,S> sequence, SuffixTree<T,S> tree) {
		this.incomingEdge = incomingEdge;
		this.sequence = sequence;
		this.tree = tree;
	}

	@SuppressWarnings("unchecked")
	void insert(Suffix<T,S> suffix, ActivePoint<T,S> activePoint) {
		Object item = suffix.getEndItem();
		
		if (edges.containsKey(item)) {
			if (tree.isNotFirstInsert() && activePoint.getNode() != tree.getRoot())
				tree.setSuffixLink(activePoint.getNode());
			activePoint.setEdge(edges.get(item));
			activePoint.incrementLength();
		} else {
			saveSequenceTerminal(item);
			Edge<T,S> newEdge = new Edge<T,S>(suffix.getEndPosition()-1, this,
					sequence, tree);
			edges.put((T) suffix.getEndItem(), newEdge);
			suffix.decrement();
			activePoint.updateAfterInsert(suffix);
			
			if(tree.isNotFirstInsert() && !this.equals(tree.getRoot())){
				tree.getLastNodeInserted().setSuffixLink(this);
			}
			if (suffix.isEmpty())
				return;
			else
				tree.insert(suffix);
		}
	}

	private void saveSequenceTerminal(Object item) {
		if(item.getClass().equals(SequenceTerminal.class)){
			@SuppressWarnings("unchecked")
			SequenceTerminal<S> terminal = (SequenceTerminal<S>) item;
			sequenceTerminals.add(terminal);
		}
	}

	void insert(Edge<T,S> edge) {
		if (edges.containsKey(edge.getStartItem()))
			throw new IllegalArgumentException("Item " + edge.getStartItem()
					+ " already exists in node " + toString());
		edges.put(edge.getStartItem(), edge);
	}

	Edge<T,S> getEdgeStarting(Object item) {
		return edges.get(item);
	}

	boolean hasSuffixLink() {
		return link != null;
	}

	int getEdgeCount() {
		return edges.size();
	}

	public Iterator<Edge<T,S>> iterator() {
		return edges.values().iterator();
	}

	Node<T,S> getSuffixLink() {
		return link;
	}

	void setSuffixLink(Node<T,S> node) {
		link = node;
	}

	@Override
	public String toString() {
		if (incomingEdge == null)
			return "root";
		else {
			return "end of edge [" + incomingEdge.toString() + "]";
		}
	}

	public Collection<SequenceTerminal<S>> getSuffixTerminals() {
		return sequenceTerminals;
	}
	
	public Collection<Edge<T,S>> getEdges(){
		return edges.values();
	}
}
