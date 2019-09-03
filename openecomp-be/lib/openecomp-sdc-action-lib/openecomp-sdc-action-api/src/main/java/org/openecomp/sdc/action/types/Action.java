/*
* Copyright © 2016-2018 European Support Limited
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

package org.openecomp.sdc.action.types;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.openecomp.sdc.action.ActionConstants;
import org.openecomp.sdc.action.dao.types.ActionEntity;
import org.openecomp.sdc.versioning.dao.types.Version;


public class Action implements Comparable {
  private String actionUuId;
  private String actionInvariantUuId;
  private String version;
  private ActionStatus status;
  private String name;
  private String displayName;
  private List<String> vendorList;
  private List<String> categoryList;
  private Date timestamp;
  private String user;
  private List<Map<String, String>> supportedModels;
  private List<Map<String, String>> supportedComponents;
  //private List<HashMap<String,String>> artifacts;
  private List<ActionArtifact> artifacts;
  private String data;

  public Action() {
  }

  /**
   * Instantiates a new Action.
   *
   * @param action the action
   */
  public Action(Action action) {
    this.actionUuId = action.getActionUuId();
    this.actionInvariantUuId = action.getActionInvariantUuId();
    this.name = action.getName();
    this.setDisplayName(action.getDisplayName());
    this.setVendorList(action.getVendorList());
    this.setCategoryList(action.getCategoryList());
    this.setTimestamp(action.getTimestamp());
    this.setUser(action.getUser());
    this.version = action.getVersion();
    this.status = action.getStatus();
    this.data = action.getData();
    this.supportedComponents = action.getSupportedComponents();
    this.supportedModels = action.getSupportedModels();
    this.artifacts = action.getArtifacts();
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

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public ActionStatus getStatus() {
    return status;
  }

  public void setStatus(ActionStatus status) {
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

  public List<String> getVendorList() {
    return vendorList;
  }

  public void setVendorList(List<String> vendorList) {
    this.vendorList = vendorList;
  }

  public List<String> getCategoryList() {
    return categoryList;
  }

  public void setCategoryList(List<String> categoryList) {
    this.categoryList = categoryList;
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

  public List<Map<String, String>> getSupportedModels() {
    return supportedModels;
  }

  public void setSupportedModels(List<Map<String, String>> supportedModels) {
    this.supportedModels = supportedModels;
  }

  public List<Map<String, String>> getSupportedComponents() {
    return supportedComponents;
  }

  public void setSupportedComponents(List<Map<String, String>> supportedComponents) {
    this.supportedComponents = supportedComponents;
  }


  public List<ActionArtifact> getArtifacts() {
    return artifacts;
  }

  public void setArtifacts(List<ActionArtifact> artifacts) {
    this.artifacts = artifacts;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  /**
   * To entity action entity.
   *
   * @return the action entity
   */
  public ActionEntity toEntity() {
    ActionEntity destination = new ActionEntity();

    destination
        .setActionUuId(this.getActionUuId() != null ? this.getActionUuId().toUpperCase() : null);
    destination.setActionInvariantUuId(
        this.getActionInvariantUuId() != null ? this.getActionInvariantUuId().toUpperCase() : null);
    destination.setName(this.getName() != null ? this.getName().toLowerCase() : null);
    if (Objects.nonNull(this.getVendorList())) {
      destination.setVendorList(new HashSet<>(this.getVendorList()));
    }
    if (Objects.nonNull(this.getCategoryList())) {
      destination.setCategoryList(new HashSet<>(this.getCategoryList()));
    }
    destination.setTimestamp(this.getTimestamp());
    destination.setUser(this.getUser());
    destination.setVersion(Version.valueOf(this.getVersion()));
    if (this.getStatus() != null) {
      destination.setStatus(this.getStatus().name());
    }
    destination.setSupportedComponents(getIdFromMap(this.getSupportedComponents(),
        ActionConstants.SUPPORTED_COMPONENTS_ID));
    destination.setSupportedModels(
        getIdFromMap(this.getSupportedModels(), ActionConstants.SUPPORTED_MODELS_VERSION_ID));
    destination.setData(this.getData());
    return destination;
  }

  private Set<String> getIdFromMap(List<Map<String, String>> map, String idName) {
    Set<String> list = new HashSet<>();
    if (map != null && !map.isEmpty()) {
      for (Map<String, String> entry : map) {
        if (entry.containsKey(idName)) {
          list.add(entry.get(idName) != null ? entry.get(idName).toLowerCase() : null);
        }
      }
      return list;
    }
    return null;
  }

  @Override
  public int compareTo(Object object) {
    Action obj = (Action) object;
    Version thisVersion = Version.valueOf(this.version);
    Version objVersion = Version.valueOf(obj.getVersion());
    if (obj.getName().compareTo(this.getName()) == 0) {
      return compareVersions(objVersion, thisVersion);
    }
    return obj.getName().compareTo(this.getName());
  }

  private int compareVersions(Version objVersion, Version thisVersion) {
    if (objVersion.getMajor() == thisVersion.getMajor()) {
      return Integer.compare(objVersion.getMinor(), thisVersion.getMinor());
    }
    return Integer.compare(objVersion.getMajor(), thisVersion.getMajor());
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Action action = (Action) o;

    if (!Objects.equals(version, action.version)) {
      return false;
    }

    return Objects.equals(name, action.name);
  }

  @Override
  public int hashCode() {
    return com.google.common.base.Objects.hashCode(version);
  }
}
