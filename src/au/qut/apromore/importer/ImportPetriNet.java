package au.qut.apromore.importer;


import au.qut.apromore.PetriNet.PetriNet;
import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramFactory;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Swimlane;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.bpmn.Bpmn;
import org.processmining.plugins.bpmn.dialogs.BpmnSelectDiagramDialog;
import org.processmining.plugins.bpmn.parameters.BpmnSelectDiagramParameters;
import org.processmining.plugins.bpmn.plugins.BpmnImportPlugin;
import org.processmining.plugins.pnml.importing.PnmlImportNet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ImportPetriNet
{
    public static Object[] importPetriNetAndMarking(String fileName) throws Exception
    {
        FakePluginContext context = new FakePluginContext();
        PnmlImportNet imp = new PnmlImportNet();
        Object[] obj =  (Object[]) imp.importFile(context, fileName);
        Petrinet pnet = (Petrinet) obj[0];
        int i = pnet.getNodes().size();
        for(org.processmining.models.graphbased.directed.petrinet.elements.Transition tr : pnet.getTransitions())
        {
            if(tr.getLabel().equals(""))
            {
                tr.getAttributeMap().put(AttributeMap.LABEL, "empty_" + ++i);
            }
        }
        obj[0] = pnet;
        return obj;
    }

    public static Object[] importPetrinetFromBPMN(String fileName) throws Exception
    {
        FakePluginContext context = new FakePluginContext();
        Bpmn bpmn = (Bpmn) new BpmnImportPlugin().importFile(context, fileName);
        //long start = System.nanoTime();
        BpmnSelectDiagramParameters parameters = new BpmnSelectDiagramParameters();
        @SuppressWarnings("unused")
        BpmnSelectDiagramDialog dialog = new BpmnSelectDiagramDialog(bpmn.getDiagrams(), parameters);
        BPMNDiagram newDiagram = BPMNDiagramFactory.newBPMNDiagram("");
        Map<String, BPMNNode> id2node = new HashMap<String, BPMNNode>();
        Map<String, Swimlane> id2lane = new HashMap<String, Swimlane>();
        if (parameters.getDiagram() == BpmnSelectDiagramParameters.NODIAGRAM) {
            bpmn.unmarshall(newDiagram, id2node, id2lane);
        } else {
            Collection<String> elements = parameters.getDiagram().getElements();
            bpmn.unmarshall(newDiagram, elements, id2node, id2lane);
        }
        Object[] object = BPMNToPetriNetConverter.convert(newDiagram);
        Petrinet pnet = (Petrinet) object[0];

        int count = 1;
        for(Place p : pnet.getPlaces()) {
            if(p.getLabel().isEmpty()) {
                p.getAttributeMap().put(AttributeMap.LABEL, "_empty_" + count++);
            }
        }
        return object;
    }

    public static PetriNet createPetriNetFromFile(String filename) throws Exception {
        if(filename.length() < 6) throw new Exception();
        Object[] pnetAndMarking = null;
        String extension = filename.substring(filename.length() -5, filename.length());
        if(extension == ".bpmn")
            pnetAndMarking = importPetrinetFromBPMN(filename);
        else if(extension.equals(".pnml"))
            pnetAndMarking = importPetriNetAndMarking(filename);
        if(pnetAndMarking==null) throw new Exception();
        PetriNet pNet = new PetriNet();
        Petrinet pnet = (Petrinet) pnetAndMarking[0];
        ObjectIntHashMap<Place> placeMapping = new ObjectIntHashMap<Place>();
        ObjectIntHashMap<Transition> transitionMapping = new ObjectIntHashMap<Transition>();
        for(Place p : pnet.getPlaces()) {
            placeMapping.put(p,pNet.addPlace(p.getLabel()));
        }
        for(Transition tr : pnet.getTransitions())
            transitionMapping.put(tr, pNet.addTransition(tr.getLabel(),!tr.isInvisible()));

        for(Place p : pnet.getPlaces())
            for(PetrinetEdge e : pnet.getOutEdges(p))
                pNet.addPTflow(placeMapping.get(p), transitionMapping.get((Transition) e.getTarget()));

        for(Transition tr : pnet.getTransitions())
            for(PetrinetEdge e : pnet.getOutEdges(tr))
                pNet.addTPflow(transitionMapping.get(tr),placeMapping.get((Place) e.getTarget()));
        pNet.calculateIncidenceMatrix();
        //pNet.setInitialAndFinalMarking();
        Marking initMarking = (Marking) pnetAndMarking[1];
        IntIntHashMap iMarking = new IntIntHashMap();
        for(Place p : initMarking)
            iMarking.addToValue(placeMapping.get(p),1);
        //System.out.println(initMarking);
        //System.out.println(pNet.getMarkingLabel(iMarking));
        pNet.setInitialMarking(iMarking);
        return pNet;

    }
}
