package org.apromore.alignmentautomaton;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.Resources;
import org.apromore.alignmentautomaton.importer.ImportEventLog;
import org.apromore.alignmentautomaton.importer.ImportProcessModel;
import org.apromore.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.apromore.processmining.plugins.bpmn.plugins.BpmnImportPlugin;
import org.deckfour.xes.model.XLog;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

class HybridAlignmentGeneratorTest {

  private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
      .configure(SerializationFeature.INDENT_OUTPUT, true);

  @Test
  void computeAlignment12() throws Exception {
    runTestPNML("BPIC12.xes.gz", "BPIC12IM.pnml", "build/al-res12.json");
  }

  @Test
  @Disabled
  void computeAlignment17() throws Exception {
    runTestPNML("BPIC17F.xes.gz", "BPIC17F_IM.pnml", "build/al-res17.json");
  }

  @Test
  void computeAlignmentHospitalBillingBPMN() throws Exception {
    runTestBPNM("Hospital_Billing.xes.gz", "Hospital_Billing.bpmn", "build/hb.json");
  }

  @Test
  @Disabled
  void loan() throws Exception {
    runTestPNML("loan.xes.gz", "loan.bpmn", "build/loan.json");
  }

  @Test
  void simpleBPNM() throws Exception {
    runTestBPNM("simple.xes", "simple.bpmn", "build/simple.json");
  }

  private void runTestPNML(final String xesF, final String modelF, String output) throws Exception {
    File xes = new File(Resources.getResource("fixtures/" + xesF).getFile());
    File modelFile = new File(Resources.getResource("fixtures/" + modelF).getFile());

    XLog xLog = new ImportEventLog().importEventLog(xes);
    ImportProcessModel importProcessModel = new ImportProcessModel();
    Object[] pnetAndM =
        modelF.trim().toLowerCase().endsWith(".bpmn") ? importProcessModel.importPetrinetFromBPMN(modelFile)
            : importProcessModel.importPetriNetAndMarking(modelFile);
    Petrinet petrinet = (Petrinet) pnetAndM[0];
    Marking markings = (Marking) pnetAndM[1];

    AlignmentResult alignmentResult = new HybridAlignmentGenerator().computeAlignment(petrinet, markings, xLog);

    try (BufferedWriter w = new BufferedWriter(new FileWriter(output))) {
      mapper.writeValue(w, alignmentResult);
    }
  }

  private void runTestBPNM(final String xesF, final String modelF, String output) throws Exception {
    File xes = new File(Resources.getResource("fixtures/" + xesF).getFile());
    File modelFile = new File(Resources.getResource("fixtures/" + modelF).getFile());

    XLog xLog = new ImportEventLog().importEventLog(xes);

    BPMNDiagram bpmn = new BpmnImportPlugin().importFromStreamToDiagram(new FileInputStream(modelFile), modelF);
    AlignmentResult alignmentResult = new HybridAlignmentGenerator().computeAlignment(bpmn, xLog);

    try (BufferedWriter w = new BufferedWriter(new FileWriter(output))) {
      mapper.writeValue(w, alignmentResult);
    }
  }
}