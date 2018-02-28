package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import lombok.Data;
@Data
public class ComputeDescription {
  private String name;
  private String description;

  public ComputeDescription() {

  }

  public ComputeDescription(String name, String description) {
    this.name = name;
    this.description = description;
  }
}
