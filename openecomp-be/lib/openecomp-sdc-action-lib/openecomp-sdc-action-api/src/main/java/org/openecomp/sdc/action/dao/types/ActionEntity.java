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

package org.openecomp.sdc.action.dao.types;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.action.types.Action;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Table(keyspace = "dox", name = "Action")
public class ActionEntity {

  @Column(name = "actionUuId")
  private String actionUuId;
  @PartitionKey(value = 0)
  @Column(name = "actionInvariantUuId")
  private String actionInvariantUuId;
  @PartitionKey(value = 1)
  @Frozen
  @Column(name = "version")
  private Version version;
  @Column(name = "status")
  private String status;
  @Column(name = "name")
  private String name;
  @Column(name = "vendor_list")
  private List<String> vendorList;
  @Column(name = "category_list")
  private List<String> categoryList;
  @Column(name = "timestamp")
  private Date timestamp;
  @Column(name = "user")
  private String user;
  @Column(name = "supportedModels")
  private List<String> supportedModels;
  @Column(name = "supportedComponents")
  private List<String> supportedComponents;
  @Column(name = "data")
  private String data;

  public ActionEntity() {
  }

  public ActionEntity(String actionInvariantUuId, Version version) {
    this.actionInvariantUuId = actionInvariantUuId;
    this.version = version;
  }

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

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
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

  public List<String> getVendorList() {
    return vendorList;
  }

  /**
   * Sets vendor list.
   *
   * @param vendorList the vendor list
   */
  public void setVendorList(List<String> vendorList) {
    if (vendorList != null && !vendorList.isEmpty()) {
      List<String> lowerCaseVendorList = new ArrayList<String>();
      lowerCaseVendorList
          .addAll(vendorList.stream().map(String::toLowerCase).collect(Collectors.toList()));
      this.vendorList = lowerCaseVendorList;
    } else {
      this.vendorList = vendorList;
    }
  }

  public List<String> getCategoryList() {
    return categoryList;
  }

  /**
   * Sets category list.
   *
   * @param categoryList the category list
   */
  public void setCategoryList(List<String> categoryList) {
    if (categoryList != null && !categoryList.isEmpty()) {
      List<String> lowerCaseCategoryList = new ArrayList<String>();
      lowerCaseCategoryList
          .addAll(categoryList.stream().map(String::toLowerCase).collect(Collectors.toList()));
      this.categoryList = lowerCaseCategoryList;
    } else {
      this.categoryList = categoryList;
    }
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public List<String> getSupportedModels() {
    return supportedModels;
  }

  public void setSupportedModels(List<String> supportedModels) {
    this.supportedModels = supportedModels;
  }

  public List<String> getSupportedComponents() {
    return supportedComponents;
  }

  public void setSupportedComponents(List<String> supportedComponents) {
    this.supportedComponents = supportedComponents;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  /**
   * To dto action.
   *
   * @return the action
   */
  public Action toDto() {
    //Action destination = new Action();

    Action destination = JsonUtil.json2Object(this.getData(), Action.class);
    destination.setData(this.getData());
    destination.setTimestamp(this.getTimestamp());
    destination.setUser(this.getUser());
    destination.setData(this.getData());
    return destination;
  }

}
