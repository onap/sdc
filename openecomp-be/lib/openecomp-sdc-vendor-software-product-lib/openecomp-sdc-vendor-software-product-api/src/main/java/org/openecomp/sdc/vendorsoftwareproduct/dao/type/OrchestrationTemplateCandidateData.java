/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.vendorsoftwareproduct.dao.type;

import java.nio.ByteBuffer;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;

public class OrchestrationTemplateCandidateData {
  private ByteBuffer contentData;
  private String filesDataStructure;
  private String fileSuffix;
  private String fileName;
  private String validationData;
  private ByteBuffer originalFileContentData;
  private String originalFileName;
  private String originalFileSuffix;

  public OrchestrationTemplateCandidateData() {
  }

  public OrchestrationTemplateCandidateData(ByteBuffer contentData, String dataStructureJson,
                                            String fileSuffix, String fileName,
                                            String originalFileName, String originalFileSuffix,
                                            ByteBuffer originalFileContentData) {
    this.contentData = contentData;
    this.filesDataStructure = dataStructureJson;
    this.fileSuffix = fileSuffix;
    this.fileName = fileName;
    this.originalFileName = originalFileName;
    this.originalFileSuffix = originalFileSuffix;
    this.originalFileContentData = originalFileContentData;
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

  public String getValidationData() {
    return validationData;
  }

  public void setValidationData(String validationData) {
    this.validationData = validationData;
  }

  public ValidationStructureList getValidationDataStructure() {
    return validationData == null ? null
        : JsonUtil.json2Object(validationData, ValidationStructureList.class);
  }

  public void setValidationDataStructure(ValidationStructureList validationData) {
    this.validationData = validationData == null ? null
        : JsonUtil.object2Json(validationData);
  }

  public ByteBuffer getOriginalFileContentData() {
    return originalFileContentData;
  }

  public void setOriginalFileContentData(ByteBuffer originalFileContentData) {
    this.originalFileContentData = originalFileContentData;
  }

  public String getOriginalFileName() {
    return originalFileName;
  }

  public void setOriginalFileName(String originalFileName) {
    this.originalFileName = originalFileName;
  }

  public String getOriginalFileSuffix() {
    return originalFileSuffix;
  }

  public void setOriginalFileSuffix(String originalFileSuffix) {
    this.originalFileSuffix = originalFileSuffix;
  }
}
