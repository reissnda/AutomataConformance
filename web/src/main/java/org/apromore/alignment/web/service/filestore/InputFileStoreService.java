package org.apromore.alignment.web.service.filestore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class InputFileStoreService {

  private Path baseDir;

  @PostConstruct
  public void init() throws IOException {
    baseDir = Files.createTempDirectory("aprom");
  }

  public void store(String fileName, byte[] fileBytes) throws IOException {
    Path p = resolve(fileName);
    delete(p);

    try (OutputStream w = new BufferedOutputStream(Files.newOutputStream(p))) {
      w.write(fileBytes);
    }
  }

  public void deleteFile(String fileName) throws IOException {
    delete(resolve(fileName));
  }

  private void delete(Path p) throws IOException {
    if (Files.exists(p)) {
      Files.delete(p);
    }
  }

  private Path resolve(String fileName) {
    return baseDir.resolve(fileName);
  }

  public List<StoredFile> listFiles() throws IOException {
    return Files.list(baseDir).map(p -> {
      try {
        return new StoredFile(p.getFileName().toString(), Files.size(p));
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }).collect(Collectors.toList());
  }

  public File retrieveFile(String fileName) {
    return retrieveFilePath(fileName).toFile();
  }

  private Path retrieveFilePath(String fileName) {
    Path resolve = resolve(fileName);

    if (!Files.exists(resolve)) {
      throw new IllegalArgumentException(String.format("File '%s' could not be find in storage", fileName));
    }
    return resolve;
  }

  public InputStream retrieveFileAsStream(String fileName) throws IOException {
    return new BufferedInputStream(Files.newInputStream(retrieveFilePath(fileName)));
  }

  public String retrieveFileAsString(String fileName) throws IOException {
    return Files.readString(retrieveFilePath(fileName));
  }
}