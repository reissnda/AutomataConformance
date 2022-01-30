package org.processmining.plugins.neconformance.trees.ukkonen;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Sequence<I, S extends Iterable<I>> implements Iterable<Object> {

	private List<Object> masterSequence = new ArrayList<Object>();
	
	Sequence(){
	}

	Sequence(S sequence) {
		for(Object item : sequence)
			masterSequence.add(item);
		SequenceTerminal<S> sequenceTerminal = new SequenceTerminal<S>(sequence);
		masterSequence.add(sequenceTerminal);
	}

	Object getItem(int index) {
		return masterSequence.get(index);
	}

	void add(S sequence){
		for(I item : sequence){
			masterSequence.add(item);
		}
		SequenceTerminal<S> terminal = new SequenceTerminal<S>(sequence);
		masterSequence.add(terminal);
	}

	public Iterator<Object> iterator() {
		return new Iterator<Object>() {

			int currentPosition = 0;

			public boolean hasNext() {
				return masterSequence.size() > currentPosition;
			}

			public Object next() {
				if (currentPosition <= masterSequence.size())
					return masterSequence.get(currentPosition++);
				else {
					return null;
				}
			}

			public void remove() {
				throw new UnsupportedOperationException(
						"Remove is not supported.");

			}

		};
	}
	
	int getLength(){
		return masterSequence.size();
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder("Sequence = [");
		for(Object i : masterSequence){
			sb.append(i).append(", ");
		}
		sb.append("]");
		return sb.toString();
	}
	
}
