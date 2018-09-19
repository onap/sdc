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

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.CombinationOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class CombinationBusinessLogicTest {
    private static final String COMBINATION_NAME = "My-Combination_Name";
    private static final String SERVICE_CATEGORY = "Mobility";
    private static final String INSTANTIATION_TYPE = "A-la-carte";

    private List<ComponentInstance> componentInstances = new ArrayList<ComponentInstance>();
    private List<RequirementCapabilityRelDef> componentInstancesRelations = new ArrayList<RequirementCapabilityRelDef>();
    private Map<String, List<ComponentInstanceProperty>> componentInstancesAttributes = new HashMap<String, List<ComponentInstanceProperty>>();
    private Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = new HashMap<String, List<ComponentInstanceProperty>>();
    private Map<String, List<ComponentInstanceInput>> componentInstancesInputs = new HashMap<String, List<ComponentInstanceInput>>();

    private TitanDao mockTitanDao = Mockito.mock(TitanDao.class);
    private CombinationBusinessLogic bl = new CombinationBusinessLogic();
    private CombinationOperation combinationOperation = Mockito.mock(CombinationOperation.class);
    private ComponentsUtils componentsUtils;

    private User user;

    private Service serviceNoCompInstances;
    private Service serviceWithCompInstances;
    private Combination combinationNoCompInstances;
    private Combination combinationWithCompInstances;

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

        // User data and management
        user = new User();
        user.setUserId("jh0003");
        user.setFirstName("Jimmi");
        user.setLastName("Hendrix");
        user.setRole(Role.ADMIN.name());

        serviceNoCompInstances = createServiceObject(false);
        serviceWithCompInstances = createServiceObject(true);
        combinationNoCompInstances = createCombinationObject(false);
        combinationWithCompInstances = createCombinationObject(true);

        Either<GraphVertex, TitanOperationStatus> getVertexEither = Either.right(TitanOperationStatus.OK);
        when(mockTitanDao.getVertexById(COMBINATION_NAME)).thenReturn(getVertexEither);
        Either<Combination, StorageOperationStatus> createCombElement = Either.left(combinationWithCompInstances);
        when(combinationOperation.createCombinationElement(eq(serviceWithCompInstances), any(Combination.class), anyString())).thenReturn(createCombElement);

        // BL object
        bl = new CombinationBusinessLogic();
        bl.setComponentsUtils(componentsUtils);
        bl.setTitanGenericDao(mockTitanDao);
        bl.setCombinationOperation(combinationOperation);
    }

    @Test
    public void testCreateCombinationFromServiceNoComponents() {
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        Either<Combination, ResponseFormat> createResponse = bl.createCombination(combinationNoCompInstances, serviceNoCompInstances);
        // expect an invalid response 400 code
        assertEquals(true, createResponse.isRight());
        assertEquals(new Integer(400), createResponse.right().value().getStatus());
    }

    @Test
    public void testCreateCombinationFromServiceWithComponents() {
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        Either<Combination, ResponseFormat> createResponse = bl.createCombination(combinationNoCompInstances, serviceWithCompInstances);
        // expect a Combination
        if (createResponse.isRight()) {
            assertEquals(new Integer(200), createResponse.right().value().getStatus());
        }
        assertEqualsCombinationObject(combinationWithCompInstances, createResponse.left().value());
    }

    private Combination createCombinationObject(boolean addComponentInstances) {
        Combination combination = new Combination();
        combination.setName(COMBINATION_NAME);
        combination.setUniqueId(combination.getName());
        combination.setDesc("My short description");

        if(addComponentInstances) {
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

        if(addComponentInstances) {
            service.setComponentInstances(componentInstances);
            service.setComponentInstancesRelations(componentInstancesRelations);
            service.setComponentInstancesAttributes(componentInstancesAttributes);
            service.setComponentInstancesProperties(componentInstancesProperties);
            service.setComponentInstancesInputs(componentInstancesInputs);
        }

        return service;
    }

    private void assertEqualsCombinationObject(Combination origComponent, Combination newComponent) {
        assertEquals(origComponent.getComponentInstances(), newComponent.getComponentInstances());
        assertEquals(origComponent.getComponentInstancesInputs(), newComponent.getComponentInstancesInputs());
        assertEquals(origComponent.getComponentInstancesAttributes(), newComponent.getComponentInstancesAttributes());
        assertEquals(origComponent.getComponentInstancesProperties(), newComponent.getComponentInstancesProperties());
        assertEquals(origComponent.getComponentInstancesRelations(), newComponent.getComponentInstancesRelations());
        assertEquals(origComponent.getDesc(), newComponent.getDesc());
        assertEquals(origComponent.getName(), newComponent.getName());
        assertEquals(origComponent.getUniqueId(), newComponent.getUniqueId());
    }

    private void validateUserRoles(Role... roles) {
        List<Role> listOfRoles = Stream.of(roles).collect(Collectors.toList());
    }
}
