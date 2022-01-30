package au.unimelb.partialorders;

import com.google.common.collect.BiMap;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntDoubleHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

import java.io.IOException;
import java.io.PrintWriter;

import static nl.tue.storage.hashing.impl.MurMur3HashCodeProvider.C1;
import static nl.tue.storage.hashing.impl.MurMur3HashCodeProvider.C2;

public class ConcurrencyHierarchyTreeWithSupport
{
    private IntObjectHashMap<IntHashSet> nodes = new IntObjectHashMap<>();
    //private IntObjectHashMap<UnifiedSet<Integer>> nodes = new IntObjectHashMap<>();
    private IntDoubleHashMap supportLevels = new IntDoubleHashMap();
    private IntDoubleHashMap absoluteSupportTable = new IntDoubleHashMap();
    private IntDoubleHashMap relativeSupportTable = new IntDoubleHashMap();
    private double supportThreshold = 0.5;
    private IntObjectHashMap<IntHashSet> parents = new IntObjectHashMap<>();
    private IntObjectHashMap<IntHashSet> children = new IntObjectHashMap<>();
    private IntHashSet transitiveConcurrentPairs = new IntHashSet();
    private IntHashSet leafs = new IntHashSet();
    private IntHashSet roots = new IntHashSet();
    private IntHashSet supportedConcurrencies;
    private BiMap<Integer, String> labelMapping;
    //private int countTransitiveConcurrentPairs = 0;

    public ConcurrencyHierarchyTreeWithSupport()
    {

    }

    public ConcurrencyHierarchyTreeWithSupport(BiMap<Integer,String> labelMapping)
    {
        this.labelMapping = labelMapping;
    }



    public void addConcurrencyRelation(int concurrentFirstLabel, int concurrentSecondLabel)
    {
        if(concurrentFirstLabel==concurrentSecondLabel) return;
        IntHashSet leaf = IntHashSet.newSetWith(concurrentFirstLabel,concurrentSecondLabel);
        int leafHashCode = compress(leaf);
        if(nodes.containsKey(leafHashCode)) return;
        this.nodes.put(leafHashCode,leaf);
        this.roots.add(leafHashCode);
        leafs.add(leafHashCode);

        for(int leafNodeHashCode : this.leafs.toArray())
        {
            if(leafNodeHashCode==leafHashCode) continue;
            IntHashSet leaf2 = nodes.get(leafNodeHashCode);
            if(leaf2.contains(concurrentFirstLabel) || leaf2.contains(concurrentSecondLabel))
            {
                addTransitiveConcurrencyRelation(leafHashCode,leafNodeHashCode);
            }
        }


    }

    private void addTransitiveConcurrencyRelation(int nodeHashCode1, int nodeHashCode2)
    {
        IntHashSet node1 = nodes.get(nodeHashCode1);
        IntHashSet node2 = nodes.get(nodeHashCode2);
        IntHashSet parent = IntHashSet.newSet(node1);
        parent.addAll(node2);
        int parentHashCode = compress(parent);
        IntHashSet parentHashCodes;
        IntHashSet childrenHashSet;
        if((parentHashCodes = parents.get(nodeHashCode1))==null)
        {
            parentHashCodes = new IntHashSet();
            parents.put(nodeHashCode1, parentHashCodes);
        }
        if(parentHashCodes.contains(parentHashCode)) return;
        parentHashCodes.add(parentHashCode);
        if((parentHashCodes=parents.get(nodeHashCode2))==null)
        {
            parentHashCodes=new IntHashSet();
            parents.put(nodeHashCode2,parentHashCodes);
        }
        parentHashCodes.add(parentHashCode);
        if((childrenHashSet=children.get(parentHashCode))==null)
        {
            childrenHashSet=new IntHashSet();
            children.put(parentHashCode,childrenHashSet);
        }
        childrenHashSet.addAll(nodeHashCode1,nodeHashCode2);
        this.roots.removeAll(nodeHashCode1,nodeHashCode2);
        this.roots.add(parentHashCode);
        if(nodes.containsKey(parentHashCode)) return;
        nodes.put(parentHashCode,parent);
        transitiveConcurrentPairs.add(parentHashCode);
        int parentSize = parent.size();
        for(int nodeHashCode : transitiveConcurrentPairs.toArray())
        {
            if(nodeHashCode==parentHashCode) continue;
            IntHashSet node = nodes.get(nodeHashCode);
            if(node.size()!=parentSize) continue;
            int countMatching=0, countMismatches=0;
            for(int label : parent.toArray())
            {
                if(node.contains(label))
                {
                    countMatching++;
                }
                else
                {
                    countMismatches++;
                    if(countMismatches>1) break;
                }
            }
            if(countMatching==parentSize-1) addTransitiveConcurrencyRelation(parentHashCode, nodeHashCode);
        }
    }

    /*public void addConcurrencyRelation(IntHashSet leaf, int concurrentSecondLabel)
    {
        IntHashSet parent;
        int leafHashCode = compress(leaf);
        if(parents.get(leafHashCode)!=null) {
            for (int parentHashCode : parents.get(leafHashCode).toArray()) {
                addConcurrencyRelation(nodes.get(parentHashCode), concurrentSecondLabel);
            }
        }
        parent = new IntHashSet(leaf);
        parent.add(concurrentSecondLabel);
        if(addParent(leaf, parent) && parent.size()>2)
        {
            transitiveConcurrentPairs.add(compress(parent));
            //System.out.println(parent);
        }
    }

    public boolean addParent(IntHashSet leaf, IntHashSet parent)
    {
        int leafHashCode=compress(leaf);
        int parentHashCode=compress(parent);
        if(leafHashCode==parentHashCode) return false;
        IntHashSet parentSet;
        if((parentSet = parents.get(leafHashCode))==null)
        {
            parentSet = new IntHashSet();
            parents.put(leafHashCode,parentSet);
        }
        if(parents.get(leafHashCode).contains(parentHashCode)) return false;
        nodes.put(leafHashCode,leaf);
        nodes.put(parentHashCode,parent);
        if(parent.size()==2) leafs.add(parentHashCode);
        parentSet.add(parentHashCode);
        roots.add(parentHashCode);
        roots.remove(leafHashCode);
        IntHashSet childrenSet;
        if((childrenSet = children.get(parentHashCode))==null)
        {
            childrenSet = new IntHashSet();
            children.put(parentHashCode,childrenSet);
        }
        childrenSet.add(leafHashCode);
        return true;
    }*/

    public void computeSupport()
    {
        IntHashSet addSupportSet;
        IntArrayList toBeVisited;
        for(int leafHashCode : leafs.toArray())
        {
            addSupportSet = new IntHashSet();
            if(!parents.containsKey(leafHashCode)) continue;
            toBeVisited = IntArrayList.newList(parents.get(leafHashCode));
            while(!toBeVisited.isEmpty())
            {
                int currentParent = toBeVisited.removeAtIndex(0);
                addSupportSet.add(currentParent);
                if(parents.get(currentParent)!=null)
                {
                    for(int parentParentHashCode : parents.get(currentParent).toArray())
                    {
                        if(addSupportSet.add(parentParentHashCode)) toBeVisited.add(parentParentHashCode);
                    }
                }
            }
            for(int supportedParentHashCode : addSupportSet.toArray())
            {
                this.supportLevels.put(supportedParentHashCode,this.supportLevels.get(supportedParentHashCode)+1);
            }
        }
        for(int hashCode : this.nodes.keySet().toArray())
        {
            absoluteSupportTable.put(hashCode, getAbsoluteSupportForHashCode(hashCode));
            relativeSupportTable.put(hashCode, getRelativeSupportForHashCode(hashCode));
        }
    }

    public void determineMostSupportedNodes()
    {
        computeSupport();
        IntArrayList toBeVisited = IntArrayList.newList(roots);
        supportedConcurrencies = new IntHashSet();
        while(!toBeVisited.isEmpty())
        {
            int nodeHashCode = toBeVisited.removeAtIndex(0);
            if(relativeSupportTable.get(nodeHashCode)>supportThreshold && relativeSupportTable.get(nodeHashCode)!=1 && nodes.get(nodeHashCode).size()>2)
            {
                supportedConcurrencies.add(nodeHashCode);
            }
            else
            {
                if(children.containsKey(nodeHashCode))
                    toBeVisited.addAll(children.get(nodeHashCode));
            }
        }
    }

    public void printMostSupportedConcurrencies()
    {
        determineMostSupportedNodes();
        for(int nodeHashCode : this.supportedConcurrencies.toArray())
        {
            String out="Concurrent Activities: [";
            for(int label : nodes.get(nodeHashCode).toArray())
            {
                if(labelMapping!=null)
                    out+=labelMapping.get(label) +  ", ";
                else
                    out+= label + ", ";
            }
            out = out.substring(0,out.length()-2) + "], Support: " + relativeSupportTable.get(nodeHashCode);
            System.out.println(out);
        }
    }

    public UnifiedSet<IntHashSet> getTransitiveConcurrenciesWithSupport()
    {
        UnifiedSet<IntHashSet> transitiveConcurrenciesWithSupport = new UnifiedSet<>();
        if(this.supportedConcurrencies==null)
            determineMostSupportedNodes();
        for(int supportedNodeHashCode : this.supportedConcurrencies.toArray())
        {
            transitiveConcurrenciesWithSupport.add(nodes.get(supportedNodeHashCode));
        }
        return transitiveConcurrenciesWithSupport;
    }

    public double getRelativeSupportForTransitiveConcurrentPair(IntHashSet concurrentActivities)
    {
        int hashCode = compress(concurrentActivities);
        if(concurrentActivities.isEmpty() || !nodes.containsKey(hashCode)) return 0;
        return getRelativeSupportForHashCode(hashCode);
    }

    private double getRelativeSupportForHashCode(int hashCode)
    {
        double support = getAbsoluteSupportForHashCode(hashCode);
        return support / computeMaxSupport(nodes.get(hashCode).size());
    }

    public double getAbsoluteSupportForTransitiveConcurrentPair(IntHashSet concurrentActivities)
    {
        return getAbsoluteSupportForHashCode(compress(concurrentActivities));
    }

    private double getAbsoluteSupportForHashCode(int hashCode)
    {
        if(!nodes.containsKey(hashCode))
            return 0;
        else if(nodes.get(hashCode).size()==2)
            return 1;
        return this.supportLevels.get(hashCode);
    }

    private double computeMaxSupport(int numberOfConcurrentActivities)
    {
        double maxSupport = 0;
        for(double i=1;i<numberOfConcurrentActivities; i++)
        {
            maxSupport+=i;
        }
        return maxSupport;
    }

    public int getCountTransitiveConcurrentPairs()
    {
        return transitiveConcurrentPairs.size();
    }

    public IntObjectHashMap<IntHashSet> getNodes()
    {
        return this.nodes;
    }

    /** Returns the MurmurHash3_x86_32 hash. */
    protected int compress(IntHashSet set) {

        int[] data = set.toArray();

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

    public void toDot(PrintWriter pw) throws IOException {
        pw.println("digraph fsm {");
        pw.println("rankdir=LR;");
        pw.println("node [shape=circle,style=filled, fillcolor=white]");

        for(int node : this.nodes.keySet().toArray())
        {
            pw.printf("%d [label=\"%s\"];%n", node, nodes.get(node));

            IntHashSet nodeOutgoingArcs = parents.get(node);
            if(nodeOutgoingArcs==null) continue;
            for(int outNode : nodeOutgoingArcs.toArray())
            {
                pw.printf("%d -> %d;%n", node, outNode);
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
