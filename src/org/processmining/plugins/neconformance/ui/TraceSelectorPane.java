package org.processmining.plugins.neconformance.ui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.neconformance.models.ProcessReplayModel;
import org.processmining.plugins.neconformance.models.ProcessReplayModel.ReplayMove;

public class TraceSelectorPane extends JPanel {

	private static final long serialVersionUID = -197086735832537712L;

	public TraceSelectorPane(final EvaluationVisualizator evaluationVisualizator) {
		this.setLayout(new BorderLayout());
		
		final JList<String> traceList = new JList<String>();
		
		List<String> traces = new ArrayList<String>();
		traces.add("-- Global overview --");
		for (int t = 0; t < evaluationVisualizator.getLog().size(); t++) {
			String name = XConceptExtension.instance().extractName(
					evaluationVisualizator.getLog().get(t));
			ProcessReplayModel<Transition, XEventClass, Marking> replayModel = evaluationVisualizator.getReplayModels().get(t);
			boolean sync = false, forc = false, inse = false, skip = false, unob = false;
			for (int step = 0; step < replayModel.size(); step++) {
				ReplayMove type = replayModel.getReplayMove(step);
				switch (type) {
				case BOTH_SYNCHRONOUS:
					sync = true; break;
				case BOTH_FORCED:
					forc = true; break;
				case LOGONLY_INSERTED:
					inse = true; break;
				case MODELONLY_SKIPPED:
					skip = true; break;
				case MODELONLY_UNOBSERVABLE:
					unob = true; break;
				default:
					break;
				}
			}
			if (sync) name += " s";
			if (forc) name += " !";
			if (inse) name += " ^";
			if (skip) name += " *";
			if (unob) name += " .";
			traces.add(name);
		}
		
		traceList.setListData(traces.toArray(new String[]{}));
		
		traceList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				evaluationVisualizator.notifyTraceSelection(traceList.getSelectedIndex());
			}
			
		});
		
		JScrollPane scrollPane = new JScrollPane(traceList);
		this.add(scrollPane);
	}


}
