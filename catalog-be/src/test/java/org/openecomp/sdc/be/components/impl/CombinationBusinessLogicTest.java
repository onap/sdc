/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.impl;

import java.io.IOException;
import java.util.function.Function;
import mockit.Deencapsulation;
import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.assertEquals;

import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.components.impl.CombinationBusinessLogic.NATIVE_NETWORK_LINK_TO;

import fj.data.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.Combination;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.Position;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.CombinationOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTypeOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.be.ui.model.UiCombination;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

public class CombinationBusinessLogicTest {

    private static final String COMBINATION_NAME = "My-Combination_Name";
    private static final String SERVICE_CATEGORY = "Mobility";
    private static final String INSTANTIATION_TYPE = "A-la-carte";
    private static final String USER_ADMIN = "jh0003";

    private List<ComponentInstance> componentInstances = new ArrayList<ComponentInstance>();
    private List<RequirementCapabilityRelDef> componentInstancesRelations = new ArrayList<RequirementCapabilityRelDef>();
    private Map<String, List<ComponentInstanceProperty>> componentInstancesAttributes = new HashMap<String, List<ComponentInstanceProperty>>();
    private Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = new HashMap<String, List<ComponentInstanceProperty>>();
    private Map<String, List<ComponentInstanceInput>> componentInstancesInputs = new HashMap<String, List<ComponentInstanceInput>>();

    private TitanDao mockTitanDao = Mockito.mock(TitanDao.class);
    private CombinationBusinessLogic bl = new CombinationBusinessLogic();
    private CombinationOperation combinationOperation = new CombinationOperation();
    private ComponentsUtils componentsUtils;
    private final UserValidations userValidations = Mockito.mock(UserValidations.class);
    ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
    NodeTypeOperation nodeTypeOperation = Mockito.mock(NodeTypeOperation.class);
    TopologyTemplateOperation topologyTemplateOperation = Mockito.mock(TopologyTemplateOperation.class);
    GraphLockOperation graphLockOperation = Mockito.mock(GraphLockOperation.class);

    private User user;

    public CombinationBusinessLogicTest() {
    }

    @Before
    public void setup() {
        ExternalConfiguration.setAppName("catalog-be");
        // init Configuration
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
                appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

        componentsUtils = new ComponentsUtils(Mockito.mock(AuditingManager.class));
        combinationOperation.setTitanDao(mockTitanDao);

        // User data and management
        user = new User();
        user.setUserId(USER_ADMIN);
        user.setFirstName("Jimmi");
        user.setLastName("Hendrix");
        user.setRole(Role.ADMIN.name());

        // BL object
        bl = new CombinatitestCreateCombinationFromServiceWithComponentsonBusinessLogic();
        bl.setComponentsUtils(componentsUtils);
        bl.setTitanGenericDao(mockTitanDao);
        bl.setCombinationOperation(combinationOperation);
        bl.setUserValidations(userValidations);
        toscaOperationFacade.setNodeTypeOperation(nodeTypeOperation);
        toscaOperationFacade.setTopologyTemplateOperation(topologyTemplateOperation);
        bl.setToscaOperationFacade(toscaOperationFacade);
        bl.setUserValidations(userValidations);
        bl.setGraphLockOperation(graphLockOperation);
    }

    @Test
    public void testCreateCombinationFromServiceNoComponents() {

        validateUserRoles(Role.ADMIN, Role.DESIGNER);

        Service serviceNoCompInstances = createServiceObject(false);
        Combination combinationNoCompInstances = createCombinationObject(false);

        Either<GraphVertex, TitanOperationStatus> getVertexEither = Either.right(TitanOperationStatus.OK);
        when(mockTitanDao.getVertexById(COMBINATION_NAME)).thenReturn(getVertexEither);

        Either<Combination, ResponseFormat> createResponse = bl
                .createCombination(combinationNoCompInstances, serviceNoCompInstances);

        // expect an invalid response 400 code
        assertEquals(true, createResponse.isRight());
    }

    @Test
    public void testCreateCombinationFromServiceWithComponents() {

        Service serviceWithCompInstances = createServiceObject(true);
        Combination combinationNoCompInstances = createCombinationObject(false);
        Combination combinationWithCompInstances = createCombinationObject(true);

        Either<GraphVertex, TitanOperationStatus> getVertexEither = Either.right(TitanOperationStatus.OK);
        when(mockTitanDao.getVertexById(COMBINATION_NAME)).thenReturn(getVertexEither);
        when(mockTitanDao.commit()).thenReturn(TitanOperationStatus.OK);
        GraphVertex gv = Mockito.mock(GraphVertex.class);
        Either<GraphVertex, TitanOperationStatus> createdVertex = Either.left(gv);
        when(mockTitanDao.createVertex(any(GraphVertex.class))).thenReturn(createdVertex);
        when(mockTitanDao
                .createEdge(any(GraphVertex.class), any(GraphVertex.class), eq(EdgeLabelEnum.CATALOG_ELEMENT), isNull()))
                .thenReturn(TitanOperationStatus.OK);
        when(mockTitanDao.getVertexByLabel(VertexTypeEnum.CATALOG_ROOT)).thenReturn(createdVertex);

        Either<Combination, ResponseFormat> createResponse = bl
                .createCombination(combinationNoCompInstances, serviceWithCompInstances);

        assertEqualsCombinationObjects(combinationWithCompInstances, createResponse.left().value());
    }

    @Test
    public void testCreateCombinationInstanceFromIncorrectResource() {

        ResponseFormat createResponse = bl
                .createCombinationInstance("wrong type", null, user.getUserId(), null);

        assertEquals(new Integer(500), createResponse.getStatus());
    }

    @Test
    public void testCreateCombinationInstanceFromService() {

        Service serviceWithCompInstances = createServiceObject(true);
        Combination combinationWithCompInstances = createCombinationObject(true);
        String containerComponentId = serviceWithCompInstances.getUniqueId();
        ComponentInstance componentInstance = null;
        String combinationJson = null;

        when(userValidations.validateUserExists(eq(user.getUserId()), anyString(), eq(false))).thenReturn(user);
        Either<Component, StorageOperationStatus> eitherLeftService = Either.left(serviceWithCompInstances);
        when(toscaOperationFacade.getToscaElement(eq(containerComponentId), any(ComponentParametersView.class)))
                .thenReturn(eitherLeftService);
        when(graphLockOperation.lockComponent(Mockito.anyString(), eq(NodeTypeEnum.Service)))
                .thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.unlockComponent(Mockito.anyString(), eq(NodeTypeEnum.Service)))
                .thenReturn(StorageOperationStatus.OK);

        try {
            combinationJson = (String) RepresentationUtils.toRepresentation(combinationWithCompInstances);
        } catch (IOException e) {
        }
        componentInstance = RepresentationUtils.fromRepresentation(combinationJson, ComponentInstance.class);
        componentInstance.setComponentUid(combinationWithCompInstances.getUniqueId());
        componentInstance.setPosX("10");
        componentInstance.setPosY("15");
        GraphVertex gv = new GraphVertex();
        gv.setJsonString(combinationJson);
        when(mockTitanDao.getVertexById(combinationWithCompInstances.getUniqueId())).thenReturn(Either.left(gv));

        ResponseFormat createResponse = bl
                .createCombinationInstance("services", containerComponentId, user.getUserId(), componentInstance);

        assertEquals(new Integer(200), createResponse.getStatus());
    }

    @Test
    public void testGetCombinationById() {

        Combination combinationWithCompInstances = createCombinationObject(true);
        ComponentInstance componentInstance = null;
        String combinationJson = null;

        try {
            combinationJson = (String) RepresentationUtils.toRepresentation(combinationWithCompInstances);
        } catch (IOException e) {
        }
        componentInstance = RepresentationUtils.fromRepresentation(combinationJson, ComponentInstance.class);
        componentInstance.setComponentUid(combinationWithCompInstances.getUniqueId());
        GraphVertex gv = new GraphVertex();
        gv.setJsonString(combinationJson);
        when(mockTitanDao.getVertexById(combinationWithCompInstances.getUniqueId())).thenReturn(Either.left(gv));

        Either<Combination, ResponseFormat> response = bl.getCombinationById(componentInstance.getComponentUid());

        if (response.isRight()) {
            assertEquals(new Integer(200), response.right().value().getStatus());
        }
        assertEqualsCombinationObjects(combinationWithCompInstances, response.left().value());
    }

    @Test
    public void testGetAllCombinationTypes() {

        Combination combinationWithCompInstances = createCombinationObject(true);
        String combinationJson = null;
        List<Combination> combList = new ArrayList<Combination>();
        combList.add(combinationWithCompInstances);

        try {
            combinationJson = (String) RepresentationUtils.toRepresentation(combinationWithCompInstances);
        } catch (IOException e) {
        }
        GraphVertex gv = new GraphVertex();
        gv.setJsonString(combinationJson);
        List<GraphVertex> gvList = new ArrayList<>();
        gvList.add(gv);
        when(mockTitanDao.getByCriteria(eq(VertexTypeEnum.COMBINATION), any(Map.class))).thenReturn(Either.left(gvList));

        Either<List<UiCombination>, ResponseFormat> response = bl.getAllCombinations();

        if (response.isRight()) {
            assertEquals(new Integer(200), response.right().value().getStatus());
        }
        assertEqualsCombinationUiCombinationLists(combList, response.left().value());
    }

    @Test
    public void testCalculateNewPosition() {
        Position position = new Position();
        ComponentInstance c = new ComponentInstance();
        c.setPosX("10");
        c.setPosY("15");
        position.setPagePosX(new Double(20));
        position.setPagePosY(new Double(25));
        position.setOrigMinPosX(new Double(-10));
        position.setOrigMinPosY(new Double(-15));
        position.setNewMinPosX(new Double(-20));
        position.setNewMinPosY(new Double(-25));
        position.setNewMaxPosX(new Double(30));
        position.setNewMaxPosY(new Double(35));
        Deencapsulation.invoke(bl, "calculateNewPosition", position, c);
        assertEquals(c.getPosX(), "40.0");
        assertEquals(c.getPosY(), "55.0");
    }

    @Test
    public void testUpdateRelations() {
        Component containerComponent = new Service();
        containerComponent.setUniqueId("s1");
        Map<String, String> latestComponentCounterMap = new HashMap<String, String>();
        latestComponentCounterMap.put("comp_a", "1");
        latestComponentCounterMap.put("comp_b", "2");
        RequirementCapabilityRelDef relDef = new RequirementCapabilityRelDef();
        relDef.setFromNode("s.1.comp_a");
        relDef.setToNode("s.2.comp_b");
        CapabilityRequirementRelationship cr = new CapabilityRequirementRelationship();
        RelationshipInfo ri = new RelationshipInfo();
        ri.setCapabilityOwnerId("c.1.comp_a");
        ri.setRequirementOwnerId("c.2.comp_b");
        RelationshipImpl relationship = new RelationshipImpl();
        relationship.setType(NATIVE_NETWORK_LINK_TO);
        ri.setRelationships(relationship);
        cr.setRelation(ri);
        List<CapabilityRequirementRelationship> crList = new ArrayList<CapabilityRequirementRelationship>();
        crList.add(cr);
        relDef.setRelationships(crList);
        Deencapsulation.invoke(bl, "updateRelations", containerComponent, relDef, latestComponentCounterMap);
        assertEquals(relDef.getFromNode(), "s1.1.comp_1");
        assertEquals(relDef.getToNode(), "s1.2.comp_2");
        assertEquals(relDef.getRelationships().get(0).getRelation().getCapabilityOwnerId(), "s1.1.comp_1");
        assertEquals(relDef.getRelationships().get(0).getRelation().getRequirementOwnerId(), "s1.2.comp_2");
    }

    private Combination createCombinationObject(boolean addComponentInstances) {
        Combination combination = new Combination();
        combination.setName(COMBINATION_NAME);
        combination.setUniqueId(combination.getName());
        combination.setDesc("My short description");

        if (addComponentInstances) {
            combination.setComponentInstances(componentInstances);
            combination.setComponentInstancesRelations(componentInstancesRelations);
            combination.setComponentInstancesAttributes(componentInstancesAttributes);
            combination.setComponentInstancesProperties(componentInstancesProperties);
            combination.setComponentInstancesInputs(componentInstancesInputs);
        }

        return combination;
    }

    private Service createServiceObject(boolean addComponentInstances) {
        Service service = new Service();
        service.setUniqueId("sid");
        service.setName("Service");
        CategoryDefinition category = new CategoryDefinition();
        category.setName(SERVICE_CATEGORY);
        List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        service.setCategories(categories);
        service.setInstantiationType(INSTANTIATION_TYPE);

        service.setDescription("description");
        List<String> tgs = new ArrayList<>();
        tgs.add(service.getName());
        service.setTags(tgs);
        service.setIcon("MyIcon");
        service.setContactId("aa1234");
        service.setProjectCode("12345");

        service.setVersion("0.1");
        service.setUniqueId(service.getName() + ":" + service.getVersion());
        service.setCreatorUserId(user.getUserId());
        service.setCreatorFullName(user.getFirstName() + " " + user.getLastName());

        if (addComponentInstances) {
            service.setComponentInstances(componentInstances);
            service.setComponentInstancesRelations(componentInstancesRelations);
            service.setComponentInstancesAttributes(componentInstancesAttributes);
            service.setComponentInstancesProperties(componentInstancesProperties);
            service.setComponentInstancesInputs(componentInstancesInputs);
        }

        return service;
    }

    private void assertEqualsCombinationObjects(Combination origComponent, Combination newComponent) {
        assertEquals(origComponent.getComponentInstances(), newComponent.getComponentInstances());
        assertEquals(origComponent.getComponentInstancesInputs(), newComponent.getComponentInstancesInputs());
        assertEquals(origComponent.getComponentInstancesAttributes(), newComponent.getComponentInstancesAttributes());
        assertEquals(origComponent.getComponentInstancesProperties(), newComponent.getComponentInstancesProperties());
        assertEquals(origComponent.getComponentInstancesRelations(), newComponent.getComponentInstancesRelations());
        assertEquals(origComponent.getDesc(), newComponent.getDesc());
        assertEquals(origComponent.getName(), newComponent.getName());
        assertEquals(origComponent.getUniqueId(), newComponent.getUniqueId());
    }

    private void assertEqualsCombinationUiCombinationLists(List<Combination> combList, List<UiCombination> uiCombList) {
        Map<String, Combination> combMap = combList.stream().collect(Collectors.toMap(Combination::getUniqueId,
                Function.identity()));
        Map<String, UiCombination> uiCombMap = uiCombList.stream().collect(Collectors.toMap(UiCombination::getUniqueId,
                Function.identity()));

        assertEquals(combMap.size(), uiCombMap.size());

        combMap.forEach((key, value) -> {
                    UiCombination uiComb = uiCombMap.get(key);
                    assertEquals(value.getName(), uiComb.getName());
                    assertEquals(value.getDesc(), uiComb.getDescription());
                }
        );
    }

    private void validateUserRoles(Role... roles) {
        List<Role> listOfRoles = Stream.of(roles).collect(Collectors.toList());
    }
}