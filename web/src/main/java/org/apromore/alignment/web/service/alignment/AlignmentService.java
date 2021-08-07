package org.apromore.alignment.web.service.alignment;

import java.io.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apromore.alignment.web.service.filestore.InputFileStoreService;
import org.apromore.alignmentautomaton.AlignmentResult;
import org.apromore.alignmentautomaton.HybridAlignmentGenerator;
import org.apromore.alignmentautomaton.importer.ImportEventLog;
import org.apromore.alignmentautomaton.importer.ImportProcessModel;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlignmentService {

  private final InputFileStoreService fileStoreService;

  public AlignmentResult runAlignment(String xesFileName, String modelFileName) {

    HybridAlignmentGenerator hybridAlignmentGenerator = new HybridAlignmentGenerator();

    File xes = fileStoreService.retrieveFile(xesFileName);
    File model = fileStoreService.retrieveFile(modelFileName);

    log.info("Generating alignment with files {}, {}", xes.getAbsolutePath(), model.getAbsolutePath());

    Object[] pnetAndM;
    try {
      pnetAndM = new ImportProcessModel().importPetriNetAndMarking(model);
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
    Petrinet petrinet = (Petrinet) pnetAndM[0];
    Marking markings = (Marking) pnetAndM[1];

    log.debug("Imported model");

    XLog xLog;
    try {
      xLog = new ImportEventLog().importEventLog(xes);
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalStateException(e);
    }

    log.debug("Imported log");

    return hybridAlignmentGenerator.computeAlignment(petrinet, markings, xLog);
  }
}