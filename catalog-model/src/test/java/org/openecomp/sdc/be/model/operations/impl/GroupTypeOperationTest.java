package org.openecomp.sdc.be.model.operations.impl;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.resources.data.CapabilityTypeData;
import org.openecomp.sdc.be.resources.data.GroupTypeData;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fj.data.Either;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class GroupTypeOperationTest extends ModelTestBase {
    private static final String TOSCA_GROUPS_ROOT = "tosca.groups.Root";

    @Resource(name = "titan-generic-dao")
    private TitanGenericDao titanDao;
    
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
                .usingElementComparatorOnFields("type")
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
    public void groupTypeWithoutCapabilityTypeCreated() {
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
        
        Either<GroupTypeDefinition, StorageOperationStatus> addGroupTypeResult =  groupTypeOperation.addGroupType(groupTypeDefinition, false);
        assertEquals("check group type added", true, addGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(groupTypeDefinition, addGroupTypeResult.left().value());
        
        addGroupTypeResult = groupTypeOperation.getGroupTypeByTypeAndVersion("org.openecomp.groups.NetworkCollection", "1.0");
        assertEquals("check group type added", true, addGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(groupTypeDefinition, addGroupTypeResult.left().value());
        
        Either<GroupTypeData, TitanOperationStatus> groupTypeResult = titanDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), groupTypeDefinition.getType(), GroupTypeData.class);
        GroupTypeData groupTypeNode = extractVal(groupTypeResult);
        
        Either<Edge, TitanOperationStatus> edgeResult = titanDao.getEdgeByNodes(groupTypeNode, rootNode, GraphEdgeLabels.DERIVED_FROM);
        validate(edgeResult);
    }
    
    @Test
    public void groupTypeWithCapabilityTypeAndEdgeCreated() {
        GroupTypeData rootNode = getOrCreateRootGroupTypeNode();
        
        CapabilityTypeDefinition capabilityTypeDef = createCapabilityType();
        Either<CapabilityTypeData, TitanOperationStatus> capabilityTypeResult = titanDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), capabilityTypeDef.getType(), CapabilityTypeData.class);
        CapabilityTypeData capabilityTypeNode = extractVal(capabilityTypeResult);
        
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDerivedFrom(TOSCA_GROUPS_ROOT);
        groupTypeDefinition.setDescription("groups l3-networks in network collection");
        groupTypeDefinition.setType("org.openecomp.groups.NetworkCollection");
        groupTypeDefinition.setCapabilityTypes(asList(capabilityTypeDef));
        
        List<PropertyDefinition> properties = asList(
                buildProperty("vfc_instance_group_role", null, "role of this VFC group"),
                buildProperty("vfc_parent_port_role", null, "common role of parent ports of VFCs in this group"),
                buildProperty("network_collection_role", null, "network collection role assigned to this group"),
                buildProperty("subinterface_role", null, "common role of subinterfaces of VFCs in this group, criteria the group is created"));

        groupTypeDefinition.setProperties(properties );
        
        Either<GroupTypeDefinition, StorageOperationStatus> addGroupTypeResult =  groupTypeOperation.addGroupType(groupTypeDefinition, true);
        assertEquals("check group type added", true, addGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(groupTypeDefinition, addGroupTypeResult.left().value());
        
        Either<GroupTypeData, TitanOperationStatus> groupTypeResult = titanDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), groupTypeDefinition.getType(), GroupTypeData.class);
        GroupTypeData groupTypeNode = extractVal(groupTypeResult);
        
        Either<Edge, TitanOperationStatus> edgeCapTypeResult = titanDao.getEdgeByNodes(groupTypeNode, capabilityTypeNode, GraphEdgeLabels.GROUP_TYPE_CAPABILITY_TYPE);
        validate(edgeCapTypeResult);
        
        Either<Edge, TitanOperationStatus> edgeDerivedFromResult = titanDao.getEdgeByNodes(groupTypeNode, rootNode, GraphEdgeLabels.DERIVED_FROM);
        validate(edgeDerivedFromResult);
    }
    
    @Test
    public void testUpgradeGroupTypeWithDerrivedFromEdge() {
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDerivedFrom(TOSCA_GROUPS_ROOT);
        groupTypeDefinition.setDescription("groups l2-networks in network collection");
        groupTypeDefinition.setType("org.openecomp.groups.PrivateCollection");
        groupTypeDefinition.setVersion("1.0");
        
        List<PropertyDefinition> properties = singletonList(
                buildProperty("network_collection_type", "l2-network", "network collection type, defined with default value"));
        
        groupTypeDefinition.setProperties(properties );
        
        Either<GroupTypeDefinition, StorageOperationStatus> addGroupTypeResult =  groupTypeOperation.addGroupType(groupTypeDefinition, true);
        assertEquals("check group type added", true, addGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(groupTypeDefinition, addGroupTypeResult.left().value());
        
        addGroupTypeResult = groupTypeOperation.getGroupTypeByTypeAndVersion("org.openecomp.groups.PrivateCollection", "1.0");
        assertEquals("check group type added", true, addGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(groupTypeDefinition, addGroupTypeResult.left().value());
 
        Either<GroupTypeDefinition, StorageOperationStatus> upgradeResult = groupTypeOperation.upgradeGroupType(groupTypeDefinition, groupTypeDefinition, true);
        assertNotNull(upgradeResult);
        assertTrue(upgradeResult.isLeft());
    }
    
    @Test
    public void testUpgradeNonExistingGroupType() {
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDerivedFrom(TOSCA_GROUPS_ROOT);
        groupTypeDefinition.setDescription("groups l2-networks in network collection");
        groupTypeDefinition.setType("org.openecomp.groups.MyCollection");
        groupTypeDefinition.setVersion("1.0");
                             
        Either<GroupTypeDefinition, StorageOperationStatus> upgradeResult = groupTypeOperation.upgradeGroupType(groupTypeDefinition, groupTypeDefinition, true);
        assertNotNull(upgradeResult);
        assertTrue(upgradeResult.isRight());
    }
    
    @Test
    public void testUpgradeNotDerivedGroupType() {
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDescription("groups social-networks in school");
        groupTypeDefinition.setType("org.openecomp.groups.Teachers");
        groupTypeDefinition.setVersion("1.0");
        
        Either<GroupTypeDefinition, StorageOperationStatus> addGroupTypeResult =  groupTypeOperation.addGroupType(groupTypeDefinition, true);
        assertEquals("check group type added", true, addGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(groupTypeDefinition, addGroupTypeResult.left().value());
              
        Either<GroupTypeDefinition, StorageOperationStatus> upgradeResult = groupTypeOperation.upgradeGroupType(groupTypeDefinition, groupTypeDefinition, true);
        assertNotNull(upgradeResult);
        assertTrue(upgradeResult.isLeft());
        assertEquals(groupTypeDefinition, upgradeResult.left().value());
    }
    
    @Test
    public void testUpgradeGroupTypeWithNonExistingParent() {
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDescription("groups social-networks in work");
        groupTypeDefinition.setType("org.openecomp.groups.Cowokers");
        groupTypeDefinition.setVersion("1.0");
        
        Either<GroupTypeDefinition, StorageOperationStatus> addGroupTypeResult =  groupTypeOperation.addGroupType(groupTypeDefinition, true);
        assertEquals("check group type added", true, addGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(groupTypeDefinition, addGroupTypeResult.left().value());
              
        groupTypeDefinition.setDerivedFrom("Non.existing.parent");
        Either<GroupTypeDefinition, StorageOperationStatus> upgradeResult = groupTypeOperation.upgradeGroupType(groupTypeDefinition, groupTypeDefinition, true);
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
        assertEquals("check group type added", true, addGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(groupTypeDefinition, addGroupTypeResult.left().value());
        
        GroupTypeDefinition parentGroupTypeDefinition = new GroupTypeDefinition();
        parentGroupTypeDefinition.setDescription("groups social-networks in university");
        parentGroupTypeDefinition.setType("org.openecomp.groups.Parents");
        parentGroupTypeDefinition.setVersion("1.0");
        
        Either<GroupTypeDefinition, StorageOperationStatus> addParentGroupTypeResult =  groupTypeOperation.addGroupType(parentGroupTypeDefinition, true);
        assertEquals("check group type added", true, addParentGroupTypeResult.isLeft());
        compareBetweenCreatedToSent(parentGroupTypeDefinition, addParentGroupTypeResult.left().value());
              
        groupTypeDefinition.setDerivedFrom("org.openecomp.groups.Parents");
        Either<GroupTypeDefinition, StorageOperationStatus> upgradeResult = groupTypeOperation.upgradeGroupType(groupTypeDefinition, groupTypeDefinition, true);
        assertNotNull(upgradeResult);
        assertTrue(upgradeResult.isLeft());
        assertEquals(groupTypeDefinition, upgradeResult.left().value());
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
        Either<GroupTypeDefinition, StorageOperationStatus> addGroupTypeResult =  groupTypeOperation.addGroupType(rootGroupDefinition, true);
        assertEquals("check group type added", true, addGroupTypeResult.isLeft());
        
        Either<GroupTypeData, TitanOperationStatus> groupTypeResult = titanDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), rootGroupDefinition.getType(), GroupTypeData.class);
        return extractVal(groupTypeResult);        
    }
    
    private GroupTypeDefinition createRootGroupDefinition() {
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDescription("The TOSCA Group Type all other TOSCA Group Types derive from");
        groupTypeDefinition.setType(TOSCA_GROUPS_ROOT);
        return groupTypeDefinition;
    }

    private GroupTypeDefinition createGroupType(String type) {
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        groupTypeDefinition.setDescription("description for type " + type);
        groupTypeDefinition.setType(type);
        return groupTypeDefinition;
    }
    
    private CapabilityTypeDefinition createCapabilityType() {
        CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition();
        capabilityTypeDefinition.setDescription("ability to expose routing information of the internal network");
        capabilityTypeDefinition.setType("org.openecomp.capabilities.VLANAssignment");
        capabilityTypeDefinition.setVersion("1.0");
        
        Map<String, PropertyDefinition> properties = new HashMap<>();
        properties.put("vfc_instance_group_reference",
                buildProperty("vfc_instance_group_reference", null, "Ability to recognize capability per vfc instance group on vnf instance"));
 
        capabilityTypeDefinition.setProperties(properties);

        Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityTypeResult = capabilityTypeOperation.addCapabilityType(capabilityTypeDefinition, true);
        assertEquals("check capability type added", true, addCapabilityTypeResult.isLeft());

        CapabilityTypeDefinition capabilityTypeAdded = addCapabilityTypeResult.left().value();
        compareBetweenCreatedToSent(capabilityTypeDefinition, capabilityTypeAdded);
        
        return capabilityTypeDefinition;
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
    
}
