package org.processmining.plugins.neconformance.ui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.neconformance.metrics.impl.BehavioralRecallMetric;
import org.processmining.plugins.neconformance.metrics.impl.BehavioralWeightedGeneralizationMetric;
import org.processmining.plugins.neconformance.metrics.impl.BehavioralWeightedPrecisionMetric;
import org.processmining.plugins.neconformance.models.ProcessReplayModel;import org.processmining.plugins.neconformance.models.impl.PetrinetReplayModel;
import org.processmining.plugins.neconformance.negativeevents.AbstractNegativeEventInducer;
import org.processmining.plugins.neconformance.negativeevents.impl.LogTreeWeightedNegativeEventInducer;
import au.unimelb.negativeEventsClasses.PetrinetLogMapper;
import au.unimelb.negativeEventsClasses.PetrinetUtils;

public class EvaluationVisualizator extends JPanel {

	private static final long serialVersionUID = -4828826958007119206L;
	private List<ProcessReplayModel<Transition, XEventClass, Marking>> replayModels;
	private AbstractNegativeEventInducer inducer;
	private XLog log;
	private PetrinetLogMapper mapper;
	
	private InformationPane infoPane;
	private AlignmentPane alignPane;
	private TraceSelectorPane tracePane;
	private PetrinetPane petriPane;
	private Petrinet net;
	private Marking marking;

	public EvaluationVisualizator(
			List<ProcessReplayModel<Transition, XEventClass, Marking>> replayModels,
			LogTreeWeightedNegativeEventInducer inducer, XLog log,
			Petrinet net, Marking marking, PetrinetLogMapper mapper) {
		this(replayModels, inducer, log, net, marking, mapper, false);
	}

	public EvaluationVisualizator(List<ProcessReplayModel<Transition, XEventClass, Marking>> replayModels, 
			AbstractNegativeEventInducer inducer, 
			XLog log, Petrinet net, Marking marking, PetrinetLogMapper mapper,
			boolean surpressMetricCalculation) {
		this.replayModels = replayModels;
		this.inducer = inducer;
		this.log = log;
		this.net = net;
		this.marking = marking;
		this.mapper = mapper;
		
		this.infoPane = new InformationPane(this);
		this.alignPane = new AlignmentPane(this);
		this.tracePane = new TraceSelectorPane(this);
		this.petriPane = new PetrinetPane(this);
		
		this.setLayout(new BorderLayout());
		
		this.add(alignPane, BorderLayout.SOUTH);
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				tracePane,
				petriPane);
		JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				split,
				infoPane);
		this.add(splitMain, BorderLayout.CENTER);
		
		// Add text
		if (surpressMetricCalculation == false) {
			PetrinetReplayModel replayModel = new PetrinetReplayModel(net, PetrinetUtils.getInitialMarking(this.net, this.marking), mapper);
			BehavioralRecallMetric br = new BehavioralRecallMetric(replayModel, inducer, log, true, true);
			BehavioralWeightedPrecisionMetric bp = new BehavioralWeightedPrecisionMetric(replayModel, inducer, log, true, true);
			BehavioralWeightedGeneralizationMetric bg = new BehavioralWeightedGeneralizationMetric(replayModel, inducer, log, true, true);
			
			String text = "";
			text += "Recall: "+br.getValue()+"\n";
			text += "Precision: "+bp.getValue()+"\n";
			text += "Generalization: "+bg.getValue()+"\n";
			infoPane.setText(text);
		} else {
			infoPane.setText("Metric calculation is surpressed");
		}
		
	}
	
	public List<ProcessReplayModel<Transition, XEventClass, Marking>> getReplayModels() {
		return replayModels;
	}

	public AbstractNegativeEventInducer getInducer() {
		return inducer;
	}

	public XLog getLog() {
		return log;
	}

	public Petrinet getNet() {
		return net;
	}

	public PetrinetLogMapper getMapper() {
		return mapper;
	}

	public void notifyTraceSelection(int selectedIndex) {
		if (selectedIndex <= 0) {
			petriPane.notifyGlobalView();
		} else {
			petriPane.notifyTraceSelection(selectedIndex-1);
			alignPane.notifyTraceSelection(selectedIndex-1);
		}
	}

	public void notifyReplayStepSelection(int selectedTraceIndex, int selectedReplayIndex) {
		if (selectedReplayIndex < 0)
			return;
		petriPane.notifyReplayStepSelection(selectedTraceIndex, selectedReplayIndex);
	}	

}
