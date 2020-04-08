/*
 * bwtest.java for sais-java
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

public class bwtest {

  private static
  void
  unbwt(byte[] T, byte[] U, int[] LF, int n, int pidx) {
    int[] C = new int[256];
    int i, t;
    for(i = 0; i < 256; ++i) { C[i] = 0; }
    for(i = 0; i < n; ++i) { LF[i] = C[(int)(T[i] & 0xff)]++; }
    for(i = 0, t = 0; i < 256; ++i) { t += C[i]; C[i] = t - C[i]; }
    for(i = n - 1, t = 0; 0 <= i; --i) {
      t = LF[t] + C[(int)((U[i] = T[t]) & 0xff)];
      t += (t < pidx) ? 1 : 0;
    }
    C = null;
  }

  public static
  void
  main(String[] args) {
    byte[] T, U, V;
    int[] A;
    int i, j, n, pidx;
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
        U = new byte[n];
        V = new byte[n];
        A = new int[n];

        /* Read n bytes of data. */
        s.read(T);
        s.close();
        s = null; f = null;

        /* Construct the suffix array. */
        start = new Date().getTime();
        pidx = new sais().bwtransform(T, U, A, n);
        finish = new Date().getTime();
        System.out.println(((finish - start) / 1000.0) + " sec");

        System.out.print("unbwtcheck ... ");
        unbwt(U, V, A, n, pidx);
        for(j = 0; j < n; ++j) {
          if(T[j] != V[j]) {
            System.err.println("error " + j + ": " + T[j] + ", " + V[j]);
            return;
          }
        }
        System.err.println("Done.");

        T = null; U = null; V = null; A = null;
      } catch(IOException e) {
        e.printStackTrace();
      } catch(OutOfMemoryError e) {
        e.printStackTrace();
      }
    }
  }
}
