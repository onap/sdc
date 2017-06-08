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

package org.openecomp.sdc.vendorsoftwareproduct.types;

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CandidateDataEntityTo {
  private final String vspId;
  private final String user;
//  private final UploadFileResponse uploadFileResponse;
  private Map<String, List<ErrorMessage>> errors = new HashMap<>();
  private final byte[] uploadedFileData;
  private final HeatStructureTree tree;
  private final Version activeVersion;
  private final FileContentHandler contentMap;

  /**
   * Instantiates a new Candidate data entity to.
   *  @param vspId              the vsp id
   * @param user               the user
   * @param uploadedFileData   the uploaded file data
   * @param tree               the tree
   * @param contentMap         the content map
   * @param activeVersion      the active version
   */
  public CandidateDataEntityTo(String vspId, String user,
                               byte[] uploadedFileData, HeatStructureTree tree,
                               FileContentHandler contentMap, Version activeVersion) {
    this.vspId = vspId;
    this.user = user;
    this.errors = errors;
    this.uploadedFileData = uploadedFileData;
    this.tree = tree;
    this.activeVersion = activeVersion;
    this.contentMap = contentMap;
  }

  public String getVspId() {
    return vspId;
  }

  public String getUser() {
    return user;
  }

  public byte[] getUploadedFileData() {
    return uploadedFileData;
  }

  public HeatStructureTree getTree() {
    return tree;
  }

  public Version getActiveVersion() {
    return activeVersion;
  }

  public FileContentHandler getContentMap() {
    return contentMap;
  }

  public Map<String, List<ErrorMessage>> getErrors() {return errors;}

  public void setErrors(Map<String, List<ErrorMessage>> errors) {this.errors = errors;}
}
