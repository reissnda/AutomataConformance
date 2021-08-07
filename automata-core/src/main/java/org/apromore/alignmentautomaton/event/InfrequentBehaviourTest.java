/*
 *  Copyright (C) 2018 Raffaele Conforti (www.raffaeleconforti.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.apromore.alignmentautomaton.event;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 20/4/17.
 */
public class InfrequentBehaviourTest {

  private static XLog test(XLog log, boolean useGurobi, boolean useArcsFrequency, boolean debug_mode) {
    System.out.println(count(log));
    InfrequentBehaviourFilter filter = new InfrequentBehaviourFilter(new XEventNameClassifier(), useGurobi,
        useArcsFrequency, debug_mode, true, true, 0.5, 0.5, true, 0.075);
    XLog filtered = filter.filterDeviances(log);
    System.out.println(count(filtered));
    return filtered;
  }

  private static int count(XLog log) {
    int count = 0;
    for (XTrace trace : log) {
      count += trace.size();
    }
    return count;
  }

}
