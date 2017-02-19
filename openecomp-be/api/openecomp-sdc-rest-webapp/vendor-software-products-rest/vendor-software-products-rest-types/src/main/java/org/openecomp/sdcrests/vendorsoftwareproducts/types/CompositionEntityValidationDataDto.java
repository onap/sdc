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

package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;

import java.util.Collection;

public class CompositionEntityValidationDataDto {
  private CompositionEntityType entityType;
  private String entityId;
  private Collection<String> errors;
  private Collection<CompositionEntityValidationDataDto> subEntitiesValidationData;

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

  public Collection<CompositionEntityValidationDataDto> getSubEntitiesValidationData() {
    return subEntitiesValidationData;
  }

  public void setSubEntitiesValidationData(
      Collection<CompositionEntityValidationDataDto> subEntitiesValidationData) {
    this.subEntitiesValidationData = subEntitiesValidationData;
  }
}
