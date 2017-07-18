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
import org.openecomp.sdc.vendorlicense.dao.types.xml.AggregationFunctionForXml;
import org.openecomp.sdc.vendorlicense.dao.types.xml.EntitlementTimeForXml;

public abstract class MixinLimitArtifact {

  @JsonIgnore
  abstract String getTime();

  @JsonIgnore
  abstract String getAggregationFunction();

  @JsonProperty(value = "aggregation-function")
  abstract AggregationFunctionForXml getAggregationFunctionForArtifact();

  @JsonProperty(value = "time")
  abstract EntitlementTimeForXml getTimeForArtifact();

}
