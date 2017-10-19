package org.openecomp.sdc.be.model.jsontitan.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.utils.GraphTestUtils;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fj.data.Either;

/**
 * Created by chaya on 6/12/2017.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class ToscaElementOperationTest extends ModelTestBase{

    private List<GraphVertex> allVertices = new ArrayList<>();
    private boolean isInitialized = false;

    @javax.annotation.Resource
    ToscaElementOperationTestImpl toscaElementOperation;

    @javax.annotation.Resource
    TitanDao titanDao;

    @BeforeClass
    public static void initTest(){
        ModelTestBase.init();

    }

    @Rule
    public TestName testName = new TestName();

    @Before
    public void beforeTest() {
        if (!isInitialized) {
            GraphTestUtils.clearGraph(titanDao);
            //exportGraphMl(titanDao.getGraph().left().value(),"");
            initGraphForTest();
            isInitialized = true;
        }
    }

    @Test
    public void testGetAllHighestResourcesNoFilter() {

        Either<List<ToscaElement>, StorageOperationStatus> highestResourcesRes = toscaElementOperation.getElementCatalogData(ComponentTypeEnum.RESOURCE, null, true);
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
        Either<List<ToscaElement>, StorageOperationStatus> highestResourcesRes = toscaElementOperation.getElementCatalogData(ComponentTypeEnum.RESOURCE, null, false);
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
        excludeList = Arrays.asList(ResourceTypeEnum.VL, ResourceTypeEnum.CP, ResourceTypeEnum.VF, ResourceTypeEnum.VFC);
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
        excludeList = Arrays.asList(ResourceTypeEnum.VL, ResourceTypeEnum.CP, ResourceTypeEnum.VF, ResourceTypeEnum.VFC);
        assertTrue(genericTestGetCertifiedResourcesWithExcludeList(excludeList));
    }

    @Test
    public void testGetAllHighestServicesNoFilter() {
        Either<List<ToscaElement>, StorageOperationStatus> highestResourcesRes = toscaElementOperation.getElementCatalogData(ComponentTypeEnum.SERVICE, null, true);
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
        Either<List<ToscaElement>, StorageOperationStatus> highestResourcesRes = toscaElementOperation.getElementCatalogData(ComponentTypeEnum.SERVICE, null, false);
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
        Either<List<ToscaElement>, StorageOperationStatus> highestResourcesRes = toscaElementOperation.getElementCatalogData(ComponentTypeEnum.SERVICE, excludeList, true);
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
        Either<List<ToscaElement>, StorageOperationStatus> highestResourcesRes = toscaElementOperation.getElementCatalogData(ComponentTypeEnum.SERVICE, excludeList, false);
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

    private boolean genericTestGetResourcesWithExcludeList(List<ResourceTypeEnum> excludeList) {
        Either<List<ToscaElement>, StorageOperationStatus> highestResourcesRes = toscaElementOperation.getElementCatalogData(ComponentTypeEnum.RESOURCE,excludeList, true);
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
        Either<List<ToscaElement>, StorageOperationStatus> highestResourcesRes = toscaElementOperation.getElementCatalogData(ComponentTypeEnum.RESOURCE, excludeList, false);
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
        allVertices.add(GraphTestUtils.createResourceVertex(titanDao, highstVerticesProps, ResourceTypeEnum.VF));
        allVertices.add(GraphTestUtils.createResourceVertex(titanDao, highstVerticesProps, ResourceTypeEnum.VFC));
        allVertices.add(GraphTestUtils.createResourceVertex(titanDao, highstVerticesProps, ResourceTypeEnum.VFCMT));
        allVertices.add(GraphTestUtils.createResourceVertex(titanDao, highstVerticesProps, ResourceTypeEnum.VL));
        allVertices.add(GraphTestUtils.createResourceVertex(titanDao, highstVerticesProps, ResourceTypeEnum.CP));
        allVertices.add(GraphTestUtils.createServiceVertex(titanDao, highstVerticesProps));

        // add vertices with non-additional properties
        for (int i=0 ; i<2 ; i++) {
            allVertices.add(GraphTestUtils.createResourceVertex(titanDao, new HashMap<>(), ResourceTypeEnum.VF));
            allVertices.add(GraphTestUtils.createResourceVertex(titanDao, new HashMap<>(), ResourceTypeEnum.VFC));
            allVertices.add(GraphTestUtils.createResourceVertex(titanDao, new HashMap<>(), ResourceTypeEnum.VFCMT));
            allVertices.add(GraphTestUtils.createResourceVertex(titanDao, new HashMap<>(), ResourceTypeEnum.VL));
            allVertices.add(GraphTestUtils.createResourceVertex(titanDao, new HashMap<>(), ResourceTypeEnum.CP));
            allVertices.add(GraphTestUtils.createServiceVertex(titanDao, new HashMap<>()));
        }

        // add certified vertices
        for (int i=0; i<3; i++) {
            allVertices.add(GraphTestUtils.createResourceVertex(titanDao, certifiedVerticesProps, ResourceTypeEnum.VF));
            allVertices.add(GraphTestUtils.createResourceVertex(titanDao, certifiedVerticesProps, ResourceTypeEnum.VFC));
            allVertices.add(GraphTestUtils.createResourceVertex(titanDao, certifiedVerticesProps, ResourceTypeEnum.VFCMT));
            allVertices.add(GraphTestUtils.createResourceVertex(titanDao, certifiedVerticesProps, ResourceTypeEnum.VL));
            allVertices.add(GraphTestUtils.createResourceVertex(titanDao, certifiedVerticesProps, ResourceTypeEnum.CP));
            allVertices.add(GraphTestUtils.createServiceVertex(titanDao, certifiedVerticesProps));
        }
        //allVertices.stream().forEach( v -> System.out.println("type: "+v.getMetadataProperty(GraphPropertyEnum.COMPONENT_TYPE)));
        //String result = GraphTestUtils.exportGraphMl(titanDao.getGraph().left().value(), "");
        //System.out.println("graph is: " + result);
    }

    private long calculateCount(HashMap<GraphPropertyEnum, Object> hasProps, Map<GraphPropertyEnum, Object> doesntHaveProps){
        return allVertices.stream().
                filter(v -> {
                    Map<GraphPropertyEnum, Object> vertexProps = v.getMetadataProperties();
                    if (hasProps != null) {
                        for (Map.Entry<GraphPropertyEnum, Object> prop: hasProps.entrySet()){
                            Object value = vertexProps.get(prop.getKey());
                            if ( value == null || !value.equals(prop.getValue())) {
                                return false;
                            }
                        }
                    }

                    if (doesntHaveProps != null) {
                        for (Map.Entry<GraphPropertyEnum, Object> prop : doesntHaveProps.entrySet()) {
                            Object value = vertexProps.get(prop.getKey());
                            Object propValue = prop.getValue();
                            if ( value != null && propValue != null && propValue instanceof List ) {
                                for (ResourceTypeEnum propVal : (List<ResourceTypeEnum>)propValue) {
                                    if (propVal.name().equals(value)) {
                                        return false;
                                    }
                                }
                            }
                            else if (value != null && value.equals(propValue)){
                                return false;
                            }
                        }
                    }
                    return true;
                }).count();
    }
}
