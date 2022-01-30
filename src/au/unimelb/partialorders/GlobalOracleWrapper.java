package au.unimelb.partialorders;

import au.qut.apromore.automaton.Automaton;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ee.ut.org.processmining.framework.util.Pair;
import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.list.mutable.primitive.BooleanArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import qut.au.oracle.alpha.AlphaRelations;
import qut.au.oracle.global.GlobalOracle;

public class GlobalOracleWrapper
{
    private XLog xLog;
    private UnifiedSet< IntArrayList > uniqueTraces;
    private UnifiedMap<IntArrayList, BooleanArrayList > uniqueTraceCompletenessMapping;
    private GlobalOracle oracle;
    private BiMap<String, Integer> inverseLabelMapping;
    private BiMap<Integer,String> labelMapping;
    private UnifiedSet<IntHashSet> concurrentPairs;

    public UnifiedSet<IntHashSet> getTransitiveConcurrentPairsWithSupport() {
        return transitiveConcurrentPairsWithSupport;
    }

    private UnifiedSet<IntHashSet> transitiveConcurrentPairsWithSupport;
    private ConcurrencyHierarchyTreeWithSupport concurrencyTree;

    public GlobalOracleWrapper(XLog xLog, BiMap<Integer,String> labelMapping, BiMap<String, Integer> inverseLabelMapping)
    {
        this.xLog = xLog;
        this.labelMapping = labelMapping;
        this.inverseLabelMapping = inverseLabelMapping;
        this.oracle = new GlobalOracle("Alpha+-oracle-test", new AlphaRelations(), xLog);
        translateAndPrepareConcurrentPairs();
        //deriveTransitiveConcurrencies();
    }

    public GlobalOracleWrapper(UnifiedSet< IntArrayList > uniqueTraces, BiMap<Integer,String> labelMapping)
    {
        this.uniqueTraces=uniqueTraces;
        this.labelMapping=labelMapping;
        au.unimelb.oracles.GlobalOracle oracle = new au.unimelb.oracles.GlobalOracle(uniqueTraces, labelMapping);
        this.concurrentPairs = oracle.getConcurrentPairs();
    }

    public GlobalOracleWrapper(UnifiedMap<IntArrayList, BooleanArrayList > uniqueTraceCompletenessMapping, BiMap<Integer, String> labelMapping)
    {
        this.uniqueTraceCompletenessMapping = uniqueTraceCompletenessMapping;
        this.labelMapping = labelMapping;
        au.unimelb.oracles.GlobalOracle oracle = new au.unimelb.oracles.GlobalOracle(uniqueTraceCompletenessMapping, labelMapping);
        this.concurrentPairs=oracle.getConcurrentPairs();
    }

    public GlobalOracleWrapper(UnifiedSet< IntArrayList > uniqueTraces, BiMap<Integer, String> labelMapping, double noiseThreshold)
    {
        this.uniqueTraceCompletenessMapping = uniqueTraceCompletenessMapping;
        this.labelMapping = labelMapping;
        au.unimelb.oracles.GlobalOracle oracle = new au.unimelb.oracles.GlobalOracle(uniqueTraces, labelMapping, noiseThreshold);
        this.concurrentPairs=oracle.getConcurrentPairs();
    }

    public void deriveTransitiveConcurrencies()
    {
        concurrencyTree = new ConcurrencyHierarchyTreeWithSupport(this.labelMapping);
        UnifiedSet<Pair<Integer, Integer>> secondPair = new UnifiedSet<Pair<Integer, Integer>>();
        for(String firstConcurrentLabel : oracle.getConcurrencyRelations().keySet())
        {
            for(String secondConcurrentLabel : oracle.getConcurrencyRelations().get(firstConcurrentLabel))
            {
                int firstLabelID = this.inverseLabelMapping.get(firstConcurrentLabel);
                int secondLabelID = this.inverseLabelMapping.get(secondConcurrentLabel);
                if(secondPair.contains(new Pair(firstLabelID,secondLabelID))) continue;
                secondPair.add(new Pair(secondLabelID,firstLabelID));
                //System.out.println(firstConcurrentLabel + " - " + secondConcurrentLabel);
                concurrencyTree.addConcurrencyRelation(firstLabelID,secondLabelID);
            }
        }
        //for(Pair<Integer, Integer> concurrentLabelPair : secondPair)
        //{
        //    System.out.println(dafsa.eventLabels().get(concurrentLabelPair.getFirst()) + " - " + dafsa.eventLabels().get(concurrentLabelPair.getSecond()));
        //    concurrencyTree.addConcurrencyRelation(IntHashSet.newSetWith(concurrentLabelPair.getFirst()),concurrentLabelPair.getSecond());
        //}
        this.transitiveConcurrentPairsWithSupport = concurrencyTree.getTransitiveConcurrenciesWithSupport();
    }

    private void translateAndPrepareConcurrentPairs()
    {
        concurrentPairs = new UnifiedSet<>();
        IntHashSet concurrentPair;
        for(String firstConcurrentLabel : oracle.getConcurrencyRelations().keySet())
        {
            for(String secondConcurrentLabel : oracle.getConcurrencyRelations().get(firstConcurrentLabel))
            {
                int firstLabelID = this.inverseLabelMapping.get(firstConcurrentLabel);
                int secondLabelID = this.inverseLabelMapping.get(secondConcurrentLabel);
                if(firstConcurrentLabel==secondConcurrentLabel) continue;
                concurrentPair = IntHashSet.newSetWith(firstLabelID, secondLabelID);
                concurrentPairs.add(concurrentPair);
            }
        }
    }

    public UnifiedSet<IntHashSet> concurrentPairs()
    {
        return this.concurrentPairs;
    }
}
