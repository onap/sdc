package org.openecomp.core.enrichment.types;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ComponentProcessInfo {
  private String name;
  private byte[] content;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public InputStream getContent() {
    return new ByteArrayInputStream(this.content);
  }

  public void setContent(byte[] content) {
    this.content = content;
  }

}
