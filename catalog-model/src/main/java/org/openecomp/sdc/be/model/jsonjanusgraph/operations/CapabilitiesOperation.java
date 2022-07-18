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
package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@org.springframework.stereotype.Component("capabilities-operation")
public class CapabilitiesOperation extends BaseOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(CapabilitiesOperation.class);

    private static ListCapabilityDataDefinition convertToListCapabilityDataDefinition(List<CapabilityDefinition> capabilities) {
        List<CapabilityDataDefinition> capabilityDefinitions = new ArrayList<>(capabilities);
        return new ListCapabilityDataDefinition(capabilityDefinitions);
    }

    public Either<List<CapabilityDefinition>, StorageOperationStatus> addCapabilities(String componentId,
                                                                                      List<CapabilityDefinition> capabilityDefinitions) {
        return addOrUpdateCapabilities(componentId, capabilityDefinitions, false);
    }

    public Either<List<CapabilityDefinition>, StorageOperationStatus> updateCapabilities(String componentId,
                                                                                         List<CapabilityDefinition> capabilityDefinitions) {
        return addOrUpdateCapabilities(componentId, capabilityDefinitions, true);
    }

    private Either<List<CapabilityDefinition>, StorageOperationStatus> addOrUpdateCapabilities(String componentId,
                                                                                               List<CapabilityDefinition> capabilityDefinitions,
                                                                                               boolean isUpdateAction) {
        StorageOperationStatus statusRes = performUpdateToscaAction(isUpdateAction, componentId,
            Collections.singletonList(convertToListCapabilityDataDefinition(capabilityDefinitions)));
        if (!statusRes.equals(StorageOperationStatus.OK)) {
            LOGGER.error("Failed to find the parent capability of capability type {}." + " status is {}", componentId, statusRes);
            return Either.right(statusRes);
        }
        return Either.left(capabilityDefinitions);
    }

    public StorageOperationStatus deleteCapabilities(Component component, String capabilityIdToDelete) {
        return deleteToscaDataElements(component.getUniqueId(), EdgeLabelEnum.CAPABILITIES, Collections.singletonList(capabilityIdToDelete));
    }

    public StorageOperationStatus deleteCapabilityProperties(Component component, String capabilityPropIdToDelete) {
        return deleteToscaDataElements(component.getUniqueId(), EdgeLabelEnum.CAPABILITIES_PROPERTIES,
            Collections.singletonList(capabilityPropIdToDelete));
    }

    private StorageOperationStatus performUpdateToscaAction(boolean isUpdate, String componentId, List<ListCapabilityDataDefinition> toscaDataList) {
        if (isUpdate) {
            return updateToscaDataOfToscaElement(componentId, EdgeLabelEnum.CAPABILITIES, VertexTypeEnum.CAPABILITIES, toscaDataList,
                JsonPresentationFields.TYPE);
        } else {
            return addToscaDataToToscaElement(componentId, EdgeLabelEnum.CAPABILITIES, VertexTypeEnum.CAPABILITIES, toscaDataList,
                JsonPresentationFields.TYPE);
        }
    }

    private StorageOperationStatus createOrUpdateCapabilityProperties(String componentId, ToscaElement toscaElement,
                                                                      Map<String, MapPropertiesDataDefinition> propertiesMap) {
        GraphVertex toscaElementV = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse).left().on(this::throwStorageException);
        Map<String, MapPropertiesDataDefinition> capabilitiesProperties = toscaElement.getCapabilitiesProperties();
        if (MapUtils.isNotEmpty(capabilitiesProperties)) {
            capabilitiesProperties.forEach((key, val) -> {
                Map<String, PropertyDataDefinition> mapToscaDataDefinition = val.getMapToscaDataDefinition();
                mapToscaDataDefinition.forEach((key1, val1) -> {
                    propertiesMap.forEach((propKey, propVal) -> {
                        Map<String, PropertyDataDefinition> propValMapToscaDataDefinition = propVal.getMapToscaDataDefinition();
                        propValMapToscaDataDefinition.forEach((propKey1, propVal1) -> {
                            if (propKey1.equals(key1) && val1.getUniqueId().equals(propVal1.getUniqueId())) {
                                ToscaDataDefinition.mergeDataMaps(mapToscaDataDefinition, propValMapToscaDataDefinition);
                            }
                        });
                    });
                });
            });
            ToscaDataDefinition.mergeDataMaps(propertiesMap, capabilitiesProperties);
        }
        return topologyTemplateOperation
            .updateFullToscaData(toscaElementV, EdgeLabelEnum.CAPABILITIES_PROPERTIES, VertexTypeEnum.CAPABILITIES_PROPERTIES, propertiesMap);
    }

    public StorageOperationStatus createOrUpdateCapabilityProperties(String componentId, boolean isTopologyTemplate, Map<String, MapPropertiesDataDefinition> propertiesMap) {
        StorageOperationStatus propertiesStatusRes = null;
        if (MapUtils.isNotEmpty(propertiesMap)) {
            propertiesStatusRes = createOrUpdateCapabilityProperties(componentId, getToscaElement(componentId, isTopologyTemplate), propertiesMap);
        }
        return propertiesStatusRes;
    }

    private ToscaElement getToscaElement(String componentId, boolean isTopologyTemplate) {
        if (isTopologyTemplate){
            return topologyTemplateOperation.getToscaElement(componentId, getFilterComponentWithCapProperties()).left()
                .on(this::throwStorageException);
        }
        return nodeTypeOperation.getToscaElement(componentId, getFilterComponentWithCapProperties()).left()
                .on(this::throwStorageException);
    }

    private ComponentParametersView getFilterComponentWithCapProperties() {
        ComponentParametersView filter = new ComponentParametersView();
        filter.setIgnoreCapabiltyProperties(false);
        return filter;
    }

    private ToscaElement throwStorageException(StorageOperationStatus status) {
        throw new StorageException(status);
    }
}
