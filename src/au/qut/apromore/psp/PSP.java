package au.qut.apromore.psp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import au.qut.apromore.automaton.Automaton;

/*
 * Copyright © 2009-2017 The Apromore Initiative.
 *
 * This file is part of "Apromore".
 *
 * "Apromore" is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * "Apromore" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */

/**
 * @author Daniel Reissner,
 * @version 1.0, 01.02.2017
 */

public class PSP 
{
	private BiMap<Integer, Node> nodes;
	private Set<Arc> arcs;
	private int sourceNode;
	private Set<Node> finalNodes;
	private BiMap<IntArrayList, Set<Node>> commutativePaths;
	private Automaton logAutomaton;
	private Automaton modelAutomaton;
	//Alternative for S-Components
	private List<Automaton> sComponentAutomata;
	
	public PSP(Automaton logAutomaton, Automaton modelAutomaton)
	{
		this.nodes = HashBiMap.create();
		this.arcs = new UnifiedSet<>();
		Node source = new Node(logAutomaton.source(),modelAutomaton.source(),
				new Configuration(new IntArrayList(), new IntIntHashMap(), new IntArrayList(), new IntIntHashMap(), new FastList<Synchronization>(),
						new IntArrayList(), new IntArrayList()), 0);
		source.configuration().logIDs().add(logAutomaton.sourceID()); source.configuration().modelIDs().add(modelAutomaton.sourceID());
		this.sourceNode = source.hashCode();
		this.nodes.put(sourceNode,source);
		this.finalNodes = new UnifiedSet<>();
		this.logAutomaton = logAutomaton;
		this.modelAutomaton = modelAutomaton;
	}

	public PSP(BiMap<Integer, Node> nodes, Set<Arc> arcs, int sourceNode, Automaton logAutomaton, Automaton modelAutomaton)
	{
		this.nodes = nodes;
		this.arcs = arcs;
		this.sourceNode = sourceNode;
		this.finalNodes = new UnifiedSet<Node>();
		this.logAutomaton = logAutomaton;
		this.modelAutomaton = modelAutomaton;
	}

	public PSP(BiMap<Integer, Node> nodes, Set<Arc> arcs, int sourceNode, Automaton logAutomaton, List<Automaton> sComponentAutomata)
	{
		this.nodes = nodes;
		this.arcs = arcs;
		this.sourceNode = sourceNode;
		this.finalNodes = new UnifiedSet<Node>();
		this.logAutomaton = logAutomaton;
		this.modelAutomaton = modelAutomaton;
	}

	public BiMap<Integer, Node> nodes()
	{
		return this.nodes;
	}
	
	public Set<Arc> arcs()
	{
		return this.arcs;
	}
	
	public Node sourceNode()
	{
		return this.nodes().get(this.sourceNode);
	}
	
	public Set<Node> finalNodes()
	{
		return this.finalNodes;
	}
	
	public Automaton logAutomaton()
	{
		return this.logAutomaton;
	}
	
	public Automaton modelAutomaton()
	{
		return this.modelAutomaton;
	}

	public List<Automaton> sComponentAutomata(){ return this.sComponentAutomata; }
	
	public BiMap<IntArrayList,Set<Node>> commutativePaths()
	{
		if(this.commutativePaths==null)
			this.commutativePaths = HashBiMap.create();
		return this.commutativePaths;
	}
	
	public String arcLabel(Arc arc)
	{
		if(arc.transition().operation() == Configuration.Operation.RHIDE)
			return "< " + arc.transition().operation() + " : " + this.modelAutomaton().eventLabels().get(arc.transition().eventModel()) + " >";
		return "< " + arc.transition().operation() + " : " + this.logAutomaton().eventLabels().get(arc.transition().eventLog()) + " >";
	}
	
	public String configurationLabel(Configuration conf)
	{
		String label = "";
		for(Synchronization tr : conf.sequenceSynchronizations())
		{
			if(tr.operation() == Configuration.Operation.RHIDE)
				label = label + "< " + tr.operation() + " : " + this.modelAutomaton().eventLabels().get(tr.eventModel()) + " >";
			else
				label = label + "< " + tr.operation() + " : " + this.logAutomaton().eventLabels().get(tr.eventLog()) + " >";
			label = label + ", ";
		}
		return label.substring(0, label.length()-2);
	}
	
	public String nodeLabel(int nodeID)
	{
		String nodeLabel = "Matches = { ";
		//for(Couple<Integer, Integer> events : this.nodes().get(nodeID).configuration().moveMatching())
		//	nodeLabel = nodeLabel + this.logAutomaton().eventLabels().get(events.getFirstElement()) + "; ";
		if(nodeLabel.substring(nodeLabel.length()-2, nodeLabel.length()).equals("{ "))
			nodeLabel = nodeLabel + "\u2205 }<br/>Moves on Log = { ";
		else
			nodeLabel = nodeLabel.substring(0, nodeLabel.length() - 2) + " }<br/>Moves on Log = { ";
		for(int event : this.nodes().get(nodeID).configuration().moveOnLog().toArray())
			nodeLabel = nodeLabel + this.logAutomaton().eventLabels().get(event) + "; ";
		if(nodeLabel.substring(nodeLabel.length()-2, nodeLabel.length()).equals("{ "))
			nodeLabel = nodeLabel + "\u2205 }<br/>Moves on Model = { ";
		else
			nodeLabel = nodeLabel.substring(0, nodeLabel.length() - 2) + " }<br/>Moves on Model = { ";
		for(int event : this.nodes().get(nodeID).configuration().moveOnModel().toArray())
			nodeLabel = nodeLabel + this.modelAutomaton().eventLabels().get(event) + "; ";
		if(nodeLabel.substring(nodeLabel.length()-2, nodeLabel.length()).equals("{ "))
			nodeLabel = nodeLabel + "\u2205 }";
		else
			nodeLabel = nodeLabel.substring(0, nodeLabel.length() - 2) + " }";
		nodeLabel = nodeLabel + "<br/>Node weight: " + this.nodes().get(nodeID).weight();
		return nodeLabel;
	}
	
	public String nodeLabel(Node node)
	{
		String nodeLabel = "Matches = { ";
		//for(Couple<Integer, Integer> events : node.configuration().moveMatching())
		//	nodeLabel = nodeLabel + this.logAutomaton().eventLabels().get(events.getFirstElement()) + "; ";
		if(nodeLabel.substring(nodeLabel.length()-2, nodeLabel.length()).equals("{ "))
			nodeLabel = nodeLabel + "\u2205 }<br/>Moves on Log = { ";
		else
			nodeLabel = nodeLabel.substring(0, nodeLabel.length() - 2) + " }<br/>Moves on Log = { ";
		for(int event : node.configuration().moveOnLog().toArray())
			nodeLabel = nodeLabel + this.logAutomaton().eventLabels().get(event) + "; ";
		if(nodeLabel.substring(nodeLabel.length()-2, nodeLabel.length()).equals("{ "))
			nodeLabel = nodeLabel + "\u2205 }<br/>Moves on Model = { ";
		else
			nodeLabel = nodeLabel.substring(0, nodeLabel.length() - 2) + " }<br/>Moves on Model = { ";
		for(int event : node.configuration().moveOnModel().toArray())
			nodeLabel = nodeLabel + this.modelAutomaton().eventLabels().get(event) + "; ";
		if(nodeLabel.substring(nodeLabel.length()-2, nodeLabel.length()).equals("{ "))
			nodeLabel = nodeLabel + "\u2205 }";
		else
			nodeLabel = nodeLabel.substring(0, nodeLabel.length() - 2) + " }";
		//nodeLabel = nodeLabel + "<br/>Node weight: " + node.weight();
		return nodeLabel;
	}
	
	public void toDot(PrintWriter pw) throws IOException {
		pw.println("digraph fsm {");
		pw.println("rankdir=LR;");
		pw.println("node [shape=box,style=filled, fillcolor=white]");
		
		for(Node node : this.nodes().values()) {
			if(node.equals(this.sourceNode())) {
				pw.printf("%d [label=<%s>, fillcolor=\"gray\"];%n", node.hashCode(), this.nodeLabel(node));
			} else {
				pw.printf("%d [label=<%s>];%n", node.hashCode(), this.nodeLabel(node));
			}
			
			for(Arc arc : node.outgoingArcs()) {
				pw.printf("%d -> %d [label=\"%s\"];%n", node.hashCode(), arc.target().hashCode(), this.arcLabel(arc));
			}

			if(node.isFinal()) {
				pw.printf("%d [label=<%s>, style=\"bold\"];%n", node.hashCode(), this.nodeLabel(node));
			}
		}
		pw.println("}");
	}
	
	public void toDot(String fileName) throws IOException {
		PrintWriter pw = new PrintWriter(fileName);
		toDot(pw);
		pw.close();
	}
}
