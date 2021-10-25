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

package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.GraphTestUtils;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.EntryData;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Created by chaya on 6/12/2017.
 */

@SpringJUnitConfig(locations = "classpath:application-context-test.xml")
public class ToscaElementOperationTest extends ModelTestBase {

    @Rule
    public TestName testName = new TestName();
    private List<GraphVertex> allVertices = new ArrayList<>();
    private boolean isInitialized = false;
    @javax.annotation.Resource
    private ToscaElementOperationTestImpl toscaElementOperation;
    @javax.annotation.Resource
    private JanusGraphDao janusGraphDao;

    @BeforeAll
    public static void initTest() {
        ModelTestBase.init();

    }

    @BeforeEach
    public void beforeTest() {
        if (!isInitialized) {
            GraphTestUtils.clearGraph(janusGraphDao);
            //exportGraphMl(janusGraphDao.getGraph().left().value(),"");
            initGraphForTest();
            isInitialized = true;
        }
    }

    @Test
    public void testGetAllHighestResourcesNoFilter() {

        Either<List<ToscaElement>, StorageOperationStatus> highestResourcesRes = toscaElementOperation
            .getElementCatalogData(ComponentTypeEnum.RESOURCE, null, true);
        assertTrue(highestResourcesRes.isLeft());
        List<ToscaElement> highestResources = highestResourcesRes.left().value();
        // calculate expected count value
        long highestResourcesExpectedCount = calculateCount(new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
                put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
            }
        }, null);
        assertEquals(highestResources.stream().count(), highestResourcesExpectedCount);
    }

    @Test
    public void testGetAllResourcesCertifiedNoFilter() {
        Either<List<ToscaElement>, StorageOperationStatus> highestResourcesRes = toscaElementOperation
            .getElementCatalogData(ComponentTypeEnum.RESOURCE, null, false);
        assertTrue(highestResourcesRes.isLeft());
        List<ToscaElement> highestResources = highestResourcesRes.left().value();
        // calculate expected count value
        long highestResourcesExpectedCount = calculateCount(new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
                put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
            }
        }, null);
        highestResourcesExpectedCount += calculateCount(new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
                put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
            }
        }, new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
            }
        });
        assertEquals(highestResources.stream().count(), highestResourcesExpectedCount);
    }

    @Test
    public void testGetHighestResourcesExclude() {

        // exclude VFCMT
        List<ResourceTypeEnum> excludeList = Arrays.asList(ResourceTypeEnum.VFCMT);
        assertTrue(genericTestGetResourcesWithExcludeList(excludeList));

        // exclude CP & VL
        excludeList = Arrays.asList(ResourceTypeEnum.VL, ResourceTypeEnum.CP);
        assertTrue(genericTestGetResourcesWithExcludeList(excludeList));

        // exclude CP & VL & VF & VFC
        excludeList = Arrays
            .asList(ResourceTypeEnum.VL, ResourceTypeEnum.CP, ResourceTypeEnum.VF, ResourceTypeEnum.VFC);
        assertTrue(genericTestGetResourcesWithExcludeList(excludeList));
    }

    @Test
    public void testGetAllResourcesCertifiedExclude() {
        // exclude VFCMT
        List<ResourceTypeEnum> excludeList = Arrays.asList(ResourceTypeEnum.VFCMT);
        assertTrue(genericTestGetCertifiedResourcesWithExcludeList(excludeList));

        // exclude CP & VL
        excludeList = Arrays.asList(ResourceTypeEnum.VL, ResourceTypeEnum.CP);
        assertTrue(genericTestGetCertifiedResourcesWithExcludeList(excludeList));

        // exclude CP & VL & VF & VFC
        excludeList = Arrays
            .asList(ResourceTypeEnum.VL, ResourceTypeEnum.CP, ResourceTypeEnum.VF, ResourceTypeEnum.VFC);
        assertTrue(genericTestGetCertifiedResourcesWithExcludeList(excludeList));
    }

    @Test
    public void testGetAllHighestServicesNoFilter() {
        Either<List<ToscaElement>, StorageOperationStatus> highestResourcesRes = toscaElementOperation
            .getElementCatalogData(ComponentTypeEnum.SERVICE, null, true);
        assertTrue(highestResourcesRes.isLeft());
        List<ToscaElement> highestResources = highestResourcesRes.left().value();
        // calculate expected count value
        long highestResourcesExpectedCount = calculateCount(new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
                put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
            }
        }, null);
        assertEquals(highestResources.stream().count(), highestResourcesExpectedCount);
    }

    @Test
    public void testGetAllCertifiedServicesNoFilter() {
        Either<List<ToscaElement>, StorageOperationStatus> highestResourcesRes = toscaElementOperation
            .getElementCatalogData(ComponentTypeEnum.SERVICE, null, false);
        assertTrue(highestResourcesRes.isLeft());
        List<ToscaElement> highestResources = highestResourcesRes.left().value();
        // calculate expected count value
        long highestResourcesExpectedCount = calculateCount(new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
                put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
            }
        }, null);
        highestResourcesExpectedCount += calculateCount(new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
                put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
            }
        }, new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
            }
        });
        assertEquals(highestResources.stream().count(), highestResourcesExpectedCount);
    }

    @Test
    public void testGetServicesExcludeList() {
        List<ResourceTypeEnum> excludeList = Arrays.asList(ResourceTypeEnum.VF, ResourceTypeEnum.VFCMT);
        Either<List<ToscaElement>, StorageOperationStatus> highestResourcesRes = toscaElementOperation
            .getElementCatalogData(ComponentTypeEnum.SERVICE, excludeList, true);
        assertTrue(highestResourcesRes.isLeft());
        List<ToscaElement> highestResources = highestResourcesRes.left().value();
        // calculate expected count value
        long highestResourcesExpectedCount = calculateCount(new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
                put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
            }
        }, null);
        assertEquals(highestResources.stream().count(), highestResourcesExpectedCount);
    }

    @Test
    public void testGetCertifiedServicesExcludeList() {
        List<ResourceTypeEnum> excludeList = Arrays.asList(ResourceTypeEnum.VL);
        Either<List<ToscaElement>, StorageOperationStatus> highestResourcesRes = toscaElementOperation
            .getElementCatalogData(ComponentTypeEnum.SERVICE, excludeList, false);
        assertTrue(highestResourcesRes.isLeft());
        List<ToscaElement> highestResources = highestResourcesRes.left().value();
        // calculate expected count value
        long highestResourcesExpectedCount = calculateCount(new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
                put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
            }
        }, null);
        highestResourcesExpectedCount += calculateCount(new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
                put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
            }
        }, new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
            }
        });
        assertEquals(highestResources.stream().count(), highestResourcesExpectedCount);
    }

    @Test
    public void testUpdateToscaElement_NotFound() {
        Either<TopologyTemplate, StorageOperationStatus> result;
        TopologyTemplate topologyTemplate = new TopologyTemplate();
        String userID = "userID";
        topologyTemplate.setLastUpdaterUserId(userID);
        GraphVertex graphVertex = new GraphVertex();
        ComponentParametersView componentParametersView = new ComponentParametersView();
        result = toscaElementOperation.updateToscaElement(topologyTemplate, graphVertex, componentParametersView);
        assertEquals(null, result);
    }

    @Test
    public void testCreateDataType() {
        final String expected = "newDataType";
        final DataTypeDefinition result = ToscaElementOperation.createDataType(expected);
        assertNotNull(result);
        assertEquals(expected, result.getName());
    }

    @Test
    public void testCreateDataTypeDefinitionWithName() {
        final String expected = "newDataType";
        final String description = "DESCRIPTION";
        final String derivedFromName = "DERIVED_FROM_NAME";
        final String uniqueId = "UNIQUE_ID";

        final Map<String, Object> attributeMap = new HashMap<>();
        attributeMap.put(TypeUtils.ToscaTagNamesEnum.DESCRIPTION.getElementName(), description);
        attributeMap.put(TypeUtils.ToscaTagNamesEnum.DERIVED_FROM_NAME.getElementName(), derivedFromName);

        final Map<String, Object> derivedFromMap = new HashMap<>();
        derivedFromMap.put(JsonPresentationFields.NAME.getPresentation(), derivedFromName);
        derivedFromMap.put(JsonPresentationFields.UNIQUE_ID.getPresentation(), uniqueId);
        final long creationTime = System.currentTimeMillis();
        derivedFromMap.put(JsonPresentationFields.CREATION_TIME.getPresentation(), creationTime);
        final long modificationTime = System.currentTimeMillis();
        derivedFromMap.put(JsonPresentationFields.MODIFICATION_TIME.getPresentation(), modificationTime);

        attributeMap.put(JsonPresentationFields.DERIVED_FROM.getPresentation(), derivedFromMap);

        final Entry<String, Object> attributeNameValue = new EntryData<>(expected, attributeMap);
        final DataTypeDefinition result = ToscaElementOperation.createDataTypeDefinitionWithName(attributeNameValue);

        assertNotNull(result);
        assertEquals(derivedFromName, result.getDerivedFromName());
        assertEquals(description, result.getDescription());

        final DataTypeDefinition resultDerivedFrom = result.getDerivedFrom();
        assertNotNull(resultDerivedFrom);
        assertEquals(derivedFromName, resultDerivedFrom.getName());
        assertEquals(uniqueId, resultDerivedFrom.getUniqueId());
        assertEquals(creationTime, resultDerivedFrom.getCreationTime().longValue());
        assertEquals(modificationTime, resultDerivedFrom.getModificationTime().longValue());

    }

    private boolean genericTestGetResourcesWithExcludeList(List<ResourceTypeEnum> excludeList) {
        Either<List<ToscaElement>, StorageOperationStatus> highestResourcesRes = toscaElementOperation
            .getElementCatalogData(ComponentTypeEnum.RESOURCE, excludeList, true);
        assertTrue(highestResourcesRes.isLeft());
        List<ToscaElement> highestResources = highestResourcesRes.left().value();
        // calculate expected count value
        long highestResourcesExpectedCount = calculateCount(new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
                put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
            }
        }, new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.RESOURCE_TYPE, excludeList);
            }
        });
        return highestResources.stream().count() == (highestResourcesExpectedCount);
    }

    private boolean genericTestGetCertifiedResourcesWithExcludeList(List<ResourceTypeEnum> excludeList) {
        Either<List<ToscaElement>, StorageOperationStatus> highestResourcesRes = toscaElementOperation
            .getElementCatalogData(ComponentTypeEnum.RESOURCE, excludeList, false);
        assertTrue(highestResourcesRes.isLeft());
        List<ToscaElement> highestResources = highestResourcesRes.left().value();
        // calculate expected count value
        long highestResourcesExpectedCount = calculateCount(new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
                put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
            }
        }, new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.RESOURCE_TYPE, excludeList);
            }
        });
        highestResourcesExpectedCount += calculateCount(new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
                put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
            }
        }, new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
                put(GraphPropertyEnum.RESOURCE_TYPE, excludeList);
            }
        });
        return highestResources.stream().count() == highestResourcesExpectedCount;
    }

    private void initGraphForTest() {
        GraphTestUtils.createRootCatalogVertex(janusGraphDao);

        Map<GraphPropertyEnum, Object> highstVerticesProps = new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
            }
        };

        Map<GraphPropertyEnum, Object> certifiedVerticesProps = new HashMap<GraphPropertyEnum, Object>() {
            {
                put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
                put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
            }
        };

        // add vertices with higestVersion = true
        allVertices.add(GraphTestUtils.createResourceVertex(janusGraphDao, highstVerticesProps, ResourceTypeEnum.VF));
        allVertices.add(GraphTestUtils.createResourceVertex(janusGraphDao, highstVerticesProps, ResourceTypeEnum.VFC));
        allVertices
            .add(GraphTestUtils.createResourceVertex(janusGraphDao, highstVerticesProps, ResourceTypeEnum.VFCMT));
        allVertices.add(GraphTestUtils.createResourceVertex(janusGraphDao, highstVerticesProps, ResourceTypeEnum.VL));
        allVertices.add(GraphTestUtils.createResourceVertex(janusGraphDao, highstVerticesProps, ResourceTypeEnum.CP));
        allVertices.add(GraphTestUtils.createServiceVertex(janusGraphDao, highstVerticesProps));

        // add vertices with non-additional properties
        for (int i = 0; i < 2; i++) {
            allVertices.add(GraphTestUtils.createResourceVertex(janusGraphDao, new HashMap<>(), ResourceTypeEnum.VF));
            allVertices.add(GraphTestUtils.createResourceVertex(janusGraphDao, new HashMap<>(), ResourceTypeEnum.VFC));
            allVertices
                .add(GraphTestUtils.createResourceVertex(janusGraphDao, new HashMap<>(), ResourceTypeEnum.VFCMT));
            allVertices.add(GraphTestUtils.createResourceVertex(janusGraphDao, new HashMap<>(), ResourceTypeEnum.VL));
            allVertices.add(GraphTestUtils.createResourceVertex(janusGraphDao, new HashMap<>(), ResourceTypeEnum.CP));
            allVertices.add(GraphTestUtils.createServiceVertex(janusGraphDao, new HashMap<>()));
        }

        // add certified vertices
        for (int i = 0; i < 3; i++) {
            allVertices
                .add(GraphTestUtils.createResourceVertex(janusGraphDao, certifiedVerticesProps, ResourceTypeEnum.VF));
            allVertices
                .add(GraphTestUtils.createResourceVertex(janusGraphDao, certifiedVerticesProps, ResourceTypeEnum.VFC));
            allVertices.add(
                GraphTestUtils.createResourceVertex(janusGraphDao, certifiedVerticesProps, ResourceTypeEnum.VFCMT));
            allVertices
                .add(GraphTestUtils.createResourceVertex(janusGraphDao, certifiedVerticesProps, ResourceTypeEnum.VL));
            allVertices
                .add(GraphTestUtils.createResourceVertex(janusGraphDao, certifiedVerticesProps, ResourceTypeEnum.CP));
            allVertices.add(GraphTestUtils.createServiceVertex(janusGraphDao, certifiedVerticesProps));
        }
        //allVertices.stream().forEach( v -> System.out.println("type: "+v.getMetadataProperty(GraphPropertyEnum.COMPONENT_TYPE)));
        //String result = GraphTestUtils.exportGraphMl(janusGraphDao.getGraph().left().value(), "");
        //System.out.println("graph is: " + result);
    }

    private long calculateCount(HashMap<GraphPropertyEnum, Object> hasProps,
                                Map<GraphPropertyEnum, Object> doesntHaveProps) {
        return allVertices.stream().
            filter(v -> {
                Map<GraphPropertyEnum, Object> vertexProps = v.getMetadataProperties();
                if (hasProps != null) {
                    for (Map.Entry<GraphPropertyEnum, Object> prop : hasProps.entrySet()) {
                        Object value = vertexProps.get(prop.getKey());
                        if (value == null || !value.equals(prop.getValue())) {
                            return false;
                        }
                    }
                }

                if (doesntHaveProps != null) {
                    for (Map.Entry<GraphPropertyEnum, Object> prop : doesntHaveProps.entrySet()) {
                        Object value = vertexProps.get(prop.getKey());
                        Object propValue = prop.getValue();
                        if (value != null && propValue != null && propValue instanceof List) {
                            for (ResourceTypeEnum propVal : (List<ResourceTypeEnum>) propValue) {
                                if (propVal.name().equals(value)) {
                                    return false;
                                }
                            }
                        } else if (value != null && value.equals(propValue)) {
                            return false;
                        }
                    }
                }
                return true;
            }).count();
    }
}
