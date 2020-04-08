package au.qut.apromore.ScalableConformanceChecker;

import au.qut.apromore.automaton.*;
import au.qut.apromore.automaton.Transition;
import au.qut.apromore.psp.*;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;

import java.util.*;
import java.util.concurrent.Callable;

public class CheckConformanceCallable implements Callable<CheckConformanceCallable> {
    IntIntHashMap visited;
    int qStates = 1;
    int stateLimit;
    Automaton logAutomaton;
    Automaton modelAutomaton;
    PSP psp;
    IntIntHashMap finalConfiguration;
    boolean useVisited;
    IntArrayList traceLabels;
    AllSyncReplayResult result;


    public CheckConformanceCallable(PSP psp, Automaton logAutomaton, Automaton modelAutomaton, IntArrayList trace, IntIntHashMap finalConfiguration, int stateLimit, boolean useVisited) {
        this.psp = psp;
        this.logAutomaton = logAutomaton;
        this.modelAutomaton = modelAutomaton;
        this.traceLabels = trace;
        this.finalConfiguration = finalConfiguration;
        this.stateLimit = stateLimit;
        this.useVisited = useVisited;
    }

    @Override
    public CheckConformanceCallable call() {
        calculatePartiallySynchronizedPathWithLeastSkips();
        return this;
    }

    public void calculatePartiallySynchronizedPathWithLeastSkips()
    {
        double start = System.currentTimeMillis();
        PriorityQueue<Node> toBeVisited = new PriorityQueue<>(new NodeComparator());
        Node currentNode = psp.sourceNode();
        UnifiedSet<Node> potentialFinalNodes = new UnifiedSet<Node>();
        int minModelMoves = modelAutomaton.minNumberOfModelMoves();
        if(minModelMoves==0)minModelMoves=Integer.MAX_VALUE;
        double actMin = (double) (finalConfiguration.sum() + minModelMoves);
        Node potentialNode;
        int sizeMoveOnLog;
        double numStates = 1;
        qStates =1;
        visited =new

        IntIntHashMap();
        this.offerPotentialNodeWithPruning(currentNode, actMin, toBeVisited);
        while(true)
        {
            if (toBeVisited.isEmpty()) break;
            currentNode = toBeVisited.poll();
            /*if (currentNode == null && useVisited) {
                useVisited = false;
                calculatePartiallySynchronizedPathWithLeastSkips();
                return;
            }*/
            numStates++;
            if (useVisited)
                if (visited.containsKey(currentNode.hashCode())) {
                    if (visited.get(currentNode.hashCode()) <= currentNode.weight()) {
                        continue;
                    }
                }
            visited.put(currentNode.hashCode(), (int) currentNode.weight());
            if (numStates == stateLimit) break;
            if (qStates != stateLimit)
                if (currentNode.tracePosition!=traceLabels.size())// && visited.size()<=stateLimit)
                {
                    int expTraceLabel = traceLabels.get(currentNode.tracePosition);
                    for (Transition tlog : currentNode.stLog().outgoingTransitions())
                        if (tlog.eventID() == expTraceLabel) {
                            //see if match is possible
                            for (Transition tmodel : currentNode.stModel().outgoingTransitions())
                                if (tlog.eventID() == tmodel.eventID()) {
                                    potentialNode = initializePotentialNode(currentNode, tlog, tmodel, Configuration.Operation.MATCH, finalConfiguration, false, traceLabels);
                                    this.offerPotentialNodeWithPruning(potentialNode, actMin, toBeVisited);
                                }
                            //consider LHIDE
                            potentialNode = initializePotentialNode(currentNode, tlog, null, Configuration.Operation.LHIDE, finalConfiguration, false, traceLabels);
                            this.offerPotentialNodeWithPruning(potentialNode, actMin, toBeVisited);
                        }
                }

            if (qStates != stateLimit)
                for (Transition tmodel : currentNode.stModel().outgoingTransitions()) {
                    potentialNode = initializePotentialNode(currentNode, null, tmodel, Configuration.Operation.RHIDE, finalConfiguration, false, traceLabels);
                    this.offerPotentialNodeWithPruning(potentialNode, actMin, toBeVisited);
                }

            if (
                    currentNode.stLog().isFinal()
                            && currentNode.stModel().isFinal()
                            && currentNode.tracePosition == traceLabels.size()
                ) {
            if (currentNode.weight() < actMin) {
                potentialFinalNodes = new UnifiedSet<Node>();
                actMin = currentNode.weight();
            }
            potentialFinalNodes.add(currentNode);
//				potentialFinalNodes.offer(currentNode);
            actMin = potentialFinalNodes.getFirst().weight();
            //break;
        }
        if (toBeVisited.isEmpty()) break; //System.out.println("Screw you!");
            //if(actMin < toBeVisited.min().getPriority()) break;
        else if (toBeVisited.peek().weight() > actMin) break;
    }

    double end = System.currentTimeMillis();
    List<List<Object>> lstNodeInstanceLst = new FastList<List<Object>>();
    List<List<StepTypes>> lstStepTypesLst = new FastList<List<StepTypes>>();
    DoubleArrayList moveLogFitness = new DoubleArrayList();
    DoubleArrayList moveModelFitness = new DoubleArrayList();
    if(!potentialFinalNodes.isEmpty())
    {
        //report results
        Node potentialFinalNode = null;
        potentialFinalNode = potentialFinalNodes.iterator().next();
        List<Object> nodeInstanceLst = new FastList<Object>();
        List<StepTypes> stepTypesLst = new FastList<StepTypes>();
        lstNodeInstanceLst.add(nodeInstanceLst);
        lstStepTypesLst.add(stepTypesLst);
        double movesOnLog = 0;
        double movesMatching = 0;
        double moveModel = 0;
        double moveInvi = 0;
        for (au.qut.apromore.psp.Synchronization tr : potentialFinalNode.configuration().sequenceSynchronizations()) {
            if (tr.operation() == Configuration.Operation.MATCH) {
                nodeInstanceLst.add(logAutomaton.eventLabels().get(tr.eventLog()));
                stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.LMGOOD);
                movesMatching++;
            } else if (tr.operation() == Configuration.Operation.LHIDE) {
                nodeInstanceLst.add(logAutomaton.eventLabels().get(tr.eventLog()));
                stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.L);
                movesOnLog++;
            } else {
                nodeInstanceLst.add(modelAutomaton.eventLabels().get(tr.eventModel()));
                if (tr.eventModel() == modelAutomaton.skipEvent()) {
                    stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.MINVI);
                    moveInvi++;
                } else {
                    stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.MREAL);
                    moveModel++;
                }
            }
        }
        moveModelFitness.add(1 - moveModel / (moveModel + movesMatching));
        moveLogFitness.add(1 - movesOnLog / (movesOnLog + movesMatching + moveInvi));
        this.insertPartiallySynchronizedPathIntoPSP(finalConfiguration, traceLabels, potentialFinalNode);
    }

    AllSyncReplayResult result = new AllSyncReplayResult(lstNodeInstanceLst, lstStepTypesLst, -1, !potentialFinalNodes.isEmpty());
        result.getTraceIndex().

    remove(-1);

    Integer[] relevantTraces = ArrayUtils.toObject(logAutomaton.caseTracesMapping.get(traceLabels).toArray());
        result.getTraceIndex().

    addAll(Arrays .<Integer>asList(relevantTraces));
        result.addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS,(double)result.getStepTypesLst().

    size());
        result.addInfo(PNMatchInstancesRepResult.RAWFITNESSCOST,(double)actMin);
        result.addInfo(PNMatchInstancesRepResult.NUMSTATES,(double)numStates);
        result.addInfo(PNMatchInstancesRepResult.QUEUEDSTATE,(double)numStates +toBeVisited.size());
        result.addInfo(PNMatchInstancesRepResult.ORIGTRACELENGTH,(double)traceLabels.size());
        result.addInfo(PNMatchInstancesRepResult.TIME,(double)(end -start));
        result.addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS,(double)lstNodeInstanceLst.size());
        result.addInfo(PNMatchInstancesRepResult.TRACEFITNESS, (double) 1-result.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST) / (result.getInfo().get(PNMatchInstancesRepResult.ORIGTRACELENGTH) + modelAutomaton.minNumberOfModelMoves()));
        if(!moveLogFitness.isEmpty())
                result.addInfo(PNRepResult.MOVELOGFITNESS,moveLogFitness.average());
        if(!moveModelFitness.isEmpty())
                result.addInfo(PNRepResult.MOVEMODELFITNESS,moveModelFitness.average());
        this.result = result;
    }

    private void insertPartiallySynchronizedPathIntoPSP(IntIntHashMap finalConfiguration, IntArrayList traceLabels, Node finalNode)
    {
        //Set<Node> commutativeFinalNodes = null;
        //if((commutativeFinalNodes = psp.commutativePaths().get(traceLabels))==null)
        //{
        //    commutativeFinalNodes = new UnifiedSet<Node>();
        //    psp.commutativePaths().put(traceLabels, commutati
        // veFinalNodes);
        //}
        //commutativeFinalNodes.add(finalNode);

        Node currentNode = psp.sourceNode();
        Node potentialNode = currentNode;
        for(au.qut.apromore.psp.Synchronization transition : finalNode.configuration().sequenceSynchronizations())
        {
            if(transition.operation() == Configuration.Operation.MATCH )
            {
                for(Transition tlog : currentNode.stLog().outgoingTransitions())
                {
                    for(Transition tmodel : currentNode.stModel().outgoingTransitions())
                    {
                        if(tlog.eventID() == transition.eventLog()
                                && tmodel.eventID() == transition.eventModel()
                                && tlog.target().id() == transition.targetLog()
                                && tmodel.target().id() == transition.targetModel())
                        {
                            potentialNode = initializePotentialNode(currentNode, tlog, tmodel, Configuration.Operation.MATCH, finalConfiguration, true, traceLabels);
                            break;
                        }
                    }
                    if(!(potentialNode.stateLogID() == currentNode.stateLogID())) break;
                }
            }
            else if (transition.operation() == Configuration.Operation.LHIDE)
            {
                for(Transition tlog : currentNode.stLog().outgoingTransitions())
                {
                    if(tlog.eventID() == transition.eventLog() && tlog.target().id() == transition.targetLog())
                    {
                        potentialNode = initializePotentialNode(currentNode, tlog, null, Configuration.Operation.LHIDE, finalConfiguration, true, traceLabels); //, pw);
                        break;
                    }
                }
            }
            else
            {
                for(Transition tmodel : currentNode.stModel().outgoingTransitions())
                {
                    if(tmodel.eventID() == transition.eventModel() && tmodel.target().id() == transition.targetModel())
                    {
                        potentialNode = initializePotentialNode(currentNode, null, tmodel, Configuration.Operation.RHIDE, finalConfiguration, true, traceLabels); //, pw);
                        break;
                    }
                }
            }
            /*if(!(potentialNode.stLog().isSource()) && potentialNode.stLog().outgoingTransitions().size()>1)
            {
                if(prefixMemorizationTable==null)
                    prefixMemorizationTable = new UnifiedMap<IntArrayList, Set<Node>>();
                Set<Node> relevantPrefix = null;
                if((relevantPrefix = prefixMemorizationTable.get(potentialNode.configuration().moveOnLog())) == null)
                {
                    relevantPrefix = new HashSet<Node>();
                    prefixMemorizationTable.put(potentialNode.configuration().moveOnLog(), relevantPrefix);
                }
                relevantPrefix.add(potentialNode);
            }
            if(!(potentialNode.stLog().isFinal()) && potentialNode.stLog().incomingTransitions().size()>1)
            {
                if(suffixMemorizationTable==null)
                    suffixMemorizationTable = new IntObjectHashMap<Set<Configuration>>();
                Set<Configuration> relevantConfiguration = null;
                if((relevantConfiguration = suffixMemorizationTable.get(potentialNode.hashCode()))==null)
                {
                    relevantConfiguration = new UnifiedSet<Configuration>();
                    suffixMemorizationTable.put(potentialNode.hashCode(), relevantConfiguration);
                }
                relevantConfiguration.add(finalNode.configuration().calculateSuffixFrom(potentialNode.configuration()));
            }*/
            currentNode = potentialNode;
        }

        finalNode.isFinal(true);
        //psp.finalNodes().add(finalNode);
    }

    private Node initializePotentialNode(Node currentNode, Transition tlog, Transition tmodel, Configuration.Operation operation,
                                         IntIntHashMap finalConfiguration, boolean insertToPSP, IntArrayList trace) //, PrintWriter pw)
    {
        Configuration potentialConfiguration;
        Node potentialNode;
        if(operation == Configuration.Operation.MATCH)
        {
            potentialConfiguration = currentNode.configuration().cloneConfiguration();
            //potentialConfiguration.moveOnLog().add(tlog.eventID());
            //potentialConfiguration.moveOnModel().add(tmodel.eventID());
            //potentialConfiguration.moveMatching().add(new Couple<Integer, Integer>(tlog.eventID(), tmodel.eventID()));
            //potentialConfiguration.setMoveOnLog().addToValue(tlog.eventID(), 1);
            //potentialConfiguration.setMoveOnModel().addToValue(tmodel.eventID(), 1);
            potentialConfiguration.sequenceSynchronizations().add(new au.qut.apromore.psp.Synchronization(Configuration.Operation.MATCH, tlog.eventID(), tmodel.eventID(), tlog.target().id(), tmodel.target().id(), currentNode.hashCode()));
            //potentialConfiguration.logIDs().add(tlog.target().id());
            //potentialConfiguration.modelIDs().add(tmodel.target().id());
            if(insertToPSP)
                potentialNode = new Node(tlog.target(), tmodel.target(), potentialConfiguration, currentNode.tracePosition + 1, currentNode.weight());
            else
                potentialNode = new Node(tlog.target(), tmodel.target(), potentialConfiguration, currentNode.tracePosition + 1, currentNode.weight());
        }
        else if (operation == Configuration.Operation.LHIDE)
        {
            potentialConfiguration = currentNode.configuration().cloneConfiguration();
            //potentialConfiguration.moveOnLog().add(tlog.eventID());
            //potentialConfiguration.setMoveOnLog().addToValue(tlog.eventID(), 1);
            //potentialConfiguration.adjustSetMoveOnLog();
            potentialConfiguration.sequenceSynchronizations().add(new au.qut.apromore.psp.Synchronization(Configuration.Operation.LHIDE, tlog.eventID(), -1, tlog.target().id(), -1, currentNode.hashCode()));
            //potentialConfiguration.logIDs().add(tlog.target().id());
            if(insertToPSP)
                potentialNode = new Node(tlog.target(), currentNode.stModel(), potentialConfiguration, currentNode.tracePosition + 1, currentNode.weight()+1);
            else
                potentialNode = new Node(tlog.target(), currentNode.stModel(), potentialConfiguration, currentNode.tracePosition + 1, currentNode.weight()+1);
        } else
        {
            potentialConfiguration = currentNode.configuration().cloneConfiguration();
            //potentialConfiguration.moveOnModel().add(tmodel.eventID());
            //potentialConfiguration.setMoveOnModel().addToValue(tmodel.eventID(), 1);
            //potentialConfiguration.adjustSetMoveOnModel();
            potentialConfiguration.sequenceSynchronizations().add(new au.qut.apromore.psp.Synchronization(Configuration.Operation.RHIDE, -1, tmodel.eventID(), -1, tmodel.target().id(), currentNode.hashCode()));
            //potentialConfiguration.modelIDs().add(tmodel.target().id());
            if(insertToPSP)
                potentialNode = new Node(currentNode.stLog(), tmodel.target(), potentialConfiguration, currentNode.tracePosition,currentNode.weight()+1);
            else
                potentialNode = new Node(currentNode.stLog(), tmodel.target(), potentialConfiguration, currentNode.tracePosition,currentNode.weight()+1);
        }

		/*if(insertToPSP)
		{
			Arc potentialArc;
			*//*if(psp.nodes().containsValue(potentialNode))
			{
				potentialNode = psp.nodes().get(potentialNode.hashCode());
			}
			else*//*
			psp.nodes().put(potentialNode.hashCode(), potentialNode);

			if(operation == Configuration.Operation.RHIDE)
				potentialArc = new Arc(new au.qut.apromore.psp.Synchronization(Configuration.Operation.RHIDE, -1, tmodel.eventID(), -1, tmodel.target().id(), currentNode.hashCode()), currentNode, potentialNode);
			else if(operation==Configuration.Operation.LHIDE)
				potentialArc = new Arc(new au.qut.apromore.psp.Synchronization(operation, tlog.eventID(), -1, tlog.target().id(),-1, currentNode.hashCode()), currentNode, potentialNode);
			else
				potentialArc = new Arc(new au.qut.apromore.psp.Synchronization(operation, tlog.eventID(), tmodel.eventID(), tlog.target().id(),tmodel.target().id(), currentNode.hashCode()), currentNode, potentialNode);
			if(currentNode.outgoingArcs().add(potentialArc))
				psp.arcs().add(potentialArc);
		}*/
        return potentialNode;
    }

    private void offerPotentialNodeWithPruning(Node potentialNode, double actMin, Queue<Node> toBeVisited)
    {
        if(visited.containsKey(potentialNode.hashCode()))
        {
            if(visited.get(potentialNode.hashCode()) <= potentialNode.weight()) return;
        }
        qStates++;
        toBeVisited.offer(potentialNode);
    }

    private double calculateCost(Configuration configuration, IntIntHashMap finalConfiguration, State stLog, State stModel, IntArrayList trace) //, PrintWriter pw)//, int stateLogID, int stateModelID, int finalState)
    {
        int futureCost = futureCost(configuration, finalConfiguration, stLog, stModel, trace);
        if(futureCost==Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        return currentCost(configuration, finalConfiguration) + futureCost;
    }

    private double currentCost(Configuration configuration, IntIntHashMap finalConfiguration) //, PrintWriter pw)
    {
        double currentCost = 0;
        for(Synchronization sync : configuration.sequenceSynchronizations())
        {
            if((sync.operation()==Configuration.Operation.RHIDE || sync.operation()== Configuration.Operation.LHIDE) && sync.eventModel()!=this.modelAutomaton.skipEvent())
            {
                currentCost +=1;
            }
        }
        return currentCost;
        //IntArrayList moveOnModelWithoutTau = new IntArrayList();
        //moveOnModelWithoutTau.addAll(configuration.moveOnModel());
        //moveOnModelWithoutTau.removeAll(modelAutomaton.skipEvent());
        //return configuration.moveOnLog().size() + moveOnModelWithoutTau.size() - 2 * configuration.moveMatching().size();// + finalConfigurationViolations.size();
    }

    //TO: Store heuristics values to speed up!
    private int futureCost(Configuration configuration, IntIntHashMap finalConfigurationLog, State stLog, State stModel, IntArrayList trace)
    {
        return 0;
    }
}
