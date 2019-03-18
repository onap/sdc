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

package org.openecomp.sdc.be.ui.model;

import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;

import java.util.List;
import java.util.Map;

public class UiResourceDataTransfer extends UiComponentDataTransfer{

    private UiResourceMetadata metadata;

    private List<String> derivedFrom;

    private List<String> derivedList;

    private List<PropertyDefinition> properties;

    private List<PropertyDefinition> attributes;

    private Map<String, InterfaceDefinition> interfaces;

    private List<String> defaultCapabilities;


    public UiResourceDataTransfer(){}

    public List<AdditionalInformationDefinition> getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(List<AdditionalInformationDefinition> additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public UiResourceMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(UiResourceMetadata metadata) {
        this.metadata = metadata;
    }

    public List<String> getDerivedFrom() {
        return derivedFrom;
    }

    public void setDerivedFrom(List<String> derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    public List<String> getDerivedList() {
        return derivedList;
    }

    public void setDerivedList(List<String> derivedList) {
        this.derivedList = derivedList;
    }

    public List<PropertyDefinition> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyDefinition> properties) {
        this.properties = properties;
    }

    public List<PropertyDefinition> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<PropertyDefinition> attributes) {
        this.attributes = attributes;
    }

    public Map<String, InterfaceDefinition> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(Map<String, InterfaceDefinition> interfaces) {
        this.interfaces = interfaces;
    }

    public List<String> getDefaultCapabilities() {
        return defaultCapabilities;
    }

    public void setDefaultCapabilities(List<String> defaultCapabilities) {
        this.defaultCapabilities = defaultCapabilities;
    }
}
