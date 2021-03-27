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
package org.onap.sdc.tosca.datatypes.model;

import java.util.List;

public class StepDefinition {

    private String target;
    private String target_relationship;
    private String operation_host;
    private List<Constraint> filter;
    private List<ActivityDefinition> activities;
    private String on_success;
    private String on_failure;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget_relationship() {
        return target_relationship;
    }

    public void setTarget_relationship(String target_relationship) {
        this.target_relationship = target_relationship;
    }

    public String getOperation_host() {
        return operation_host;
    }

    public void setOperation_host(String operation_host) {
        this.operation_host = operation_host;
    }

    public List<Constraint> getFilter() {
        return filter;
    }

    public void setFilter(List<Constraint> filter) {
        this.filter = filter;
    }

    public List<ActivityDefinition> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityDefinition> activities) {
        this.activities = activities;
    }

    public String getOn_success() {
        return on_success;
    }

    public void setOn_success(String on_success) {
        this.on_success = on_success;
    }

    public String getOn_failure() {
        return on_failure;
    }

    public void setOn_failure(String on_failure) {
        this.on_failure = on_failure;
    }
}
