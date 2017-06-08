package org.openecomp.sdc.vendorsoftwareproduct.dao.type;

import java.nio.ByteBuffer;

public class OrchestrationTemplateCandidateData {
  private ByteBuffer contentData;
  private String filesDataStructure;


  public OrchestrationTemplateCandidateData() {
  }

  public OrchestrationTemplateCandidateData(ByteBuffer contentData,
                                            String dataStructureJson) {
    this.contentData = contentData;
    this.filesDataStructure = dataStructureJson;
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
}
