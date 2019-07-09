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

package org.openecomp.sdc.ci.tests.datatypes;

import java.util.ArrayList;
import java.util.List;

public class PropertyObject {

    private String defaultValue;
    private String name;
    private String parentUniqueId;
    private boolean password;
    private boolean required;
    private List<Schema> Schema;
    private String type;
    private String uniqueId;
    private boolean definition;
    private Object value = null;


    public PropertyObject() {
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public PropertyObject(String defaultValue, String name, String parentUniqueId, String uniqueId) {
        this.defaultValue = defaultValue;
        this.name = name;
        this.parentUniqueId = parentUniqueId;
        this.uniqueId = uniqueId;
        this.password = false;
        this.required = false;
        this.type = "String";
        this.definition = false;
        this.Schema = new ArrayList<Schema>();
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentUniqueId() {
        return parentUniqueId;
    }

    public void setParentUniqueId(String parentUniqueId) {
        this.parentUniqueId = parentUniqueId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}

class Schema {

    private List<Property> property;
}

class Property {}

