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

package org.openecomp.sdc.heat.datatypes.model;

import java.util.Map;

public class Resource {
  String type;
  Map<String, Object> properties;
  Object metadata;
  Object depends_on;
  Object update_policy;
  Object deletion_policy;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  public Object getMetadata() {
    return metadata;
  }

  public void setMetadata(Object metadata) {
    this.metadata = metadata;
  }

  public Object getDepends_on() {
    return depends_on;
  }

  public void setDepends_on(Object depends_on) {
    this.depends_on = depends_on;
  }

  public Object getUpdate_policy() {
    return update_policy;
  }

  public void setUpdate_policy(Object update_policy) {
    this.update_policy = update_policy;
  }

  public Object getDeletion_policy() {
    return deletion_policy;
  }

  public void setDeletion_policy(Object deletion_policy) {
    this.deletion_policy = deletion_policy;
  }

  @Override
  public String toString() {
    return "Resource{"
        + "type='" + type + '\''
        + ", properties=" + properties
        + ", metadata=" + metadata
        + ", depends_on=" + depends_on
        + ", update_policy='" + update_policy + '\''
        + ", deletion_policy='" + deletion_policy + '\''
        + '}';
  }
}
