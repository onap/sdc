package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import lombok.Data;

@Data
public class DeploymentFlavorListResponseDto {
  private String model;
  private String description;
  private String id;
}
