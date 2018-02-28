package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import org.hibernate.validator.constraints.NotBlank;
import lombok.Data;

@Data
public class ImageRequestDto implements CompositionDataEntityDto {

  @NotBlank(message = "is mandatory and should not be empty")
  private String fileName;
  private String description;
  /*private String version;
  private String format;
  private String md5;
  //private String providedBy;*/


}
