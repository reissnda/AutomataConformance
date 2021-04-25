package org.apromore.alignment.web.service.filestore;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class StoredFile {

  private final String fileName;

  private final Long size;
}
