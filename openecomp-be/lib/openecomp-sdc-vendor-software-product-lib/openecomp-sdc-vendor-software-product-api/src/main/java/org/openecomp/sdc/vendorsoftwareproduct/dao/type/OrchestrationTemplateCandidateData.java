package org.openecomp.sdc.vendorsoftwareproduct.dao.type;

import java.nio.ByteBuffer;

public class OrchestrationTemplateCandidateData {
  private ByteBuffer contentData;
  private String filesDataStructure;
  private String fileSuffix;
  private String fileName;

  public OrchestrationTemplateCandidateData() {
  }

  public OrchestrationTemplateCandidateData(ByteBuffer contentData, String dataStructureJson,
                                            String fileSuffix, String fileName) {
    this.contentData = contentData;
    this.filesDataStructure = dataStructureJson;
    this.fileSuffix = fileSuffix;
    this.fileName = fileName;
  }

  public ByteBuffer getContentData() {
    return contentData;
  }

  public void setContentData(ByteBuffer contentData) {
    this.contentData = contentData;
  }

  public String getFilesDataStructure() {
    return filesDataStructure;
  }

  public void setFilesDataStructure(String filesDataStructure) {
    this.filesDataStructure = filesDataStructure;
  }

  public String getFileSuffix() {
    return fileSuffix;
  }

  public void setFileSuffix(String fileSuffix) {
    this.fileSuffix = fileSuffix;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
}
