/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.model.jsonjanusgraph.utils;

import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CapabilityTestUtils {

    public static Map<String, MapPropertiesDataDefinition> createCapPropsForTopologyTemplate(TopologyTemplate topologyTemplate) {
        Map<String, ListCapabilityDataDefinition> capabilitiesMap = new HashMap<>();

        List<CapabilityDataDefinition> capabilityDefinitions = new ArrayList<>();
        CapabilityDefinition capabilityDefinition = createCapabilityDefinition();

        capabilityDefinitions.add(capabilityDefinition);
        ListCapabilityDataDefinition listCapabilityDataDefinition = new ListCapabilityDataDefinition(capabilityDefinitions);
        capabilitiesMap.put(capabilityDefinition.getType(), listCapabilityDataDefinition);
        topologyTemplate.setCapabilities(capabilitiesMap);

        List<ComponentInstanceProperty> capPropList = new ArrayList<>();
        ComponentInstanceProperty instanceProperty = createProperties();
        capPropList.add(instanceProperty);

        MapPropertiesDataDefinition dataToCreate = new MapPropertiesDataDefinition();
        for (ComponentInstanceProperty cip : capPropList) {
            PropertyDataDefinition propertyDataDefinition = new PropertyDataDefinition(cip);
            dataToCreate.put(cip.getName(), propertyDataDefinition);
        }

        Map<String, MapPropertiesDataDefinition> capabilitiesProperties = new HashMap<>();
        capabilitiesProperties.put(capabilityDefinition.getType() + ModelConverter.CAP_PROP_DELIM +
                capabilityDefinition.getName(), dataToCreate);
        return capabilitiesProperties;
    }

    private static CapabilityDefinition createCapabilityDefinition() {
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName("cap" + Math.random());
        capabilityDefinition.setType("tosca.capabilities.network.Bindable");
        capabilityDefinition.setOwnerId("resourceId");
        capabilityDefinition.setUniqueId("capUniqueId");
        List<String> path = new ArrayList<>();
        path.add("path1");
        capabilityDefinition.setPath(path);
        return capabilityDefinition;
    }

    private static ComponentInstanceProperty createProperties() {
        ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty();
        instanceProperty.setUniqueId("ComponentInput1_uniqueId");
        instanceProperty.setType("Integer");
        instanceProperty.setName("prop_name");
        instanceProperty.setDescription("prop_description_prop_desc");
        instanceProperty.setOwnerId("capUniqueId");
        instanceProperty.setValue("{\"get_input\":\"extcp20_order\"}");
        instanceProperty.setSchema(new SchemaDefinition());
        return instanceProperty;
    }
}
