package au.qut.apromore.PetriNet;

import cern.colt.matrix.DoubleFactory2D;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

import java.io.IOException;
import java.io.PrintWriter;

public class SynchronousNet extends PetriNet
{
    PetriNet eventNet, processNet;
    public enum op {match, rhide, lhide};
    public FastList<SynchronousTransition> syncTransitions = new FastList<SynchronousTransition>();
    IntArrayList trCost = new IntArrayList();
    int pTrSize;
    public SynchronousNet(PetriNet eventNet, PetriNet processNet)
    {
        this.eventNet = eventNet;
        this.processNet = processNet;
        int pPlaceSize = processNet.places.size();
        pTrSize = processNet.transitions.size();
        int pEvTrSize = pTrSize + eventNet.transitions.size();
        IntHashSet set;
        for(int p = 0; p < pPlaceSize;p++) {
            this.places.add(processNet.places.get(p));
            set = new IntHashSet();
            if(processNet.pOutEdges.containsKey(p))
                set.addAll(processNet.pOutEdges.get(p));
            pOutEdges.put(p,set);
            set = new IntHashSet();
            if(processNet.pInEdges.containsKey(p))
                set.addAll(processNet.pInEdges.get(p));
            pInEdges.put(p,set);
        }
        for(int p = pPlaceSize; p < pPlaceSize + eventNet.places.size(); p++)
        {
            this.places.add(eventNet.places.get(p-pPlaceSize));
            set = new IntHashSet();
            if(eventNet.pOutEdges.containsKey(p-pPlaceSize))
                for(int tr : eventNet.pOutEdges.get(p-pPlaceSize).toArray())
                    set.add(tr + pTrSize);
            pOutEdges.put(p,set);
            set = new IntHashSet();
            if(eventNet.pInEdges.containsKey(p-pPlaceSize))
                if(pInEdges.get(p-pPlaceSize)!=null)
                    for(int tr : pInEdges.get(p-pPlaceSize).toArray())
                        set.add(tr+pTrSize);
            pInEdges.put(p,set);
        }

        for(int tr = 0; tr < processNet.transitions.size(); tr++) {
            syncTransitions.add(new SynchronousTransition(null, processNet.transitions.get(tr)));
            if(processNet.transitions.get(tr).isVisible && !processNet.transitions.get(tr).label.equals("tau")) trCost.add(1);
            else trCost.add(0);
            set = new IntHashSet();
            set.addAll(processNet.tOutEdges.get(tr));
            tOutEdges.put(tr,set);
            set = new IntHashSet();
            set.addAll(processNet.tInEdges.get(tr));
            tInEdges.put(tr,set);

        }
        for(int tr = pTrSize; tr < pTrSize + eventNet.transitions.size(); tr++)
        {
            syncTransitions.add(new SynchronousTransition(eventNet.transitions.get(tr - pTrSize), null));
            trCost.add(1);
            set = new IntHashSet();
            for(int p : eventNet.tOutEdges.get(tr - pTrSize).toArray())
                set.add(p + pPlaceSize);
            tOutEdges.put(tr,set);
            set = new IntHashSet();
            for(int p : eventNet.tInEdges.get(tr - pTrSize).toArray())
                set.add(p + pPlaceSize);
            tInEdges.put(tr,set);
        }
        int noSyncTrs = 0;
        for(int eventTr=0; eventTr < eventNet.transitions.size(); eventTr++)
            for(int processTr = 0; processTr < pTrSize; processTr++)
                if(eventNet.transitions.get(eventTr).label().equals(processNet.transitions.get(processTr).label()))
                {
                    syncTransitions.add(new SynchronousTransition(eventNet.transitions.get(eventTr), processNet.transitions.get(processTr)));
                    trCost.add(0);
                    set = new IntHashSet();
                    for(int p : eventNet.tOutEdges.get(eventTr).toArray()) {
                        set.add(p + pPlaceSize);
                        pInEdges.get(p+pPlaceSize).add(pEvTrSize + noSyncTrs);
                    }
                    for(int p : processNet.tOutEdges.get(processTr).toArray()) {
                        set.add(p);
                        pInEdges.get(p).add(pEvTrSize + noSyncTrs);
                    }
                    tOutEdges.put(pEvTrSize + noSyncTrs, set);
                    set = new IntHashSet();
                    for(int p : eventNet.tInEdges.get(eventTr).toArray()) {
                        set.add(p + pPlaceSize);
                        pOutEdges.get(p+pPlaceSize).add(pEvTrSize + noSyncTrs);
                    }
                    for(int p : processNet.tInEdges.get(processTr).toArray())
                    {
                        set.add(p);
                        pOutEdges.get(p).add(pEvTrSize + noSyncTrs);
                    }
                    tInEdges.put(pEvTrSize + noSyncTrs, set);
                    noSyncTrs++;
                }
        //System.out.println(syncTransitions.size() + " = " + trCost.size() + "?");
        //System.out.println(trCost);
            //System.out.println(noSyncTrs);
        /*//Add the processNet flows
        for(int p=0;p<eventNet.places.size();p++)
            for(int out : eventNet.pOutEdges.get(p).toArray())
                addPTflow(p+pPlaceSize, out+pTrSize);
        for(int tr=0;tr<pTrSize;tr++)
            for(int out : processNet.tOutEdges.get(tr).toArray())
                ;

        //Add the eventNetFlows
        for(int p=0;p<eventNet.places.size();p++)
            for(int out : eventNet.pOutEdges.get(p).toArray())
                addPTflow(p+pPlaceSize, out+pTrSize);
        for(int tr=0;tr<eventNet.transitions.size();tr++)
            for(int out : eventNet.tOutEdges.get(tr).toArray())
                addTPflow(tr+pTrSize,out+pPlaceSize);
*/
        ptFlows = DoubleFactory2D.sparse.make(places.size(), syncTransitions.size(),0);
        tpFlows = DoubleFactory2D.sparse.make(places.size(), syncTransitions.size(), 0);
        //add all PTFlows
        for(int p=0;p<places.size();p++)
            for(int out : pOutEdges.get(p).toArray())
                addPTflow(p,out);
        //add all TPFlows
        for(int tr = 0; tr < syncTransitions.size(); tr++)
            for(int out : tOutEdges.get(tr).toArray())
                addTPflow(tr,out);
        calculateIncidenceMatrix();
        //setInitialAndFinalMarking();
        this.initialMarking = new IntIntHashMap(processNet.initialMarking);
        for(int key : eventNet.initialMarking.keySet().toArray())
            this.initialMarking.addToValue(key + pPlaceSize, eventNet.initialMarking.get(key));
        this.finalMarking = new IntIntHashMap(processNet.finalMarking);
        for(int key : eventNet.finalMarking.keySet().toArray())
            this.finalMarking.addToValue(key + pPlaceSize, eventNet.finalMarking.get(key));
    }

    @Override
    public void calculateIncidenceMatrix()
    {
        incidenceMatrix = DoubleFactory2D.sparse.make(this.places.size(), this.syncTransitions.size(),0);
        for(int transitionID = 0; transitionID < syncTransitions.size(); transitionID++)
            for(int  placeID = 0; placeID < places.size(); placeID++)
                incidenceMatrix.set(placeID, transitionID,tpFlows.get(placeID, transitionID) - ptFlows.get(placeID, transitionID));
    }

    @Override
    public void toDot(PrintWriter pw) throws IOException {
        pw.println("digraph fsm {");
        pw.println("rankdir=LR;");
        pw.println("node [shape=circle,style=filled, fillcolor=white]");

        for(int p = 0; p < places.size(); p++)
        {
            Place pl = places.get(p);
            pw.printf("%d [label=\"%s\"];%n", pl.id(),pl.label());
        }


        for(int t = 0; t < syncTransitions.size(); t++)
        {
            SynchronousTransition tr = syncTransitions.get(t);
            pw.printf("%d [label=\"%s\", shape=\"box\"];%n", tr.id() + places.size(), tr.operation() + "("+ tr.label() +")");
        }

        for(int p = 0; p < places.size(); p++)
            if(pOutEdges.containsKey(p))
                for(int t : pOutEdges.get(p).toArray())
                    pw.printf("%d -> %d;%n", places.get(p).id(), syncTransitions.get(t).id() + places.size());

        for(int t = 0; t < syncTransitions.size(); t++)
            if(tOutEdges.containsKey(t))
                for(int p : tOutEdges.get(t).toArray())
                    pw.printf("%d -> %d;%n", syncTransitions.get(t).id() + places.size(), places.get(p).id());

        pw.println("}");
    }

    public IntArrayList trCost() { return this.trCost; }

    public int nProNetTransitions() {return this.pTrSize; }
}
