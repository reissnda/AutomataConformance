package org.processmining.plugins.neconformance.plugins;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.neconformance.ui.NegativeEventInspector;

@Plugin(name = "Inspect Negative Event Weight Distributions", 
		parameterLabels = {"Log"},
		returnLabels = {"Visualization"},
		returnTypes = {
			JComponent.class
		},
		userAccessible = true,
		help = "Inspect Negative Event Weight Distributions")

public class NegativeEventInspectorPlugin {
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV,
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@econ.kuleuven.be",
			website = "http://econ.kuleuven.be")
	
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	
	public static JComponent ExecutePlugin(UIPluginContext context, XLog log) {
		return new NegativeEventInspector(log, context);
	}
}
