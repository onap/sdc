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

package org.openecomp.sdc.be.model.jsonjanusgraph.datamodel;

import org.openecomp.sdc.be.datatypes.elements.*;

import java.util.List;
import java.util.Map;

public class NodeType extends ToscaElement{

    public NodeType() {
        super(ToscaElementTypeEnum.NODE_TYPE);
    }

    private List<String> derivedFrom;
    private List<String> derivedList;
    private Map<String, PropertyDataDefinition> attributes;
    private Map<String, InterfaceDataDefinition> interfaceArtifacts;

    public List<String> getDerivedList() {
        return derivedList;
    }

    public void setDerivedList(List<String> derivedList) {
        this.derivedList = derivedList;
    }

    public List<String> getDerivedFrom() {
        return derivedFrom;
    }

    public void setDerivedFrom(List<String> derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    public Map<String, PropertyDataDefinition> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, PropertyDataDefinition> attributes) {
        this.attributes = attributes;
    }

    public Map<String, InterfaceDataDefinition> getInterfaceArtifacts() {
        return interfaceArtifacts;
    }

    public void setInterfaceArtifacts(Map<String, InterfaceDataDefinition> interfaceArtifacts) {
        this.interfaceArtifacts = interfaceArtifacts;
    }

}
