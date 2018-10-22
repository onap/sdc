package org.openecomp.core.enrichment.types;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import lombok.Data;

@Data
public class ComponentProcessInfo {
  private String name;
  private byte[] content;

  public InputStream getContent() {
    return new ByteArrayInputStream(this.content);
  }


}
