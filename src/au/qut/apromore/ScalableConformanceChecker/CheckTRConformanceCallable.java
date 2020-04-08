package au.qut.apromore.ScalableConformanceChecker;

import au.qut.apromore.automaton.Automaton;
import au.qut.apromore.automaton.Transition;
import au.qut.apromore.importer.DecodeTandemRepeats;
import au.qut.apromore.psp.Configuration;
import au.qut.apromore.psp.Couple;
import au.qut.apromore.psp.Node;
import au.qut.apromore.psp.PSP;
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

public class CheckTRConformanceCallable implements Callable<CheckTRConformanceCallable>
{
    IntIntHashMap visited;
    int qStates = 1;
    int stateLimit;
    Automaton logAutomaton;
    Automaton modelAutomaton;
    PSP psp;
    IntIntHashMap finalConfiguration;
    boolean useVisited;
    IntArrayList reducedTrace;
    UnifiedMap<DecodeTandemRepeats, AllSyncReplayResult> results = new UnifiedMap<>();
    int fitnessProblemsSolved=0;

    public CheckTRConformanceCallable(PSP psp, Automaton logAutomaton, Automaton modelAutomaton, IntIntHashMap finalConfiguration, IntArrayList reducedTrace, int stateLimit, boolean useVisited)
    {
        this.psp = psp;
        this.logAutomaton = logAutomaton;
        this.modelAutomaton = modelAutomaton;
        this.finalConfiguration = finalConfiguration;
        this.reducedTrace = reducedTrace;
        this.stateLimit = stateLimit;
        this.useVisited = useVisited;
    }

    @Override
    public CheckTRConformanceCallable call() {
        int min, max;
        double middle;
        int[] keySet = logAutomaton.reductions.get(reducedTrace).keySet().toArray();
        Arrays.sort(keySet);
        Couple<Integer, Integer> interval = new Couple<>(0, keySet.length - 1);
        IntObjectHashMap<ReducedResult> results = new IntObjectHashMap<>();
        ReducedResult minResult, maxResult;
        FastList<Couple<Integer, Integer>> listIntervals = new FastList<>();
        listIntervals.add(interval);
        while (!listIntervals.isEmpty()) {
            interval = listIntervals.remove(0);
            min = interval.getFirstElement();
            max = interval.getSecondElement();
            if ((minResult = results.get(min)) == null) {
                fitnessProblemsSolved++;
                minResult = this.calculateReducedAlignmentFor(finalConfiguration, logAutomaton.reductions.get(reducedTrace).get(keySet[min]).getFirst(), stateLimit);
                results.put(min, minResult);
            }
            if (min == max) {
                for (DecodeTandemRepeats decoder : logAutomaton.reductions.get(reducedTrace).get(keySet[min]))
                    this.extendReducedAlignment(finalConfiguration, decoder, minResult);
                continue;
            }
            if ((maxResult = results.get(max)) == null) {
                fitnessProblemsSolved++;
                maxResult = this.calculateReducedAlignmentFor(finalConfiguration, logAutomaton.reductions.get(reducedTrace).get(keySet[max]).getFirst(), stateLimit);
                results.put(max, maxResult);
            }
            if (minResult.getFinalNode().configuration().equals(maxResult.getFinalNode().configuration())) {
                for (int pos = min; pos <= max; pos++)
                    for (DecodeTandemRepeats decoder : logAutomaton.reductions.get(reducedTrace).get(keySet[pos]))
                        this.extendReducedAlignment(finalConfiguration, decoder, minResult);
                continue;
            }
            else
            {
                middle = (min / 1.0 + max / 1.0) / 2;
                listIntervals.add(new Couple<>(min, (int) Math.floor(middle)));
                listIntervals.add(new Couple<>((int) Math.ceil(middle), max));
            }
        }
        return this;
    }

    private ReducedResult calculateReducedAlignmentFor(IntIntHashMap finalConfiguration, DecodeTandemRepeats decoder, int stateLimit)
    {
        double start = System.currentTimeMillis();
        PriorityQueue<Node> toBeVisited = new PriorityQueue<>(new NodeComparator());
        Node currentNode = psp.sourceNode();
        UnifiedSet<Node> potentialFinalNodes = new UnifiedSet<Node>();
        int minModelMoves = modelAutomaton.minNumberOfModelMoves();
        //if(minModelMoves==0) minModelMoves=Integer.MAX_VALUE;
        double actMin = (double) (decoder.trace().size() + minModelMoves);
        Node potentialNode;
        int sizeMoveOnLog;
        double numStates = 1;
        qStates = 1;
        visited = new IntIntHashMap();
        IntArrayList traceLabels = decoder.reducedTrace();
		/*Set<Node> prefixMemoization =  this.getPrefixMemoization(traceLabels);
		for(Node node : prefixMemoization)
		{
			potentialNode = this.cloneNodeForConfiguration(node, finalConfiguration, traceLabels);
			this.offerPotentialNodeWithPruning(potentialNode, actMin, toBeVisited);
		}*/
        this.offerPotentialNodeWithPruning(currentNode, actMin, toBeVisited);

        while(true)
        {
            if(toBeVisited.isEmpty()) break;
            currentNode = toBeVisited.poll();
            /*if(currentNode==null && useVisited)
            {
                useVisited=false;
                return calculateReducedAlignmentFor(finalConfiguration, decoder, stateLimit);
            }*/
            numStates++;
            if(useVisited)
                if (visited.containsKey(currentNode.hashCode()))
                {
                    if (visited.get(currentNode.hashCode()) <= currentNode.weight()) {
                        continue;
                    }
                }
            visited.put(currentNode.hashCode(), (int) currentNode.weight());
            if(numStates == stateLimit)
                break;
            if(qStates!=stateLimit)
                if(currentNode.tracePosition!=traceLabels.size())// && visited.size()<=stateLimit)
                {
                    int expTraceLabel = traceLabels.get(currentNode.tracePosition);
                    for(Transition tlog : currentNode.stLog().outgoingTransitions())
                        if(tlog.eventID()==expTraceLabel)
                        {
                            //see if match is possible
                            for(Transition tmodel : currentNode.stModel().outgoingTransitions())
                                if(tlog.eventID()==tmodel.eventID())
                                {
                                    potentialNode = initializePotentialNode(currentNode, tlog, tmodel, Configuration.Operation.MATCH, finalConfiguration, false, decoder);
                                    this.offerPotentialNodeWithPruning(potentialNode, actMin, toBeVisited);

                                }
                            //consider LHIDE
                            potentialNode = initializePotentialNode(currentNode, tlog, null, Configuration.Operation.LHIDE, finalConfiguration, false, decoder);
                            this.offerPotentialNodeWithPruning(potentialNode, actMin, toBeVisited);
                        }
                }

            if(qStates!=stateLimit)
                for(Transition tmodel : currentNode.stModel().outgoingTransitions())
                {
                    potentialNode = initializePotentialNode(currentNode, null, tmodel, Configuration.Operation.RHIDE, finalConfiguration, false, decoder);
                    this.offerPotentialNodeWithPruning(potentialNode, actMin, toBeVisited);
                }

            if(
                    currentNode.stLog().isFinal()
                            && currentNode.stModel().isFinal()
                            && currentNode.tracePosition==decoder.reducedTrace().size()
                    )
            {
                if(currentNode.weight()<actMin)
                {
                    potentialFinalNodes = new UnifiedSet<Node>();
                    actMin = currentNode.weight();
                }
                potentialFinalNodes.add(currentNode);
//				potentialFinalNodes.offer(currentNode);
                actMin = (double) potentialFinalNodes.getFirst().weight();
                //break;
            }
            if(toBeVisited.isEmpty()) break; //System.out.println("Screw you!");
                //if(actMin < toBeVisited.min().getPriority()) break;
            else if(toBeVisited.peek().weight() > actMin) break;
        }
        double end = System.currentTimeMillis();
        useVisited = true;
        if(potentialFinalNodes.isEmpty())
            System.out.println("Problem!");
        return new ReducedResult(potentialFinalNodes.getFirst(),numStates,numStates+toBeVisited.size(),end-start);
    }

    private void extendReducedAlignment(IntIntHashMap finalConfiguration, DecodeTandemRepeats decoder, ReducedResult res)
    {
        Node finalNode = res.getFinalNode();
        List<List<Object>> lstNodeInstanceLst = new FastList<List<Object>>();
        List<List<StepTypes>> lstStepTypesLst = new FastList<List<StepTypes>>();
        DoubleArrayList moveLogFitness = new DoubleArrayList();
        DoubleArrayList moveModelFitness = new DoubleArrayList();
        double movesOnLog = 0, lhideToExtend, lhideSecondPair;
        double moveModel = 0, rhideToExtend, rhideSecondPair;
        List<Object> nodeInstanceLst = new FastList<Object>(), lstToExtend, lstSecondPair;
        List<StepTypes> stepTypesLst = new FastList<StepTypes>(), lstToExtendStepTypes, lstSecondPairStepTypes;
        lstNodeInstanceLst.add(nodeInstanceLst);
        lstStepTypesLst.add(stepTypesLst);
        double movesMatching = 0, matchingToExtend, matchingSecondPair;
        double moveInvi = 0;
        int tracePos=0;
        au.qut.apromore.psp.Synchronization tr, tr2;
        int posToReduce = 0, posToExtend;
        int lengthTandemRepeat, reductionCollapsedLength, numberRepetitions;
        int posTandemRepeat, posAlignShift=0;
        for(int alignPos=0; alignPos < finalNode.configuration().sequenceSynchronizations().size(); alignPos++)
        {
            if(posToReduce<decoder.reductionStartPositions().size())
            {
                reductionCollapsedLength = decoder.reductionCollapsedLength().get(posToReduce);
                lengthTandemRepeat =  reductionCollapsedLength / 2;
                if(tracePos==decoder.reductionStartPositions().get(posToReduce)-1)
                {
                    tr = finalNode.configuration().sequenceSynchronizations().get(alignPos);
                    while (tr.eventLog() != decoder.reducedTrace().get(tracePos))
                    {
                        nodeInstanceLst.add(modelAutomaton.eventLabels().get(tr.eventModel()));
                        if (tr.eventModel() == modelAutomaton.skipEvent()) {
                            stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.MINVI);
                            moveInvi++;
                        } else {
                            stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.MREAL);
                            moveModel++;
                        }
                        alignPos++;
                        tr = finalNode.configuration().sequenceSynchronizations().get(alignPos);
                    }
                    numberRepetitions = decoder.reductionOriginalLength().get(posToReduce) / lengthTandemRepeat-2;
                    if(numberRepetitions==0)
                    {
                        tr = finalNode.configuration().sequenceSynchronizations().get(alignPos);
                        if(tr.operation() == Configuration.Operation.MATCH)
                        {
                            nodeInstanceLst.add(logAutomaton.eventLabels().get(tr.eventLog()));
                            stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.LMGOOD);
                            movesMatching++;
                            tracePos++;
                        }
                        else if(tr.operation() == Configuration.Operation.LHIDE)
                        {
                            nodeInstanceLst.add(logAutomaton.eventLabels().get(tr.eventLog()));
                            stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.L);
                            movesOnLog++;
                            tracePos++;
                        }
                        else
                        {
                            nodeInstanceLst.add(modelAutomaton.eventLabels().get(tr.eventModel()));
                            if(tr.eventModel()==modelAutomaton.skipEvent())
                            {
                                stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.MINVI);
                                moveInvi++;
                            }
                            else
                            {
                                stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.MREAL);
                                moveModel++;
                            }
                        }
                        posToReduce++;
                        continue;
                    }
                    lstToExtend = new FastList<>();
                    lstToExtendStepTypes = new FastList<>();
                    posTandemRepeat = 0;
                    posAlignShift = 0;
                    lhideToExtend=0; rhideToExtend=0; matchingToExtend=0;
                    while (posTandemRepeat <= lengthTandemRepeat) {
                        if (alignPos + posAlignShift == finalNode.configuration().sequenceSynchronizations().size())
                            break;
                        tr = finalNode.configuration().sequenceSynchronizations().get(alignPos + posAlignShift);
                        if (tr.operation() == Configuration.Operation.MATCH) {
                            posTandemRepeat++;
                            if (posTandemRepeat == lengthTandemRepeat + 1) break;
                            nodeInstanceLst.add(logAutomaton.eventLabels().get(tr.eventLog()));
                            stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.LMGOOD);
                            movesMatching++;
                            lstToExtend.add(logAutomaton.eventLabels().get(tr.eventLog()));
                            lstToExtendStepTypes.add(org.processmining.plugins.petrinet.replayresult.StepTypes.LMGOOD);
                            matchingToExtend++;
                        } else if (tr.operation() == Configuration.Operation.LHIDE) {
                            posTandemRepeat++;
                            if (posTandemRepeat == lengthTandemRepeat + 1) break;
                            nodeInstanceLst.add(logAutomaton.eventLabels().get(tr.eventLog()));
                            stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.L);
                            movesOnLog++;
                            lstToExtend.add(logAutomaton.eventLabels().get(tr.eventLog()));
                            lstToExtendStepTypes.add(org.processmining.plugins.petrinet.replayresult.StepTypes.L);
                            lhideToExtend++;
                        } else {
                            nodeInstanceLst.add(modelAutomaton.eventLabels().get(tr.eventModel()));
                            lstToExtend.add(modelAutomaton.eventLabels().get(tr.eventModel()));
                            if (tr.eventModel() == modelAutomaton.skipEvent()) {
                                stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.MINVI);
                                lstToExtendStepTypes.add(StepTypes.MINVI);
                                moveInvi++;
                            } else {
                                stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.MREAL);
                                lstToExtendStepTypes.add(StepTypes.MREAL);
                                moveModel++;
                                rhideToExtend++;
                            }
                        }
                        posAlignShift++;
                    }
                    alignPos+=posAlignShift;
                    posAlignShift=0;
                    posToExtend=0;
                    posTandemRepeat=0;
                    StepTypes curStep = lstToExtendStepTypes.get(posToExtend);
                    lstSecondPair = new FastList<>();
                    lstSecondPairStepTypes = new FastList<>();
                    matchingSecondPair=0; lhideSecondPair =0; rhideSecondPair=0;
                    boolean changesAllowed = lhideToExtend!=lengthTandemRepeat;
                    boolean matchADDED = false;
                    while(posTandemRepeat<lengthTandemRepeat)
                    {
                        if (alignPos + posAlignShift == finalNode.configuration().sequenceSynchronizations().size())
                            break;

                        tr=finalNode.configuration().sequenceSynchronizations().get(alignPos + posAlignShift);
                        if(tr.operation()==Configuration.Operation.RHIDE)
                        {
                            rhideSecondPair++;
                            lstSecondPair.add(modelAutomaton.eventLabels().get(tr.eventModel()));
                            lstSecondPairStepTypes.add(StepTypes.MREAL);
                        }
                        else if(tr.operation()==Configuration.Operation.MATCH)
                        {
                            if(changesAllowed && curStep==StepTypes.L && posTandemRepeat<lengthTandemRepeat-1)
                            {
                                lstToExtendStepTypes.set(posToExtend,StepTypes.LMGOOD);
                                lhideToExtend--;
                                matchingToExtend++;
                                matchADDED=true;
                            }
                            do
                            {
                                if(posToExtend<lstToExtendStepTypes.size()-1) {
                                    curStep = lstToExtendStepTypes.get(++posToExtend);
                                }
                            }
                            while(curStep==StepTypes.MREAL);
                            matchingSecondPair++;
                            lstSecondPair.add(logAutomaton.eventLabels().get(tr.eventLog()));
                            lstSecondPairStepTypes.add(StepTypes.LMGOOD);
                            posTandemRepeat++;
                        }
                        else
                        {
                            if(changesAllowed && (posToExtend==0||(posToExtend==lengthTandemRepeat-1 && matchADDED)) && curStep==StepTypes.LMGOOD)
                            {
                                lstToExtendStepTypes.set(posToExtend, StepTypes.L);
                                lhideToExtend++;
                                matchingToExtend--;
                            }
                            do
                            {
                                if(posToExtend<lstToExtendStepTypes.size()-1) {
                                    curStep = lstToExtendStepTypes.get(++posToExtend);
                                }
                            }
                            while(curStep==StepTypes.MREAL);
                            lhideSecondPair++;
                            lstSecondPair.add(logAutomaton.eventLabels().get(tr.eventLog()));
                            lstSecondPairStepTypes.add(StepTypes.L);
                            posTandemRepeat++;
                        }
                        posAlignShift++;
                    }
                    for(int itRepetitions = 0; itRepetitions < numberRepetitions; itRepetitions++)
                    {
                        nodeInstanceLst.addAll(lstToExtend);
                        stepTypesLst.addAll(lstToExtendStepTypes);
                        movesMatching+=matchingToExtend;
                        movesOnLog+=lhideToExtend;
                        moveModel+=rhideToExtend;
                    }
                    nodeInstanceLst.addAll(lstSecondPair);
                    stepTypesLst.addAll(lstSecondPairStepTypes);
                    movesMatching+=matchingSecondPair;
                    movesOnLog+=lhideSecondPair;
                    moveModel+=rhideSecondPair;
                    alignPos+=posAlignShift-1;
                    tracePos+=lengthTandemRepeat*2;
                    posToReduce++;

                    /*for (int itRepetitions = 0; itRepetitions < numberRepetitions; itRepetitions++) {
                        posTandemRepeat = 0;
                        posAlignShift = 0;
                        while (posTandemRepeat <= lengthTandemRepeat) {
                            if (alignPos + posAlignShift == finalNode.configuration().sequenceSynchronizations().size())
                                break;
                            tr = finalNode.configuration().sequenceSynchronizations().get(alignPos + posAlignShift);
                            if (tr.operation() == Configuration.Operation.MATCH) {

                                posTandemRepeat++;
                                if (posTandemRepeat == lengthTandemRepeat + 1) break;
                                nodeInstanceLst.add(logAutomaton.eventLabels().get(tr.eventLog()));
                                stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.LMGOOD);
                                movesMatching++;
                            } else if (tr.operation() == Configuration.Operation.LHIDE) {
                                posTandemRepeat++;
                                if (posTandemRepeat == lengthTandemRepeat + 1) break;
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
                            posAlignShift++;
                        }
                    }
                    tracePos += lengthTandemRepeat;
                    posToReduce++;
                    alignPos += posAlignShift - 1;*/
                }
                else
                {
                    tr = finalNode.configuration().sequenceSynchronizations().get(alignPos);
                    if(tr.operation() == Configuration.Operation.MATCH)
                    {
                        nodeInstanceLst.add(logAutomaton.eventLabels().get(tr.eventLog()));
                        stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.LMGOOD);
                        movesMatching++;
                        tracePos++;
                    }
                    else if(tr.operation() == Configuration.Operation.LHIDE)
                    {
                        nodeInstanceLst.add(logAutomaton.eventLabels().get(tr.eventLog()));
                        stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.L);
                        movesOnLog++;
                        tracePos++;
                    }
                    else
                    {
                        nodeInstanceLst.add(modelAutomaton.eventLabels().get(tr.eventModel()));
                        if(tr.eventModel()==modelAutomaton.skipEvent())
                        {
                            stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.MINVI);
                            moveInvi++;
                        }
                        else
                        {
                            stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.MREAL);
                            moveModel++;
                        }
                    }
                }
            }
            else
            {
                tr = finalNode.configuration().sequenceSynchronizations().get(alignPos);
                if(tr.operation() == Configuration.Operation.MATCH)
                {
                    nodeInstanceLst.add(logAutomaton.eventLabels().get(tr.eventLog()));
                    stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.LMGOOD);
                    movesMatching++;
                    tracePos++;
                }
                else if(tr.operation() == Configuration.Operation.LHIDE)
                {
                    nodeInstanceLst.add(logAutomaton.eventLabels().get(tr.eventLog()));
                    stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.L);
                    movesOnLog++;
                    tracePos++;
                }
                else
                {
                    nodeInstanceLst.add(modelAutomaton.eventLabels().get(tr.eventModel()));
                    if(tr.eventModel()==modelAutomaton.skipEvent())
                    {
                        stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.MINVI);
                        moveInvi++;
                    }
                    else
                    {
                        stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.MREAL);
                        moveModel++;
                    }
                }
            }
        }
        moveModelFitness.add(1-moveModel / (moveModel + movesMatching));
        moveLogFitness.add(1-movesOnLog / (movesOnLog + movesMatching + moveInvi));
        this.insertPartiallySynchronizedPathIntoPSP(finalConfiguration, decoder, finalNode);

        /*IntHashSet toBeVisited = new IntHashSet();
        IntHashSet next;
        toBeVisited.add(this.modelAutomaton.sourceID());
        boolean isReliable = true;
        //if(res.getNodeInstanceLst().get(0).size()==1) {
        //	if (((String) res.getNodeInstanceLst().get(0).get(0)).equals("INS_DIPLOMI_UNIV")) {
        //		System.out.print("");
        //	}
        //}
        for(int pos=0; pos < nodeInstanceLst.size(); pos++)
        {
            if(toBeVisited.isEmpty())
            {
                isReliable=false;
                break;
            }
            if(stepTypesLst.get(pos) == StepTypes.L) continue;
            int curEventID = this.modelAutomaton.inverseEventLabels().get(nodeInstanceLst.get(pos));
            next = new IntHashSet();
            for(int curNode : toBeVisited.toArray())
            {
                for(Transition tr1 : this.modelAutomaton.states().get(curNode).outgoingTransitions())
                {
                    if(tr1.eventID()==curEventID) next.add(tr1.target().id());
                }
            }
            toBeVisited = next;
        }*/

        AllSyncReplayResult result = new AllSyncReplayResult(lstNodeInstanceLst, lstStepTypesLst, -1, finalNode!=null);
        result.getTraceIndex().remove(-1);
        Integer[] relevantTraces = ArrayUtils.toObject(logAutomaton.caseTracesMapping.get(decoder.trace()).toArray());
        result.getTraceIndex().addAll(Arrays.<Integer>asList( relevantTraces));
        result.addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS, (double) result.getStepTypesLst().size());
        result.addInfo(PNMatchInstancesRepResult.RAWFITNESSCOST,  movesOnLog+moveModel);
        result.addInfo(PNMatchInstancesRepResult.NUMSTATES, res.getNumStates());
        result.addInfo(PNMatchInstancesRepResult.QUEUEDSTATE,res.getNumQueuedStates());
        result.addInfo(PNMatchInstancesRepResult.ORIGTRACELENGTH, (double) decoder.trace().size());
        result.addInfo(PNMatchInstancesRepResult.TIME, (double) res.getTime());
        result.addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS, (double) lstNodeInstanceLst.size());
        result.addInfo(PNMatchInstancesRepResult.TRACEFITNESS, (double) 1-result.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST) / (result.getInfo().get(PNMatchInstancesRepResult.ORIGTRACELENGTH) + modelAutomaton.minNumberOfModelMoves()));
        if(!moveLogFitness.isEmpty())
            result.addInfo(PNRepResult.MOVELOGFITNESS, moveLogFitness.average());
        if(!moveModelFitness.isEmpty())
            result.addInfo(PNRepResult.MOVEMODELFITNESS, moveModelFitness.average());
        this.results.put(decoder, result);
        //IntArrayList traces = new IntArrayList();
        //traces.addAll(logAutomaton.caseTracesMapping.get(decoder.trace()).toArray());
        //this.caseReplayResultMapping().put(traces, result);
        //this.traceAlignmentsMapping.put(decoder, result);
    }

    private void insertPartiallySynchronizedPathIntoPSP(IntIntHashMap finalConfiguration, DecodeTandemRepeats decoder, Node finalNode)
    {
        Set<Node> commutativeFinalNodes = null;
        IntArrayList traceLabels = decoder.reducedTrace();
        /*if((commutativeFinalNodes = psp.commutativePaths().get(traceLabels))==null)
        {
            commutativeFinalNodes = new UnifiedSet<Node>();
            psp.commutativePaths().put(traceLabels, commutativeFinalNodes);
        }
        commutativeFinalNodes.add(finalNode);*/

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
                            potentialNode = initializePotentialNode(currentNode, tlog, tmodel, Configuration.Operation.MATCH, finalConfiguration, true, decoder);
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
                        potentialNode = initializePotentialNode(currentNode, tlog, null, Configuration.Operation.LHIDE, finalConfiguration, true, decoder); //, pw);
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
                        potentialNode = initializePotentialNode(currentNode, null, tmodel, Configuration.Operation.RHIDE, finalConfiguration, true, decoder); //, pw);
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
        psp.finalNodes().add(finalNode);
    }

    private void offerPotentialNodeWithPruning(Node potentialNode, double actMin, Queue<Node> toBeVisited)
    //private void offerPotentialNodeWithPruning(Node potentialNode, int actMin, FibonacciHeap<Node> toBeVisited, Set<Node> visited)
    {
//		int pruningCost;
//		if(statePruning.containsKey((long) potentialNode.stateLogID() | ((long) potentialNode.stateModelID() << 32)))
//			pruningCost = statePruning.get((long) potentialNode.stateLogID() | ((long) potentialNode.stateModelID() << 32));
//		else
//		{
//			pruningCost = actMin;
//			statePruning.put((long) potentialNode.stateLogID() | ((long) potentialNode.stateModelID() << 32), pruningCost);
//		}
        //if(potentialNode.weight()<pruningCost && visited.add(potentialNode))
        if(visited.containsKey(potentialNode.hashCode()))
        {
            if(visited.get(potentialNode.hashCode()) <= potentialNode.weight()) return;
        }
        qStates++;
        //toBeVisited.enqueue(potentialNode, potentialNode.weight());

        toBeVisited.offer(potentialNode);
    }

    private Node initializePotentialNode(Node currentNode, Transition tlog, Transition tmodel, Configuration.Operation operation,
                                         IntIntHashMap finalConfiguration, boolean insertToPSP, DecodeTandemRepeats decoder) //, PrintWriter pw)
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
                potentialNode = new Node(tlog.target(), tmodel.target(), potentialConfiguration, currentNode.tracePosition+1, currentNode.weight());
                //calculateCost(potentialConfiguration, finalConfiguration, tlog.target(), tmodel.target(), decoder));
            else
                potentialNode = new Node(tlog.target(), tmodel.target(), potentialConfiguration, currentNode.tracePosition+1, currentNode.weight());
            //calculateCost(potentialConfiguration, finalConfiguration, tlog.target(), tmodel.target(), decoder));
        }
        else if (operation == Configuration.Operation.LHIDE)
        {
            potentialConfiguration = currentNode.configuration().cloneConfiguration();
            //potentialConfiguration.moveOnLog().add(tlog.eventID());
            //potentialConfiguration.setMoveOnLog().addToValue(tlog.eventID(), 1);
            //potentialConfiguration.adjustSetMoveOnLog();
            potentialConfiguration.sequenceSynchronizations().add(new au.qut.apromore.psp.Synchronization(Configuration.Operation.LHIDE, tlog.eventID(), -1, tlog.target().id(), -1, currentNode.hashCode()));
            //potentialConfiguration.logIDs().add(tlog.target().id());
            if(insertToPSP) {
                potentialNode = new Node(tlog.target(), currentNode.stModel(), potentialConfiguration, currentNode.tracePosition + 1, decoder.adjustedCost().get(currentNode.tracePosition));
                //calculateCost(potentialConfiguration, finalConfiguration, tlog.target(), currentNode.stModel(), decoder));
            }
            else {
                potentialNode = new Node(tlog.target(), currentNode.stModel(), potentialConfiguration, currentNode.tracePosition+1, currentNode.weight() + decoder.adjustedCost().get(currentNode.tracePosition));
                //calculateCost(potentialConfiguration, finalConfiguration, tlog.target(), currentNode.stModel(), decoder));
            }
        } else
        {
            potentialConfiguration = currentNode.configuration().cloneConfiguration();
            //potentialConfiguration.moveOnModel().add(tmodel.eventID());
            //potentialConfiguration.setMoveOnModel().addToValue(tmodel.eventID(), 1);
            //potentialConfiguration.adjustSetMoveOnModel();
            potentialConfiguration.sequenceSynchronizations().add(new au.qut.apromore.psp.Synchronization(Configuration.Operation.RHIDE, -1, tmodel.eventID(), -1, tmodel.target().id(), currentNode.hashCode()));
            //potentialConfiguration.modelIDs().add(tmodel.target().id());
            if(insertToPSP)
                potentialNode = new Node(currentNode.stLog(), tmodel.target(), potentialConfiguration, currentNode.tracePosition, currentNode.weight() + decoder.adjustedRHIDECost().get(currentNode.tracePosition));
                //calculateCost(potentialConfiguration, finalConfiguration, currentNode.stLog(), tmodel.target(), decoder));
            else
                potentialNode = new Node(currentNode.stLog(), tmodel.target(), potentialConfiguration, currentNode.tracePosition, currentNode.weight() + decoder.adjustedRHIDECost().get(currentNode.tracePosition));
            //calculateCost(potentialConfiguration, finalConfiguration, currentNode.stLog(), tmodel.target(), decoder));
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
}
