package org.apromore.alignmentautomaton;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import au.edu.qut.context.FakePluginContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.Resources;
import org.apromore.alignmentautomaton.importer.ImportEventLog;
import org.apromore.alignmentautomaton.importer.ImportProcessModel;
import org.deckfour.xes.model.XLog;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.pnml.exporting.PnmlExportNetToPNML;

class HybridAlignmentGeneratorTest {

  private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
      .configure(SerializationFeature.INDENT_OUTPUT, true);

  @Test
  void computeAlignment12() throws Exception {
    runTest("BPIC12.xes.gz", "BPIC12IM.pnml", "build/al-res12.json");
  }

  @Test
  @Disabled
  void computeAlignment17() throws Exception {
    runTest("BPIC17F.xes.gz", "BPIC17F_IM.pnml", "build/al-res17.json");
  }

  @Test
  @Disabled
  void computeAlignmentHospitalBilling() throws Exception {
    runTest("Hospital_Billing.xes.gz", "Hospital_Billing.bpmn", "build/hb.json");
  }

  @Test
  void simple() throws Exception {
    runTest("simple.xes", "simple.bpmn", "build/simple.json");
  }

  private void runTest(final String xesF, final String modelF, String output) throws Exception {
    File xes = new File(Resources.getResource("fixtures/" + xesF).getFile());
    File modelFile = new File(Resources.getResource("fixtures/" + modelF).getFile());

    XLog xLog = new ImportEventLog().importEventLog(xes);
    ImportProcessModel importProcessModel = new ImportProcessModel();
    Object[] pnetAndM =
        modelF.trim().toLowerCase().endsWith(".bpmn") ? importProcessModel.importPetrinetFromBPMN(modelFile)
            : importProcessModel.importPetriNetAndMarking(modelFile);
    Petrinet petrinet = (Petrinet) pnetAndM[0];

    new PnmlExportNetToPNML().exportPetriNetToPNMLFile(new FakePluginContext(), petrinet, new File("pnet.pnml"));

    System.out.println("PNET" + petrinet);

    Marking markings = (Marking) pnetAndM[1];

    AlignmentResult alignmentResult = new HybridAlignmentGenerator().computeAlignment(petrinet, markings, xLog);

    try (BufferedWriter w = new BufferedWriter(new FileWriter(output))) {
      mapper.writeValue(w, alignmentResult);
    }
  }
}