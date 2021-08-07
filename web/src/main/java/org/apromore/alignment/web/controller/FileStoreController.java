package org.apromore.alignment.web.controller;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import com.google.common.io.BaseEncoding;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apromore.alignment.web.service.filestore.InputFileStoreService;
import org.apromore.alignment.web.service.filestore.StoredFile;
import org.apromore.alignmentautomaton.api.FileStoreResponse;
import org.apromore.alignmentautomaton.api.RESTEndpointsConfig;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileStoreController {

  private final InputFileStoreService service;

  @PostMapping(RESTEndpointsConfig.INPUT_FILE_UPLOAD_PATH)
  @ApiOperation(value = "Uploads files to be used as input for alignment generations", notes = "Stores any file in the server local filesystem")
  public void addFile(@RequestParam("fileName") String fileName, @RequestParam("contents") MultipartFile contents)
      throws IOException {
    service.store(fileName, contents.getBytes());
  }

  @GetMapping(RESTEndpointsConfig.INPUT_FILE_UPLOAD_PATH)
  @ApiOperation(value = "Lists stored files", notes = "Lists all stored files")
  public List<StoredFile> getAllFileNames() throws Exception {
    return service.listFiles();
  }

  @PostMapping(value = RESTEndpointsConfig.XES_UPLOAD_PATH, consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Uploads files to be used as input for alignment generations", notes = "Stores XES content as a file into the server local filesystem")
  public @ResponseBody
  FileStoreResponse addXES(@RequestParam(value = "fileName", required = false) String fileName, @RequestBody String xes)
      throws IOException {
    // TODO parse and validate XES
    return storeF(fileName, xes, "xes");
  }

  @PostMapping(value = RESTEndpointsConfig.BPMN_UPLOAD_PATH, consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Uploads files to be used as input for alignment generations", notes = "Stores BPMN content as a file into the server local filesystem")
  public @ResponseBody
  FileStoreResponse addBPMN(@RequestParam(value = "fileName", required = false) String fileName,
      @RequestBody String bpmn) throws IOException {
    // TODO parse and validate BPMN
    return storeF(fileName, bpmn, "bpmn");
  }

  private FileStoreResponse storeF(@RequestParam(value = "fileName", required = false) String fileName,
      @RequestBody String request, String extension) throws IOException {
    String name = StringUtils.hasText(fileName) ? fileName.trim() : (randomFileName() + '.' + extension);
    log.info("Storing {}", name);
    service.store(name, request.getBytes(StandardCharsets.UTF_8));
    return new FileStoreResponse(name);
  }

  public static String randomFileName() {
    UUID uu = UUID.randomUUID();
    ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb.putLong(uu.getMostSignificantBits());
    bb.putLong(uu.getLeastSignificantBits());
    return StringUtils.trimTrailingCharacter(BaseEncoding.base64Url().encode(bb.array()), '=');
  }
}
