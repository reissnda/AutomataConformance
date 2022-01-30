package org.processmining.plugins.neconformance.ui.simple;

import javax.swing.JComponent;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.neconformance.plugins.simple.PetrinetEvaluatorResult;

@Plugin(name = "Visualize Petri Net Conformance Evaluation Result",
		parameterLabels = { "Petri net Evaluation Result" }, 
		returnLabels = { "Visualization" },
		returnTypes = { JComponent.class })

@Visualizer
public class PetrinetEvaluatorPluginVizualization {

	@PluginVariant(requiredParameterLabels = { 0 })
	public static JComponent visualize(PluginContext context, PetrinetEvaluatorResult result) {
		return new EvaluationVisualizator(result);
	}
}

