package org.processmining.plugins.neconformance.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class InformationPane extends JPanel {

	private static final long serialVersionUID = 7296759288208140407L;

	private JTextArea textarea;
	
	public InformationPane(EvaluationVisualizator evaluationVisualizator) {
		this.setLayout(new BorderLayout());
		textarea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(textarea);
		this.add(scrollPane, BorderLayout.CENTER);
	}

	public void setText(String text) {
		this.textarea.setText(text);
	}
}
