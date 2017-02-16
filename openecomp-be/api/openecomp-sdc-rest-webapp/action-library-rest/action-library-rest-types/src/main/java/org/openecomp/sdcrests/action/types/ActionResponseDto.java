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

package org.openecomp.sdcrests.action.types;


import java.util.HashMap;
import java.util.List;

/**
 * Defines DTO used for Action Response.
 */
public class ActionResponseDto {

  String actionUuId;
  String actionInvariantUuId;
  String name;
  String displayName;
  String version;
  String description;
  String status;
  String timestamp;
  String updatedBy;
  List<String> vendorList;
  List<String> categoryList;
  List<HashMap<String, String>> supportedModels;
  List<HashMap<String, String>> supportedComponents;

  public String getActionUuId() {
    return actionUuId;
  }

  public void setActionUuId(String actionUuId) {
    this.actionUuId = actionUuId;
  }

  public String getActionInvariantUuId() {
    return actionInvariantUuId;
  }

  public void setActionInvariantUuId(String actionInvariantUuId) {
    this.actionInvariantUuId = actionInvariantUuId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getCategoryList() {
    return categoryList;
  }

  public void setCategoryList(List<String> categoryList) {
    this.categoryList = categoryList;
  }

  public List<String> getVendorList() {
    return vendorList;
  }

  public void setVendorList(List<String> vendorList) {
    this.vendorList = vendorList;
  }

  public List<HashMap<String, String>> getSupportedComponents() {
    return supportedComponents;
  }

  public void setSupportedComponents(List<HashMap<String, String>> supportedComponents) {
    this.supportedComponents = supportedComponents;
  }

  public List<HashMap<String, String>> getSupportedModels() {
    return supportedModels;
  }

  public void setSupportedModels(List<HashMap<String, String>> supportedModels) {
    this.supportedModels = supportedModels;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timeStamp) {
    this.timestamp = timeStamp;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }
}
