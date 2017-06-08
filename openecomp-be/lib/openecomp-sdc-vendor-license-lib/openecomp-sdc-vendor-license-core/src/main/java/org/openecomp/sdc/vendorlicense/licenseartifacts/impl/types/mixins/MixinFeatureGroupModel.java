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

package org.openecomp.sdc.vendorlicense.licenseartifacts.impl.types.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;

import java.util.Set;

public abstract class MixinFeatureGroupModel {
  @JacksonXmlProperty(isAttribute = false, localName = "entitlement-pool")
  @JacksonXmlElementWrapper(localName = "entitlement-pool-list")
  abstract Set<EntitlementPoolEntity> getEntitlementPools();

  @JacksonXmlProperty(isAttribute = false, localName = "license-key-group")
  @JacksonXmlElementWrapper(localName = "license-key-group-list")
  abstract Set<LicenseKeyGroupEntity> getLicenseKeyGroups();

  @JacksonXmlProperty(isAttribute = false, localName = "part-number")
  abstract String getEntityPartNumber();

  @JacksonXmlProperty(isAttribute = false, localName = "feature-group-uuid")
  abstract String getEntityId();

  @JacksonXmlProperty(isAttribute = false, localName = "description")
  abstract String getEntityDesc();

  @JacksonXmlProperty(isAttribute = false, localName = "name")
  abstract String getEntityName();


  @JsonIgnore
  abstract FeatureGroupEntity getFeatureGroup();

  @JsonIgnore
  abstract String getEntityType();


}
