package org.processmining.plugins.neconformance.ui.simple;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.jgraph.ProMGraphModel;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.visualizators.GraphViewPanel;
import org.processmining.plugins.neconformance.models.ProcessReplayModel;
import org.processmining.plugins.neconformance.models.ProcessReplayModel.ReplayMove;
import org.processmining.plugins.neconformance.models.impl.PetrinetReplayModel;
import org.processmining.plugins.neconformance.types.WeightedEventClass;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;

public class PetrinetPane extends JPanel {
	private static final long serialVersionUID = 10027791212343506L;
	private ProMJGraph graph;
	private EvaluationVisualizator evaluationVisualizator;
	private GraphViewPanel graphViewPanel;
	
	private Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> drawnEdges;
	
	public PetrinetPane(EvaluationVisualizator evaluationVisualizator) {
		this.evaluationVisualizator = evaluationVisualizator;
		
		this.setLayout(new BorderLayout());
		
		this.initializeGraph();	
		
	}
	
	private void initializeGraph(){
		drawnEdges = new HashSet<>();
		
		GraphLayoutConnection layoutConnection = new GraphLayoutConnection(evaluationVisualizator.getResult().net);
		ProMGraphModel model = new ProMGraphModel(evaluationVisualizator.getResult().net);
		ProMJGraph jGraph = new ProMJGraph(model, new ViewSpecificAttributeMap(), layoutConnection);
		
		for (DirectedGraphNode node : jGraph.getModel().getGraph().getNodes()) {
			node.getAttributeMap().put(AttributeMap.SHOWLABEL, true);
			if (node instanceof Place) {
				node.getAttributeMap().put(AttributeMap.SIZE, new Dimension(60,60));
			} else if (node instanceof Transition) {
				if (!((Transition) node).isInvisible())
					node.getAttributeMap().put(AttributeMap.SIZE, new Dimension(80,60));
			}
		}
		
		JGraphHierarchicalLayout layout = new JGraphHierarchicalLayout();
		layout.setDeterministic(false);
		layout.setCompactLayout(false);
		layout.setFineTuning(true);
		layout.setParallelEdgeSpacing(15);
		layout.setFixRoots(false);
		layout.setOrientation(SwingConstants.WEST);
	
		if(!layoutConnection.isLayedOut()){
			JGraphFacade facade = new JGraphFacade(jGraph);
			facade.setOrdered(false);
			facade.setEdgePromotion(true);
			facade.setIgnoresCellsInGroups(false);
			facade.setIgnoresHiddenCells(false);
			facade.setIgnoresUnconnectedCells(false);
			facade.setDirected(true);
			facade.resetControlPoints();
			facade.run(layout, true);
	
			java.util.Map<?, ?> nested = facade.createNestedMap(true, true);
	
			jGraph.getGraphLayoutCache().edit(nested);
			layoutConnection.setLayedOut(true);
		}
		
		jGraph.setUpdateLayout(layout);
				
		this.graph = jGraph;
		
		if (graphViewPanel != null)
			this.remove(graphViewPanel);
		
		graphViewPanel = new GraphViewPanel(graph);
		this.add(graphViewPanel, BorderLayout.CENTER);
		
		this.resetGraph();
	}
	
	private PetrinetGraph getGraphAsPetrinet() {
		return (PetrinetGraph) graph.getModel().getGraph();
	}
	
	private void resetGraph() {
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : drawnEdges)
			getGraphAsPetrinet().removeArc(e.getSource(), e.getParent());
		drawnEdges.clear();
		
		for (DirectedGraphEdge<? extends DirectedGraphNode, ? extends DirectedGraphNode> edge : getGraphAsPetrinet().getEdges()) {
			edge.getAttributeMap().put(AttributeMap.EDGECOLOR, Color.BLACK);
			edge.getAttributeMap().put(AttributeMap.LINEWIDTH, 1.0F);
		}
		for (DirectedGraphNode node : getGraphAsPetrinet().getNodes()) {
			node.getAttributeMap().put(AttributeMap.STROKECOLOR, Color.BLACK);
			node.getAttributeMap().put(AttributeMap.LINEWIDTH, 1.5F);
			node.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.WHITE);
			if (node instanceof Place){
				node.getAttributeMap().put(AttributeMap.SHOWLABEL, true);
				node.getAttributeMap().put(AttributeMap.LABEL, "");
			}
			if (node instanceof Transition && ((Transition) node).isInvisible()){
				node.getAttributeMap().put(AttributeMap.SHOWLABEL, false);
				node.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.BLACK);
			}
		}
		graph.refresh();		
	}

	public void annotateGraphReplay(
			Map<Transition, Integer> normalTransitions, 
			Map<Transition, Integer> forcedTransitions, 
			Map<Transition, Integer> unobservableTransitions, 
			Map<Transition, Integer> skippedTransitions, 
			Map<Place, Integer> markingPlaces, 
			Map<Place, Integer> logmovedPlaces, 
			Map<Transition, Integer> allowedNegativeEvents, 
			Map<Transition, Integer> allowedUnmappedNegativeEvents, 
			Map<Transition, Integer> disallowedGeneralizedEvents, 
			Map<Transition, Integer> disallowedNegativeEvents, 
			Map<Transition, Integer> allowedGeneralizedEvents
		) {
		
		for (DirectedGraphNode node : getGraphAsPetrinet().getNodes()) {
			if (node instanceof Place) {
				node.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.white);
				node.getAttributeMap().put(AttributeMap.LABEL, markingPlaces.containsKey(node) ? "<html>&bull;</html>" : "");
				
//				if (logmovedPlaces.keySet().contains(node)) {
//					node.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.yellow);
//					node.getAttributeMap().put(AttributeMap.TOOLTIP, 
//							"Log only moves from this state: "+logmovedPlaces.get(node));
//				}
			}
			
			if (node instanceof Transition) {
				node.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.white);
				node.getAttributeMap().put(AttributeMap.TOOLTIP, "");
				
				// Determine color of transition
				if (normalTransitions.keySet().contains(node)) {
					node.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.green);
					node.getAttributeMap().put(AttributeMap.TOOLTIP, 
							node.getAttributeMap().get(AttributeMap.TOOLTIP) + "\n" +
							"Normal firings: "+normalTransitions.get(node));
				}
				if (unobservableTransitions.keySet().contains(node)) {
					node.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.gray);
					node.getAttributeMap().put(AttributeMap.TOOLTIP, 
							node.getAttributeMap().get(AttributeMap.TOOLTIP) + "\n" +
							"Unobserveable firings: "+unobservableTransitions.get(node));
				}
				if (skippedTransitions.keySet().contains(node)) {
					node.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.pink);
					node.getAttributeMap().put(AttributeMap.TOOLTIP, 
							node.getAttributeMap().get(AttributeMap.TOOLTIP) + "\n" +
							"Skipped firings: "+skippedTransitions.get(node));
				}
				if (forcedTransitions.keySet().contains(node)) {
					node.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.red);
					node.getAttributeMap().put(AttributeMap.TOOLTIP, 
							node.getAttributeMap().get(AttributeMap.TOOLTIP) + "\n" +
							"Forced firings: "+forcedTransitions.get(node));
				}
				
				if (allowedNegativeEvents.keySet().contains(node)) {
					node.getAttributeMap().put(AttributeMap.TOOLTIP, 
							node.getAttributeMap().get(AttributeMap.TOOLTIP) + "\n" +
							"Precision violations: "+allowedNegativeEvents.get(node));
					for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> ie : getGraphAsPetrinet().getInEdges(node)) {
						Arc a = getGraphAsPetrinet().getArc((Place) ie.getSource(), (Transition) node);
						if (a==null) {
							a = getGraphAsPetrinet().addArc((Place) ie.getSource(), (Transition) node);
							drawnEdges.add(a);
						}
						a.getAttributeMap().put(AttributeMap.EDGECOLOR, Color.RED);
						a.getAttributeMap().put(AttributeMap.LINEWIDTH, 2.0F);
					}
				}
				if (allowedUnmappedNegativeEvents.keySet().contains(node)) {
					node.getAttributeMap().put(AttributeMap.TOOLTIP, 
							node.getAttributeMap().get(AttributeMap.TOOLTIP) + "\n" +
							"Precision violations (unmapped): "+allowedUnmappedNegativeEvents.get(node));
					for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> ie : getGraphAsPetrinet().getInEdges(node)) {
						Arc a = getGraphAsPetrinet().getArc((Place) ie.getSource(), (Transition) node);
						if (a==null) {
							a = getGraphAsPetrinet().addArc((Place) ie.getSource(), (Transition) node);
							drawnEdges.add(a);
						}
						a.getAttributeMap().put(AttributeMap.EDGECOLOR, Color.RED);
						a.getAttributeMap().put(AttributeMap.LINEWIDTH, 2.0F);
					}
				}
//				if (disallowedGeneralizedEvents.keySet().contains(node)) {
//					node.getAttributeMap().put(AttributeMap.TOOLTIP, 
//							node.getAttributeMap().get(AttributeMap.TOOLTIP) + "\n" +
//							"Generalization violations: "+disallowedGeneralizedEvents.get(node));
//				}
				if (disallowedNegativeEvents.keySet().contains(node)) {
					node.getAttributeMap().put(AttributeMap.TOOLTIP, 
							node.getAttributeMap().get(AttributeMap.TOOLTIP) + "\n" +
							"Precision conformances: "+disallowedNegativeEvents.get(node));
				}
//				if (allowedGeneralizedEvents.keySet().contains(node)) {
//					node.getAttributeMap().put(AttributeMap.TOOLTIP, 
//							node.getAttributeMap().get(AttributeMap.TOOLTIP) + "\n" +
//							"Generalization conformances: "+allowedGeneralizedEvents.get(node));
//				}
				
			}
		}
		fixGraphTooltips();
		graph.refresh();
	}
	
	public void notifyGlobalView() {
		resetGraph();
		
		Map<Transition, Integer> normalTransitions = new HashMap<Transition, Integer>();
		Map<Transition, Integer> forcedTransitions = new HashMap<Transition, Integer>();
		Map<Transition, Integer> unobservableTransitions = new HashMap<Transition, Integer>();
		Map<Transition, Integer> skippedTransitions = new HashMap<Transition, Integer>();
		Map<Place, Integer> markingPlaces = new HashMap<Place, Integer>();
		Map<Place, Integer> logmovedPlaces = new HashMap<Place, Integer>();
		Map<Transition, Integer> allowedNegativeEvents = new HashMap<Transition, Integer>();
		Map<Transition, Integer> allowedUnmappedNegativeEvents = new HashMap<Transition, Integer>();
		Map<Transition, Integer> disallowedGeneralizedEvents = new HashMap<Transition, Integer>();
		Map<Transition, Integer> disallowedNegativeEvents = new HashMap<Transition, Integer>();
		Map<Transition, Integer> allowedGeneralizedEvents = new HashMap<Transition, Integer>();
		
		for (int selectedTraceIndex = 0; selectedTraceIndex < evaluationVisualizator.getResult().log.size(); selectedTraceIndex++) {
			XTrace trace = evaluationVisualizator.getResult().log.get(selectedTraceIndex);
			ProcessReplayModel<Transition, XEventClass, Marking> replayModel = evaluationVisualizator.getResult().replayModels.get(selectedTraceIndex);
			for (int step = 0; step < replayModel.size(); step++) {
				incrementAnnotationInfo(trace, 
						replayModel, 
						step, 
						normalTransitions,
						forcedTransitions,
						unobservableTransitions,
						skippedTransitions,
						markingPlaces,
						logmovedPlaces, 
						allowedNegativeEvents, 
						allowedUnmappedNegativeEvents, 
						disallowedGeneralizedEvents, 
						disallowedNegativeEvents, 
						allowedGeneralizedEvents);
			}
		}
		
		annotateGraphReplay(normalTransitions,
				forcedTransitions,
				unobservableTransitions,
				skippedTransitions,
				new HashMap<Place, Integer>(),
				logmovedPlaces, 
				allowedNegativeEvents, 
				allowedUnmappedNegativeEvents, 
				disallowedGeneralizedEvents, 
				disallowedNegativeEvents, 
				allowedGeneralizedEvents);
	}

	public void notifyTraceSelection(int selectedTraceIndex) {
		resetGraph();
		
		Map<Transition, Integer> normalTransitions = new HashMap<Transition, Integer>();
		Map<Transition, Integer> forcedTransitions = new HashMap<Transition, Integer>();
		Map<Transition, Integer> unobservableTransitions = new HashMap<Transition, Integer>();
		Map<Transition, Integer> skippedTransitions = new HashMap<Transition, Integer>();
		Map<Place, Integer> logmovedPlaces = new HashMap<Place, Integer>();
		Map<Place, Integer> markingPlaces = new HashMap<Place, Integer>();
		Map<Transition, Integer> allowedNegativeEvents = new HashMap<Transition, Integer>();
		Map<Transition, Integer> allowedUnmappedNegativeEvents = new HashMap<Transition, Integer>();
		Map<Transition, Integer> disallowedGeneralizedEvents = new HashMap<Transition, Integer>();
		Map<Transition, Integer> disallowedNegativeEvents = new HashMap<Transition, Integer>();
		Map<Transition, Integer> allowedGeneralizedEvents = new HashMap<Transition, Integer>();
		
		XTrace trace = evaluationVisualizator.getResult().log.get(selectedTraceIndex);
		ProcessReplayModel<Transition, XEventClass, Marking> replayModel = evaluationVisualizator.getResult().replayModels.get(selectedTraceIndex);
		for (int step = 0; step < replayModel.size(); step++) {
			incrementAnnotationInfo(trace, 
					replayModel, 
					step, 
					normalTransitions,
					forcedTransitions,
					unobservableTransitions,
					skippedTransitions,
					new HashMap<Place, Integer>(),
					logmovedPlaces, 
					allowedNegativeEvents, 
					allowedUnmappedNegativeEvents, 
					disallowedGeneralizedEvents, 
					disallowedNegativeEvents, 
					allowedGeneralizedEvents);
		}
		
		annotateGraphReplay(normalTransitions,
				forcedTransitions,
				unobservableTransitions,
				skippedTransitions,
				markingPlaces,
				logmovedPlaces, 
				allowedNegativeEvents, 
				allowedUnmappedNegativeEvents, 
				disallowedGeneralizedEvents, 
				disallowedNegativeEvents, 
				allowedGeneralizedEvents);
		 
	}
	
	public void notifyReplayStepSelection(int selectedTraceIndex, int selectedStepIndex) {
		resetGraph();
		
		Map<Transition, Integer> normalTransitions = new HashMap<Transition, Integer>();
		Map<Transition, Integer> forcedTransitions = new HashMap<Transition, Integer>();
		Map<Transition, Integer> unobservableTransitions = new HashMap<Transition, Integer>();
		Map<Transition, Integer> skippedTransitions = new HashMap<Transition, Integer>();
		Map<Place, Integer> logmovedPlaces = new HashMap<Place, Integer>();
		Map<Place, Integer> markingPlaces = new HashMap<Place, Integer>();
		Map<Transition, Integer> allowedNegativeEvents = new HashMap<Transition, Integer>();
		Map<Transition, Integer> allowedUnmappedNegativeEvents = new HashMap<Transition, Integer>();
		Map<Transition, Integer> disallowedGeneralizedEvents = new HashMap<Transition, Integer>();
		Map<Transition, Integer> disallowedNegativeEvents = new HashMap<Transition, Integer>();
		Map<Transition, Integer> allowedGeneralizedEvents = new HashMap<Transition, Integer>();
		
		XTrace trace = evaluationVisualizator.getResult().log.get(selectedTraceIndex);
		ProcessReplayModel<Transition, XEventClass, Marking> replayModel = evaluationVisualizator.getResult().replayModels.get(selectedTraceIndex);
		incrementAnnotationInfo(trace, 
					replayModel, 
					selectedStepIndex, 
					normalTransitions,
					forcedTransitions,
					unobservableTransitions,
					skippedTransitions,
					markingPlaces,
					logmovedPlaces, 
					allowedNegativeEvents, 
					allowedUnmappedNegativeEvents, 
					disallowedGeneralizedEvents, 
					disallowedNegativeEvents, 
					allowedGeneralizedEvents);
		
		annotateGraphReplay(normalTransitions,
				forcedTransitions,
				unobservableTransitions,
				skippedTransitions,
				markingPlaces,
				logmovedPlaces, 
				allowedNegativeEvents, 
				allowedUnmappedNegativeEvents, 
				disallowedGeneralizedEvents, 
				disallowedNegativeEvents, 
				allowedGeneralizedEvents);
		
		/*
		notifyTraceSelection(selectedTraceIndex);

		XTrace trace = evaluationVisualizator.getResult().log.get(selectedTraceIndex);
		ProcessReplayModel<Transition, XEventClass, Marking> replayModel = evaluationVisualizator.getResult().replayModels.get(selectedTraceIndex);
		ReplayMove type = replayModel.getReplayMove(selectedStepIndex);
		Marking mbefore = replayModel.getModelState(selectedStepIndex-1);
		Marking mafter = replayModel.getModelState(selectedStepIndex);
		Transition fired = replayModel.getModelElement(selectedStepIndex);
		Map<XEventClass, WeightedEventClass> allowednegativeevents = new HashMap<XEventClass, WeightedEventClass>();
		Set<Transition> allowedunmappednegativeevents = new HashSet<Transition>();
		Map<XEventClass, WeightedEventClass> disallowedgeneralizedevents = new HashMap<XEventClass, WeightedEventClass>();
		Map<XEventClass, WeightedEventClass> disallowednegativeevents = new HashMap<XEventClass, WeightedEventClass>();
		Map<XEventClass, WeightedEventClass> allowedgeneralizedevents = new HashMap<XEventClass, WeightedEventClass>();
		Set<Transition> orphanedModelElements = replayModel.getOrphanedModelElements();
		
		if ( type.equals(ReplayMove.BOTH_SYNCHRONOUS) 
				|| type.equals(ReplayMove.BOTH_FORCED) 
				|| type.equals(ReplayMove.LOGONLY_INSERTED) ) {		
			int latestNonInvisibleStep;
			int currentPositionInTrace = getTracePositionForReplayStep(replayModel, selectedStepIndex);
			for (latestNonInvisibleStep = selectedStepIndex-1; latestNonInvisibleStep >= 0; latestNonInvisibleStep--) {
				if (!replayModel.getReplayMove(latestNonInvisibleStep).equals(ReplayMove.MODELONLY_UNOBSERVABLE))
					break;			
			}
			
			Set<XEventClass> executables = (latestNonInvisibleStep < 0) 
					? replayModel.getExecutableLogElements(
							(Marking) replayModel.getInitialState())
					: replayModel.getExecutableLogElements(
							(Marking) replayModel.getModelState(latestNonInvisibleStep));
			Set<Transition> modelExecutables = (latestNonInvisibleStep < 0) 
					? ((PetrinetReplayModel)replayModel).getExecutableModelElements(
							(Marking) replayModel.getInitialState())
					: ((PetrinetReplayModel)replayModel).getExecutableModelElements(
							(Marking) replayModel.getModelState(latestNonInvisibleStep));
			
			Set<WeightedEventClass> negativeevents = evaluationVisualizator.getResult().inducer.
					getNegativeEvents(trace, currentPositionInTrace);
			Set<WeightedEventClass> generalizedevents = evaluationVisualizator.getResult().inducer.
					getGeneralizedEvents(trace, currentPositionInTrace);
			
			for (Transition tr : orphanedModelElements) {
				if (modelExecutables.contains(tr))
					allowedunmappednegativeevents.add(tr);
			}
			
			for (WeightedEventClass ec : negativeevents) {
				if (executables.contains(ec.eventClass)) {
					allowednegativeevents.put(ec.eventClass, ec);
				}else{
					disallowednegativeevents.put(ec.eventClass, ec);
				}
			}
			
			for (WeightedEventClass ec : generalizedevents) {
				if (!executables.contains(ec.eventClass)){
					disallowedgeneralizedevents.put(ec.eventClass, ec);
				}else{
					allowedgeneralizedevents.put(ec.eventClass, ec);
				}
			}
		}
		
		for (DirectedGraphNode node : graph.getModel().getGraph().getNodes()) {
			if (node instanceof Place) {
				if (mbefore.contains(node)) {
					node.getAttributeMap().put(AttributeMap.STROKECOLOR, Color.blue);
					node.getAttributeMap().put(AttributeMap.STROKE, new BasicStroke(3));
				}
				if (mafter.contains(node)) {
					node.getAttributeMap().put(AttributeMap.STROKECOLOR, Color.green);
					node.getAttributeMap().put(AttributeMap.STROKE, new BasicStroke(3));
				}
			}
			
			if (node instanceof Transition) {
				if (node.equals(fired)) {
					node.getAttributeMap().put(AttributeMap.STROKECOLOR, 
							(type.equals(ReplayMove.BOTH_SYNCHRONOUS)) ? Color.green : Color.red);
					node.getAttributeMap().put(AttributeMap.STROKE, new BasicStroke(4));
					for (DirectedGraphEdge<? extends DirectedGraphNode, ? extends DirectedGraphNode> edge : graph.getModel().getGraph().getEdges()) {
						if (edge.getTarget().equals(node) || edge.getSource().equals(node))
							edge.getAttributeMap().put(AttributeMap.LINEWIDTH, 3.0F);
					}
					for (DirectedGraphNode node2 : graph.getModel().getGraph().getNodes()) {
						if (node2 instanceof Transition) {
							node2.getAttributeMap().put(AttributeMap.TOOLTIP, 
									node2.getAttributeMap().get(AttributeMap.TOOLTIP) + "\n" +
									"-------------------------");
							if (allowednegativeevents.keySet().contains(evaluationVisualizator.getResult().mapper.get(node2))) {
								node2.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.orange);
								node2.getAttributeMap().put(AttributeMap.TOOLTIP, 
										node2.getAttributeMap().get(AttributeMap.TOOLTIP) + "\n" +
										"Precision impact: "+allowednegativeevents.get(
												evaluationVisualizator.getResult().mapper.get(node2)).weight);
							}
							if (allowedunmappednegativeevents.contains(node2)) {
								node2.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.orange);
								node2.getAttributeMap().put(AttributeMap.TOOLTIP, 
										node2.getAttributeMap().get(AttributeMap.TOOLTIP) + "\n" +
										"Precision impact (unmapped model element): 1");
							}
							if (disallowedgeneralizedevents.keySet().contains(evaluationVisualizator.getResult().mapper.get(node2))) {
								node2.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.cyan);
								node2.getAttributeMap().put(AttributeMap.TOOLTIP, 
										node2.getAttributeMap().get(AttributeMap.TOOLTIP) + "\n" +
										"Generalization impact: "+disallowedgeneralizedevents.get(
												evaluationVisualizator.getResult().mapper.get(node2)).weight);
							}
							if (disallowednegativeevents.keySet().contains(evaluationVisualizator.getResult().mapper.get(node2))) {
								node2.getAttributeMap().put(AttributeMap.STROKECOLOR, Color.orange);
								node2.getAttributeMap().put(AttributeMap.STROKE, new BasicStroke(16));
								node2.getAttributeMap().put(AttributeMap.TOOLTIP, 
										node2.getAttributeMap().get(AttributeMap.TOOLTIP) + "\n" +
										"Precision benefit: "+disallowednegativeevents.get(
												evaluationVisualizator.getResult().mapper.get(node2)).weight);
							}
							if (allowedgeneralizedevents.keySet().contains(evaluationVisualizator.getResult().mapper.get(node2))) {
								node2.getAttributeMap().put(AttributeMap.STROKECOLOR, Color.cyan);
								node2.getAttributeMap().put(AttributeMap.STROKE, new BasicStroke(16));
								node2.getAttributeMap().put(AttributeMap.TOOLTIP, 
										node2.getAttributeMap().get(AttributeMap.TOOLTIP) + "\n" +
										"Generalization benefit: "+allowedgeneralizedevents.get(
												evaluationVisualizator.getResult().mapper.get(node2)).weight);
							}
						}
					}
				}
			}
		}
		
		
		fixGraphTooltips();
		graph.refresh();
		*/
		
	}
	
	private void incrementAnnotationInfo(
			XTrace trace,
			ProcessReplayModel<Transition, XEventClass, Marking> replayModel,
			int replayModelStep,
			Map<Transition, Integer> normalTransitions,
			Map<Transition, Integer> forcedTransitions,
			Map<Transition, Integer> unobservableTransitions,
			Map<Transition, Integer> skippedTransitions,
			Map<Place, Integer> markingPlaces,
			Map<Place, Integer> logmovedPlaces,
			Map<Transition, Integer> allowedNegativeEvents,
			Map<Transition, Integer> allowedUnmappedNegativeEvents,
			Map<Transition, Integer> disallowedGeneralizedEvents,
			Map<Transition, Integer> disallowedNegativeEvents,
			Map<Transition, Integer> allowedGeneralizedEvents) {
		
		ReplayMove type = replayModel.getReplayMove(replayModelStep);
		Set<Transition> orphanedModelElements = replayModel.getOrphanedModelElements();
		
		Marking marking = replayModel.getModelState(replayModelStep-1);
		for (Place p : marking) {
			markingPlaces.put(p, marking.occurrences(p));
		}
		
		switch (type) {
		case BOTH_SYNCHRONOUS:
			if (!normalTransitions.containsKey(replayModel.getModelElement(replayModelStep)))
				normalTransitions.put(replayModel.getModelElement(replayModelStep), 0);
			normalTransitions.put(replayModel.getModelElement(replayModelStep), 
					normalTransitions.get(replayModel.getModelElement(replayModelStep)) + 1);
			break;
		case BOTH_FORCED:
			if (!forcedTransitions.containsKey(replayModel.getModelElement(replayModelStep)))
				forcedTransitions.put(replayModel.getModelElement(replayModelStep), 0);
			forcedTransitions.put(replayModel.getModelElement(replayModelStep), 
					forcedTransitions.get(replayModel.getModelElement(replayModelStep)) + 1);
			break;
		case LOGONLY_INSERTED:
			Marking s = replayModel.getModelState(replayModelStep);
			for (Place p : s) {
				if (!logmovedPlaces.containsKey(p))
					logmovedPlaces.put(p, 0);
				logmovedPlaces.put(p, logmovedPlaces.get(p) + 1);
			}
			break;
		case MODELONLY_SKIPPED:
			if (!skippedTransitions.containsKey(replayModel.getModelElement(replayModelStep)))
				skippedTransitions.put(replayModel.getModelElement(replayModelStep), 0);
			skippedTransitions.put(replayModel.getModelElement(replayModelStep), 
					skippedTransitions.get(replayModel.getModelElement(replayModelStep)) + 1);
			break;
		case MODELONLY_UNOBSERVABLE:
			if (!unobservableTransitions.containsKey(replayModel.getModelElement(replayModelStep)))
				unobservableTransitions.put(replayModel.getModelElement(replayModelStep), 0);
			unobservableTransitions.put(replayModel.getModelElement(replayModelStep), 
					unobservableTransitions.get(replayModel.getModelElement(replayModelStep)) + 1);
			break;
		default:
			break;
		}
		
		if ( type.equals(ReplayMove.BOTH_SYNCHRONOUS) 
				|| type.equals(ReplayMove.BOTH_FORCED) 
				|| type.equals(ReplayMove.LOGONLY_INSERTED) ) {		
			int latestNonInvisibleStep;
			int currentPositionInTrace = getTracePositionForReplayStep(replayModel, replayModelStep);
			for (latestNonInvisibleStep = replayModelStep-1; latestNonInvisibleStep >= 0; latestNonInvisibleStep--) {
				if (!replayModel.getReplayMove(latestNonInvisibleStep).equals(ReplayMove.MODELONLY_UNOBSERVABLE))
					break;			
			}
			
			Set<XEventClass> executables = (latestNonInvisibleStep < 0) 
					? replayModel.getExecutableLogElements(
							(Marking) replayModel.getInitialState())
					: replayModel.getExecutableLogElements(
							(Marking) replayModel.getModelState(latestNonInvisibleStep));
			Set<Transition> modelExecutables = (latestNonInvisibleStep < 0) 
					? ((PetrinetReplayModel)replayModel).getExecutableModelElements(
							(Marking) replayModel.getInitialState())
					: ((PetrinetReplayModel)replayModel).getExecutableModelElements(
							(Marking) replayModel.getModelState(latestNonInvisibleStep));
			
			Set<WeightedEventClass> negativeevents = evaluationVisualizator.getResult().inducer.
					getNegativeEvents(trace, currentPositionInTrace);
			Set<WeightedEventClass> generalizedevents = evaluationVisualizator.getResult().inducer.
					getGeneralizedEvents(trace, currentPositionInTrace);
			
			for (Transition tr : orphanedModelElements) {
				if (modelExecutables.contains(tr)) {
					if (!allowedUnmappedNegativeEvents.containsKey(tr))
						allowedUnmappedNegativeEvents.put(tr, 0);
					allowedUnmappedNegativeEvents.put(tr, 
							allowedUnmappedNegativeEvents.get(tr) + 1);
				}
			}
			
			for (WeightedEventClass ec : negativeevents) {
				for (Transition tr :evaluationVisualizator.getResult().mapper.getTransitionsForActivity(ec.eventClass.getId())) {
					if (executables.contains(ec.eventClass)) {
						if (!allowedNegativeEvents.containsKey(tr))
							allowedNegativeEvents.put(tr, 0);
						allowedNegativeEvents.put(tr, 
								allowedNegativeEvents.get(tr) + 1);
					}else{
						if (!disallowedNegativeEvents.containsKey(tr))
							disallowedNegativeEvents.put(tr, 0);
						disallowedNegativeEvents.put(tr, 
								disallowedNegativeEvents.get(tr) + 1);
					}
				}
			}
			
			for (WeightedEventClass ec : generalizedevents) {
				for (Transition tr :evaluationVisualizator.getResult().mapper.getTransitionsForActivity(ec.eventClass.getId())) {
					if (executables.contains(ec.eventClass)) {
						if (!disallowedGeneralizedEvents.containsKey(tr))
							disallowedGeneralizedEvents.put(tr, 0);
						disallowedGeneralizedEvents.put(tr, 
								disallowedGeneralizedEvents.get(tr) + 1);
					}else{
						if (!allowedGeneralizedEvents.containsKey(tr))
							allowedGeneralizedEvents.put(tr, 0);
						allowedGeneralizedEvents.put(tr, 
								allowedGeneralizedEvents.get(tr) + 1);
					}
				}
			}
		}
		
	}

	private int getTracePositionForReplayStep(ProcessReplayModel<Transition, XEventClass, Marking> replayModel, int replayStep) {
		int positionInTrace = -1;
		for (int step = 0; step <= replayStep; step++) {
			ReplayMove type = replayModel.getReplayMove(step);
			if ( type.equals(ReplayMove.BOTH_SYNCHRONOUS) 
					|| type.equals(ReplayMove.BOTH_FORCED) 
					|| type.equals(ReplayMove.LOGONLY_INSERTED) ) {
				positionInTrace++;
			}
		}
		return positionInTrace;
	}
	
	private void fixGraphTooltips() {
		for (DirectedGraphNode node : graph.getModel().getGraph().getNodes()) {
			if (node.getAttributeMap().containsKey(AttributeMap.TOOLTIP)) {
				String tip = node.getAttributeMap().get(AttributeMap.TOOLTIP).toString();
				tip = tip.replaceAll("<html>", "");
				tip = tip.replaceAll("</html>", "");
				tip = tip.replaceAll("<br>", "");
				tip = tip.replaceAll("\\n", "<br>\n");
				tip = "<html>"+tip+"</html>";
				node.getAttributeMap().put(AttributeMap.TOOLTIP, tip);
			}
		}
		
	}

}
