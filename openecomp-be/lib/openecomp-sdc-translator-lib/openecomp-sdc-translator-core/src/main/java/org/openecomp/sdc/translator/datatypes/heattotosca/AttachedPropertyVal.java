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

public class AttachedPropertyVal {
  private Object propertyValue;
  private ReferenceType referenceType;

  /**
   * Instantiates a new Attached resource id.
   *
   * @param propertyValue the property value
   * @param referenceType the reference type
   */
  public AttachedPropertyVal(Object propertyValue, ReferenceType referenceType) {
    this.referenceType = referenceType;
    this.propertyValue = propertyValue;
  }

  public Object getPropertyValue() {
    return propertyValue;
  }

  public ReferenceType getReferenceType() {
    return referenceType;
  }

  public boolean isGetResource() {
    return referenceType == ReferenceType.GET_RESOURCE;
  }

  public boolean isGetParam() {
    return referenceType == ReferenceType.GET_PARAM;
  }

  public boolean isGetAttr() {
    return referenceType == ReferenceType.GET_ATTR;
  }
}
