/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.vendorsoftwareproduct.dao.type;

import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;

import java.nio.ByteBuffer;


public class UploadData {

  private String id;

  private String packageName;

  private String packageVersion;

  private String validationData;

  private ByteBuffer contentData;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getPackageVersion() {
    return packageVersion;
  }

  public void setPackageVersion(String packageVersion) {
    this.packageVersion = packageVersion;
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

  public ByteBuffer getContentData() {
    return contentData;
  }

  public void setContentData(ByteBuffer contentData) {
    this.contentData = contentData;
  }
}
