package org.processmining.plugins.neconformance.trees.ukkonen;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class Cursor<T,S extends Iterable<T>> {

	private final SuffixTree<T,S> tree;
	private Node<T,S> node;
	private Edge<T,S> edge;
	private int length;
	
	Cursor(SuffixTree<T,S> tree){
		this.tree = tree;
		node = tree.getRoot();
		edge = null;
		length = 0;
	}
	
	boolean hasSingleContinuation(){
		if (edge == null) {
			return node.getEdgeCount() == 1;
		} else if (edge.getLength() > length) {
			return true;
		} else {
			Node<T, S> terminal = edge.getTerminal();
			if (terminal == null) {
				return false;
			} else {
				return terminal.getEdgeCount() == 1;
			}
		}
	}
	
	boolean proceedTo(T item){
		if(edge == null){
			Edge<T,S> tmpEdge = node.getEdgeStarting(item);
			if(tmpEdge != null){
				edge = tmpEdge;
				length = 1;
				return true;
			}else{
				return false;
			}
		}else if(edge.getLength() > length){
			T nextItem = edge.getItemAt(length);
			if(nextItem != null && item.equals(nextItem)){
				length++;
				return true;
			}else{
				return false;
			}
		}else{
			Node<T,S> terminal = edge.getTerminal();
			if(terminal == null)
				return false;
			else{
				Edge<T,S> tmpEdge = terminal.getEdgeStarting(item);
				if(tmpEdge != null){
					edge = tmpEdge;
					length = 1;
					node = terminal;
					return true;
				}else{
					return false;
				}
			}
		}
	}
	
	Collection<SequenceTerminal<S>> getSequenceTerminals(){
		if(edge == null){
			return node.getSuffixTerminals();
		}else{
			if((edge.getLength()-1 == length && !edge.isTerminating()) 
					|| (edge.getItemAt(length).getClass().equals(SequenceTerminal.class)) && !edge.isTerminating()){
				Object seqTerminal = edge.getItemAt(length);
				@SuppressWarnings("unchecked")
				SequenceTerminal<S> term = (SequenceTerminal<S>)seqTerminal;
				Collection<SequenceTerminal<S>> collection = new HashSet<SequenceTerminal<S>>();
				collection.add(term);
				return collection;
			}else{
				Node<T,S> terminal = edge.getTerminal();
				if(terminal == null)
					return Collections.emptySet();
				else{
					Collection<Edge<T,S>> edges = terminal.getEdges();
					Collection<SequenceTerminal<S>> returnCollection = new HashSet<SequenceTerminal<S>>();
					for(Edge<T,S> edge : edges){
						Object o = edge.getStartItem();
						if(o.getClass().equals(SequenceTerminal.class)){
							@SuppressWarnings("unchecked")
							SequenceTerminal<S> returnTerminal = (SequenceTerminal<S>)o;
							returnCollection.add(returnTerminal);
						}
					}
					return returnCollection;
				}
			}
		}
	}
	
	void returnToRoot(){
		node = tree.getRoot();
		edge = null;
		length = 0;
	}
	
}
