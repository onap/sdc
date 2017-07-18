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

import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;

import java.util.ArrayList;
import java.util.Collection;

public class FeatureGroupForArtifact {
  Collection<EntitlementPoolEntity> entitlementPoolEntities = new ArrayList<>();
  Collection<LicenseKeyGroupEntity> licenseKeyGroupEntities = new ArrayList<>();
  private String name;
  private String description;
  private String partNumber;
  private String manufacturerReferenceNumber;
  private String id;

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getPartNumber() {
    return partNumber;
  }

  public String getManufacturerReferenceNumber(){
    return manufacturerReferenceNumber;
  }

  public String getId() {
    return id;
  }

  public Collection<EntitlementPoolEntity> getEntitlementPoolEntities() {
    return entitlementPoolEntities;
  }

  public Collection<LicenseKeyGroupEntity> getLicenseKeyGroupEntities() {
    return licenseKeyGroupEntities;
  }
}
