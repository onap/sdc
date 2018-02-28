package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VspRequestDto extends VspDescriptionDto {
  @NotNull
  private String onboardingMethod;

}
