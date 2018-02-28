package org.openecomp.sdcrests.vendorsoftwareproducts.types;
import lombok.Data;

@Data
public class VspComputeDto {
  private String name;
  private String componentId;
  private String computeFlavorId;
}
