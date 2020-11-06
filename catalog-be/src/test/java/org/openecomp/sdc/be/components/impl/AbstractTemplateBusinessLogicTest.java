/*
 * Copyright (C) 2020 CMCC, Inc. and others. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.validation.ServiceDistributionValidation;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.externalapi.servlet.representation.AbstractTemplateInfo;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.*;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class AbstractTemplateBusinessLogicTest {
    private static final String SERVICE_CATEGORY = "Mobility";
    private static final String INSTANTIATION_TYPE = "A-la-carte";
    private static final String RESOURCE_NAME = "My-Resource_Name with   space";
    private static final String RESOURCE_TOSCA_NAME = "My-Resource_Tosca_Name";
    private static final String RESOURCE_CATEGORY1 = "Network Layer 2-3";
    private static final String RESOURCE_SUBCATEGORY = "Router";

    IElementOperation elementDao = Mockito.mock(IElementOperation.class);
    IGroupOperation groupOperation = Mockito.mock(IGroupOperation.class);
    IGroupInstanceOperation groupInstanceOperation = Mockito.mock(IGroupInstanceOperation.class);
    IGroupTypeOperation groupTypeOperation = Mockito.mock(IGroupTypeOperation.class);
    InterfaceOperation interfaceOperation = Mockito.mock(InterfaceOperation.class);
    InterfaceLifecycleOperation interfaceLifecycleTypeOperation = Mockito.mock(InterfaceLifecycleOperation.class);
    ArtifactsOperations artifactToscaOperation = Mockito.mock(ArtifactsOperations.class);
    ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);


    @InjectMocks
    private AbstractTemplateBusinessLogic abstractTemplateBusinessLogic;

    User user = null;

    private AbstractTemplateBusinessLogic createTestSubject(){
        return new AbstractTemplateBusinessLogic(elementDao, groupOperation, groupInstanceOperation,
                groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
                artifactToscaOperation);
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // User data and management
        user = new User();
        user.setUserId("jh0003");
        user.setFirstName("Jimmi");
        user.setLastName("Hendrix");
        user.setRole(Role.ADMIN.name());

        abstractTemplateBusinessLogic = new AbstractTemplateBusinessLogic(elementDao, groupOperation, groupInstanceOperation,
                groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
                artifactToscaOperation);
        abstractTemplateBusinessLogic.setToscaOperationFacade(toscaOperationFacade);
    }

    @Test
    public void testSetServiceDistributionValidation(){
        AbstractTemplateBusinessLogic testSubject;
        ServiceDistributionValidation result = null;

        testSubject = createTestSubject();
        testSubject.setServiceDistributionValidation(result);
    }

    @Test
    public void testGetServiceDistributionValidation(){
        AbstractTemplateBusinessLogic testSubject;
        ServiceDistributionValidation result;

        testSubject = createTestSubject();
        result = testSubject.getServiceDistributionValidation();
    }

    @Test
    public void testSetServiceImportManager(){
        AbstractTemplateBusinessLogic testSubject;
        ServiceImportManager result = null;

        testSubject = createTestSubject();
        testSubject.setServiceImportManager(result);
    }

    @Test
    public void testGetServiceImportManager(){
        AbstractTemplateBusinessLogic testSubject;
        ServiceImportManager result;

        testSubject = createTestSubject();
        result = testSubject.getServiceImportManager();
    }

    @Test
    public void testSetServiceBusinessLogic(){
        AbstractTemplateBusinessLogic testSubject;
        ServiceBusinessLogic result = null;

        testSubject = createTestSubject();
        testSubject.setServiceBusinessLogic(result);
    }

    @Test
    public void testGetServiceBusinessLogic(){
        AbstractTemplateBusinessLogic testSubject;
        ServiceBusinessLogic result;

        testSubject = createTestSubject();
        result = testSubject.getServiceBusinessLogic();
    }

    @Test
    public void testGetServiceAbstractStatus() {
        List<Component> componentList = new ArrayList<Component>();
        Component service = createServiceObject(false);
        componentList.add(service);
        Resource resource = createParseResourceObject(false);
        resource.setUniqueId("8d46e136-2d4c-4008-9edb-4f112ce1ba6f");
        resource.setNormalizedName("huaweivhssabs0.test");

        when(toscaOperationFacade.getToscaElement(anyString()))
                .thenReturn(Either.left(resource));
        Either<AbstractTemplateInfo, ResponseFormat> serviceAbstractStatus = abstractTemplateBusinessLogic.getServiceAbstractStatus(componentList);
        assertNotNull(Either.left(serviceAbstractStatus));
    }

    protected Service createServiceObject(boolean afterCreate) {
        Service service = new Service();
        service.setUniqueId("33f9e84e-5666-4c98-829e-2cde0a196ebf");
        service.setName("Service");
        CategoryDefinition category = new CategoryDefinition();
        category.setName(SERVICE_CATEGORY);
        category.setIcons(Collections.singletonList("defaulticon"));
        List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        service.setCategories(categories);
        service.setInstantiationType(INSTANTIATION_TYPE);

        service.setDescription("description");
        List<String> tgs = new ArrayList<>();
        tgs.add(service.getName());
        service.setTags(tgs);
        service.setIcon("defaulticon");
        service.setContactId("aa1234");
        service.setProjectCode("12345");
        service.setEcompGeneratedNaming(true);
        service.setComponentInstances(getComponentInstanceList());
        service.setComponentInstancesRelations(getComponentInstancesRelations());
        if (afterCreate) {
            service.setVersion("0.1");
            service.setUniqueId(service.getName() + ":" + service.getVersion());
            service.setCreatorUserId(user.getUserId());
            service.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
        }
        return service;
    }

    private List<ComponentInstance> getComponentInstanceList() {
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId("componentInstanceUniqueId");
        componentInstance.setComponentUid("componentInstanceComponentUid");
        componentInstances.add(componentInstance);
        return componentInstances;
    }

    private Resource createParseResourceObject(boolean afterCreate) {
        Resource resource = new Resource();
        resource.setName(RESOURCE_NAME);
        resource.setToscaResourceName(RESOURCE_TOSCA_NAME);
        resource.addCategory(RESOURCE_CATEGORY1, RESOURCE_SUBCATEGORY);
        resource.setDescription("My short description");
        List<String> tgs = new ArrayList<>();
        tgs.add("test");
        tgs.add(resource.getName());
        resource.setTags(tgs);
        List<String> template = new ArrayList<>();
        template.add("tosca.nodes.Root");
        resource.setDerivedFrom(template);
        resource.setVendorName("Motorola");
        resource.setVendorRelease("1.0.0");
        resource.setContactId("ya5467");
        resource.setIcon("defaulticon");
        resource.setUUID("92e32e49-55f8-46bf-984d-a98c924037ec");
        resource.setInvariantUUID("InvariantUUID");
        resource.setNormalizedName("NormalizedName");
        Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
        List<RequirementDefinition> requirementDefinitionList= new ArrayList<>();
        requirements.put("test", requirementDefinitionList);
        resource.setRequirements(requirements);
        resource.setCategories(getCategories());

        if (afterCreate) {
            resource.setName(resource.getName());
            resource.setVersion("0.1");
            resource.setUniqueId(resource.getName()
                    .toLowerCase() + ":" + resource.getVersion());
            resource.setCreatorUserId(user.getUserId());
            resource.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
            resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        }
        return resource;
    }

    public List<CategoryDefinition> getCategories(){
        List<CategoryDefinition> categories = new ArrayList<>();
        CategoryDefinition categoryDefinition = new CategoryDefinition();
        List<SubCategoryDefinition> subcategories = new ArrayList<>();
        SubCategoryDefinition subCategoryDefinition = new SubCategoryDefinition();
        subCategoryDefinition.setName("Abstract");
        subcategories.add(subCategoryDefinition);
        categoryDefinition.setName("Generic");
        categoryDefinition.setSubcategories(subcategories);
        categories.add(categoryDefinition);
        return categories;
    }

    private List<RequirementCapabilityRelDef> getComponentInstancesRelations(){
        List<RequirementCapabilityRelDef> componentInstancesRelations = new ArrayList<>();
        RequirementCapabilityRelDef requirementCapabilityRelDef = new RequirementCapabilityRelDef();
        List<CapabilityRequirementRelationship> relationships = new ArrayList<>();
        CapabilityRequirementRelationship capabilityRequirementRelationship = new CapabilityRequirementRelationship();
        RequirementDataDefinition requirement = new RequirementDataDefinition();
        requirement.setName("mme_ipu_vdu.dependency.test");
        requirement.setCapability("featureTest");
        requirement.setNode("ext ZTE VL Test 0");
        capabilityRequirementRelationship.setRequirement(requirement);
        relationships.add(capabilityRequirementRelationship);
        requirementCapabilityRelDef.setFromNode("33f9e84e-5666-4c98-829e-2cde0a196ebf.8d46e136-2d4c-4008-9edb-4f112ce1ba6f.huaweivhssabs0.test");
        requirementCapabilityRelDef.setOriginUI(true);
        requirementCapabilityRelDef.setRelationships(relationships);
        requirementCapabilityRelDef.setToNode("33f9e84e-5666-4c98-829e-2cde0a196ebf.9866c3c5-3ca6-4dcc-8c85-df13e504b377.extvl0.test");
        requirementCapabilityRelDef.setUid("requirementCapabilityRelDefUid");
        componentInstancesRelations.add(requirementCapabilityRelDef);
        return componentInstancesRelations;
    }
}