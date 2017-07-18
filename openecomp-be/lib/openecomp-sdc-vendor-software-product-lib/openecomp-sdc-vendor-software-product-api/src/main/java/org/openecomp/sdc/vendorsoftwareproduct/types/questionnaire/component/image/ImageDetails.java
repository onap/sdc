package org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.image;


public class ImageDetails {

  private String version;
  private String format;
  private String md5;

  public String getMd5() {
    return md5;
  }

  public void setMd5(String md5) {
    this.md5 = md5;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
