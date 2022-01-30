package org.processmining.plugins.neconformance.plugins;


import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.kutoolbox.utils.LogUtils;

@Plugin(name = "Fix Event Concept Names for Ukkonen Tree", 
	parameterLabels = {"Log"},
	returnLabels = {"Fixed Log"},
	returnTypes = {
		XLog.class
	},
	userAccessible = true,
	help = "Fix Event Concept Names for Ukkonen Tree")

public class FixConceptNamesPlugin {
	
	@UITopiaVariant(uiLabel = "Fix Event Concept Names for Ukkonen Tree",
			affiliation = UITopiaVariant.EHV,
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@econ.kuleuven.be",
			website = "http://econ.kuleuven.be")
	@PluginVariant(variantLabel = "Wizard settings", requiredParameterLabels = { 0 })
	
	public static XLog fixLog(UIPluginContext context, XLog log) {
		XEventClasses classes = XEventClasses.deriveEventClasses(XLogInfoImpl.NAME_CLASSIFIER, log);
		
		XLog newLog = LogUtils.newLog("Fixed Log");
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		
		for (XTrace trace : log) {
			XTrace newTrace = factory.createTrace(trace.getAttributes());
			String traceAsString = "";
			for (int i = 0; i < trace.size(); i++) {
				XEvent clonedEvent = (XEvent) trace.get(i).clone();
				XEventClass oldName = classes.getClassOf(clonedEvent);
				String newName = oldName.getIndex()+"";
				XConceptExtension.instance().assignName(clonedEvent, newName);
				newTrace.add(clonedEvent);
				context.log(oldName.getId()+" --> "+newName);
				traceAsString += "\""+newName+"\", ";
			}
			System.out.println(traceAsString);
			newLog.add(newTrace);
		}
		
		return newLog;
	}
	
	

}
