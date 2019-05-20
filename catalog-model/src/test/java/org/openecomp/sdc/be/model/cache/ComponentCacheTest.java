package org.openecomp.sdc.be.model.cache;

import fj.data.Either;
import mockit.Deencapsulation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.ComponentCassandraDao;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.resources.data.ComponentCacheData;
import org.openecomp.sdc.be.unittests.utils.ModelConfDependentTest;

import java.util.*;
import java.util.function.Function;

public class ComponentCacheTest extends ModelConfDependentTest {

    @InjectMocks
    ComponentCache testSubject;

    @Mock
    ComponentCassandraDao componentCassandraDao;

    @Mock
    ToscaOperationFacade toscaOperationFacade;

    @Before
    public void setUpMocks() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInit() throws Exception {
        // default test
        testSubject.init();
    }

    @Test
    public void testIsEnabled() throws Exception {

        boolean result;

        // default test

        result = testSubject.isEnabled();
    }

    @Test
    public void testSetEnabled() throws Exception {

        boolean enabled = false;

        // default test

        testSubject.setEnabled(enabled);
    }

    @Test
    public void testGetComponentNotFound() throws Exception {

        String componentUid = "mock";
        Long lastModificationTime = null;
        Function<Component, Component> filterFieldsFunc = null;
        Either<Component, ActionStatus> result;

        Mockito.when(componentCassandraDao.getComponent("mock"))
                .thenReturn(Either.right(ActionStatus.ARTIFACT_NOT_FOUND));
        // default test
        result = testSubject.getComponent(componentUid, lastModificationTime, filterFieldsFunc);
    }

    @Test
    public void testGetComponentInvalidDate() throws Exception {

        String componentUid = "mock";
        Long lastModificationTime = 0L;
        Function<Component, Component> filterFieldsFunc = null;
        Either<Component, ActionStatus> result;

        ComponentCacheData a = new ComponentCacheData();
        a.setModificationTime(new Date());
        Mockito.when(componentCassandraDao.getComponent("mock")).thenReturn(Either.left(a));
        // default test
        result = testSubject.getComponent(componentUid, lastModificationTime, filterFieldsFunc);
    }

    @Test
    public void testGetComponentDeserializeError() throws Exception {

        String componentUid = "mock";
        Long lastModificationTime = 0L;
        Function<Component, Component> filterFieldsFunc = null;
        Either<Component, ActionStatus> result;

        ComponentCacheData a = new ComponentCacheData();
        a.setModificationTime(new Date(0L));
        a.setType(NodeTypeEnum.Resource.getName());
        Mockito.when(componentCassandraDao.getComponent("mock")).thenReturn(Either.left(a));
        // default test
        result = testSubject.getComponent(componentUid, lastModificationTime, filterFieldsFunc);
    }

    @Test
    public void testGetAllComponentIdTimeAndType() throws Exception {

        Either<List<ComponentCacheData>, ActionStatus> result;

        // default test

        result = testSubject.getAllComponentIdTimeAndType();
        testSubject.setEnabled(false);
        result = testSubject.getAllComponentIdTimeAndType();
    }

    @Test
    public void testUpdateCatalogInMemoryCacheWithCertified() throws Exception {

        List<Component> foundComponents = new LinkedList<>();

        // default test
        testSubject.init();
        Deencapsulation.invoke(testSubject, "updateCatalogInMemoryCacheWithCertified", foundComponents,
                ComponentTypeEnum.RESOURCE);
    }

    @Test
    public void testGetDataFromInMemoryCache() throws Exception {

        Set<String> components = new HashSet<>();
        components.add("mock");
        ComponentTypeEnum componentTypeEnum = null;
        List<Component> result;

        // default test
        testSubject.init();
        result = Deencapsulation.invoke(testSubject, "getDataFromInMemoryCache", components,
                ComponentTypeEnum.RESOURCE);
    }

    @Test
    public void testGetComponents() throws Exception {

        Set<String> components = new HashSet<>();
        Function<List<Component>, List<Component>> filterFieldsFunc = new Function<List<Component>, List<Component>>() {

            @Override
            public List<Component> apply(List<Component> t) {
                return t;
            }
        };
        Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> result;

        List<ComponentCacheData> list = new LinkedList<>();
        Mockito.when(componentCassandraDao.getComponents(Mockito.any(List.class))).thenReturn(Either.left(list));

        // default test
        testSubject.init();
        result = testSubject.getComponents(components, filterFieldsFunc);
    }

    @Test
    public void testGetComponentsNotAllowed() throws Exception {

        Set<String> components = new HashSet<>();
        Function<List<Component>, List<Component>> filterFieldsFunc = null;

        Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> result;

        // default test
        testSubject.setEnabled(false);
        result = testSubject.getComponents(components, filterFieldsFunc);
    }

    @Test
    public void testGetComponentsCassndraError() throws Exception {

        Set<String> components = new HashSet<>();
        Function<List<Component>, List<Component>> filterFieldsFunc = null;
        Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> result;

        Mockito.when(componentCassandraDao.getComponents(Mockito.any(List.class)))
                .thenReturn(Either.right(ActionStatus.GENERAL_ERROR));

        // default test
        testSubject.init();
        result = testSubject.getComponents(components, filterFieldsFunc);
    }

    @Test
    public void testGetComponentsForLeftPanel() throws Exception {

        ComponentTypeEnum componentTypeEnum = null;
        String internalComponentType = "mock";
        Set<String> filteredResources = new HashSet<>();
        Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> result;

        List<ComponentCacheData> list = new LinkedList<>();
        Mockito.when(componentCassandraDao.getComponents(Mockito.any(List.class))).thenReturn(Either.left(list));

        // default test
        result = testSubject.getComponentsForLeftPanel(ComponentTypeEnum.RESOURCE, internalComponentType,
                filteredResources);
    }

    @Test
    public void testFilterForLeftPanel() throws Exception {

        List<Component> components = new LinkedList<>();
        List<Component> result;

        // test 1

        result = Deencapsulation.invoke(testSubject, "filterForLeftPanel", components);
        Assert.assertNotEquals(null, result);
    }

    @Test
    public void testFilterForCatalog() throws Exception {

        List<Component> components = new LinkedList<>();
        List<Component> result;

        // test 1
        result = Deencapsulation.invoke(testSubject, "filterForCatalog", components);
        Assert.assertNotEquals(null, result);
    }

    @Test
    public void testFilterFieldsForLeftPanel() throws Exception {
        Component result;

        // default test
        Resource resource = new Resource();
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        result = Deencapsulation.invoke(testSubject, "filterFieldsForLeftPanel", resource);
        Service service = new Service();
        service.setComponentType(ComponentTypeEnum.SERVICE);
        result = Deencapsulation.invoke(testSubject, "filterFieldsForLeftPanel", service);
    }

    @Test
    public void testFilterFieldsForCatalog() throws Exception {
        Component result;

        // default test

        Resource resource = new Resource();
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        result = Deencapsulation.invoke(testSubject, "filterFieldsForCatalog", resource);
        Service service = new Service();
        service.setComponentType(ComponentTypeEnum.SERVICE);
        result = Deencapsulation.invoke(testSubject, "filterFieldsForCatalog", service);
        Product product = new Product();
        product.setComponentType(ComponentTypeEnum.PRODUCT);
        result = Deencapsulation.invoke(testSubject, "filterFieldsForCatalog", product);
    }

    @Test
    public void testCopyFieldsForLeftPanel() throws Exception {

        Component component = new Resource();
        Component filteredComponent = new Resource();
        ((ResourceMetadataDataDefinition) component.getComponentMetadataDefinition().getMetadataDataDefinition())
                .setResourceType(ResourceTypeEnum.VL);
        // default test

        Deencapsulation.invoke(testSubject, "copyFieldsForLeftPanel", component, filteredComponent);
    }

    @Test
    public void testGetComponentsFullDisabled() throws Exception {

        Set<String> filteredResources = null;
        Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> result;

        // default test
        testSubject.setEnabled(false);
        result = Deencapsulation.invoke(testSubject, "getComponentsFull", Set.class);
    }


    @Test
    public void testGetComponentsFullDesirializeError() throws Exception {

        Set<String> filteredResources = new HashSet<>();
        filteredResources.add("mock");
        Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> result;

        List<ComponentCacheData> a = new LinkedList<>();
        ComponentCacheData e = new ComponentCacheData();
        e.setId("mock");
        e.setType(NodeTypeEnum.Resource.getName());
        a.add(e);
        Mockito.when(componentCassandraDao.getComponents(Mockito.any(List.class))).thenReturn(Either.left(a));

        // default test

        result = Deencapsulation.invoke(testSubject, "getComponentsFull", filteredResources);
    }


    @Test
    public void testGetComponent_1() throws Exception {

        String componentUid = "mock";
        Either<Component, ActionStatus> result;

        Mockito.when(componentCassandraDao.getComponent("mock"))
                .thenReturn(Either.right(ActionStatus.ARTIFACT_NOT_FOUND));

        // default test
        result = testSubject.getComponent(componentUid);
    }

    @Test
    public void testGetComponent_2() throws Exception {

        String componentUid = "mock";
        Long lastModificationTime = null;
        Either<Component, ActionStatus> result;

        Mockito.when(componentCassandraDao.getComponent("mock"))
                .thenReturn(Either.right(ActionStatus.ARTIFACT_NOT_FOUND));

        // default test
        Function<Component, Component> filterFieldsFunc = new Function<Component, Component>() {
            @Override
            public Component apply(Component component) {
                return new Resource();
            }
        };
        result = testSubject.getComponent(componentUid, lastModificationTime, filterFieldsFunc);
    }

    @Test
    public void testSaveComponent() throws Exception {

        String componentUid = "";
        Component component = new Resource();
        boolean result;

        // default test
        Mockito.when(componentCassandraDao.saveComponent(Mockito.any(ComponentCacheData.class)))
                .thenReturn(CassandraOperationStatus.OK);

        result = Deencapsulation.invoke(testSubject, "saveComponent", componentUid, 0L, NodeTypeEnum.Resource,
                component);
    }

    @Test
    public void testSetComponent_1Disabled() throws Exception {

        Component component = new Resource();
        component.setLastUpdateDate(0L);
        boolean result;

        // default test
        testSubject.setEnabled(false);
        result = testSubject.setComponent(component, NodeTypeEnum.Resource);
    }

    @Test
    public void testSetComponent_1() throws Exception {

        Component component = new Resource();
        component.setLastUpdateDate(0L);
        boolean result;

        // default test

        result = testSubject.setComponent(component, NodeTypeEnum.Resource);
    }


    @Test
    public void testGetComponentsFull_1CannotDeserialize() throws Exception {
        Map<String, Long> filteredResources = new HashMap<>();
        Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> result;

        // default test
        LinkedList<ComponentCacheData> left = new LinkedList<>();
        ComponentCacheData e = new ComponentCacheData();
        e.setType(NodeTypeEnum.Resource.getName());
        left.add(e);
        ImmutablePair<List<ComponentCacheData>, Set<String>> immutablePair = ImmutablePair.of(left, new HashSet<>());
        Mockito.when(componentCassandraDao.getComponents(Mockito.any(Map.class))).thenReturn(Either.left(immutablePair));

        result = Deencapsulation.invoke(testSubject, "getComponentsFull", filteredResources);
    }

    @Test
    public void testGetComponentsFull_1Disabled() throws Exception {
        Map<String, Long> filteredResources = new HashMap<>();
        Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> result;

        // default test
        testSubject.setEnabled(false);
        result = Deencapsulation.invoke(testSubject, "getComponentsFull", filteredResources);
    }

    @Test
    public void testGetComponentsFull_1NotFound() throws Exception {
        Map<String, Long> filteredResources = new HashMap<>();
        Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> result;

        // default test
        Mockito.when(componentCassandraDao.getComponents(Mockito.any(Map.class))).thenReturn(Either.right(ActionStatus.ARTIFACT_NOT_FOUND));

        result = Deencapsulation.invoke(testSubject, "getComponentsFull", filteredResources);
    }

    @Test
    public void testGetComponentsForCatalog_1Disabled() throws Exception {

        Map<String, Long> components = null;
        Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> result;

        // default test
        testSubject.setEnabled(false);
        result = testSubject.getComponentsForCatalog(components, ComponentTypeEnum.RESOURCE);
    }

    @Test
    public void testGetComponentsForCatalog_1() throws Exception {
        Map<String, Long> components = new HashMap<>();
        Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> result;

        // default test
        ImmutablePair<List<ComponentCacheData>, Set<String>> value = ImmutablePair.of(new LinkedList<>(), new HashSet<>());
        Mockito.when(componentCassandraDao.getComponents(Mockito.any(Map.class))).thenReturn(Either.left(value));
        testSubject.init();
        result = testSubject.getComponentsForCatalog(components, ComponentTypeEnum.RESOURCE);
    }

    @Test
    public void testGetComponentsForCatalog_1Error() throws Exception {
        Map<String, Long> components = new HashMap<>();
        Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> result;

        // default test
        Mockito.when(componentCassandraDao.getComponents(Mockito.any(Map.class))).thenReturn(Either.right(ActionStatus.COMPONENT_NOT_FOUND));

        result = testSubject.getComponentsForCatalog(components, ComponentTypeEnum.RESOURCE);
    }

    @Test
    public void testGetComponents_1Disabled() throws Exception {

        Map<String, Long> components = null;
        Function<List<Component>, List<Component>> filterFieldsFunc = null;
        Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> result;

        // default test
        testSubject.setEnabled(false);
        result = testSubject.getComponents(components, filterFieldsFunc);
    }

    @Test
    public void testGetComponentAndTimeNotFound() throws Exception {

        String componentUid = "";
        Function<Component, Component> filterFieldsFunc = null;
        Either<ImmutablePair<Component, Long>, ActionStatus> result;

        // default test
        Mockito.when(componentCassandraDao.getComponent(Mockito.anyString())).thenReturn(Either.right(ActionStatus.API_RESOURCE_NOT_FOUND));

        result = testSubject.getComponentAndTime(componentUid, filterFieldsFunc);
    }

    @Test
    public void testGetComponentFromCacheDisabled() throws Exception {
        String componentUid = "";
        Long lastModificationTime = null;
        Function<Component, Component> filterFieldsFunc = null;
        Either<ImmutablePair<Component, ComponentCacheData>, ActionStatus> result;

        // test 1
        lastModificationTime = null;
        testSubject.setEnabled(false);
        result = Deencapsulation.invoke(testSubject, "getComponentFromCache",
                new Object[]{componentUid, Long.class, Function.class});
    }

    @Test
    public void testDeleteComponentFromCacheFails() throws Exception {

        String id = "";
        ActionStatus result;

        // default test

        result = testSubject.deleteComponentFromCache(id);
    }

    @Test
    public void testDeleteComponentFromCacheDisabled() throws Exception {

        String id = "";
        ActionStatus result;

        // default test
        testSubject.setEnabled(false);
        result = testSubject.deleteComponentFromCache(id);
    }

    @Test
    public void testDeleteComponentFromCache() throws Exception {

        String id = "";
        ActionStatus result;

        // default test
        Mockito.when(componentCassandraDao.deleteComponent(Mockito.anyString())).thenReturn(CassandraOperationStatus.OK);
        result = testSubject.deleteComponentFromCache(id);
    }
}