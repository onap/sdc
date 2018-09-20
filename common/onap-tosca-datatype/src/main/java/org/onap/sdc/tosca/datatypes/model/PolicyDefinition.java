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

package org.onap.sdc.tosca.datatypes.model;

import java.util.List;
import java.util.Map;

public class PolicyDefinition implements Template {

    private String type;
    private String description;
    private Map<String, String> metadata;
    private Map<String, Object> properties;
    private List<String> targets;
    private Map<String, Trigger> triggers;

    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getMetadata() != null ? getMetadata().hashCode() : 0);
        result = 31 * result + (getProperties() != null ? getProperties().hashCode() : 0);
        result = 31 * result + (getTargets() != null ? getTargets().hashCode() : 0);
        result = 31 * result + (getTriggers() != null ? getTriggers().hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PolicyDefinition)) {
            return false;
        }

        PolicyDefinition that = (PolicyDefinition) o;

        if (!getType().equals(that.getType())) {
            return false;
        }
        if (getDescription() != null ? !getDescription().equals(that.getDescription()) :
                    that.getDescription() != null) {
            return false;
        }
        if (getMetadata() != null ? !getMetadata().equals(that.getMetadata()) : that.getMetadata() != null) {
            return false;
        }
        if (getProperties() != null ? !getProperties().equals(that.getProperties()) : that.getProperties() != null) {
            return false;
        }
        if (getTargets() != null ? !getTargets().equals(that.getTargets()) : that.getTargets() != null) {
            return false;
        }
        return getTriggers() != null ? getTriggers().equals(that.getTriggers()) : that.getTriggers() == null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public List<String> getTargets() {
        return targets;
    }

    public void setTargets(List<String> targets) {
        this.targets = targets;
    }

    public Map<String, Trigger> getTriggers() {

        return triggers;
    }

    public void setTriggers(Map<String, Trigger> triggers) {
        this.triggers = triggers;
    }
}
