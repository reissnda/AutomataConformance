package org.apromore.alignmentautomaton.importer;

import org.apromore.alignmentautomaton.psp.Couple;
import org.eclipse.collections.impl.list.mutable.primitive.BooleanArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

public class DecodeTandemRepeats {

  private final IntArrayList trace;

  int[] arTrace;

  int[] SA;

  int n;

  int k;

  IntArrayList p = new IntArrayList(); //starting positions of each LZ77 Block

  IntArrayList l = new IntArrayList(); //length of each LZ77 Block

  IntObjectHashMap<IntHashSet> tandemRepeats = new IntObjectHashMap<IntHashSet>();

  IntObjectHashMap<UnifiedSet<Couple<Integer, Integer>>> maximalPrimitiveRepeats = new IntObjectHashMap<>();

  IntIntHashMap finalConfiguration;

  IntIntHashMap finalReducedConfiguration;

  private IntArrayList startReduce, reduceLength, reduceTo;

  private IntArrayList reducedTrace;

  private IntIntHashMap reducedLabels;

  private int reductionLength = 0;

  public boolean doCompression = false;

  private DoubleArrayList adjustedCost;

  private DoubleArrayList adjustedRHIDECost;

  private BooleanArrayList isReduced;

  private IntIntHashMap SecondTRpositions;

  private BooleanArrayList isFirstTRelement;

//  public static void main(String[] args) {
//    IntArrayList test = new IntArrayList();
//    //test.addAll(1,2,1,1,2,1,1,2,2,1,1,1,2,1,1,2,1);
//    test.addAll(1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1);
//    //test.addAll(2,1,2,2,1,2,1,2,2,2,1,2);
//    //test.addAll(1,2,3,3,2,3,3,2,4,4,2,2);
//    //test.addAll(1,2,3,3,2,3,3,2,3,3,2,4,4,2,2);
//    //test.addAll(1,2,3,3,3,3,3);
//    //test.addAll(1,2,3,2,3,2,3,2,3,2,3,4,5,6);
//    //test.addAll(1,2,3,2,3,2,3,4,5,3,4,3,4,3,4,5,6);
//    //test.addAll(1,2,3,4,5);
//    System.out.println(test);
//    DecodeTandemRepeats decoder = new DecodeTandemRepeats(test, 0, test.size() * 2);
//    System.out.println("p: " + decoder.p);
//    System.out.println("l: " + decoder.l);
//    decoder.findMaximalPrimitiveTandemrepeats();
//    //for(int repeatStart : decoder.maximalPrimitiveRepeats.keySet().toArray())
//    //{
//    //    System.out.println("Repeat start: " + repeatStart + ", k: " + decoder.maximalPrimitiveRepeats.get(repeatStart).getFirst().getFirstElement() + ", maximal length " + decoder.maximalPrimitiveRepeats.get(repeatStart).getFirst().getSecondElement());
//    //}
//    decoder.reduceTandemRepeats();
//    System.out.println(decoder.trace);
//    System.out.println(decoder.reducedTrace);
//    System.out.println(decoder.reductionStartPositions());
//    System.out.println(decoder.reductionOriginalLength());
//    System.out.println(decoder.reductionCollapsedLength());
//    IntArrayList reconstructedTrace = new IntArrayList();
//    int posToReduce = 0;
//    int lengthTandemRepeat, numberRepetitions;
//    for (int pos = 0; pos < decoder.reducedTrace.size(); pos++) {
//      if (posToReduce < decoder.reductionStartPositions().size()) {
//        if (pos == decoder.reductionStartPositions().get(posToReduce) - 1) {
//          lengthTandemRepeat = decoder.reductionCollapsedLength().get(posToReduce) / 2;
//          numberRepetitions = decoder.reductionOriginalLength().get(posToReduce) / lengthTandemRepeat;
//          for (int itRepetitions = 0; itRepetitions < numberRepetitions; itRepetitions++) {
//            for (int posTandemRepeat = 0; posTandemRepeat < lengthTandemRepeat; posTandemRepeat++) {
//              reconstructedTrace.add(decoder.reducedTrace().get(pos + posTandemRepeat));
//            }
//          }
//          pos += decoder.reductionCollapsedLength().get(posToReduce) - 1;
//          posToReduce++;
//        } else {
//          reconstructedTrace.add(decoder.reducedTrace.get(pos));
//        }
//      } else {
//        reconstructedTrace.add(decoder.reducedTrace.get(pos));
//      }
//    }
//    System.out.println(reconstructedTrace);
//    System.out.println(reconstructedTrace.equals(decoder.trace()));
//  }

  public DecodeTandemRepeats() {
    this.trace = new IntArrayList();
    this.reducedTrace = trace;
    this.adjustedRHIDECost = new DoubleArrayList();
    this.adjustedRHIDECost.add(1.0);
  }

  public DecodeTandemRepeats(IntArrayList trace, int $, int $$) {
    this.trace = trace;
    //TObjectShortHashMap map = new TObjectShortHashMap();
    finalConfiguration = new IntIntHashMap();
    int count;
    for (int label : trace.distinct().toArray()) {
      count = trace.count(l -> l == label);
      if (count >= 3) {
        doCompression = true;
      }
      finalConfiguration.put(label, count);
    }
    if (doCompression) {
      n = k = trace.size();
      arTrace = new int[n + 2];
      arTrace[0] = $;
      arTrace[n + 1] = $$;
      for (int pos = 0; pos < trace.size(); pos++) {
        arTrace[pos + 1] = trace.get(pos);
      }
      SA = new SuffixArray(arTrace).getSA();
      //sais.suffixsort(this.arTrace, this.SA, this.n, n+2); //had some issue
      //KKP3(); //LZ77 with time complexity O(n) and space complexity O(3 n log(n))
      KKP2(); //LZ77 with time complexity O(n) and space complexity O(2 n log(n))
      detectTandemRepeatTypes();
      reduceTandemRepeats();
      finalReducedConfiguration = new IntIntHashMap();
      for (int label : this.reducedTrace().distinct().toArray()) {
        finalReducedConfiguration.put(label, this.reducedTrace().count(l -> l == label));
      }
    } else {
      this.reducedTrace = trace;
      this.adjustedRHIDECost = new DoubleArrayList();
      this.adjustedCost = new DoubleArrayList();
      this.isReduced = new BooleanArrayList();
      for (int pos = 0; pos < trace().size(); pos++) {
        this.adjustedRHIDECost.add(1.0);
        this.adjustedCost.add(1.0);
        this.isReduced.add(false);
      }
      this.adjustedRHIDECost.add(1.0);
      this.isReduced.add(false);
      finalReducedConfiguration = finalConfiguration;
    }

  }

  public IntArrayList trace() {
    return this.trace;
  }

  public int getReductionLength() {
    return this.reductionLength;
  }

  public IntArrayList reducedTrace() {
    if (reducedTrace == null) {
      reduceTandemRepeats();
    }
    return reducedTrace;
  }

  public IntArrayList reductionStartPositions() {
    if (this.startReduce == null)
    //findMaximalPrimitiveTandemrepeats();
    {
      this.startReduce = new IntArrayList();
    }
    return this.startReduce;
  }

  public IntArrayList reductionOriginalLength() {
    if (this.reduceLength == null)
    //findMaximalPrimitiveTandemrepeats();
    {
      this.reduceLength = new IntArrayList();
    }
    return this.reduceLength;
  }

  public IntArrayList reductionCollapsedLength() {
    if (this.reduceTo == null)
    //findMaximalPrimitiveTandemrepeats();
    {
      this.reduceTo = new IntArrayList();
    }
    return this.reduceTo;
  }

  public IntIntHashMap reducedLabels() {
    if (reducedLabels == null) {
      reduceTandemRepeats();
    }
    return reducedLabels;
  }

  public DoubleArrayList adjustedCost() {
    if (adjustedCost == null) {
      reduceTandemRepeats();
    }
    return adjustedCost;
  }

  public DoubleArrayList adjustedRHIDECost() {
    if (adjustedRHIDECost == null) {
      reduceTandemRepeats();
    }
    return adjustedRHIDECost;
  }

  public BooleanArrayList isReduced() {
    if (this.isReduced == null) {
      reduceTandemRepeats();
    }
    return isReduced;
  }

  public BooleanArrayList isFirstTRelement() {
    if (this.isFirstTRelement == null) {
      reduceTandemRepeats();
    }
    return this.isFirstTRelement;
  }

  public IntIntHashMap getSecondTRpositions() {
    if (SecondTRpositions == null) {
      reduceTandemRepeats();
    }
    return SecondTRpositions;
  }

  public void reduceTandemRepeats() {
    if (!tandemRepeats.isEmpty()) {
      findMaximalPrimitiveTandemrepeats();
    }
    UnifiedSet<Couple<Integer, Integer>> repeatLengths;
    int maxLength, maxK, curLength;
    startReduce = new IntArrayList();
    reduceLength = new IntArrayList();
    reduceTo = new IntArrayList();
    for (int startPos = 0; startPos < n + 1; startPos++) {
      if ((repeatLengths = maximalPrimitiveRepeats.get(startPos)) != null) {
        if (repeatLengths.size() != 0) {
          maxLength = 0;
          maxK = 1;
          for (Couple<Integer, Integer> repeatLength : repeatLengths) {
            curLength = repeatLength.getFirstElement() * repeatLength.getSecondElement();
            if (curLength > maxLength) {
              maxLength = curLength;
              maxK = repeatLength.getFirstElement();
            }
          }
          for (int pos = startPos + 1; pos < startPos + maxLength; pos++) {
            if ((repeatLengths = maximalPrimitiveRepeats.get(pos)) != null) {
              if (repeatLengths.size() != 0) {
                for (Couple<Integer, Integer> repeatLength : repeatLengths) {
                  curLength = repeatLength.getFirstElement() * repeatLength.getSecondElement();
                  if (curLength > maxLength) {
                    maxLength = curLength;
                    maxK = repeatLength.getFirstElement();
                    startPos = pos;
                    pos = startPos + 1;
                  }
                }
              }
            }
          }
          startReduce.add(startPos);
          reduceLength.add(maxLength);
          reduceTo.add(maxK * 2);
          startPos += maxLength - 1;
        }
      }
    }
    reducedLabels = new IntIntHashMap();
    //System.out.println("Start Pos : " + startReduce);
    //System.out.println("Reduce Length : " + reduceLength);
    //System.out.println("Reduce to : " + reduceTo);

    reducedTrace = new IntArrayList();
    reducedTrace.addAll(arTrace);

    for (int toReduce = startReduce.size() - 1; toReduce >= 0; toReduce--) {
      for (int pos = 0; pos < reduceLength.get(toReduce) - reduceTo.get(toReduce); pos++) {
        reducedLabels.addToValue(reducedTrace.get(startReduce.get(toReduce)), 1);
        reducedTrace.removeAtIndex(startReduce.get(toReduce));
      }
    }

    reducedTrace.removeAtIndex(reducedTrace.size() - 1);
    reducedTrace.removeAtIndex(0);
    this.reductionLength = trace.size() - reducedTrace.size();
       /*isReduced = new BooleanArrayList();
       int posTR=0, posTRstart=-1;
       if(!startReduce.isEmpty()) posTRstart = startReduce.get(0);
       for(int pos=0; pos<reducedTrace.size();pos++)
       {
           if(posTR < startReduce.size())
           {
               if (pos == posTRstart)
               {
                    for(int pos2=0;pos2<reduceTo.get(posTR);pos2++)
                    {
                        pos++;
                        isReduced.add(true);
                    }
                    if(++posTR < startReduce.size()) posTRstart = startReduce.get(posTR);
               }
               else isReduced.add(false);
           }
           else isReduced.add(false);
       }*/
    //System.out.println("Reduced Trace : " + reducedTrace);
    //System.out.println("Reduced Labels: " + reducedLabels);
    adjustedCost = new DoubleArrayList();
    isReduced = new BooleanArrayList();
    isFirstTRelement = new BooleanArrayList();
    SecondTRpositions = new IntIntHashMap();
    int posToReduce = 0;
    int copy;
    double cost;
    for (int pos = 0; pos < reducedTrace.size(); pos++) {
      if (posToReduce < startReduce.size()) {
        if (pos == startReduce.get(posToReduce) - 1) {
          for (int pos2 = posToReduce + 1; pos2 < startReduce.size(); pos2++) {
            copy = startReduce.removeAtIndex(pos2);
            startReduce.addAtIndex(pos2, copy - (reduceLength.get(posToReduce) - reduceTo.get(posToReduce)));
          }
          if (reduceLength.get(posToReduce) > reduceTo.get(posToReduce)) {
            cost = ((double) reduceLength.get(posToReduce)) / (((double) reduceTo.get(posToReduce)) / 2) - 2.5;
          } else {
            cost = 1;
          }
          for (int pos2 = pos; pos2 < pos + reduceTo.get(posToReduce); pos2++) {
            //adjustedCost.add(cost);
            if (reduceLength.get(posToReduce) == reduceTo.get(posToReduce)) {
              isReduced.add(false);
              adjustedCost.add(1.0);
            } else {
              adjustedCost.add(cost);
              isReduced.add(true);
            }

            if (pos2 < pos + (reduceTo.get(posToReduce) / 2)) {
              isFirstTRelement.add(true);
              SecondTRpositions.put(pos2, pos2 + reduceTo.get(posToReduce) / 2);
            } else {
              isFirstTRelement.add(false);
            }
          }

          pos += reduceTo.get(posToReduce) - 1;
          posToReduce++;
        } else {
          adjustedCost.add(1);
          isReduced.add(false);
          isFirstTRelement.add(false);
        }
      } else {
        adjustedCost.add(1);
        isReduced.add(false);
        isFirstTRelement.add(false);
      }
    }
    adjustedCost.add(1);
    isReduced.add(false);
    isFirstTRelement.add(false);
    adjustedRHIDECost = new DoubleArrayList();
    posToReduce = 0;
    for (int pos = 0; pos < adjustedCost.size(); pos++) {
      if (posToReduce < startReduce.size()) {
        if (pos == startReduce.get(posToReduce) - 1) {
          adjustedRHIDECost.add(1);
          posToReduce++;
        } else if (this.isReduced.get(pos)) {
          adjustedRHIDECost.add(adjustedCost.get(pos) + 1);
        } else {
          adjustedRHIDECost.add(1);
        }
      } else if (this.isReduced.get(pos)) {
        adjustedRHIDECost.add(adjustedCost.get(pos) + 1);
      } else {
        adjustedRHIDECost.add(1);
      }
    }

    //System.out.println(trace + " => " + (trace.size() - reducedTrace.size()));
       /* if(adjustedCost.average()>1.0)
            System.out.println(adjustedCost);*/
  }

  public void findMaximalPrimitiveTandemrepeats() {
    IntHashSet alphabet, posOneKs;
    IntArrayList alpha;
    //UnifiedMap<IntHashSet, UnifiedSet<IntArrayList>> alphabetRepeatMapping = new UnifiedMap<IntHashSet, UnifiedSet<IntArrayList>>();
    //UnifiedSet<IntArrayList> primitiveTandemRepeats;
    //IntObjectHashMap<UnifiedSet<IntArrayList>> posPrimitiveRepeatsMapping = new IntObjectHashMap<>();
    for (int pos : tandemRepeats.keySet().toArray()) {
      for (int k : tandemRepeats.get(pos).toArray()) {
        alpha = new IntArrayList();
        alphabet = new IntHashSet();
        for (int i = pos; i < pos + k; i++) {
          alpha.add(arTrace[i]);
          alphabet.add(arTrace[i]);
        }
        if (alphabet.size() == 1 || alphabet.size() == alpha.size()) {
          if (alpha.size() > alphabet.size()) {
            continue;
          }
          //tandem repeat is primitive
          determineMaxRepetitions(pos, alpha);
        } else if (alpha.size() % 2 == 0) {
          DecodeTandemRepeats decoder = new DecodeTandemRepeats(alpha, arTrace[0], arTrace[n + 1]);
          if ((posOneKs = decoder.tandemRepeats.get(1)) != null) {
            if ((posOneKs.contains(alpha.size() / 2)))//alpha is a primitive repeat type
            {
              determineMaxRepetitions(pos, alpha);
            }
          }
        } else {
          determineMaxRepetitions(pos, alpha);
        }
      }
    }

  }

  private void determineMaxRepetitions(int start, IntArrayList primitiveRepeat) {
    int nReps = 2;
    int startNewIt, endNewIt, length = primitiveRepeat.size();
    UnifiedSet<Couple<Integer, Integer>> repeatLengths;
    boolean valid = true;
    while (valid) {
      startNewIt = start + length * nReps;
      endNewIt = startNewIt + length - 1;
      if (endNewIt > n) {
        break;
      }
      for (int pos = startNewIt; pos <= endNewIt; pos++) {
        if (arTrace[pos] != primitiveRepeat.get(pos - startNewIt)) {
          valid = false;
          break;
        }
      }
      if (valid) {
        nReps++;
      }
    }
    Couple<Integer, Integer> repeatLength = new Couple<>(length, nReps);
    if ((repeatLengths = maximalPrimitiveRepeats.get(start)) == null) {
      repeatLengths = new UnifiedSet<Couple<Integer, Integer>>();
      maximalPrimitiveRepeats.put(start, repeatLengths);
    }
    repeatLengths.add(repeatLength);

  }

  private void detectTandemRepeatTypes() {
    for (int block = 0; block < p.size() - 1; block++) {
      Algorithm1A(p.get(block + 1), l.get(block));
      Algorithm1B(p.get(block), l.get(block), p.get(block + 1), l.get(block + 1));
    }
  }

  private void Algorithm1A(int h1, int lengthB) {
    int q, k1, k2, start;
    IntHashSet ks;
    for (int k = 1; k <= lengthB; k++) {
      q = h1 - k;
      k1 = 0;
      while ((h1 + k1 + 1 < n + 2) && (arTrace[h1 + k1] == arTrace[q + k1])) {
        k1++;
      }
      k2 = 0;
      while ((q - k2 - 1 > 0) && (arTrace[h1 - k2 - 1] == arTrace[q - k2 - 1])) {
        k2++;
      }
      start = Math.max(q - k2, q - k);
      if (k1 + k2 >= k && k1 > 0) {
        //System.out.println("A: " + start + " " + k);
        if ((ks = tandemRepeats.get(start)) == null) {
          ks = new IntHashSet();
          tandemRepeats.put(start, ks);
        }
        ks.add(k);
      }
    }
  }

  private void Algorithm1B(int h, int lengthB, int h1, int lengthB1) {
    int q, k1, k2, start;
    IntHashSet ks;
    for (int k = 1; k <= lengthB + lengthB1; k++) {
      q = h + k;
      k1 = 0;
      while ((q + k1 + 1 < n + 2) && (arTrace[q + k1] == arTrace[h + k1])) {
        k1++;
      }
      k2 = 0;
      while ((h - k2 - 1 > 0) && (arTrace[h - k2 - 1] == arTrace[q - k2 - 1])) {
        k2++;
      }
      start = Math.max(h - k2, h - k);
      if (k1 + k2 >= k && k1 > 0 && (start + k - 1 < h1) && k2 > 0) {
        //System.out.println("B: " + start + " " + k);
        if ((ks = tandemRepeats.get(start)) == null) {
          ks = new IntHashSet();
          tandemRepeats.put(start, ks);
        }
        ks.add(k);
      }
    }
  }

  private void KKP3() {
    int[] copy = new int[n + 2];
    copy[0] = copy[n + 1] = 0;
    for (int i = 1; i < n + 1; i++) {
      copy[i] = SA[i - 1];
    }
    SA = copy;
    int i, top = 0;
    int[] psv = new int[n + 2], nsv = new int[n + 2];
    for (i = 1; i <= n + 1; i++) {
      while (SA[top] > SA[i]) {
        nsv[SA[top]] = SA[i];
        psv[SA[top]] = SA[top - 1];
        top--;
      }
      top++;
      SA[top] = SA[i];
    }
    i = 1;
    while (i <= n) {
      i = lzFactor(i, psv[i], nsv[i]);
    }
  }

  private void KKP2() {
    //int[] copy = new int[n+2];
    //copy[0] = copy[n+1] = 0;
    //for(int i=1; i < n + 1;i++) copy[i] = SA[i-1];
    //SA = copy;
    SA[0] = SA[n + 1] = 0;
    int top = 0;
    int[] phi = new int[n + 2];
    for (int i = 1; i <= n + 1; i++) {
      while (SA[top] > SA[i]) {
        phi[SA[top]] = SA[i];
        top--;
      }
      top++;
      SA[top] = SA[i];
    }
    phi[0] = 0;
    int psv, nsv, next = 1;

    for (int t = 1; t <= n; t++) {
      nsv = phi[t];
      psv = phi[nsv];
      if (t == next) {
        next = lzFactor(t, psv, nsv);
      }
      phi[t] = psv;
      phi[nsv] = t;
    }
    p.add(n + 1);
    l.add(0);
  }

  private int lzFactor(int i, int psv, int nsv) {
    int l_psv = lcp(i, psv);
    int l_nsv = lcp(i, nsv);

    if (l_psv > l_nsv) {
      p.add(i);
      l.add(l_psv);
    } else {
      p.add(i);
      l.add(Math.max(1, l_nsv));
    }

    return i + Math.max(1, l.get(p.indexOf(i)));
  }

  private int lcp(int i, int pos) {
    int l, max;
    l = 0;
    for (l = 0; l <= n - i; l++) {
      if (arTrace[i + l] != arTrace[pos + l]) {
        break;
      }
    }
    return l;
  }

}
