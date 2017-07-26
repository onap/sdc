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

package org.openecomp.sdcrests.vendorlicense.types;

import io.swagger.annotations.ApiModel;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementMetric;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementTime;
import org.openecomp.sdc.vendorlicense.dao.types.OperationalScope;
import org.openecomp.sdc.vendorlicense.dao.types.ThresholdUnit;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@ApiModel(value = "EntitlementPoolRequest")
@JsonIgnoreProperties({"manufacturerReferenceNumber", "time", "aggregationFunction", "entitlementMetric"})
public class EntitlementPoolRequestDto {

  @NotNull
  @Size(max = 120)
  private String name;

  @Size(max = 1000)
  private String description;

  private Integer thresholdValue;

  private ThresholdUnit thresholdUnits;
  @Size(max = 120)
  private String increments;

  @Valid
  private MultiChoiceOrOtherDto<OperationalScope> operationalScope;

  private String startDate;
  private String expiryDate;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getThresholdValue() {
    return thresholdValue;
  }

  public void setThresholdValue(Integer thresholdValue) {
    this.thresholdValue = thresholdValue;
  }

  public ThresholdUnit getThresholdUnits() {
    return thresholdUnits;
  }

  public void setThresholdUnits(ThresholdUnit thresholdUnits) {
    this.thresholdUnits = thresholdUnits;
  }

  public String getIncrements() {
    return increments;
  }

  public void setIncrements(String increments) {
    this.increments = increments;
  }

  public MultiChoiceOrOtherDto<OperationalScope> getOperationalScope() {
    return operationalScope;
  }

  public void setOperationalScope(MultiChoiceOrOtherDto<OperationalScope> operationalScope) {
    this.operationalScope = operationalScope;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(String expiryDate) {
    this.expiryDate = expiryDate;
  }
}
