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

package org.openecomp.sdc.generator.data;

public class Artifact {

  String name;
  String type;
  String groupType;
  String description;
  String label;
  String version;
  String checksum;
  byte[] payload;

  /**
   * Instantiates a new Artifact.
   *
   * @param type      the type
   * @param groupType the group type
   * @param checksum  the checksum
   * @param payload   the payload
   */
  public Artifact(String type, String groupType, String checksum, byte[] payload) {
    this.type = type;
    this.groupType = groupType;
    this.checksum = checksum;
    this.payload = payload;
  }

  public byte[] getPayload() {
    return payload;
  }

  public String getChecksum() {
    return checksum;
  }

  public String getType() {
    return type;
  }

  public String getGroupType() {
    return groupType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
