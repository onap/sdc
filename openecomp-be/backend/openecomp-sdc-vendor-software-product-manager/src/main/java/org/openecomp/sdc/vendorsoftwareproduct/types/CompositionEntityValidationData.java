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

import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The type Composition entity validation data.
 */
public class CompositionEntityValidationData {
  private CompositionEntityType entityType;
  private String entityId;
  private Collection<String> errors;
  private Collection<CompositionEntityValidationData> subEntitiesValidationData;

  /**
   * Instantiates a new Composition entity validation data.
   *
   * @param entityType the entity type
   * @param entityId   the entity id
   */
  public CompositionEntityValidationData(CompositionEntityType entityType, String entityId) {
    this.entityType = entityType;
    this.entityId = entityId;
  }

  /**
   * Gets entity type.
   *
   * @return the entity type
   */
  public CompositionEntityType getEntityType() {
    return entityType;
  }

  /**
   * Sets entity type.
   *
   * @param entityType the entity type
   */
  public void setEntityType(CompositionEntityType entityType) {
    this.entityType = entityType;
  }

  /**
   * Gets entity id.
   *
   * @return the entity id
   */
  public String getEntityId() {
    return entityId;
  }

  /**
   * Sets entity id.
   *
   * @param entityId the entity id
   */
  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  /**
   * Gets errors.
   *
   * @return the errors
   */
  public Collection<String> getErrors() {
    return errors;
  }

  /**
   * Sets errors.
   *
   * @param errors the errors
   */
  public void setErrors(Collection<String> errors) {
    this.errors = errors;
  }

  /**
   * Gets sub entities validation data.
   *
   * @return the sub entities validation data
   */
  public Collection<CompositionEntityValidationData> getSubEntitiesValidationData() {
    return subEntitiesValidationData;
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
}
