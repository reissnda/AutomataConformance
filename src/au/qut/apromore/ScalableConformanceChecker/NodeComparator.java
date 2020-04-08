package au.qut.apromore.ScalableConformanceChecker;
import java.util.Comparator;

import au.qut.apromore.psp.Configuration;
import au.qut.apromore.psp.Node;
import au.qut.apromore.psp.Synchronization;

/*
 * Copyright © 2009-2017 The Apromore Initiative.
 *
 * This file is part of "Apromore".
 *
 * "Apromore" is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * "Apromore" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */

/**
 * @author Daniel Reissner,
 * @version 1.0, 01.02.2017
 */

public class NodeComparator implements Comparator<Node>
{
	@Override
	public int compare(Node x, Node y)
	{
		
		//return (int) (x.weight() - y.weight());
		//double weight =  x.weight() - y.weight();
		//if(weight != 0 ) return (int) weight;
		//int x_size = x.configuration().sequenceSynchronizations().size();
		//int y_size = y.configuration().sequenceSynchronizations().size();
		//return 0;
		//if(x_size ==0 || y_size==0) return -x_size + y_size;
		//if(x_size ==0 || y_size==0) return 0;
		//if(x_size ==0 || y_size==0)  //|| x_size != y_size) return - x_size + y_size;
		/*Synchronization x_sync = x.configuration().sequenceSynchronizations().get(x_size-1);
		Synchronization y_sync = y.configuration().sequenceSynchronizations().get(y_size-1);
		int op = y_sync.operation().ordinal() - x_sync.operation().ordinal();
		if(op != 0) return op;
		if(x_sync.operation()==Configuration.Operation.RHIDE)
			return x_sync.eventModel() - y_sync.eventModel();
		else
			return x_sync.eventLog() - y_sync.eventLog();*/
		int score_x = x.getQueueing_score(), score_y = y.getQueueing_score();
		/*if(score_x==score_y)
		{
			int length = Math.min(x.configuration().sequenceSynchronizations().size(), y.configuration().sequenceSynchronizations().size());
			Configuration.Operation x_op, y_op;
			for(int pos=0; pos<length; pos++)
			{
				x_op = x.configuration().sequenceSynchronizations().get(pos).operation();
				y_op = y.configuration().sequenceSynchronizations().get(pos).operation();
				if(x_op!=y_op)
				{
					return y_op.ordinal()-x_op.ordinal();
				}
			}
		}*/
		return score_x-score_y;
	}
}
