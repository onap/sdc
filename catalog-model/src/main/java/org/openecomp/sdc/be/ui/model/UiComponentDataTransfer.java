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

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceInterface;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.category.CategoryDefinition;

@Getter
@Setter
public class UiComponentDataTransfer {

    protected ComponentTypeEnum componentType;
    protected List<AdditionalInformationDefinition> additionalInformation;
    private Map<String, ArtifactDefinition> artifacts;
    private Map<String, ArtifactDefinition> deploymentArtifacts;
    private Map<String, ArtifactDefinition> toscaArtifacts;
    private List<CategoryDefinition> categories;
    // User
    private String creatorUserId;
    private String creatorFullName;
    private String lastUpdaterUserId;
    private String lastUpdaterFullName;
    private List<ComponentInstance> componentInstances;
    private List<RequirementCapabilityRelDef> componentInstancesRelations;
    private Map<String, List<ComponentInstanceInput>> componentInstancesInputs;
    private Map<String, List<ComponentInstanceProperty>> componentInstancesProperties;
    private Map<String, List<ComponentInstanceAttribute>> componentInstancesAttributes;
    private Map<String, List<CapabilityDefinition>> capabilities;
    private List<PolicyDefinition> policies;
    private Map<String, List<RequirementDefinition>> requirements;
    private List<InputDefinition> inputs;
    private List<OutputDefinition> outputs;
    private List<GroupDefinition> groups;
    private Map<String, InterfaceDefinition> interfaces;
    private Map<String, CINodeFilterDataDefinition> nodeFilter;
    private Map<String, SubstitutionFilterDataDefinition> substitutionFilter;
    private Map<String, UINodeFilter> nodeFilterforNode;
    private Map<String, UINodeFilter> substitutionFilterForTopologyTemplate;
    private List<PropertyDefinition> properties;
    private List<AttributeDefinition> attributes;
    private Map<String, List<ComponentInstanceInterface>> componentInstancesInterfaces;
}
