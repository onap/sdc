/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.sdc.backend.ci.tests.execute.interfaceoperation;

import static org.testng.AssertJUnit.fail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import fj.data.Either;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationOutputDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.utils.general.AtomicOperationUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.rest.BaseRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.InterfaceOperationsRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ResponseParser;
import org.onap.sdc.backend.ci.tests.utils.rest.ServiceRestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class InterfaceOperationsTest extends ComponentBaseTest {

    @Rule
    private static final TestName name = new TestName();
    private static final String INTERFACES = "interfaces";
    private static final String TOSCA_PRESENTATION = "toscaPresentation";
    private static final User user = new ElementFactory().getDefaultUser(UserRoleEnum.DESIGNER);
    private static final String WORKFLOW_ID_STR = "WorkflowId";
    private static final String WORKFLOW_VERSION_ID_STR = "workflowVersionId";
    private static final String WORKFLOW_ASSOCIATION_TYPE_NONE_STR = "NONE";

    private static Service service;
    private static Resource resource;
    private static Resource pnfResource;
    private String resourceInterfaceUniqueId;
    private String resourceOperationUniqueId;
    private String pnfResourceInterfaceUniqueId;
    private String pnfResourceOperationUniqueId;
    private String serviceInterfaceUniqueId;
    private String serviceOperationUniqueId;

    @BeforeClass
    public static void init() throws Exception {
        // Create default service
        Either<Service, RestResponse> createDefaultServiceEither =
                new AtomicOperationUtils().createDefaultService(UserRoleEnum.DESIGNER, true);
        if (createDefaultServiceEither.isRight()) {
            fail("Error creating default service");
        }
        service = createDefaultServiceEither.left().value();

        // Create default resource
        Either<Resource, RestResponse> createDefaultResourceEither =
                new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true);
        if (createDefaultResourceEither.isRight()) {
            fail("Error creating default resource");
        }
        resource = createDefaultResourceEither.left().value();

        // Create default PNF resource
        Either<Resource, RestResponse> createDefaultPNFResourceEither =
                new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.PNF, UserRoleEnum.DESIGNER, true);
        if (createDefaultPNFResourceEither.isRight()) {
            fail("Error creating default pnf resource");
        }
        pnfResource = createDefaultPNFResourceEither.left().value();
    }

    public Map<String, Object> buildInterfaceDefinitionForResource(Resource resource,
                                                                    String resourceInterfaceUniqueId,
                                                                    String resourceOperationUniqueId) {
        Operation operation = new Operation();
        operation.setName("TestOperationOnResource");
        operation.setWorkflowId(WORKFLOW_ID_STR);
        operation.setWorkflowVersionId(WORKFLOW_VERSION_ID_STR);
        operation.setWorkflowAssociationType(WORKFLOW_ASSOCIATION_TYPE_NONE_STR);
        if(CollectionUtils.isNotEmpty(resource.getInputs())){
            PropertyDefinition property =
                    resource.getInputs().stream().filter(a -> a.getName().equalsIgnoreCase("nf_naming")).findFirst()
                            .orElse(new InputDefinition());
            ListDataDefinition<OperationInputDefinition> operationInputDefinitionList = new ListDataDefinition<>();
            operationInputDefinitionList.add(createOperationInputDefinition("TestInput1", property.getUniqueId()));
            operation.setInputs(operationInputDefinitionList);
        }
        ListDataDefinition<OperationOutputDefinition> operationOutputDefinitionList = new ListDataDefinition<>();
        operationOutputDefinitionList.add(createOperationOutputDefinition("TestOutput1"));
        operation.setOutputs(operationOutputDefinitionList);
        return buildInterfaceDefinitionMap(operation, "TestInterface", resourceInterfaceUniqueId,
                resourceOperationUniqueId);
    }

    private Map<String, Object> buildInterfaceDefinitionOfGlobalTypeForResource(Resource resource) {
        Operation operation = new Operation();
        operation.setName("create");
        operation.setWorkflowId(WORKFLOW_ID_STR);
        operation.setWorkflowVersionId(WORKFLOW_VERSION_ID_STR);
        operation.setWorkflowAssociationType("NONE");
        if(CollectionUtils.isNotEmpty(resource.getInputs())){
            PropertyDefinition property =
                    resource.getInputs().stream().filter(a -> a.getName().equalsIgnoreCase("nf_naming")).findFirst()
                            .orElse(new InputDefinition());
            ListDataDefinition<OperationInputDefinition> operationInputDefinitionList = new ListDataDefinition<>();
            operationInputDefinitionList.add(createOperationInputDefinition("TestInput1", property.getUniqueId()));
            operation.setInputs(operationInputDefinitionList);
        }
        ListDataDefinition<OperationOutputDefinition> operationOutputDefinitionList = new ListDataDefinition<>();
        operationOutputDefinitionList.add(createOperationOutputDefinition("TestOutput1"));
        operation.setOutputs(operationOutputDefinitionList);
        return buildInterfaceDefinitionMap(operation, "tosca.interfaces.node.lifecycle.Standard",
                resourceInterfaceUniqueId, resourceOperationUniqueId);
    }

    private OperationInputDefinition createOperationInputDefinition(String name, String inputId) {
        OperationInputDefinition operationInputDefinition = new OperationInputDefinition();
        operationInputDefinition.setName(name);
        operationInputDefinition.setInputId(inputId);
        operationInputDefinition.setRequired(true);
        operationInputDefinition.setType("string");
        return operationInputDefinition;
    }

    private OperationOutputDefinition createOperationOutputDefinition(String name) {
        OperationOutputDefinition operationOutputDefinition = new OperationOutputDefinition();
        operationOutputDefinition.setName(name);
        operationOutputDefinition.setRequired(true);
        operationOutputDefinition.setType("string");
        return operationOutputDefinition;
    }

    private Map<String, Object> buildInterfaceDefinitionMap(Operation operation, String interfaceType,
                                                            String interfaceId,
                                                            String operationId) {
        if (operationId != null) {
            operation.setUniqueId(operationId);
        }
        Map<String, Operation> operationMap = new HashMap<>();
        operationMap.put(operation.getName(), operation);

        InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setType(interfaceType);
        interfaceDefinition.setOperationsMap(operationMap);
        if (interfaceId != null) {
            interfaceDefinition.setUniqueId(interfaceId);
        }
        interfaceDefinition.setOperationsMap(operationMap);

        Map<String, Object> interfaceDefAsMap = getObjectAsMap(interfaceDefinition);
        Map<String, Object> interfaceMap = new HashMap<>();
        interfaceMap.put(interfaceDefinition.getType(), interfaceDefAsMap);
        Map<String, Object> outerMap = new HashMap<>();
        outerMap.put(INTERFACES, interfaceMap);
        return outerMap;
    }

    private static Map<String, Object> getObjectAsMap(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (obj instanceof InterfaceDefinition) {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
        Map<String, Object> objectAsMap =
                obj instanceof Map ? (Map<String, Object>) obj : objectMapper.convertValue(obj, Map.class);
        objectAsMap.remove(TOSCA_PRESENTATION);
        return objectAsMap;
    }

    public Map<String, Object> buildInterfaceDefinitionForService() {
        Operation operation = new Operation();
        operation.setName("TestOperationOnService");
        operation.setWorkflowId(WORKFLOW_ID_STR);
        operation.setWorkflowVersionId(WORKFLOW_VERSION_ID_STR);
        operation.setWorkflowAssociationType("NONE");
        return buildInterfaceDefinitionMap(operation, "TestInterface", serviceInterfaceUniqueId,
                serviceOperationUniqueId);
    }

    @Test
    public void addInterfaceOperationsOnResource() throws Exception {
        RestResponse restResponse = InterfaceOperationsRestUtils
                .addInterfaceOperations(resource,
                        buildInterfaceDefinitionForResource(resource, resourceInterfaceUniqueId,
                                resourceOperationUniqueId), user);
        logger.info("addInterfaceOperationsOnResource Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
        String interfaceDefinitionStr = ResponseParser.getListFromJson(restResponse, INTERFACES).get(0).toString();
        InterfaceDefinition interfaceDefinition =
                ResponseParser.convertInterfaceDefinitionResponseToJavaObject(interfaceDefinitionStr);
        resourceInterfaceUniqueId = interfaceDefinition.getUniqueId();
        resourceOperationUniqueId = interfaceDefinition.getOperationsMap().keySet().stream().findFirst().orElse(null);
    }

    @Test(dependsOnMethods = "addInterfaceOperationsOnResource")
    public void getInterfaceOperationsFromResource() throws Exception {
        RestResponse restResponse = InterfaceOperationsRestUtils
                .getInterfaceOperations(resource, resourceInterfaceUniqueId,
                        resourceOperationUniqueId, user);
        logger.info("getInterfaceOperationsFromResource Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "getInterfaceOperationsFromResource")
    public void updateInterfaceOperationsOnResource() throws Exception {
        RestResponse restResponse = InterfaceOperationsRestUtils
                .updateInterfaceOperations(resource,
                        buildInterfaceDefinitionForResource(resource, resourceInterfaceUniqueId, resourceOperationUniqueId),
                        user);
        logger.info("updateInterfaceOperationsOnResource Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "updateInterfaceOperationsOnResource")
    public void deleteInterfaceOperationsFromResource() throws Exception {
        RestResponse restResponse = InterfaceOperationsRestUtils
                .deleteInterfaceOperations(resource, resourceInterfaceUniqueId,
                        resourceOperationUniqueId, user);
        logger.info("deleteInterfaceOperationsFromResource Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test
    public void addInterfaceOperationsOnPNFResource() throws Exception {
        RestResponse restResponse = InterfaceOperationsRestUtils
                .addInterfaceOperations(pnfResource, buildInterfaceDefinitionForResource(pnfResource, pnfResourceInterfaceUniqueId,
                        pnfResourceOperationUniqueId), user);
        logger.info("addInterfaceOperationsOnPNFResource Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
        String interfaceDefinitionStr = ResponseParser.getListFromJson(restResponse, INTERFACES).get(0).toString();
        InterfaceDefinition interfaceDefinition =
                ResponseParser.convertInterfaceDefinitionResponseToJavaObject(interfaceDefinitionStr);
        pnfResourceInterfaceUniqueId = interfaceDefinition.getUniqueId();
        pnfResourceOperationUniqueId =
                interfaceDefinition.getOperationsMap().keySet().stream().findFirst().orElse(null);
    }

    @Test(dependsOnMethods = "addInterfaceOperationsOnPNFResource")
    public void getInterfaceOperationsFromPNFResource() throws Exception {
        RestResponse restResponse = InterfaceOperationsRestUtils
                .getInterfaceOperations(pnfResource, pnfResourceInterfaceUniqueId,
                        pnfResourceOperationUniqueId, user);
        logger.info("getInterfaceOperationsFromPNFResource Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "getInterfaceOperationsFromPNFResource")
    public void updateInterfaceOperationsOnPNFResource() throws Exception {
        RestResponse restResponse = InterfaceOperationsRestUtils
                .updateInterfaceOperations(pnfResource,
                        buildInterfaceDefinitionForResource(pnfResource, pnfResourceInterfaceUniqueId,
                                pnfResourceOperationUniqueId), user);
        logger.info("updateInterfaceOperationsOnPNFResource Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "updateInterfaceOperationsOnPNFResource")
    public void deleteInterfaceOperationsFromPNFResource() throws Exception {
        RestResponse restResponse = InterfaceOperationsRestUtils
                .deleteInterfaceOperations(pnfResource, pnfResourceInterfaceUniqueId,
                        pnfResourceOperationUniqueId, user);
        logger.info("deleteInterfaceOperationsFromPNFResource Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test
    public void testCreateOperationWithLocalInterfaceAndDownloadArtifact() throws Exception{
        Either<Service, RestResponse> createDefaultServiceEither =
                new AtomicOperationUtils().createDefaultService(UserRoleEnum.DESIGNER, true);
        if (createDefaultServiceEither.isRight()) {
            fail("Error creating default service");
        }
        Service service = createDefaultServiceEither.left().value();
        String serviceUniqueId = service.getUniqueId();
        Operation operation = new Operation();
        operation.setName("LocalOper");
        operation.setWorkflowAssociationType("NONE");
        Map<String, Object> interfaceOperationMap = buildInterfaceDefinitionMap(operation, "Local", null, null);

        RestResponse restResponse = InterfaceOperationsRestUtils.addInterfaceOperations(service, interfaceOperationMap,
                user);

        Integer responseCode = restResponse.getErrorCode();
        Integer expectedCode = 200;
        Assert.assertEquals(responseCode, expectedCode);

        service = ResponseParser.convertServiceResponseToJavaObject(
                new ServiceRestUtils().getServiceToscaArtifacts(service.getUniqueId()).getResponse());
        service.setUniqueId(serviceUniqueId);
        service.setComponentType(ComponentTypeEnum.SERVICE);
        service.setLastUpdaterUserId(user.getUserId());
        Either<String, RestResponse> responseEither = new AtomicOperationUtils()
                .getComponenetArtifactPayload(service, "assettoscacsar");

        Assert.assertTrue(responseEither.isLeft());
    }

    @Test
    public void addInterfaceOperationsOnService() throws Exception {
        RestResponse restResponse = InterfaceOperationsRestUtils
                .addInterfaceOperations(service, buildInterfaceDefinitionForService(),
                        user);
        logger.info("addInterfaceOperationsOnService Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
        String interfaceDefinitionStr = ResponseParser.getListFromJson(restResponse, INTERFACES).get(0).toString();
        InterfaceDefinition interfaceDefinition =
                ResponseParser.convertInterfaceDefinitionResponseToJavaObject(interfaceDefinitionStr);
        serviceInterfaceUniqueId = interfaceDefinition.getUniqueId();
        serviceOperationUniqueId = interfaceDefinition.getOperationsMap().keySet().stream().findFirst().orElse(null);
    }

    @Test(dependsOnMethods = "addInterfaceOperationsOnService")
    public void getInterfaceOperationsFromService() throws Exception {
        RestResponse restResponse = InterfaceOperationsRestUtils
                .getInterfaceOperations(service, serviceInterfaceUniqueId,
                        serviceOperationUniqueId, user);
        logger.info("getInterfaceOperationsFromService Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "getInterfaceOperationsFromService")
    public void updateInterfaceOperationsOnService() throws Exception {
        RestResponse restResponse = InterfaceOperationsRestUtils
                .updateInterfaceOperations(service, buildInterfaceDefinitionForService(),
                        user);
        logger.info("updateInterfaceOperations Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "updateInterfaceOperationsOnService")
    public void deleteInterfaceOperationsFromService() throws Exception {
        RestResponse restResponse = InterfaceOperationsRestUtils
                .deleteInterfaceOperations(service, serviceInterfaceUniqueId,
                        serviceOperationUniqueId, user);
        logger.info("deleteInterfaceOperations Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test
    public void addInterfaceOperationsOfGlobalTypeOnResource() throws Exception {
        RestResponse restResponse =
                InterfaceOperationsRestUtils.addInterfaceOperations(resource,
                        buildInterfaceDefinitionOfGlobalTypeForResource(resource), user);

        logger.info("addInterfaceOperationsOnResource Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test
    public void addInterfaceOperationsOfGlobalTypeOnPNFResource() throws Exception {
        RestResponse restResponse =
                InterfaceOperationsRestUtils.addInterfaceOperations(pnfResource,
                        buildInterfaceDefinitionOfGlobalTypeForResource(pnfResource), user);

        logger.info("addInterfaceOperationsOnPNFResource Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
        String interfaceDefinitionStr = ResponseParser.getListFromJson(restResponse, INTERFACES).get(0).toString();
        InterfaceDefinition interfaceDefinition =
                ResponseParser.convertInterfaceDefinitionResponseToJavaObject(interfaceDefinitionStr);
    }

}
