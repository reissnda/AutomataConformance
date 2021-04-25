package org.apromore.alignment.web.controller;

import java.io.IOException;
import java.util.List;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apromore.alignment.web.config.RESTEndpointsConfig;
import org.apromore.alignment.web.service.filestore.InputFileStoreService;
import org.apromore.alignment.web.service.filestore.StoredFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileStoreController {

  private final InputFileStoreService service;

  @PostMapping(RESTEndpointsConfig.INPUT_FILE_UPLOAD_PATH)
  @ApiOperation(value = "Uploads files to be used as input for alignment generations", notes = "Stores any file in the server local filesystemn")
  public void addFile(@RequestParam("fileName") String fileName, @RequestParam("contents") MultipartFile contents)
      throws IOException {
    service.store(fileName, contents.getBytes());
  }

  @GetMapping(RESTEndpointsConfig.INPUT_FILE_UPLOAD_PATH)
  @ApiOperation(value = "Lists stored files", notes = "Lists all stored files")
  public List<StoredFile> getAllFileNames() throws Exception {
    return service.listFiles();
  }
}
