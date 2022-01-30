package au.unimelb.partialorders;

import au.qut.apromore.automaton.Automaton;
import au.qut.apromore.automaton.State;
import au.qut.apromore.automaton.Transition;
import au.qut.apromore.importer.ImportEventLog;
import au.unimelb.pattern.ConcurrentPattern;
import com.google.common.collect.BiMap;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class PartialOrder
{
    public static int startEvent = -20;
    public static int endEvent = -30;
    private UnifiedSet<IntHashSet> transitiveConcurrencies;
    private IntIntHashMap vertexLabelMapping = new IntIntHashMap();
    private int vertexID = 0;
    private IntObjectHashMap<IntHashSet> labelVertexMapping = new IntObjectHashMap<>();
    private IntObjectHashMap<IntHashSet> outgoingArcs = new IntObjectHashMap<>();

    private IntObjectHashMap<IntHashSet> incomingArcs;
    private IntArrayList trace;
    private IntIntHashMap finalConfiguration;
    private UnifiedSet<IntHashSet> concurrencies;
    private IntHashSet visitedDFS;
    private UnifiedSet<IntArrayList> representativeTraces;
    //private UnifiedMap<IntArrayList,IntArrayList> trPositionsConcurrentLabelMapping;
    private Set<ConcurrentPattern> concurrentPatterns;
    private Automaton dafsa;
    private BiMap<String, Integer> inverseLabelMapping;
    private UnifiedMap<IntArrayList, IntArrayList> caseTracesMapping;
    private Map<Integer, String> caseIDs;
    private int traceCount=0;

    public PartialOrder(IntArrayList trace, IntIntHashMap finalConfiguration, UnifiedSet<IntHashSet> concurrencies)
    {
        this.trace = IntArrayList.newList(trace);
        this.finalConfiguration = finalConfiguration;
        this.trace.addAtIndex(0,startEvent);
        this.trace.add(endEvent);
        this.concurrencies = concurrencies;
        buildPartialOrder();
    }

    public PartialOrder(IntArrayList trace, IntIntHashMap finalConfiguration, UnifiedSet<IntHashSet> concurrencies,
                        BiMap<String, Integer> inverseLabelMapping, UnifiedMap<IntArrayList, IntArrayList> caseTracesMapping, Map<Integer, String> caseIDs)
    {
        this.trace = IntArrayList.newList(trace);
        this.finalConfiguration = finalConfiguration;
        trace.addAtIndex(0,startEvent);
        trace.add(endEvent);
        this.concurrencies = concurrencies;
        this.caseTracesMapping = caseTracesMapping;
        this.caseIDs = caseIDs;
        this.inverseLabelMapping = inverseLabelMapping;
        buildPartialOrder();
    }

    public PartialOrder(IntArrayList trace, UnifiedSet<IntHashSet> concurrencies)
    {
        this.trace = IntArrayList.newList(trace);
        this.finalConfiguration = new IntIntHashMap();
        for(int label : trace.distinct().toArray())
        {
            this.finalConfiguration.put(label,trace.count(l->l==label));
        }
        this.trace.addAtIndex(0,startEvent);
        this.trace.add(endEvent);
        this.concurrencies = concurrencies;
        buildPartialOrder();
    }

    public PartialOrder(IntArrayList trace, UnifiedSet<IntHashSet> concurrencies,
                        BiMap<String, Integer> inverseLabelMapping, UnifiedMap<IntArrayList, IntArrayList> caseTracesMapping,
                        Map<Integer, String> caseIDs,int traceCount)
    {
        this.trace = IntArrayList.newList(trace);
        this.finalConfiguration = new IntIntHashMap();
        for(int label : trace.distinct().toArray())
        {
            this.finalConfiguration.put(label,trace.count(l->l==label));
        }
        this.trace.addAtIndex(0,startEvent);
        this.trace.add(endEvent);
        this.concurrencies = concurrencies;
        this.caseTracesMapping = caseTracesMapping;
        this.caseIDs = caseIDs;
        this.inverseLabelMapping = inverseLabelMapping;
        this.traceCount = traceCount;
        buildPartialOrder();
    }

    public PartialOrder(IntArrayList trace, UnifiedSet<IntHashSet> concurrencies, UnifiedSet<IntHashSet> transitiveConcurrencies,
                        BiMap<String, Integer> inverseLabelMapping, UnifiedMap<IntArrayList, IntArrayList> caseTracesMapping,
                        Map<Integer, String> caseIDs,int traceCount)
    {
        this.trace = IntArrayList.newList(trace);
        this.finalConfiguration = new IntIntHashMap();
        for(int label : trace.distinct().toArray())
        {
            this.finalConfiguration.put(label,trace.count(l->l==label));
        }
        this.trace.addAtIndex(0,startEvent);
        this.trace.add(endEvent);
        this.concurrencies = concurrencies;
        this.transitiveConcurrencies=transitiveConcurrencies;
        this.caseTracesMapping = caseTracesMapping;
        this.caseIDs = caseIDs;
        this.inverseLabelMapping = inverseLabelMapping;
        this.traceCount = traceCount;
        buildPartialOrderWithTransitiveConcurrencies();
    }

    private void buildPartialOrderWithTransitiveConcurrencies()
    {
        deriveDFandEFrelations();
        introduceConcurrencyRelations();
        introduceTransitiveConcurrencyRelations();
        performTransitiveReduction();
        reduceRepetitions();
        computeIncomingArcs();
        deriveRepresentativeTraces();
        removeNonTransitivePatterns();
    }

    private void buildPartialOrder()
    {
        deriveDFandEFrelations();
        introduceConcurrencyRelations();
        performTransitiveReduction();
        reduceRepetitions();
        computeIncomingArcs();
        deriveRepresentativeTraces();
    }

    private void reduceRepetitions()
    {
        for(int node : this.vertexLabelMapping.keySet().toArray())
        {
            reduceRepetitivePath(node);
        }
    }

    private void reduceRepetitivePath(int node)
    {
        int currentNode= node,nextNode;
        int label = this.vertexLabelMapping.get(node);
        while((nextNode = getNextNode(currentNode,label))>0)
        {
            currentNode=nextNode;
        }
        if(currentNode==node || this.outgoingArcs.get(node).contains(currentNode)) return;
        this.outgoingArcs.get(node).remove(getNextNode(node,label));
        for(int inNode: this.vertexLabelMapping.keySet().toArray())
        {
            if(this.vertexLabelMapping.get(inNode)!=label) continue;
            if(getNextNode(inNode,label)==currentNode)
            {
                this.outgoingArcs.get(inNode).remove(currentNode);
                break;
            }
        }
        this.outgoingArcs.get(node).add(currentNode);
    }

    private int getNextNode(int currentNode, int label)
    {
        int nextNode = -1;
        IntHashSet outVertices = this.outgoingArcs.get(currentNode);
        if(outVertices==null) return nextNode;
        for(int outNode : outVertices.toArray())
        {
            if(this.vertexLabelMapping.get(outNode)==label)
            {
                nextNode=outNode;
                break;
            }
        }
        return nextNode;
    }

    private void addVertex(int vertex)
    {
        IntHashSet vertices;
        int label = trace.get(vertex);
        vertexLabelMapping.put(vertex,label);
        if((vertices=labelVertexMapping.get(label))==null)
        {
            vertices=new IntHashSet();
            labelVertexMapping.put(label,vertices);
        }
        vertices.add(vertex);
    }

    private void addArc(int source, int target)
    {
        if(!vertexLabelMapping.containsKey(source))
        {
            addVertex(source);
        }
        if(!vertexLabelMapping.containsKey(target))
        {
            addVertex(target);
        }
        IntHashSet vertices;
        if((vertices=outgoingArcs.get(source))==null)
        {
            vertices=new IntHashSet();
            outgoingArcs.put(source,vertices);
        }
        vertices.add(target);
    }

    private void deriveDFandEFrelations()
    {
        for(int source=0; source<trace.size()-1;source++)
        {
            for(int target=source+1;target<trace.size();target++)
            {
                addArc(source,target);
            }
        }
    }

    private void introduceConcurrencyRelations()
    {
        //only consider the
        if(concurrencies==null) return;
        for(IntHashSet concurrency : concurrencies)
        {
            /*boolean concurrencySupported = true;
            for(int concurrentLabel : concurrency.toArray())
            {
                if(!labelVertexMapping.containsKey(concurrentLabel))
                {
                    concurrencySupported = false;
                    break;
                }
            }
            if(!concurrencySupported) continue;*/
            int firstConcurrentLabel = concurrency.min();
            int secondConcurrentLabel = concurrency.max();
            if(!labelVertexMapping.containsKey(firstConcurrentLabel) || !labelVertexMapping.containsKey(secondConcurrentLabel))
                continue;
            for(int vertex : labelVertexMapping.get(firstConcurrentLabel).toArray())
                outgoingArcs.get(vertex).removeAll(labelVertexMapping.get(secondConcurrentLabel));
            for(int vertex : labelVertexMapping.get(secondConcurrentLabel).toArray())
                outgoingArcs.get(vertex).removeAll(labelVertexMapping.get(firstConcurrentLabel));
        }
    }

    private void introduceTransitiveConcurrencyRelations()
    {
        if(this.transitiveConcurrencies.isEmpty()) return;
        for(IntHashSet transitiveConcurrency : this.transitiveConcurrencies)
        {
            if(!trace.containsAll(transitiveConcurrency)) continue;
            int[] concurrentLabels = transitiveConcurrency.toArray();
            for(int i=0;i<concurrentLabels.length;i++)
            {
                for(int j=0;j<concurrentLabels.length;j++)
                {
                    if(i==j) continue;
                    for(int vertex : labelVertexMapping.get(concurrentLabels[i]).toArray())
                        outgoingArcs.get(vertex).removeAll(labelVertexMapping.get(concurrentLabels[j]));
                }
            }
        }
    }

    private void performTransitiveReduction()
    {
        for(int vertex : vertexLabelMapping.keySet().toArray())
        {
            IntHashSet vertexOutgoingArcs;
            if((vertexOutgoingArcs = outgoingArcs.get(vertex))==null) return;
            for(int directlyLinkedVertex : vertexOutgoingArcs.toArray())
            {
                removeAllTransitiveEdges(vertex, directlyLinkedVertex);
            }
        }
    }

    private void removeAllTransitiveEdges(int vertex, int directlyLinkedVertex)
    {
        IntHashSet directlyLinkedVertexOutgoingArcs;
        if((directlyLinkedVertexOutgoingArcs = outgoingArcs.get(directlyLinkedVertex))==null) return;
        visitedDFS = IntHashSet.newSetWith(directlyLinkedVertex);
        for(int outVertex : directlyLinkedVertexOutgoingArcs.toArray())
        {
            if(!visitedDFS.contains(outVertex)) dfs(vertex,outVertex);
        }
        visitedDFS=null;
    }

    private void dfs(int v, int u)
    {
        visitedDFS.add(u);
        outgoingArcs.get(v).remove(u);
        IntHashSet uoutgoingArcs;
        if(( uoutgoingArcs = outgoingArcs.get(u))==null) return;
        for(int outVertex : uoutgoingArcs.toArray())
        {
            if(!visitedDFS.contains(outVertex)) dfs(v,outVertex);
        }
    }

    private void computeIncomingArcs()
    {
        incomingArcs = new IntObjectHashMap<>();
        IntHashSet inVertices;
        for(int vertex : vertexLabelMapping.keySet().toArray())
        {
            inVertices = new IntHashSet();
            incomingArcs.put(vertex,inVertices);
            for(int secondVertex : outgoingArcs.keySet().toArray())
            {
                if(secondVertex==vertex) continue;
                if(outgoingArcs.get(secondVertex).contains(vertex)) inVertices.add(secondVertex);
            }
        }
    }

    private void deriveRepresentativeTraces()
    {
        int nNodesThreshold = 300000;
        this.representativeTraces = new UnifiedSet<>();
        //this.trPositionsConcurrentLabelMapping = new UnifiedMap<>();
        this.concurrentPatterns = new UnifiedSet<>();
        PartialOrderTraversalNode nextNode, node = new PartialOrderTraversalNode(vertexLabelMapping.get(startEvent),new IntArrayList());
        IntObjectHashMap<PartialOrderTraversalNode> idPOnodesMapping = new IntObjectHashMap<>();
        idPOnodesMapping.put(node.id(),node);
        IntArrayList toBeVisited = IntArrayList.newListWith(node.id());
        int nodeID, vertex;
        IntArrayList trace;
        IntHashSet concurrencyMemory;

        while(!toBeVisited.isEmpty())
        {
            nodeID = toBeVisited.removeAtIndex(0);
            if(nodeID==nNodesThreshold) break;
            node = idPOnodesMapping.get(nodeID);

            IntHashSet outVertices = outgoingArcs.get(node.vertex());
            if(outVertices==null)
            {
                representativeTraces.add(node.trace());
                continue;
            }
            if(outVertices.size()>1)
            {
                node.setConcurrent(true);
                //IntArrayList trPositions = new IntArrayList();
                //IntArrayList concurrentTraceLabels = new IntArrayList();
                //for(int pos=node.trace().size()+1;pos<=node.trace().size()+outVertices.size();pos++) trPositions.add(pos);
                //for(int outVertex : outVertices.toArray()) concurrentTraceLabels.add(vertexLabelMapping.get(outVertex));
                //concurrentPatterns.add(new ConcurrentPattern(trPositions,concurrentTraceLabels));
            }
            for(int outVertex : outVertices.toArray())
            {
                if(!allDFrelationsfulfilled(node.vertices(), outVertex)) continue;
                boolean isConcurrent = node.isConcurrent();
                if(incomingArcs.get(outVertex).size()>1 && node.memory().isEmpty())
                {
                    //if(node.getConcurrentTracePositions()==null)
                    //    System.out.println("Problem");
                    if(node.getConcurrentTracePositions()!=null && !node.getConcurrentTracePositions().isEmpty()) {
                        //System.out.println("Problem");
                        concurrentPatterns.add(new ConcurrentPattern(node.getConcurrentTracePositions(), node.getConcurrentLabels()));
                        isConcurrent = false;
                    }
                }
                trace = IntArrayList.newList(node.trace());
                int label = vertexLabelMapping.get(outVertex);
                if(label!=endEvent)
                    trace.add(label);
                concurrencyMemory = IntHashSet.newSet(node.memory());
                if(outVertices.size()>1)
                {
                    concurrencyMemory.addAll(outVertices);
                    concurrencyMemory.remove(outVertex);
                }
                IntArrayList concurrentTracePositions = null;
                IntHashSet concurrentLabels = null;
                if(node.isConcurrent())
                {
                    concurrentTracePositions=IntArrayList.newList(node.getConcurrentTracePositions());
                    concurrentLabels=IntHashSet.newSet(node.getConcurrentLabels());
                    concurrentTracePositions.add(trace.size()-1);
                    concurrentLabels.add(label);
                }
                nextNode = new PartialOrderTraversalNode(outVertex,trace,node.vertices(),concurrencyMemory,concurrentTracePositions,concurrentLabels,isConcurrent);
                idPOnodesMapping.put(nextNode.id(),nextNode);
                toBeVisited.add(nextNode.id());
            }
            //if(!node.memory().isEmpty()) {
            for (int memoryVertex : node.memory().toArray()) {
                trace = IntArrayList.newList(node.trace());
                int label = vertexLabelMapping.get(memoryVertex);
                if(label!=endEvent)
                    trace.add(label);
                concurrencyMemory = IntHashSet.newSet(node.memory());
                concurrencyMemory.remove(memoryVertex);
                for(int outVertex : outVertices.toArray())
                    if(allDFrelationsfulfilled(node.vertices(),outVertex)) concurrencyMemory.add(outVertex);
                IntArrayList concurrentTracePositions = null;
                IntHashSet concurrentLabels = null;
                if(node.isConcurrent())
                {
                    concurrentTracePositions=IntArrayList.newList(node.getConcurrentTracePositions());
                    concurrentLabels=IntHashSet.newSet(node.getConcurrentLabels());
                    concurrentTracePositions.add(trace.size()-1);
                    concurrentLabels.add(label);
                }
                nextNode = new PartialOrderTraversalNode(memoryVertex, trace, node.vertices(),concurrencyMemory,concurrentTracePositions,concurrentLabels,node.isConcurrent());
                idPOnodesMapping.put(nextNode.id(), nextNode);
                toBeVisited.add(nextNode.id());
            }
            //}
            idPOnodesMapping.remove(node.id());
        }
        this.caseTracesMapping=new UnifiedMap<>();
        int i=0;
        for(IntArrayList repTrace : representativeTraces)
        {
        //    System.out.println(repTrace);
            caseTracesMapping.put(repTrace,IntArrayList.newListWith(i++));
        }
        //System.out.println("----------");


    }

    private boolean allDFrelationsfulfilled(int vertex, IntArrayList trace)
    {
        boolean allFulfilled = true;
        int label;
        for(int inVertex : incomingArcs.get(vertex).toArray())
        {
            label = vertexLabelMapping.get(inVertex);
            if(label==startEvent) continue;
            if(!trace.contains(label))
            {
                allFulfilled = false;
                break;
            }
        }
        return allFulfilled;
    }

    private boolean allDFrelationsfulfilled(IntArrayList vertices, int vertex)
    {
        boolean allFulfilled = true;
        int label;
        for(int inVertex : incomingArcs.get(vertex).toArray())
        {
            if(!vertices.contains(inVertex))
            {
                allFulfilled = false;
                break;
            }
        }
        return allFulfilled;
    }

    private void removeNonTransitivePatterns()
    {
        Iterator<ConcurrentPattern> iterator = this.concurrentPatterns.iterator();
        while(iterator.hasNext())
        {
            ConcurrentPattern concurrentPattern = iterator.next();
            boolean isATransitivePattern=false;
            for(IntHashSet concurrentLabels : this.transitiveConcurrencies)
            {
                if(concurrentPattern.getConcurrentLabels().containsAll(concurrentLabels))
                {
                    isATransitivePattern=true;
                    break;
                }
            }
            if(!isATransitivePattern) iterator.remove();
        }
    }

    public Automaton getDAFSA() throws Exception
    {
        if(this.dafsa==null)
            buildDAFSA();
        return this.dafsa;
    }

    private void buildDAFSA() throws Exception
    {
        ImportEventLog importer = new ImportEventLog(inverseLabelMapping,caseTracesMapping, caseIDs);
        this. dafsa = importer.createDAFSAfromDecodedTraces(this.representativeTraces);
    }

    public void toDot(PrintWriter pw) throws IOException {
        pw.println("digraph fsm {");
        pw.println("rankdir=LR;");
        pw.println("node [shape=circle,style=filled, fillcolor=white]");

        for(int vertex : vertexLabelMapping.keySet().toArray())
        {
            pw.printf("%d [label=\"%d, label: %d\"];%n", vertex, vertex, vertexLabelMapping.get(vertex));
            IntHashSet vertexOutgoingArcs = outgoingArcs.get(vertex);
            if(vertexOutgoingArcs==null) continue;
            for(int outVertex : vertexOutgoingArcs.toArray())
            {
                pw.printf("%d -> %d;%n", vertex, outVertex);
            }
        }
        pw.println("}");
    }

    public void toDot(String fileName) throws IOException {
        PrintWriter pw = new PrintWriter(fileName);
        toDot(pw);
        pw.close();
    }

    public UnifiedSet<IntArrayList> representativeTraces()
    {
        return this.representativeTraces;
    }

    public Set<ConcurrentPattern> getConcurrentPatterns()
    {
        return concurrentPatterns;
    }

    public int getTraceCount()
    {
        return  this.traceCount;
    }

    public void addToTraceCount(int addToTraceCount)
    {
        this.traceCount+=addToTraceCount;
    }

    public IntArrayList getTrace()
    {
        return this.trace;
    }
}
