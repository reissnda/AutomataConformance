package au.unimelb.partialorders;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multiset;
import ee.ut.org.processmining.framework.util.Pair;
import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import qut.au.automaton.factory.state.MultisetFPFactory;
import qut.au.oracle.alpha.AlphaRelations;
import qut.au.oracle.local.LocalOracle;
import qut.au.oracle.local.scopes.Scope;
import qut.au.oracle.local.validation.CoOccurrenceValidator;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class LocalOracleWrapper
{
    private XLog xLog;
    private LocalOracle oracle;
    private BiMap<String, Integer> inverseLabelMapping;
    private BiMap<Integer,String> labelMapping;
    private UnifiedMap<IntIntHashMap, UnifiedSet<IntHashSet>> configConcurrentPairsMapping;
    public int nContexts=0;
    public double avgConcPairs=0;
    public UnifiedMap<IntIntHashMap,UnifiedSet<IntHashSet>> getTransitiveConcurrentPairsWithSupport() {
        return transitiveConcurrentPairsWithSupport;
    }

    private UnifiedMap<IntIntHashMap,UnifiedSet<IntHashSet>> transitiveConcurrentPairsWithSupport;

    private UnifiedMap<IntIntHashMap,ConcurrencyHierarchyTreeWithSupport> concurrencyTrees;

    public LocalOracleWrapper(XLog xLog, BiMap<Integer,String> labelMapping, BiMap<String, Integer> inverseLabelMapping)
    {
        this.xLog = xLog;
        this.inverseLabelMapping = inverseLabelMapping;
        this.labelMapping = labelMapping;
        this.oracle = new LocalOracle("Local concurrency oracle", xLog, new MultisetFPFactory(), new AlphaRelations(), new CoOccurrenceValidator(0.55F, 0.3F), (List) null);
        translateAndPrepareConcurrentPairs();
        //deriveTransitiveConcurrencies();
    }

    public LocalOracleWrapper(XLog xLog, BiMap<Integer,String> labelMapping, BiMap<String, Integer> inverseLabelMapping, float occurence, float balance)
    {
        this.xLog = xLog;
        this.inverseLabelMapping = inverseLabelMapping;
        this.labelMapping = labelMapping;
        this.oracle = new LocalOracle("Local concurrency oracle", xLog, new MultisetFPFactory(), new AlphaRelations(), new CoOccurrenceValidator(occurence, balance), (List) null);
        translateAndPrepareConcurrentPairs();
        //deriveTransitiveConcurrencies();
    }

    private void deriveTransitiveConcurrencies()
    {
        //IntIntHashMap configuration;
        UnifiedSet<IntHashSet> concurrentPairs;
        //IntHashSet concurrentPair;
        concurrencyTrees = new UnifiedMap<>();
        transitiveConcurrentPairsWithSupport=new UnifiedMap<>();

        for(IntIntHashMap configuration : configConcurrentPairsMapping.keySet()) {
            ConcurrencyHierarchyTreeWithSupport concurrencyTree = new ConcurrencyHierarchyTreeWithSupport(this.labelMapping);
            concurrencyTrees.put(configuration, concurrencyTree);
            UnifiedSet<Pair<Integer, Integer>> secondPair = new UnifiedSet<Pair<Integer, Integer>>();
            for (IntHashSet concurrentPair : configConcurrentPairsMapping.get(configuration)) {
                int firstLabelID = concurrentPair.min();
                int secondLabelID = concurrentPair.max();
                if (secondPair.contains(new Pair(firstLabelID, secondLabelID))) continue;
                secondPair.add(new Pair(secondLabelID, firstLabelID));
                //System.out.println(firstConcurrentLabel + " - " + secondConcurrentLabel);
                concurrencyTree.addConcurrencyRelation(firstLabelID, secondLabelID);
            }

            UnifiedSet<IntHashSet> configTransitiveConcurrentPairsWithSupport = concurrencyTree.getTransitiveConcurrenciesWithSupport();
            this.transitiveConcurrentPairsWithSupport.put(configuration, configTransitiveConcurrentPairsWithSupport);
        }
    }

    private void translateAndPrepareConcurrentPairs()
    {
        IntIntHashMap configuration;
        UnifiedSet<IntHashSet> concurrentPairs;
        IntHashSet concurrentPair;
        configConcurrentPairsMapping = new UnifiedMap<>();

        for(Map.Entry<Multiset<String>, Collection<Scope>> entry : oracle.getOracles().asMap().entrySet())
        {
            configuration = new IntIntHashMap();
            for(String label : entry.getKey())
            {
                if(this.inverseLabelMapping.containsKey(label))
                    configuration.addToValue(this.inverseLabelMapping.get(label),1);
            }
            nContexts++;
            concurrentPairs = new UnifiedSet<>();
            for(Scope scope : entry.getValue())
            {
                for(String firstConcurrentLabel : scope.getConcurrentPairs().keySet())
                {
                    for(String secondConcurrentLabel : scope.getConcurrentPairs().get(firstConcurrentLabel))
                    {
                        int first = inverseLabelMapping.get(firstConcurrentLabel);
                        int second = inverseLabelMapping.get(secondConcurrentLabel);
                        if(first==second) continue;
                        concurrentPair = new IntHashSet();
                        concurrentPair.add(first);
                        concurrentPair.add(second);
                        if(concurrentPairs.add(concurrentPair)) avgConcPairs++;
                    }
                }
            }
            configConcurrentPairsMapping.put(configuration,concurrentPairs);
            //System.out.print(entry.getValue());
            //System.out.println();
        }
        avgConcPairs=avgConcPairs/nContexts;
    }

    public UnifiedMap<IntIntHashMap, UnifiedSet<IntHashSet>> configConcurrentPairsMapping() {
        return configConcurrentPairsMapping;
    }
}
