package org.processmining.plugins.neconformance.trees.ukkonen;

import java.util.Iterator;

class Edge<T, S extends Iterable<T>> implements Iterable<T> {
	private final int start;
	private int end = -1;
	private final Sequence<T,S> sequence;

	private Node<T, S> terminal = null;
	private SuffixTree<T,S> tree = null;
	
	Edge(int start, Node<T,S> parent, Sequence<T,S> sequence, SuffixTree<T,S> tree) {
		this.start = start;
		this.sequence = sequence;
		this.tree = tree;
	}

	boolean isStarting(Object item) {
		return sequence.getItem(start).equals(item);
	}

	void insert(Suffix<T,S> suffix, ActivePoint<T,S> activePoint) {
		Object item = suffix.getEndItem();
		Object nextItem = getItemAt(activePoint.getLength());
		if (item.equals(nextItem)) {
			activePoint.incrementLength();
		} else {
			split(suffix, activePoint);
			suffix.decrement();
			activePoint.updateAfterInsert(suffix);

			if (suffix.isEmpty())
				return;
			else
				tree.insert(suffix);
		}
	}

	private void split(Suffix<T,S> suffix, ActivePoint<T,S> activePoint) {
		Node<T,S> breakNode = new Node<T,S>(this, sequence, tree);
		Edge<T,S> newEdge = new Edge<T,S>(suffix.getEndPosition()-1, breakNode,
				sequence, tree);
		breakNode.insert(newEdge);
		Edge<T,S> oldEdge = new Edge<T,S>(start + activePoint.getLength(),
				breakNode, sequence, tree);
		oldEdge.end = end;
		oldEdge.terminal = this.terminal;
		breakNode.insert(oldEdge);
		this.terminal = breakNode;
		end = start + activePoint.getLength();
		tree.setSuffixLink(breakNode);
		tree.incrementInsertCount();
	}

	int getEnd() {
		tree.getCurrentEnd();
		return end != -1 ? end : tree.getCurrentEnd();
	}

	boolean isTerminating() {
		return terminal != null;
	}

	int getLength() {
		int realEnd = getEnd();
		return realEnd - start;
	}

	Node<T,S> getTerminal() {
		return terminal;
	}

	@SuppressWarnings("unchecked")
	T getItemAt(int position) {
		if (position > getLength())
			throw new IllegalArgumentException("Index " + position
					+ " is greater than " + getLength()
					+ " - the length of this edge.");
		return (T) sequence.getItem(start + position);
	}

	@SuppressWarnings("unchecked")
	T getStartItem() {
		return (T) sequence.getItem(start);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < getEnd(); i++) {
			sb.append(sequence.getItem(i).toString()).append(", ");
			if(sequence.getItem(i).getClass().equals(SequenceTerminal.class))
				break;
		}
		return sb.toString();
	}

	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private int currentPosition = start;
			private boolean hasNext = true;
			
			public boolean hasNext() {
				return hasNext;
			}

			@SuppressWarnings("unchecked")
			public T next() {
				if(end == -1)
					hasNext = !sequence.getItem(currentPosition).getClass().equals(SequenceTerminal.class);
				else
					hasNext = currentPosition < getEnd()-1;
				return (T) sequence.getItem(currentPosition++);
			}

			public void remove() {
				throw new UnsupportedOperationException(
						"The remove method is not supported.");
			}
		};
	}
}
