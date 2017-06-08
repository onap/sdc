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

package org.openecomp.sdc.versioning.types;

import java.util.ArrayList;
import java.util.List;

public class VersionableEntityMetadata {

  private VersionableEntityStoreType storeType;
  private String name;
  private String identifierName;
  private String versionIdentifierName;
  private List<UniqueValueMetadata> uniqueValuesMetadata = new ArrayList<>();

  public VersionableEntityMetadata(String name, String identifierName,
                                   String versionIdentifierName) {
    this(VersionableEntityStoreType.Cassandra, name, identifierName, versionIdentifierName);
  }

  public VersionableEntityMetadata(VersionableEntityStoreType storeType, String name,
                                   String identifierName, String versionIdentifierName) {
    this.storeType = storeType;
    this.name = name;
    this.identifierName = identifierName;
    this.versionIdentifierName = versionIdentifierName;
  }

  public VersionableEntityStoreType getStoreType() {
    return storeType;
  }

  public void setStoreType(VersionableEntityStoreType storeType) {
    this.storeType = storeType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIdentifierName() {
    return identifierName;
  }

  public void setIdentifierName(String idColumnName) {
    this.identifierName = idColumnName;
  }

  public String getVersionIdentifierName() {
    return versionIdentifierName;
  }

  public void setVersionIdentifierName(String versionColumnName) {
    this.versionIdentifierName = versionColumnName;
  }

  public List<UniqueValueMetadata> getUniqueValuesMetadata() {
    return uniqueValuesMetadata;
  }

  public void setUniqueValuesMetadata(List<UniqueValueMetadata> uniqueValuesMetadata) {
    this.uniqueValuesMetadata = uniqueValuesMetadata;
  }
}
