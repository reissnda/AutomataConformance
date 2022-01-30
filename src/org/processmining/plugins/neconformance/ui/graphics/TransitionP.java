/**
 * 
 */
package org.processmining.plugins.neconformance.ui.graphics;

import java.awt.Dimension;
import java.awt.Graphics2D;

import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.AbstractDirectedGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.ExpandableSubNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.shapes.Decorated;

/**
 * @author aadrians
 * Oct 26, 2011
 *
 */
public class TransitionP extends Transition implements Decorated {

	protected ITransitionPDecorator decorator;
	
	public TransitionP(String label,
			AbstractDirectedGraph<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> net) {
		this(label, net, new Dimension(50, 35));
	}

	public TransitionP(String label,
			AbstractDirectedGraph<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> net, Dimension dimension) {
		super(label, net);
		getAttributeMap().put(AttributeMap.SHOWLABEL, false);
		getAttributeMap().put(AttributeMap.SIZE, dimension);
	}

	public TransitionP(String label, PetrinetGraphP net, ExpandableSubNet parent) {
		this(label, net, parent, new Dimension(50, 35));
	}

	public TransitionP(String label, PetrinetGraphP net, ExpandableSubNet parent, Dimension dimension) {
		super(label, net, parent);
		getAttributeMap().put(AttributeMap.SHOWLABEL, false);
		getAttributeMap().put(AttributeMap.SIZE, dimension);
	}

	public void decorate(Graphics2D g2d, double x, double y, double width, double height) {
		if (decorator != null){
			decorator.decorate(g2d, x, y, width, height);
		}
	}

	/**
	 * @return the decorator
	 */
	public ITransitionPDecorator getDecorator() {
		return decorator;
	}

	/**
	 * @param decorator the decorator to set
	 */
	public void setDecorator(ITransitionPDecorator decorator) {
		this.decorator = decorator;
	}

}
