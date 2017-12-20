package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import javax.validation.constraints.NotNull;

public class VspRequestDto extends VspDescriptionDto {
  @NotNull
  private String onboardingMethod;

  public String getOnboardingMethod() {
    return onboardingMethod;
  }

  public void setOnboardingMethod(String onboardingMethod) {
    this.onboardingMethod = onboardingMethod;
  }
}
