package org.openecomp.sdc.be.components.merge.utils;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MergeInstanceUtilsTest {
    private MergeInstanceUtils mergeInstanceUtils = new MergeInstanceUtils();

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Before
    public void startUp() {
        mergeInstanceUtils.setToscaOperationFacade(toscaOperationFacade);
    }

    @Test
    public void testMapOldToNewCapabilitiesOwnerIdsComponentComponent() {
        Component container = new Service();

        Resource vfOld = new Resource();
        vfOld.setComponentType(ComponentTypeEnum.RESOURCE);
        vfOld.setResourceType(ResourceTypeEnum.VF);

        ComponentInstance vfci1 = createComponentInstance("SRV1.VF1.VFI_1.VFC1.VFCI_1" ,"SRV1.VF1.VFI_1.VFC1", false);
        createVfc(vfci1);
        List<ComponentInstance> vfciList = Arrays.asList(vfci1);
        vfOld.setComponentInstances(vfciList);

        ComponentInstance vfi1 = createComponentInstance("SRV1.VF2.VFI_1" ,"SRV1.VF2", false);
        container.setComponentInstances(Arrays.asList(vfi1));

        Resource vfNew = new Resource();
        vfNew.setComponentType(ComponentTypeEnum.RESOURCE);
        vfNew.setResourceType(ResourceTypeEnum.VF);

        List<ComponentInstance> vfciList2 = Arrays.asList(vfci1);
        vfNew.setComponentInstances(vfciList2);

        Either<Component, StorageOperationStatus> eitherComponent = Either.left(vfNew);
        when(toscaOperationFacade.getToscaElement(vfi1.getComponentUid())).thenReturn(eitherComponent);


        Map<String, String> mapResult = mergeInstanceUtils.mapOldToNewCapabilitiesOwnerIds(container, vfOld, "SRV1.VF2.VFI_1", Arrays.asList("SRV1.VF1.VFI_1.VFC1.VFCI_1"));
        assertEquals("SRV1.VF1.VFI_1.VFC1.VFCI_1", mapResult.get("SRV1.VF1.VFI_1.VFC1.VFCI_1"));
    }

    @Test
    public void testMapOldToNewCapabilitiesOwnerIdsComponentComponent_Proxy() {
        Resource container = new Resource();
        container.setComponentType(ComponentTypeEnum.RESOURCE);
        container.setResourceType(ResourceTypeEnum.VF);


        Component serviceOld = new Service();
        serviceOld.setComponentType(ComponentTypeEnum.SERVICE);

        Resource vfOld = new Resource();
        vfOld.setComponentType(ComponentTypeEnum.RESOURCE);
        vfOld.setResourceType(ResourceTypeEnum.VF);

        ComponentInstance vfciOld = createComponentInstance("SRV1.VF1.VFI_1.VFC1.VFCI_1" ,"SRV1.VF1.VFI_1.VFC1", false);
        createVfc(vfciOld);
        List<ComponentInstance> vfciList = Arrays.asList(vfciOld);
        vfOld.setComponentInstances(vfciList);

        ComponentInstance vfiOld = createComponentInstance("SRV1.VF1.VFI_1" ,"SRV1.VF1", false);
        serviceOld.setComponentInstances(Arrays.asList(vfiOld));
        Either<Component, StorageOperationStatus> eitherComponent = Either.left(vfOld);
        when(toscaOperationFacade.getToscaElement(vfiOld.getComponentUid())).thenReturn(eitherComponent);

        Component serviceNew = new Service();
        serviceNew.setComponentType(ComponentTypeEnum.SERVICE);

        Resource vfNew = new Resource();
        vfNew.setComponentType(ComponentTypeEnum.RESOURCE);
        vfNew.setResourceType(ResourceTypeEnum.VF);

        ComponentInstance vfciNew = createComponentInstance("SRV1.VF2.VFI_1.VFC2.VFCI_1" ,"SRV1.VF2.VFI_1.VFC2", false);
        createVfc(vfciNew);
        List<ComponentInstance> vfciList2 = Arrays.asList(vfciNew);
        vfNew.setComponentInstances(vfciList2);

        ComponentInstance vfiNew = createComponentInstance("SRV1.VF2.VFI_1" ,"SRV1.VF2", false);
        serviceNew.setComponentInstances(Arrays.asList(vfiNew));
        Either<Component, StorageOperationStatus> eitherComponent3 = Either.left(vfNew);
        when(toscaOperationFacade.getToscaElement(vfiNew.getComponentUid())).thenReturn(eitherComponent3);

        ComponentInstance proxyVfciNew = createComponentInstance("SRV1.PROXY_VFC_NEW.VFCI1", "SRV1.PROXY_VFC_NEW", true);
        proxyVfciNew.setSourceModelUid("SRV_PROXY_NEW");
        Resource proxyVfcNew = new Resource();
        proxyVfcNew.setComponentType(ComponentTypeEnum.RESOURCE);
        proxyVfcNew.setResourceType(ResourceTypeEnum.VFC);

        Either<Component, StorageOperationStatus> eitherComponent4 = Either.left(serviceNew);
        when(toscaOperationFacade.getToscaElement(proxyVfciNew.getSourceModelUid())).thenReturn(eitherComponent4);

        container.setComponentInstances(Arrays.asList(proxyVfciNew));

        Map<String, String> mapResult = mergeInstanceUtils.mapOldToNewCapabilitiesOwnerIds(container, serviceOld, "SRV1.PROXY_VFC_NEW.VFCI1", Arrays.asList("SRV1.VF1.VFI_1.VFC1.VFCI_1"));

        assertEquals("SRV1.VF2.VFI_1.VFC2.VFCI_1", mapResult.get("SRV1.VF1.VFI_1.VFC1.VFCI_1"));
    }

    @Test
    public void testMapOldToNewCapabilitiesOwnerIdsComponentInstanceComponentInstance() {
        ComponentInstance oldInstance = createComponentInstance("SRV1.VF1.VFI_1.VFC1.VFCI_1" ,"SRV1.VF1.VFI_1.VFC1", false);
        createVfc(oldInstance);

        ComponentInstance newInstance = createComponentInstance("SRV1.VF1.VFI_1.VFC2.VFCI_1" ,"SRV1.VF1.VFI_1.VFC2", false);
        createVfc(newInstance);

        Map<String, String> mapResult = mergeInstanceUtils.mapOldToNewCapabilitiesOwnerIds(oldInstance, newInstance);
        assertEquals("SRV1.VF1.VFI_1.VFC2.VFCI_1", mapResult.get("SRV1.VF1.VFI_1.VFC1.VFCI_1"));
    }

    @Test
    public void testMapOldToNewCapabilitiesOwnerIdsComponentInstComponentInst_Proxy() {

        Component serviceOld = new Service();
        serviceOld.setComponentType(ComponentTypeEnum.SERVICE);

        Resource vfOld = new Resource();
        vfOld.setComponentType(ComponentTypeEnum.RESOURCE);
        vfOld.setResourceType(ResourceTypeEnum.VF);

        ComponentInstance vfciOld = createComponentInstance("SRV1.VF1.VFI_1.VFC1.VFCI_1" ,"SRV1.VF1.VFI_1.VFC1", false);
        createVfc(vfciOld);
        List<ComponentInstance> vfciList = Arrays.asList(vfciOld);
        vfOld.setComponentInstances(vfciList);

        ComponentInstance vfiOld = createComponentInstance("SRV1.VF1.VFI_1" ,"SRV1.VF1", false);
        serviceOld.setComponentInstances(Arrays.asList(vfiOld));
        Either<Component, StorageOperationStatus> eitherComponent = Either.left(vfOld);
        when(toscaOperationFacade.getToscaElement(vfiOld.getComponentUid())).thenReturn(eitherComponent);

        ComponentInstance proxyVfciOld = createComponentInstance("SRV1.PROXY_VFC.VFCI1", "SRV1.PROXY_VFC", true);
        proxyVfciOld.setSourceModelUid("SRV_PROXY");
        Resource proxyVfcOld = new Resource();
        proxyVfcOld.setComponentType(ComponentTypeEnum.RESOURCE);
        proxyVfcOld.setResourceType(ResourceTypeEnum.VFC);

        Either<Component, StorageOperationStatus> eitherComponent2 = Either.left(serviceOld);
        when(toscaOperationFacade.getToscaElement(proxyVfciOld.getSourceModelUid())).thenReturn(eitherComponent2);


        Component serviceNew = new Service();
        serviceNew.setComponentType(ComponentTypeEnum.SERVICE);

        Resource vfNew = new Resource();
        vfNew.setComponentType(ComponentTypeEnum.RESOURCE);
        vfNew.setResourceType(ResourceTypeEnum.VF);

        ComponentInstance vfciNew = createComponentInstance("SRV1.VF2.VFI_1.VFC2.VFCI_1" ,"SRV1.VF2.VFI_1.VFC2", false);
        createVfc(vfciNew);
        List<ComponentInstance> vfciList2 = Arrays.asList(vfciNew);
        vfNew.setComponentInstances(vfciList2);

        ComponentInstance vfiNew = createComponentInstance("SRV1.VF2.VFI_1" ,"SRV1.VF2", false);
        serviceNew.setComponentInstances(Arrays.asList(vfiNew));
        Either<Component, StorageOperationStatus> eitherComponent3 = Either.left(vfNew);
        when(toscaOperationFacade.getToscaElement(vfiNew.getComponentUid())).thenReturn(eitherComponent3);

        ComponentInstance proxyVfciNew = createComponentInstance("SRV1.PROXY_VFC_NEW.VFCI1", "SRV1.PROXY_VFC_NEW", true);
        proxyVfciNew.setSourceModelUid("SRV_PROXY_NEW");
        Resource proxyVfcNew = new Resource();
        proxyVfcNew.setComponentType(ComponentTypeEnum.RESOURCE);
        proxyVfcNew.setResourceType(ResourceTypeEnum.VFC);

        Either<Component, StorageOperationStatus> eitherComponent4 = Either.left(serviceNew);
        when(toscaOperationFacade.getToscaElement(proxyVfciNew.getSourceModelUid())).thenReturn(eitherComponent4);

        Map<String, String> mapResult = mergeInstanceUtils.mapOldToNewCapabilitiesOwnerIds(proxyVfciOld, proxyVfciNew);
        assertEquals("SRV1.VF2.VFI_1.VFC2.VFCI_1", mapResult.get("SRV1.VF1.VFI_1.VFC1.VFCI_1"));
    }


    @Test
    public void testConvertToVfciNameMap() {
        ComponentInstance componentInstance1 = new ComponentInstance();
        componentInstance1.setName("ComponentInstance1");

        ComponentInstance componentInstance2 = new ComponentInstance();
        componentInstance2.setName("ComponentInstance2");

        List<ComponentInstance> componentInstances = Arrays.asList(componentInstance1, componentInstance2);
        Map<String, ComponentInstance> mapResult = mergeInstanceUtils.convertToVfciNameMap(componentInstances);

        assertEquals(2, mapResult.size());
        assertEquals(componentInstance1, mapResult.get("ComponentInstance1"));
        assertEquals(componentInstance2, mapResult.get("ComponentInstance2"));
    }

    @Test
    public void testGetVfcInstances_NullComponentInstance() {
        List<ComponentInstance> vfcInstances = mergeInstanceUtils.getVfcInstances(null);

        assertTrue(vfcInstances.isEmpty());
    }

    @Test
    public void testGetVfcInstances_ComponentInstanceFailedLoadComponent() {
        ComponentInstance componentInstance = createComponentInstance("SRV1.VF1.VFI_1", "SRV1.VF1", false);

        Either<Component, StorageOperationStatus> eitherComponent = Either.right(StorageOperationStatus.NOT_FOUND);
        when(toscaOperationFacade.getToscaElement(componentInstance.getComponentUid())).thenReturn(eitherComponent);

        List<ComponentInstance> vfcInstances = mergeInstanceUtils.getVfcInstances(componentInstance);

        assertTrue(vfcInstances.isEmpty());
    }

    @Test
    public void testGetVfcInstances_ComponentInstanceFailedLoadActualComponent() {
        ComponentInstance componentInstance = createComponentInstance("SRV1.PROXY_VFC.VFCI1", "SRV1.PROXY_VFC", true);
        componentInstance.setSourceModelUid("SRV_PROXY");

        Either<Component, StorageOperationStatus> eitherComponent = Either.right(StorageOperationStatus.NOT_FOUND);
        when(toscaOperationFacade.getToscaElement(componentInstance.getSourceModelUid())).thenReturn(eitherComponent);

        List<ComponentInstance> vfcInstances = mergeInstanceUtils.getVfcInstances(componentInstance);

        assertTrue(vfcInstances.isEmpty());
    }


    @Test
    public void testGetVfcInstancesAtomicComponentInstance() {
        ComponentInstance componentInstance = createComponentInstance("SRV1.VF1.VFI_1.VFC1.VFCI_1" ,"SRV1.VF1.VFI_1.VFC1", false);

        createVfc(componentInstance);

        List<ComponentInstance> vfcInstances = mergeInstanceUtils.getVfcInstances(componentInstance);

        assertEquals(1, vfcInstances.size());
        assertEquals(componentInstance, vfcInstances.get(0));
    }


    @Test
    public void testGetVfcInstancesNonAtomicComponentInstance() {
        ComponentInstance componentInstance = createComponentInstance("SRV1.VF1.VFI_1", "SRV1.VF1", false);

        Resource vf = new Resource();
        Either<Component, StorageOperationStatus> eitherComponent = Either.left(vf);
        vf.setComponentType(ComponentTypeEnum.RESOURCE);
        vf.setResourceType(ResourceTypeEnum.VF);

        ComponentInstance vfci = createComponentInstance("SRV1.VF1.VFI_1.VFC1.VFCI_1" ,"SRV1.VF1.VFI_1.VFC1", false);
        createVfc(vfci);
        List<ComponentInstance> vfciList = Arrays.asList(vfci);

        vf.setComponentInstances(vfciList);
        when(toscaOperationFacade.getToscaElement(componentInstance.getComponentUid())).thenReturn(eitherComponent);

        List<ComponentInstance> vfcInstances = mergeInstanceUtils.getVfcInstances(componentInstance);

        assertEquals(vfciList, vfcInstances);
    }

    @Test
    public void testGetVfcInstances_ComponentNullI_nstanceComponent() {
        List<ComponentInstance> vfcInstances = mergeInstanceUtils.getVfcInstances(null, new Resource());

        assertTrue(vfcInstances.isEmpty());
    }

    @Test
    public void testGetVfcInstances_ComponentInstance_NullComponent() {
        List<ComponentInstance> vfcInstances = mergeInstanceUtils.getVfcInstances(new ComponentInstance(), null);

        assertTrue(vfcInstances.isEmpty());
    }

    @Test
    public void testGetVfcInstances_NullComponentInstance_NullComponent() {
        List<ComponentInstance> vfcInstances = mergeInstanceUtils.getVfcInstances(null, null);

        assertTrue(vfcInstances.isEmpty());
    }


    /**
     * @param uniqueId
     * @param componentUid
     * @param isProxy
     * @return
     */
    private ComponentInstance createComponentInstance(String uniqueId, String componentUid, boolean isProxy) {
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(uniqueId);
        componentInstance.setComponentUid(componentUid);
        componentInstance.setIsProxy(isProxy);
        return componentInstance;
    }

    /**
     * @param componentInstance
     */
    private void createVfc(ComponentInstance componentInstance) {
        Resource vfc = new Resource();
        Either<Component, StorageOperationStatus> eitherComponent = Either.left(vfc);
        vfc.setComponentType(ComponentTypeEnum.RESOURCE);
        vfc.setResourceType(ResourceTypeEnum.VFC);
        when(toscaOperationFacade.getToscaElement(componentInstance.getComponentUid())).thenReturn(eitherComponent);
    }
}
