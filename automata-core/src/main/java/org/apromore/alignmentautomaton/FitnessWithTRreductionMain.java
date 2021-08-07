package org.apromore.alignmentautomaton;

import java.io.File;
import java.io.FileWriter;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;

public class FitnessWithTRreductionMain {

  public static String printAlignment(AllSyncReplayResult res) {
    String printAlignment = "[";
    for (int pos = 0; pos < res.getNodeInstanceLst().get(0).size(); pos++) {
      printAlignment +=
          "(" + res.getStepTypesLst().get(0).get(pos) + "," + res.getNodeInstanceLst().get(0).get(pos) + "), ";
    }
    printAlignment = printAlignment.substring(0, printAlignment.length() - 2) + "]";
    return printAlignment;
  }

  private static void recordResult(String path, String result) throws Exception {
    FileWriter pw = null;
    File eval = new File(path + "evaluation.txt");
    if (!eval.exists()) {
      pw = new FileWriter(path + "evaluation.txt", true);
      pw.append("type,model,time,cost,#Problems solved\n");
    }
    if (pw == null) {
      pw = new FileWriter(path + "evaluation.txt", true);
    }
    pw.append(result);
    pw.close();
  }
}

