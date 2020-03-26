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

package org.openecomp.sdc.be.unittests.utils;

import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.resources.data.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class FactoryUtils {
    private static final String STRING = "string";
    private static final String DEFAULT_CAPABILITY_TYPE = "tosca.capabilities.Node";

	private FactoryUtils() {
    }

    public static Resource createVFWithRI(String riVersion) {
        Resource vf = createVF();
        ComponentInstance ri = createResourceInstanceWithVersion(riVersion);
        addComponentInstanceToVF(vf, ri);
        return vf;
    }

    public static Resource createVF() {
        Resource resource = new Resource();
        String uniqueId = UUID.randomUUID().toString();
        resource.setUniqueId(uniqueId);
        return resource;
    }

    public static ResourceMetadataData createResourceByType(String resourceType) {
        ResourceMetadataData resource = new ResourceMetadataData();
        String uniqueId = UUID.randomUUID().toString();
        resource.getMetadataDataDefinition().setHighestVersion(true);
        resource.getMetadataDataDefinition().setUniqueId(uniqueId);
        ((ResourceMetadataDataDefinition)resource.getMetadataDataDefinition()).setResourceType(ResourceTypeEnum.getTypeIgnoreCase(resourceType));
        return resource;
    }

    public static void addComponentInstanceToVF(Resource vf, ComponentInstance resourceInstance) {
        List<ComponentInstance> componentsInstances = vf.getComponentInstances() != null ? vf.getComponentInstances()
                : new ArrayList<>();
        componentsInstances.add(resourceInstance);
        vf.setComponentInstances(componentsInstances);
    }

    public static ComponentInstance createResourceInstance() {
        ComponentInstance ri = new ComponentInstance();
        ri.setComponentVersion("0.1");
        String uniqueId = UUID.randomUUID().toString();
        ri.setComponentUid(uniqueId);
        ri.setUniqueId(uniqueId);
        ri.setName("genericRI" + uniqueId);
        ri.setOriginType(OriginTypeEnum.VF);
        return ri;

    }

    public static ComponentInstance createResourceInstanceWithVersion(String riVersion) {
        ComponentInstance ri = createResourceInstance();
        ri.setComponentVersion(riVersion);
        return ri;
    }

    public static CapabilityData createCapabilityData() {
        CapabilityData capData = new CapabilityData();
        String uniqueId = UUID.randomUUID().toString();
        capData.setUniqueId(uniqueId);

        capData.setType(DEFAULT_CAPABILITY_TYPE);
        return capData;
    }

    public static RequirementData createRequirementData() {
        RequirementData reqData = new RequirementData();
        String uniqueId = UUID.randomUUID().toString();
        reqData.setUniqueId(uniqueId);
        return reqData;
    }

    public static CapabilityDefinition convertCapabilityDataToCapabilityDefinitionAddProperties(
            CapabilityData capData) {
        CapabilityDefinition capDef = new CapabilityDefinition();
        capDef.setName("Cap2");
        capDef.setDescription(capData.getDescription());
        capDef.setUniqueId(capData.getUniqueId());
        capDef.setValidSourceTypes(capData.getValidSourceTypes());
        capDef.setType(capData.getType());
        capDef.setProperties(new ArrayList<>());
        ComponentInstanceProperty host = new ComponentInstanceProperty();
        host.setUniqueId(UUID.randomUUID().toString());
        host.setName("host");
        host.setDefaultValue("defhost");
        host.setType(STRING);

        host.setSchema(new SchemaDefinition());
        host.getSchema().setProperty(new PropertyDataDefinition());
        host.getSchema().getProperty().setType(STRING);

        capDef.getProperties().add(host);
        ComponentInstanceProperty port = new ComponentInstanceProperty();
        port.setName("port");
        port.setDefaultValue("defport");
        port.setUniqueId(UUID.randomUUID().toString());
        port.setType(STRING);

        port.setSchema(new SchemaDefinition());
        port.getSchema().setProperty(new PropertyDataDefinition());
        port.getSchema().getProperty().setType(STRING);

        capDef.getProperties().add(port);
        return capDef;
    }

    public static List<ComponentInstanceProperty> createComponentInstancePropertyList() {
        List<ComponentInstanceProperty> properties = new ArrayList<>();
        ComponentInstanceProperty host = new ComponentInstanceProperty();
        host.setUniqueId(UUID.randomUUID().toString());
        host.setName("host");
        host.setValue("newhost");
        host.setType(STRING);

        host.setSchema(new SchemaDefinition());
        host.getSchema().setProperty(new PropertyDataDefinition());
        host.getSchema().getProperty().setType(STRING);

        properties.add(host);
        ComponentInstanceProperty port = new ComponentInstanceProperty();
        port.setName("port");
        port.setValue("newport");
        port.setUniqueId(UUID.randomUUID().toString());
        port.setType(STRING);

        port.setSchema(new SchemaDefinition());
        port.getSchema().setProperty(new PropertyDataDefinition());
        port.getSchema().getProperty().setType(STRING);

        properties.add(port);
        return properties;
    }

    public static RequirementDefinition convertRequirementDataIDToRequirementDefinition(String reqDataId) {
        RequirementDefinition reqDef = new RequirementDefinition();
        reqDef.setUniqueId(reqDataId);
        reqDef.setCapability(DEFAULT_CAPABILITY_TYPE);
        return reqDef;
    }

    public static GraphEdge createGraphEdge() {
        return new GraphEdge();
    }

    public static CapabilityInstData createCapabilityInstData() {
        CapabilityInstData capInstData = new CapabilityInstData();
        String uniqueId = UUID.randomUUID().toString();
        capInstData.setUniqueId(uniqueId);
        return capInstData;
    }

    public static PropertyValueData createPropertyData() {
        PropertyValueData propData = new PropertyValueData();
        String uniqueId = UUID.randomUUID().toString();
        propData.setValue("localhost");
        propData.setUniqueId(uniqueId);
        return propData;
    }

    public static PropertyData convertCapabilityDefinitionToCapabilityData(PropertyDefinition propDef) {
        PropertyData propData = new PropertyData();
        propData.getPropertyDataDefinition().setUniqueId(propDef.getUniqueId());
        propData.getPropertyDataDefinition().setDefaultValue(propDef.getDefaultValue());
        return propData;
    }

    public static CapabilityDefinition convertCapabilityDataToCapabilityDefinitionRoot(CapabilityData capData) {
        CapabilityDefinition capDef = new CapabilityDefinition();
        capDef.setName("Cap1");
        capDef.setDescription(capData.getDescription());
        capDef.setUniqueId(capData.getUniqueId());
        capDef.setValidSourceTypes(capData.getValidSourceTypes());
        capDef.setType(capData.getType());
        capDef.setProperties(new ArrayList<>());
        ComponentInstanceProperty host = new ComponentInstanceProperty();
        host.setUniqueId(UUID.randomUUID().toString());
        host.setName("host");
        host.setDefaultValue("roothost");
        host.setType(STRING);

        host.setSchema(new SchemaDefinition());
        host.getSchema().setProperty(new PropertyDataDefinition());
        host.getSchema().getProperty().setType(STRING);

        capDef.getProperties().add(host);
        ComponentInstanceProperty port = new ComponentInstanceProperty();
        port.setName("port");
        port.setDefaultValue("rootport");
        port.setUniqueId(UUID.randomUUID().toString());
        port.setType(STRING);

        port.setSchema(new SchemaDefinition());
        port.getSchema().setProperty(new PropertyDataDefinition());
        port.getSchema().getProperty().setType(STRING);

        capDef.getProperties().add(port);
        return capDef;
    }
}
