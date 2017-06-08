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

package org.openecomp.sdc.heat.datatypes.structure;


import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;

import java.util.ArrayList;
import java.util.List;

public class Artifact implements Comparable<Artifact> {

  private String fileName;
  private FileData.Type type;
  private List<ErrorMessage> errors;

  public Artifact(String fileName, FileData.Type type) {
    this.fileName = fileName;
    this.type = type;
  }


  public String getFileName() {
    return fileName;
  }

  public void setFileName(String name) {
    this.fileName = name;
  }


  public FileData.Type getType() {
    return type;
  }

  public List<ErrorMessage> getErrors() {
    return errors;
  }

  public void setErrors(List<ErrorMessage> errors) {
    this.errors = errors;
  }

  /**
   * Add error to error list.
   *
   * @param error the error
   */
  public void addErrorToErrorList(ErrorMessage error) {
    if (this.errors == null || this.errors.isEmpty()) {
      this.errors = new ArrayList<>();
    }

    this.errors.add(error);
  }

  @Override
  public int hashCode() {
    int result = fileName.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Artifact artifact = (Artifact) obj;

    if (!fileName.equals(artifact.fileName)) {
      return false;
    }
    return true;

  }

  @Override
  public int compareTo(Artifact artifact) {
    return artifact.getFileName().compareTo(this.getFileName());
  }
}
