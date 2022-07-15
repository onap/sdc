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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.CapabilityTestUtils;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class CapabilitiesOperationTest {

    @InjectMocks
    CapabilitiesOperation operation = new CapabilitiesOperation();
    @Mock
    private  JanusGraphDao mockJanusGraphDao;
    @Mock
    private TopologyTemplateOperation topologyTemplateOperation;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(mockJanusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);
        when(mockJanusGraphDao.getVertexById(anyString(), any())).thenReturn(Either.left(new GraphVertex()));

        when(topologyTemplateOperation.updateFullToscaData(any(), any(), any(), anyMap())).thenReturn(StorageOperationStatus.OK);
        TopologyTemplate topologyTemplate = new TopologyTemplate();

        Map<String, MapPropertiesDataDefinition> capPropsForTopologyTemplate = CapabilityTestUtils
                .createCapPropsForTopologyTemplate(topologyTemplate);
        topologyTemplate.setCapabilitiesProperties(capPropsForTopologyTemplate);

        when(topologyTemplateOperation.getToscaElement(anyString(), any())).thenReturn(Either.left(topologyTemplate));
    }

    @Test
    public void testCreateOrUpdateCapabilitiesProperties() {

        Map<String, PropertyDataDefinition> mapToscaDataDefinition = new HashMap<>();
        PropertyDataDefinition propertyDataDefinition = new PropertyDataDefinition();
        propertyDataDefinition.setUniqueId("ComponentInput1_uniqueId");
        propertyDataDefinition.setName("propName");
        mapToscaDataDefinition.put(propertyDataDefinition.getUniqueId(), propertyDataDefinition);
        MapPropertiesDataDefinition  mapPropertiesDataDefinition = new MapPropertiesDataDefinition(mapToscaDataDefinition);

        Map<String, MapPropertiesDataDefinition> propertiesMap = new HashMap<>();
        propertiesMap.put(propertyDataDefinition.getUniqueId(), mapPropertiesDataDefinition);

        StorageOperationStatus operationStatus = operation.createOrUpdateCapabilityProperties("componentId", true,
                propertiesMap);

        Assert.assertEquals(StorageOperationStatus.OK, operationStatus);
    }
}