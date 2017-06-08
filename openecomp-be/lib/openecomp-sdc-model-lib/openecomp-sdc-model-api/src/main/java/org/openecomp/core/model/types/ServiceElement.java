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

package org.openecomp.core.model.types;

import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class ServiceElement {

  private String vspId;
  private Version version;
  private String name;
  private byte[] contentData;

  public ServiceElement() {
  }

  public String getVspId() {
    return vspId;
  }

  public void setVspId(String vspId) {
    this.vspId = vspId;
  }

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets content.
   *
   * @return the content
   */
  public InputStream getContent() {
    if (this.contentData == null) {
      return null;
    }
    return new ByteArrayInputStream(contentData);
  }

  public void setContentData(byte[] contentData) {
    this.contentData = contentData;
  }
}
