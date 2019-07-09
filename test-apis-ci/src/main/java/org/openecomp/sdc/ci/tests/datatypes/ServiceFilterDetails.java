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

public class ServiceFilterDetails {
    private String servicePropertyName;
    private String constraintOperator;
    private String sourceType;
    private String sourceName;
    private Object value;

    public ServiceFilterDetails() {}

    public ServiceFilterDetails(String servicePropertyName, String constraintOperator, String sourceType, String sourceName,
                        Object value) {
        this.servicePropertyName = servicePropertyName;
        this.constraintOperator = constraintOperator;
        this.sourceType = sourceType;
        this.sourceName = sourceName;
        this.value = value;
    }

    public String getServicePropertyName() {
        return servicePropertyName;
    }

    public void setServicePropertyName(String servicePropertyName) {
        this.servicePropertyName = servicePropertyName;
    }

    public String getConstraintOperator() {
        return constraintOperator;
    }

    public void setConstraintOperator(String constraintOperator) {
        this.constraintOperator = constraintOperator;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
}
