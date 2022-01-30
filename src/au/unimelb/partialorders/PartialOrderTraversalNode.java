package au.unimelb.partialorders;

import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.jbpt.hypergraph.abs.IVertex;

public class PartialOrderTraversalNode
{
    private static int ID = 0;
    private int id;
    private int vertex;
    private IntArrayList trace;

    public IntArrayList vertices() {
        return vertices;
    }

    private IntArrayList vertices;
    private IntHashSet memory;

    public IntHashSet getConcurrentLabels() {
        return concurrentLabels;
    }


    private IntHashSet concurrentLabels;

    public IntArrayList getConcurrentTracePositions() {
        return concurrentTracePositions;
    }

    private IntArrayList concurrentTracePositions;

    public boolean isConcurrent() {
        return isConcurrent;
    }

    public void setConcurrent(boolean concurrent) {
        isConcurrent = concurrent;
        if(isConcurrent)
        {
            this.concurrentLabels=new IntHashSet();
            this.concurrentTracePositions=new IntArrayList();
        }
        else {
            this.concurrentTracePositions=null;
            this.concurrentLabels=null;

        }
    }

    private boolean isConcurrent;

    public PartialOrderTraversalNode(int vertex, IntArrayList trace)
    {
        this.id = PartialOrderTraversalNode.ID++;
        this.vertex = vertex;
        this.vertices=IntArrayList.newListWith(vertex);
        this.trace = trace;
        this.memory = new IntHashSet();
        this.setConcurrent(false);
    }

    public PartialOrderTraversalNode(int vertex, IntArrayList trace, IntHashSet concurrencyMemory)
    {
        this.id = PartialOrderTraversalNode.ID++;
        this.vertex = vertex;
        this.trace = trace;
        this.memory =concurrencyMemory;
        this.setConcurrent(false);
    }

    public PartialOrderTraversalNode(int vertex, IntArrayList trace, IntHashSet concurrencyMemory,
                                     IntArrayList concurrentTracePositions, IntHashSet concurrentLabels,
                                     boolean isConcurrent)
    {
        this.id = PartialOrderTraversalNode.ID++;
        this.vertex = vertex;
        this.trace = trace;
        this.memory =concurrencyMemory;
        this.concurrentTracePositions=concurrentTracePositions;
        this.concurrentLabels = concurrentLabels;
        this.isConcurrent=isConcurrent;
    }

    //public PartialOrderTraversalNode(int vertex, IntArrayList trace, IntArrayList vertices, IntHashSet concurrencyMemory)
    //{
    //
    //}

    public PartialOrderTraversalNode(int outVertex, IntArrayList trace, IntArrayList vertices, IntHashSet concurrencyMemory,
                                     IntArrayList concurrentTracePositions, IntHashSet concurrentLabels, boolean isConcurrent)
    {
        this.id = PartialOrderTraversalNode.ID++;
        this.vertex = outVertex;
        this.vertices=IntArrayList.newList(vertices);
        this.vertices.add(vertex);
        this.trace = trace;
        this.memory =concurrencyMemory;
        this.concurrentTracePositions=concurrentTracePositions;
        this.concurrentLabels = concurrentLabels;
        this.isConcurrent=isConcurrent;
    }

    public int id()
    {
        return this.id;
    }

    public int vertex()
    {
        return this.vertex;
    }

    public IntArrayList trace()
    {
        return this.trace;
    }

    public IntHashSet memory()
    {
        return this.memory;
    }
}
