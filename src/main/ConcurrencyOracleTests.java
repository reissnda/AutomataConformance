package main;

import au.qut.apromore.automaton.Automaton;
import au.qut.apromore.importer.ImportEventLog;
import com.google.common.collect.Multiset;
import ee.ut.org.processmining.framework.util.Pair;
import au.unimelb.partialorders.ConcurrencyHierarchyTreeWithSupport;
import mytree.ConcurrentHierarchyLabelsTree;
import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import qut.au.automaton.factory.state.MultisetFPFactory;
import qut.au.oracle.alpha.AlphaRelations;
import qut.au.oracle.global.GlobalOracle;
import qut.au.oracle.local.LocalOracle;
import qut.au.oracle.local.scopes.Scope;
import qut.au.oracle.local.validation.CoOccurrenceValidator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConcurrencyOracleTests
{
    public static void main(String[] args) throws Exception
    {
        //Local concurrency oracle test
        String logFile = "/Users/dreissner/Documents/Evaluations/Tests/ConcurrencyOracleTests/ConcurrencyTestIncomplete.xes";
        //String logFile = "/Users/dreissner/Documents/Evaluations/Tests/ConcurrencyOracleTests/ConcurrencyTest.xes";
        //String logFile = "/Users/dreissner/Documents/Evaluations/Tests/ConcurrencyOracleTests/ConcurrencyTestIncompleteFourActivities.xes";
        //String logFile = "/Users/dreissner/Documents/Evaluations/testing/artificial/artificial.xes";
        printLocalConcurrencyResults(logFile);
        //testPublicDataset();
        //testTransitiveConcurrencyLocal(logFile);
        //printAlphaConcurrencyResults(logFile);
        //testTransitiveConcurrencyGlobal(logFile);
        //testTransitiveConcurrencyGlobalWithSupport(logFile);
        //IntArrayList trace = IntArrayList.newListWith(0,1,2,3);
        //UnifiedSet<IntHashSet> concurrencies = UnifiedSet.newSetWith(IntHashSet.newSetWith(1,2));
        //PartialOrder po = new PartialOrder(trace, concurrencies);
        //po.toDot("/Users/dreissner/Documents/Evaluations/Tests/ConcurrencyOracleTests/po.dot");
    }

    public static void printAlphaConcurrencyResults(String logFile) throws Exception
    {
        XLog log = new ImportEventLog().importEventLog(logFile);

        GlobalOracle globalOracle = new GlobalOracle("Alpha-oracle-test", new AlphaRelations(), log);
        System.out.println(globalOracle.getConcurrencyRelations());
    }

    public static void printLocalConcurrencyResults(String logFile) throws Exception
    {
        XLog log = new ImportEventLog().importEventLog(logFile);
        //long start = System.nanoTime();
        LocalOracle oracle = new LocalOracle("kjhsd", log, new MultisetFPFactory(), new AlphaRelations(), new CoOccurrenceValidator(0.4F, 0.3F), (List) null);
        //oracle.get
        //System.out.println(oracle.getOracles());

        for(Map.Entry<Multiset<String>, Collection<Scope>> entry : oracle.getOracles().asMap().entrySet())
        {
            System.out.println(entry.getKey());
            System.out.println("-------------------------------------------");
            for(Scope scope : entry.getValue()) {
                System.out.println("Start: " + scope.getStartMS());
                System.out.println("End: " + scope.getEndMS());
                System.out.println("Concurrent events: " + scope.getConcurrentPairs());
                //System.out.println(scope.);
                System.out.println("-------------------------------------------");
            }
            //System.out.print(entry.getValue());
            //System.out.println();
        }
        System.out.println(oracle.getOracles().size());
        //System.out.println(TimeUnit.SECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS));
    }

    public static void testTransitiveConcurrencyLocal(String logFile) throws Exception
    {
        XLog log = new ImportEventLog().importEventLog(logFile);
        //long start = System.nanoTime();
        LocalOracle oracle = new LocalOracle("kjhsd", log, new MultisetFPFactory(), new AlphaRelations(), new CoOccurrenceValidator(0.4F, 0.3F), (List) null);
        computeTransitiveConcurrencyTree(oracle);
    }

    public static void testTransitiveConcurrencyGlobal(String logFile) throws Exception
    {
        XLog log = new ImportEventLog().importEventLog(logFile);
        GlobalOracle globalOracle = new GlobalOracle("Alpha-oracle-test", new AlphaRelations(), log);
        computeTransitiveConcurrencyTree(globalOracle);
    }

    public static void testTransitiveConcurrencyGlobalWithSupport(String logFile) throws Exception
    {
        ImportEventLog importer = new ImportEventLog();
        XLog log = importer.importEventLog(logFile);
        Automaton dafsa = importer.createDAFSAfromLog(log);
        GlobalOracle oracle = new GlobalOracle("Alpha-Oracle-Test", new AlphaRelations(), log);
        computeTransitiveConcurrencyTreeWithSupport(oracle, dafsa);
    }

    public static void computeTransitiveConcurrencyTreeWithSupport(GlobalOracle oracle, Automaton dafsa)
    {
        ConcurrencyHierarchyTreeWithSupport concurrencyTree = new ConcurrencyHierarchyTreeWithSupport(dafsa.eventLabels());
        UnifiedSet<Pair<Integer, Integer>> secondPair = new UnifiedSet<Pair<Integer, Integer>>();
        for(String firstConcurrentLabel : oracle.getConcurrencyRelations().keySet())
        {
            for(String secondConcurrentLabel : oracle.getConcurrencyRelations().get(firstConcurrentLabel))
            {
                int firstLabelID = dafsa.inverseEventLabels().get(firstConcurrentLabel);
                int secondLabelID = dafsa.inverseEventLabels().get(secondConcurrentLabel);
                if(secondPair.contains(new Pair(firstLabelID,secondLabelID))) continue;
                secondPair.add(new Pair(secondLabelID,firstLabelID));
                System.out.println(firstConcurrentLabel + " - " + secondConcurrentLabel);
                concurrencyTree.addConcurrencyRelation(firstLabelID,secondLabelID);
            }
        }
        //for(Pair<Integer, Integer> concurrentLabelPair : secondPair)
        //{
        //    System.out.println(dafsa.eventLabels().get(concurrentLabelPair.getFirst()) + " - " + dafsa.eventLabels().get(concurrentLabelPair.getSecond()));
        //    concurrencyTree.addConcurrencyRelation(IntHashSet.newSetWith(concurrentLabelPair.getFirst()),concurrentLabelPair.getSecond());
        //}
        concurrencyTree.printMostSupportedConcurrencies();
    }

    public static void computeTransitiveConcurrencyTreeWithLabelConversion(GlobalOracle oracle, Automaton dafsa)
    {
        ConcurrentHierarchyLabelsTree concurrencyTree;
        int countHigherLevelConcurrencyRelations = 0;
        concurrencyTree = new ConcurrentHierarchyLabelsTree();
        for(String firstConcurrentLabel : oracle.getConcurrencyRelations().keySet())
        {
            int firstLabelID = dafsa.inverseEventLabels().get(firstConcurrentLabel);
            for(String secondConcurrentLabel : oracle.getConcurrencyRelations().get(firstConcurrentLabel))
            {
                concurrencyTree.addConcurrencyRelation(UnifiedSet.newSetWith(firstConcurrentLabel), secondConcurrentLabel);
            }

        }
        countHigherLevelConcurrencyRelations += concurrencyTree.getCountTransitiveConcurrentPairs();

        System.out.println(countHigherLevelConcurrencyRelations);
    }

    public static void computeTransitiveConcurrencyTree(GlobalOracle oracle)
    {
        ConcurrentHierarchyLabelsTree concurrencyTree;
        int countHigherLevelConcurrencyRelations = 0;
        concurrencyTree = new ConcurrentHierarchyLabelsTree();
        for(String firstConcurrentLabel : oracle.getConcurrencyRelations().keySet())
        {

                    for(String secondConcurrentLabel : oracle.getConcurrencyRelations().get(firstConcurrentLabel))
                    {
                        concurrencyTree.addConcurrencyRelation(UnifiedSet.newSetWith(firstConcurrentLabel),secondConcurrentLabel);
                    }

            }
        countHigherLevelConcurrencyRelations += concurrencyTree.getCountTransitiveConcurrentPairs();

        System.out.println(countHigherLevelConcurrencyRelations);
    }

    public static void computeTransitiveConcurrencyTree(LocalOracle oracle)
    {
        //map get label every set add second label?
        ConcurrentHierarchyLabelsTree concurrencyTree;
        int countHigherLevelConcurrencyRelations = 0;
        for(Map.Entry<Multiset<String>, Collection<Scope>> configuration : oracle.getOracles().asMap().entrySet())
        {
            concurrencyTree = new ConcurrentHierarchyLabelsTree();
            for(Scope scope : configuration.getValue())
            {
                for(String firstConcurrentLabel : scope.getConcurrentPairs().keySet())
                {
                    for(String secondConcurrentLabel : scope.getConcurrentPairs().get(firstConcurrentLabel))
                    {
                        concurrencyTree.addConcurrencyRelation(UnifiedSet.newSetWith(firstConcurrentLabel),secondConcurrentLabel);
                    }
                }
            }
            countHigherLevelConcurrencyRelations += concurrencyTree.getCountTransitiveConcurrentPairs();
        }
        System.out.println(countHigherLevelConcurrencyRelations);
    }

    public static void computeLocalTransitiveConcurrencyTreeWithSupport(String logFile) throws Exception
    {
        ImportEventLog importer = new ImportEventLog();
        XLog log = importer.importEventLog(logFile);
        Automaton dafsa = importer.createDAFSAfromLog(log);
        LocalOracle oracle = new LocalOracle("Local-Oracle", log, new MultisetFPFactory(), new AlphaRelations(), new CoOccurrenceValidator(0.4F, 0.3F), (List) null);
        computeTransitiveConcurrencyTreeWithSupport(oracle, dafsa);
    }

    public static void computeTransitiveConcurrencyTreeWithSupport(LocalOracle oracle, Automaton dafsa)
    {
        ConcurrencyHierarchyTreeWithSupport concurrencyTree;
        int countHigherLevelConcurrencyRelations = 0;
        for(Map.Entry<Multiset<String>, Collection<Scope>> configuration : oracle.getOracles().asMap().entrySet())
        {
            concurrencyTree = new ConcurrencyHierarchyTreeWithSupport();
            UnifiedSet<Pair<Integer, Integer>> secondPair = new UnifiedSet<Pair<Integer, Integer>>();


            for(Scope scope : configuration.getValue())
            {
                for(String firstConcurrentLabel : scope.getConcurrentPairs().keySet())
                {
                    for(String secondConcurrentLabel : scope.getConcurrentPairs().get(firstConcurrentLabel))
                    {
                        int firstLabelID = dafsa.inverseEventLabels().get(firstConcurrentLabel);
                        int secondLabelID = dafsa.inverseEventLabels().get(secondConcurrentLabel);
                        if(secondPair.contains(new Pair(firstLabelID,secondLabelID))) continue;
                        secondPair.add(new Pair(secondLabelID,firstLabelID));
                        System.out.println(firstConcurrentLabel + " - " + secondConcurrentLabel);
                        concurrencyTree.addConcurrencyRelation(firstLabelID,secondLabelID);
                    }
                }
            }
            countHigherLevelConcurrencyRelations += concurrencyTree.getCountTransitiveConcurrentPairs();
        }
        System.out.println(countHigherLevelConcurrencyRelations);
    }



    public static void testPublicDataset() throws Exception
    {
        String path = "/Users/dreissner/Documents/Evaluations/TandemRepeatsPaper/public/IM/";
        for(int i=1;i<=17;i++) {
            if(i==4 || i==12 || i==13 || i==15 || i==16) continue;
            measureLocaleConcurrencyTime(path + i + ".xes.gz");
        }
    }

    public static void measureLocaleConcurrencyTime(String logFile) throws Exception
    {
        System.out.println(logFile);
        XLog log = new ImportEventLog().importEventLog(logFile);
        long start = System.nanoTime();
        LocalOracle oracle = new LocalOracle("kjhsd", log, new MultisetFPFactory(), new AlphaRelations(), new CoOccurrenceValidator(0.4F, 0.3F), (List) null);
        //oracle.get
        //System.out.println(oracle.getOracles());
        /*
        for(Map.Entry<Multiset<String>, Collection<Scope>> entry : oracle.getOracles().asMap().entrySet())
        {
            System.out.println(entry.getKey());
            System.out.println("-------------------------------------------");
            for(Scope scope : entry.getValue()) {
                System.out.println("Start: " + scope.getStartMS());
                System.out.println("End: " + scope.getEndMS());
                System.out.println("Concurrent events: " + scope.getConcurrentPairs());
                //System.out.println(scope.);
                System.out.println("-------------------------------------------");
            }
            //System.out.print(entry.getValue());
            //System.out.println();
        }*/
        System.out.println(oracle.getOracles().size());
        System.out.println(TimeUnit.SECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS));
    }
}
