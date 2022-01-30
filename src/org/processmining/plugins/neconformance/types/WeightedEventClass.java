package org.processmining.plugins.neconformance.types;

import org.deckfour.xes.classification.XEventClass;

public class WeightedEventClass {
	public final XEventClass eventClass;
	public final double weight;

	public WeightedEventClass(XEventClass eventClass, double weight) {
		this.eventClass = eventClass;
		this.weight = weight;
	}

	public int hashCode() {
		return eventClass.hashCode();
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WeightedEventClass other = (WeightedEventClass) obj;
		return eventClass.equals(other.eventClass);
	}

	public String toString() {
		return eventClass.getId() + " (" + weight + ")";
	}
}