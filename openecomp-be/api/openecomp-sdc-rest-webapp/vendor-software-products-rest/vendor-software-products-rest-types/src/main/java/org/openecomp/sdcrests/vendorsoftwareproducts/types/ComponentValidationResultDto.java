package org.openecomp.sdcrests.vendorsoftwareproducts.types;


import java.util.Set;

public class ComponentValidationResultDto {
  private boolean valid;
  private Set<CompositionEntityValidationDataDto> validationData;

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public Set<CompositionEntityValidationDataDto> getValidationData() {
    return validationData;
  }

  public void setValidationData(Set<CompositionEntityValidationDataDto> validationData) {
    this.validationData = validationData;
  }
}
