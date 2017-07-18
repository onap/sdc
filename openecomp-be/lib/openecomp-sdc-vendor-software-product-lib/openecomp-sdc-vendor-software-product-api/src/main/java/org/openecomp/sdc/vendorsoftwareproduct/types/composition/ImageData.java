package org.openecomp.sdc.vendorsoftwareproduct.types.composition;

public class ImageData implements CompositionDataEntity {

  private String fileName;
  private String description;

  public ImageData(){

  }

  public ImageData(String fileName, String description) {
    this.fileName = fileName;
    this.description = description;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
