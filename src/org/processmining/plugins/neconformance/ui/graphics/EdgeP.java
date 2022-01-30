/**
 * 
 */
package org.processmining.plugins.neconformance.ui.graphics;

import java.awt.Graphics2D;

import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.shapes.Decorated;

/**
 * @author aadrians
 * Oct 26, 2011
 *
 */
public class EdgeP extends Arc implements Decorated {
	protected IEdgePDecorator decorator;
	
	public EdgeP(PetrinetNode source, PetrinetNode target, int weight) {
		super(source, target, weight);
	}

	public void decorate(Graphics2D g2d, double x, double y, double width, double height) {
		if (decorator != null){
			decorator.decorate(g2d, x, y, width, height);
		}
	}

}
