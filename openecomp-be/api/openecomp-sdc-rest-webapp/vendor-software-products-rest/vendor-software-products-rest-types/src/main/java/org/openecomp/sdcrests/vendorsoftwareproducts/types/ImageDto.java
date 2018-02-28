package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import lombok.Data;

@Data
public class ImageDto extends ImageRequestDto implements CompositionDataEntityDto {
  private String id;
}
