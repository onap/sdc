package org.openecomp.sdc.be.components.merge.utils;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.utils.ExceptionUtils;
import org.openecomp.sdc.be.components.utils.ComponentInstanceBuilder;
import org.openecomp.sdc.be.components.utils.GroupDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.components.utils.ServiceBuilder;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MergeInstanceUtilsTest {

    private MergeInstanceUtils mergeInstanceUtils;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private JanusGraphDao janusGraphDao;

    @Before
    public void startUp() {
        ExceptionUtils exceptionUtils = new ExceptionUtils(janusGraphDao);
        mergeInstanceUtils = new MergeInstanceUtils(toscaOperationFacade, exceptionUtils);
    }

    @Test
    public void testMapOldToNewCapabilitiesOwnerIdsComponentComponent() {
        Component container = new Service();

        ComponentInstance vfciOld = createVfcInstance("SRV1.VF1.VFI_1.VFC1.VFCI_1", "SRV1.VF1.VFI_1.VFC1");
        GroupDefinition oldGrp = createGroup("group1", "grp1Id");
        Component vfOld = createVf("prevVfId", vfciOld, oldGrp);
        ComponentInstance vfi1 = createComponentInstance("SRV1.VF2.VFI_1" ,"SRV1.VF2", false);
        container.setComponentInstances(singletonList(vfi1));

        ComponentInstance vfciNew = createComponentInstance("SRV1.VF1.VFI_1.VFC1.VFCI_2", "SRV1.VF1.VFI_1.VFC1", false);
        GroupDefinition newGrp = createGroup("group1", "newGrp1Id");
        Component vfNew = createVf("newVfId", vfciNew, newGrp);
        when(toscaOperationFacade.getToscaElement(vfi1.getComponentUid())).thenReturn(Either.left(vfNew));

        Map<String, String> mapResult = mergeInstanceUtils.mapOldToNewCapabilitiesOwnerIds(container, vfOld, "SRV1.VF2.VFI_1", asList("SRV1.VF1.VFI_1.VFC1.VFCI_1", oldGrp.getUniqueId()));
        assertThat(mapResult)
                .containsEntry("SRV1.VF1.VFI_1.VFC1.VFCI_1", "SRV1.VF1.VFI_1.VFC1.VFCI_2")
                .containsEntry(oldGrp.getUniqueId(), newGrp.getUniqueId());
    }

    @Test
    public void testMapOldToNewCapabilitiesOwnerIdsComponentComponent_Proxy() {
        Resource container = new Resource();
        container.setComponentType(ComponentTypeEnum.RESOURCE);
        container.setResourceType(ResourceTypeEnum.VF);

        Component serviceOld = new Service();
        serviceOld.setComponentType(ComponentTypeEnum.SERVICE);

        ComponentInstance vfciOld = createVfcInstance("SRV1.VF1.VFI_1.VFC1.VFCI_1", "SRV1.VF1.VFI_1.VFC1");
        GroupDefinition prevGroup = createGroup("grp1", "grp1Old");
        ComponentInstance vfiOld = createVfInstance("SRV1.VF1.VFI_1", vfciOld, prevGroup);
        serviceOld.setComponentInstances(singletonList(vfiOld));

        Component serviceNew = new Service();
        serviceNew.setComponentType(ComponentTypeEnum.SERVICE);

        ComponentInstance vfciNew = createVfcInstance("SRV1.VF2.VFI_1.VFC2.VFCI_1", "SRV1.VF2.VFI_1.VFC2");
        GroupDefinition grpNew = createGroup("grp1", "newGrp1");
        ComponentInstance vfiNew = createVfInstance("SRV1.VF2.VFI_1" ,vfciNew, grpNew);
        serviceNew.setComponentInstances(singletonList(vfiNew));

        ComponentInstance proxyVfciNew = createComponentInstance("SRV1.PROXY_VFC_NEW.VFCI1", "SRV1.PROXY_VFC_NEW", true);
        proxyVfciNew.setSourceModelUid("SRV_PROXY_NEW");
        Resource proxyVfcNew = new Resource();
        proxyVfcNew.setComponentType(ComponentTypeEnum.RESOURCE);
        proxyVfcNew.setResourceType(ResourceTypeEnum.VFC);

        Either<Component, StorageOperationStatus> eitherComponent4 = Either.left(serviceNew);
        when(toscaOperationFacade.getToscaElement(proxyVfciNew.getSourceModelUid())).thenReturn(eitherComponent4);

        container.setComponentInstances(singletonList(proxyVfciNew));

        Map<String, String> mapResult = mergeInstanceUtils.mapOldToNewCapabilitiesOwnerIds(container, serviceOld, "SRV1.PROXY_VFC_NEW.VFCI1", asList("SRV1.VF1.VFI_1.VFC1.VFCI_1", prevGroup.getUniqueId()));
        assertThat(mapResult)
                .containsEntry("SRV1.VF1.VFI_1.VFC1.VFCI_1", "SRV1.VF2.VFI_1.VFC2.VFCI_1")
                .containsEntry(prevGroup.getUniqueId(), grpNew.getUniqueId());
    }

    @Test
    public void whenFailingToGetInstanceOriginNodeType_throwExceptionAndRollBack() {
        Resource oldVf = new ResourceBuilder()
                .setResourceType(ResourceTypeEnum.VF)
                .setComponentType(ComponentTypeEnum.RESOURCE)
                .build();

        ComponentInstance newVfInstance = createComponentInstance("inst1", "inst1Uid", false);
        Resource container = new ResourceBuilder()
                .addComponentInstance(newVfInstance)
                .build();
        when(toscaOperationFacade.getToscaElement("inst1Uid")).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        assertThatExceptionOfType(StorageException.class)
                .isThrownBy(() -> mergeInstanceUtils.mapOldToNewCapabilitiesOwnerIds(container, oldVf, "inst1", emptyList()));
        verify(janusGraphDao).rollback();
    }

    @Test
    public void testMapOldToNewCapabilitiesOwnerIdsComponentInstanceComponentInstance() {
        ComponentInstance oldInstance = createVfcInstance("SRV1.VF1.VFI_1.VFC1.VFCI_1", "SRV1.VF1.VFI_1.VFC1");
        ComponentInstance newInstance = createVfcInstance("SRV1.VF1.VFI_1.VFC2.VFCI_1", "SRV1.VF1.VFI_1.VFC2");
        Map<String, String> mapResult = mergeInstanceUtils.mapOldToNewCapabilitiesOwnerIds(oldInstance, newInstance);
        assertEquals("SRV1.VF1.VFI_1.VFC2.VFCI_1", mapResult.get("SRV1.VF1.VFI_1.VFC1.VFCI_1"));
    }

    @Test
    public void testMapOldToNewCapabilitiesOwnerIdsInstToInstWithGroups() {
        ComponentInstance prevInstance = createVfcInstance("prevInst1", "prevInst1Uid");
        GroupDefinition prevGroup = createGroup("grp1", "prevGrp1");
        ComponentInstance prevInstanceRoot = createVfInstance("prevId", prevInstance, prevGroup);

        ComponentInstance currInstance = createVfcInstance("newInst1", "newInst1Uid");
        GroupDefinition currGroup = createGroup("grp1", "currGrp1");
        ComponentInstance newInstanceRoot = createVfInstance("currId", currInstance, currGroup);

        Map<String, String> mapResult = mergeInstanceUtils.mapOldToNewCapabilitiesOwnerIds(prevInstanceRoot, newInstanceRoot);
        assertThat(mapResult)
                .containsEntry(prevInstance.getUniqueId(), currInstance.getUniqueId())
                .containsEntry(prevGroup.getUniqueId(), currGroup.getUniqueId());
    }


    @Test
    public void testMapOldToNewCapabilitiesOwnerIdsComponentInstComponentInst_Proxy() {

        ComponentInstance vfciOld = createVfcInstance("SRV1.VF1.VFI_1.VFC1.VFCI_1", "SRV1.VF1.VFI_1.VFC1");
        GroupDefinition oldGrp = createGroup("grp1", "grp1Old");
        ComponentInstance vfiOld = createVfInstance("SRV1.VF1.VFI_1", vfciOld, oldGrp);

        Component serviceOld = new Service();
        serviceOld.setComponentType(ComponentTypeEnum.SERVICE);
        serviceOld.setComponentInstances(singletonList(vfiOld));

        ComponentInstance proxyVfciOld = createComponentInstance("SRV1.PROXY_VFC.VFCI1", "SRV1.PROXY_VFC", true);
        proxyVfciOld.setSourceModelUid("SRV_PROXY");
        Resource proxyVfcOld = new Resource();
        proxyVfcOld.setComponentType(ComponentTypeEnum.RESOURCE);
        proxyVfcOld.setResourceType(ResourceTypeEnum.VFC);

        Either<Component, StorageOperationStatus> eitherComponent2 = Either.left(serviceOld);
        when(toscaOperationFacade.getToscaElement(proxyVfciOld.getSourceModelUid())).thenReturn(eitherComponent2);

        ComponentInstance vfciNew = createVfcInstance("SRV1.VF2.VFI_1.VFC2.VFCI_1", "SRV1.VF2.VFI_1.VFC2");
        GroupDefinition newGrp = createGroup("grp1", "grp1New");
        ComponentInstance vfiNew = createVfInstance("SRV1.VF2.VFI_1" ,vfciNew, newGrp);

        Component serviceNew = new Service();
        serviceNew.setComponentType(ComponentTypeEnum.SERVICE);
        serviceNew.setComponentInstances(singletonList(vfiNew));

        ComponentInstance proxyVfciNew = createComponentInstance("SRV1.PROXY_VFC_NEW.VFCI1", "SRV1.PROXY_VFC_NEW", true);
        proxyVfciNew.setSourceModelUid("SRV_PROXY_NEW");
        Resource proxyVfcNew = new Resource();
        proxyVfcNew.setComponentType(ComponentTypeEnum.RESOURCE);
        proxyVfcNew.setResourceType(ResourceTypeEnum.VFC);

        Either<Component, StorageOperationStatus> eitherComponent4 = Either.left(serviceNew);
        when(toscaOperationFacade.getToscaElement(proxyVfciNew.getSourceModelUid())).thenReturn(eitherComponent4);

        Map<String, String> mapResult = mergeInstanceUtils.mapOldToNewCapabilitiesOwnerIds(proxyVfciOld, proxyVfciNew);
        assertThat(mapResult)
                .containsEntry("SRV1.VF1.VFI_1.VFC1.VFCI_1", "SRV1.VF2.VFI_1.VFC2.VFCI_1")
                .containsEntry(oldGrp.getUniqueId(), newGrp.getUniqueId());
    }


    @Test
    public void testGetInstanceAtomicBuildingBlocks_NullComponentInstance() {
        assertEmpty(mergeInstanceUtils.getInstanceAtomicBuildingBlocks(null));
    }

    @Test
    public void testgetInstanceAtomicBuildingBlocks_ComponentInstanceFailedLoadComponent() {
        ComponentInstance componentInstance = createComponentInstance("SRV1.VF1.VFI_1", "SRV1.VF1", false);
        Either<Component, StorageOperationStatus> eitherComponent = Either.right(StorageOperationStatus.NOT_FOUND);
        when(toscaOperationFacade.getToscaElement(componentInstance.getComponentUid())).thenReturn(eitherComponent);
        assertThatExceptionOfType(StorageException.class).isThrownBy(() -> mergeInstanceUtils.getInstanceAtomicBuildingBlocks(componentInstance));
    }

    @Test
    public void testGetInstanceAtomicBuildingBlocks_ComponentInstanceFailedLoadActualComponent() {
        ComponentInstance componentInstance = createComponentInstance("SRV1.PROXY_VFC.VFCI1", "SRV1.PROXY_VFC", true);
        componentInstance.setSourceModelUid("SRV_PROXY");
        Either<Component, StorageOperationStatus> eitherComponent = Either.right(StorageOperationStatus.NOT_FOUND);
        when(toscaOperationFacade.getToscaElement(componentInstance.getSourceModelUid())).thenReturn(eitherComponent);
        assertThatExceptionOfType(StorageException.class).isThrownBy(() -> mergeInstanceUtils.getInstanceAtomicBuildingBlocks(componentInstance));
    }


    @Test
    public void testGetAtomicBuildingBlocks() {
        ComponentInstance componentInstance = createVfcInstance("inst1", "inst1Uid");
        GroupDefinition group = createGroup("grp1", "grp1Id");
        ComponentInstance vfi = createVfInstance("vfi", componentInstance, group);
        ComponentInstanceBuildingBlocks instanceBuildingBlocks = mergeInstanceUtils.getInstanceAtomicBuildingBlocks(vfi);
        assertThat(instanceBuildingBlocks)
                .extracting("vfcInstances", "groups")
                .containsExactlyInAnyOrder(singletonList(componentInstance), singletonList(group));
    }

    @Test
    public void testGetAtomicBuildingBlocksComponentInstance_noGroups() {
        ComponentInstance componentInstance = createVfcInstance("SRV1.VF1.VFI_1.VFC1.VFCI_1", "SRV1.VF1.VFI_1.VFC1");
        ComponentInstanceBuildingBlocks instanceBuildingBlocks = mergeInstanceUtils.getInstanceAtomicBuildingBlocks(componentInstance);
        assertThat(instanceBuildingBlocks)
                .extracting("vfcInstances", "groups")
                .containsExactly(singletonList(componentInstance), emptyList());
    }

    @Test
    public void testGetAtomicBuildingBlocks_noInstances() {
        GroupDefinition group = createGroup("grp1", "grp1Id");
        ComponentInstance vfi = createVfInstance("vfi", null, group);
        ComponentInstanceBuildingBlocks instanceBuildingBlocks = mergeInstanceUtils.getInstanceAtomicBuildingBlocks(vfi);
        assertThat(instanceBuildingBlocks)
                .extracting("groups", "vfcInstances")
                .containsExactlyInAnyOrder(singletonList(group), emptyList());
    }

    @Test
    public void testGetAtomicBuildingBlocks_noDuplication() {
        GroupDefinition group1FirstCopy = createGroup("grp1", "grp1Id");
        GroupDefinition group1SecondCopy = createGroup("grp1", "grp1Id");

        ComponentInstance cmtInst1FirstCopy = createVfcInstance("inst1", "inst1Uid");
        ComponentInstance cmtInst1SecondCopy = createVfcInstance("inst1", "inst1Uid");

        ComponentInstance vfi = createVfInstance("vfi", cmtInst1FirstCopy, group1FirstCopy);
        ComponentInstance vfi2 = createVfInstance("vfi2", cmtInst1SecondCopy, group1SecondCopy);

        Service service = new ServiceBuilder()
                .addComponentInstance(vfi)
                .addComponentInstance(vfi2)
                .setUniqueId("service1")
                .build();

        ComponentInstance proxy = createServiceProxy("serviceProxy", service);
        ComponentInstanceBuildingBlocks instanceAtomicBuildingBlocks = mergeInstanceUtils.getInstanceAtomicBuildingBlocks(proxy);
        assertThat(instanceAtomicBuildingBlocks)
                .extracting("groups", "vfcInstances")
                .containsExactlyInAnyOrder(singletonList(group1FirstCopy), singletonList(cmtInst1FirstCopy));
    }

    @Test
    public void testGetAtomicBuildingBlocks_ComponentNull_InstanceComponent() {
        assertEmpty(mergeInstanceUtils.getInstanceAtomicBuildingBlocks(null, new Resource()));
    }

    @Test
    public void testGetInstanceAtomicBuildingBlocks_ComponentInstance_NullComponent() {
        assertEmpty(mergeInstanceUtils.getInstanceAtomicBuildingBlocks(new ComponentInstance(), null));
    }

    @Test
    public void testGetInstanceAtomicBuildingBlocks_NullComponentInstance_NullComponent() {
        assertEmpty(mergeInstanceUtils.getInstanceAtomicBuildingBlocks(null, null));
    }

    private void assertEmpty(ComponentInstanceBuildingBlocks componentInstanceBuildingBlocks) {
        assertThat(componentInstanceBuildingBlocks)
                .extracting("vfcInstances", "groups")
                .containsExactly(emptyList(), emptyList());
    }

    private ComponentInstance createVfcInstance(String instId, String instUid) {
        ComponentInstance vfci = createComponentInstance(instId, instUid, false);
        createVfc(vfci);
        return vfci;
    }

    private ComponentInstance createVfInstance(String id, ComponentInstance withInstance, GroupDefinition withGroup) {
        Component vf = createVf(id, withInstance, withGroup);
        ComponentInstance vfInstance = new ComponentInstanceBuilder().setComponentUid(vf.getUniqueId()).build();
        when(toscaOperationFacade.getToscaElement(id)).thenReturn(Either.left(vf));
        return vfInstance;
    }

    private ComponentInstance createServiceProxy(String id, Service fromService) {
        when(toscaOperationFacade.getToscaElement(fromService.getUniqueId())).thenReturn(Either.left(fromService));
        return createComponentInstance(id, fromService.getUniqueId(), true);
    }

    private Component createVf(String id, ComponentInstance instance, GroupDefinition group) {
        return new ResourceBuilder()
                .setResourceType(ResourceTypeEnum.VF)
                .setComponentType(ComponentTypeEnum.RESOURCE)
                .addGroup(group)
                .addComponentInstance(instance)
                .setUniqueId(id)
                .build();
    }

    private ComponentInstance createComponentInstance(String uniqueId, String componentUid, boolean isProxy) {
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(uniqueId);
        componentInstance.setIsProxy(isProxy);
        if (isProxy) {
            componentInstance.setSourceModelUid(componentUid);
        } else {
            componentInstance.setComponentUid(componentUid);
        }
        return componentInstance;
    }

    private void createVfc(ComponentInstance componentInstance) {
        Resource vfc = new Resource();
        Either<Component, StorageOperationStatus> eitherComponent = Either.left(vfc);
        vfc.setComponentType(ComponentTypeEnum.RESOURCE);
        vfc.setResourceType(ResourceTypeEnum.VFC);
        when(toscaOperationFacade.getToscaElement(componentInstance.getComponentUid())).thenReturn(eitherComponent);
    }

    private GroupDefinition createGroup(String invariantName, String id) {
        return GroupDefinitionBuilder.create()
                .setInvariantName(invariantName)
                .setUniqueId(id)
                .setName(id + "name")
                .build();

    }
}
