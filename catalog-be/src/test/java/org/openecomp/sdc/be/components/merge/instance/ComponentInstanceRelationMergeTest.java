package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.merge.utils.MergeInstanceUtils;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComponentInstanceRelationMergeTest {
    private ComponentInstanceRelationMerge compInstanceRelationMerge;
    @Mock
    private DataForMergeHolder dataHolder;
    private Component containerComponent;
    private Component updatedContainerComponent;
    private ComponentInstance currentResourceInstance;

    private RequirementCapabilityRelDef requirementDef1;
    private RequirementCapabilityRelDef requirementDef2;
    private RequirementCapabilityRelDef capabilityDef1;
    private RequirementCapabilityRelDef capabilityDef2;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private User user;

    @Captor
    ArgumentCaptor<VfRelationsMergeInfo> argumentCaptor;


    @Before
    public void startUp() {
        compInstanceRelationMerge = new ComponentInstanceRelationMerge();
        compInstanceRelationMerge.setToscaOperationFacade(toscaOperationFacade);

        MergeInstanceUtils mergeInstanceUtils = new MergeInstanceUtils();
        mergeInstanceUtils.setToscaOperationFacade(toscaOperationFacade);
        compInstanceRelationMerge.setMergeInstanceUtils(mergeInstanceUtils);

        containerComponent = new Service();
        List<RequirementCapabilityRelDef> resourceInstancesRelations = new ArrayList<>();

        requirementDef1 = createRequirementDef("SRV1.VF1.VFI_1", "SRV1.VF2.VFI_1", "SRV1.VF1.VFC_1.VFCI_1", "Requirement1");
        resourceInstancesRelations.add(requirementDef1);
        requirementDef2 = createRequirementDef("SRV1.VF1.VFI_1", "SRV1.VF3.VFI_1", "SRV1.VF1.VFC_2.VFCI_2", "Requirement2");
        resourceInstancesRelations.add(requirementDef2);


        capabilityDef1 = createCapabilityDef("SRV1.VF4.VFI_1", "SRV1.VF1.VFI_1", "SRV1.VF1.VFC_3.VFCI_3", "Capability3");
        resourceInstancesRelations.add(capabilityDef1);
        capabilityDef2 = createCapabilityDef("SRV1.VF5.VFI_1", "SRV1.VF1.VFI_1", "SRV1.VF1.VFC_4.VFCI_1", "Capability4");
        resourceInstancesRelations.add(capabilityDef2);

        containerComponent.setComponentInstancesRelations(resourceInstancesRelations );

        currentResourceInstance = new ComponentInstance();
        currentResourceInstance.setUniqueId("SRV1.VF1.VFI_1");
        currentResourceInstance.setComponentUid("SRV1.VF1");
        currentResourceInstance.setIsProxy(false);

        updatedContainerComponent = new Service();
        updatedContainerComponent.setUniqueId("123123123123123123");
    }

    @Test
    public void testSaveDataBeforeMerge() {
        Resource vf = new Resource();

        List<ComponentInstance> vfcInstances = new ArrayList<>();
        vfcInstances.add(createVfci("vfc_A", "SRV1.VF1.VFC_1.VFCI_1", "SRV1.VF1.VFC_1", true));
        vfcInstances.add(createVfci("vfc_B", "SRV1.VF1.VFC_2.VFCI_2", "SRV1.VF1.VFC_2", true));
        vfcInstances.add(createVfci("vfc_C", "SRV1.VF1.VFC_3.VFCI_3", "SRV1.VF1.VFC_3", false));
        vfcInstances.add(createVfci("vfc_D", "SRV1.VF1.VFC_4.VFCI_1", "SRV1.VF1.VFC_4", true));
        vf.setComponentInstances(vfcInstances);
        vf.setComponentType(ComponentTypeEnum.RESOURCE);
        vf.setResourceType(ResourceTypeEnum.VF);

        compInstanceRelationMerge.saveDataBeforeMerge(dataHolder, containerComponent, currentResourceInstance, vf);

        verify(dataHolder).setVfRelationsInfo(argumentCaptor.capture());
        VfRelationsMergeInfo relationsMergeInfo = argumentCaptor.getValue();
        List<RelationMergeInfo> fromRelationsMergeInfo = relationsMergeInfo.getFromRelationsInfo();
        List<RelationMergeInfo> toRelationsMergeInfo = relationsMergeInfo.getToRelationsInfo();

        assertNotNull("Expected not null list of relations merge info", fromRelationsMergeInfo);
        assertNotNull("Expected not null list of relations merge info", toRelationsMergeInfo);

        assertEquals("Expected 2 elements", 2, fromRelationsMergeInfo.size());
        assertEquals("Expected 1 elements", 1, toRelationsMergeInfo.size());
    }

    @Test
    public void testMergeDataAfterCreate_NoSavedData() {
        compInstanceRelationMerge.mergeDataAfterCreate(user, dataHolder, updatedContainerComponent, "SRV1.VF1.VFI_2");

        verify(dataHolder).getVfRelationsMergeInfo();
        List<RequirementCapabilityRelDef> relations = updatedContainerComponent.getComponentInstancesRelations();
        assertNull("Expected no relations", relations);
    }

    @Test
    public void testMergeDataAfterCreate() {
        Resource vf = new Resource();

        List<ComponentInstance> vfcInstances = new ArrayList<>();
        vfcInstances.add(createVfci("vfc_A", "SRV1.VF1.VFC_1.VFCI_1", "SRV1.VF1.VFC_1", true));
        vfcInstances.add(createVfci("vfc_B", "SRV1.VF1.VFC_2.VFCI_2", "SRV1.VF1.VFC_2", true));
        vfcInstances.add(createVfci("vfc_C", "SRV1.VF1.VFC_3.VFCI_3", "SRV1.VF1.VFC_3", false));
        vfcInstances.add(createVfci("vfc_D", "SRV1.VF1.VFC_4.VFCI_1", "SRV1.VF1.VFC_4", true));
        vf.setComponentInstances(vfcInstances);
        vf.setComponentType(ComponentTypeEnum.RESOURCE);
        vf.setResourceType(ResourceTypeEnum.VF);

        Either<Component, StorageOperationStatus> eitherVF = Either.left(vf);
        when(toscaOperationFacade.getToscaElement("SRV1.VF1")).thenReturn(eitherVF);


        List<RelationMergeInfo> fromRelationsMergeInfo = new ArrayList<>();
        List<RelationMergeInfo> toRelationsMergeInfo = new ArrayList<>();

        RelationMergeInfo relationMergeInfo1 = new RelationMergeInfo("CapabilityType1", "capabilityA", "vfc_A", requirementDef1);
        fromRelationsMergeInfo.add(relationMergeInfo1);
        RelationMergeInfo relationMergeInfo2 = new RelationMergeInfo("CapabilityType4", "capabilityD", "vfc_D", capabilityDef2);
        toRelationsMergeInfo.add(relationMergeInfo2);

        VfRelationsMergeInfo relationsMergeInfo = new VfRelationsMergeInfo(fromRelationsMergeInfo, toRelationsMergeInfo);

        when(dataHolder.getVfRelationsMergeInfo()).thenReturn(relationsMergeInfo);

        List<ComponentInstance> componentInstances = new ArrayList<>();
        componentInstances.add(createVfi("SRV1.VF1", "SRV1.VF1.VFI_2"));
        componentInstances.add(createVfi("SRV1.VF2", "SRV1.VF2.VFI_1"));
        componentInstances.add(createVfi("SRV1.VF3", "SRV1.VF3.VFI_1"));
        componentInstances.add(createVfi("SRV1.VF4", "SRV1.VF4.VFI_1"));
        componentInstances.add(createVfi("SRV1.VF5", "SRV1.VF5.VFI_1"));
        updatedContainerComponent.setComponentInstances(componentInstances);

        List<RequirementCapabilityRelDef> resourceInstancesRelations = new ArrayList<>();
        updatedContainerComponent.setComponentInstancesRelations(resourceInstancesRelations);

        when(toscaOperationFacade.associateResourceInstances(Mockito.anyString(), Mockito.anyList())).thenReturn(StorageOperationStatus.OK);

        compInstanceRelationMerge.mergeDataAfterCreate(user, dataHolder, updatedContainerComponent, "SRV1.VF1.VFI_2");

        verify(dataHolder).getVfRelationsMergeInfo();
        verify(toscaOperationFacade).associateResourceInstances(Mockito.anyString(), Mockito.anyList());

        List<RequirementCapabilityRelDef> relations = updatedContainerComponent.getComponentInstancesRelations();
        assertEquals("Expected 2 relations", 2, relations.size());


        RequirementCapabilityRelDef capabilityRelDef = relations.get(0);
        assertEquals("SRV1.VF1.VFC_4.VFCI_1", capabilityRelDef.resolveSingleRelationship().getRelation().getCapabilityOwnerId());
        assertEquals("SRV1.VF5.VFI_1", capabilityRelDef.getFromNode());
        assertEquals("SRV1.VF1.VFI_2", capabilityRelDef.getToNode());

        RequirementCapabilityRelDef requirementRelDef = relations.get(1);
        assertEquals("SRV1.VF1.VFC_1.VFCI_1", requirementRelDef.resolveSingleRelationship().getRelation().getRequirementOwnerId());
        assertEquals("SRV1.VF1.VFI_2", requirementRelDef.getFromNode());
        assertEquals("SRV1.VF2.VFI_1", requirementRelDef.getToNode());
    }



    @Test
    public void testMergeDataAfterCreate_OwnerChanged() {
        Resource vf = new Resource();

        List<ComponentInstance> vfcInstances = new ArrayList<>();
        vfcInstances.add(createVfci("vfc_A", "SRV1.VF1.VFC_1.VFCI_2", "SRV1.VF1.VFC_1", true));
        vfcInstances.add(createVfci("vfc_B", "SRV1.VF1.VFC_2.VFCI_2", "SRV1.VF1.VFC_2", true));
        vfcInstances.add(createVfci("vfc_C", "SRV1.VF1.VFC_3.VFCI_3", "SRV1.VF1.VFC_3", false));
        vfcInstances.add(createVfci("vfc_D", "SRV1.VF1.VFC_4.VFCI_2", "SRV1.VF1.VFC_4", true));
        vf.setComponentInstances(vfcInstances);
        vf.setComponentType(ComponentTypeEnum.RESOURCE);
        vf.setResourceType(ResourceTypeEnum.VF);

        Either<Component, StorageOperationStatus> eitherVF = Either.left(vf);
        when(toscaOperationFacade.getToscaElement("SRV1.VF1")).thenReturn(eitherVF);


        List<RelationMergeInfo> fromRelationsMergeInfo = new ArrayList<>();
        List<RelationMergeInfo> toRelationsMergeInfo = new ArrayList<>();

        RelationMergeInfo relationMergeInfo1 = new RelationMergeInfo("CapabilityType1", "capabilityA", "vfc_A", requirementDef1);
        fromRelationsMergeInfo.add(relationMergeInfo1);
        RelationMergeInfo relationMergeInfo2 = new RelationMergeInfo("CapabilityType4", "capabilityD", "vfc_D", capabilityDef2);
        toRelationsMergeInfo.add(relationMergeInfo2);

        VfRelationsMergeInfo relationsMergeInfo = new VfRelationsMergeInfo(fromRelationsMergeInfo, toRelationsMergeInfo);

        when(dataHolder.getVfRelationsMergeInfo()).thenReturn(relationsMergeInfo);

        List<ComponentInstance> componentInstances = new ArrayList<>();
        componentInstances.add(createVfi("SRV1.VF1", "SRV1.VF1.VFI_2"));
        componentInstances.add(createVfi("SRV1.VF2", "SRV1.VF2.VFI_1"));
        componentInstances.add(createVfi("SRV1.VF3", "SRV1.VF3.VFI_1"));
        componentInstances.add(createVfi("SRV1.VF4", "SRV1.VF4.VFI_1"));
        componentInstances.add(createVfi("SRV1.VF5", "SRV1.VF5.VFI_1"));
        updatedContainerComponent.setComponentInstances(componentInstances);

        List<RequirementCapabilityRelDef> resourceInstancesRelations = new ArrayList<>();
        updatedContainerComponent.setComponentInstancesRelations(resourceInstancesRelations);

        when(toscaOperationFacade.associateResourceInstances(Mockito.anyString(), Mockito.anyList())).thenReturn(StorageOperationStatus.OK);

        compInstanceRelationMerge.mergeDataAfterCreate(user, dataHolder, updatedContainerComponent, "SRV1.VF1.VFI_2");

        verify(dataHolder).getVfRelationsMergeInfo();
        verify(toscaOperationFacade).associateResourceInstances(Mockito.anyString(), Mockito.anyList());

        List<RequirementCapabilityRelDef> relations = updatedContainerComponent.getComponentInstancesRelations();
        assertEquals("Expected 2 relations", 2, relations.size());


        RequirementCapabilityRelDef capabilityRelDef = relations.get(0);
        assertEquals("SRV1.VF1.VFC_4.VFCI_2", capabilityRelDef.resolveSingleRelationship().getRelation().getCapabilityOwnerId());
        assertEquals("SRV1.VF5.VFI_1", capabilityRelDef.getFromNode());
        assertEquals("SRV1.VF1.VFI_2", capabilityRelDef.getToNode());

        RequirementCapabilityRelDef requirementRelDef = relations.get(1);
        assertEquals("SRV1.VF1.VFC_1.VFCI_2", requirementRelDef.resolveSingleRelationship().getRelation().getRequirementOwnerId());
        assertEquals("SRV1.VF1.VFI_2", requirementRelDef.getFromNode());
        assertEquals("SRV1.VF2.VFI_1", requirementRelDef.getToNode());
    }


    /**
     * @param vfId
     * @param vfiUniqueId
     * @return
     */
    private ComponentInstance createVfi(String vfId, String vfiUniqueId) {
        ComponentInstance vfi = new ComponentInstance();
        vfi.setUniqueId(vfiUniqueId);
        vfi.setComponentUid(vfId);

        Resource vf = new Resource();
        vf.setUniqueId(vfId);
        return vfi;
    }

    private ComponentInstance createVfci(String name, String uniqueId, String componentUid, boolean foundVfc) {
        ComponentInstance compInst = new ComponentInstance();
        compInst.setName(name);
        compInst.setUniqueId(uniqueId);
        compInst.setComponentUid(componentUid);

        if(foundVfc) {
            createVfc(componentUid);
        }
        else {
            failLoadVfc(componentUid);
        }
        return compInst;
    }

    private void failLoadVfc(String uid) {
        Either<Component, StorageOperationStatus> eitherVFC = Either.right(StorageOperationStatus.NOT_FOUND);
        when(toscaOperationFacade.getToscaElement(uid)).thenReturn(eitherVFC);
    }

    private Component createVfc(String uid) {
        Resource vfc = new Resource();
        vfc.setUniqueId(uid);

        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        Map<String, List<RequirementDefinition>> requirements = new HashMap<>();;
        IntStream.range(0, 5).forEach(i -> {

            List<CapabilityDefinition> capList = new LinkedList<>();
            capList.add(null);
            CapabilityDefinition capDef = new CapabilityDefinition();
            capDef.setName("CapabilityName" + i);
            capDef.setUniqueId("Capability" + i);
            capDef.setType("CapabilityType" + i);
            capList.add(capDef);
            capabilities.put("Key" + i, capList);

            List<RequirementDefinition> reqList = new LinkedList<>();
            reqList.add(null);
            RequirementDefinition reqDef = new RequirementDefinition();
            reqDef.setName("RequirementName" + i);
            reqDef.setUniqueId("Requirement" + i);
            reqDef.setCapability("CapabilityType" + i);
            reqList.add(reqDef);
            requirements.put("Key" + i, reqList);

        });
        vfc.setCapabilities(capabilities );
        vfc.setRequirements(requirements);

        Either<Component, StorageOperationStatus> eitherVFC = Either.left(vfc);
        when(toscaOperationFacade.getToscaElement(uid)).thenReturn(eitherVFC);

        return vfc;
    }

    private RequirementCapabilityRelDef createRequirementCapabilityDef(String fromNode, String toNode) {
        RequirementCapabilityRelDef reqCapDef = new RequirementCapabilityRelDef();

        reqCapDef.setFromNode(fromNode);
        reqCapDef.setToNode(toNode);

        List<CapabilityRequirementRelationship> relationships = new ArrayList<>();
        CapabilityRequirementRelationship capabilityRequirementRelationship = new CapabilityRequirementRelationship();
        relationships.add(capabilityRequirementRelationship);
        reqCapDef.setRelationships(relationships);

        return reqCapDef;
    }

    private RequirementCapabilityRelDef createRequirementDef(String fromNode, String toNode, String ownerId, String requirementUid) {
        RequirementCapabilityRelDef reqCapDef = createRequirementCapabilityDef(fromNode, toNode);
        CapabilityRequirementRelationship capabilityRequirementRelationship = reqCapDef.resolveSingleRelationship();

        RelationshipInfo relationshipInfo = new RelationshipInfo();
        relationshipInfo.setRequirementOwnerId(ownerId);
        relationshipInfo.setRequirementUid(requirementUid);
        relationshipInfo.setRelationships(new RelationshipImpl());
        capabilityRequirementRelationship.setRelation(relationshipInfo );



        return reqCapDef;
    }

    private RequirementCapabilityRelDef createCapabilityDef(String fromNode, String toNode, String ownerId, String capabilityUid) {
        RequirementCapabilityRelDef reqCapDef = createRequirementCapabilityDef(fromNode, toNode);
        CapabilityRequirementRelationship capabilityRequirementRelationship = reqCapDef.resolveSingleRelationship();

        RelationshipInfo relationshipInfo = new RelationshipInfo();
        relationshipInfo.setCapabilityOwnerId(ownerId);
        relationshipInfo.setCapabilityUid(capabilityUid);
        relationshipInfo.setRelationships(new RelationshipImpl());
        capabilityRequirementRelationship.setRelation(relationshipInfo );

        return reqCapDef;
    }



}
