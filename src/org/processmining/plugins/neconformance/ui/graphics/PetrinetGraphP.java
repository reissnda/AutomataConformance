/**
 * 
 */
package org.processmining.plugins.neconformance.ui.graphics;

import java.awt.Dimension;

import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl;


/**
 * @author aadrians
 * Oct 26, 2011
 *
 */
public class PetrinetGraphP extends ResetInhibitorNetImpl {
	public PetrinetGraphP(String label) {
		super(label);
	}
	
	public synchronized TransitionP addTransition(String label) {
		TransitionP t = new TransitionP(label, this);
		transitions.add(t);
		graphElementAdded(t);
		return t;
	}

	public synchronized TransitionP addTransition(String label, Dimension dimension) {
		TransitionP t = new TransitionP(label, this, dimension);
		transitions.add(t);
		graphElementAdded(t);
		return t;
	}
	
	public synchronized PlaceP addPlace(String label) {
		PlaceP p = new PlaceP(label, this);
		places.add(p);
		graphElementAdded(p);
		return p;
	}
	
	public synchronized PlaceP addPlace(String label, Dimension dimension) {
		PlaceP p = new PlaceP(label, this, dimension);
		places.add(p);
		graphElementAdded(p);
		return p;
	}
	
	
}
