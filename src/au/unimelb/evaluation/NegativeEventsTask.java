package au.unimelb.evaluation;


import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.neconformance.bags.LogBag;
import org.processmining.plugins.neconformance.bags.naieve.NaieveLogBag;
import org.processmining.plugins.neconformance.metrics.ConformanceMetric;
import org.processmining.plugins.neconformance.metrics.impl.BehavioralRecallMetric;
import org.processmining.plugins.neconformance.metrics.impl.BehavioralWeightedGeneralizationMetric;
import org.processmining.plugins.neconformance.metrics.impl.BehavioralWeightedPrecisionMetric;
import org.processmining.plugins.neconformance.models.impl.AryaPetrinetReplayModel;
import org.processmining.plugins.neconformance.models.impl.PetrinetReplayModel;
import org.processmining.plugins.neconformance.models.impl.RecoveringPetrinetReplayModel;
import org.processmining.plugins.neconformance.negativeevents.AbstractNegativeEventInducer;
import org.processmining.plugins.neconformance.negativeevents.impl.LogBagWeightedNegativeEventInducer;
import org.processmining.plugins.neconformance.negativeevents.impl.LogTreeWeightedNegativeEventInducer;
import org.processmining.plugins.neconformance.trees.LogTree;
import org.processmining.plugins.neconformance.trees.ukkonen.UkkonenLogTree;
import au.unimelb.negativeEventsClasses.PetrinetLogMapper;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class NegativeEventsTask implements Callable<String>
{
    private Marking initMarking;
    private Petrinet petrinet;
    private XLog xLog;
    private String model;
    private String log;

    public static double getMetricValue(XLog log, Petrinet net, Marking marking,
                                        PetrinetLogMapper mapper,
                                        int replayer, int inducer, boolean useWeighted,
                                        boolean useBothRatios, boolean useCutOff, int negWindow, int genWindow,
                                        boolean checkUnmappedRecall, boolean checkUnmappedPrecision, boolean checkUnmappedGeneralization,
                                        boolean useMultiTreadedCalculation,
                                        String metric) {

        mapper.applyMappingOnTransitions();

        PetrinetReplayModel rm = null;
        AbstractNegativeEventInducer in = null;
        ConformanceMetric cm = null;

        if (replayer == 0) {
            rm = new PetrinetReplayModel(net,
                    PetrinetUtils.getInitialMarking(net, marking),
                    mapper);
        } else if (replayer == 1) {
            rm = new AryaPetrinetReplayModel(net,
                    XEventClasses.deriveEventClasses(mapper.getEventClassifier(), log),
                    PetrinetUtils.getInitialMarking(net, marking),
                    mapper);
        } else if (replayer == 2) {
            rm = new RecoveringPetrinetReplayModel(net,
                    PetrinetUtils.getInitialMarking(net, marking),
                    mapper);
        }


        XEventClasses eventClasses = XEventClasses.deriveEventClasses(mapper.getEventClassifier(), log);

        if (inducer == 0) {
            LogTree logTree = new UkkonenLogTree(log);
            in = new LogTreeWeightedNegativeEventInducer(
                    eventClasses,
                    AbstractNegativeEventInducer.deriveStartingClasses(eventClasses, log),
                    logTree);
            ((LogTreeWeightedNegativeEventInducer)in).setReturnZeroEvents(false);
            ((LogTreeWeightedNegativeEventInducer)in).setUseWeighted(useWeighted);
            ((LogTreeWeightedNegativeEventInducer)in).setUseBothWindowRatios(useBothRatios);
            ((LogTreeWeightedNegativeEventInducer)in).setUseWindowOccurrenceCut(useCutOff);
            ((LogTreeWeightedNegativeEventInducer)in).setNegWindowSize(negWindow);
            ((LogTreeWeightedNegativeEventInducer)in).setGenWindowSize(genWindow);
        } else if (inducer == 1) {
            LogBag logBag = new NaieveLogBag(log);
            in = new LogBagWeightedNegativeEventInducer(
                    eventClasses,
                    AbstractNegativeEventInducer.deriveStartingClasses(eventClasses, log),
                    logBag);
            ((LogBagWeightedNegativeEventInducer)in).setReturnZeroEvents(false);
            ((LogBagWeightedNegativeEventInducer)in).setUseWeighted(useWeighted);
            ((LogBagWeightedNegativeEventInducer)in).setUseBothWindowRatios(useBothRatios);
            ((LogBagWeightedNegativeEventInducer)in).setUseWindowOccurrenceCut(useCutOff);
            ((LogBagWeightedNegativeEventInducer)in).setNegWindowSize(negWindow);
            ((LogBagWeightedNegativeEventInducer)in).setGenWindowSize(genWindow);
        }
        //else if (inducer == 2) {
            //in = new PetrinetNegativeEventInducer(
            //        eventClasses,
            //        AbstractNegativeEventInducer.deriveStartingClasses(eventClasses, log),
            //        net, marking, mapper);
            //((PetrinetNegativeEventInducer)in).setReturnZeroEvents(false);
        //}

        if (metric.equals("recall"))
            cm = new BehavioralRecallMetric(rm, in, log, checkUnmappedRecall, useMultiTreadedCalculation);
        if (metric.equals("precision"))
            cm = new BehavioralWeightedPrecisionMetric(rm, in, log, checkUnmappedPrecision, useMultiTreadedCalculation);
        if (metric.equals("generalization"))
            cm = new BehavioralWeightedGeneralizationMetric(rm, in, log, checkUnmappedGeneralization, useMultiTreadedCalculation);
        double result = cm.getValue();
        return result;
    }

    public NegativeEventsTask(String log, String model,XLog xLog, Petrinet petrinet, Marking initMarking)
    {
        this.log=log;
        this.model = model;
        this.xLog=xLog;
        this.petrinet=petrinet;
        this.initMarking=initMarking;
    }

    @Override
    public String call() throws Exception {
        long executionTime = System.nanoTime();
        XEventClassifier classifier = new XEventNameClassifier();
        PetrinetLogMapper mapper = PetrinetLogMapper.getStandardMap(xLog,petrinet);// new PetrinetLogMapper(classifier, XEventClasses.deriveEventClasses(classifier, log).getClasses(), onet.getTransitions());
        //double neGeneralization = PetrinetEvaluatorPlugin.getMetricValue(log, onet, marking, mapper, 0, 1, true, true, true, 10, 10, true, false, true, false, "generalization");
        double neGeneralization = NegativeEventsTask.getMetricValue(xLog, petrinet, initMarking, mapper, 1, 1, true, true, true, 20, 20, true, false, true, false, "generalization");
        executionTime=System.nanoTime()-executionTime;
        String result=log+","+model+",NegativeEvents,"+ TimeUnit.MILLISECONDS.convert(executionTime,TimeUnit.NANOSECONDS) + "," +neGeneralization +",\n";
        return result;
    }
}
