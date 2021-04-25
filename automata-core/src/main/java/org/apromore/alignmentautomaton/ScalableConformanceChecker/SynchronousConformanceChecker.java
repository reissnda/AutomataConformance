package org.apromore.alignmentautomaton.ScalableConformanceChecker;

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

public class SynchronousConformanceChecker {
/*
    static {
        try {
            System.loadLibrary("lpsolve55");
            System.loadLibrary("lpsolve55j");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Automaton logAutomaton;
    private DecomposingConformanceImporter decompositions;
    private PSP psp;
    private Map<IntArrayList, Set<Node>> prefixMemorizationTable;
    private Map<Long, Set<Configuration>> suffixMemorizationTable;
    private PNMatchInstancesRepResult resOneOptimal;
    private PNMatchInstancesRepResult resAllOptimal;
    private Map<IntArrayList, AllSyncReplayResult> caseReplayResultMapping;
    public UnifiedMap<IntArrayList, AllSyncReplayResult> traceAlignmentsMapping = new UnifiedMap<IntArrayList, AllSyncReplayResult>();
    public int cost = 0;
    public LongIntHashMap statePruning;
    public long preperationLog;
    public long preperationModel;
    public long timeOneOptimal;
    public long timeAllOptimal;
    private int qStates=1;
    private static LpSolve lp;

    public SynchronousConformanceChecker(){}

    public SynchronousConformanceChecker(Automaton logAutomaton, DecomposingConformanceImporter decompositions, int stateLimit)
    {
        long start = System.currentTimeMillis();
        this.logAutomaton = logAutomaton;
        System.out.println(logAutomaton.eventLabels());
        this.decompositions = decompositions;
        calculateOneOptimalAlignments(stateLimit);
        //calculateAllOptimalAlignments(stateLimit);
        this.timeOneOptimal = System.currentTimeMillis() - start;
    }

    //TODO: Implement Standalone tool
    public SynchronousConformanceChecker(String path, String log, String model, XLog xLog, Petrinet pnet, Marking marking, int stateLimit, boolean toDot) throws Exception {
        long start = System.currentTimeMillis();
        //System.out.println("DAFSA creation");
        logAutomaton = new ImportEventLog().createDAFSAfromLog(xLog);
        long logTime = System.currentTimeMillis();
        if(toDot)
            logAutomaton.toDot(log.substring(0, log.indexOf(".")) + "_dafsa.dot");
        this.preperationLog = logTime - start;
        logTime = System.currentTimeMillis();
        //System.out.println("FSM creation");
        //modelAutomaton = new ImportProcessModel().createFSMfromPetrinet(pnet, marking, logAutomaton.eventLabels(), logAutomaton.inverseEventLabels());
        this.decompositions = new DecomposingConformanceImporter();
        this.decompositions.importAndDecomposeModelAndLogForConformanceChecking(path, model, log);
        long modelTime = System.currentTimeMillis();
        if(toDot) {
            int i = 1;
            for(Automaton fsm : this.decompositions.sComponentFSMs)
                fsm.toDot(model.substring(0, model.indexOf(".")) + "S-Component" + (i++) + "_taulessRG.dot");
        }
        initializePSP();
        this.preperationModel = modelTime - logTime;
        this.calculateOneOptimalAlignments(stateLimit);
        if(toDot)
            psp.toDot(path + "psp.dot");
//		Double time = Double.parseDouble(this.resAllOptimal().getInfo().get(PNRepResult.TIME))  * this.logAutomaton.caseTracesMapping.size();
//		this.timeOneOptimal = time.intValue();
    }

    private void initializePSP()
    {
        IntObjectHashMap modelStartNodes = new IntObjectHashMap();
        for(Automaton fsm : this.decompositions.sComponentFSMs)
        {
            System.out.println(fsm.eventLabels());
            modelStartNodes.put(fsm.sourceID(), fsm.source());

        }
        Node source = new Node(logAutomaton.source(), modelStartNodes,
                new Configuration(new IntArrayList(), new IntIntHashMap(), new IntArrayList(), new IntIntHashMap(), new FastList<Couple<Integer, Integer>>(), new FastList<org.apromore.alignmentautomaton.psp.Synchronization>(),
                        new IntArrayList(), new IntArrayList()), 0);
        source.configuration().logIDs().add(logAutomaton.sourceID());
        for(int i = 0; i < this.decompositions.sComponentFSMs.size(); i++)
            {source.configuration().sCompIDs(). add(new Couple(i, this.decompositions.sComponentFSMs.get(i).sourceID()));};
        psp = new PSP(HashBiMap.create(), new UnifiedSet<Arc>(), source.hashCode(), logAutomaton, this.decompositions.sComponentFSMs);
        psp.nodes().put(source.hashCode(), source);
    }

    private void calculateOneOptimalAlignments(int stateLimit)
    {
        long start = System.nanoTime();
        double time = 0;
        double rawFitnessCost = 0;
        double numStates = 0;
        double numAlignments = 0;
        double traceFitness = 0;
        double moveModelFitness = 0;
        double moveLogFitness = 0;
        double traceLength = 0;
        double queuedStates = 0;
        double logSize =  0;
        for(IntArrayList trace : logAutomaton.caseTracesMapping.keySet())
        {
            logSize += logAutomaton.caseTracesMapping.get(trace).size();
        }

//		IntArrayList test = new IntArrayList();
//		List<String> testCase = Arrays.asList("Activity A", "Activity C", "Activity D", "Activity C", "Activity E", "Activity F", "Activity G", "Activity I", "Activity K", "Activity J", "Activity H", "Activity C", "Activity E", "Activity D", "Activity F", "Activity G", "Activity I", "Activity K", "Activity J", "Activity H", "Activity C", "Activity E", "Activity D", "Activity F", "Activity B");
////		test.addAll(2, 0, 0, 2, 2, 0, 0, 0, 0, 1);
//		for(int i = 0; i < testCase.size();i++)
//			test.add(this.logAutomaton.inverseEventLabels().get(testCase.get(i)));
        for(IntIntHashMap finalConfiguration : logAutomaton.configCasesMapping().keySet())
            for(IntArrayList trace : logAutomaton.configCasesMapping().get(finalConfiguration))
            {
                //if(trace.equals(test))
                //long start2 = System.nanoTime();
                System.out.println(trace);
                //System.out.println(trace.size());
                {
                    System.out.println(trace);// + " - " + finalConfiguration);
                    this.calculatePartiallySynchronizedPathWithLeastSkipsFor(finalConfiguration, trace, stateLimit);
                }
                System.out.println(this.traceAlignmentsMapping.get(trace).getInfo());
                //System.out.println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start2));
            }

        for(AllSyncReplayResult result : this.resOneOptimal())
        {
            if(result.isReliable())
            {
                time += (result.getInfo().get(PNMatchInstancesRepResult.TIME) * result.getTraceIndex().size());
                rawFitnessCost += (result.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST) * result.getTraceIndex().size());
                numStates += (result.getInfo().get(PNMatchInstancesRepResult.NUMSTATES) * result.getTraceIndex().size());
                numAlignments += result.getInfo().get(PNMatchInstancesRepResult.NUMALIGNMENTS);
                traceFitness += (result.getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS) * result.getTraceIndex().size());
                moveModelFitness += (result.getInfo().get(PNRepResult.MOVEMODELFITNESS) * result.getTraceIndex().size());
                moveLogFitness += (result.getInfo().get(PNRepResult.MOVELOGFITNESS) * result.getTraceIndex().size());
                traceLength += (result.getInfo().get(PNMatchInstancesRepResult.ORIGTRACELENGTH) * result.getTraceIndex().size());
                queuedStates += (result.getInfo().get(PNMatchInstancesRepResult.QUEUEDSTATE) * result.getTraceIndex().size());
            }
        }

        this.resOneOptimal.addInfo(PNMatchInstancesRepResult.TIME, "" + (time / logSize));
        this.resOneOptimal.addInfo(PNMatchInstancesRepResult.RAWFITNESSCOST, "" + (rawFitnessCost / logSize));
        this.resOneOptimal.addInfo(PNMatchInstancesRepResult.NUMSTATES, "" + (numStates / logSize));
        this.resOneOptimal.addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS, "" + numAlignments);
        this.resOneOptimal.addInfo(PNMatchInstancesRepResult.TRACEFITNESS, "" +  (traceFitness / logSize));
        this.resOneOptimal.addInfo(PNRepResult.MOVEMODELFITNESS, "" + (moveModelFitness  / logSize));
        this.resOneOptimal.addInfo(PNRepResult.MOVELOGFITNESS, "" + (moveLogFitness / logSize));
        this.resOneOptimal.addInfo(PNRepResult.ORIGTRACELENGTH, "" + traceLength / logSize);
        this.resOneOptimal.addInfo(PNMatchInstancesRepResult.QUEUEDSTATE, "" + queuedStates / logSize);
        this.timeOneOptimal =TimeUnit.MILLISECONDS.convert(System.nanoTime() - start,TimeUnit.NANOSECONDS);
    }

    //TODO:Get One Optimal Alignment as quick as possible -> Done! Implement new Data Structure
    private void calculatePartiallySynchronizedPathWithLeastSkipsFor(IntIntHashMap finalConfiguration, IntArrayList traceLabels, int stateLimit)
    {
        double start = System.currentTimeMillis();
        Set<Node> visited = new UnifiedSet<Node>();
        PriorityQueue<Node> toBeVisited = new PriorityQueue<>(new NodeComparator());
        //FibonacciHeap<Node> toBeVisited = new FibonacciHeap<Node>();
        Node currentNode = psp.sourceNode();
        //PriorityQueue<Node> potentialFinalNodes = new PriorityQueue<>(new NodeComparator());
        UnifiedSet<Node> potentialFinalNodes = new UnifiedSet<Node>();
        //int actMin = (int) (finalConfiguration.sum() + modelAutomaton.minNumberOfModelMoves());
        int actMin = (int) (finalConfiguration.sum() + modelAutomaton.minNumberOfModelMoves());
        Node potentialNode;
        int sizeMoveOnLog;
        double numStates = 1;
        qStates = 1;
        //this.statePruning = new LongIntHashMap();
        *//*Set<Node> prefixMemoization =  this.getPrefixMemoization(traceLabels);
        for(Node node : prefixMemoization)
        {
            potentialNode = this.cloneNodeForConfiguration(node, finalConfiguration, traceLabels);
            this.offerPotentialNode(potentialNode, actMin, toBeVisited, visited);
        }*//*
        this.offerPotentialNodeWithPruning(currentNode, actMin, toBeVisited, visited);

        while(true)
        {
            //currentNode = toBeVisited.dequeueMin().getValue();
            currentNode = toBeVisited.poll();
            numStates++;
//			if(numStates > 100000)
//				System.out.println("long case");
            if(numStates == stateLimit)
                break;
            //if(qStates == stateLimit) break?
            *//*if(suffixMemorizationTable!=null)
            {
                long decodedStates = (long) currentNode.stateLogID() | ((long) currentNode.stateModelID() >> 32);
                Set<Configuration> set;
                if((set = suffixMemorizationTable.get(decodedStates)) != null)
                    for(Configuration suffix : set)
                    {
                        potentialNode = this.createPotentialFinalNodeFrom(currentNode, suffix, finalConfiguration, traceLabels);
                        this.offerPotentialNodeWithPruning(potentialNode, actMin, toBeVisited, visited);
                    }
            }*//*
            if(qStates!=stateLimit)
                if((sizeMoveOnLog=currentNode.configuration().moveOnLog().size())!=traceLabels.size())// && visited.size()<=stateLimit)
                {
                    int expTraceLabel = traceLabels.get(sizeMoveOnLog);
                    for(Transition tlog : currentNode.stLog().outgoingTransitions())
                        if(tlog.eventID()==expTraceLabel)
                        {
                            //see if match is possible in all S-Components that contain expTraceLabel
                            boolean matchPossible = true;
                            for(int i = 0; i < this.decompositions.sComponentFSMs.size(); i++)
                            {
                                if(this.decompositions.sComponentFSMs.get(i).eventLabels().keySet().contains(expTraceLabel))
                                {
                                    boolean expLabelFound = false;
                                    for (Transition tmodel : currentNode.stSComps().get(i).outgoingTransitions())
                                        if (tlog.eventID() == tmodel.eventID()) {
                                            expLabelFound = true;

                                        }
                                     if(!expLabelFound) {matchPossible = false;}
                                }
                            }
                            if(matchPossible)
                            {
                                potentialNode = initializePotentialNode(currentNode, tlog, tmodel, Configuration.Operation.MATCH, finalConfiguration, false, traceLabels);
                                this.offerPotentialNodeWithPruning(potentialNode, actMin, toBeVisited, visited);
                            }
                            //consider LHIDE
                            potentialNode = initializePotentialNode(currentNode, tlog, null, Configuration.Operation.LHIDE, finalConfiguration, false, traceLabels);
                            this.offerPotentialNodeWithPruning(potentialNode, actMin, toBeVisited, visited);

                            //break;
                        }
                }

            if(qStates!=stateLimit)
                for(Transition tmodel : currentNode.stModel().outgoingTransitions())
                {
                    potentialNode = initializePotentialNode(currentNode, null, tmodel, Configuration.Operation.RHIDE, finalConfiguration, false, traceLabels);
                    this.offerPotentialNodeWithPruning(potentialNode, actMin, toBeVisited, visited);
                }

            if(
                    currentNode.stLog().isFinal()
                            && currentNode.stModel().isFinal()
                            && currentNode.configuration().moveOnLog().equals(traceLabels)
                    )
            {
                if(currentNode.weight()<actMin)
                {
                    potentialFinalNodes = new UnifiedSet<Node>();
                    actMin = currentNode.weight();
                }
                potentialFinalNodes.add(currentNode);
//				potentialFinalNodes.offer(currentNode);
//				actMin = potentialFinalNodes.peek().weight();
            }
            if(toBeVisited.isEmpty()) break; //System.out.println("Screw you!");
                //if(actMin < toBeVisited.min().getPriority()) break;
            else if(toBeVisited.peek().weight() >= actMin) break;
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
            for(org.apromore.alignmentautomaton.psp.Synchronization tr : potentialFinalNode.configuration().sequenceTransitions())
            {
                if(tr.operation() == Configuration.Operation.MATCH)
                {
                    nodeInstanceLst.add(logAutomaton.eventLabels().get(tr.eventLog()));
                    stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.LMGOOD);
                    movesMatching++;
                }
                else if(tr.operation() == Configuration.Operation.LHIDE)
                {
                    nodeInstanceLst.add(logAutomaton.eventLabels().get(tr.eventLog()));
                    stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.L);
                    movesOnLog++;
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
            moveModelFitness.add(1-moveModel / (moveModel + movesMatching));
            moveLogFitness.add(1-movesOnLog / (movesOnLog + movesMatching + moveInvi));
            this.insertPartiallySynchronizedPathIntoPSP(finalConfiguration, traceLabels, potentialFinalNode);
        }
        AllSyncReplayResult result = new AllSyncReplayResult(lstNodeInstanceLst, lstStepTypesLst, -1, !potentialFinalNodes.isEmpty());
        result.getTraceIndex().remove(-1);
        Integer[] relevantTraces = ArrayUtils.toObject(logAutomaton.caseTracesMapping.get(traceLabels).toArray());
        result.getTraceIndex().addAll(Arrays.<Integer>asList( relevantTraces));
        result.addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS, (double) result.getStepTypesLst().size());
        result.addInfo(PNMatchInstancesRepResult.RAWFITNESSCOST, (double) actMin);
        result.addInfo(PNMatchInstancesRepResult.NUMSTATES, (double) numStates);
        result.addInfo(PNMatchInstancesRepResult.QUEUEDSTATE, (double) numStates + toBeVisited.size());
        result.addInfo(PNMatchInstancesRepResult.ORIGTRACELENGTH, (double) traceLabels.size());
        result.addInfo(PNMatchInstancesRepResult.TIME, (double) (end - start));
        result.addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS, (double) lstNodeInstanceLst.size());
        result.addInfo(PNMatchInstancesRepResult.TRACEFITNESS, (double) 1-result.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST) / (result.getInfo().get(PNMatchInstancesRepResult.ORIGTRACELENGTH) + modelAutomaton.minNumberOfModelMoves()));
        if(!moveLogFitness.isEmpty())
            result.addInfo(PNRepResult.MOVELOGFITNESS, moveLogFitness.average());
        if(!moveModelFitness.isEmpty())
            result.addInfo(PNRepResult.MOVEMODELFITNESS, moveModelFitness.average());
        this.resOneOptimal().add(result);
        IntArrayList traces = new IntArrayList();
        traces.addAll(logAutomaton.caseTracesMapping.get(traceLabels).toArray());
        this.caseReplayResultMapping().put(traces, result);
        this.traceAlignmentsMapping.put(traceLabels, result);
    }

    private void insertPartiallySynchronizedPathIntoPSP(IntIntHashMap finalConfiguration, IntArrayList traceLabels, Node finalNode)
    {
        Set<Node> commutativeFinalNodes = null;
        if((commutativeFinalNodes = psp.commutativePaths().get(traceLabels))==null)
        {
            commutativeFinalNodes = new UnifiedSet<Node>();
            psp.commutativePaths().put(traceLabels, commutativeFinalNodes);
        }
        commutativeFinalNodes.add(finalNode);

        Node currentNode = psp.sourceNode();
        Node potentialNode = currentNode;
        for(org.apromore.alignmentautomaton.psp.Synchronization transition : finalNode.configuration().sequenceTransitions())
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
            if(!(potentialNode.stLog().isSource()) && potentialNode.stLog().outgoingTransitions().size()>1)
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
                    suffixMemorizationTable = new UnifiedMap<Long, Set<Configuration>>();
                Set<Configuration> relevantConfiguration = null;
                if((relevantConfiguration = suffixMemorizationTable.get( (long) potentialNode.stateLogID() | ((long) potentialNode.stateModelID() << 32)))==null)
                {
                    relevantConfiguration = new UnifiedSet<Configuration>();
                    suffixMemorizationTable.put( (long) potentialNode.stateLogID() | ((long) potentialNode.stateModelID() << 32), relevantConfiguration);
                }
                relevantConfiguration.add(finalNode.configuration().calculateSuffixFrom(potentialNode.configuration()));
            }
            currentNode = potentialNode;
        }

        finalNode.isFinal(true);
        psp.finalNodes().add(finalNode);
    }

    private Node initializePotentialNode(Node currentNode, Transition tlog, Transition tmodel, Configuration.Operation operation,
                                         IntIntHashMap finalConfiguration, boolean insertToPSP, IntArrayList trace) //, PrintWriter pw)
    {
        Configuration potentialConfiguration;
        Node potentialNode;
        if(operation == Configuration.Operation.MATCH)
        {
            potentialConfiguration = currentNode.configuration().cloneConfiguration();
            potentialConfiguration.moveOnLog().add(tlog.eventID());
            potentialConfiguration.moveOnModel().add(tmodel.eventID());
            potentialConfiguration.moveMatching().add(new Couple<Integer, Integer>(tlog.eventID(), tmodel.eventID()));
            potentialConfiguration.setMoveOnLog().addToValue(tlog.eventID(), 1);
            potentialConfiguration.setMoveOnModel().addToValue(tmodel.eventID(), 1);
            //potentialConfiguration.adjustSetMoveOnLog();
            //potentialConfiguration.adjustSetMoveOnModel();
            potentialConfiguration.sequenceTransitions().add(new org.apromore.alignmentautomaton.psp.Synchronization(Configuration.Operation.MATCH, tlog.eventID(), tmodel.eventID(), tlog.target().id(), tmodel.target().id(), currentNode.hashCode()));
            potentialConfiguration.logIDs().add(tlog.target().id());
            potentialConfiguration.modelIDs().add(tmodel.target().id());
            if(insertToPSP)
                potentialNode = new Node(tlog.target(), tmodel.target(), potentialConfiguration, 0);
            else
                potentialNode = new Node(tlog.target(), tmodel.target(), potentialConfiguration,
                        calculateCost(potentialConfiguration, finalConfiguration, tlog.target(), tmodel.target(), trace));
        }
        else if (operation == Configuration.Operation.LHIDE)
        {
            potentialConfiguration = currentNode.configuration().cloneConfiguration();
            potentialConfiguration.moveOnLog().add(tlog.eventID());
            potentialConfiguration.setMoveOnLog().addToValue(tlog.eventID(), 1);
            //potentialConfiguration.adjustSetMoveOnLog();
            potentialConfiguration.sequenceTransitions().add(new org.apromore.alignmentautomaton.psp.Synchronization(Configuration.Operation.LHIDE, tlog.eventID(), -1, tlog.target().id(), -1, currentNode.hashCode()));
            potentialConfiguration.logIDs().add(tlog.target().id());
            if(insertToPSP)
                potentialNode = new Node(tlog.target(), currentNode.stModel(), potentialConfiguration, 0);
            else
                potentialNode = new Node(tlog.target(), currentNode.stModel(), potentialConfiguration,
                        calculateCost(potentialConfiguration, finalConfiguration, tlog.target(), currentNode.stModel(), trace));
        } else
        {
            potentialConfiguration = currentNode.configuration().cloneConfiguration();
            potentialConfiguration.moveOnModel().add(tmodel.eventID());
            potentialConfiguration.setMoveOnModel().addToValue(tmodel.eventID(), 1);
            //potentialConfiguration.adjustSetMoveOnModel();
            potentialConfiguration.sequenceTransitions().add(new org.apromore.alignmentautomaton.psp.Synchronization(Configuration.Operation.RHIDE, -1, tmodel.eventID(), -1, tmodel.target().id(), currentNode.hashCode()));
            potentialConfiguration.modelIDs().add(tmodel.target().id());
            if(insertToPSP)
                potentialNode = new Node(currentNode.stLog(), tmodel.target(), potentialConfiguration, 0);
            else
                potentialNode = new Node(currentNode.stLog(), tmodel.target(), potentialConfiguration,
                        calculateCost(potentialConfiguration, finalConfiguration, currentNode.stLog(), tmodel.target(), trace));
        }

        if(insertToPSP)
        {
            Arc potentialArc;
            if(psp.nodes().containsValue(potentialNode))
            {
                potentialNode = psp.nodes().get(potentialNode.hashCode());
            }
            else
                psp.nodes().put(potentialNode.hashCode(), potentialNode);

            if(operation == Configuration.Operation.RHIDE)
                potentialArc = new Arc(new org.apromore.alignmentautomaton.psp.Synchronization(Configuration.Operation.RHIDE, -1, tmodel.eventID(), -1, tmodel.target().id(), currentNode.hashCode()), currentNode, potentialNode);
            else if(operation==Configuration.Operation.LHIDE)
                potentialArc = new Arc(new org.apromore.alignmentautomaton.psp.Synchronization(operation, tlog.eventID(), -1, tlog.target().id(),-1, currentNode.hashCode()), currentNode, potentialNode);
            else
                potentialArc = new Arc(new org.apromore.alignmentautomaton.psp.Synchronization(operation, tlog.eventID(), tmodel.eventID(), tlog.target().id(),tmodel.target().id(), currentNode.hashCode()), currentNode, potentialNode);
            if(currentNode.outgoingArcs().add(potentialArc))
                psp.arcs().add(potentialArc);
        }
        return potentialNode;
    }

    public Set<Node> getPrefixMemoization(IntArrayList traceLabels)
    {
        Set<Node> prefixMemo = new UnifiedSet<Node>();
        if(prefixMemorizationTable==null) return prefixMemo;
        IntArrayList prefix = new IntArrayList();
        Set<Node> memo;
        for(int i=0; i<traceLabels.size();i++)
        {
            prefix.add(traceLabels.get(i));
            if((memo = prefixMemorizationTable.get(prefix))!=null)
            {
                prefixMemo.addAll(memo);
            }
        }
        return prefixMemo;
    }

    private Node cloneNodeForConfiguration(Node source, IntIntHashMap finalConfiguration, IntArrayList trace)
    {
        return new Node(source.stLog(), source.stModel(), source.configuration().cloneConfiguration(), this.calculateCost(source.configuration(), finalConfiguration, source.stLog(), source.stModel(), trace));
    }

    private Node createPotentialFinalNodeFrom(Node currentNode, Configuration suffixConfiguration, IntIntHashMap finalConfiguration, IntArrayList trace )
    {
        Configuration configuration = currentNode.configuration().cloneConfiguration();
        configuration.addSuffixFrom(suffixConfiguration);
        if(configuration.setMoveOnLog().equals(finalConfiguration))
            return new Node(logAutomaton.states().get(suffixConfiguration.logIDs().getLast()), modelAutomaton.states().get(suffixConfiguration.modelIDs().getLast()), configuration,
                    this.calculateCost(configuration, finalConfiguration, logAutomaton.states().get(suffixConfiguration.logIDs().getLast()),
                            modelAutomaton.states().get(suffixConfiguration.modelIDs().getLast()), trace));
        else
            return new Node(logAutomaton.states().get(suffixConfiguration.logIDs().getLast()), modelAutomaton.states().get(suffixConfiguration.modelIDs().getLast()), configuration, Integer.MAX_VALUE);
    }

    private int calculateCost(Configuration configuration, IntIntHashMap finalConfiguration, State stLog, State stModel, IntArrayList trace) //, PrintWriter pw)//, int stateLogID, int stateModelID, int finalState)
    {
        int futureCost = futureCost(configuration, finalConfiguration, stLog, stModel, trace);
        if(futureCost==Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        return currentCost(configuration, finalConfiguration) + futureCost;
    }

    private int currentCost(Configuration configuration, IntIntHashMap finalConfiguration) //, PrintWriter pw)
    {
        IntArrayList moveOnModelWithoutTau = new IntArrayList();
        moveOnModelWithoutTau.addAll(configuration.moveOnModel());
        moveOnModelWithoutTau.removeAll(modelAutomaton.skipEvent());
        return configuration.moveOnLog().size() + moveOnModelWithoutTau.size() - 2 * configuration.moveMatching().size();// + finalConfigurationViolations.size();
    }

    private int futureCost(Configuration configuration, IntIntHashMap finalConfigurationLog, State stLog, State stModel, IntArrayList trace)
    {
        int skips = 0;
        int count_log;
        int count_model;
        IntIntHashMap finalConfigLog = this.mapDifference(finalConfigurationLog, configuration.setMoveOnLog());
        if(stLog.isFinal() && finalConfigLog.isEmpty() && stModel.isFinal())
            return 0;
        int futureSkips = Integer.MAX_VALUE;//(int) finalConfigLog.sum();
        int loopSkips =0;
//		if(stModel.hasLoopFuture() && trace.size()>=10)
//		{
//			try {
//				loopSkips = this.calcLoopSkips(finalConfigLog, stModel.futureLoops());
//			} catch (LpSolveException e) {
//				// TODO Auto-generated catch block
//				//e.printStackTrace();
//			}
//		}
        for(IntIntHashMap optimalModelFuture : stModel.possibleFutures())
        {
            int pos = 0;
            int[] list = new int[finalConfigLog.size() + optimalModelFuture.size()];
            MutableIntSet set = finalConfigLog.keySet();
            for(int a : set.toArray()) {
                list[pos] = a;
                pos++;
            }
            for(int a : optimalModelFuture.keySet().toArray()) {
                if(!set.contains(a)) {
                    list[pos] = a;
                    pos++;
                }
            }
            skips=loopSkips;
            for(int i = 0; i < pos; i++)
            {
                int key = list[i];
                if(key == modelAutomaton.skipEvent()) continue;
                count_model = optimalModelFuture.get(key);
                count_log = finalConfigLog.get(key);
                if(count_model >= 200)
                {
                    skips += Math.max(count_model%200-count_log, 0);
                }
                else
                {
                    skips += Math.abs(count_log - count_model);
                }
            }
            futureSkips = Math.min(futureSkips, skips);
            if(futureSkips==0) break;
        }
        if(futureSkips==Integer.MAX_VALUE)
            futureSkips = (int) finalConfigLog.sum();
        return (int) futureSkips;
    }



    public int calcLoopSkips(IntIntHashMap conf, Set<IntIntHashMap> loops) throws LpSolveException
    {
        int pos = 0;
        for(IntIntHashMap loop : loops)
            pos+=loop.size();
        int[] list = new int[pos];
        pos=0;
        MutableIntSet set = new IntHashSet();
        for(IntIntHashMap loop : loops)
        {
            for(int a : loop.keySet().toArray()) {
                if(set.add(a)) {
                    list[pos] = a;
                    pos++;
                }
            }
        }
        int Ncol, i, j, rh1, rh2, ret = 0;

        Ncol = pos + loops.size();

        int[] colno1 = new int[Ncol];
        int[] colno2 = new int[Ncol];
        double[] row1 = new double[Ncol];
        double[] row2 = new double[Ncol];
        int col_val;
        double row_val;
        int val_key;

        if(lp==null)
        {
            lp = LpSolve.makeLp(pos*2, Ncol);
            lp.setAddRowmode(true);
            for(int key = 0; key < pos; key++)
            {
                for(i = 0; i < Ncol;i++)
                {
                    row1[i] = 0;
                    row2[i] = 0;
                    colno1[i] = 0;
                    colno2[i] = 0;
                }
                j = 0;
                val_key = list[key];
                col_val = key+1;
                row_val = -1;
                colno1[j] = col_val;
                row1[j] = row_val;
                colno2[j] = col_val;
                row2[j++] = row_val;
                i=1;
                for(IntIntHashMap loop : loops)
                {
                    col_val = pos + i++;
                    row_val = loop.get(val_key);
                    colno1[j] = col_val;
                    row1[j] = -row_val;
                    colno2[j] = col_val;
                    row2[j++] = row_val;
                }
                col_val = conf.get(val_key);
                rh1 = -col_val;
                rh2 = col_val;
                lp.addConstraintex(j, row1, colno1, LpSolve.LE, rh1);
                lp.addConstraintex(j, row2, colno2, LpSolve.LE, rh2);
            }
            lp.setAddRowmode(false);
            j = 0;
            for(i=1; i<=pos; i++)
            {
                colno1[j] = i;
                row1[j++] = 1;
            }
            for(int loop=1;loop<=loops.size();loop++)
            {
                colno1[j] = i++;
                row1[j++] = 0;
            }
            lp.setObjFnex(j, row1, colno1);

        }
        else
        {
            for(int key = 1; key <= lp.getNorigRows(); key++)
                lp.delConstraint(key);
            for(int key = 0; key < pos; key++)
            {
                for(i = 0; i < Ncol;i++)
                {
                    row1[i] = 0;
                    row2[i] = 0;
                    colno1[i] = 0;
                    colno2[i] = 0;
                }
                j = 0;
                val_key = list[key];
                col_val = key+1;
                row_val = -1;
                colno1[j] = col_val;
                row1[j] = row_val;
                colno2[j] = col_val;
                row2[j++] = row_val;
                i=1;
                for(IntIntHashMap loop : loops)
                {
                    col_val = pos + i++;
                    row_val = loop.get(val_key);
                    colno1[j] = col_val;
                    row1[j] = -row_val;
                    colno2[j] = col_val;
                    row2[j++] = row_val;
                }
                col_val = conf.get(val_key);
                rh1 = -col_val;
                rh2 = col_val;
                lp.addConstraintex(j, row1, colno1, LpSolve.LE, rh1);
                lp.addConstraintex(j, row2, colno2, LpSolve.LE, rh2);
            }
            j = 0;
            for(i=1; i<=pos; i++)
            {
                colno1[j] = i;
                row1[j++] = 1;
            }

            for(int loop=1;loop<=loops.size();loop++)
            {
                colno1[j] =i++;
                row1[j++] = 0;
            }

            lp.setObjFnex(j, row1, colno1);
        }

        if(ret == 0) {
            lp.setMinim();
            lp.setVerbose(LpSolve.IMPORTANT);
            ret = lp.solve();
        }
        ret = (int) lp.getObjective();
        return(ret);
    }

    //TODO: Implement Pruning
    private void offerPotentialNodeWithPruning(Node potentialNode, int actMin, Queue<Node> toBeVisited, Set<Node> visited)
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
        if(visited.add(potentialNode))
        {
            statePruning.put((long) potentialNode.stateLogID() | ((long) potentialNode.stateModelID() << 32), potentialNode.weight());
            qStates++;
            //toBeVisited.enqueue(potentialNode, potentialNode.weight());
            toBeVisited.offer(potentialNode);
        }
    }

    private void offerPotentialNode(Node potentialNode, int actMin, Queue<Node> toBeVisited, Set<Node> visited)
    //private void offerPotentialNode(Node potentialNode, int actMin, FibonacciHeap<Node> toBeVisited, Set<Node> visited)
    {
        if(potentialNode.weight()<=actMin && visited.add(potentialNode))
        {
            //toBeVisited.enqueue(potentialNode, potentialNode.weight());
            toBeVisited.add(potentialNode);
            qStates++;
        }
    }

    public PSP psp()
    {
        return this.psp;
    }

    public PNMatchInstancesRepResult resOneOptimal()
    {
        if(this.resOneOptimal==null)
            this.resOneOptimal = new PNMatchInstancesRepResult(new TreeSet<AllSyncReplayResult>());
        return this.resOneOptimal;
    }

    public PNMatchInstancesRepResult resAllOptimal()
    {
        if(this.resAllOptimal==null)
            this.resAllOptimal = new PNMatchInstancesRepResult(new TreeSet<AllSyncReplayResult>());
        return this.resAllOptimal;
    }

    public int futureSkips(IntIntHashMap log, IntIntHashMap model)
    {
        int futureSkips = 0;
        int count;
        for(int key : log.keySet().toArray())
            futureSkips += Math.max(log.get(key) - model.get(key), 0);
        for(int key : model.keySet().toArray())
        {
            if(key == modelAutomaton.skipEvent()) continue;
            if((count = model.get(key)) >= 200) count = 1;
            //count -= subtraction.get(key);
            futureSkips += Math.max(count - log.get(key), 0);
        }
        return futureSkips;
    }
    public int mapLogDifference(IntIntHashMap base, IntIntHashMap subtraction)
    {
//		IntIntHashMap mapDifference = new IntIntHashMap();
//		for(int key : mapDifference.keySet().toArray())
//		{
//			mapDifference.addToValue(key, -subtraction.get(key));
//			if(mapDifference.get(key)<0) mapDifference.put(key, 0);
//			if(mapDifference.get(key)==0) mapDifference.remove(key);
//		}
        int sum = 0;
        //int count;
        for(int key : base.keySet().toArray())
        {
//			count = base.get(key) - subtraction.get(key);
//			if(count>0)
//				mapDifference.put(key, count);
            sum += Math.max(base.get(key) - subtraction.get(key), 0);
        }
        //return mapDifference;
        return sum;
    }

    public IntIntHashMap mapDifference(IntIntHashMap base, IntIntHashMap subtraction)
    {
        IntIntHashMap mapDifference = new IntIntHashMap();
//		for(int key : mapDifference.keySet().toArray())
//		{
//			mapDifference.addToValue(key, -subtraction.get(key));
//			if(mapDifference.get(key)<0) mapDifference.put(key, 0);
//			if(mapDifference.get(key)==0) mapDifference.remove(key);
//		}
//		int sum = 0;
        int count;
        for(int key : base.keySet().toArray())
        {
            count = base.get(key) - subtraction.get(key);
            if(count>0)
                mapDifference.put(key, count);
//			sum += Math.abs(base.get(key) - subtraction.get(key));
        }
        return mapDifference;
//		return sum;
    }

    public IntIntHashMap mapModelDifference(IntIntHashMap base, IntIntHashMap subtraction)
    {
        IntIntHashMap mapDifference = new IntIntHashMap();
        int count;
        for(int key : base.keySet().toArray())
        {
            if((count=base.get(key))>=200) {mapDifference.put(key, 200); continue;}
            count = count - subtraction.get(key);
            if(count>0)
                mapDifference.put(key, count);
        }
        return mapDifference;
    }

    public int mapSpecialDifference(IntIntHashMap base, IntIntHashMap subtraction)
    {
//		IntIntHashMap mapDifference = new IntIntHashMap();
//		for(int key : mapDifference.keySet().toArray())
//		{
//			mapDifference.addToValue(key, -subtraction.get(key));
//			if(mapDifference.get(key)<0) mapDifference.put(key, 0);
//			if(mapDifference.get(key)==0) mapDifference.remove(key);
//		}
        int count;
        int sum = 0;
        for(int key : base.keySet().toArray())
        {
            if(key == modelAutomaton.skipEvent()) continue;
            if((count = base.get(key)) >= 200) count = 1;
            //count -= subtraction.get(key);
            sum += Math.max(count - subtraction.get(key), 0);
//			if(count > 0)
//				mapDifference.put(key, count);
        }
        //return mapDifference;
        return sum;
    }

    public Map<IntArrayList, AllSyncReplayResult> caseReplayResultMapping()
    {
        if(this.caseReplayResultMapping==null)
            this.caseReplayResultMapping = new UnifiedMap<IntArrayList, AllSyncReplayResult>();
        return this.caseReplayResultMapping;
    }

    public void printAlignmentResults(String alignmentStatisticsFile, String caseTypeAlignmentResultsFile) throws FileNotFoundException
    {
        PrintWriter pw1 = new PrintWriter(alignmentStatisticsFile);
        PrintWriter pw2 = new PrintWriter(caseTypeAlignmentResultsFile);

        pw1.println("Average Log Alignment Statistics per Case Type:");
        pw1.println(PNMatchInstancesRepResult.RAWFITNESSCOST + "," + PNMatchInstancesRepResult.ORIGTRACELENGTH + "," + PNMatchInstancesRepResult.QUEUEDSTATE + ","
                + PNMatchInstancesRepResult.NUMSTATES + "," + PNMatchInstancesRepResult.TIME + "," + PNMatchInstancesRepResult.NUMALIGNMENTS + "," + PNMatchInstancesRepResult.TRACEFITNESS);
        pw1.println(this.resAllOptimal().getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST) + ","
                + this.resAllOptimal().getInfo().get(PNMatchInstancesRepResult.ORIGTRACELENGTH) + ","
                + this.resAllOptimal().getInfo().get(PNMatchInstancesRepResult.QUEUEDSTATE) + ","
                + this.resAllOptimal().getInfo().get(PNMatchInstancesRepResult.NUMSTATES) + ","
                + this.resAllOptimal().getInfo().get(PNMatchInstancesRepResult.TIME) + ","
                + this.resAllOptimal().getInfo().get(PNMatchInstancesRepResult.NUMALIGNMENTS) + ","
                + this.resAllOptimal().getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS));

        pw1.println();
        pw1.println();
        pw1.println("Alignment Statistics per Case:");
        pw1.println("Num.,"
                +	"Case Type,"
                +	"Case ID,"
                +	"Trace Index,"
                +	"isReliable,"
                +	PNMatchInstancesRepResult.RAWFITNESSCOST +","
                +	PNMatchInstancesRepResult.ORIGTRACELENGTH +","
                +	PNMatchInstancesRepResult.QUEUEDSTATE +","
                +	PNMatchInstancesRepResult.NUMSTATES +","
                +	PNMatchInstancesRepResult.TIME +","
                +	PNMatchInstancesRepResult.NUMALIGNMENTS +","
                +	PNMatchInstancesRepResult.TRACEFITNESS
        );

        pw2.println("Alignments per Case Type:");
        pw2.println("Case Type, Represented number of traces,alignment(task) #1,alignment(task) #2,alignment(task) #3,...");
        int num=1, caseType =1;
        AllSyncReplayResult res;
        for(IntArrayList traces : caseReplayResultMapping.keySet())
        {
            res = caseReplayResultMapping.get(traces);
            for(int trace : traces.toArray())
            {
                pw1.println(num++ +","
                        +	caseType + ","
                        +	this.logAutomaton.caseIDs.get(trace) + ","
                        +	trace + ","
                        +	res.isReliable() + ","
                        +	res.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST) +","
                        +	res.getInfo().get(PNMatchInstancesRepResult.ORIGTRACELENGTH) +","
                        +	res.getInfo().get(PNMatchInstancesRepResult.QUEUEDSTATE) +","
                        +	res.getInfo().get(PNMatchInstancesRepResult.NUMSTATES) +","
                        +	res.getInfo().get(PNMatchInstancesRepResult.TIME) +","
                        +	res.getInfo().get(PNMatchInstancesRepResult.NUMALIGNMENTS) +","
                        +	res.getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS)
                );
            }
            for(int i=0;i< res.getStepTypesLst().size();i++)
            {
                List<Object> labels = res.getNodeInstanceLst().get(i);
                List<StepTypes> ops = res.getStepTypesLst().get(i);
                pw2.println();
                pw2.print(caseType +","
                        +	res.getTraceIndex().size() + ",");
                for(int j=0;j<labels.size();j++)
                {
                    pw2.print(ops.get(j) +"( " + labels.get(j) +")");
                    if(j!=labels.size()-1)
                        pw2.print(",");
                }
            }
            caseType++;
        }

        pw1.close();
        pw2.close();
    }*/
}
