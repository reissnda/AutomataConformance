package org.processmining.plugins.neconformance.trees.suffixtree;

public class SuffixTreeEdge {
	private SuffixTreeNode from;
	private SuffixTreeNode to;
	private String transitionName;
	
	public SuffixTreeEdge(SuffixTreeNode from, SuffixTreeNode to, String transitionName) {
		this.from = from;
		this.to = to;
		this.transitionName = transitionName;
	}

	public SuffixTreeNode getFrom() {
		return from;
	}

	public SuffixTreeNode getTo() {
		return to;
	}
	
	public String getTransitionName() {
		return transitionName;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		result = prime * result
				+ ((transitionName == null) ? 0 : transitionName.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SuffixTreeEdge other = (SuffixTreeEdge) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		if (transitionName == null) {
			if (other.transitionName != null)
				return false;
		} else if (!transitionName.equals(other.transitionName))
			return false;
		return true;
	}

	public String toString() {
		return "--(" + transitionName + ")->";
	}
	
}
