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

package org.openecomp.sdc.vendorlicense.licenseartifacts.impl.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.licenseartifacts.impl.types.mixins.MixinEntitlementPoolEntityForVendorLicenseArtifact;
import org.openecomp.sdc.vendorlicense.licenseartifacts.impl.types.mixins.MixinLicenseKeyGroupEntityForVendorLicenseArtifact;

import java.util.Collection;

@JacksonXmlRootElement(localName = "vendor-license-model",
    namespace = "http://xmlns.openecomp.org/asdc/license-model/1.0")
public class VendorLicenseArtifact extends XmlArtifact {
  @JsonProperty(value = "vendor-name")
  String vendorName;

  Collection<EntitlementPoolEntity> entitlementPoolEntities;
  Collection<LicenseKeyGroupEntity> licenseKeyGroupEntities;

  public String getVendorName() {
    return vendorName;
  }

  public void setVendorName(String vendorName) {
    this.vendorName = vendorName;
  }

  @JacksonXmlProperty(isAttribute = false, localName = "entitlement-pool")
  @JacksonXmlElementWrapper(localName = "entitlement-pool-list")
  public Collection<EntitlementPoolEntity> getEntitlementPoolEntities() {
    return entitlementPoolEntities;
  }

  public void setEntitlementPoolEntities(
      Collection<EntitlementPoolEntity> entitlementPoolEntities) {
    this.entitlementPoolEntities = entitlementPoolEntities;
  }

  @JacksonXmlProperty(isAttribute = false, localName = "license-key-group")
  @JacksonXmlElementWrapper(localName = "license-key-group-list")
  public Collection<LicenseKeyGroupEntity> getLicenseKeyGroupEntities() {
    return licenseKeyGroupEntities;
  }

  public void setLicenseKeyGroupEntities(
      Collection<LicenseKeyGroupEntity> licenseKeyGroupEntities) {
    this.licenseKeyGroupEntities = licenseKeyGroupEntities;
  }

  @Override
  void initMapper() {
    xmlMapper.addMixIn(EntitlementPoolEntity.class,
        MixinEntitlementPoolEntityForVendorLicenseArtifact.class);
    xmlMapper.addMixIn(LicenseKeyGroupEntity.class,
        MixinLicenseKeyGroupEntityForVendorLicenseArtifact.class);
  }
}
