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

package org.openecomp.sdc.vendorlicense.dao.types.xml;

public class LimitXml {
  String description;
  String metric;
  String values;
  String unit;
  String time;
  String aggregationFunction;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getMetric() {
    return metric;
  }

  public void setMetric(String metric) {
    this.metric = metric;
  }

  public String getValues() {
    return values;
  }

  public void setValues(String values) {
    this.values = values;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getAggregationFunction() {
    return aggregationFunction;
  }

  public void setAggregationFunction(String aggregationFunction) {
    this.aggregationFunction = aggregationFunction;
  }

  public EntitlementTimeForXml getTimeForArtifact() {
    EntitlementTimeForXml timeForXml = new EntitlementTimeForXml();
    if (time != null) {
      timeForXml.setValue(time);
    }

    return timeForXml;
  }

  public AggregationFunctionForXml getAggregationFunctionForArtifact() {
    AggregationFunctionForXml aggregationFunctionForXml = new AggregationFunctionForXml();
    if (aggregationFunction != null) {
      aggregationFunctionForXml.setValue(aggregationFunction);
    }
    return aggregationFunctionForXml;
  }
}
