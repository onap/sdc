package org.openecomp.sdc.be.model.operations.impl;

import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.HealingTitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.GroupTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.resources.data.CapabilityTypeData;
import org.openecomp.sdc.be.resources.data.GroupTypeData;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class GroupTypeOperationTest extends ModelTestBase {

    private static final String TOSCA_GROUPS_ROOT = "tosca.groups.Root";
    private static final String NULL_STRING = null;

    @Resource(name = "titan-generic-dao")
    private HealingTitanGenericDao titanDao;
    
    @Resource(name = "capability-type-operation")
    private CapabilityTypeOperation capabilityTypeOperation;
    
    @Resource(name = "group-type-operation")
    private GroupTypeOperation groupTypeOperation;
    
    @BeforeClass
    public static void setupBeforeClass() {
        ModelTestBase.init();
    }

    @After
    public void tearDown() {
        titanDao.rollback();
        cleanUp();
    }

    @Test
    public void getAllGroupTypes() {
        GroupTypeDefinition rootGroupDefinition = createRootGroupDefinition();
        GroupTypeDefinition type1 = createGroupType("type1");
        GroupTypeDefinition type2 = createGroupType("type2");
        groupTypeOperation.addGroupType(rootGroupDefinition);
        groupTypeOperation.addGroupType(type1);
        groupTypeOperation.addGroupType(type2);

        List<GroupTypeDefinition> allGroupTypesNoExclusion = groupTypeOperation.getAllGroupTypes(null);
        assertThat(allGroupTypesNoExclusion)
                .usingElementComparatorOnFields("type", "icon", "name")
                .containsExactlyInAnyOrder(rootGroupDefinition, type1, type2);
    }

    @Test
    public void getAllGroupTypes_whenPassingExclusionList_doNotReturnExcludedTypes() {
        GroupTypeDefinition rootGroupDefinition = createRootGroupDefinition();
        GroupTypeDefinition type1 = createGroupType("type1");
        GroupTypeDefinition type2 = createGroupType("type2");
        groupTypeOperation.addGroupType(rootGroupDefinition);
        groupTypeOperation.addGroupType(type1);
        groupTypeOperation.addGroupType(type2);

        List<GroupTypeDefinition> allGroupTypes = groupTypeOperation.getAllGroupTypes(newHashSet("type1", "type2"));
        assertThat(allGroupTypes)
                .usingElementComparatorOnFields("type")
                .containsExactly(rootGroupDefinition);
    }

    @Test
    public void groupTypeWithoutCapabilityCreated() {
        GroupTypeData rootNode = getOrCreateRootGroupTypeNode();
        
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDerivedFrom(TOSCA_GROUPS_ROOT);
        groupTypeDefinition.setDescription("groups l3-networks in network collection");
        groupTypeDefinition.setType("org.openecomp.groups.NetworkCollection");
        groupTypeDefinition.setVersion("1.0");
        
        List<PropertyDefinition> properties = asList(
                buildProperty("network_collection_type", "l3-network", "network collection type, defined with default value"),
                buildProperty("network_collection_subtype", "sub-interface", "network collection subtype, defined with default value"),
                buildProperty("network_collection_role", null, "network collection role"),
                buildProperty("network_collection_description", null, "network collection description, free format text"));
        
        groupTypeDefinition.setProperties(properties );
        
        Either<GroupTypeDefinition, StorageOperationStatus> addGroupTypeResult =  groupTypeOperation.addGroupType(groupTypeDefinition, true);
        assertTrue("check group type added", addGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(groupTypeDefinition, addGroupTypeResult.left().value());
        
        addGroupTypeResult = groupTypeOperation.getGroupTypeByTypeAndVersion("org.openecomp.groups.NetworkCollection", "1.0");
        assertTrue("check group type added", addGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(groupTypeDefinition, addGroupTypeResult.left().value());
        
        Either<GroupTypeData, TitanOperationStatus> groupTypeResult = titanDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), groupTypeDefinition.getType(), GroupTypeData.class);
        GroupTypeData groupTypeNode = extractVal(groupTypeResult);
        
        Either<Edge, TitanOperationStatus> edgeResult = titanDao.getEdgeByNodes(groupTypeNode, rootNode, GraphEdgeLabels.DERIVED_FROM);
        validate(edgeResult);
    }
    
    @Test
    public void groupTypeWithCapabilityAndPropsButCapTypeWithoutProps() {
        getOrCreateRootGroupTypeNode();

        CapabilityTypeDefinition capabilityTypeDef = createCapabilityType(null);
        Either<CapabilityTypeData, TitanOperationStatus> capabilityTypeResult = titanDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), capabilityTypeDef.getType(), CapabilityTypeData.class);
        extractVal(capabilityTypeResult);

        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDerivedFrom(TOSCA_GROUPS_ROOT);
        groupTypeDefinition.setDescription("groups l3-networks in network collection");
        groupTypeDefinition.setType("org.openecomp.groups.NetworkCollection");

        Map<String, CapabilityDefinition> mapCapabilities = new HashMap<>();

        ComponentInstanceProperty property = new ComponentInstanceProperty(
                buildProperty("vfc_instance_group_reference", null, "Ability to recognize capability per vfc instance group on vnf instance"));
        CapabilityDefinition capabilityDef = buildCapabilityDefintion(asList(property));
        mapCapabilities.put("vlan_assignment", capabilityDef);
        groupTypeDefinition.setCapabilities(mapCapabilities);


        List<PropertyDefinition> properties = asList(
                buildProperty("vfc_instance_group_role", null, "role of this VFC group"),
                buildProperty("vfc_parent_port_role", null, "common role of parent ports of VFCs in this group"),
                buildProperty("network_collection_role", null, "network collection role assigned to this group"),
                buildProperty("subinterface_role", null, "common role of subinterfaces of VFCs in this group, criteria the group is created"));

        groupTypeDefinition.setProperties(properties );

        Either<GroupTypeDefinition, StorageOperationStatus> addGroupTypeResult =  groupTypeOperation.addGroupType(groupTypeDefinition, true);
        assertTrue(addGroupTypeResult.isRight());
        assertEquals(StorageOperationStatus.MATCH_NOT_FOUND, addGroupTypeResult.right().value());
    }

    @Test
    public void groupTypeWithCapabilityTypeAndEdgeCreated() {
        GroupTypeData rootNode = getOrCreateRootGroupTypeNode();
        
        Map<String, PropertyDefinition> capTypeProperties = new HashMap<>();
        capTypeProperties.put("vfc_instance_group_reference",
                buildProperty("vfc_instance_group_reference", null, "Ability to recognize capability per vfc instance group on vnf instance"));

        CapabilityTypeDefinition capabilityTypeDef = createCapabilityType(capTypeProperties);
        Either<CapabilityTypeData, TitanOperationStatus> capabilityTypeResult = titanDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), capabilityTypeDef.getType(), CapabilityTypeData.class);
        extractVal(capabilityTypeResult);
        
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDerivedFrom(TOSCA_GROUPS_ROOT);
        groupTypeDefinition.setDescription("groups l3-networks in network collection");
        groupTypeDefinition.setType("org.openecomp.groups.NetworkCollection");

        Map<String, CapabilityDefinition> mapCapabilities = new HashMap<>();
        ComponentInstanceProperty property = new ComponentInstanceProperty(
                buildProperty("vfc_instance_group_reference", null, "Ability to recognize capability per vfc instance group on vnf instance"));
        CapabilityDefinition capabilityDef = buildCapabilityDefintion(asList(property));
        mapCapabilities.put("vlan_assignment", capabilityDef);
        groupTypeDefinition.setCapabilities(mapCapabilities);
        
        
        List<PropertyDefinition> properties = asList(
                buildProperty("vfc_instance_group_role", null, "role of this VFC group"),
                buildProperty("vfc_parent_port_role", null, "common role of parent ports of VFCs in this group"),
                buildProperty("network_collection_role", null, "network collection role assigned to this group"),
                buildProperty("subinterface_role", null, "common role of subinterfaces of VFCs in this group, criteria the group is created"));

        groupTypeDefinition.setProperties(properties );
        
        Either<GroupTypeDefinition, StorageOperationStatus> addGroupTypeResult =  groupTypeOperation.addGroupType(groupTypeDefinition, true);
        assertTrue("check group type added", addGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(groupTypeDefinition, addGroupTypeResult.left().value());
        
        Either<GroupTypeData, TitanOperationStatus> groupTypeResult = titanDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), groupTypeDefinition.getType(), GroupTypeData.class);
        GroupTypeData groupTypeNode = extractVal(groupTypeResult);
        
        Either<GroupTypeDefinition, StorageOperationStatus> groupTypeDefResult = groupTypeOperation.getGroupTypeByUid(groupTypeNode.getUniqueId());
        assertTrue(groupTypeDefResult.isLeft());
        GroupTypeDefinition groupTypeDefinitionRetrieved = groupTypeDefResult.left().value();
        assertNotNull(groupTypeDefinitionRetrieved);
        Map<String, CapabilityDefinition> capabilityDefs = groupTypeDefinitionRetrieved.getCapabilities();
        assertNotNull(capabilityDefs);
        assertEquals(1, capabilityDefs.size());
        assertTrue(capabilityDefs.containsKey("vlan_assignment"));
        CapabilityDefinition updatedCapabilityDef = capabilityDefs.get("vlan_assignment");
        assertEquals(2, updatedCapabilityDef.getProperties().size());
        
        Either<Edge, TitanOperationStatus> edgeDerivedFromResult = titanDao.getEdgeByNodes(groupTypeNode, rootNode, GraphEdgeLabels.DERIVED_FROM);
        validate(edgeDerivedFromResult);
    }
    
    @Test
    public void groupTypeWithCapabilityTypeAndEdgeCreated_OverrideDefaultCapabilityTypeValue() {
        getOrCreateRootGroupTypeNode();

        PropertyDefinition property = buildProperty("vfc_instance_group_reference", null, "Ability to recognize capability per vfc instance group on vnf instance");

        Map<String, PropertyDefinition> capTypeProperties = new HashMap<>();
        capTypeProperties.put("vfc_instance_group_reference", property);
        CapabilityTypeDefinition capabilityTypeDef = createCapabilityType(capTypeProperties);
        Either<CapabilityTypeData, TitanOperationStatus> capabilityTypeResult = titanDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), capabilityTypeDef.getType(), CapabilityTypeData.class);
        extractVal(capabilityTypeResult);

        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDerivedFrom(TOSCA_GROUPS_ROOT);
        groupTypeDefinition.setDescription("groups l3-networks in network collection");
        groupTypeDefinition.setType("org.openecomp.groups.NetworkCollection");

        Map<String, CapabilityDefinition> mapCapabilities = new HashMap<>();
        property.setValue("new_value");
        ComponentInstanceProperty capDefProperty = new ComponentInstanceProperty(property);
        CapabilityDefinition capabilityDef = buildCapabilityDefintion(asList(capDefProperty));
        mapCapabilities.put("vlan_assignment", capabilityDef);
        groupTypeDefinition.setCapabilities(mapCapabilities);


        List<PropertyDefinition> properties = asList(
                buildProperty("vfc_instance_group_role", null, "role of this VFC group"),
                buildProperty("vfc_parent_port_role", null, "common role of parent ports of VFCs in this group"),
                buildProperty("network_collection_role", null, "network collection role assigned to this group"),
                buildProperty("subinterface_role", null, "common role of subinterfaces of VFCs in this group, criteria the group is created"));

        groupTypeDefinition.setProperties(properties );

        Either<GroupTypeDefinition, StorageOperationStatus> addGroupTypeResult =  groupTypeOperation.addGroupType(groupTypeDefinition, true);
        assertTrue("check group type added", addGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(groupTypeDefinition, addGroupTypeResult.left().value());

        Either<GroupTypeData, TitanOperationStatus> groupTypeResult = titanDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), groupTypeDefinition.getType(), GroupTypeData.class);
        GroupTypeData groupTypeNode = extractVal(groupTypeResult);

        Either<GroupTypeDefinition, StorageOperationStatus> groupTypeDefResult = groupTypeOperation.getGroupTypeByUid(groupTypeNode.getUniqueId());
        assertTrue(groupTypeDefResult.isLeft());
        GroupTypeDefinition groupTypeDefinitionRetrieved = groupTypeDefResult.left().value();
        assertNotNull(groupTypeDefinitionRetrieved);
        Map<String, CapabilityDefinition> capabilityDefs = groupTypeDefinitionRetrieved.getCapabilities();
        assertNotNull(capabilityDefs);
        assertEquals(1, capabilityDefs.size());
        assertTrue(capabilityDefs.containsKey("vlan_assignment"));

        CapabilityDefinition capDefinition = capabilityDefs.get("vlan_assignment");
        assertEquals("new_value", capDefinition.getProperties().get(0).getValue());
        assertEquals(2, capDefinition.getProperties().size());
    }
    
    
    @Test
    public void updateGroupTypeWithCapability_FailedDueToCapabilityDeleteAttempt() {
        createRootGroupTypeNode();

        PropertyDefinition property = buildProperty("vfc_instance_group_reference", null, "Ability to recognize capability per vfc instance group on vnf instance");

        Map<String, PropertyDefinition> capTypeProperties = new HashMap<>();
        capTypeProperties.put("vfc_instance_group_reference", property);
        CapabilityTypeDefinition capabilityTypeDef = createCapabilityType(capTypeProperties);
        Either<CapabilityTypeData, TitanOperationStatus> capabilityTypeResult = titanDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), capabilityTypeDef.getType(), CapabilityTypeData.class);
        extractVal(capabilityTypeResult);

        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDerivedFrom(TOSCA_GROUPS_ROOT);
        groupTypeDefinition.setDescription("groups l3-networks in network collection");
        groupTypeDefinition.setType("org.openecomp.groups.NetworkCollection");

        Map<String, CapabilityDefinition> mapCapabilities = new HashMap<>();
        property.setValue("new_value");
        ComponentInstanceProperty capDefProperty = new ComponentInstanceProperty(property);
        CapabilityDefinition capabilityDef = buildCapabilityDefintion(asList(capDefProperty));
        mapCapabilities.put("vlan_assignment", capabilityDef);
        groupTypeDefinition.setCapabilities(mapCapabilities);

        Either<GroupTypeDefinition, StorageOperationStatus> addGroupTypeResult =  groupTypeOperation.addGroupType(groupTypeDefinition);
        assertTrue(addGroupTypeResult.isLeft());
        
        GroupTypeDefinition newGroupTypeDefinition = new GroupTypeDefinition();
        newGroupTypeDefinition.setDerivedFrom(TOSCA_GROUPS_ROOT);
        newGroupTypeDefinition.setDescription("groups l3-networks in network collection");
        newGroupTypeDefinition.setType("org.openecomp.groups.NetworkCollection");
        Either<GroupTypeDefinition, StorageOperationStatus> updateGroupTypeResult =  groupTypeOperation.updateGroupType(newGroupTypeDefinition, addGroupTypeResult.left().value());
        assertTrue(updateGroupTypeResult.isRight());
        assertEquals(StorageOperationStatus.MATCH_NOT_FOUND, updateGroupTypeResult.right().value());
    }
    
    @Test
    public void updateGroupTypeWithCapability_FailedDueToCapabilityChangeTypeAttempt() {
        createRootGroupTypeNode();

        PropertyDefinition property = buildProperty("vfc_instance_group_reference", null, "Ability to recognize capability per vfc instance group on vnf instance");

        Map<String, PropertyDefinition> capTypeProperties = new HashMap<>();
        capTypeProperties.put("vfc_instance_group_reference", property);
        CapabilityTypeDefinition capabilityTypeDef = createCapabilityType(capTypeProperties);
        Either<CapabilityTypeData, TitanOperationStatus> capabilityTypeResult = titanDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), capabilityTypeDef.getType(), CapabilityTypeData.class);
        extractVal(capabilityTypeResult);

        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDerivedFrom(TOSCA_GROUPS_ROOT);
        groupTypeDefinition.setDescription("groups l3-networks in network collection");
        groupTypeDefinition.setType("org.openecomp.groups.NetworkCollection");

        Map<String, CapabilityDefinition> mapCapabilities = new HashMap<>();
        property.setValue("new_value");
        ComponentInstanceProperty capDefProperty = new ComponentInstanceProperty(property);
        CapabilityDefinition capabilityDef = buildCapabilityDefintion(asList(capDefProperty));
        mapCapabilities.put("vlan_assignment", capabilityDef);
        groupTypeDefinition.setCapabilities(mapCapabilities);

        Either<GroupTypeDefinition, StorageOperationStatus> addGroupTypeResult =  groupTypeOperation.addGroupType(groupTypeDefinition);
        assertTrue(addGroupTypeResult.isLeft());
        
        GroupTypeDefinition newGroupTypeDefinition = new GroupTypeDefinition();
        newGroupTypeDefinition.setDerivedFrom(TOSCA_GROUPS_ROOT);
        newGroupTypeDefinition.setDescription("groups l3-networks in network collection");
        newGroupTypeDefinition.setType("org.openecomp.groups.NetworkCollection");
        
        Map<String, CapabilityDefinition> updatedMapCapabilities = new HashMap<>();
        property.setValue("new_value");
        ComponentInstanceProperty newCapDefProperty = new ComponentInstanceProperty(property);
        CapabilityDefinition updatedCapabilityDef = buildCapabilityDefintion(asList(newCapDefProperty));
        updatedCapabilityDef.setType("Another type");
        updatedMapCapabilities.put("vlan_assignment", updatedCapabilityDef);
        newGroupTypeDefinition.setCapabilities(updatedMapCapabilities);
        
        Either<GroupTypeDefinition, StorageOperationStatus> updateGroupTypeResult =  groupTypeOperation.updateGroupType(newGroupTypeDefinition,  addGroupTypeResult.left().value());
        assertTrue(updateGroupTypeResult.isRight());
        assertEquals(StorageOperationStatus.MATCH_NOT_FOUND, updateGroupTypeResult.right().value());
    }
    
    @Test
    public void updateGroupTypeWithCapability_Success() {
        createRootGroupTypeNode();

        PropertyDefinition property = buildProperty("vfc_instance_group_reference", null, "Ability to recognize capability per vfc instance group on vnf instance");

        Map<String, PropertyDefinition> capTypeProperties = new HashMap<>();
        capTypeProperties.put("vfc_instance_group_reference", property);
        CapabilityTypeDefinition capabilityTypeDef = createCapabilityType(capTypeProperties);
        Either<CapabilityTypeData, TitanOperationStatus> capabilityTypeResult = titanDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), capabilityTypeDef.getType(), CapabilityTypeData.class);
        extractVal(capabilityTypeResult);

        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDerivedFrom(TOSCA_GROUPS_ROOT);
        groupTypeDefinition.setDescription("groups l3-networks in network collection");
        groupTypeDefinition.setType("org.openecomp.groups.NetworkCollection");

        Map<String, CapabilityDefinition> mapCapabilities = new HashMap<>();
        property.setValue("new_value");
        ComponentInstanceProperty capDefProperty = new ComponentInstanceProperty(property);
        CapabilityDefinition capabilityDef = buildCapabilityDefintion(asList(capDefProperty));
        mapCapabilities.put("vlan_assignment", capabilityDef);
        groupTypeDefinition.setCapabilities(mapCapabilities);

        Either<GroupTypeDefinition, StorageOperationStatus> addGroupTypeResult =  groupTypeOperation.addGroupType(groupTypeDefinition);
        assertTrue(addGroupTypeResult.isLeft());
        
        GroupTypeDefinition newGroupTypeDefinition = new GroupTypeDefinition();
        newGroupTypeDefinition.setDerivedFrom(TOSCA_GROUPS_ROOT);
        newGroupTypeDefinition.setDescription("groups l3-networks in network collection");
        newGroupTypeDefinition.setType("org.openecomp.groups.NetworkCollection");
        
        Map<String, CapabilityDefinition> updatedMapCapabilities = new HashMap<>();
        property.setValue("another_value");
        ComponentInstanceProperty newCapDefProperty = new ComponentInstanceProperty(property);
        CapabilityDefinition updatedCapabilityDef = buildCapabilityDefintion(asList(newCapDefProperty));
        updatedMapCapabilities.put("vlan_assignment", updatedCapabilityDef);
        newGroupTypeDefinition.setCapabilities(updatedMapCapabilities);
        
        Either<GroupTypeDefinition, StorageOperationStatus> updateGroupTypeResult =  groupTypeOperation.updateGroupType(newGroupTypeDefinition,  addGroupTypeResult.left().value());
        assertTrue(updateGroupTypeResult.isLeft());
    }

    @Test
    public void testUpdateGroupTypeWithDerivedFromEdge() {
        createRootGroupTypeNode();

        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDescription("groups l2-networks in network collection");
        groupTypeDefinition.setType("org.openecomp.groups.PrivateCollection");
        groupTypeDefinition.setVersion("1.0");

        List<PropertyDefinition> properties = singletonList(
                buildProperty("network_collection_type", "l2-network", "network collection type, defined with default value"));
        
        groupTypeDefinition.setProperties(properties );
        
        Either<GroupTypeDefinition, StorageOperationStatus> addGroupTypeResult =  groupTypeOperation.addGroupType(groupTypeDefinition, true);
        assertTrue("check group type added", addGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(groupTypeDefinition, addGroupTypeResult.left().value());
        
        addGroupTypeResult = groupTypeOperation.getGroupTypeByTypeAndVersion("org.openecomp.groups.PrivateCollection", "1.0");
        assertTrue("check group type added", addGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(groupTypeDefinition, addGroupTypeResult.left().value());
 
        Either<GroupTypeDefinition, StorageOperationStatus> upgradeResult = groupTypeOperation.updateGroupType(groupTypeDefinition, groupTypeDefinition);
        assertNotNull(upgradeResult);
        assertTrue(upgradeResult.isLeft());
    }
    
    @Test
    public void testUpdateNonExistingGroupType() {
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDerivedFrom(TOSCA_GROUPS_ROOT);
        groupTypeDefinition.setDescription("groups l2-networks in network collection");
        groupTypeDefinition.setType("org.openecomp.groups.MyCollection");
        groupTypeDefinition.setVersion("1.0");
                             
        Either<GroupTypeDefinition, StorageOperationStatus> upgradeResult = groupTypeOperation.updateGroupType(groupTypeDefinition, groupTypeDefinition);
        assertNotNull(upgradeResult);
        assertTrue(upgradeResult.isRight());
    }
    
    @Test
    public void testUpdateNotDerivedGroupType() {
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDescription("groups social-networks in school");
        groupTypeDefinition.setType("org.openecomp.groups.Teachers");
        groupTypeDefinition.setVersion("1.0");
        
        Either<GroupTypeDefinition, StorageOperationStatus> addGroupTypeResult =  groupTypeOperation.addGroupType(groupTypeDefinition, true);
        assertTrue("check group type added", addGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(groupTypeDefinition, addGroupTypeResult.left().value());
              
        Either<GroupTypeDefinition, StorageOperationStatus> upgradeResult = groupTypeOperation.updateGroupType(groupTypeDefinition, groupTypeDefinition);
        assertNotNull(upgradeResult);
        assertTrue(upgradeResult.isLeft());
        assertThat(groupTypeDefinition).isEqualToIgnoringGivenFields(upgradeResult.left().value(), "properties", "capabilities");
    }
    
    @Test
    public void testUpdateGroupTypeWithNonExistingParent() {
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDescription("groups social-networks in work");
        groupTypeDefinition.setType("org.openecomp.groups.Cowokers");
        groupTypeDefinition.setVersion("1.0");
        
        Either<GroupTypeDefinition, StorageOperationStatus> addGroupTypeResult =  groupTypeOperation.addGroupType(groupTypeDefinition, true);
        assertTrue("check group type added", addGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(groupTypeDefinition, addGroupTypeResult.left().value());
              
        groupTypeDefinition.setDerivedFrom("Non.existing.parent");
        Either<GroupTypeDefinition, StorageOperationStatus> upgradeResult = groupTypeOperation.updateGroupType(groupTypeDefinition, groupTypeDefinition);
        assertNotNull(upgradeResult);
        assertTrue(upgradeResult.isRight());
    }
    
    @Test
    public void testUpgradeGroupType() {
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDescription("groups social-networks in university");
        groupTypeDefinition.setType("org.openecomp.groups.Students");
        groupTypeDefinition.setVersion("1.0");
        
        Either<GroupTypeDefinition, StorageOperationStatus> addGroupTypeResult =  groupTypeOperation.addGroupType(groupTypeDefinition, true);
        assertTrue("check group type added", addGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(groupTypeDefinition, addGroupTypeResult.left().value());
        
        GroupTypeDefinition parentGroupTypeDefinition = new GroupTypeDefinition();
        parentGroupTypeDefinition.setDescription("groups social-networks in university");
        parentGroupTypeDefinition.setType("org.openecomp.groups.Parents");
        parentGroupTypeDefinition.setVersion("1.0");
        parentGroupTypeDefinition.setHighestVersion(true);

        
        Either<GroupTypeDefinition, StorageOperationStatus> addParentGroupTypeResult =  groupTypeOperation.addGroupType(parentGroupTypeDefinition, true);
        assertTrue("check group type added", addParentGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(parentGroupTypeDefinition, addParentGroupTypeResult.left().value());
              
        groupTypeDefinition.setDerivedFrom("org.openecomp.groups.Parents");
        Either<GroupTypeDefinition, StorageOperationStatus> upgradeResult = groupTypeOperation.updateGroupType(groupTypeDefinition, addGroupTypeResult.left().value());
        assertNotNull(upgradeResult);
        assertTrue(upgradeResult.isLeft());
        assertThat(groupTypeDefinition).isEqualToIgnoringGivenFields(upgradeResult.left().value(), "properties", "capabilities");
    }
    
    
    private GroupTypeData getOrCreateRootGroupTypeNode() {
        Either<GroupTypeData, TitanOperationStatus> groupTypeResult = titanDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), TOSCA_GROUPS_ROOT, GroupTypeData.class);
        if(groupTypeResult.isLeft()) {
            return groupTypeResult.left().value();
        }
        
        return createRootGroupTypeNode();
    }
    
    private GroupTypeData createRootGroupTypeNode() {
        GroupTypeDefinition rootGroupDefinition = createRootGroupDefinition();
        Either<GroupTypeDefinition, StorageOperationStatus> addGroupTypeResult =  groupTypeOperation.addGroupType(rootGroupDefinition, false);
        assertTrue("check group type added", addGroupTypeResult.isLeft());
        
        Either<GroupTypeData, TitanOperationStatus> groupTypeResult = titanDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), rootGroupDefinition.getType(), GroupTypeData.class);
        return extractVal(groupTypeResult);        
    }
    
    private GroupTypeDefinition createRootGroupDefinition() {
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDescription("The TOSCA Group Type all other TOSCA Group Types derive from");
        groupTypeDefinition.setType(TOSCA_GROUPS_ROOT);
        groupTypeDefinition.setHighestVersion(true);
        return groupTypeDefinition;
    }

    private GroupTypeDefinition createGroupType(String type) {
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDescription("description for type " + type);
        groupTypeDefinition.setType(type);
        groupTypeDefinition.setName(type + "name");
        groupTypeDefinition.setIcon(type + "icon");
        return groupTypeDefinition;
    }
    
    private CapabilityTypeDefinition createCapabilityType(Map<String, PropertyDefinition> properties) {
        CapabilityTypeDefinition rootCapabilityTypeDefinition = new CapabilityTypeDefinition();
        rootCapabilityTypeDefinition.setType("tosca.capabilities.Root");
        rootCapabilityTypeDefinition.setDescription("Dummy root type");
        rootCapabilityTypeDefinition.setVersion("1.0");
        capabilityTypeOperation.addCapabilityType(rootCapabilityTypeDefinition, true);


        CapabilityTypeDefinition parentCapabilityTypeDefinition = new CapabilityTypeDefinition();
        parentCapabilityTypeDefinition.setType("tosca.capabilities.Parent");
        parentCapabilityTypeDefinition.setDescription("Dummy parent type");
        parentCapabilityTypeDefinition.setDerivedFrom("tosca.capabilities.Root");
        parentCapabilityTypeDefinition.setVersion("1.0");
        PropertyDefinition property = buildProperty("parentProp", "any", "Description");
        Map<String, PropertyDefinition> capTypeProperties = new HashMap<>();
        capTypeProperties.put("parent_prop", property);
        parentCapabilityTypeDefinition.setProperties(capTypeProperties);
        capabilityTypeOperation.addCapabilityType(parentCapabilityTypeDefinition, true);


        CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition();
        capabilityTypeDefinition.setDescription("ability to expose routing information of the internal network");
        capabilityTypeDefinition.setType("org.openecomp.capabilities.VLANAssignment");
        capabilityTypeDefinition.setVersion("1.0");
        capabilityTypeDefinition.setDerivedFrom("tosca.capabilities.Parent");

        capabilityTypeDefinition.setProperties(properties);

        Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityTypeResult = capabilityTypeOperation.addCapabilityType(capabilityTypeDefinition, true);
        assertTrue("check capability type added", addCapabilityTypeResult.isLeft());

        CapabilityTypeDefinition capabilityTypeAdded = addCapabilityTypeResult.left().value();
        compareBetweenCreatedToSent(capabilityTypeDefinition, capabilityTypeAdded);
        
        return capabilityTypeDefinition;
    }
    
    private CapabilityDefinition buildCapabilityDefintion(List<ComponentInstanceProperty> properties) {
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName("vlan_assignment");
        capabilityDefinition.setDescription("ability to expose routing information of the internal network");
        capabilityDefinition.setType("org.openecomp.capabilities.VLANAssignment");
        capabilityDefinition.setProperties(properties);
        return capabilityDefinition;
    }

    private PropertyDefinition buildProperty(String name, String defaultValue, String description) {
        PropertyDefinition property = new PropertyDefinition();
        property.setName(name);
        property.setDefaultValue(defaultValue);
        property.setRequired(true);
        property.setDescription(description);
        property.setType(ToscaType.STRING.name().toLowerCase());

        return property;
    }
    
    private void compareBetweenCreatedToSent(CapabilityTypeDefinition expected, CapabilityTypeDefinition actual) {
        assertEquals(expected.getDerivedFrom(), actual.getDerivedFrom());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getDescription(), actual.getDescription());
    }
    
    private void compareBetweenCreatedToSent(GroupTypeDefinition expected, GroupTypeDefinition actual) {
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getDescription(), actual.getDescription());
    }
    
    private <T> void validate(Either<T, TitanOperationStatus> result) {
        extractVal(result);
    }
    
    private <T> T extractVal(Either<T, TitanOperationStatus> result) {
        assertTrue(result.isLeft());
        T t = result.left().value();
        assertNotNull(t);
        
        return t;
    }

    private void cleanUp() {
        Either<TitanGraph, TitanOperationStatus> graphResult = titanDao.getGraph();
        TitanGraph graph = graphResult.left().value();

        Iterable<TitanVertex> vertices = graph.query().vertices();
        if (vertices != null) {
            Iterator<TitanVertex> iterator = vertices.iterator();
            while (iterator.hasNext()) {
                TitanVertex vertex = iterator.next();
                vertex.remove();
            }

        }
        titanDao.commit();
    }


    @Test
    public void updateGroupType_returnNotFoundErrorIfTryingToUpdateANonExistingType() {
        GroupTypeDefinition currType = createGroupTypeDef();
        GroupTypeDefinition updatedType = createGroupTypeDef();
        Either<GroupTypeDefinition, StorageOperationStatus> updatedGroupTypeRes = groupTypeOperation.updateGroupType(updatedType, currType);
        assertThat(updatedGroupTypeRes.right().value()).isEqualTo(StorageOperationStatus.NOT_FOUND);
    }

    @Test
    public void updateGroupType_basicFields() {
        GroupTypeDefinition createdType = createGroupTypeDef("type1", "description1", NULL_STRING);
        Either<GroupTypeDefinition, StorageOperationStatus> currGroupType = groupTypeOperation.addGroupType(createdType);

        GroupTypeDefinition updatedType = createGroupTypeDef("type1", "description2", NULL_STRING);
        updatedType.setName("newName");
        updatedType.setIcon("icon");
        groupTypeOperation.updateGroupType(updatedType, currGroupType.left().value());

        Either<GroupTypeDefinition, StorageOperationStatus> fetchedUpdatedType = groupTypeOperation.getLatestGroupTypeByType(createdType.getType());
        GroupTypeDefinition fetchedGroupType = fetchedUpdatedType.left().value();
        assertThat(fetchedGroupType.getProperties()).isEmpty();
        assertThat(fetchedGroupType)
                .isEqualToIgnoringGivenFields(updatedType, "properties", "capabilities");

    }

    @Test
    public void updateGroupType_updatePropertiesType_FailedDueAttemptToChangePropertyType() {
        PropertyDefinition prop1 = createSimpleProperty("val1", "prop1", "string");
        GroupTypeDefinition groupType = createGroupTypeDef(prop1);
        Either<GroupTypeDefinition, StorageOperationStatus> currGroupType = groupTypeOperation.addGroupType(groupType);

        PropertyDefinition updatedProp1 = duplicateProperty(prop1, "newVal1", "int");
        PropertyDefinition prop3 = createSimpleProperty("val3", "prop3", "string");
        GroupTypeDefinition updatedGroupType = createGroupTypeDef(updatedProp1, prop3);

        Either<GroupTypeDefinition, StorageOperationStatus> updatedGroupTypeRetrieved = groupTypeOperation.updateGroupType(updatedGroupType, currGroupType.left().value());
        assertEquals(StorageOperationStatus.MATCH_NOT_FOUND, updatedGroupTypeRetrieved.right().value());
    }
    
    @Test
    public void validateGroupType_FailedDueAttempToCreateGroupTypeWithPropertyWhichTypeIsDifferentFromTypeOfParentPropertyWithTheSameName() {
        GroupTypeDefinition rootGroupType = createGroupTypeDef();
        Either<GroupTypeDefinition, StorageOperationStatus> rootGroupTypeRes = groupTypeOperation.addGroupType(rootGroupType);
        assertTrue(rootGroupTypeRes.isLeft());
        
        PropertyDefinition prop = createSimpleProperty("val1", "prop", "string");
        GroupTypeDefinition groupType1 = createGroupTypeDef("type1", "descr1", rootGroupType.getType(), prop);
        Either<GroupTypeDefinition, StorageOperationStatus> groupType1Res = groupTypeOperation.addGroupType(groupType1);
        assertTrue(groupType1Res.isLeft());
        
        PropertyDefinition prop1 = createSimpleProperty("33", "prop", "int");
        PropertyDefinition prop2 = createSimpleProperty("val2", "prop2", "string");
        GroupTypeDefinition groupType2 = createGroupTypeDef("type2", "descr", groupType1.getType(), prop1, prop2);
        
        Either<GroupTypeDefinition, StorageOperationStatus> updatedGroupTypeRetrieved = groupTypeOperation.validateUpdateProperties(groupType2);
        assertEquals(StorageOperationStatus.PROPERTY_NAME_ALREADY_EXISTS, updatedGroupTypeRetrieved.right().value());
    }

    @Test
    public void updateGroupType_derivedFrom_whenNoPrevDerivedFrom_updateToNewDerivedFrom() {
        GroupTypeDefinition rootGroupType = createGroupTypeDef();
        GroupTypeDefinition groupType1 = createGroupTypeDef("type1", "descr", NULL_STRING);
        GroupTypeDefinition updatedGroupType = createGroupTypeDef("type1", "descr", rootGroupType.getType());
        groupTypeOperation.addGroupType(rootGroupType);
        Either<GroupTypeDefinition, StorageOperationStatus> currGroupType = groupTypeOperation.addGroupType(groupType1);
        groupTypeOperation.updateGroupType(updatedGroupType, currGroupType.left().value());

        Either<GroupTypeDefinition, StorageOperationStatus> latestGroupType = groupTypeOperation.getLatestGroupTypeByType(groupType1.getType());
        assertThat(latestGroupType.left().value().getDerivedFrom()).isEqualTo(rootGroupType.getType());
        verifyDerivedFromNodeEqualsToRootGroupType(rootGroupType, latestGroupType.left().value().getUniqueId());
    }

    @Test
    public void updateGroupType_derivedFrom_updateToNullDerivedFrom_derivedFromDeleted_Failed() {
        GroupTypeDefinition rootGroupType = createGroupTypeDef();
        GroupTypeDefinition groupType1 = createGroupTypeDef("type1", "descr", rootGroupType.getType());
        GroupTypeDefinition updatedGroupType = createGroupTypeDef("type1", "descr", null, new PropertyDefinition[]{});
        groupTypeOperation.addGroupType(rootGroupType);
        Either<GroupTypeDefinition, StorageOperationStatus> currGroupType = groupTypeOperation.addGroupType(groupType1);

        Either<GroupTypeDefinition, StorageOperationStatus> updateGroupTypeRes = groupTypeOperation.updateGroupType(updatedGroupType, currGroupType.left().value());
        assertThat(updateGroupTypeRes.right().value()).isEqualTo(StorageOperationStatus.NOT_FOUND);

        Either<GroupTypeDefinition, StorageOperationStatus> latestGroupType = groupTypeOperation.getLatestGroupTypeByType(groupType1.getType());
        assertThat(latestGroupType.left().value().getDerivedFrom()).isEqualTo(rootGroupType.getType());
    }

    @Test
    public void updateGroupType_updateDerivedFrom() {
        GroupTypeDefinition rootGroupType = createGroupTypeDef();
        GroupTypeDefinition derivedType1 = createGroupTypeDef("derivedType1", "descr", rootGroupType.getType());
        GroupTypeDefinition groupType1 = createGroupTypeDef("type1", "descr", rootGroupType.getType());
        GroupTypeDefinition updatedGroupType = createGroupTypeDef("type1", "descr", derivedType1.getType());

        groupTypeOperation.addGroupType(rootGroupType);
        groupTypeOperation.addGroupType(derivedType1);
        Either<GroupTypeDefinition, StorageOperationStatus> currGroupType = groupTypeOperation.addGroupType(groupType1);

        groupTypeOperation.updateGroupType(updatedGroupType, currGroupType.left().value());

        Either<GroupTypeDefinition, StorageOperationStatus> latestGroupType = groupTypeOperation.getLatestGroupTypeByType(groupType1.getType());
        assertThat(latestGroupType.left().value().getDerivedFrom()).isEqualTo(derivedType1.getType());
    }
    
    @Test
    public void updateGroupType_updateDerivedFrom_CauseEndlessRecursion() {
        GroupTypeDefinition rootGroupType = createGroupTypeDef();
        GroupTypeDefinition derivedType1 = createGroupTypeDef("derivedType1", "descr", rootGroupType.getType());
        GroupTypeDefinition groupType1 = createGroupTypeDef("type1", "descr", derivedType1.getType());
        GroupTypeDefinition updatedGroupType = createGroupTypeDef("derivedType1", "descr", groupType1.getType());

        groupTypeOperation.addGroupType(rootGroupType);
        Either<GroupTypeDefinition, StorageOperationStatus> currGroupType = groupTypeOperation.addGroupType(derivedType1);
        groupTypeOperation.addGroupType(groupType1);

        Either<GroupTypeDefinition, StorageOperationStatus> updateResult = groupTypeOperation.updateGroupType(updatedGroupType, currGroupType.left().value());
        assertThat(updateResult.right().value()).isEqualTo(StorageOperationStatus.GENERAL_ERROR);

        Either<GroupTypeDefinition, StorageOperationStatus> latestGroupType = groupTypeOperation.getLatestGroupTypeByType(updatedGroupType.getType());
        assertThat(latestGroupType.left().value().getDerivedFrom()).isEqualTo(rootGroupType.getType());
    }

    private PropertyDefinition duplicateProperty(PropertyDefinition prop1, String updatedValue, String updatedType) {
        PropertyDefinition updatedProp1 = new PropertyDefinition(prop1);
        updatedProp1.setUniqueId(null);
        updatedProp1.setDefaultValue(updatedValue);
        updatedProp1.setType(updatedType);
        return updatedProp1;
    }

    private void verifyDerivedFromNodeEqualsToRootGroupType(GroupTypeDefinition rootGroupType, String parentGroupId) {
        Either<ImmutablePair<GroupTypeData, GraphEdge>, TitanOperationStatus> derivedFromRelation = titanDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.GroupType), parentGroupId, GraphEdgeLabels.DERIVED_FROM,
                NodeTypeEnum.GroupType, GroupTypeData.class);
        assertThat(derivedFromRelation.left().value().getLeft().getGroupTypeDataDefinition())
                .isEqualToComparingFieldByField(rootGroupType);
    }

    private void verifyDerivedFromRelationDoesntExist(String parentGroupId) {
        Either<ImmutablePair<GroupTypeData, GraphEdge>, TitanOperationStatus> derivedFromRelation = titanDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.GroupType), parentGroupId, GraphEdgeLabels.DERIVED_FROM,
                NodeTypeEnum.GroupType, GroupTypeData.class);
        assertThat(derivedFromRelation.right().value())
                .isEqualTo(TitanOperationStatus.NOT_FOUND);
    }

    private GroupTypeDefinition createGroupTypeDef() {
        return createGroupTypeDef("tosca.groups.Root", "description: The TOSCA Group Type all other TOSCA Group Types derive from", null, new PropertyDefinition[]{});
    }

    private GroupTypeDefinition createGroupTypeDef(PropertyDefinition ... props) {
        return createGroupTypeDef("tosca.groups.Root",  null, props);
    }

    private GroupTypeDefinition createGroupTypeDef(String type, String derivedFrom, PropertyDefinition ... props) {
        GroupTypeDefinition groupType = createGroupTypeDef(type, "description: The TOSCA Group Type all other TOSCA Group Types derive from", derivedFrom);
        groupType.setProperties(asList(props));
        return groupType;
    }

    private GroupTypeDefinition createGroupTypeDef(String type, String description, String derivedFrom) {
        return createGroupTypeDef(type, description, derivedFrom, null);
    }

    private GroupTypeDefinition createGroupTypeDef(String type, String description, String derivedFrom,  PropertyDefinition ... props) {
        GroupTypeDataDefinition groupTypeDataDefinition = new GroupTypeDataDefinition();
        groupTypeDataDefinition.setDescription(description);
        groupTypeDataDefinition.setType(type);
        groupTypeDataDefinition.setName(type + "name");
        groupTypeDataDefinition.setIcon(type + "icon");
        groupTypeDataDefinition.setDerivedFrom(derivedFrom);
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition(groupTypeDataDefinition);
        groupTypeDefinition.setHighestVersion(true);
        groupTypeDefinition.setVersion("1.0");
        if (props != null) {
            groupTypeDefinition.setProperties(asList(props));
        }
        return groupTypeDefinition;
    }

}
