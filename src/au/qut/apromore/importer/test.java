package au.qut.apromore.importer;

import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.processmining.plugins.signaturediscovery.util.UkkonenSuffixTree;
import sais.java.sais;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

public class test {
    public static void main(String[] args)
    {
        IntArrayList test = new IntArrayList();
        test.addAll(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16);
        //System.out.println(au.qut.apromore.importer.test.subList(test, 7,11));
        String t = "test";
        //t = "abaabaabbaaabaaba";
        t="mississippii";
        //System.out.println(t.substring(1,2));
        UkkonenSuffixTree st = new UkkonenSuffixTree(1,t);
        st.printTree();
        st.LZDecomposition();
        System.out.println(st.getTandemRepeats());

        //System.out.println(st.getPrimitiveTandemRepeats());
        //for(String tandemRepeat : st.getTandemRepeats())
            //System.out.println(tandemRepeat + " - " + getPrimitiveRepeat(tandemRepeat));
        //System.out.println(getPrimitiveRepeat("ississ"));
        //int[] test2 = st.getMatches("i");
        //for(int i =0; i<test2.length;i++)
        //System.out.println(test2[i] );
        IntArrayList l1 = new IntArrayList();
        l1.addAll(1,2,3);
        IntArrayList l2 = new IntArrayList();
        l2.addAll(1,2,3);
        System.out.println(l1.equals(l2));

        /*int[] string = new int[]{1,2,1,1,2,1,1,2,2,1,1,1,2,1,1,2,1};
        int[] SA = new int[string.length];
        int n = string.length; int k = n;
        System.out.println(sais.suffixsort(string,SA,n,k));
        String SAresult = "";
        for(int i = 0; i<SA.length;i++) SAresult += SA[i] + " ";
        System.out.println(SAresult);*/

    }

      public static HashMap<TreeSet<String>, TreeSet<String>>  alphabetTandemRepeatMap = new HashMap<TreeSet<String>, TreeSet<String>>();
      public static HashMap<TreeSet<String>, TreeSet<String>>alphabetPrimitiveRepeatMap = new HashMap<TreeSet<String>, TreeSet<String>>();
      public static HashSet<String> complexTandemRepeats = new HashSet<String>();

      private static String getPrimitiveRepeat(String tandemRepeat)
      {
            String primitiveRepeat = "";
            TreeSet<String> tandemRepeatAlphabet = new TreeSet<String>();
            TreeSet<String> alphabetTandemRepeatSet, alphabetPrimitiveRepeatSet;
            boolean isComplex = false;
            int tandemRepeatLength = tandemRepeat.length();


            for(int i = 0; i < tandemRepeatLength; i++)
            {
                tandemRepeatAlphabet.add(tandemRepeat.substring(i, i+1));
            }

            if(alphabetTandemRepeatMap.containsKey(tandemRepeatAlphabet))
            {
                alphabetTandemRepeatSet = alphabetPrimitiveRepeatMap.get(tandemRepeatAlphabet);
            }
            else
            {
                alphabetTandemRepeatSet = new TreeSet<String>();
            }
            alphabetTandemRepeatSet.add(tandemRepeat);
            alphabetTandemRepeatMap.put(tandemRepeatAlphabet, alphabetTandemRepeatSet);
            /**
            * Simple Cases
            */

            if(tandemRepeatAlphabet.size() == 1)
            {
                primitiveRepeat = tandemRepeat.substring(0, 1);
            }
            else if(tandemRepeatLength == tandemRepeatAlphabet.size())
            {
                primitiveRepeat = tandemRepeat;
            }
            else
            {
                isComplex = true;
                complexTandemRepeats.add(tandemRepeat);
            }

            if(!isComplex)
            {
                if(alphabetPrimitiveRepeatMap.containsKey(tandemRepeatAlphabet)){
                    alphabetPrimitiveRepeatSet = alphabetPrimitiveRepeatMap.get(tandemRepeatAlphabet);
                }
                else
                {
                    alphabetPrimitiveRepeatSet = new TreeSet<String>();
                }

                alphabetPrimitiveRepeatSet.add(primitiveRepeat);
                alphabetPrimitiveRepeatMap.put(tandemRepeatAlphabet,alphabetPrimitiveRepeatSet);

            }

            return primitiveRepeat;
      }


}
