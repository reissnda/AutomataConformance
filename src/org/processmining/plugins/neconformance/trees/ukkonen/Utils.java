package org.processmining.plugins.neconformance.trees.ukkonen;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Utils {
	
	static <I,S extends Iterable<I>> Object[] addTerminalToSequence(S sequence,
			SequenceTerminal<S> terminatingObject) {
		
		ArrayList<Object> list = new ArrayList<Object>();
		for(I item : sequence)
			list.add(item);
		
		Object[] newSequence = new Object[list.size() + 1];
		
		int i = 0;
		for (; i < list.size(); i++)
			newSequence[i] = list.get(i);
		newSequence[i] = terminatingObject;
		return newSequence;
	}

	static <T,S extends Iterable<T>> String printTreeForGraphViz(SuffixTree<T,S> tree) {
		return printTreeForGraphViz(tree, true);
	}
	
	static <T,S extends Iterable<T>> String printTreeForGraphViz(SuffixTree<T,S> tree, boolean printSuffixLinks) {
		LinkedList<Node<T,S>> stack = new LinkedList<Node<T,S>>();
		stack.add(tree.getRoot());
		Map<Node<T,S>, Integer> nodeMap = new HashMap<Node<T,S>, Integer>();
		nodeMap.put(tree.getRoot(), 0);
		int nodeId = 1;

		String sb = "\ndigraph suffixTree{\n node [shape=circle, label=\"\", fixedsize=true, width=0.1, height=0.1]\n";

		while (stack.size() > 0) {
			LinkedList<Node<T,S>> childNodes = new LinkedList<Node<T,S>>();
			for (Node<T,S> node : stack) {

				// List<Edge> edges = node.getEdges();
				for (Edge<T,S> edge : node) {
					int id = nodeId++;
					if (edge.isTerminating()) {
						childNodes.push(edge.getTerminal());
						nodeMap.put(edge.getTerminal(), id);
					}

					sb += (nodeMap.get(node))+" -> "+(id)+" [label=\"";
							
					for (T item : edge) {
						//if(item != null)
							sb += item.toString();
					}
					sb += ("\"];\n");
				}
			}
			stack = childNodes;
		}
		if(printSuffixLinks){
			// loop again to find all suffix links.
			sb += ("edge [color=red]\n");
			for (Map.Entry<Node<T,S>, Integer> entry : nodeMap.entrySet()) {
				Node<T, S> n1 = entry.getKey();
				int id1 = entry.getValue();
	
				if (n1.hasSuffixLink()) {
					Node<T, S> n2 = n1.getSuffixLink();
					Integer id2 = nodeMap.get(n2);
					// if(id2 != null)
					sb += (id1)+" -> "+(id2)+" ;\n";
				}
			}
		}
		sb += ("}");
		return sb;
	}
}
