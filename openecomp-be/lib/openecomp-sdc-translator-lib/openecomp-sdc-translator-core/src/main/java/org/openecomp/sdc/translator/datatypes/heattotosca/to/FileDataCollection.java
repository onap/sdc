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

import java.util.ArrayList;
import java.util.Collection;

public class FileDataCollection {

  Collection<FileData> baseFiles;
  Collection<FileData> addOnFiles;
  Collection<FileData> nestedFiles;
  Collection<FileData> artifactFiles;

  public Collection<FileData> getBaseFile() {
    return baseFiles;
  }

  public void setBaseFile(Collection<FileData> baseFiles) {
    this.baseFiles = baseFiles;
  }

  public Collection<FileData> getAddOnFiles() {
    return addOnFiles;
  }

  public void setAddOnFiles(Collection<FileData> addOnFiles) {
    this.addOnFiles = addOnFiles;
  }

  public Collection<FileData> getNestedFiles() {
    return nestedFiles;
  }

  public void setNestedFiles(Collection<FileData> nestedFiles) {
    this.nestedFiles = nestedFiles;
  }

  public Collection<FileData> getBaseFiles() {
    return baseFiles;
  }

  public void setBaseFiles(Collection<FileData> baseFiles) {
    this.baseFiles = baseFiles;
  }

  public Collection<FileData> getArtifactFiles() {
    return artifactFiles;
  }

  public void setArtifactFiles(Collection<FileData> artifactFiles) {
    this.artifactFiles = artifactFiles;
  }

  /**
   * Add add on files.
   *
   * @param addonFile the addon file
   */
  public void addAddOnFiles(FileData addonFile) {
    if (this.addOnFiles == null) {
      this.addOnFiles = new ArrayList<>();
    }
    this.addOnFiles.add(addonFile);
  }

  /**
   * Add nested files.
   *
   * @param nestedFile the nested file
   */
  public void addNestedFiles(FileData nestedFile) {
    if (this.nestedFiles == null) {
      this.nestedFiles = new ArrayList<>();
    }
    this.nestedFiles.add(nestedFile);
  }

  /**
   * Add base files.
   *
   * @param baseFile the base file
   */
  public void addBaseFiles(FileData baseFile) {
    if (this.baseFiles == null) {
      this.baseFiles = new ArrayList<>();
    }
    this.baseFiles.add(baseFile);
  }

  /**
   * Add artifact files.
   *
   * @param artifactFile the artifact file
   */
  public void addArtifactFiles(FileData artifactFile) {
    if (this.artifactFiles == null) {
      this.artifactFiles = new ArrayList<>();
    }
    this.artifactFiles.add(artifactFile);
  }
}
