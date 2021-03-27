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

import java.util.Objects;

public class Trigger {

    private String description;
    private String event_type;
    private TimeInterval schedule;
    private EventFilter target_filter;
    private Condition condition;
    private Object action;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEvent_type() {
        return event_type;
    }

    public void setEvent_type(String eventType) {
        this.event_type = eventType;
    }

    public TimeInterval getSchedule() {
        return schedule;
    }

    public void setSchedule(TimeInterval schedule) {
        this.schedule = schedule;
    }

    public EventFilter getTarget_filter() {
        return target_filter;
    }

    public void setTarget_filter(EventFilter targetFilter) {
        this.target_filter = targetFilter;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Object getAction() {
        return action;
    }

    public void setAction(Object action) {
        this.action = action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, event_type, schedule, target_filter, condition, action);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Trigger trigger = (Trigger) o;
        return Objects.equals(description, trigger.description) && Objects.equals(event_type, trigger.event_type) && Objects
            .equals(schedule, trigger.schedule) && Objects.equals(target_filter, trigger.target_filter) && Objects
            .equals(condition, trigger.condition) && Objects.equals(action, trigger.action);
    }
}
