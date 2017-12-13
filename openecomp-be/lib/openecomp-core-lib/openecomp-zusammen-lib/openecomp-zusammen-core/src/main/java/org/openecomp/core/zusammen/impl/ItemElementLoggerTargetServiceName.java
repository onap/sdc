/*
* Copyright © 2016-2017 European Support Limited
*
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
*/

package org.openecomp.core.zusammen.impl;

public enum ItemElementLoggerTargetServiceName {
  ITEM_CREATION("Item Creation"),
  ITEM_VERSION_CREATION("Item Version Creation"),
  ELEMENT_CREATION("Element Creation"),
  ELEMENT_UPDATE("Element Update"),
  ELEMENT_LIST("Element List"),
  ELEMENT_GET("Element Get"),
  ELEMENT_GET_BY_PROPERTY("Element Get By Property"),
  ITEM_RETRIEVAL("Item retrieval"),
  ITEM_VERSION_RETRIEVAL("Item version retrieval)");

  private final String description;

  ItemElementLoggerTargetServiceName(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
