package org.openecomp.sdcrests.vendorsoftwareproducts.types;
import lombok.Data;

@Data
public class ComputeDto {
  private String name;
  private String id;
  private String description;
  private boolean associatedToDeploymentFlavor;
}
