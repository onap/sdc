/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import lombok.Data;
import org.openecomp.sdc.common.util.ValidationUtils;

@Data
public class LimitEntityDto {

    private String id;
    private String name;
    private String type;
    private String description;
    private String metric;
    private String value;
    private String unit;
    private String aggregationFunction;
    private String time;

    public void setId(final String id) {
        this.id = ValidationUtils.sanitizeInputString(id);
    }

    public void setName(final String name) {
        this.name = ValidationUtils.sanitizeInputString(name);
    }

    public void setType(final String type) {
        this.type = ValidationUtils.sanitizeInputString(type);
    }

    public void setDescription(final String description) {
        this.description = ValidationUtils.sanitizeInputString(description);
    }

    public void setMetric(final String metric) {
        this.metric = ValidationUtils.sanitizeInputString(metric);
    }

    public void setUnit(final String unit) {
        this.unit = ValidationUtils.sanitizeInputString(unit);
    }

    public void setAggregationFunction(final String aggregationFunction) {
        this.aggregationFunction = ValidationUtils.sanitizeInputString(aggregationFunction);
    }

    public void setTime(final String time) {
        this.time = ValidationUtils.sanitizeInputString(time);
    }

    public void setValue(final String value) {
        this.value = ValidationUtils.sanitizeInputString(value);
    }
}
