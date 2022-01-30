package org.processmining.plugins.neconformance.ui.simple;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.neconformance.models.ProcessReplayModel;
import org.processmining.plugins.neconformance.models.ProcessReplayModel.ReplayMove;

public class AlignmentPane extends JPanel {

	private static final long serialVersionUID = 4552315260112714479L;
	private EvaluationVisualizator evaluationVisualizator;
	private ProcessReplayModel<Transition, XEventClass, Marking> selectedReplayModel;
	private int selectedTraceIndex = -1;
	
	private final JList<ReplayMove> stepList;
	
	public AlignmentPane(final EvaluationVisualizator evaluationVisualizator) {
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(300, 90));
		
		this.evaluationVisualizator = evaluationVisualizator;
		this.stepList = new JList<ReplayMove>();
		this.stepList.setLayoutOrientation(JList.HORIZONTAL_WRAP);  
		this.stepList.setVisibleRowCount(1);
		JScrollPane scroll = new JScrollPane(this.stepList,  
		                                JScrollPane.VERTICAL_SCROLLBAR_NEVER,  
		                                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.add(scroll);
		
		stepList.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = -2745441247826070962L;

			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {  
	            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);  
	            ReplayMove move = (ReplayMove) value;
	            String text = "";
	            String traceEl = selectedReplayModel.getTraceElement(index) == null ? "***" 
	            		: selectedReplayModel.getTraceElement(index).toString();
	            String modelEl = selectedReplayModel.getModelElement(index) == null ? "***" 
	            		: selectedReplayModel.getModelElement(index).toString();
	            text = "<html>" + modelEl + "<br>" + traceEl+ "<br>" +
						move.toString() + "</html>";
				switch (move) {
				case BOTH_SYNCHRONOUS:
					c.setBackground(Color.green);
					break;
				case BOTH_FORCED:
					c.setBackground(Color.red);
					break;
				case LOGONLY_INSERTED:
					c.setBackground(Color.yellow);
					break;
				case MODELONLY_SKIPPED:
					c.setBackground(Color.pink);
					break;
				case MODELONLY_UNOBSERVABLE:
					c.setBackground(Color.gray);
					break;
				default:
					break;
				}
	            setText(text);
	            return c;  
	        }  
		});
		stepList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				evaluationVisualizator.notifyReplayStepSelection(selectedTraceIndex, stepList.getSelectedIndex());
			}
			
		});
	}
	
	public void notifyTraceSelection(int selectedTraceIndex) {
		this.selectedTraceIndex = selectedTraceIndex;
		this.selectedReplayModel = evaluationVisualizator.getResult().replayModels.get(selectedTraceIndex);
		
		List<ReplayMove> steps = new ArrayList<ReplayMove>();
		
		for (int step = 0; step < selectedReplayModel.size(); step++) {
			ReplayMove type = selectedReplayModel.getReplayMove(step);
			steps.add(type);
		}
		stepList.setListData(steps.toArray(new ReplayMove[]{}));
	}

}
