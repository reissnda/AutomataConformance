package org.apromore.alignmentautomaton.client;

import java.net.URI;
import java.util.UUID;
import org.apromore.alignmentautomaton.AlignmentGenerator;
import org.apromore.alignmentautomaton.AlignmentResult;
import org.apromore.alignmentautomaton.api.FileStoreResponse;
import org.apromore.alignmentautomaton.api.RESTEndpointsConfig;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.bpmn.Bpmn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class AlignmentClient implements AlignmentGenerator {

  private final RestTemplate restTemplate;

  private final String apiURI;

  @Autowired
  public AlignmentClient(RestTemplate restTemplate, String apiURI) {
    this.restTemplate = restTemplate;
    this.apiURI = apiURI;
  }

  @Override
  public AlignmentResult computeAlignment(Petrinet petriNet, Marking marking, XLog xLog) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AlignmentResult computeAlignment(Bpmn bpmn, XLog xLog) {

    String modelName = UUID.randomUUID().toString();
    String logName = UUID.randomUUID().toString();

    URI bpURI = UriComponentsBuilder.fromHttpUrl(apiURI + RESTEndpointsConfig.BPMN_UPLOAD_PATH)
        .queryParam("fileName", modelName).build().encode().toUri();
    URI lURI = UriComponentsBuilder.fromHttpUrl(apiURI + RESTEndpointsConfig.XES_UPLOAD_PATH)
        .queryParam("fileName", logName).build().encode().toUri();

    URI reqURI = UriComponentsBuilder.fromHttpUrl(apiURI + RESTEndpointsConfig.ALIGNMENT_PATH)
        .queryParam("xesFileName", logName).queryParam("modelFileName", modelName).build().encode().toUri();

    FileStoreResponse bpmnLoc = restTemplate.postForObject(bpURI, bpmn, FileStoreResponse.class);
    FileStoreResponse logLoc = restTemplate.postForObject(lURI, xLog, FileStoreResponse.class);
    return restTemplate.postForObject(reqURI, "", AlignmentResult.class);
  }
}
