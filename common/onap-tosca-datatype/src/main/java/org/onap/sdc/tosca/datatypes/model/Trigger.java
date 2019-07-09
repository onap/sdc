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

public class Trigger {

    private String description;
    private String event_type;
    private TimeInterval schedule;
    private EventFilter target_filter;
    private Condition condition;
    private Object action;

    @Override
    public int hashCode() {
        int result = getDescription() != null ? getDescription().hashCode() : 0;
        result = 31 * result + getEvent_type().hashCode();
        result = 31 * result + (getSchedule() != null ? getSchedule().hashCode() : 0);
        result = 31 * result + (getTarget_filter() != null ? getTarget_filter().hashCode() : 0);
        result = 31 * result + (getCondition() != null ? getCondition().hashCode() : 0);
        result = 31 * result + getAction().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Trigger)) {
            return false;
        }

        Trigger trigger = (Trigger) o;

        if (getDescription() != null ? !getDescription().equals(trigger.getDescription()) :
                    trigger.getDescription() != null) {
            return false;
        }
        if (!getEvent_type().equals(trigger.getEvent_type())) {
            return false;
        }
        if (getSchedule() != null ? !getSchedule().equals(trigger.getSchedule()) : trigger.getSchedule() != null) {
            return false;
        }
        if (getTarget_filter() != null ? !getTarget_filter().equals(trigger.getTarget_filter()) :
                    trigger.getTarget_filter() != null) {
            return false;
        }
        if (getCondition() != null ? !getCondition().equals(trigger.getCondition()) : trigger.getCondition() != null) {
            return false;
        }
        return getAction().equals(trigger.getAction());
    }

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
}
