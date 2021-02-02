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

package org.openecomp.sdc.be.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertiesOwner;
import org.openecomp.sdc.be.datatypes.enums.CreatedFrom;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.log.api.ILogConfiguration;

@Getter
@Setter
public class ComponentInstance extends ComponentInstanceDataDefinition implements PropertiesOwner {

    private Map<String, List<CapabilityDefinition>> capabilities;
    private Map<String, List<RequirementDefinition>> requirements;
    private Map<String, ArtifactDefinition> deploymentArtifacts;
    private Map<String, ArtifactDefinition> artifacts;
    private List<GroupInstance> groupInstances;
    private Map<String, Object> interfaces;
    private List<PropertyDefinition> properties;
    private List<AttributeDefinition> attributes;
    private CINodeFilterDataDefinition nodeFilter;
    private List<InputDefinition> inputs;

    public ComponentInstance() {
        super();
    }

    public ComponentInstance(ComponentInstanceDataDefinition r) {
        super(r);
    }

    public Map<String, ArtifactDefinition> safeGetDeploymentArtifacts() {
        return deploymentArtifacts == null ? Collections.emptyMap() : deploymentArtifacts;
    }

    public Map<String, ArtifactDefinition> safeGetInformationalArtifacts() {
        return artifacts == null ? Collections.emptyMap() : deploymentArtifacts;
    }

    public Map<String, ArtifactDefinition> safeGetArtifacts() {
        return artifacts == null ? Collections.emptyMap() : artifacts;
    }

    public String getActualComponentUid() {
        return getIsProxy() || isServiceSubstitution() ? getSourceModelUid() : getComponentUid();
    }

    public boolean isArtifactExists(ArtifactGroupTypeEnum groupType, String artifactLabel) {
        if (ArtifactGroupTypeEnum.DEPLOYMENT == groupType) {
            return safeGetDeploymentArtifacts().get(artifactLabel) != null;
        }
        return safeGetInformationalArtifacts().get(artifactLabel) != null;
    }

    public void addInterface(String interfaceName, Object interfaceDefinition) {
        if (MapUtils.isEmpty(this.interfaces)) {
            this.interfaces = new HashMap<>();
        }
        this.interfaces.put(interfaceName, interfaceDefinition);
    }

    //supportability log method return map of component metadata teddy.h
    public Map<String, String> getComponentMetadataForSupportLog() {
        Map<String, String> componentMetadata = new HashMap<>();
        componentMetadata.put(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_NAME, getName());
        componentMetadata.put(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_VERSION, getVersion());
        componentMetadata.put(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_UUID, getSourceModelUuid());
        return componentMetadata;
    }

    public boolean isCreatedFromCsar() {
        return CreatedFrom.CSAR.equals(this.getCreatedFrom());
    }

}
