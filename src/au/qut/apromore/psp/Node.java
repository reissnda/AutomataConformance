package au.qut.apromore.psp;

import java.util.List;
import java.util.Set;

import au.qut.apromore.automaton.Automaton;
import au.qut.apromore.automaton.Transition;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.IntBooleanHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import au.qut.apromore.automaton.State;

import static nl.tue.storage.hashing.impl.MurMur3HashCodeProvider.C1;
import static nl.tue.storage.hashing.impl.MurMur3HashCodeProvider.C2;

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

public class Node {

	private State stLog;
	private State stModel;
	private IntObjectHashMap<State> stSComps;
	private int stateLogID;
	private int stateModelID;
	private int stateModelrep;
	private Configuration configuration;
	private Set<Arc> outgoingArcs;
	private double weight;
	private Integer queueing_score;
	private boolean isFinal;
	private Integer hashCode = null;
	public int tracePosition=0;
	private IntBooleanHashMap tracePenalties;
	
	public Node(State stLog, State stModel, Configuration configuration, double weight)
	{
		this.stLog = stLog;
		this.stModel = stModel;
		this.stateLogID = stLog.id();
		this.stateModelID = stModel.id();
		this.configuration = configuration;
		//this.stateModelrep = configuration.modelIDs().count(x->x==this.stateModelID);
		//if(this.stateModelrep>2) this.stateModelrep=2;
		//if(this.stateModelrep >= 2)
		//	this.stateModelrep=this.stateModelrep +1 -1;
		this.weight = weight;
		this.isFinal = false;
	}

	public Node(State stLog, State stModel, Configuration configuration, int tracePosition , double weight)
	{
		this.stLog = stLog;
		this.stModel = stModel;
		this.stateLogID = stLog.id();
		this.stateModelID = stModel.id();
		this.configuration = configuration;
		//this.stateModelrep = configuration.modelIDs().count(x->x==this.stateModelID);
		//if(this.stateModelrep >= 2)
		//	this.stateModelrep=this.stateModelrep +1 -1;
		this.tracePosition=tracePosition;
		this.weight = weight;
		this.isFinal = false;
	}

	public Node(State stLog, State stModel, Configuration configuration, int tracePosition , double weight, int posPenalty, boolean penalty)
	{
		this.stLog = stLog;
		this.stModel = stModel;
		this.stateLogID = stLog.id();
		this.stateModelID = stModel.id();
		this.configuration = configuration;
		//this.stateModelrep = configuration.modelIDs().count(x->x==this.stateModelID);
		//if(this.stateModelrep >= 2)
		//	this.stateModelrep=this.stateModelrep +1 -1;
		this.tracePosition=tracePosition;
		this.weight = weight;
		this.isFinal = false;
		this.getTracePenalties().put(posPenalty,penalty);
	}
	
	public Node(int stateLogID, int stateModelID, Configuration configuration, double weight)
	{
		this.stateLogID = stateLogID;
		this.stateModelID = stateModelID;
		this.configuration = configuration;
		//this.stateModelrep = configuration.modelIDs().count(x->x==this.stateModelID);
		this.weight = weight;
		this.isFinal = false;
	}

	public Node(State stLog, IntObjectHashMap<State> stSComps, Configuration configuration, double weight)
    {
        this.stLog = stLog;
        this.stSComps = stSComps;
        this.configuration = configuration;
		//this.stateModelrep = configuration.modelIDs().count(x->x==this.stateModelID);
        this.weight = weight;
        this.isFinal = false;
    }

    public Node(Node previousNode, Transition trLog, Transition trModel)
	{
		int eventLog = Synchronization.perp, eventModel = Synchronization.perp, tracePosition = previousNode.tracePosition;
		if(trLog==null)
			this.stLog = previousNode.stLog;
		else
		{
			this.stLog = trLog.target();
			eventLog = trLog.eventID();
			tracePosition++;
		}
		if(trModel==null)
			this.stModel = previousNode.stModel;
		else
		{
			this.stModel = trModel.target();
			eventModel = trModel.eventID();
		}
		this.tracePosition = tracePosition;
		Synchronization sync = new Synchronization(eventLog, eventModel);
		FastList<Synchronization> alignment = new FastList<>();
		alignment.addAll(previousNode.configuration.sequenceSynchronizations());
		alignment.add(sync);
		configuration = new Configuration(alignment);
		setStandardCost();
	}

	private void setStandardCost()
	{
		double currentCost = 0;
		for(Synchronization sync : configuration.sequenceSynchronizations())
		{
			if((sync.operation()==Configuration.Operation.RHIDE || sync.operation()== Configuration.Operation.LHIDE) && sync.eventModel() != Automaton.skipEvent)
			{
				currentCost +=1;
			}
		}
		this.weight = currentCost;
		//IntArrayList moveOnModelWithoutTau = new IntArrayList();
		//moveOnModelWithoutTau.addAll(configuration.moveOnModel());
		//moveOnModelWithoutTau.removeAll(modelAutomaton.skipEvent());
		//return configuration.moveOnLog().size() + moveOnModelWithoutTau.size() - 2 * configuration.moveMatching().size();// + finalConfigurationViolations.size();
	}
	
	public State stLog()
	{
		return this.stLog;
	}
	
	public State stModel()
	{
		return this.stModel;
	}
	
	public int stateLogID()
	{
		return this.stateLogID;
	}

	public int stateModelID() { return this.stateModelID; }

	public IntObjectHashMap<State> stSComps() { return this.stSComps; }

	public Configuration configuration()
	{
		return this.configuration;
	}
	
	public Set<Arc> outgoingArcs()
	{
		if(this.outgoingArcs==null)
			this.outgoingArcs = new UnifiedSet<Arc>();
		return this.outgoingArcs;
	}
	
	public double weight()
	{
		return this.weight;
	}
	
	public boolean isFinal()
	{
		return this.isFinal;
	}
	
	public void isFinal(boolean isFinal)
	{
		this.isFinal = isFinal;
	}

	public Integer getQueueing_score() {
		if(queueing_score==null)
		{
			int match=0, r=0, l=0;
			for(Synchronization sync : this.configuration().sequenceSynchronizations())
			{
				if(sync.operation()==Configuration.Operation.MATCH) match++;
				else if(sync.operation()==Configuration.Operation.RHIDE) r++;
				else if(sync.operation()==Configuration.Operation.LHIDE) l++;
			}
			this.queueing_score = (int) (this.weight() * 100000 + match * 1000 + r * 10 + l);
		}
		return queueing_score;
	}

	public IntBooleanHashMap getTracePenalties()
	{
		if(this.tracePenalties==null)
			tracePenalties = new IntBooleanHashMap();
		return tracePenalties;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;
//        if(this.stateLogID()!=node.stateLogID())
//        	return false;
//        if(this.stateModelID()!=node.stateModelID())
//        	return false;
//        if(this.configuration().equals(node.configuration()))
//        	return true;
//        return false;
        return this.hashCode == ((Node) o).hashCode();
		//return new EqualsBuilder()
        //		.append(this.stateLogID(), node.stateLogID())
        //		//.append(this.stateModelID(), node.stateModelID())
        //		.append(this.configuration(), node.configuration())
        //		//.append(this.weight(), node.weight())
        //		//.append(this.isFinal(), node.isFinal())
        //		.isEquals();
    }

    @Override
    public int hashCode() {
    	if(hashCode == null) {
			hashCode = compress();
		}
//    		hashCode = (int) (31*this.stateLogID() + Math.pow(31, 2)*this.stateModelID() + this.configuration().hashCode());
    		//hashCode = new HashCodeBuilder(17,37)
            //		.append(this.stateLogID())
            		//.append(this.stateModelID())
            //		.append(this.configuration())
            		//.append(this.weight())
            		//.append(this.isFinal())
            //		.toHashCode();
    	return hashCode;
//        return new HashCodeBuilder(17,37)
//        		.append(this.stateLogID())
//        		.append(this.stateModelID())
//        		.append(this.configuration())
//        		//.append(this.weight())
//        		//.append(this.isFinal())
//        		.toHashCode();
    }

	/** Returns the MurmurHash3_x86_32 hash. */
	protected int compress() {

		int[] data = new int[3];
		data[0] = this.stateLogID;
		data[1] = this.stateModelID;
		//data[2] = this.stateModelrep;
		int hash =0;
		final int len = data.length;
		if(len==0) return -1;
		int k1;
		for (int i = 0; i < len - 1; i++) {
			// little endian load order
			k1 = data[i];
			k1 *= C1;

			k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
			k1 *= C2;

			hash ^= k1;

			hash = (hash << 13) | (hash >>> 19); // ROTL32(h1,13);
			hash = hash * 5 + 0xe6546b64;

		}

		// tail
		k1 = data[len - 1];
		k1 *= C1;
		k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
		k1 *= C2;
		hash ^= k1;

		// finalization
		hash ^= len;

		// fmix(h1);
		hash ^= hash >>> 16;
		hash *= 0x85ebca6b;
		hash ^= hash >>> 13;
		hash *= 0xc2b2ae35;
		hash ^= hash >>> 16;

		return hash;
	}
}
