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

package org.openecomp.sdc.action.types;

import org.openecomp.sdc.action.dao.types.EcompComponentEntity;

public class EcompComponent {

  private String id;
  private String name;

  public EcompComponent() {
    //Default constructor
  }

  public EcompComponent(String name, String id) {
    this.name = name;
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * To entity ecomp component entity.
   *
   * @return the ecomp component entity
   */
  public EcompComponentEntity toEntity() {
    EcompComponentEntity destination = new EcompComponentEntity();
    destination.setId(this.getId());
    destination.setName(this.getName());
    return destination;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null) {
      return false;
    }
    if (this.getClass() != object.getClass()) {
      return false;
    }
    EcompComponent obj = (EcompComponent) object;
    if (id == null) {
      if (obj.id != null) {
        return false;
      }
    } else if (!id.equals(obj.id)) {
      return false;
    }
    if (name == null) {
      if (obj.name != null) {
        return false;
      }
    } else if (!name.equals(obj.name)) {
      return false;
    }
    return true;
  }
}
