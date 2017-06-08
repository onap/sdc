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

package org.openecomp.sdc.translator.datatypes.heattotosca.to;


import org.openecomp.sdc.heat.datatypes.manifest.FileData;


public class ResourceFileDataAndIDs {
  private String resourceId;
  private String translatedResourceId;
  private FileData fileData;

  public ResourceFileDataAndIDs() {
  }

  /**
   * Instantiates a new Resource file data and i ds.
   *
   * @param resourceId           the resource id
   * @param translatedResourceId the translated resource id
   * @param fileData             the file data
   */
  public ResourceFileDataAndIDs(String resourceId, String translatedResourceId, FileData fileData) {
    this.resourceId = resourceId;
    this.translatedResourceId = translatedResourceId;
    this.fileData = fileData;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getTranslatedResourceId() {
    return translatedResourceId;
  }

  public void setTranslatedResourceId(String translatedResourceId) {
    this.translatedResourceId = translatedResourceId;
  }

  public FileData getFileData() {
    return fileData;
  }

  public void setFileData(FileData fileData) {
    this.fileData = fileData;
  }
}
