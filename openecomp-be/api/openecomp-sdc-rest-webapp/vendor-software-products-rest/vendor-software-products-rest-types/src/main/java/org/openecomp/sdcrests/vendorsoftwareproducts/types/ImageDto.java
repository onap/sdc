package org.openecomp.sdcrests.vendorsoftwareproducts.types;


public class ImageDto extends ImageRequestDto implements CompositionDataEntityDto {
  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
