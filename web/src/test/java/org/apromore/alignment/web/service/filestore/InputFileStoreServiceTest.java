package org.apromore.alignment.web.service.filestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InputFileStoreServiceTest {

  private static final String S1 = UUID.randomUUID().toString();

  private InputFileStoreService service;

  @BeforeEach
  void setUp() throws IOException {
    service = new InputFileStoreService();
    service.init();
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void storeRetrieve() throws Exception {

    String fileName = "s1.xes";
    service.store(fileName, S1.getBytes(StandardCharsets.UTF_8));

    String s = service.retrieveFileAsString(fileName);

    assertEquals(S1, s);
  }

  @Test
  void storeRetrieveStream() throws Exception {

    String fileName = "s1.xes";
    service.store(fileName, S1.getBytes(StandardCharsets.UTF_8));

    InputStream inputStream = service.retrieveFileAsStream(fileName);

    String s = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
        .collect(Collectors.joining(System.lineSeparator()));
    assertEquals(S1, s);
  }

  @Test
  void storeDoesNotExist() throws Exception {

    String fileName = "s1.xes";
    service.store(fileName, S1.getBytes(StandardCharsets.UTF_8));

    assertThrows(IllegalArgumentException.class, () -> service.retrieveFileAsString("doesnotexist"));
  }
}