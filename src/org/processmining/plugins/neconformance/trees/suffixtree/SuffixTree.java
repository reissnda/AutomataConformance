package org.processmining.plugins.neconformance.trees.suffixtree;

import java.util.Collection;
import java.util.HashSet;
import edu.uci.ics.jung.graph.DelegateTree;

public class SuffixTree {
	private DelegateTree<SuffixTreeNode, SuffixTreeEdge> tree;
	
	public SuffixTree() {
		tree = new DelegateTree<SuffixTreeNode, SuffixTreeEdge>();

		SuffixTreeNode ROOT_NODE = new SuffixTreeNode(-1);
		tree.addVertex(ROOT_NODE);
	}
	
	public SuffixTreeNode addNode(SuffixTreeNode from, int stateCounter, String transitionName) {
		SuffixTreeNode node = new SuffixTreeNode(stateCounter);
		SuffixTreeEdge edge = new SuffixTreeEdge(from, node, transitionName);
		if (!tree.containsVertex(node)) {
			tree.addChild(edge, from, node);
			from.addEdge(transitionName, node);
		}
		return node;
	}
	
	public SuffixTreeNode getNodeOut(SuffixTreeNode from, String transitionName) {
		Collection<SuffixTreeNode> n = getNodesOut(from, transitionName);
		if (n == null || n.size() == 0)
			return null;
		return n.iterator().next();
	}
	
	public Collection<SuffixTreeNode> getNodesOut(SuffixTreeNode from, String transitionName) {
		Collection<SuffixTreeNode> n = from.getNodesOut(transitionName);
		if (n == null)
			return new HashSet<SuffixTreeNode>();
		return n;
	}
	
	public int nrNodesOut(SuffixTreeNode from) {
		return from.nrNodesOut();
	}
	
	public SuffixTreeEdge getEdge(SuffixTreeNode from, SuffixTreeNode to) {
		return tree.findEdge(from, to);
	}
	
	public Collection<SuffixTreeEdge> getInEdges(SuffixTreeNode to) {
		return tree.getInEdges(to);
	}
	
	public Collection<SuffixTreeEdge> getOutEdges(SuffixTreeNode from) {
		return tree.getOutEdges(from);
	}
	
	public SuffixTreeNode getRootNode() {
		return tree.getRoot();
	}

	public DelegateTree<SuffixTreeNode, SuffixTreeEdge> getTree() {
		return tree;
	}
	
}
