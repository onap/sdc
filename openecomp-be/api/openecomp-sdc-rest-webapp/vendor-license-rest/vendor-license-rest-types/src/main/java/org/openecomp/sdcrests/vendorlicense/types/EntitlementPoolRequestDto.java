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
import org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementMetric;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementTime;
import org.openecomp.sdc.vendorlicense.dao.types.OperationalScope;
import org.openecomp.sdc.vendorlicense.dao.types.ThresholdUnit;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@ApiModel(value = "EntitlementPoolRequest")
public class EntitlementPoolRequestDto {

  @NotNull
  @Size(max = 120)
  private String name;
  @NotNull
  @Size(max = 1000)
  private String description;
  @NotNull
  private int thresholdValue;
  @NotNull
  private ThresholdUnit thresholdUnits;
  @NotNull
  @Valid
  private ChoiceOrOtherDto<EntitlementMetric> entitlementMetric;
  @Size(max = 120)
  private String increments;
  @NotNull
  @Valid
  private ChoiceOrOtherDto<AggregationFunction> aggregationFunction;
  @NotNull
  @Valid
  private MultiChoiceOrOtherDto<OperationalScope> operationalScope;
  @NotNull
  @Valid
  private ChoiceOrOtherDto<EntitlementTime> time;
  @NotNull
  @Size(max = 100)
  private String manufacturerReferenceNumber;

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

  public int getThresholdValue() {
    return thresholdValue;
  }

  public void setThresholdValue(int thresholdValue) {
    this.thresholdValue = thresholdValue;
  }

  public ThresholdUnit getThresholdUnits() {
    return thresholdUnits;
  }

  public void setThresholdUnits(ThresholdUnit thresholdUnits) {
    this.thresholdUnits = thresholdUnits;
  }

  public ChoiceOrOtherDto<EntitlementMetric> getEntitlementMetric() {
    return entitlementMetric;
  }

  public void setEntitlementMetric(ChoiceOrOtherDto<EntitlementMetric> entitlementMetric) {
    this.entitlementMetric = entitlementMetric;
  }

  public String getIncrements() {
    return increments;
  }

  public void setIncrements(String increments) {
    this.increments = increments;
  }

  public ChoiceOrOtherDto<AggregationFunction> getAggregationFunction() {
    return aggregationFunction;
  }

  public void setAggregationFunction(ChoiceOrOtherDto<AggregationFunction> aggregationFunction) {
    this.aggregationFunction = aggregationFunction;
  }

  public MultiChoiceOrOtherDto<OperationalScope> getOperationalScope() {
    return operationalScope;
  }

  public void setOperationalScope(MultiChoiceOrOtherDto<OperationalScope> operationalScope) {
    this.operationalScope = operationalScope;
  }

  public ChoiceOrOtherDto<EntitlementTime> getTime() {
    return time;
  }

  public void setTime(ChoiceOrOtherDto<EntitlementTime> time) {
    this.time = time;
  }

  public String getManufacturerReferenceNumber() {
    return manufacturerReferenceNumber;
  }

  public void setManufacturerReferenceNumber(String manufacturerReferenceNumber) {
    this.manufacturerReferenceNumber = manufacturerReferenceNumber;
  }
}
