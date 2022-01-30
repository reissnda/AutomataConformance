package mytree;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

public class ConcurrentHierarchyLabelsTree
{
    private IntObjectHashMap<UnifiedSet<String>> nodes = new IntObjectHashMap<>();
    private IntObjectHashMap<IntHashSet> parents = new IntObjectHashMap<>();
    private IntHashSet transitiveConcurrentPairs = new IntHashSet();
    //private int countTransitiveConcurrentPairs = 0;

    public boolean addParent(UnifiedSet<String> leaf, UnifiedSet<String> parent)
    {
        int leafHashCode=leaf.hashCode();
        int parentHashCode=parent.hashCode();
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
        parentSet.add(parentHashCode);
        return true;
    }

    public void addConcurrencyRelation(UnifiedSet<String> leaf, String concurrentSecondLabel)
    {
        UnifiedSet<String> parent;
        int leafHashCode = leaf.hashCode();
        if(parents.get(leafHashCode)!=null) {
            for (int parentHashCode : parents.get(leafHashCode).toArray()) {
                addConcurrencyRelation(nodes.get(parentHashCode), concurrentSecondLabel);
            }
        }
        parent = new UnifiedSet<>(leaf);
        parent.add(concurrentSecondLabel);
        if(addParent(leaf, parent) && parent.size()>2)
        {
            transitiveConcurrentPairs.add(parent.hashCode());
            //System.out.println(parent);
        }
    }

    public int getCountTransitiveConcurrentPairs()
    {
        return transitiveConcurrentPairs.size();
    }

    public IntObjectHashMap<UnifiedSet<String>> getNodes()
    {
        return this.nodes;
    }
}
