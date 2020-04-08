/*
 * suftest.java for sais-java
 * Copyright (c) 2008-2010 Yuta Mori All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.*;
import java.util.*;

public class suftest {

  private static
  int
  sufcheck(byte[] T, int[] SA, int n, boolean verbose) {
    int[] C = new int[256];
    int i, p, q, t;
    int c;

    if(verbose) { System.err.print("sufcheck: "); }
    if(n == 0) {
      if(verbose) { System.err.println("Done."); }
      return 0;
    }

    /* Check arguments. */
    if((T == null) || (SA == null) || (n < 0)) {
      if(verbose) { System.err.println("Invalid arguments."); }
      return -1;
    }

    /* check range: [0..n-1] */
    for(i = 0; i < n; ++i) {
      if((SA[i] < 0) || (n <= SA[i])) {
        if(verbose) {
          System.err.println("Out of the range [0," + (n - 1) + "].");
          System.err.println("  SA[" + i + "]=" + SA[i]);
        }
        return -2;
      }
    }

    /* check first characters. */
    for(i = 1; i < n; ++i) {
      if((int)(T[SA[i - 1]] & 0xff) > (int)(T[SA[i]] & 0xff)) {
        if(verbose) {
          System.err.println("Suffixes in wrong order.");
          System.err.print("  T[SA[" + (i - 1) + "]=" + SA[i - 1] + "]=" + (int)(T[SA[i - 1]] & 0xff));
          System.err.println(" > T[SA[" + i + "]=" + SA[i] + "]=" + (int)(T[SA[i]] & 0xff));
        }
        return -3;
      }
    }

    /* check suffixes. */
    for(i = 0; i < 256; ++i) { C[i] = 0; }
    for(i = 0; i < n; ++i) { ++C[(int)(T[i] & 0xff)]; }
    for(i = 0, p = 0; i < 256; ++i) {
      t = C[i];
      C[i] = p;
      p += t;
    }

    q = C[(int)(T[n - 1] & 0xff)];
    C[(int)(T[n - 1] & 0xff)] += 1;
    for(i = 0; i < n; ++i) {
      p = SA[i];
      if(0 < p) {
        c = (int)(T[--p] & 0xff);
        t = C[c];
      } else {
        c = (int)(T[p = n - 1] & 0xff);
        t = q;
      }
      if((t < 0) || (p != SA[t])) {
        if(verbose) {
          System.err.println("Suffixes in wrong position.");
          System.err.println("  SA[" + t + "]=" + ((0 <= t) ? SA[t] : -1) + " or");
          System.err.println("  SA[" + i + "]=" + SA[i]);
        }
        return -4;
      }
      if(t != q) {
        ++C[c];
        if((n <= C[c]) || ((int)(T[SA[C[c]]] & 0xff) != c)) { C[c] = -1; }
      }
    }
    C = null;

    if(verbose) { System.err.println("Done."); }
    return 0;
  }


  public static
  void
  main(String[] args) {
    byte[] T;
    int[] SA;
    int i, n;
    long start, finish;

    for(i = 0; i < args.length; ++i) {
      System.out.print(args[i] + ": ");
      try {
        /* Open a file for reading. */
        File f = new File(args[i]);
        FileInputStream s = new FileInputStream(f);

        n = (int)f.length();
        System.out.print(n + " bytes ... ");

        /* Allocate 5n bytes of memory. */
        T = new byte[n];
        SA = new int[n];

        /* Read n bytes of data. */
        s.read(T);
        s.close();
        s = null; f = null;

        /* Construct the suffix array. */
        start = new Date().getTime();
        new sais().suffixsort(T, SA, n);
        finish = new Date().getTime();
        System.out.println(((finish - start) / 1000.0) + " sec");

        /* Check the suffix array. */
        sufcheck(T, SA, n, true);

        T = null; SA = null;
      } catch(IOException e) {
        e.printStackTrace();
      } catch(OutOfMemoryError e) {
        e.printStackTrace();
      }
    }
  }
}
