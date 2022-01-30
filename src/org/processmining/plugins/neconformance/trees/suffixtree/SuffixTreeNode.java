package org.processmining.plugins.neconformance.trees.suffixtree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SuffixTreeNode {
	private int id;
	private Map<String, Set<SuffixTreeNode>> edges;
	
	public SuffixTreeNode(int id) {
		this.id = id;
		this.edges = new HashMap<String, Set<SuffixTreeNode>>();
	}
	
	public int getId() {
		return id;
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SuffixTreeNode other = (SuffixTreeNode) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public String toString() {
		return "SuffixTreeNode [id=" + id + "]";
	}

	public void addEdge(String transitionName, SuffixTreeNode node) {
		if (!edges.containsKey(transitionName))
			edges.put(transitionName, new HashSet<SuffixTreeNode>());
		edges.get(transitionName).add(node);
	}
	
	public int nrNodesOut() {
		return edges.size();
	}
	
	public Set<SuffixTreeNode> getNodesOut(String transitionName) {
		if (!edges.containsKey(transitionName))
			return null;
		return edges.get(transitionName);
	}

}
