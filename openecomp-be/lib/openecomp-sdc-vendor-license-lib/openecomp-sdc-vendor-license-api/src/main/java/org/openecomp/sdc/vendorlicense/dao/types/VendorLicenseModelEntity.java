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

package org.openecomp.sdc.vendorlicense.dao.types;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Computed;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

import java.util.Objects;

@Table(keyspace = "dox", name = "vendor_license_model")
public class VendorLicenseModelEntity implements VersionableEntity {
  public static final String ENTITY_TYPE = "Vendor License Model";

  @PartitionKey
  @Column(name = "vlm_id")
  private String id;

  @PartitionKey(value = 1)
  @Frozen
  private Version version;

  @Column(name = "vendor_name")
  private String vendorName;
  private String description;
  private String oldVersion;
  @Column(name = "icon")
  private String iconRef;

  @Computed("writetime(vendor_name)")
  private Long writetimeMicroSeconds;

  public VendorLicenseModelEntity() {
  }

  public VendorLicenseModelEntity(String id, Version version) {
    this.id = id;
    this.version = version;
  }

  @Override
  public String getEntityType() {
    return ENTITY_TYPE;
  }

  @Override
  public String getFirstClassCitizenId() {
    return getId();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public Version getVersion() {
    return version;
  }

  @Override
  public void setVersion(Version version) {
    this.version = version;
  }

  public String getVendorName() {
    return vendorName;
  }

  public void setVendorName(String vendorName) {
    this.vendorName = vendorName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getIconRef() {
    return iconRef;
  }

  public void setIconRef(String iconRef) {
    this.iconRef = iconRef;
  }

  public void setOldVersion(String oldVersion) {
    this.oldVersion = oldVersion;
  }

  public String getOldVersion() {
    return oldVersion;
  }


  @Override
  public int hashCode() {
    return Objects.hash(id, version, vendorName, description, iconRef);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    VendorLicenseModelEntity that = (VendorLicenseModelEntity) obj;
    return Objects.equals(id, that.id)
        && Objects.equals(version, that.version)
        && Objects.equals(vendorName, that.vendorName)
        && Objects.equals(description, that.description)
        && Objects.equals(iconRef, that.iconRef);
  }

  public Long getWritetimeMicroSeconds() {
    return writetimeMicroSeconds;
  }

  public void setWritetimeMicroSeconds(Long writetimeMicroSeconds) {
    this.writetimeMicroSeconds = writetimeMicroSeconds;
  }
}
