package au.qut.apromore.importer;

import au.qut.apromore.automaton.Automaton;
import au.qut.apromore.automaton.State;
import au.qut.apromore.automaton.Transition;
import au.unimelb.partialorders.GlobalOracleWrapper;
import au.unimelb.partialorders.LocalOracleWrapper;
import au.unimelb.partialorders.PartialOrder;
import au.unimelb.pattern.ConcurrentPattern;
import au.unimelb.pattern.ReducedTrace;
import au.unimelb.pattern.RepetitivePattern;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import name.kazennikov.dafsa.AbstractIntDAFSA;
import name.kazennikov.dafsa.IntDAFSAInt;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.*;
import org.eclipse.collections.impl.list.mutable.primitive.BooleanArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import qut.au.automaton.factory.state.MultisetFPFactory;
import qut.au.oracle.alpha.AlphaRelations;
import qut.au.oracle.global.GlobalOracle;
import qut.au.oracle.local.LocalOracle;
import qut.au.oracle.local.validation.CoOccurrenceValidator;
import sun.jvm.hotspot.utilities.IntArray;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class GeneralizationImporter implements Callable<String>
{
    private final String conceptname = "concept:name";
    private final String lifecycle = "lifecycle:transition";
    private String log;
    private String path;
    private ImportEventLog importer = new ImportEventLog();
    private XLog xLog;
    private int nEvents;
    private int nUnqEvents;
    private int nTraces;
    private int nUnqTraces;
    private int nConcPairsGlobal;
    private int nPartialOrders;
    private int nConcPatterns;
    private int nConcUnqTraces;
    private int nConcTotalTraces;
    private int nContexts;
    private double avgConcPairsLocal;
    private int nPartialOrdersLocal;
    private int nConcUnqTracesLocal;
    private int nConcPatternsLocal;
    private int nConcTotalTracesLocal;
    private int nTRs;
    private int nExtendedTraces;
    private double avgLabels;
    private int nRepPatterns;
    private int nRepPatternsTotalTraceCount;
    public String statistics="";

    public DecomposingTRImporter getDecompositions() {
        return decompositions;
    }

    private DecomposingTRImporter decompositions;

    public int getTraceCount()
    {
        return xLog.size();
    }
    private BiMap<Integer, String> labelMapping;
    private BiMap<String, Integer> inverseLabelMapping = HashBiMap.create();
    private UnifiedMap<IntArrayList,IntArrayList> caseTracesMapping;
    private Map<Integer, String> caseIDs;
    private boolean useGlobal;
    private UnifiedSet<IntArrayList> uniqueTraces;
    private ObjectIntHashMap uniqueTraceCounts;
    private UnifiedMap<IntIntHashMap, UnifiedSet<IntArrayList>> configTracesMapping;
    private UnifiedMap<IntIntHashMap,UnifiedSet<PartialOrder>> configPartialOrderMapping;
    private UnifiedMap<IntArrayList,IntObjectHashMap<UnifiedSet<DecodeTandemRepeats>>> reductions;
    private BiMap<Integer, State> stateMapping;
    private BiMap<Integer, Transition> transitionMapping;
    private int initialState=0;
    private IntHashSet finalStates;
    private Automaton reducedDAFSA;
    private String statisticResult = "";

    public String getLogFile() {
        String log = logFile.split("/")[logFile.split("/").length-1];
        return log;
    }

    private String logFile;

    public String getModelFile() {
        String model = modelFile.split("/")[modelFile.split("/").length-1];
        return model;
    }

    private String modelFile;
    //private Petrinet petrinet;
    private Automaton reachabilityGraph;
    private boolean includeTransitiveConcurrencyPatterns=false;
    private XFactory xFac;
    private XExtension xtend;
    private XAttributeLiteral xLc;
    private UnifiedMap<IntArrayList,BooleanArrayList> uniqueTraceCompletenessMapping;
    private boolean applyAlphaPlusPlusOracle = false;
    private String concurrencyOracleResult;
    private double noiseThreshold=0.02;
    private float occurence=0.5f;
    private float balance=0.1f;
    private Object[] petrinetAndMarking;

    public boolean includesTransitiveConcurrencyPatterns() {
        return includeTransitiveConcurrencyPatterns;
    }

    public UnifiedMap<IntIntHashMap, UnifiedSet<PartialOrder>> getConfigTransitivePartialOrderMapping() {
        return configTransitivePartialOrderMapping;
    }

    private UnifiedMap<IntIntHashMap, UnifiedSet<PartialOrder>> configTransitivePartialOrderMapping;

    public UnifiedMap<IntArrayList, ReducedTrace> getReducedTraces() {
        return reducedTraces;
    }

    private UnifiedMap<IntArrayList, ReducedTrace> reducedTraces;

    public GeneralizationImporter(){}

    public GeneralizationImporter(String path, String log, boolean useGlobal, double noiseThreshold, float occurence, float balance) throws Exception
    {
        this.path=path;
        this.log=log;
        this.logFile=path+log;
        this.useGlobal=useGlobal;
        this.noiseThreshold=noiseThreshold;
        this.occurence=occurence;
        this.balance=balance;
        this.collectLogStatisticsGlobal();
    }

    public String testConcurrencyOracle(String logFile, boolean useGlobal) throws Exception
    {
        this.logFile = logFile;
        this.useGlobal = useGlobal;
        deriveLogInformation();
        testConcurrencyOracle();
        return this.concurrencyOracleResult;
    }

    public String testConcurrencyOracle(String logFile,float occurence,float balance) throws Exception
    {
        this.logFile=logFile;
        this.useGlobal=false;
        this.occurence=occurence;
        this.balance=balance;
        deriveLogInformation();
        testConcurrencyOracle();
        return this.concurrencyOracleResult;
    }

    private void testConcurrencyOracle()
    {
        //double noiseThreshold = 0.02;
        if(useGlobal) {
            GlobalOracleWrapper globalOracle = null;
            globalOracle = new GlobalOracleWrapper(xLog,labelMapping, inverseLabelMapping);
            UnifiedSet<IntHashSet> abelGlobalOracleImplementationConcurrentPairs = globalOracle.concurrentPairs();
            UnifiedSet<IntHashSet> myImplementationConcurrentPairs=null;
            UnifiedSet<IntHashSet> myImplementationWithNoiseFilter=null;
            if(applyAlphaPlusPlusOracle) {
                globalOracle = new GlobalOracleWrapper(uniqueTraceCompletenessMapping, labelMapping);
                myImplementationConcurrentPairs = globalOracle.concurrentPairs();
            }
            else {
                globalOracle = new GlobalOracleWrapper(uniqueTraces, labelMapping);
                myImplementationConcurrentPairs = globalOracle.concurrentPairs();

                globalOracle = new GlobalOracleWrapper(uniqueTraces,labelMapping,noiseThreshold);
                myImplementationWithNoiseFilter = globalOracle.concurrentPairs();
            }
            /*System.out.println(applyAlphaPlusPlusOracle);
            System.out.println("Abel's implementation:");
            System.out.println(abelGlobalOracleImplementationConcurrentPairs.size());
            System.out.println(abelGlobalOracleImplementationConcurrentPairs);
            System.out.println("My implementation:");
            System.out.println(myImplementationConcurrentPairs.size());
            System.out.println(myImplementationConcurrentPairs);
            System.out.println("My implementation with noise filter:");
            System.out.println(myImplementationWithNoiseFilter.size());
            System.out.println(myImplementationWithNoiseFilter);
            */
            this.concurrencyOracleResult=applyAlphaPlusPlusOracle + "," + noiseThreshold +"," + abelGlobalOracleImplementationConcurrentPairs.size() +"," + myImplementationConcurrentPairs.size() + "," + myImplementationWithNoiseFilter.size();
            //System.out.println(this.concurrencyOracleResult);

        }
        else
        {
            //float occurence=0.50f;
            //float balance = 0.1f;
            LocalOracleWrapper localOracle = new LocalOracleWrapper(xLog,labelMapping,inverseLabelMapping,occurence,balance);
            int nConfigs=0, nPairs=0;
            for(IntIntHashMap configuration : localOracle.configConcurrentPairsMapping().keySet())
            {
                nConfigs++;
                nPairs+=localOracle.configConcurrentPairsMapping().get(configuration).size();
            }
            this.concurrencyOracleResult=occurence +"," + balance +"," + nConfigs + "," + (double) nPairs/nConfigs;
            //System.out.println(this.concurrencyOracleResult);
        }
    }

    public String collectLogStatisticsGlobal() throws Exception
    {
        statistics = log + ",";
        deriveLogInformation();
        statistics += nEvents + "," + nUnqEvents + "," + nTraces + "," + nUnqTraces +",";
        deriveRepetitivePatterns();
        statistics+=nTRs + "," + nExtendedTraces + "," + nRepPatterns + "," + avgLabels + "," + nRepPatternsTotalTraceCount + ",";
        return statistics;
    }

    public String collectLogStatisticsLocal()
    {
        this.useGlobal=true;
        deriveConcurrentPatterns();
        statistics += nConcPairsGlobal + "," + nPartialOrders + "," + nConcUnqTraces + "," + nConcPatterns + "," + nConcTotalTraces + ",";
        this.useGlobal=false;
        deriveConcurrentPatterns();
        statistics+=nContexts + "," + avgConcPairsLocal + "," + nPartialOrdersLocal + "," + nConcUnqTracesLocal + "," + nConcPatternsLocal + "," + nConcTotalTracesLocal+"\n";
        return statistics;
    }

    public void derivePatternsForEventLog(String logFile, String modelFile, boolean useGlobal) throws Exception
    {
        this.logFile = logFile;
        this.modelFile = modelFile;
        this.useGlobal = useGlobal;
        deriveLogInformation();
        deriveConcurrentPatterns();
        deriveRepetitivePatterns();
        buildReachabilityGraphAndDecompositions();
    }

    public void derivePatternsForEventLog(String logFile, String modelFile, boolean useGlobal, boolean includeTransitiveConcurrencyPatterns) throws Exception
    {
        this.logFile = logFile;
        this.modelFile = modelFile;
        this.useGlobal = useGlobal;
        this.includeTransitiveConcurrencyPatterns=includeTransitiveConcurrencyPatterns;
        deriveLogInformation();
        deriveConcurrentPatterns();
        deriveRepetitivePatterns();
        buildReachabilityGraphAndDecompositions();
    }

    public void derivePatternsForEventLog(String logFile, String modelFile, double noiseThreshold) throws Exception
    {
        this.logFile = logFile;
        this.modelFile = modelFile;
        this.useGlobal = true;
        this.noiseThreshold=noiseThreshold;
        //this.includeTransitiveConcurrencyPatterns=includeTransitiveConcurrencyPatterns;
        deriveLogInformation();
        deriveConcurrentPatterns();
        deriveRepetitivePatterns();
        buildReachabilityGraphAndDecompositions();
    }

    public void derivePatternsForEventLog(String logFile, String modelFile, float occurence, float balance) throws Exception
    {
        this.logFile = logFile;
        this.modelFile = modelFile;
        this.useGlobal = false;
        this.occurence=occurence;
        this.balance=balance;
        deriveLogInformation();
        deriveConcurrentPatterns();
        deriveRepetitivePatterns();
        buildReachabilityGraphAndDecompositions();
    }

    public void importEventLogAndPetriNet(String logFile, String modelFile) throws Exception
    {
        this.logFile=logFile;
        this.modelFile=modelFile;
        this.xLog = importer.importEventLog(logFile);
        this.petrinetAndMarking = new ImportProcessModel().importPetriNetAndMarkingFromPNMLorBPMN(modelFile);
    }

    private void buildReachabilityGraphAndDecompositions() throws Exception
    {
        this.decompositions = new DecomposingTRImporter();
        if(petrinetAndMarking==null) {
            decompositions.importAndDecomposeModelForConformanceChecking(modelFile, labelMapping, inverseLabelMapping);
            //reachabilityGraph = new ImportProcessModel().createAutomatonFromPNMLorBPMNFile(modelFile, labelMapping, inverseLabelMapping);
        }
        else
        {
           decompositions.importAndDecomposeModelForConformanceChecking((Petrinet) petrinetAndMarking[0],(Marking) petrinetAndMarking[1],this.labelMapping, this.inverseLabelMapping);
            // reachabilityGraph = new ImportProcessModel().createFSMfromPetrinet((Petrinet) petrinetAndMarking[0],(Marking) petrinetAndMarking[1],this.labelMapping, this.inverseLabelMapping);
        }
        this.petrinetAndMarking=decompositions.getPnetAndMarking();
        this.reachabilityGraph=decompositions.modelFSM;
    }

    private void deriveRepetitivePatterns()
    {
        reductions = new UnifiedMap<>();
        reducedTraces = new UnifiedMap<>();
        IntObjectHashMap<UnifiedSet<DecodeTandemRepeats>> redTr;
        UnifiedSet<DecodeTandemRepeats> setDecoders;
        UnifiedMap<IntIntHashMap,UnifiedSet<IntArrayList>> configReducedTraceMapping = new UnifiedMap<>();
        IntDAFSAInt fsa = new IntDAFSAInt();
        DecodeTandemRepeats decoder;
        nTRs=0;
        for(IntArrayList trace : uniqueTraces)
        {
            //uniqueTraces++;
            decoder = new DecodeTandemRepeats(trace, 0, trace.size());
            if(decoder.doCompression) nTRs+=decoder.startReduce.size();
            //reductionStats.add(decoder.getReductionLength());
            //traceLengthStats.add(decoder.trace().size());
            //redTraceLengthStats.add(decoder.reducedTrace().size());
            if((redTr = reductions.get(decoder.reducedTrace()))==null)
            {
                //uniqueTRTraces++;
                redTr = new IntObjectHashMap<>();
                reductions.put(decoder.reducedTrace(),redTr);
                reducedTraces.put(decoder.reducedTrace(),new ReducedTrace(decoder,uniqueTraceCounts.get(trace), labelMapping,inverseLabelMapping,caseIDs));
                fsa.addMinWord(decoder.reducedTrace());
                UnifiedSet<IntArrayList> reducedTraces;
                if((reducedTraces = configReducedTraceMapping.get(decoder.finalReducedConfiguration))==null)
                {
                    reducedTraces = new UnifiedSet<>();
                    configReducedTraceMapping.put(decoder.finalReducedConfiguration, reducedTraces);
                }
                reducedTraces.add(decoder.reducedTrace());
            }
            else
            {
                reducedTraces.get(decoder.reducedTrace()).addToTraceCount(uniqueTraceCounts.get(trace));
            }
            if((setDecoders=redTr.get(decoder.getReductionLength()))==null)
            {
                setDecoders = new UnifiedSet<>();
                redTr.put(decoder.getReductionLength(),setDecoders);
            }
            setDecoders.add(decoder);
        }
        labelMapping = inverseLabelMapping.inverse();

        nExtendedTraces=0;
        nRepPatterns=0;
        avgLabels=0;
        nRepPatternsTotalTraceCount=0;
        for(ReducedTrace redTrace : reducedTraces.values())
        {
            if(redTrace.getRepetitivePatterns().isEmpty()) continue;
            nExtendedTraces++;
            for(RepetitivePattern repPattern : redTrace.getRepetitivePatterns()) {
                nRepPatterns++;
                nRepPatternsTotalTraceCount += redTrace.getTraceCount();
                avgLabels+=repPattern.getMaxFulFillment();
            }
        }
        avgLabels=avgLabels/nRepPatterns;
        //this.reductionLength=reductionStats.average();
        //System.out.println("Stats - Avg. : " + reductionStats.average() + "; Max : " + reductionStats.max() + "; Med. : " + reductionStats.median());
        //System.out.println("Stats - Avg. : " + traceLengthStats.average() + "; Max : " + traceLengthStats.max() + "; Med. : " + traceLengthStats.median());
        //System.out.println("Stats - Avg. : " + redTraceLengthStats.average() + "; Max : " + redTraceLengthStats.max() + "; Med. : " + redTraceLengthStats.median());
        //System.out.println("Unique Traces : " + uniqueTraces);
        //System.out.println("Unique TR Traces : " + uniqueTRTraces);
        //this.prepareLogAutomaton(fsa);
        //this.reducedDAFSA = new Automaton(stateMapping, labelMapping, inverseLabelMapping, transitionMapping, initialState, finalStates, caseTracesMapping, caseIDs, reductions, configReducedTraceMapping);
    }

    private void deriveConcurrentPatterns()
    {
        configPartialOrderMapping = new UnifiedMap<>();
        configTransitivePartialOrderMapping = new UnifiedMap<>();
        UnifiedSet<PartialOrder> configPartialOrders,configTransitivePartialOrders=null;
        int uniqueConcurrentTraces=0;
        if(useGlobal) {
            GlobalOracleWrapper globalOracle = null;
            //GlobalOracleWrapper globalOracle = new GlobalOracleWrapper(xLog,labelMapping, inverseLabelMapping);
            if(applyAlphaPlusPlusOracle)
                globalOracle = new GlobalOracleWrapper(uniqueTraceCompletenessMapping,labelMapping);
            else
                globalOracle = new GlobalOracleWrapper(uniqueTraces,labelMapping,noiseThreshold);
            nConcPairsGlobal = globalOracle.concurrentPairs().size();

            for(IntIntHashMap configuration : configTracesMapping.keySet())
            {
                configPartialOrders = new UnifiedSet<>();
                configPartialOrderMapping.put(configuration,configPartialOrders);
                if(includeTransitiveConcurrencyPatterns) {
                    configTransitivePartialOrders = new UnifiedSet<>();
                    configTransitivePartialOrderMapping.put(configuration, configTransitivePartialOrders);
                }
                for (IntArrayList trace : configTracesMapping.get(configuration)) {
                    boolean alreadyContained=false;
                    for(PartialOrder partialOrder : configPartialOrders)
                    {
                        if(partialOrder.representativeTraces().contains(trace))
                        {
                            partialOrder.addToTraceCount(uniqueTraceCounts.get(trace));
                            alreadyContained = true;
                            break;
                        }
                    }
                    if(alreadyContained) continue;
                    PartialOrder partialOrder = new PartialOrder(trace, globalOracle.concurrentPairs(), inverseLabelMapping, caseTracesMapping, caseIDs,uniqueTraceCounts.get(trace));
                    uniqueConcurrentTraces+=partialOrder.representativeTraces().size();
                    configPartialOrders.add(partialOrder);
                    if(includeTransitiveConcurrencyPatterns) {
                        boolean containsLabelsforTransitiveConcurrency = false;
                        for (IntHashSet concurrentLabels : globalOracle.getTransitiveConcurrentPairsWithSupport()) {
                            if (trace.containsAll(concurrentLabels)) {
                                containsLabelsforTransitiveConcurrency = true;
                                break;
                            }
                        }
                        if (!containsLabelsforTransitiveConcurrency) continue;
                        PartialOrder partialOrderWithTransitiveConcurrencies = new PartialOrder(trace, globalOracle.concurrentPairs(), globalOracle.getTransitiveConcurrentPairsWithSupport(),
                                inverseLabelMapping, caseTracesMapping, caseIDs, uniqueTraceCounts.get(trace));
                        configTransitivePartialOrders.add(partialOrderWithTransitiveConcurrencies);
                    }
                }
            }
            nPartialOrders=0;
            nConcPatterns=0;
            nConcUnqTraces=0;
            nConcTotalTraces=0;
            for(IntIntHashMap config : configPartialOrderMapping.keySet())
            {
                for(PartialOrder partialOrder : configPartialOrderMapping.get(config))
                {
                    if(partialOrder.getConcurrentPatterns().isEmpty()) continue;
                    nPartialOrders++;
                    nConcUnqTraces+=partialOrder.representativeTraces().size();
                    for(ConcurrentPattern concPattern : partialOrder.getConcurrentPatterns())
                    {
                        nConcPatterns++;
                        nConcTotalTraces+=partialOrder.getTraceCount();
                    }
                }
            }
        }
        else
        {
            LocalOracleWrapper localOracle = new LocalOracleWrapper(xLog,labelMapping,inverseLabelMapping,occurence,balance);
            nContexts=localOracle.nContexts;
            avgConcPairsLocal=localOracle.avgConcPairs;
            for(IntIntHashMap configuration : configTracesMapping.keySet())
            {
                configPartialOrders = new UnifiedSet<>();
                configPartialOrderMapping.put(configuration,configPartialOrders);
                if(includeTransitiveConcurrencyPatterns) {
                    configTransitivePartialOrders = new UnifiedSet<>();
                    configTransitivePartialOrderMapping.put(configuration, configTransitivePartialOrders);
                }
                for(IntArrayList trace : configTracesMapping.get(configuration))
                {
                    boolean alreadyContained=false;
                    for(PartialOrder partialOrder : configPartialOrders) {
                        if (partialOrder.representativeTraces().contains(trace)) {
                            partialOrder.addToTraceCount(uniqueTraceCounts.get(trace));
                            alreadyContained = true;
                            break;
                        }
                    }
                    if(alreadyContained) continue;
                    PartialOrder partialOrder = new PartialOrder(trace,localOracle.configConcurrentPairsMapping().get(configuration), inverseLabelMapping, caseTracesMapping, caseIDs,uniqueTraceCounts.get(trace));
                    uniqueConcurrentTraces+=partialOrder.representativeTraces().size();
                    configPartialOrders.add(partialOrder);
                    if(includeTransitiveConcurrencyPatterns) {
                        boolean containsLabelsforTransitiveConcurrency = false;
                        if(localOracle.getTransitiveConcurrentPairsWithSupport().containsKey(configuration)) {
                            for (IntHashSet concurrentLabels : localOracle.getTransitiveConcurrentPairsWithSupport().get(configuration)) {
                                if (trace.containsAll(concurrentLabels)) {
                                    containsLabelsforTransitiveConcurrency = true;
                                    break;
                                }
                            }
                        }
                        if (!containsLabelsforTransitiveConcurrency) continue;
                        PartialOrder partialOrderWithTransitiveConcurrencies = new PartialOrder(trace, localOracle.configConcurrentPairsMapping().get(configuration), localOracle.getTransitiveConcurrentPairsWithSupport().get(configuration),
                                inverseLabelMapping, caseTracesMapping, caseIDs, uniqueTraceCounts.get(trace));
                        configTransitivePartialOrders.add(partialOrderWithTransitiveConcurrencies);
                    }
                }
            }
            nPartialOrdersLocal=0;
            nConcPatternsLocal=0;
            nConcUnqTracesLocal=0;
            nConcTotalTracesLocal=0;
            for(IntIntHashMap config : configPartialOrderMapping.keySet())
            {
                for(PartialOrder partialOrder : configPartialOrderMapping.get(config))
                {
                    if(partialOrder.getConcurrentPatterns().isEmpty()) continue;
                    nPartialOrdersLocal++;
                    nConcUnqTracesLocal+=partialOrder.representativeTraces().size();
                    for(ConcurrentPattern concPattern : partialOrder.getConcurrentPatterns())
                    {
                        nConcPatternsLocal++;
                        nConcTotalTracesLocal+=partialOrder.getTraceCount();
                    }
                }
            }
        }
        System.out.println(uniqueConcurrentTraces);
    }

    private void deriveLogInformation() throws Exception
    {
        if(xLog==null) xLog = importer.importEventLog(logFile);
        uniqueTraces = new UnifiedSet<IntArrayList>();
        uniqueTraceCounts = new ObjectIntHashMap();
        configTracesMapping = new UnifiedMap<>();
        caseIDs = new UnifiedMap<>();
        caseTracesMapping = new UnifiedMap<>();
        IntArrayList traces;
        //traceIDtraceName = new IntObjectHashMap<String>();
        labelMapping = HashBiMap.create();
        if(inverseLabelMapping==null) inverseLabelMapping = HashBiMap.create();
        String eventName;
        String traceID;
        int translation = inverseLabelMapping.size();
        int iTransition = 0;
        IntArrayList tr;
        IntDAFSAInt fsa = new IntDAFSAInt();
        Integer key = null;
        int it = 0;
        XTrace trace;
        int i, j;
        nEvents=0; nUnqEvents=0; nTraces=0; nUnqTraces=0;

        Iterator<XTrace> traceIterator = xLog.iterator();
        applyAlphaPlusPlusOracle = true;
        while(traceIterator.hasNext())
        {
            XTrace xtrace = traceIterator.next();
            //System.out.println(xtrace.size());
            for(XEvent xEvent : xtrace)
            {
                if(!xEvent.getAttributes().containsKey(lifecycle) ||
                        !(xEvent.getAttributes().get(lifecycle).equals("start") || xEvent.getAttributes().get(lifecycle).equals("complete")))
                {
                    applyAlphaPlusPlusOracle=false;
                }
                addMissingLifecycleTransition(xEvent);
            }
            //getAsList(xtrace);
            //if(xtrace.isEmpty())
            //    traceIterator.remove();

        }
        BooleanArrayList eventCompletion = null;
        if(applyAlphaPlusPlusOracle)
        {
            uniqueTraceCompletenessMapping=new UnifiedMap<>();
        }
        for (i = 0; i < xLog.size(); i++)
        {
            trace = xLog.get(i);
            nTraces++;
            XAttributeLiteral xAt;
            if(((xAt = (XAttributeLiteral) trace.getAttributes().get(conceptname))) == null)
            {
                traceID = "" + it;
            }
            else
            {
                traceID = xAt.getValue();
            }
            tr = new IntArrayList(trace.size());
            if(applyAlphaPlusPlusOracle) eventCompletion=new BooleanArrayList();
            for (j = 0; j < trace.size(); j++)
            {
                eventName = ((XAttributeLiteral) trace.get(j).getAttributes().get(conceptname)).getValue();//xce.extractName(event);
                nEvents++;
                if((key = (inverseLabelMapping.get(eventName))) == null)
                {
                    //labelMapping.put(translation, eventName);
                    inverseLabelMapping.put(eventName, translation);
                    key = translation;
                    translation++;
                    nUnqEvents++;
                }
                tr.add(key);
                if(applyAlphaPlusPlusOracle)
                {
                    boolean completion = ((XAttributeLiteral) trace.get(j).getAttributes().get(lifecycle)).getValue()=="complete";
                    eventCompletion.add(completion);
                }
            }
            uniqueTraceCounts.addToValue(tr,1);
            if(uniqueTraces.add(tr))
            {
                nUnqTraces++;
                IntIntHashMap configuration = new IntIntHashMap();
                for(int label : tr.distinct().toArray())
                    configuration.put(label,tr.count(l->l==label));
                UnifiedSet<IntArrayList> configTraces = configTracesMapping.get(configuration);
                if(configTraces==null)
                {
                    configTraces = new UnifiedSet<>();
                    configTracesMapping.put(configuration,configTraces);
                }
                configTraces.add(tr);
                if(applyAlphaPlusPlusOracle)
                {
                    uniqueTraceCompletenessMapping.put(tr,eventCompletion);
                }
            }
            //if(visited.add(tr))
            //  fsa.addMinWord(tr);
            //caseTracesMapping.put(tr, tracesLabelsMapping.get(trace));
            if((traces = caseTracesMapping.get(tr))==null)
            {
                traces = new IntArrayList();
                caseTracesMapping.put(tr, traces);
            }
            traces.add(it);
            caseIDs.put(it, traceID);
            it++;
            //listTraces.add(traceLabels);

//			if((traces = tracesLabelsMapping.get(traceLabels))==null)
//			{
//				traces = new IntArrayList();
//				tracesLabelsMapping.put(traceLabels, traces);
//			}
//			traces.add(it);
        }
        labelMapping = inverseLabelMapping.inverse();
    }

    private void prepareLogAutomaton(IntDAFSAInt fsa) throws IOException {
        int i;
        int iTransition = 0;
        int idest=0;
        int ilabel=0;
        stateMapping = HashBiMap.create();
        transitionMapping = HashBiMap.create();
        finalStates = new IntHashSet();
        for(AbstractIntDAFSA.State n : fsa.getStates())
        {
            if(!(n.outbound()==0 && (!fsa.isFinalState(n.getNumber()))))
            {
                if(!stateMapping.containsKey(n.getNumber()))
                    stateMapping.put(n.getNumber(), new State(n.getNumber(), fsa.isSource(n.getNumber()), fsa.isFinalState(n.getNumber())));
                if(initialState !=0 && fsa.isSource(n.getNumber())){initialState = n.getNumber(); }
                if(fsa.isFinalState(n.getNumber())){finalStates.add(n.getNumber());}
                for(i = 0; i < n.outbound(); i++)
                {
                    idest = AbstractIntDAFSA.decodeDest(n.next.get(i));
                    ilabel = AbstractIntDAFSA.decodeLabel(n.next.get(i));

                    if (!stateMapping.containsKey(idest))
                        stateMapping.put(idest, new State(idest, fsa.isSource(idest), fsa.isFinalState(AbstractIntDAFSA.decodeDest(n.next.get(i)))));
                    iTransition++;
                    Transition t = new Transition(stateMapping.get(n.getNumber()), stateMapping.get(idest), ilabel);
                    transitionMapping.put(iTransition, t);
                    stateMapping.get(n.getNumber()).outgoingTransitions().add(t);
                    stateMapping.get(idest).incomingTransitions().add(t);
                }
            }
        }
        stateMapping.get(initialState).setFinal(true);
        //Automaton logAutomaton = new Automaton(stateMapping, labelMapping, inverseLabelMapping, transitionMapping, initialState, finalStates, caseTracesMapping, caseIDs);//, concurrencyOracle);
        //long conversion = System.nanoTime();
        //System.out.println("Log Automaton creation: " + TimeUnit.MILLISECONDS.convert((automaton - start), TimeUnit.NANOSECONDS) + "ms");
        //System.out.println("Log Automaton conversion: " + TimeUnit.MILLISECONDS.convert((conversion - automaton), TimeUnit.NANOSECONDS) + "ms");
        //return logAutomaton;
    }

    public Automaton getReducedDAFSA()
    {
        return this.reducedDAFSA;
    }

    public Automaton getReachabilityGraph()
    {
        return this.reachabilityGraph;
    }

    public UnifiedMap<IntArrayList, IntObjectHashMap<UnifiedSet<DecodeTandemRepeats>>> getReductions() {
        return reductions;
    }

    public UnifiedMap<IntIntHashMap, UnifiedSet<PartialOrder>> getConfigPartialOrderMapping() {
        return configPartialOrderMapping;
    }

    private LinkedList<String> getAsList(XTrace t) {
        LinkedList<String> list = new LinkedList();
        Iterator var4 = t.iterator();

        while(var4.hasNext()) {
            XEvent e = (XEvent)var4.next();
            if (this.isCompleteEvent(e) && e.getAttributes().get("concept:name") != null) {
                list.add(this.getEventName(e));
            }
        }

        if(list.isEmpty())
            System.out.println("Problem");
        if (!((String)list.getFirst()).equals("###$$$%%%$$$###START###$$$%%%$$$###")) {
            list.addFirst("###$$$%%%$$$###START###$$$%%%$$$###");
        }

        if (!((String)list.getLast()).equals("###$$$%%%$$$###END###$$$%%%$$$###")) {
            list.addLast("###$$$%%%$$$###END###$$$%%%$$$###");
        }

        return list;
    }

    private String getEventName(XEvent e) {
        String name = ((XAttribute)e.getAttributes().get("concept:name")).toString();
        if (name.contains("_end")) {
            name = name.substring(0, name.length() - 4);
        }

        return name;
    }

    private boolean isCompleteEvent(XEvent e) {
        if (((XAttribute)e.getAttributes().get("concept:name")).toString().contains("_start")) {
            return false;
        } else {
            XAttributeMap amap = e.getAttributes();
            return ((XAttribute)amap.get("lifecycle:transition")).toString().toLowerCase().equals("complete");
        }
    }

    private void addMissingLifecycleTransition(XEvent e)
    {
        if(xFac==null) {
            xFac = new XFactoryNaiveImpl();
            xtend = XConceptExtension.instance();
            xLc = xFac.createAttributeLiteral(lifecycle, "", xtend);
            xLc.setValue("complete");
        }
        if(!e.getAttributes().containsKey(lifecycle) || !e.getAttributes().get(lifecycle).equals("start"))
            e.getAttributes().put(lifecycle,xLc);
    }

    public UnifiedMap<IntArrayList, BooleanArrayList> getUniqueTraceCompletenessMapping() {
        return uniqueTraceCompletenessMapping;
    }

    public boolean useGlobal()
    {
        return this.useGlobal;
    }

    @Override
    public String call() throws Exception {
        return collectLogStatisticsLocal();
    }
}
