package au.unimelb.patternBasedGeneralization;

import au.qut.apromore.importer.GeneralizationImporter;
import au.unimelb.partialorders.PartialOrder;
import au.unimelb.pattern.ConcurrentPattern;
import au.unimelb.pattern.ReducedTrace;
import au.unimelb.pattern.RepetitivePattern;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PatternBasedGeneralizationMeasure
{


    private GeneralizationImporter patterns = new GeneralizationImporter();
    public double cummulativePatternFulfillmentsTimesTraceCounts=0;
    public int totalTraceCount=0;
    public int traceCountConcurrentPattern=0;
    public int traceCountRepetitivePattern=0;
    public int traceCountTransitiveConcurrencyPattern=0;
    public double fulfillmentConcurrentPattern=0;
    public double fulfillmentsRepetitivePattern=0;
    public double fulfillmentTransitiveConcurrentPattern=0;
    public double concurrentPatternGeneralization;
    public double transitiveConcurrencyGeneralization;
    public double repetitivePatternGeneralization;
    public double generalization;
    public String result;
    private long executionTime;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    private boolean usePartialMatching=true;



    public PatternBasedGeneralizationMeasure(String logFile, String modelFile,boolean useGlobal) throws Exception
    {
        patterns.derivePatternsForEventLog(logFile,modelFile,useGlobal);
        determineGeneralization();
    }

    public PatternBasedGeneralizationMeasure(String logFile, String modelFile, double noiseThreshold) throws Exception
    {
        patterns.derivePatternsForEventLog(logFile,modelFile,noiseThreshold);
        determineGeneralization();
    }

    public PatternBasedGeneralizationMeasure(String logFile, String modelFile, float occurence, float balance) throws Exception
    {
        patterns.derivePatternsForEventLog(logFile,modelFile,occurence,balance);
        determineGeneralization();
    }

    public PatternBasedGeneralizationMeasure(String logFile, String modelFile, boolean useGlobal, boolean useTransitivePattern) throws Exception
    {
        patterns.derivePatternsForEventLog(logFile,modelFile,useGlobal,useTransitivePattern);
        determineGeneralization();
    }

    public PatternBasedGeneralizationMeasure(GeneralizationImporter patterns, double noiseThreshold) throws Exception
    {
        long start = System.nanoTime();
        this.patterns=patterns;
        this.patterns.derivePatternsForEventLog(patterns.getLogFile(),patterns.getModelFile(),noiseThreshold);
        determineGeneralization();
        this.executionTime = timeUnit.convert(System.nanoTime()-start,TimeUnit.NANOSECONDS);
        this.result = patterns.getLogFile() + "," + patterns.getModelFile() + ",PatternBasedGeneralization," + patterns.useGlobal() + "," + usePartialMatching + "," + executionTime + "," + this.result;
    }

    public PatternBasedGeneralizationMeasure(GeneralizationImporter patterns, double noiseThreshold, boolean usePartialMatching) throws Exception
    {
        long start = System.nanoTime();
        this.patterns=patterns;
        this.usePartialMatching=usePartialMatching;
        this.patterns.derivePatternsForEventLog(patterns.getLogFile(),patterns.getModelFile(),noiseThreshold);
        determineGeneralization();
        this.executionTime = timeUnit.convert(System.nanoTime()-start,TimeUnit.NANOSECONDS);
        this.result = patterns.getLogFile() + "," + patterns.getModelFile() + ",PatternBasedGeneralization," + patterns.useGlobal() + "," + usePartialMatching +"," + executionTime + "," + this.result;
    }

    public PatternBasedGeneralizationMeasure(GeneralizationImporter patterns, float occurence, float balance) throws Exception
    {
        long start = System.nanoTime();
        this.patterns=patterns;
        this.patterns.derivePatternsForEventLog(patterns.getLogFile(),patterns.getModelFile(),occurence,balance);
        determineGeneralization();
        this.executionTime = timeUnit.convert(System.nanoTime()-start,TimeUnit.NANOSECONDS);
        this.result = patterns.getLogFile() + "," + patterns.getModelFile() + ",PatternBasedGeneralization," + patterns.useGlobal() + "," + usePartialMatching + "," + executionTime + "," + this.result;
    }

    public PatternBasedGeneralizationMeasure(GeneralizationImporter patterns, float occurence, float balance, boolean usePartialMatching) throws Exception
    {
        long start = System.nanoTime();
        this.patterns=patterns;
        this.usePartialMatching=usePartialMatching;
        this.patterns.derivePatternsForEventLog(patterns.getLogFile(),patterns.getModelFile(),occurence,balance);
        determineGeneralization();
        this.executionTime = timeUnit.convert(System.nanoTime()-start,TimeUnit.NANOSECONDS);
        this.result = patterns.getLogFile() + "," + patterns.getModelFile() + ",PatternBasedGeneralization," + patterns.useGlobal() + "," + usePartialMatching + "," + executionTime + "," + this.result;
    }

    public PatternBasedGeneralizationMeasure(GeneralizationImporter patterns,boolean useGlobal) throws Exception
    {
        this.patterns=patterns;
        this.patterns.derivePatternsForEventLog(patterns.getLogFile(),patterns.getModelFile(),useGlobal);
        determineGeneralization();
    }

    private void determineGeneralization() throws Exception {
        //Concurrent Patterns from Oracle
        PartialOrder maxPO=null;
        int maxTraces=0;
        for(IntIntHashMap finalConfiguration : patterns.getConfigPartialOrderMapping().keySet())
        {
            for(PartialOrder partialOrder : patterns.getConfigPartialOrderMapping().get(finalConfiguration))
            {
                //System.out.println(partialOrder.representativeTraces());
                ConcurrentPatternChecker concurrentPatternChecker = new ConcurrentPatternChecker(partialOrder,patterns.getDecompositions(),usePartialMatching);
                for(ConcurrentPattern concurrentPattern : partialOrder.getConcurrentPatterns())
                {
                    //System.out.println(concurrentPattern.getConcurrentLabels());
                    fulfillmentConcurrentPattern+=concurrentPattern.getFulfillment()*partialOrder.getTraceCount();
                    traceCountConcurrentPattern+=partialOrder.getTraceCount();
                    cummulativePatternFulfillmentsTimesTraceCounts+=concurrentPattern.getFulfillment()*partialOrder.getTraceCount();
                    totalTraceCount+=partialOrder.getTraceCount();
                    if(concurrentPattern.getFulfillment()!=1)
                        if(partialOrder.getTraceCount()>maxTraces)
                        {
                            maxTraces=partialOrder.getTraceCount();
                            maxPO=partialOrder;
                        }
                }
            }
        }
        System.out.println(maxPO);

        concurrentPatternGeneralization = fulfillmentConcurrentPattern / traceCountConcurrentPattern;

        //Pattern for transitive Concurrency
        if(patterns.includesTransitiveConcurrencyPatterns()) {
            for (IntIntHashMap finalConfiguration : patterns.getConfigPartialOrderMapping().keySet()) {
                if (patterns.getConfigTransitivePartialOrderMapping().get(finalConfiguration).isEmpty()) continue;
                for (PartialOrder partialOrder : patterns.getConfigTransitivePartialOrderMapping().get(finalConfiguration)) {
                    ConcurrentPatternChecker concurrentPatternChecker = new ConcurrentPatternChecker(partialOrder, patterns.getReachabilityGraph());
                    for (ConcurrentPattern concurrentPattern : partialOrder.getConcurrentPatterns()) {
                        //System.out.println(concurrentPattern.getConcurrentLabels());
                        fulfillmentTransitiveConcurrentPattern+=concurrentPattern.getFulfillment()*partialOrder.getTraceCount();
                        traceCountTransitiveConcurrencyPattern+=partialOrder.getTraceCount();
                        cummulativePatternFulfillmentsTimesTraceCounts += concurrentPattern.getFulfillment() * partialOrder.getTraceCount();
                        totalTraceCount += partialOrder.getTraceCount();
                    }
                }
            }
        }

        transitiveConcurrencyGeneralization = fulfillmentTransitiveConcurrentPattern/traceCountTransitiveConcurrencyPattern;


        //Repetitive Pattern based on tandem repeats
        for(ReducedTrace redTrace : patterns.getReducedTraces().values())
        {
            if(redTrace.getRepetitivePatterns().isEmpty()) continue;
            RepetitivePatternChecker repetitivePatternChecker = new RepetitivePatternChecker(redTrace,patterns.getDecompositions());
            for(RepetitivePattern repPattern : redTrace.getRepetitivePatterns())
            {
                fulfillmentsRepetitivePattern+=repPattern.getFulfillment()*redTrace.getTraceCount();
                traceCountRepetitivePattern+=redTrace.getTraceCount();
                cummulativePatternFulfillmentsTimesTraceCounts+=repPattern.getFulfillment()*redTrace.getTraceCount();
                totalTraceCount+=redTrace.getTraceCount();
            }
        }

        repetitivePatternGeneralization= fulfillmentsRepetitivePattern/traceCountRepetitivePattern;
        this.generalization = cummulativePatternFulfillmentsTimesTraceCounts / totalTraceCount;
        result=patterns.getTraceCount() + "," + traceCountConcurrentPattern + "," + concurrentPatternGeneralization + "," + traceCountRepetitivePattern + "," + repetitivePatternGeneralization + "," + totalTraceCount + "," + generalization+"\n";
    }

    public static void main(String[] args) throws Exception
    {
        //String path="/Users/dreissner/Documents/Evaluations/testing/artificial/";
        //String log="artificial.xes";
        //String model="fig2.pnml";
        String path = "/Users/dreissner/Documents/Evaluations/TandemRepeatsPaper/public/SM/";
        String log = "11.xes.gz";
        String model="11.pnml";
        PatternBasedGeneralizationMeasure generalizationMeasure = new PatternBasedGeneralizationMeasure(path+log,path+model,false);
        System.out.println("Repetitive pattern generalization: " + generalizationMeasure.traceCountRepetitivePattern + " : " + generalizationMeasure.repetitivePatternGeneralization);
        System.out.println("Concurrent pattern generalization: " + generalizationMeasure.traceCountConcurrentPattern + " : " + generalizationMeasure.concurrentPatternGeneralization);
        System.out.println("Overall generalization: " + generalizationMeasure.totalTraceCount + " : " + generalizationMeasure.generalization);
    }

}
