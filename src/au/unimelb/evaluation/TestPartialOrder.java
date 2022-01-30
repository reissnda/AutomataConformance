package au.unimelb.evaluation;

import au.unimelb.partialorders.PartialOrder;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

public class TestPartialOrder
{
    public static void main(String[] args)
    {

        UnifiedSet<IntHashSet> concurrencies = new UnifiedSet<>();
        //IntArrayList trace = IntArrayList.newListWith(1,1,0,1,1,2,1,0,1,2,1,2);
        //concurrencies.add(IntHashSet.newSetWith(0,1));
        IntArrayList trace = IntArrayList.newListWith(0, 1, 2, 6, 7, 8, 3, 9, 4);
        String strConcurrencies = "[[1, 5], [2, 5], [1, 6], [3, 5], [2, 6], [1, 7], [3, 6], [2, 7], [3, 7], [5, 6], [3, 8], [4, 7], [1, 10], [5, 7], [3, 9], [5, 8], [6, 7], [3, 10], [5, 9], [6, 8], [5, 10], [7, 8], [8, 9], [7, 10], [6, 10], [7, 9], [9, 10], [8, 10]]";
        //System.out.println(strConcurrencies);
        concurrencies = extractConcurrencies(strConcurrencies);
        PartialOrder testPartialOrder = new PartialOrder(trace,concurrencies);
        System.out.println(testPartialOrder.representativeTraces());
    }

    private static UnifiedSet<IntHashSet> extractConcurrencies(String strConcurrencies)
    {
        UnifiedSet<IntHashSet> extractConcurrencies = new UnifiedSet<>();
        String[] removedClosingBracket = strConcurrencies.split("]");
        for(String concurrentPair : removedClosingBracket)
        {
            if(concurrentPair.isEmpty()) continue;
            //concurrentPair.replace("[","");
            concurrentPair = concurrentPair.replaceAll("\\[","");
            concurrentPair = concurrentPair.replaceAll(" ","");
            if(concurrentPair.charAt(0)==',')
            {
                concurrentPair=concurrentPair.substring(1,concurrentPair.length());
            }
            String[] concurrentLabels = concurrentPair.split(",");
            extractConcurrencies.add(IntHashSet.newSetWith(Integer.parseInt(concurrentLabels[0]),Integer.parseInt(concurrentLabels[1])));
        }
        //System.out.println(extractConcurrencies);
        return extractConcurrencies;
    }
}
