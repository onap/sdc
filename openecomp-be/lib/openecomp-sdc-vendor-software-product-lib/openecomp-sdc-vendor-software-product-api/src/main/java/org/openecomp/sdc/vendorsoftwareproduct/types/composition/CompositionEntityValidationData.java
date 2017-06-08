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

package org.openecomp.sdc.vendorsoftwareproduct.types.composition;

import java.util.ArrayList;
import java.util.Collection;

public class CompositionEntityValidationData {
  private CompositionEntityType entityType;
  private String entityId;
  private String entityName;
  private Collection<String> errors;
  private Collection<CompositionEntityValidationData> subEntitiesValidationData;

  public CompositionEntityValidationData(CompositionEntityType entityType, String entityId) {
    this.entityType = entityType;
    this.entityId = entityId;
  }

  public String getEntityName() {
    return entityName;
  }

  public void setEntityName(String entityName) {
    this.entityName = entityName;
  }

  public CompositionEntityType getEntityType() {
    return entityType;
  }

  public void setEntityType(CompositionEntityType entityType) {
    this.entityType = entityType;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public Collection<String> getErrors() {
    return errors;
  }

  public void setErrors(Collection<String> errors) {
    this.errors = errors;
  }

  public Collection<CompositionEntityValidationData> getSubEntitiesValidationData() {
    return subEntitiesValidationData;
  }

  public void setSubEntitiesValidationData(Collection<CompositionEntityValidationData> toSet) {
    this.subEntitiesValidationData = toSet;
  }

  /**
   * Add sub entity validation data.
   *
   * @param subEntityValidationData the sub entity validation data
   */
  public void addSubEntityValidationData(CompositionEntityValidationData subEntityValidationData) {
    if (subEntitiesValidationData == null) {
      subEntitiesValidationData = new ArrayList<>();
    }
    subEntitiesValidationData.add(subEntityValidationData);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CompositionEntityValidationData that = (CompositionEntityValidationData) o;

    if (entityType != that.entityType) {
      return false;
    }
    if (entityId != null ? !entityId.equals(that.entityId) : that.entityId != null) {
      return false;
    }
    if (entityName != null ? !entityName.equals(that.entityName) : that.entityName != null) {
      return false;
    }
    if (errors != null ? !errors.equals(that.errors) : that.errors != null) {
      return false;
    }
    if (subEntitiesValidationData != null ? !subEntitiesValidationData
        .equals(that.subEntitiesValidationData) : that.subEntitiesValidationData != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = entityType != null ? entityType.hashCode() : 0;
    result = 31 * result + (entityId != null ? entityId.hashCode() : 0);
    result = 31 * result + (entityName != null ? entityName.hashCode() : 0);
    result = 31 * result + (errors != null ? errors.hashCode() : 0);
    result =
        31 * result +
            (subEntitiesValidationData != null ? subEntitiesValidationData.hashCode() : 0);
    return result;
  }
}
