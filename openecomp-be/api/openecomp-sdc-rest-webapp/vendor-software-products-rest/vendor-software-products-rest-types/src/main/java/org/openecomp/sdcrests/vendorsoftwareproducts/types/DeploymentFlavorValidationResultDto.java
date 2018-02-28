package org.openecomp.sdcrests.vendorsoftwareproducts.types;


import java.util.Set;
import lombok.Data;

@Data
public class DeploymentFlavorValidationResultDto {
  private boolean valid;
  private Set<CompositionEntityValidationDataDto> validationData;
}
