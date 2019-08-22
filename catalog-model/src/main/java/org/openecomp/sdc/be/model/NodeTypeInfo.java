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

package org.openecomp.sdc.be.model;

import java.util.List;
import java.util.Map;

public class NodeTypeInfo {
    String type;
    String templateFileName;
    List<String> derivedFrom;
    boolean isNested;
    boolean isSubstitutionMapping;
    Map<String, Object> mappedToscaTemplate;

    public NodeTypeInfo getUnmarkedCopy(){
        NodeTypeInfo unmarked = new NodeTypeInfo();
        unmarked.type = this.type;
        unmarked.templateFileName = this.templateFileName;
        unmarked.derivedFrom = this.derivedFrom;
        unmarked.isNested = false;
        unmarked.mappedToscaTemplate = this.mappedToscaTemplate;
        return unmarked;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getTemplateFileName() {
        return templateFileName;
    }
    public void setTemplateFileName(String templateFileName) {
        this.templateFileName = templateFileName;
    }
    public List<String> getDerivedFrom() {
        return derivedFrom;
    }
    public void setDerivedFrom(List<String> derivedFrom) {
        this.derivedFrom = derivedFrom;
    }
    public boolean isNested() {
        return isNested;
    }
    public void setNested(boolean isNested) {
        this.isNested = isNested;
    }
    public boolean isSubstitutionMapping() {
        return isSubstitutionMapping;
    }
    public void setSubstitutionMapping(boolean isSubstitutionMapping) {
        this.isSubstitutionMapping = isSubstitutionMapping;
    }

    public Map<String, Object> getMappedToscaTemplate() {
        return mappedToscaTemplate;
    }

    public void setMappedToscaTemplate(Map<String, Object> mappedToscaTemplate) {
        this.mappedToscaTemplate = mappedToscaTemplate;
    }
}
