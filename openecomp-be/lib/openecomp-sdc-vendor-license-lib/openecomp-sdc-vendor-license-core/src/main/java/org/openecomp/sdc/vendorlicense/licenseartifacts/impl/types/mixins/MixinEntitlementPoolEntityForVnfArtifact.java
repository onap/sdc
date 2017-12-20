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
import com.fasterxml.jackson.annotation.JsonProperty;
import org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction;
import org.openecomp.sdc.vendorlicense.dao.types.ChoiceOrOther;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementMetric;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementTime;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdc.vendorlicense.dao.types.OperationalScope;
import org.openecomp.sdc.vendorlicense.dao.types.xml.AggregationFunctionForXml;
import org.openecomp.sdc.vendorlicense.dao.types.xml.EntitlementMetricForXml;
import org.openecomp.sdc.vendorlicense.dao.types.xml.EntitlementTimeForXml;
import org.openecomp.sdc.vendorlicense.dao.types.xml.LimitForXml;
import org.openecomp.sdc.vendorlicense.dao.types.xml.OperationalScopeForXml;
import org.openecomp.sdc.vendorlicense.dao.types.xml.ThresholdForXml;

import java.util.Collection;
import java.util.Set;

public abstract class MixinEntitlementPoolEntityForVnfArtifact {
  @JsonProperty(value = "threshold-value")
  abstract ThresholdForXml getThresholdForArtifact();

  @JsonProperty(value = "entitlement-pool-invariant-uuid")
  abstract String getId();

  @JsonProperty(value = "manufacturer-reference-number")
  abstract String getManufacturerReferenceNumber();

  @JsonIgnore
  abstract Set<String> getReferencingFeatureGroups();

  @JsonIgnore
  abstract String getFirstClassCitizenId();

  @JsonProperty(value = "entitlement-pool-uuid")
  abstract String getVersionUuId();

  @JsonIgnore
  abstract String getVersion();

  @JsonProperty(value = "version")
  abstract String getVersionForArtifact();

  @JsonIgnore
  abstract String getVendorLicenseModelId();

  @JsonIgnore
  abstract String getThresholdUnit();

  @JsonIgnore
  abstract float getThresholdValue();

  @JsonIgnore
  abstract String getStartDate();

  @JsonIgnore
  abstract String getExpiryDate();

  @JsonProperty(value = "start-date")
  abstract String getIsoFormatStartDate();

  @JsonProperty(value = "expiry-date")
  abstract String getIsoFormatExpiryDate();

  @JsonIgnore
  abstract ChoiceOrOther<EntitlementMetric> getEntitlementMetric();

  @JsonIgnore
  abstract ChoiceOrOther<EntitlementTime> getTime();

  @JsonIgnore
  abstract ChoiceOrOther<AggregationFunction> getAggregationFunction();

  @JsonIgnore
  abstract String getEntityType();

  @JsonProperty(value = "operational-scope")
  abstract OperationalScopeForXml getOperationalScopeForArtifact();

  @JsonIgnore
  abstract ChoiceOrOther<OperationalScope> getOperationalScope();


  @JsonProperty(value = "entitlement-metric")
  abstract EntitlementMetricForXml getEntitlementMetricForArtifact();

  @JsonProperty(value = "time")
  abstract EntitlementTimeForXml getTimeForArtifact();


  @JsonProperty(value = "aggregation-function")
  abstract AggregationFunctionForXml getAggregationFunctionForArtifact();

  @JsonProperty(value = "sp-limits")
  abstract LimitForXml getSPLimits();

  @JsonProperty(value = "vendor-limits")
  abstract LimitForXml getVendorLimits();

  @JsonIgnore
  abstract Collection<LimitEntity> getLimits();

}
