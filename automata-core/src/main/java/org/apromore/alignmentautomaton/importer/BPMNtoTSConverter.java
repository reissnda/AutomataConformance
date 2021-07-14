package org.apromore.alignmentautomaton.importer;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.models.graphbased.directed.transitionsystem.ReachabilityGraph;

/**
 * @author Volodymyr Leno,
 * @version 1.0, 09.06.2021
 */

public class BPMNtoTSConverter {
    Queue<BitSet> toBeVisited;
    LinkedHashMap<Integer, Flow> labeledFlows;
    LinkedHashMap<Flow, Integer> invertedLabeledFlows;
    LinkedHashMap<BPMNNode, LinkedHashSet<BitSet>> bitMasks;

    LinkedHashMap<BPMNNode, LinkedHashMap<Integer, BitSet>> orJoinGatewayBitMasks;
    LinkedHashSet<BPMNNode> orJoins;
    LinkedHashSet<BPMNNode> orSplits;
    LinkedHashMap<BPMNNode, List<BPMNNode>> structuralConflicts;

    ReachabilityGraph rg;
    BPMNDiagram diagram;


    public ReachabilityGraph BPMNtoTS(BPMNDiagram diagram){
        this.diagram = diagram;

        structuralConflicts = getStructuralConflicts();

        rg = new ReachabilityGraph("");
        toBeVisited = new LinkedList<>();
        labeledFlows = labelFlows(diagram.getFlows());
        invertedLabeledFlows = new LinkedHashMap<>(labeledFlows.entrySet().stream().
                collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)));

        orJoinGatewayBitMasks = new LinkedHashMap<>();
        orJoins = new LinkedHashSet<>();
        orSplits = new LinkedHashSet<>();

        bitMasks = computeBitMasks();

        getInitialMarking();
        var next = toBeVisited.poll();
        while(next != null){
            visit(next);
            next = toBeVisited.poll();
        }

        return rg;
    }

    private void getInitialMarking(){
        for(Map.Entry<Integer, Flow> flow: labeledFlows.entrySet()){
            var value = flow.getValue();
            if(isStart(value.getSource())){
                BitSet marking = new BitSet();
                marking.set(flow.getKey());
                toBeVisited.add(marking);
                rg.addState(marking);
            }
        }
    }

    private LinkedHashMap<BPMNNode, LinkedHashSet<BitSet>> computeBitMasks(){
        LinkedHashMap<BPMNNode, LinkedHashSet<BitSet>> bitMasks = new LinkedHashMap<>();
        var nodes = diagram.getNodes();
        for(var node: nodes){
            if(!isORGateway(node)){
                bitMasks.put(node, computeBitMask(node));
            }
            else{
                if(diagram.getInEdges(node).size() > 1){
                    orJoins.add(node);
                    computeOrJoinGatewayBitMask(node);
                }
                else{
                    orSplits.add(node);
                    bitMasks.put(node, computeBitMask(node));
                }
            }
        }

        return bitMasks;
    }

    private LinkedHashSet<BitSet> computeBitMask(BPMNNode node){
        LinkedHashSet<BitSet> bitMask = new LinkedHashSet<>();

        var inEdges = diagram.getInEdges(node);
        if(inEdges.size() > 1){
            if(isANDGateway(node)){
                BitSet mask = new BitSet(labeledFlows.size());
                for(var flow: inEdges)
                    mask.set(invertedLabeledFlows.get(flow));

                bitMask.add(mask);
            }
            else if(isXORGateway(node)){
                for(var flow: inEdges){
                    BitSet mask = new BitSet(labeledFlows.size());
                    mask.set(invertedLabeledFlows.get(flow));
                    bitMask.add(mask);
                }
            }
        }
        else if(inEdges.size() > 0) {
            BitSet mask = new BitSet(labeledFlows.size());
            mask.set(invertedLabeledFlows.get(inEdges.toArray()[0]));
            bitMask.add(mask);
        }
        return bitMask;
    }

    private void computeOrJoinGatewayBitMask(BPMNNode node){
        LinkedHashMap<Integer, BitSet> mask = new LinkedHashMap<>();

        var inEdges = diagram.getInEdges(node);
        for(var edge: inEdges){
            BitSet m = new BitSet(labeledFlows.size());
            int idx = invertedLabeledFlows.get(edge);

            List<Integer> alreadyVisited = new ArrayList<>();
            Queue<Integer> toBeVisited = new LinkedList<>();
            toBeVisited.add(idx);

            var prev = toBeVisited.poll();

            while(prev != null){
                m.set(prev);
                alreadyVisited.add(prev);
                var source = labeledFlows.get(prev).getSource();
                if(!isStart(source) && !source.equals(node)){
                //if((!isORGateway(source) || diagram.getInEdges(source).size() == 1) && !isStart(source)){
                    for(var flow: diagram.getInEdges(source)){
                        int i = invertedLabeledFlows.get(flow);
                        if(!alreadyVisited.contains(i)){
                            toBeVisited.add(invertedLabeledFlows.get(flow));
                        }
                    }
                }

                prev = toBeVisited.poll();
            }

            mask.put(idx, m);
        }

        orJoinGatewayBitMasks.put(node, mask);
    }

    private LinkedHashMap<Integer, Flow> labelFlows(Collection<Flow> flows){
        LinkedHashMap<Integer, Flow> labeledFlows = new LinkedHashMap<>();
        int i = 0;
        for(Flow flow: flows)
            labeledFlows.put(i++, flow);

        return labeledFlows;
    }

    private void visit(BitSet activeMarking){
        var enabledElements = enabledElements(activeMarking);
        for(var element: enabledElements)
            if(!isEnd(element))
                fire(activeMarking, element);
    }

    private LinkedHashSet<BPMNNode> enabledElements(BitSet activeMarking){
        LinkedHashSet<BPMNNode> enabledElements = new LinkedHashSet<>();

        for (int i = activeMarking.nextSetBit(0); i >= 0; i = activeMarking.nextSetBit(i+1)) {
            var target = labeledFlows.get(i).getTarget();
            if(enabled(activeMarking, target))
                enabledElements.add(target);
            if (i == Integer.MAX_VALUE) {
                break;
            }
        }
        return enabledElements;
    }

    private boolean enabled(BitSet activeMarking, BPMNNode node){
        if(!orJoins.contains(node)){
            var bitMask = bitMasks.get(node);
            if(bitMask.size() > 1){
                for(BitSet mask: bitMask){
                    BitSet m = (BitSet) activeMarking.clone();
                    m.and(mask);
                    if(m.equals(mask))
                        return true;
                }
                return false;
            }
            else{
                BitSet m = (BitSet) activeMarking.clone();
                BitSet mask = (BitSet) bitMask.toArray()[0];
                m.and(mask);
                return m.equals(mask);
            }
        }
        else{
            if(isPartiallyEnabled(activeMarking, node)){
                var mask = orJoinGatewayBitMasks.get(node);
                for(int key: mask.keySet()){
                    if(!activeMarking.get(key)){
                        BitSet m = (BitSet) activeMarking.clone();
                        m.and(mask.get(key));
                        if(m.cardinality() > 0) {

                            if(!structuralConflicts.containsKey(node))
                                return false;
                            else{
                                int dest = invertedLabeledFlows.get(this.diagram.getOutEdges(node).iterator().next());
                                var conflictingGateways = structuralConflicts.get(node);

                                for(var gateway: conflictingGateways){
                                    if(isPartiallyEnabled(activeMarking, gateway)){

                                        for(int incomingFlow: orJoinGatewayBitMasks.get(gateway).keySet()){
                                            if(!activeMarking.get(incomingFlow)){
                                                var pathMasks = getPaths(incomingFlow, dest);
                                                for(var pathMask: pathMasks){
                                                    m = (BitSet) activeMarking.clone();
                                                    m.and(pathMask);
                                                    if(m.cardinality() == 0)
                                                        return true;
                                                }
                                            }
                                        }
                                    }
                                    else
                                        return false;
                                }
                                return false;
                            }
                        }
                    }
                }
            }

            return true;
        }
    }

    private boolean isPartiallyEnabled(BitSet activeMarking, BPMNNode node){
        var mask = orJoinGatewayBitMasks.get(node);
        for(int key: mask.keySet()){
            if(activeMarking.get(key))
                return true;
        }
        return false;
    }

    private void fire(BitSet activeMarking, BPMNNode node){
        List<Integer> newFlows = new ArrayList<>();
        var outEdges = diagram.getOutEdges(node);

        for(var flow: outEdges)
            newFlows.add(invertedLabeledFlows.get(flow));

        List<Integer> oldFlows = new ArrayList<>();
        var inEdges = diagram.getInEdges(node);

        for(var flow: inEdges)
            oldFlows.add(invertedLabeledFlows.get(flow));

        if(orSplits.contains(node)){
            var combinations = getAllCombinations(newFlows);
            for(var combination: combinations){
                BitSet newMarking = (BitSet) activeMarking.clone();
                for(int idx: combination)
                    newMarking.set(idx);
                updateReachabilityGraph(activeMarking, node, newMarking, oldFlows, true);
            }
        }
        else if(!isXORGateway(node)){
            BitSet newMarking = (BitSet) activeMarking.clone();

            for(int idx: newFlows)
                newMarking.set(idx);

            updateReachabilityGraph(activeMarking, node, newMarking, oldFlows, !isActivity(node));
        }
        else {
            for(int idx: newFlows){
                BitSet newMarking = (BitSet) activeMarking.clone();
                newMarking.set(idx);
                updateReachabilityGraph(activeMarking, node, newMarking, oldFlows, true);
            }
        }
    }

    private List<List<Integer>> getAllCombinations(List<Integer> flows){
        List<List<Integer>> combinations = new ArrayList<>();

        int n = flows.size();

        for(int i = 0; i < (1<<n); i++) {
            List<Integer> combination = new ArrayList<>();
            for(int j = 0; j < n; j++){
                if((i & (1<<j)) > 0)
                    combination.add(flows.get(j));
            }
            combinations.add(combination);
        }

        combinations.remove(0);
        return combinations;
    }

    private void updateReachabilityGraph(BitSet activeMarking, BPMNNode node, BitSet newMarking,
                                         List<Integer> oldFlows, Boolean invisibleTransition) {
        for(int jdx: oldFlows)
            newMarking.set(jdx, false);

        if(newMarking.cardinality() > 0){
            if(!rg.getStates().contains(newMarking)){
                rg.addState(newMarking);
                toBeVisited.add(newMarking);
            }

            if(invisibleTransition){
                rg.addTransition(activeMarking, newMarking, node);

                //String label = ((Gateway) rg.findTransition(activeMarking, newMarking, node).getIdentifier()).getId().toString();

                String label = "gateway " + ((Gateway) rg.findTransition(activeMarking, newMarking, node).getIdentifier()).getAttributeMap().get("Original id").toString();
                rg.findTransition(activeMarking, newMarking, node).setLabel(label);
            }
            else
                rg.addTransition(activeMarking, newMarking, node);
        }
    }

    private HashMap<BPMNNode, BitSet> computeFullEnablement(){
        HashMap<BPMNNode, BitSet> fullEnablements = new HashMap<>();
        for(var orJoin: orJoins){
            BitSet fullEnablement = new BitSet(labeledFlows.size());
            for(var edge: this.diagram.getInEdges(orJoin))
                fullEnablement.set(this.invertedLabeledFlows.get(edge));
            fullEnablements.put(orJoin, fullEnablement);
        }
        return fullEnablements;
    }


    private void DFS(int[][] adjacencyMatrix, int v, boolean[] visited, List<Integer> comp){
        visited[v] = true;
        for(int i = 0; i < adjacencyMatrix[v].length; i++)
            if(adjacencyMatrix[v][i] > 0 && !visited[i])
                DFS(adjacencyMatrix, i, visited, comp);
        comp.add(v);
    }

    private List<Integer> fillOrder(int[][] adjacencyMatrix, boolean[] visited)
    {
        int V = adjacencyMatrix.length;
        List<Integer> order = new ArrayList<>();

        for (int i = 0; i < V; i++)
            if (!visited[i])
                DFS(adjacencyMatrix, i, visited, order);
        return order;
    }

    public int[][] transposeAdjacencyMatrix(int[][] adj){
        int[][] transposedMatrix = new int[adj.length][adj[0].length];
        for(int i = 0; i < adj.length; i++){
            for(int j = 0; j < adj[i].length; j++){
                transposedMatrix[i][j] = adj[j][i];
            }
        }
        return transposedMatrix;
    }

    public List<List<BPMNNode>> getSCComponents()
    {
        var nodes = new ArrayList<>(this.diagram.getNodes());
        int N = nodes.size();

        int[][] adjacencyMatrix = new int[N][N];
        for(int i = 0; i < N; i ++){
            var outEdges = this.diagram.getOutEdges(nodes.get(i));
            for(var edge: outEdges){
                adjacencyMatrix[i][nodes.indexOf(edge.getTarget())] = 1;
            }
        }

        boolean[] visited = new boolean[N];
        List<Integer> order = fillOrder(adjacencyMatrix, visited);
        int[][] transposedAdjacencyMatrix = transposeAdjacencyMatrix(adjacencyMatrix);
        visited = new boolean[N];
        Collections.reverse(order);

        List<List<Integer>> SCComp = new ArrayList<>();
        for (Integer anOrder : order) {
            int v = anOrder;
            if (!visited[v]) {
                List<Integer> comp = new ArrayList<>();
                DFS(transposedAdjacencyMatrix, v, visited, comp);
                SCComp.add(comp);
            }
        }

        List<List<BPMNNode>> sccs = new ArrayList<>();

        for(var scomp: SCComp){
            List<BPMNNode> scc = new ArrayList<>();
            for(var idx: scomp)
                scc.add(nodes.get(idx));
            sccs.add(scc);
        }

        return sccs;
    }

    private LinkedHashMap<BPMNNode, List<BPMNNode>> getStructuralConflicts(){
        LinkedHashMap<BPMNNode, List<BPMNNode>> structuralConflicts = new LinkedHashMap<>();

        var sccs = getSCComponents();

        List<BPMNNode> orJoinGateways = this.diagram.getGateways().stream().filter(el -> isORGateway(el) &&
                this.diagram.getInEdges(el).size() > 1).collect(Collectors.toList());

        for(var gateway: orJoinGateways){
            List<BPMNNode> gateways = new ArrayList<>();
            for(var scc: sccs)
                if(scc.contains(gateway)){
                    gateways.addAll(scc.stream().filter(el -> orJoinGateways.contains(el) &&
                            !el.equals(gateway)).collect(Collectors.toList()));
                    break;
                }

            if(gateways.size() > 0)
                structuralConflicts.put(gateway, gateways);
        }

        return structuralConflicts;
    }


    private List<BitSet> getPaths(int source, int destination){
        List<List<Integer>> paths = new ArrayList<>();
        List<BitSet> pathMasks = new ArrayList<>();

        boolean[] isVisited = new boolean[this.diagram.getFlows().size()];
        ArrayList<Integer> pathList = new ArrayList<>();

        // add source to path[]
        pathList.add(source);
        getAllPathsUtil(source, destination, isVisited, pathList, paths);

        for(var path: paths){
            BitSet pathMask = new BitSet(labeledFlows.size());
            for(int idx: path)
                pathMask.set(idx);
            pathMasks.add(pathMask);
        }

        return pathMasks;
    }

    private void getAllPathsUtil(int u, int d, boolean[] isVisited, List<Integer> localPathList, List<List<Integer>> globalPathList) {

        if (u == d) {
            globalPathList.add(new ArrayList<>(localPathList));
        }

        // Mark the current node
        isVisited[u] = true;

        // Recur for all the vertices
        // adjacent to current vertex

        var currentNode = labeledFlows.get(u).getSource();
        var adjacentFlows = this.diagram.getInEdges(currentNode).stream().map(el -> this.invertedLabeledFlows.get(el)).collect(Collectors.toList());

        for (Integer i : adjacentFlows) {
            if (!isVisited[i]) {
                // store current node
                // in path[]
                localPathList.add(i);
                getAllPathsUtil(i, d, isVisited, localPathList, globalPathList);

                // remove current node
                // in path[]
                localPathList.remove(i);
            }
        }

        // Mark the current node
        isVisited[u] = false;
    }


    private boolean isStart(BPMNNode node){
        return node instanceof Event &&
                ((Event) node).getEventType().name().equals("START");
    }

    private boolean isEnd(BPMNNode node){
        return  node instanceof Event &&
                ((Event) node).getEventType().name().equals("END");
    }

    private boolean isActivity(BPMNNode node){
        return node instanceof org.processmining.models.graphbased.directed.bpmn.elements.Activity;
    }

    private boolean isXORGateway(BPMNNode node){
        return node instanceof Gateway &&
                ((Gateway) node).getGatewayType().name().equals("DATABASED");
    }

    private boolean isANDGateway(BPMNNode node){
        return node instanceof Gateway &&
                ((Gateway) node).getGatewayType().name().equals("PARALLEL");
    }

    private boolean isORGateway(BPMNNode node){
        return node instanceof Gateway &&
                ((Gateway) node).getGatewayType().name().equals("INCLUSIVE");
    }
}