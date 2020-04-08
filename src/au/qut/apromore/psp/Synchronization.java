package au.qut.apromore.psp;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import sun.security.krb5.Config;

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

public class Synchronization {
	
	private Configuration.Operation operation;
	private int eventLog = -1;
	private int eventModel = -1;
	private int targetLog;
	private int targetModel;
	private boolean explained = false;
	private boolean transitivelyExplained = false;
	private int sourceNode = -1;
	private Integer hashCode = null;
	public static final int perp = -1;

	public Synchronization(int eventLog, int eventModel)
	{
		this.eventLog = eventLog;
		this.eventModel = eventModel;
		if(eventLog==perp)
			operation= Configuration.Operation.RHIDE;
		else if(eventModel==perp)
			operation = Configuration.Operation.LHIDE;
		else operation = Configuration.Operation.MATCH;
	}

	public Synchronization(Configuration.Operation operation, int eventLog, int eventModel)
	{
		this.operation = operation;
		if(operation==Configuration.Operation.MATCH)
		{
			this.eventLog = eventLog;
			this.eventModel = eventModel;
		}
		else if(operation==Configuration.Operation.LHIDE)
			this.eventLog = eventLog;
		else
			this.eventModel = eventModel;
	}
	
	public Synchronization(Configuration.Operation operation, int eventLog, int eventModel, int targetLog, int targetModel, int sourceNode)
	{
		this.operation = operation;
		this.sourceNode = sourceNode;
		
		if(operation==Configuration.Operation.MATCH)
		{
			this.eventLog = eventLog;
			this.eventModel = eventModel;
			this.targetLog = targetLog;
			this.targetModel = targetModel;
		}
		else if(operation==Configuration.Operation.LHIDE)
		{
			this.eventLog = eventLog;
			this.targetLog = targetLog;
		}
		else
		{
			this.eventModel = eventModel;
			this.targetModel = targetModel;
		}
	}
	
	public Configuration.Operation operation()
	{
		return this.operation;
	}

	public String label()
	{
		String label = "(" + this.operation;
		if(operation != Configuration.Operation.RHIDE)
		{
			label += ", " + eventLog +")";
		}
		else
			label += ", " + eventModel +")";
		return label;
	}

	public int eventLog()
	{
		if(this.operation!=Configuration.Operation.RHIDE)
			return this.eventLog;
		return -1;
	}
	
	public int eventModel()
	{
		if(this.operation!=Configuration.Operation.LHIDE)
			return this.eventModel;
		return -1;
	}
	
	public int targetLog()
	{
		if(this.operation!=Configuration.Operation.RHIDE)
			return this.targetLog;
		return -1;
	}
	
	public int targetModel()
	{
		if(this.operation!=Configuration.Operation.LHIDE)
			return this.targetModel;
		return -1;
	}
	
	public boolean isExplained()
	{
		return this.explained;
	}
	
	public void setExplainedTo(boolean explained)
	{
		this.explained = explained;
	}
	
	public boolean isTransitivelyExplained()
	{
		return this.transitivelyExplained;
	}
	
	public void setTransitivelyExplainedTo(boolean transitivelyExplained)
	{
		this.transitivelyExplained = transitivelyExplained;
	}
	
	public int sourceNode()
	{
		return this.sourceNode;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Synchronization transition = (Synchronization) o;
        
        if(this.operation() != transition.operation()) return false;
        if(this.eventLog() != transition.eventLog()) return false;
        if(this.eventModel() != transition.eventModel()) return false;
        return true;
        
//    	return new EqualsBuilder()
//		.append(this.operation(), transition.operation())
//		.append(this.eventLog(), transition.eventLog())
//		.append(this.eventModel(), transition.eventModel())
//		.isEquals();
		
//        if(this.operation()==Configuration.Operation.MATCH)
//        	return new EqualsBuilder()
//        		.append(this.operation(), transition.operation())
//        		.append(this.eventLog(), transition.eventLog())
//        		.append(this.eventModel(), transition.eventModel())
//        		.isEquals();
//        else if(this.operation()==Configuration.Operation.LHIDE)
//        	return new EqualsBuilder()
//            		.append(this.operation(), transition.operation())
//            		.append(this.eventLog(), transition.eventLog())
//            		.isEquals();
//        else
//        	return new EqualsBuilder()
//            		.append(this.operation(), transition.operation())
//            		.append(this.eventModel(), transition.eventModel())
//            		.isEquals();
    }

    @Override
    public int hashCode() {
    	if(hashCode == null) {
//    		hashCode = (int) (operation().hashCode() + 31 * this.eventLog() + Math.pow(31, 2) * this.eventModel());
    		//hashCode = new HashCodeBuilder(17,37)
            //		.append(this.operation())
            //		.append(this.eventLog())
            //		.append(this.eventModel())
            //		.toHashCode();
			hashCode = compress();
    	}
    	return hashCode;
//    	return new HashCodeBuilder(17,37)
//        		.append(this.operation())
//        		.append(this.eventLog())
//        		.append(this.eventModel())
//        		.toHashCode();
    	
//    	if(this.operation()==Configuration.Operation.MATCH)
//        	return new HashCodeBuilder(17,37)
//        		.append(this.operation())
//        		.append(this.eventLog())
//        		.append(this.eventModel())
//        		.toHashCode();
//        else if(this.operation()==Configuration.Operation.LHIDE)
//        	return new HashCodeBuilder(17,37)
//            		.append(this.operation())
//            		.append(this.eventLog())
//            		.toHashCode();
//        else
//        	return new HashCodeBuilder(17,37)
//            		.append(this.operation())
//            		.append(this.eventModel())
//            		.toHashCode();
    }

	/** Returns the MurmurHash3_x86_32 hash. */
	protected int compress() {

		int[] data = new int[3];
		data[0] = operation.ordinal();
		data[1] = eventLog;
		data[2] = eventModel;

		//data[2] = this.stateModelrep;
		int hash =0;
		final int len = data.length;

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
