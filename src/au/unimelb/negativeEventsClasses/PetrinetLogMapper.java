package au.unimelb.negativeEventsClasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.processmining.framework.util.ArrayUtils;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.kutoolbox.logmappers.LogMapper;
import org.processmining.plugins.kutoolbox.utils.LogUtils;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class PetrinetLogMapper extends HashMap<Transition, XEventClass> implements LogMapper {
    private static final long serialVersionUID = -4344051692440782096L;
    private XEventClassifier eventClassifier;
    private Collection<XEventClass> eventClasses;
    private Collection<Transition> transitions;
    public static final XEventClass BLOCKING_CLASS = new XEventClass("BLOCKED VISIBLE", -101);

    public PetrinetLogMapper(XEventClassifier eventClassifier, XLog log, Collection<Transition> transitions) {
        this(eventClassifier, XEventClasses.deriveEventClasses(eventClassifier, log).getClasses(), transitions);
    }

    public PetrinetLogMapper(XEventClassifier eventClassifier, XEventClasses eventClasses, Collection<Transition> transitions) {
        this(eventClassifier, eventClasses.getClasses(), transitions);
    }

    public PetrinetLogMapper(XEventClassifier eventClassifier, Collection<XEventClass> eventClasses, Collection<Transition> transitions) {
        this.eventClassifier = eventClassifier;
        this.eventClasses = eventClasses;
        this.transitions = transitions;
    }

    public XEventClassifier getEventClassifier() {
        return this.eventClassifier;
    }

    public Collection<XEventClass> getEventClasses() {
        return this.eventClasses;
    }

    public Collection<XEventClass> getMappedEventClasses() {
        return this.values();
    }

    public boolean eventHasTransition(XEvent event) {
        Iterator var3 = this.keySet().iterator();

        while(var3.hasNext()) {
            Transition t = (Transition)var3.next();
            if (this.eventClassifier.getClassIdentity(event).equals(((XEventClass)this.get(t)).getId())) {
                return true;
            }
        }

        return false;
    }

    public boolean eventHasTransition(XEventClass eventClass) {
        return this.containsValue(eventClass);
    }

    public boolean eventHasTransition(String eventName) {
        Iterator var3 = this.keySet().iterator();

        while(var3.hasNext()) {
            Transition t = (Transition)var3.next();
            if (eventName.equals(((XEventClass)this.get(t)).getId())) {
                return true;
            }
        }

        return false;
    }

    public XEvent makeEvent(String classId) {
        XEvent event = new XEventImpl();
        String[] keys = this.getEventClassifier().getDefiningAttributeKeys();
        String[] values = classId.split("\\+");

        for(int i = 0; i < keys.length; ++i) {
            event.getAttributes().put(keys[i], new XAttributeLiteralImpl(keys[i], values[i]));
        }

        return event;
    }

    public Collection<Transition> getTransitions() {
        return this.transitions;
    }

    public boolean transitionEqualsEvent(Transition transition, XEvent event) {
        XEventClass eventClass = (XEventClass)this.get(transition);
        return eventClass == null ? false : this.eventClassifier.getClassIdentity(event).equals(eventClass.getId());
    }

    public boolean transitionHasEvent(Transition transition) {
        return this.containsKey(transition) && !((XEventClass)this.get(transition)).equals(BLOCKING_CLASS);
    }

    public boolean transitionIsInvisible(Transition tr)
    {
        String tLabel = tr.getLabel();
        String tau = "tau";
        String cTau = "Tau";
        String invisible = "invisible";
        String strRegEx = "(T|t)(\\d+)";
        String emptyStr = "";
        String empty = "empty";
        if(tLabel.contains(cTau) || tLabel.contains(tau) || tLabel.contains(invisible)
                || tLabel.contains(empty) || tLabel==emptyStr || tLabel.matches(strRegEx)) {
            return true;
        }
        return false;
    }

    public Collection<Transition> getTransitionsForActivity(String currentActivity) {
        HashSet<Transition> toReturn = new HashSet();
        Iterator var4 = this.keySet().iterator();

        while(var4.hasNext()) {
            Transition t = (Transition)var4.next();
            XEventClass eventClass = (XEventClass)this.get(t);
            if (currentActivity.equals(eventClass.getId())) {
                toReturn.add(t);
            }
        }

        return toReturn;
    }

    public void applyMappingOnTransitions() {
        Iterator var2 = this.transitions.iterator();

        while(var2.hasNext()) {
            Transition t = (Transition)var2.next();
            t.setInvisible(this.transitionHasEvent(t));
        }

    }

    public String toString() {
        String repr = "PetrinetLogMapper (eventClassifier=" + this.eventClassifier + ")\n";

        Transition transition;
        Iterator var3;
        for(var3 = this.keySet().iterator(); var3.hasNext(); repr = repr + "  " + transition.getLabel() + " --> " + ((XEventClass)this.get(transition)).getId() + "\n") {
            transition = (Transition)var3.next();
        }

        var3 = this.eventClasses.iterator();

        while(var3.hasNext()) {
            XEventClass xclass = (XEventClass)var3.next();
            if (!this.eventHasTransition(xclass)) {
                repr = repr + "  !! unmapped !! --> " + xclass.getId() + "\n";
            }
        }

        var3 = this.transitions.iterator();

        while(var3.hasNext()) {
            transition = (Transition)var3.next();
            if (!this.transitionHasEvent(transition)) {
                repr = repr + "  " + transition.getLabel() + " --> * unmapped *\n";
            }
        }

        return repr;
    }

    public static PetrinetLogMapper getStandardMap(XLog log, Petrinet net) {
        PetrinetLogMapper map = new PetrinetLogMapper(XLogInfoImpl.STANDARD_CLASSIFIER, LogUtils.getEventClasses(log, XLogInfoImpl.STANDARD_CLASSIFIER), net.getTransitions());
        Object[] boxOptions = extractEventClassList(log, XLogInfoImpl.STANDARD_CLASSIFIER);
        Iterator var5 = net.getTransitions().iterator();

        while(var5.hasNext()) {
            Transition transition = (Transition)var5.next();
            Object sEventClass = boxOptions[preSelectOption(transition.getLabel(), boxOptions)];
            if (sEventClass instanceof XEventClass) {
                XEventClass eventClass = (XEventClass)sEventClass;
                map.put(transition, eventClass);
            }
        }

        return map;
    }

    public static Object[] extractEventClassList(XLog log, XEventClassifier classifier) {
        Collection<XEventClass> classes = LogUtils.getEventClasses(log, classifier);
        Object[] arrEvClass = classes.toArray();
        Arrays.sort(arrEvClass);
        Object[] notMappedAct = new Object[]{"NONE"};
        Object[] blockedAct = new Object[]{BLOCKING_CLASS};
        Object[] boxOptions = ArrayUtils.concatAll(notMappedAct, new Object[][]{blockedAct, arrEvClass});
        return boxOptions;
    }

    public static Transition[] extractTransitionList(Petrinet net) {
        List<Transition> transitions = new ArrayList(net.getTransitions());
        Collections.sort(transitions, new Comparator<Transition>() {
            public int compare(Transition a, Transition b) {
                return a.getLabel().compareToIgnoreCase(b.getLabel());
            }
        });
        Transition[] arrTransitions = (Transition[])transitions.toArray(new Transition[0]);
        return arrTransitions;
    }

    public static int preSelectOption(String transition, Object[] events) {
        AbstractStringMetric metric = new Levenshtein();
        int index = 0;
        float simOld = metric.getSimilarity(transition, "none");
        simOld = Math.max(simOld, metric.getSimilarity(transition, "invisible"));
        simOld = Math.max(simOld, metric.getSimilarity(transition, "skip"));
        simOld = Math.max(simOld, metric.getSimilarity(transition, "tau"));
        Math.max(simOld, metric.getSimilarity(transition, "inv"));

        for(int i = 1; i < events.length; ++i) {
            String event = ((XEventClass)events[i]).toString();
            if (event.indexOf("+completeRejected") < 0 && event.indexOf("+rejected") < 0) {
                if (transition.equals(event)) {
                    index = i;
                    break;
                }

                if (event.replace("+complete", "").replace("\\ncomplete", "").equals(transition.replace("\\ncomplete", "").replace("+complete", ""))) {
                    index = i;
                    break;
                }
            }
        }

        return index;
    }

    public XEventClass getEventClass(String s)
    {
        for(XEventClass eClass : this.eventClasses)
        {
            if(eClass.getId().equals(s)) return eClass;
        }
        return null;
    }
}
