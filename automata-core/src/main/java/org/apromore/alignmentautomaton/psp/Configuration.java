package org.apromore.alignmentautomaton.psp;

import java.util.List;
import static nl.tue.storage.hashing.impl.MurMur3HashCodeProvider.C1;
import static nl.tue.storage.hashing.impl.MurMur3HashCodeProvider.C2;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;

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

public class Configuration {

  private IntArrayList moveOnLog;

  private IntIntHashMap setMoveOnLog;

  private IntArrayList moveOnModel;

  private IntIntHashMap setMoveOnModel;

  //private List<Couple<Integer, Integer>> moveMatching;
  public enum Operation {
    MATCH,
    RHIDE,
    LHIDE
  }

  private final List<Synchronization> sequenceSynchronizations;

  private IntArrayList logIDs;

  private IntArrayList modelIDs;

  //alternative for the S-Components
  private List<Couple<Integer, Integer>> sCompIDs;

  private Integer hashCode = null;

  public Configuration(IntArrayList cloneMoveOnLog, List<Synchronization> alignment) {
    this.moveOnLog = cloneMoveOnLog;
    this.sequenceSynchronizations = alignment;
  }

  public Configuration(IntArrayList moveOnLog, IntIntHashMap setMoveOnLog, IntArrayList moveOnModel,
      IntIntHashMap setMoveOnModel, List<Synchronization> sequenceTransitions, IntArrayList logIDs,
      IntArrayList modelIDs) {
    this.moveOnLog = moveOnLog;
    this.setMoveOnLog = setMoveOnLog;
    this.moveOnModel = moveOnModel;
    this.setMoveOnModel = setMoveOnModel;
    //this.moveMatching = moveMatching;
    this.sequenceSynchronizations = sequenceTransitions;
    this.logIDs = logIDs;
    this.modelIDs = modelIDs;
  }

  public Configuration(IntArrayList moveOnLog, IntIntHashMap setMoveOnLog, IntArrayList moveOnModel,
      IntIntHashMap setMoveOnModel, List<Couple<Integer, Integer>> moveMatching,
      List<Synchronization> sequenceSynchronizations, IntArrayList logIDs, List<Couple<Integer, Integer>> sCompIDs) {
    this.moveOnLog = moveOnLog;
    this.setMoveOnLog = setMoveOnLog;
    this.moveOnModel = moveOnModel;
    this.setMoveOnModel = setMoveOnModel;
    //this.moveMatching = moveMatching;
    this.sequenceSynchronizations = sequenceSynchronizations;
    this.logIDs = logIDs;
    this.sCompIDs = sCompIDs;
  }

  public Configuration(List<Synchronization> partialAlignment) {
    this.sequenceSynchronizations = partialAlignment;
  }

  public IntArrayList moveOnLog() {
    return this.moveOnLog;
  }

  public IntIntHashMap setMoveOnLog() {
    if (this.setMoveOnLog == null) {
      this.setMoveOnLog = new IntIntHashMap();
    }
    //this.adjustSetMoveOnLog();
    return this.setMoveOnLog;
  }

  public IntArrayList moveOnModel() {
    return this.moveOnModel;
  }

  public IntIntHashMap setMoveOnModel() {
    if (this.setMoveOnModel == null) {
      this.setMoveOnModel = new IntIntHashMap();
    }
    //this.adjustSetMoveOnModel();
    return this.setMoveOnModel;
  }

  public List<Couple<Integer, Integer>> sCompIDs() {
    return this.sCompIDs;
  }

  //	public void adjustSetMoveOnLog()
  //	{
  //		if(this.setMoveOnLog==null)
  //			this.setMoveOnLog = new IntIntHashMap();
  //		setMoveOnLog.clear();
  //		for(int element : this.moveOnLog.distinct().toArray())
  //			setMoveOnLog.put(element, this.moveOnLog.count(t -> t==element));
  //	}
  //
  //	public void adjustSetMoveOnModel()
  //	{
  //		if(this.setMoveOnModel==null)
  //			this.setMoveOnModel = new IntIntHashMap();
  //		setMoveOnModel.clear();
  //		for(int element : this.moveOnModel.distinct().toArray())
  //			setMoveOnModel.put(element, this.moveOnModel.count(t -> t==element));
  //	}

  //public List<Couple<Integer, Integer>> moveMatching()
  //{
  //	return this.moveMatching;
  //}

  public String printAlignment() {
    String alignment = "Alignment: [";
    for (Synchronization sync : this.sequenceSynchronizations()) {
      alignment += sync.label() + ", ";
    }
    if (alignment.length() == 12) {
      alignment += "]";
    } else {
      alignment = alignment.substring(0, alignment.length() - 2) + "]";
    }
    return alignment;
  }

  public String printLastSync() {
    String lastSync = "";
    if (this.sequenceSynchronizations.isEmpty()) {
      lastSync = "[empty]";
    } else {
      lastSync = this.sequenceSynchronizations.get(this.sequenceSynchronizations.size() - 1).label();
    }
    return lastSync;
  }

  public Configuration cloneConfiguration() {
    Configuration clone;
    //IntArrayList cloneMoveOnLog = new IntArrayList();
    //cloneMoveOnLog.addAll(this.moveOnLog);
    //IntArrayList cloneMoveOnModel = new IntArrayList();
    //cloneMoveOnModel.addAll(this.moveOnModel);
    //IntArrayList cloneLogIDs = new IntArrayList();
    //cloneLogIDs.addAll(this.logIDs);
    //IntArrayList cloneModelIDs = new IntArrayList();
    //cloneModelIDs.addAll(this.modelIDs);
    //FastList<Couple<Integer,Integer>> moveMatching = new FastList<>();
    //moveMatching.addAll(this.moveMatching);
    List<Synchronization> alignment = new FastList<>(this.sequenceSynchronizations);
    //alignment.addAll(this.sequenceSynchronizations);
    //IntIntHashMap setMoveOnLog = new IntIntHashMap(this.setMoveOnLog);
    //IntIntHashMap setMoveOnModel = new IntIntHashMap(this.setMoveOnModel);
    //clone = new Configuration(cloneMoveOnLog, setMoveOnLog, cloneMoveOnModel, setMoveOnModel, alignment, cloneLogIDs, cloneModelIDs);
    clone = new Configuration(alignment);
    return clone;
  }

  public Configuration calculateSuffixFrom(Configuration configuration) {
    Configuration suffixConfiguration = this.cloneConfiguration();
    try {
      for (int i = 1; i <= configuration.logIDs().size(); i++) {
        suffixConfiguration.logIDs().removeAtIndex(0);
      }
      //suffixConfiguration.logIDs().removeAll(configuration.logIDs());
      for (int i = 1; i <= configuration.modelIDs().size(); i++) {
        suffixConfiguration.modelIDs().removeAtIndex(0);
      }
      //suffixConfiguration.modelIDs().removeAll(configuration.modelIDs());
      for (int i = 1; i <= configuration.moveOnLog().size(); i++) {
        suffixConfiguration.moveOnLog().removeAtIndex(0);
      }
      //suffixConfiguration.moveOnLog().removeAll(configuration.moveOnLog());
      for (int i = 1; i <= configuration.moveOnModel().size(); i++) {
        suffixConfiguration.moveOnModel().removeAtIndex(0);
      }
      //suffixConfiguration.moveOnModel().removeAll(configuration.moveOnModel());
      //for (int i = 1; i <= configuration.moveMatching().size(); i++) suffixConfiguration.moveMatching().remove(0);
      //suffixConfiguration.moveMatching = suffixConfiguration.moveMatching().subList(configuration.moveMatching().size(), suffixConfiguration.moveMatching().size());
      for (int i = 1; i <= configuration.sequenceSynchronizations().size(); i++) {
        suffixConfiguration.sequenceSynchronizations().remove(0);
      }
      //suffixConfiguration.sequenceTransitions = suffixConfiguration.sequenceTransitions().subList(configuration.sequenceTransitions().size(), suffixConfiguration.sequenceTransitions().size());
      for (int key : suffixConfiguration.setMoveOnLog().toArray()) {
        suffixConfiguration.setMoveOnLog().addToValue(key, -configuration.setMoveOnLog().get(key));
      }
      for (int key : suffixConfiguration.setMoveOnModel().toArray()) {
        suffixConfiguration.setMoveOnModel().addToValue(key, -configuration.setMoveOnModel().get(key));
      }
    } catch (Exception e) {
      System.out.println(this.printAlignment());
      System.out.println(configuration.printAlignment());
    }
    return suffixConfiguration;
  }

  public void addSuffixFrom(Configuration suffix) {
    this.logIDs().addAll(suffix.logIDs());
    this.modelIDs().addAll(suffix.modelIDs());
    this.moveOnLog().addAll(suffix.moveOnLog());
    this.moveOnModel().addAll(suffix.moveOnModel());
    //this.moveMatching().addAll(suffix.moveMatching());
    for (int key : suffix.setMoveOnLog().keySet().toArray()) {
      this.setMoveOnLog().addToValue(key, suffix.setMoveOnLog().get(key));
    }
    for (int key : suffix.setMoveOnModel().keySet().toArray()) {
      this.setMoveOnModel().addToValue(key, suffix.setMoveOnLog().get(key));
    }
    //		this.setMoveOnLog().putAll(map);
    //		this.adjustSetMoveOnLog();
    //		this.adjustSetMoveOnModel();
    this.sequenceSynchronizations().addAll(suffix.sequenceSynchronizations());
  }

  public List<Synchronization> sequenceSynchronizations() {
    return this.sequenceSynchronizations;
  }

  public IntArrayList logIDs() {
    return this.logIDs;
  }

  public IntArrayList modelIDs() {
    return this.modelIDs;
  }

  /*
  public String label()
  {
    String label = "Synchronuous Moves: {";
    for(Event e : this.moveMatching().keySet())
      label = label + "(" + e.label() + "; " + this.moveMatching().get(e).label() + "), ";
    label = label.substring(0, label.length() - 2) + "}\n";
    label = label + "Move on Log: {";
    for(Event e : this.moveOnLog())
      label = label + e.label() + ", ";
    label = label.substring(0, label.length() - 2) + "}\n";
    label = label + "Move on Model: {";
    for(Event e : this.moveOnModel())
      label = label + e.label() + ", ";
    label = label.substring(0, label.length() - 2) + "}";
    return label;
  }
  */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Configuration configuration = (Configuration) o;
    if (this.sequenceSynchronizations().size() != configuration.sequenceSynchronizations().size()) {
      return false;
    }

    for (int i = 0; i < this.sequenceSynchronizations().size(); i++) {
      if (this.sequenceSynchronizations().get(i).hashCode() != configuration.sequenceSynchronizations().get(i)
          .hashCode()) {
        return false;
      }
      if (!this.sequenceSynchronizations().get(i).equals(configuration.sequenceSynchronizations().get(i))) {
        return false;
      }
    }

    return true;
    //        return this.sequenceTransitions().equals(configuration.sequenceTransitions());
    //        for(int i=0; i<this.sequenceTransitions().size();i++)
    //        	if(!this.sequenceTransitions().get(i).equals(configuration.sequenceTransitions().get(i)))
    //        		return false;
    //        return true;
    //        return new EqualsBuilder()
    //        		//.append(this.moveMatching(), configuration.moveMatching())
    //        		//.append(this.moveOnLog(), configuration.moveOnLog())
    //        		//.append(this.moveOnModel(), configuration.moveOnModel())
    //        		.append(this.sequenceTransitions(), configuration.sequenceTransitions())
    //        		//.append(this.logIDs(), configuration.logIDs())
    //        		//.append(this.modelIDs(), configuration.modelIDs())
    //        		.isEquals();
  }

  @Override
  public int hashCode() {
    if (hashCode == null) {
      hashCode = compress();
      //	    	hashCode = 1;
      //	    	for(Synchronization tr : this.sequenceTransitions())
      //	    		hashCode += 31 * tr.hashCode();
      //hashCode = new HashCodeBuilder(17,37)
      //    	        		//.append(this.moveMatching())
      //    	        		//.append(this.moveOnLog())
      //    	        		//.append(this.moveOnModel())
      //.append(this.sequenceTransitions())
      //    	        		//.append(this.logIDs())
      //    	        		//.append(this.modelIDs())
      //.toHashCode();
    }
    return hashCode;
    //        return new HashCodeBuilder(17,37)
    //        		//.append(this.moveMatching())
    //        		//.append(this.moveOnLog())
    //        		//.append(this.moveOnModel())
    //        		.append(this.sequenceTransitions())
    //        		//.append(this.logIDs())
    //        		//.append(this.modelIDs())
    //        		.toHashCode();
  }

  /**
   * Returns the MurmurHash3_x86_32 hash.
   */
  protected int compress() {

    int[] data = new int[sequenceSynchronizations.size()];
    for (int i = 0; i < sequenceSynchronizations.size(); i++) {
      data[i] = sequenceSynchronizations.get(i).hashCode();
    }

    //data[2] = this.stateModelrep;
    int hash = 0;
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
