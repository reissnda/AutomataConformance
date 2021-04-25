package org.apromore.alignmentautomaton;

import java.io.IOException;
import org.apromore.alignmentautomaton.ScalableConformanceChecker.HybridConformanceChecker;
import org.apromore.alignmentautomaton.importer.DecomposingTRImporter;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

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
}
