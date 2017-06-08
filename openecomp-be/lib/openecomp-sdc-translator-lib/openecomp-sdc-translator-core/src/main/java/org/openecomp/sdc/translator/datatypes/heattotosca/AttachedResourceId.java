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

package org.openecomp.sdc.translator.datatypes.heattotosca;

public class AttachedResourceId {
  private Object translatedId;
  private Object entityId;
  private ReferenceType resourceReferenceType;

  /**
   * Instantiates a new Attached resource id.
   *
   * @param translatedId          the translated id
   * @param entityId              the entity id
   * @param resourceReferenceType the resource reference type
   */
  public AttachedResourceId(Object translatedId, Object entityId,
                            ReferenceType resourceReferenceType) {
    this.translatedId = translatedId;
    this.resourceReferenceType = resourceReferenceType;
    this.entityId = entityId;
  }

  public Object getEntityId() {
    return entityId;
  }

  public Object getTranslatedId() {
    return translatedId;
  }

  public ReferenceType getResourceReferenceType() {
    return resourceReferenceType;
  }

  public boolean isGetResource() {
    return resourceReferenceType == ReferenceType.GET_RESOURCE;
  }

  public boolean isGetParam() {
    return resourceReferenceType == ReferenceType.GET_PARAM;
  }

  public boolean isGetAttr() {
    return resourceReferenceType == ReferenceType.GET_ATTR;
  }
}
