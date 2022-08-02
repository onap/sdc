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
package org.openecomp.sdc.be.components.impl.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterPropertyDataDefinition;
import org.openecomp.sdc.be.model.UploadNodeFilterCapabilitiesInfo;
import org.openecomp.sdc.be.model.UploadNodeFilterInfo;
import org.openecomp.sdc.be.model.UploadNodeFilterPropertyInfo;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class CINodeFilterUtils {

    Logger log = Logger.getLogger(CINodeFilterUtils.class);

    public CINodeFilterDataDefinition getNodeFilterDataDefinition(UploadNodeFilterInfo uploadNodeFilterInfo, String uniqueId) {
        CINodeFilterDataDefinition nodeFilterDataDefinition = new CINodeFilterDataDefinition();
        nodeFilterDataDefinition.setName(uploadNodeFilterInfo.getName());
        List<RequirementNodeFilterPropertyDataDefinition> collect = uploadNodeFilterInfo.getProperties().stream().map(this::buildProperty)
            .collect(Collectors.toList());
        ListDataDefinition<RequirementNodeFilterPropertyDataDefinition> listDataDefinition = new ListDataDefinition<>();
        listDataDefinition.getListToscaDataDefinition().addAll(collect);
        nodeFilterDataDefinition.setProperties(listDataDefinition);
        nodeFilterDataDefinition.setCapabilities(convertCapabilities(uploadNodeFilterInfo.getCapabilities()));
        nodeFilterDataDefinition.setID(uniqueId);
        nodeFilterDataDefinition.setTosca_id(uploadNodeFilterInfo.getTosca_id());
        return nodeFilterDataDefinition;
    }

    private ListDataDefinition<RequirementNodeFilterCapabilityDataDefinition> convertCapabilities(
        List<Map<String, UploadNodeFilterCapabilitiesInfo>> capabilities) {
        ListDataDefinition<RequirementNodeFilterCapabilityDataDefinition> listDataDefinition = new ListDataDefinition<>();
        for (Map<String, UploadNodeFilterCapabilitiesInfo> capability : capabilities) {
            for (UploadNodeFilterCapabilitiesInfo capabilityDetail : capability.values()) {
                RequirementNodeFilterCapabilityDataDefinition requirementNodeFilterCapabilityDataDefinition = convertCapability(capabilityDetail);
                listDataDefinition.add(requirementNodeFilterCapabilityDataDefinition);
            }
        }
        return listDataDefinition;
    }

    private RequirementNodeFilterCapabilityDataDefinition convertCapability(UploadNodeFilterCapabilitiesInfo capability) {
        RequirementNodeFilterCapabilityDataDefinition retVal = new RequirementNodeFilterCapabilityDataDefinition();
        retVal.setName(capability.getName());
        List<RequirementNodeFilterPropertyDataDefinition> props = capability.getProperties().stream().map(this::buildProperty)
            .collect(Collectors.toList());
        ListDataDefinition<RequirementNodeFilterPropertyDataDefinition> propsList = new ListDataDefinition<>();
        propsList.getListToscaDataDefinition().addAll(props);
        retVal.setProperties(propsList);
        return retVal;
    }

    private RequirementNodeFilterPropertyDataDefinition buildProperty(UploadNodeFilterPropertyInfo uploadNodeFilterPropertyInfo) {
        RequirementNodeFilterPropertyDataDefinition retVal = new RequirementNodeFilterPropertyDataDefinition();
        retVal.setName(uploadNodeFilterPropertyInfo.getName());
        List<String> propertyConstraints = uploadNodeFilterPropertyInfo.getValues();
        retVal.setConstraints(propertyConstraints);
        return retVal;
    }
}
