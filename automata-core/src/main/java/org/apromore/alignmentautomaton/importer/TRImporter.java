package org.apromore.alignmentautomaton.importer;

import java.io.File;
import java.io.FileWriter;
import org.apromore.alignmentautomaton.automaton.Automaton;
import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

public class TRImporter {

  public int nTraces = 0;

  public int events = 0;

  public IntArrayList nTRrepeatTpls = new IntArrayList();

  public DoubleArrayList nTRrepsList = new DoubleArrayList();

  public IntArrayList TRReductionLength = new IntArrayList();

  public IntArrayList TRReducedTraceLengths = new IntArrayList();

  public IntArrayList OriginalTraceLengths = new IntArrayList();

  public int uniqueTraces = 0;

  public int nTRconsidered;

  public int uniqueTracesAfterTR = 0;

  public String path;

  public XLog xLog;

  public Petrinet pnet;

  public Marking initMarking;

  String log;

  String model;

  public Automaton logAutomaton;

  public Automaton modelAutomaton;

  private DecomposingConformanceImporter stats;

  public ImportEventLog logImporter;

  public ImportProcessModel modelImporter;

  public TRImporter(String path, String log, String model) throws Exception {
    this.path = path;
    this.log = log;
    this.model = model;
    logImporter = new ImportEventLog();
    modelImporter = new ImportProcessModel();
    xLog = logImporter.importEventLog(path + log);
    Object[] pnetAndMarking = modelImporter.importPetriNetAndMarking(path + model);
    pnet = (Petrinet) pnetAndMarking[0];
    initMarking = (Marking) pnetAndMarking[1];
  }

  public void createAutomata() throws Exception {
    this.logAutomaton = logImporter.createReducedDAFSAfromLog(this.xLog);
    this.modelAutomaton = this.modelImporter
        .createFSMfromPetrinet(this.pnet, this.initMarking, this.logAutomaton.eventLabels(),
            this.logAutomaton.inverseEventLabels());
  }

  public void recordStatistics() throws Exception {
    logAutomaton = logImporter.convertLogToAutomatonWithTRFrom(path + log);
    stats = new DecomposingConformanceImporter();
    //stats.importModelForStatistics(path,model);
    stats.importAndDecomposeModelForStatistics(path, model);
    gatherLogStatistics();
    recordLogStatistics();
    recordModelStatistics();
  }

  private void gatherLogStatistics() {
    int reducedTraceLength = 0, originalTraceLength = 0;
    for (IntArrayList reducedTr : logAutomaton.reductions.keySet()) {
      uniqueTracesAfterTR++;
      reducedTraceLength = reducedTr.size();
      for (int reduction : logAutomaton.reductions.get(reducedTr).keySet().toArray()) {
        for (DecodeTandemRepeats decoder : logAutomaton.reductions.get(reducedTr).get(reduction)) {
          int nTRreps = 0;
          uniqueTraces++;
          events += (logAutomaton.caseFrequencies.get(decoder.trace()) * decoder.trace().size());
          nTraces += logAutomaton.caseFrequencies.get(decoder.trace());

          for (int redPos = 0; redPos < decoder.reductionStartPositions().size(); redPos++) {
            nTRreps +=
                decoder.reductionOriginalLength().get(redPos) / (decoder.reductionCollapsedLength().get(redPos) / 2);
          }
          nTRrepeatTpls.add(decoder.reductionStartPositions().size());
          nTRrepsList.add(nTRreps);
          //nTRreps+=decoder.re
          if (decoder.doCompression) {
            nTRconsidered++;
          }
          originalTraceLength = decoder.trace().size();
          TRReductionLength.add(originalTraceLength - reducedTraceLength);
          TRReducedTraceLengths.add(reducedTraceLength);
          OriginalTraceLengths.add(originalTraceLength);
        }
      }
    }
  }

  public void gatherTRStatistics() {
    int reducedTraceLength = 0, originalTraceLength = 0;
    DoubleArrayList lengths = new DoubleArrayList();
    IntArrayList nReduced = new IntArrayList();
    IntArrayList originalLengths = new IntArrayList();
    IntArrayList collapsedLengths = new IntArrayList();
    for (IntArrayList reducedTr : logAutomaton.reductions.keySet()) {
      //uniqueTracesAfterTR++;
      reducedTraceLength = reducedTr.size();
      for (int reduction : logAutomaton.reductions.get(reducedTr).keySet().toArray()) {
        for (DecodeTandemRepeats decoder : logAutomaton.reductions.get(reducedTr).get(reduction)) {
          int nTRtpls = 0;
          int nTRreps = 0;
          int lngth = 0;

          //uniqueTraces++;
          //events+=(logAutomaton.caseFrequencies.get(decoder.trace()) * decoder.trace().size());
          //nTraces += logAutomaton.caseFrequencies.get(decoder.trace());

          for (int redPos = 0; redPos < decoder.reductionStartPositions().size(); redPos++) {
            if (decoder.reductionOriginalLength().get(redPos) > decoder.reductionCollapsedLength().get(redPos)) {
              nTRtpls++;
              double length = decoder.reductionCollapsedLength().get(redPos) / 2;
              //nTRreps += decoder.reductionOriginalLength().get(redPos) / (decoder.reductionCollapsedLength().get(redPos) / 2);
              double originalLength = decoder.reductionOriginalLength().get(redPos);
              originalLengths.add((int) originalLength);
              collapsedLengths.add(decoder.reductionCollapsedLength().get(redPos));
              double reps = originalLength / length;
              nTRrepsList.add(reps);

              lengths.add(length);
              nReduced
                  .add(decoder.reductionOriginalLength().get(redPos) - decoder.reductionCollapsedLength().get(redPos));
              //System.out.println(originalLength + ", " + length + ", " + reps);
            }
          }
          nTRrepeatTpls.add(nTRtpls);
          //nTRrepsList.add(nTRreps);
          //nTRreps+=decoder.re
          //if(decoder.doCompression) nTRconsidered++;
          originalTraceLength = decoder.trace().size();
          TRReductionLength.add(originalTraceLength - reducedTraceLength);
          //TRReducedTraceLengths.add(reducedTraceLength);
          //OriginalTraceLengths.add(originalTraceLength);
        }
      }
    }
    if (nTRrepeatTpls.size() == 0) {
      nTRrepeatTpls.add(0);
    }
    if (nTRrepsList.size() == 0) {
      nTRrepsList.add(0);
    }
    if (lengths.size() == 0) {
      lengths.add(0);
    }
    if (nReduced.size() == 0) {
      nReduced.add(0);
    }
    if (originalLengths.size() == 0) {
      originalLengths.add(0);
    }
    if (collapsedLengths.size() == 0) {
      collapsedLengths.add(0);
    }
    //System.out.println(nTRrepeatTpls.size() + ", " + nTRrepsList.size() + ", " + lengths.size() + ", "  + nReduced.size() + ", " + originalLengths.size() + ", " + collapsedLengths.size() + ", " + TRReductionLength.size());
    System.out.println(
        nTRrepeatTpls.average() + ", " + nTRrepsList.average() + ", " + lengths.average() + ", " + nReduced.average()
            + ", " + originalLengths.average() + ", " + collapsedLengths.average());
  }

  private void recordLogStatistics() throws Exception {
    String headlines = "Path, Log File, #Events, #Unique Events, #Traces, #Unique Traces, #Times TR Considered, "
        + "#Unique Traces after TR, Original Trace length average, (median,max), "
        + "Reduction length average, (median,max), Reduced Trace Lengths average, (median,max), "
        + "#TR touples average, (max,sum), #Repetitions average, (max, sum)\n";
    String result = path + "," + log + "," + events + "," + logAutomaton.eventLabels().size() + "," + nTraces + ","
        + this.uniqueTraces + "," + this.nTRconsidered + "," + this.uniqueTracesAfterTR + ","
        + this.OriginalTraceLengths.average() + "," + this.OriginalTraceLengths.median() + ","
        + this.OriginalTraceLengths.max() + "," + this.TRReductionLength.average() + "," + this.TRReductionLength
        .median() + "," + this.TRReductionLength.max() + "," + this.TRReducedTraceLengths.average() + ","
        + this.TRReducedTraceLengths.median() + "," + this.TRReducedTraceLengths.max() + "," + this.nTRrepeatTpls
        .average() + "," + this.nTRrepeatTpls.average() + "," + this.nTRrepeatTpls.sum() + "," + this.nTRrepsList
        .average() + "," + this.nTRrepsList.max() + "," + this.nTRrepsList.sum() + "\n";
    System.out.println(headlines);
    System.out.println(result);
    FileWriter pw = null;
    File statF = new File(path + "log_stats.txt");
    if (!statF.exists()) {
      pw = new FileWriter(path + "log_stats.txt", true);
      pw.append(headlines);
    }
    if (pw == null) {
      pw = new FileWriter(path + "log_stats.txt", true);
    }
    pw.append(result);
    pw.close();

  }

  private void recordModelStatistics() throws Exception {
    String headlines = "Path,Model,#Places,#Transitions,#Arcs,Size,#Choices,#Parallel,#RGNodes,#RGArcs,RGSize,#SComponents,Avg. RGSize,Avg. Nodes, Avg. Arcs\n";
    String result =
        path + "," + model + "," + stats.places + "," + stats.transitions + "," + stats.arcs + "," + stats.size + ","
            + stats.choice + "," + stats.parallel + "," + stats.rg_nodes + "," + stats.rg_arcs + "," + stats.rg_size;
    if (stats.doDecomposition) {
      result +=
          ", " + stats.scompRGSize.size() + "," + stats.scompRGSize.average() + "," + stats.scompRGNodes.average() + ","
              + stats.scompRGArcs.average() + "\n";
    } else {
      result += ",1\n";
    }
    System.out.println(headlines);
    System.out.println(result);
    FileWriter pw = null;
    File statF = new File(path + "model_stats.txt");
    if (!statF.exists()) {
      pw = new FileWriter(path + "model_stats.txt", true);
      pw.append(headlines);
    }
    if (pw == null) {
      pw = new FileWriter(path + "model_stats.txt", true);
    }
    pw.append(result);
    pw.close();
  }
}
