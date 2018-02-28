package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ComputeDetailsDto implements CompositionDataEntityDto {
  @NotBlank(message = "is mandatory and should not be empty")
  @Size(min = 0, max = 30, message = "length should not exceed 30 characters.")
  private String name;
  @Size(min = 0, max = 300, message = "length should not exceed 300 characters.")
  private String description;

  public ComputeDetailsDto() {
  }
}
