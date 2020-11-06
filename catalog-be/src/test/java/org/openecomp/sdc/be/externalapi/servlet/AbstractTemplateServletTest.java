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

package org.openecomp.sdc.be.externalapi.servlet;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fj.data.Either;
import org.apache.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.*;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.externalapi.servlet.representation.AbstractTemplateInfo;
import org.openecomp.sdc.be.externalapi.servlet.representation.CopyServiceInfo;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class AbstractTemplateServletTest extends JerseyTest {

    private static final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    private static final HttpSession session = Mockito.mock(HttpSession.class);
    private static final ServletUtils servletUtils = Mockito.mock(ServletUtils.class);
    private static final ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);
    private static final ResourceImportManager resourceImportManager = Mockito.mock(ResourceImportManager.class);
    private static final ResourceBusinessLogic resourceBusinessLogic = Mockito.mock(ResourceBusinessLogic.class);
    private static final ServiceBusinessLogic serviceBusinessLogic = Mockito.mock(ServiceBusinessLogic.class);
    private static final ElementBusinessLogic elementBusinessLogic = Mockito.mock(ElementBusinessLogic.class);
    private static final Resource resource = Mockito.mock(Resource.class);
    private static final AbstractTemplateBusinessLogic abstractTemplateBusinessLogic = Mockito.mock(AbstractTemplateBusinessLogic.class);
    private static final UserBusinessLogic userBusinessLogic = Mockito.mock(UserBusinessLogic.class);
    private static final ComponentInstanceBusinessLogic componentInstanceBusinessLogic = Mockito.mock(ComponentInstanceBusinessLogic.class);
    private static final ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
    private static final ResponseFormat responseFormat = Mockito.mock(ResponseFormat.class);
    private static User user = null;

    private static final String SERVICE_CATEGORY = "Mobility";
    private static final String INSTANTIATION_TYPE = "A-la-carte";
    private static final String RESOURCE_NAME = "My-Resource_Name with   space";
    private static final String RESOURCE_TOSCA_NAME = "My-Resource_Tosca_Name";
    private static final String RESOURCE_CATEGORY1 = "Network Layer 2-3";
    private static final String RESOURCE_SUBCATEGORY = "Router";


    @BeforeClass
    public static void setup() {
        ExternalConfiguration.setAppName("catalog-be");

        // User data and management
        user = new User();
        user.setUserId("jh0003");
        user.setFirstName("Jimmi");
        user.setLastName("Hendrix");
        user.setRole(Role.ADMIN.name());

        when(request.getSession()).thenReturn(session);
        when(request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER)).thenReturn("mockXEcompInstanceId");
        when(request.getHeader(Constants.X_ECOMP_REQUEST_ID_HEADER)).thenReturn("mockXEcompRequestID");
        when(request.getHeader(Constants.ACCEPT_HEADER)).thenReturn("mockAttID");
        when(request.getHeader(Constants.USER_ID_HEADER)).thenReturn("mockAttID");
    }

    @Test
    public void testGetServiceAbstractStatus(){
        String serviceUUID = "serviceUUID";
        String path = "/v1/catalog/abstract/service/serviceUUID/" + serviceUUID + "/status";
        MultivaluedMap<String,Object> headers = new MultivaluedHashMap<>();
        headers.putSingle(Constants.X_ECOMP_REQUEST_ID_HEADER, "mockXEcompRequestID");
        headers.putSingle(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId");
        List<Component> componentList = new ArrayList<Component>();
        Component service = createServiceObject(false);
        componentList.add(service);
        Resource resource = createParseResourceObject(false);
        resource.setUniqueId("8d46e136-2d4c-4008-9edb-4f112ce1ba6f");
        resource.setNormalizedName("huaweivhssabs0.test");
        AbstractTemplateInfo abstractTemplateInfo = new AbstractTemplateInfo();

        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
        when(toscaOperationFacade.getToscaElement(anyString()))
                .thenReturn(Either.left(resource));
        when(elementBusinessLogic.getCatalogComponentsByUuidAndAssetType(anyString(),anyString()))
                .thenReturn(Either.left(componentList));
        when(abstractTemplateBusinessLogic.getServiceAbstractStatus(eq(componentList)))
                .thenReturn(Either.left(abstractTemplateInfo));
        when(responseFormat.getStatus()).thenReturn(org.eclipse.jetty.http.HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers)
                .get();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void testGetServiceAbstractStatusFailure_500(){
        String serviceUUID = "serviceUUID";
        String path = "/v1/catalog/abstract/service/serviceUUID/" + serviceUUID + "/status";
        MultivaluedMap<String,Object> headers = new MultivaluedHashMap<>();
        headers.putSingle(Constants.X_ECOMP_REQUEST_ID_HEADER, "mockXEcompRequestID");
        headers.putSingle(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId");
        List<Component> componentList = new ArrayList<Component>();
        Component service = createServiceObject(false);
        componentList.add(service);
        Resource resource = createParseResourceObject(false);
        resource.setUniqueId("8d46e136-2d4c-4008-9edb-4f112ce1ba6f");
        resource.setNormalizedName("huaweivhssabs0.test");
        AbstractTemplateInfo abstractTemplateInfo = new AbstractTemplateInfo();

        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
        when(toscaOperationFacade.getToscaElement(anyString()))
                .thenReturn(Either.left(resource));
        when(elementBusinessLogic.getCatalogComponentsByUuidAndAssetType(anyString(),anyString()))
                .thenReturn(Either.right(new ResponseFormat(500)));
        when(abstractTemplateBusinessLogic.getServiceAbstractStatus(eq(componentList)))
                .thenReturn(Either.left(abstractTemplateInfo));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers)
                .get();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testGetServiceAbstractStatusInstanceIDIsNull(){
        String serviceUUID = "serviceUUID";
        String path = "/v1/catalog/abstract/service/serviceUUID/" + serviceUUID + "/status";
        MultivaluedMap<String,Object> headers = new MultivaluedHashMap<>();
        headers.putSingle(Constants.X_ECOMP_REQUEST_ID_HEADER, "mockXEcompRequestID");
        List<Component> componentList = new ArrayList<Component>();
        Component service = createServiceObject(false);
        componentList.add(service);
        Resource resource = createParseResourceObject(false);
        resource.setUniqueId("8d46e136-2d4c-4008-9edb-4f112ce1ba6f");
        resource.setNormalizedName("huaweivhssabs0.test");
        AbstractTemplateInfo abstractTemplateInfo = new AbstractTemplateInfo();

        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
        when(toscaOperationFacade.getToscaElement(anyString()))
                .thenReturn(Either.left(resource));
        when(elementBusinessLogic.getCatalogComponentsByUuidAndAssetType(anyString(),anyString()))
                .thenReturn(Either.right(new ResponseFormat(400)));
        when(abstractTemplateBusinessLogic.getServiceAbstractStatus(eq(componentList)))
                .thenReturn(Either.left(abstractTemplateInfo));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.SC_BAD_REQUEST);
        when(componentsUtils.getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID)).thenReturn(responseFormat);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers)
                .get();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testGetServiceAbstractStatusFailure(){
        String serviceUUID = "serviceUUID";
        String path = "/v1/catalog/abstract/service/serviceUUID/" + serviceUUID + "/status";
        MultivaluedMap<String,Object> headers = new MultivaluedHashMap<>();
        headers.putSingle(Constants.X_ECOMP_REQUEST_ID_HEADER, "mockXEcompRequestID");
        headers.putSingle(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId");
        List<Component> componentList = new ArrayList<Component>();
        Component service = createServiceObject(false);
        componentList.add(service);

        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
        when(toscaOperationFacade.getToscaElement(anyString()))
                .thenReturn(Either.left(resource));
        when(elementBusinessLogic.getCatalogComponentsByUuidAndAssetType(anyString(),anyString()))
                .thenReturn(Either.left(componentList));
        when(abstractTemplateBusinessLogic.getServiceAbstractStatus(eq(componentList)))
                .thenReturn(Either.right(new ResponseFormat(500)));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers)
                .get();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testGetServiceAbstractStatusFailure_Exception(){
        String serviceUUID = "serviceUUID";
        String path = "/v1/catalog/abstract/service/serviceUUID/" + serviceUUID + "/status";
        MultivaluedMap<String,Object> headers = new MultivaluedHashMap<>();
        headers.putSingle(Constants.X_ECOMP_REQUEST_ID_HEADER, "mockXEcompRequestID");
        headers.putSingle(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId");
        List<Component> componentList = new ArrayList<Component>();

        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
        when(toscaOperationFacade.getToscaElement(anyString()))
                .thenReturn(Either.left(resource));
        when(elementBusinessLogic.getCatalogComponentsByUuidAndAssetType(anyString(),anyString()))
                .thenReturn(Either.left(componentList));
        when(abstractTemplateBusinessLogic.getServiceAbstractStatus(eq(componentList)))
                .thenReturn(Either.right(new ResponseFormat(500)));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers)
                .get();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testCopyExistService(){
        String serviceUUID = "serviceUUID";
        MultivaluedMap<String,Object> headers = new MultivaluedHashMap<>();
        headers.putSingle(Constants.X_ECOMP_REQUEST_ID_HEADER, "mockXEcompRequestID");
        headers.putSingle(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId");
        headers.putSingle("USER_ID", user.getUserId());
        CopyServiceInfo copyServiceInfo = new CopyServiceInfo();
        copyServiceInfo.setOldServiceUUid("mockOldServiceUUid");
        copyServiceInfo.setNewServiceUUid("mockNewServiceUUid");
        copyServiceInfo.setNewServiceName("mockNewServiceName");
        ObjectMapper mapper = new ObjectMapper();
        String copyServiceInfoJson = null;
        try {
            copyServiceInfoJson = mapper.writeValueAsString(copyServiceInfo);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        List<Component> componentList = new ArrayList<Component>();
        Component service = createServiceObject(false);
        componentList.add(service);

        String path = "/v1/catalog/abstract/service/serviceUUID/" + serviceUUID + "/copy";

        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
        when(componentsUtils.convertJsonToObjectUsingObjectMapper(eq(copyServiceInfoJson), any(User.class), ArgumentMatchers.<Class<CopyServiceInfo>>any(),
                nullable(AuditingActionEnum.class), nullable(ComponentTypeEnum.class)))
                .thenReturn(Either.left(copyServiceInfo));
        when(elementBusinessLogic.getCatalogComponentsByUuidAndAssetType(anyString(),anyString()))
                .thenReturn(Either.left(componentList));
        when(serviceBusinessLogic.createService(any(Service.class),any(User.class)))
                .thenReturn(Either.left((Service) componentList.get(0)));

        when(responseFormat.getStatus()).thenReturn(HttpStatus.SC_OK);
        when(componentsUtils.getResponseFormat(ActionStatus.CREATED)).thenReturn(responseFormat);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers)
                .post(Entity.entity(copyServiceInfo, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void testCopyExistServiceFailure_500(){
        String serviceUUID = "serviceUUID";
        MultivaluedMap<String,Object> headers = new MultivaluedHashMap<>();
        headers.putSingle(Constants.X_ECOMP_REQUEST_ID_HEADER, "mockXEcompRequestID");
        headers.putSingle(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId");
        headers.putSingle("USER_ID", user.getUserId());
        CopyServiceInfo copyServiceInfo = new CopyServiceInfo();
        copyServiceInfo.setOldServiceUUid("mockOldServiceUUid");
        copyServiceInfo.setNewServiceUUid("mockNewServiceUUid");
        copyServiceInfo.setNewServiceName("mockNewServiceName");
        ObjectMapper mapper = new ObjectMapper();
        String copyServiceInfoJson = null;
        try {
            copyServiceInfoJson = mapper.writeValueAsString(copyServiceInfo);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        List<Component> componentList = new ArrayList<Component>();
        Component service = createServiceObject(false);
        componentList.add(service);

        String path = "/v1/catalog/abstract/service/serviceUUID/" + serviceUUID + "/copy";

        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
        when(componentsUtils.convertJsonToObjectUsingObjectMapper(eq(copyServiceInfoJson), any(User.class), ArgumentMatchers.<Class<CopyServiceInfo>>any(),
                nullable(AuditingActionEnum.class), nullable(ComponentTypeEnum.class)))
                .thenReturn(Either.left(copyServiceInfo));
        when(elementBusinessLogic.getCatalogComponentsByUuidAndAssetType(anyString(),anyString()))
                .thenReturn(Either.right(new ResponseFormat(500)));
        when(serviceBusinessLogic.createService(any(Service.class),any(User.class)))
                .thenReturn(Either.left((Service) componentList.get(0)));

        when(responseFormat.getStatus()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers)
                .post(Entity.entity(copyServiceInfo, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Override
    protected Application configure() {
        ApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return new ResourceConfig()
                .register(new AbstractTemplateServlet(userBusinessLogic, componentInstanceBusinessLogic,
                        componentsUtils,servletUtils,resourceImportManager, elementBusinessLogic,
                        abstractTemplateBusinessLogic, serviceBusinessLogic, resourceBusinessLogic))
                .register(new AbstractBinder() {

                    @Override
                    protected void configure() {
                        bind(request).to(HttpServletRequest.class);
                    }
                })
                .property("contextConfig", context);
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