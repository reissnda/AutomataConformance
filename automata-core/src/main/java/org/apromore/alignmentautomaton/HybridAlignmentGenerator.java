package org.apromore.alignmentautomaton;

import java.io.IOException;
import org.apromore.alignmentautomaton.ScalableConformanceChecker.HybridConformanceChecker;
import org.apromore.alignmentautomaton.automaton.Automaton;
import org.apromore.alignmentautomaton.importer.DecomposingTRImporter;
import org.apromore.alignmentautomaton.importer.ImportEventLog;
import org.apromore.alignmentautomaton.importer.ImportProcessModel;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.bpmn.Bpmn;

public class HybridAlignmentGenerator implements AlignmentGenerator {

  private static final int NUM_THREADS = 4;

  public AlignmentResult computeAlignment(Petrinet petrinet, Marking marking, XLog xLog) {
    try {
      DecomposingTRImporter importer = new DecomposingTRImporter();
      importer.importAndDecomposeModelAndLogForConformanceChecking(petrinet, marking, xLog);
      HybridConformanceChecker checker = new HybridConformanceChecker(importer, NUM_THREADS);
      return new AlignmentResult(checker.getAlignments(), importer.caseIDs);
    } catch (ConnectionCannotBeObtained | IOException ex) {
      throw new AlignmentGenerationException("Internal error generating alignment, cause: " + ex.getMessage(), ex);
    }
  }

  @Override
  public AlignmentResult computeAlignment(Bpmn bpmn, XLog xLog) {
    try {

      DecomposingTRImporter importer = new DecomposingTRImporter();

      ImportEventLog importEventLog = new ImportEventLog();
      ImportProcessModel importProcessModel = new ImportProcessModel();
      Automaton logAutomaton = importEventLog.createDAFSAfromLog(xLog);
      Automaton modelAutomaton = importProcessModel
          .createFSMFromBPNM(bpmn, logAutomaton.eventLabels(), logAutomaton.inverseEventLabels());



    } catch (IOException ex) {
      throw new AlignmentGenerationException("Internal error generating alignment, cause: " + ex.getMessage(), ex);
    }

    return null;
  }
}
