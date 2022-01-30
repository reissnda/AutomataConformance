package org.processmining.plugins.neconformance.ui.simple;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.processmining.plugins.neconformance.plugins.simple.PetrinetEvaluatorResult;

public class EvaluationVisualizator extends JPanel {

	private static final long serialVersionUID = -4858007119206L;
	
	private AlignmentPane alignPane;
	private TraceSelectorPane tracePane;
	private PetrinetPane petriPane;
	
	private PetrinetEvaluatorResult result;

	public EvaluationVisualizator(PetrinetEvaluatorResult result) {
		this.result = result;
		
		this.alignPane = new AlignmentPane(this);
		this.tracePane = new TraceSelectorPane(this);
		this.petriPane = new PetrinetPane(this);
		
		this.setLayout(new BorderLayout());
		this.add(alignPane, BorderLayout.SOUTH);
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tracePane, petriPane);
		this.add(split, BorderLayout.CENTER);
	}
	
	public PetrinetEvaluatorResult getResult() {
		return this.result;
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
