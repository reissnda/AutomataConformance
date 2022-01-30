package org.processmining.plugins.neconformance.plugins;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.exceptions.OperationCancelledException;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapperPanel;
import org.processmining.plugins.kutoolbox.ui.ParametersWizard;
import org.processmining.plugins.kutoolbox.ui.UIAttributeConfigurator;
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
import org.processmining.plugins.neconformance.ui.wizard.WizardMetricSelectionPanel;
import org.processmining.plugins.neconformance.ui.wizard.WizardSettingsPanel;
import au.unimelb.negativeEventsClasses.PetrinetLogMapper;
import au.unimelb.negativeEventsClasses.PetrinetUtils;

@Plugin(name = "Get Behavioral (Weighted) Conformance Metric", 
	parameterLabels = {"Log", "Petri net", "Marking"},
	returnLabels = {"Metric value"},
	returnTypes = {
		Double.class
	},
	userAccessible = true,
	help = "Get a weighted behavioral conformance metric")

public class PetrinetEvaluatorPlugin {
	
	public static boolean SUPPRESS_OUTPUT = false;

	@UITopiaVariant(uiLabel = "Get Behavioral (Weighted) Conformance Metric",
			affiliation = UITopiaVariant.EHV,
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@econ.kuleuven.be",
			website = "http://econ.kuleuven.be")
	@PluginVariant(variantLabel = "Wizard settings", requiredParameterLabels = { 0, 1 })
	
	public static Double metricWizard(UIPluginContext context, XLog log, Petrinet onet, Marking marking) {
		// 1. Make the mapping
		PetrinetLogMapper mapper = null;
		PetrinetLogMapperPanel mapperPanel = new PetrinetLogMapperPanel(log, onet);
		InteractionResult ir = context.showWizard("Mapping", true, true, mapperPanel);
		if (!ir.equals(InteractionResult.FINISHED)) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		//mapper = mapperPanel.getMap();
		
		UIAttributeConfigurator[] panels = new UIAttributeConfigurator[] {
			new WizardSettingsPanel(),
			new WizardMetricSelectionPanel(),
		};
		ParametersWizard wizard = new ParametersWizard(context, panels);
		try {
			wizard.show();
		} catch (OperationCancelledException e) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		return getMetricValue(log, onet, marking, mapper,
				Integer.parseInt(wizard.getSettings(0).get("useAryaReplayer").toString()),
				Integer.parseInt(wizard.getSettings(0).get("useBagInducer").toString()),
				wizard.getSettings(0).get("useWeighted").toString().equals("1"),
				wizard.getSettings(0).get("useBothRatios").toString().equals("1"),
				wizard.getSettings(0).get("useCutOff").toString().equals("1"),
				Integer.parseInt(wizard.getSettings(0).get("negWindow").toString()),
				Integer.parseInt(wizard.getSettings(0).get("genWindow").toString()),
				wizard.getSettings(0).get("unmappedRecall").toString().equals("1"),
				wizard.getSettings(0).get("unmappedPrecision").toString().equals("1"),
				wizard.getSettings(0).get("unmappedGeneralization").toString().equals("1"),
				wizard.getSettings(0).get("useMultithreaded").toString().equals("1"),
				wizard.getSettings(1).get("metric").toString()
			); 
	}
	
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
		//	in = new PetrinetNegativeEventInducer(
		//			eventClasses,
		//			AbstractNegativeEventInducer.deriveStartingClasses(eventClasses, log),
		//			net, marking, mapper);
		//	((PetrinetNegativeEventInducer)in).setReturnZeroEvents(false);
		//}
		
		if (metric.equals("recall"))
			cm = new BehavioralRecallMetric(rm, in, log, checkUnmappedRecall, useMultiTreadedCalculation);
		if (metric.equals("precision"))
			cm = new BehavioralWeightedPrecisionMetric(rm, in, log, checkUnmappedPrecision, useMultiTreadedCalculation);
		if (metric.equals("generalization"))
			cm = new BehavioralWeightedGeneralizationMetric(rm, in, log, checkUnmappedGeneralization, useMultiTreadedCalculation);
		double result = getMetricValue(log, net, mapper, in, rm, cm);
		if (!SUPPRESS_OUTPUT)
			System.err.println(result);
		return result;
	}
	
	public static void appendToFile(String targetFile, String s) throws IOException {
	    appendToFile(new File(targetFile), s);
	}

	public static void appendToFile(File targetFile, String s) throws IOException {
	    PrintWriter out = null;
	    try {
	        out = new PrintWriter(new BufferedWriter(new FileWriter(targetFile, true)));
	        out.println(s);
	    } finally {
	        if (out != null) {
	            out.close();
	        }
	    }
	}
	
	public static double getMetricValue(XLog log, Petrinet net, 
			PetrinetLogMapper mapper, 
			AbstractNegativeEventInducer inducer, 
			PetrinetReplayModel replayModel,
			ConformanceMetric metric) {
		return metric.getValue();
	}
	
	
	
}
