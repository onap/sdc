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
import org.nustaq.serialization.FSTConfiguration;
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
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.resources.data.ComponentCacheData;
import org.openecomp.sdc.be.unittests.utils.ModelConfDependentTest;
import org.openecomp.sdc.common.util.SerializationUtils;
import org.openecomp.sdc.common.util.ZipUtil;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        assertTrue(result);
    }

    @Test
    public void testSetEnabled() throws Exception {

        // default test

        testSubject.setEnabled(false);
        assertFalse(testSubject.isEnabled());
    }

    @Test
    public void testGetComponentNotFound() throws Exception {

        String componentUid = "mock";
        Either<Component, ActionStatus> result;

        Mockito.when(componentCassandraDao.getComponent(componentUid))
                .thenReturn(Either.right(ActionStatus.ARTIFACT_NOT_FOUND));
        // default test
        result = testSubject.getComponent(componentUid, null, null);
        assertEquals(Either.right(ActionStatus.ARTIFACT_NOT_FOUND), result);
    }

    @Test
    public void testGetComponentInvalidDate() throws Exception {

        String componentUid = "mock";
        Long invalidDateTime = 0L;
        Either<Component, ActionStatus> result;

        ComponentCacheData a = new ComponentCacheData();
        a.setModificationTime(new Date());
        Mockito.when(componentCassandraDao.getComponent("mock")).thenReturn(Either.left(a));
        // default test
        result = testSubject.getComponent(componentUid, invalidDateTime, null);
        assertEquals(Either.right(ActionStatus.INVALID_CONTENT), result);
    }

    @Test
    public void testGetComponentDeserializeError() throws Exception {

        String componentUid = "mock";
        Long lastModificationTime = 0L;
        Either<Component, ActionStatus> result;

        ComponentCacheData a = new ComponentCacheData();
        a.setModificationTime(new Date(lastModificationTime));
        a.setType(NodeTypeEnum.Resource.getName());
        Mockito.when(componentCassandraDao.getComponent("mock")).thenReturn(Either.left(a));
        // default test
        result = testSubject.getComponent(componentUid, lastModificationTime, null);
        assertEquals(Either.right(ActionStatus.CONVERT_COMPONENT_ERROR), result);
    }

    @Test
    public void testGetAllComponentIdTimeAndType() throws Exception {

        Either<List<ComponentCacheData>, ActionStatus> result;

        // default test

        result = testSubject.getAllComponentIdTimeAndType();
        assertEquals(null, result);
        testSubject.setEnabled(false);
        result = testSubject.getAllComponentIdTimeAndType();
        assertEquals(Either.right(ActionStatus.NOT_ALLOWED), result);
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
        List<Component> result;

        // default test
        testSubject.init();
        result = Deencapsulation.invoke(testSubject, "getDataFromInMemoryCache", components,
                ComponentTypeEnum.RESOURCE);
        assertNotNull(result);
        assertTrue(result.isEmpty());
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
        assertTrue(result.isLeft());
        assertTrue(result.left().value().left.isEmpty());
        assertTrue(result.left().value().middle.isEmpty());
        assertTrue(result.left().value().right.isEmpty());
    }

    @Test
    public void testGetComponentsNotAllowed() throws Exception {

        Set<String> components = new HashSet<>();

        Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> result;

        // default test
        testSubject.setEnabled(false);
        result = testSubject.getComponents(components, null);
        assertEquals(Either.right(ActionStatus.NOT_ALLOWED), result);
    }

    @Test
    public void testGetComponentsCassndraError() throws Exception {

        Set<String> components = new HashSet<>();
        Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> result;

        Mockito.when(componentCassandraDao.getComponents(Mockito.any(List.class)))
                .thenReturn(Either.right(ActionStatus.GENERAL_ERROR));

        // default test
        testSubject.init();
        result = testSubject.getComponents(components, null);
        assertEquals(Either.right(ActionStatus.GENERAL_ERROR), result);
    }

    @Test
    public void testGetComponentsForLeftPanel() throws Exception {

        String internalComponentType = "mock";
        Set<String> filteredResources = new HashSet<>();
        Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> result;

        List<ComponentCacheData> list = new LinkedList<>();
        Mockito.when(componentCassandraDao.getComponents(Mockito.any(List.class))).thenReturn(Either.left(list));

        // default test
        result = testSubject.getComponentsForLeftPanel(ComponentTypeEnum.RESOURCE, internalComponentType,
                filteredResources);
        assertTrue(result.isLeft());
        assertTrue(result.left().value().left.isEmpty());
        assertTrue(result.left().value().middle.isEmpty());
        assertTrue(result.left().value().right.isEmpty());
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
        String shouldBeCopied = "shouldBeCopied";
        String shouldNotBe = "shouldNotBeCopied";

        // default test
        Resource resource = new Resource();
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        resource.setDescription(shouldBeCopied);
        resource.setContactId(shouldNotBe);
        result = Deencapsulation.invoke(testSubject, "filterFieldsForLeftPanel", resource);
        assertEquals(ComponentTypeEnum.RESOURCE, result.getComponentType());
        assertEquals(shouldBeCopied, result.getDescription());
        assertNull(result.getContactId());
        Service service = new Service();
        service.setComponentType(ComponentTypeEnum.SERVICE);
        service.setSystemName(shouldBeCopied);
        service.setCreatorFullName(shouldNotBe);
        result = Deencapsulation.invoke(testSubject, "filterFieldsForLeftPanel", service);
        assertEquals(ComponentTypeEnum.SERVICE, result.getComponentType());
        assertEquals(shouldBeCopied, result.getSystemName());
        assertNull(result.getCreatorFullName());
    }

    @Test
    public void testFilterFieldsForCatalog() throws Exception {
        Component result;

        // default test

        Resource resource = new Resource();
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        result = Deencapsulation.invoke(testSubject, "filterFieldsForCatalog", resource);
        assertEquals(ComponentTypeEnum.RESOURCE, result.getComponentType());
        Service service = new Service();
        service.setComponentType(ComponentTypeEnum.SERVICE);
        result = Deencapsulation.invoke(testSubject, "filterFieldsForCatalog", service);
        assertEquals(ComponentTypeEnum.SERVICE, result.getComponentType());
        Product product = new Product();
        product.setComponentType(ComponentTypeEnum.PRODUCT);
        result = Deencapsulation.invoke(testSubject, "filterFieldsForCatalog", product);
        assertEquals(ComponentTypeEnum.PRODUCT, result.getComponentType());
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
        assertEquals(Either.right(ActionStatus.NOT_ALLOWED), result);
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
        assertTrue(result.isLeft());
        assertTrue(result.left().value().left.isEmpty());
        assertTrue(result.left().value().middle.isEmpty());
        assertEquals(filteredResources, result.left().value().right);
    }


    @Test
    public void testGetComponent_1() throws Exception {

        String componentUid = "mock";
        Either<Component, ActionStatus> result;

        Mockito.when(componentCassandraDao.getComponent("mock"))
                .thenReturn(Either.right(ActionStatus.ARTIFACT_NOT_FOUND));

        // default test
        result = testSubject.getComponent(componentUid);
        assertEquals(Either.right(ActionStatus.ARTIFACT_NOT_FOUND), result);
    }

    @Test
    public void testGetComponent_2() throws Exception {

        String componentUid = "mock";
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
        result = testSubject.getComponent(componentUid, null, filterFieldsFunc);
        assertEquals(Either.right(ActionStatus.ARTIFACT_NOT_FOUND), result);
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
        assertFalse(result);
    }

    @Test
    public void testSetComponent_1Disabled() throws Exception {

        Component component = new Resource();
        component.setLastUpdateDate(0L);
        boolean result;

        // default test
        testSubject.setEnabled(false);
        result = testSubject.setComponent(component, NodeTypeEnum.Resource);
        assertFalse(result);
    }

    @Test
    public void testSetComponent_1() throws Exception {

        Component component = new Resource();
        component.setLastUpdateDate(0L);
        boolean result;

        // default test

        result = testSubject.setComponent(component, NodeTypeEnum.Resource);
        assertFalse(result);
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
        assertTrue(result.isLeft());
        assertTrue(result.left().value().left.isEmpty());
        assertEquals(1, result.left().value().right.size());
        assertNull(result.left().value().right.iterator().next());
    }

    @Test
    public void testGetComponentsFull_1Disabled() throws Exception {
        Map<String, Long> filteredResources = new HashMap<>();
        Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> result;

        // default test
        testSubject.setEnabled(false);
        result = Deencapsulation.invoke(testSubject, "getComponentsFull", filteredResources);
        assertEquals(Either.right(ActionStatus.NOT_ALLOWED), result);
    }

    @Test
    public void testGetComponentsFull_1NotFound() throws Exception {
        Map<String, Long> filteredResources = new HashMap<>();
        Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> result;

        // default test
        Mockito.when(componentCassandraDao.getComponents(Mockito.any(Map.class))).thenReturn(Either.right(ActionStatus.ARTIFACT_NOT_FOUND));

        result = Deencapsulation.invoke(testSubject, "getComponentsFull", filteredResources);
        assertEquals(Either.right(ActionStatus.ARTIFACT_NOT_FOUND), result);
    }

    @Test
    public void testGetComponentsForCatalog_1Disabled() throws Exception {

        Map<String, Long> components = null;
        Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> result;

        // default test
        testSubject.setEnabled(false);
        result = testSubject.getComponentsForCatalog(components, ComponentTypeEnum.RESOURCE);
        assertEquals(Either.right(ActionStatus.NOT_ALLOWED), result);
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
        assertTrue(result.isLeft());
        assertTrue(result.left().value().left.isEmpty());
        assertTrue(result.left().value().right.isEmpty());
    }

    @Test
    public void testGetComponentsForCatalog_1Error() throws Exception {
        Map<String, Long> components = new HashMap<>();
        Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> result;

        // default test
        Mockito.when(componentCassandraDao.getComponents(Mockito.any(Map.class))).thenReturn(Either.right(ActionStatus.COMPONENT_NOT_FOUND));

        result = testSubject.getComponentsForCatalog(components, ComponentTypeEnum.RESOURCE);
        assertEquals(Either.right(ActionStatus.COMPONENT_NOT_FOUND), result);
    }

    @Test
    public void testGetComponents_1Disabled() throws Exception {

        Map<String, Long> components = null;
        Function<List<Component>, List<Component>> filterFieldsFunc = null;
        Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> result;

        // default test
        testSubject.setEnabled(false);
        result = testSubject.getComponents(components, filterFieldsFunc);
        assertEquals(Either.right(ActionStatus.NOT_ALLOWED), result);
    }

    @Test
    public void testGetComponentAndTimeNotFound() throws Exception {

        String componentUid = "";
        Either<ImmutablePair<Component, Long>, ActionStatus> result;

        // default test
        Mockito.when(componentCassandraDao.getComponent(Mockito.anyString())).thenReturn(Either.right(ActionStatus.API_RESOURCE_NOT_FOUND));

        result = testSubject.getComponentAndTime(componentUid, null);
        assertEquals(Either.right(ActionStatus.API_RESOURCE_NOT_FOUND), result);
    }

    @Test
    public void testGetComponentFromCacheDisabled() throws Exception {
        String componentUid = "";
        Either<ImmutablePair<Component, ComponentCacheData>, ActionStatus> result;

        // test 1
        testSubject.setEnabled(false);
        result = Deencapsulation.invoke(testSubject, "getComponentFromCache",
                new Object[]{componentUid, Long.class, Function.class});
        assertEquals(Either.right(ActionStatus.NOT_ALLOWED), result);
    }

    @Test
    public void testDeleteComponentFromCacheFails() throws Exception {

        String id = "";
        ActionStatus result;

        // default test

        result = testSubject.deleteComponentFromCache(id);
        assertEquals(ActionStatus.GENERAL_ERROR, result);
    }

    @Test
    public void testDeleteComponentFromCacheDisabled() throws Exception {

        String id = "";
        ActionStatus result;

        // default test
        testSubject.setEnabled(false);
        result = testSubject.deleteComponentFromCache(id);
        assertEquals(ActionStatus.NOT_ALLOWED, result);
    }

    @Test
    public void testDeleteComponentFromCache() throws Exception {

        String id = "";
        ActionStatus result;

        // default test
        Mockito.when(componentCassandraDao.deleteComponent(Mockito.anyString())).thenReturn(CassandraOperationStatus.OK);
        result = testSubject.deleteComponentFromCache(id);
        assertEquals(ActionStatus.OK, result);
    }
}