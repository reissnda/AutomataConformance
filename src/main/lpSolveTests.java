package main;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class lpSolveTests
{
    private static LpSolve lp;
    static {
        try {
            System.loadLibrary("lpsolve55");
            System.loadLibrary("lpsolve55j");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws LpSolveException {
        IntIntHashMap conf = new IntIntHashMap();
        conf.addToValue(1,20);
        conf.addToValue(2,40);
        conf.addToValue(3,17);
        Set<IntIntHashMap> loops = new UnifiedSet<>();
        IntIntHashMap loop = new IntIntHashMap();
        loop.addToValue(1,2);
        loop.addToValue(2,1);
        loops.add(loop);
        loop = new IntIntHashMap();
        loop.addToValue(1,2);
        loop.addToValue(3,1);
        loops.add(loop);
        long start = System.nanoTime();
        for(int i = 0; i < 100000; i++)
        {
            conf.addToValue(1,1);
            //calcLoopSkips(conf,loops);
            System.out.println(calcLoopSkips(conf,loops));
        }
        System.out.println(TimeUnit.SECONDS.convert(System.nanoTime()-start,TimeUnit.NANOSECONDS));
    }

    public static int calcLoopSkips(IntIntHashMap conf, Set<IntIntHashMap> loops) throws LpSolveException
    {
        int pos = 0;
        for(IntIntHashMap loop : loops)
            pos+=loop.size();
        int[] list = new int[pos];
        pos=0;
        MutableIntSet set = new IntHashSet();
        for(IntIntHashMap loop : loops)
        {
            for(int a : loop.keySet().toArray()) {
                if(set.add(a)) {
                    list[pos] = a;
                    pos++;
                }
            }
        }
        int Ncol, i, j, rh1, rh2, ret = 0;

        Ncol = pos + loops.size();

        int[] colno1 = new int[Ncol];
        int[] colno2 = new int[Ncol];
        double[] row1 = new double[Ncol];
        double[] row2 = new double[Ncol];
        int col_val;
        double row_val;
        int val_key;

        if(lp==null)
        {
            lp = LpSolve.makeLp(pos*2, Ncol);
            lp.setAddRowmode(true);
            for(int key = 0; key < pos; key++)
            {
                for(i = 0; i < Ncol;i++)
                {
                    row1[i] = 0;
                    row2[i] = 0;
                    colno1[i] = 0;
                    colno2[i] = 0;
                }
                j = 0;
                val_key = list[key];
                col_val = key+1;
                row_val = -1;
                colno1[j] = col_val;
                row1[j] = row_val;
                colno2[j] = col_val;
                row2[j++] = row_val;
                i=1;
                for(IntIntHashMap loop : loops)
                {
                    col_val = pos + i++;
                    row_val = loop.get(val_key);
                    colno1[j] = col_val;
                    row1[j] = -row_val;
                    colno2[j] = col_val;
                    row2[j++] = row_val;
                }
                col_val = conf.get(val_key);
                rh1 = -col_val;
                rh2 = col_val;
                lp.addConstraintex(j, row1, colno1, LpSolve.LE, rh1);
                lp.addConstraintex(j, row2, colno2, LpSolve.LE, rh2);
            }
            lp.setAddRowmode(false);
            j = 0;
            for(i=1; i<=pos; i++)
            {
                colno1[j] = i;
                row1[j++] = 1;
            }
            for(int loop=1;loop<=loops.size();loop++)
            {
                colno1[j] = i++;
                row1[j++] = 0;
            }
            lp.setObjFnex(j, row1, colno1);

        }
        else
        {
            for(int key = 1; key <= lp.getNorigRows(); key++)
                lp.delConstraint(key);
            for(int key = 0; key < pos; key++)
            {
                for(i = 0; i < Ncol;i++)
                {
                    row1[i] = 0;
                    row2[i] = 0;
                    colno1[i] = 0;
                    colno2[i] = 0;
                }
                j = 0;
                val_key = list[key];
                col_val = key+1;
                row_val = -1;
                colno1[j] = col_val;
                row1[j] = row_val;
                colno2[j] = col_val;
                row2[j++] = row_val;
                i=1;
                for(IntIntHashMap loop : loops)
                {
                    col_val = pos + i++;
                    row_val = loop.get(val_key);
                    colno1[j] = col_val;
                    row1[j] = -row_val;
                    colno2[j] = col_val;
                    row2[j++] = row_val;
                }
                col_val = conf.get(val_key);
                rh1 = -col_val;
                rh2 = col_val;
                lp.addConstraintex(j, row1, colno1, LpSolve.LE, rh1);
                lp.addConstraintex(j, row2, colno2, LpSolve.LE, rh2);
            }
            j = 0;
            for(i=1; i<=pos; i++)
            {
                colno1[j] = i;
                row1[j++] = 1;
            }

            for(int loop=1;loop<=loops.size();loop++)
            {
                colno1[j] =i++;
                row1[j++] = 0;
            }

            lp.setObjFnex(j, row1, colno1);
        }

        if(ret == 0) {
            lp.defaultBasis();
            lp.setMinim();
            lp.setVerbose(LpSolve.IMPORTANT);
            ret = lp.solve();
        }
        ret = (int) lp.getObjective();
        return(ret);
    }
}
