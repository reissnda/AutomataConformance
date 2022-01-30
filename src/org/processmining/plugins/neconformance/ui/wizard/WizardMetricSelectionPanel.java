package org.processmining.plugins.neconformance.ui.wizard;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.processmining.plugins.kutoolbox.ui.TwoColumnParameterPanel;
import org.processmining.plugins.kutoolbox.ui.UIAttributeConfigurator;

public class WizardMetricSelectionPanel extends TwoColumnParameterPanel implements UIAttributeConfigurator {
	
	private static final long serialVersionUID = -5233833365930723360L;
	
	private String metric;

	private JRadioButton radioRecall;
	private JRadioButton radioPrecision;
	private JRadioButton radioGeneralization;

	public WizardMetricSelectionPanel() {
		super(5);
		this.init();
	}

	protected void init() {
		resetSettings();
		
		this.addDoubleLabel("Select metric", 1);
		radioRecall = this.addRadiobutton("Behavioral Recall", metric.equals("recall"), 2, false);
		radioPrecision = this.addRadiobutton("Behavioral Precision", metric.equals("precision"), 3, false);
		radioGeneralization = this.addRadiobutton("Behavioral Generalization", metric.equals("generalization"), 4, false);
		ButtonGroup group = new ButtonGroup();
		group.add(radioRecall);
		group.add(radioPrecision);
		group.add(radioGeneralization);
	}

	@Override
	public String getTitle() {
		return "Select a Metric";
	}

	@Override
	public void resetSettings() {
		metric = "recall";
	}

	@Override
	public void setSettings(Map<String, Object> settings) {
		metric = settings.get("metric").toString();
		if (metric.equals("recall"))
			radioRecall.setSelected(true);
		if (metric.equals("precision"))
			radioPrecision.setSelected(true);
		if (metric.equals("generalization"))
			radioGeneralization.setSelected(true);
	}

	@Override
	public Map<String, Object> getSettings() {
		if (radioRecall.isSelected())
			metric = "recall";
		if (radioPrecision.isSelected())
			metric = "precision";
		if (radioGeneralization.isSelected())
			metric = "generalization";
		
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("metric", metric);
		
		return settings;
	}

	@Override
	protected void updateFields() {
		
	}

}
