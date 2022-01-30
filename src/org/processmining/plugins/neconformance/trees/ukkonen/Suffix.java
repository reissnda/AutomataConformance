package org.processmining.plugins.neconformance.trees.ukkonen;

class Suffix<T,S extends Iterable<T>> {
	private int start;
	private int end;
	private Sequence<T,S> sequence;

	public Suffix(int start, int end, Sequence<T,S> sequence) {
		testStartAndEndValues(start, end);
		testStartEndAgainstSequenceLength(start, end, sequence.getLength());
		this.start = start;
		this.end = end;
		this.sequence = sequence;
	}
	
	private void testStartEndAgainstSequenceLength(int start, int end, int sequenceLength){
		if(start > sequenceLength || end > sequenceLength)
			throw new IllegalArgumentException("Suffix start and end must be less than or equal to sequence length");
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[(");
		sb.append(start).append(", ").append(end).append(")");
		int end = getEndPosition();
		for (int i = start; i < end; i++) {
			sb.append(sequence.getItem(i)).append(",");
		}
		sb.append("]");
		return sb.toString();
	}

	int getEndPosition() {
		return end;
	}

	Object getEndItem() {
		if(isEmpty())
			return null;
		return sequence.getItem(end-1);
	}

	Object getStart() {
		if(isEmpty())
			return null;
		return sequence.getItem(start);
	}

	void decrement() {
		if(start==end)
			increment();
		start++;
	}

	void increment() {
		end++;
		if(end > sequence.getLength())
			throw new IndexOutOfBoundsException("Incremented suffix beyond end of sequence");
		
	}

	boolean isEmpty() {
		return start >= end || end > sequence.getLength();
	}

	int getRemaining() {
		if(isEmpty())
			return 0;
		else
			return (end - start);
	}

	public Object getItemXFromEnd(int distanceFromEnd) {
		if ((end - (distanceFromEnd)) < start){
			throw new IllegalArgumentException(distanceFromEnd
					+ " extends before the start of this suffix: ");
		}
		return sequence.getItem(end - distanceFromEnd);
	}
	
	void reset(int start, int end){
		testStartAndEndValues(start, end);
		this.start = start;
		this.end = end;
	}
	
	private void testStartAndEndValues(int start, int end){
		if(start < 0 || end < 0)
			throw new IllegalArgumentException("You cannot set a suffix start or end to less than zero.");
		if(end < start)
			throw new IllegalArgumentException("A suffix end position cannot be less than its start position.");
	}
}
