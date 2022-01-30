package au.unimelb.oracles;

import au.unimelb.basicstructures.IntIntPair;
import com.google.common.collect.BiMap;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.list.mutable.primitive.BooleanArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntDoubleHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectBooleanHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

public class GlobalOracle
{
    private  double noiseThreshold;
    private UnifiedSet<IntArrayList> uniqueTraces;
    private UnifiedMap<IntArrayList, BooleanArrayList> uniqueTraceCompletenessMapping;
    private BiMap<Integer, String> labelMapping;
    private ObjectBooleanHashMap<IntIntPair> dfDependencies;
    private ObjectIntHashMap<IntIntPair> dfDependenciesWFrequencies;
    private ObjectBooleanHashMap<IntIntPair> shortLoops;
    private IntIntHashMap countIncoming;
    private IntIntHashMap countOutgoing;

    public UnifiedSet<IntHashSet> getConcurrentPairs() {
        return concurrentPairs;
    }

    private UnifiedSet<IntHashSet> concurrentPairs;
    private ObjectBooleanHashMap<IntIntPair> intersections;

    public GlobalOracle(UnifiedSet<IntArrayList> uniqueTraces, BiMap<Integer, String> labelMapping)
    {
        this.uniqueTraces=uniqueTraces;
        this.labelMapping=labelMapping;
        determineConcurrenciesWithAlphaPlusOracle();
    }

    public GlobalOracle(UnifiedSet<IntArrayList> uniqueTraces, BiMap<Integer, String> labelMapping, double noiseThreshold)
    {
        this.uniqueTraces=uniqueTraces;
        this.labelMapping=labelMapping;
        this.noiseThreshold=noiseThreshold;
        determineConcurrenciesWithAlphaPlusOracleAndFilters();
    }

    public GlobalOracle(UnifiedMap<IntArrayList,BooleanArrayList> uniqueTraceCompletenessMapping, BiMap<Integer, String> labelMapping)
    {
        this.uniqueTraceCompletenessMapping=uniqueTraceCompletenessMapping;
        this.labelMapping=labelMapping;
        determineConcurrenciesWithAlphaPlusPlusOracle();
    }

    private void determineConcurrenciesWithAlphaPlusPlusOracle()
    {
        initializeDfDependenciesAndIntersections();
        for(IntArrayList trace : uniqueTraceCompletenessMapping.keySet())
        {
            BooleanArrayList completenessExtensions = uniqueTraceCompletenessMapping.get(trace);
            for(int pos=0;pos<=trace.size();pos++)
            {
                int ev=trace.get(pos);
                if(completenessExtensions.get(pos)) continue;
                int pos2=pos+1;
                IntHashSet intersectedEvents = new IntHashSet();
                boolean completeEventfound=false;
                while(pos2<trace.size())
                {
                    int ev2=trace.get(pos2);
                    //int ev2Comp=completenessExtensions.get(pos2);
                    if(ev2==ev)
                    {
                        completeEventfound=true;
                        break;
                    }
                    intersectedEvents.add(ev2);
                    pos2++;
                }
                if(completeEventfound) {
                    for (int ev2 : intersectedEvents.toArray()) {
                        IntIntPair intersection = new IntIntPair(ev, ev2);
                        intersections.put(intersection, true);
                    }
                }
                pos2++;
                if(pos2<trace.size())
                {
                    int ev2 = trace.get(pos2);
                    IntIntPair df = new IntIntPair(ev,ev2);
                    dfDependencies.put(df,true);
                }
                pos=pos2-1;
            }
        }
        this.concurrentPairs=new UnifiedSet<>();
        for(int uniqueEvent : this.labelMapping.keySet())
        {
            for(int uniqueEvent2 : this.labelMapping.keySet())
            {
                IntIntPair df1 = new IntIntPair(uniqueEvent,uniqueEvent2);
                //IntIntPair df2 = new IntIntPair(uniqueEvent2,uniqueEvent);
                if(intersections.get(df1))
                {
                    concurrentPairs.add(IntHashSet.newSetWith(df1.getFirst(),df1.getSecond()));
                }
            }
        }
    }

    private void determineConcurrenciesWithAlphaPlusOracle()
    {
        initializeDfDependenciesAndShortLoops();
        for(IntArrayList trace : uniqueTraces)
        {
            for(int pos=0;pos<trace.size()-1;pos++)
            {
                int ev = trace.get(pos);
                int ev2 = trace.get(pos+1);
                IntIntPair df = new IntIntPair(ev,ev2);
                dfDependencies.put(df,true);
                if(pos+2<trace.size())
                {
                    int ev3=trace.get(pos+2);
                    if(ev==ev3)
                    {
                        shortLoops.put(df,true);
                    }
                }
            }
        }
        this.concurrentPairs=new UnifiedSet<>();
        for(int uniqueEvent : this.labelMapping.keySet())
        {
            for(int uniqueEvent2 : this.labelMapping.keySet())
            {
                if(uniqueEvent==uniqueEvent2) continue;
                IntIntPair df1 = new IntIntPair(uniqueEvent,uniqueEvent2);
                IntIntPair df2 = new IntIntPair(uniqueEvent2,uniqueEvent);
                if(dfDependencies.get(df1) && dfDependencies.get(df2) && !shortLoops.get(df1))
                {
                    concurrentPairs.add(IntHashSet.newSetWith(df1.getFirst(),df1.getSecond()));
                }
            }
        }
    }

    private void determineConcurrenciesWithAlphaPlusOracleAndFilters()
    {
        initializeDfDependenciesWithFiltersAndShortLoops();
        for(IntArrayList trace : uniqueTraces)
        {
            for(int pos=0;pos<trace.size()-1;pos++)
            {
                int ev = trace.get(pos);
                int ev2 = trace.get(pos+1);
                IntIntPair df = new IntIntPair(ev,ev2);
                dfDependenciesWFrequencies.addToValue(df,1);
                countOutgoing.addToValue(ev,1);
                countIncoming.addToValue(ev2,1);
                if(pos+2<trace.size())
                {
                    int ev3=trace.get(pos+2);
                    if(ev==ev3)
                    {
                        shortLoops.put(df,true);
                    }
                }
            }
        }
        this.concurrentPairs=new UnifiedSet<>();

        for(int uniqueEvent : this.labelMapping.keySet())
        {
            for(int uniqueEvent2 : this.labelMapping.keySet())
            {
                if(uniqueEvent==uniqueEvent2) continue;
                IntIntPair df1 = new IntIntPair(uniqueEvent,uniqueEvent2);
                IntIntPair df2 = new IntIntPair(uniqueEvent2,uniqueEvent);
                int df1threshold = (int) Math.round((countOutgoing.get(uniqueEvent) + countIncoming.get(uniqueEvent2)) / 2 * noiseThreshold);
                int df2threshold = (int) Math.round((countOutgoing.get(uniqueEvent2) + countIncoming.get(uniqueEvent)) / 2 * noiseThreshold);
                if(dfDependenciesWFrequencies.get(df1)>df1threshold && dfDependenciesWFrequencies.get(df2)>df2threshold)
                {
                    dfDependencies.put(df1,true);
                    dfDependencies.put(df2,true);
                    if(!shortLoops.get(df1)) concurrentPairs.add(IntHashSet.newSetWith(df1.getFirst(),df1.getSecond()));
                }
            }
        }
    }

    private void initializeDfDependenciesWithFiltersAndShortLoops()
    {
        dfDependenciesWFrequencies = new ObjectIntHashMap<>();
        shortLoops = new ObjectBooleanHashMap<>();
        countIncoming = new IntIntHashMap();
        countOutgoing = new IntIntHashMap();
        dfDependencies = new ObjectBooleanHashMap<>();
        intersections = new ObjectBooleanHashMap<>();
        for(int uniqueEvent : this.labelMapping.keySet())
        {
            countIncoming.put(uniqueEvent,0);
            countOutgoing.put(uniqueEvent,0);
            for(int uniqueEvent2 : this.labelMapping.keySet())
            {
                IntIntPair df = new IntIntPair(uniqueEvent,uniqueEvent2);
                dfDependencies.put(df,false);
                dfDependenciesWFrequencies.put(df,0);
                shortLoops.put(df,false);
                intersections.put(df,false);
            }
        }
    }

    private void initializeDfDependenciesAndIntersections()
    {
        dfDependencies = new ObjectBooleanHashMap<>();
        intersections = new ObjectBooleanHashMap<>();
        for(int uniqueEvent : this.labelMapping.keySet())
        {
            for(int uniqueEvent2 : this.labelMapping.keySet())
            {
                IntIntPair df = new IntIntPair(uniqueEvent,uniqueEvent2);
                dfDependencies.put(df,false);
                intersections.put(df,false);
            }
        }
    }

    private void initializeDfDependenciesAndShortLoops()
    {
        dfDependencies = new ObjectBooleanHashMap<>();
        shortLoops = new ObjectBooleanHashMap<>();
        for(int uniqueEvent : this.labelMapping.keySet())
        {
            for(int uniqueEvent2 : this.labelMapping.keySet())
            {
                IntIntPair df = new IntIntPair(uniqueEvent,uniqueEvent2);
                dfDependencies.put(df,false);
                shortLoops.put(df,false);
            }
        }
    }
}
