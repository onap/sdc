/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (c) 2018 Huawei Intellectual Property.
 *  Modifications Copyright (c) 2019 Samsung.
 *  Modifications Copyright (c) 2021 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.csar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.BaseBusinessLogicMock;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.VendorSoftwareProduct;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CsarOperation;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.zip.ZipUtils;
import org.openecomp.sdc.common.zip.exception.ZipException;
import org.openecomp.sdc.exception.ResponseFormat;


class CsarBusinessLogicTest extends BaseBusinessLogicMock {

    private final CsarOperation csarOperation = Mockito.mock(CsarOperation.class);
    private final ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
    private final ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);
    private final User user = Mockito.mock(User.class);
    private final YamlTemplateParsingHandler yamlHandler = Mockito.mock(YamlTemplateParsingHandler.class);
    private final ModelOperation modelOperation = Mockito.mock(ModelOperation.class);

    private final CsarBusinessLogic csarBusinessLogic = new CsarBusinessLogic(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation,
        interfaceOperation, interfaceLifecycleTypeOperation, yamlHandler, artifactToscaOperation, modelOperation);

    private static final String CSAR_UUID = "csarUUID";
    private static final String CSAR_ENTRY = "Definitions/tosca_mock_vf.yaml";
    private static final String CSAR_METADATA = "TOSCA-Metadata/TOSCA.meta";
    private static final String CSAR_METADATA_CONTENT = "TOSCA-Meta-File-Version: 1.0\n" +
            "CSAR-Version: 1.1\n" +
            "Created-By: OASIS TOSCA TC\n" +
            "Entry-Definitions:" + CSAR_ENTRY;
    private static final String CSAR_ENTRY_CONTENT = "tosca_definitions_version: tosca_simple_yaml_1_0\n";

    private static final String RESOURCE_NAME = "resourceName";
    private static final String PAYLOAD_NAME = "mock_vf.csar";

    @BeforeEach
    void setUp() throws Exception {
        csarBusinessLogic.setCsarOperation(csarOperation);
        csarBusinessLogic.setToscaOperationFacade(toscaOperationFacade);
        csarBusinessLogic.setComponentsUtils(componentsUtils);
    }

    @Test
    void testGetCsarInfo() {
        // given
        Resource resource = new Resource();
        resource.setName(RESOURCE_NAME);
        resource.setCsarUUID(CSAR_UUID);
        resource.setCsarVersionId("csarVersionId");

        Map<String, byte[]> csar_data = new HashMap<>();
        csar_data.put(CSAR_METADATA, CSAR_METADATA_CONTENT.getBytes());
        csar_data.put(CSAR_ENTRY, CSAR_ENTRY_CONTENT.getBytes());
        final var vendorSoftwareProduct = new VendorSoftwareProduct();
        vendorSoftwareProduct.setFileMap(csar_data);
        vendorSoftwareProduct.setModelList(Collections.emptyList());
        when(csarOperation.findVsp(eq(resource.getCsarUUID()), eq(resource.getCsarVersionId()), any(User.class)))
            .thenReturn(Optional.of(vendorSoftwareProduct));

        // when
        final CsarInfo csarInfo = csarBusinessLogic.getCsarInfo(resource, null, user, null, CSAR_UUID);

        // then
        assertNotNull(csarInfo);

        assertEquals(resource.getCsarUUID(), csarInfo.getCsarUUID());
        assertEquals(resource.getCsarVersionId(), csarInfo.getCsarVersionId());
        assertEquals(CSAR_ENTRY, csarInfo.getMainTemplateName());
        assertEquals(RESOURCE_NAME, csarInfo.getVfResourceName());

        assertEquals(CSAR_ENTRY_CONTENT, csarInfo.getMainTemplateContent());
        assertTrue(csarInfo.getCsar().keySet().containsAll(Arrays.asList(CSAR_ENTRY, CSAR_METADATA)));
    }

    @Test
    void testGetCsarInfo_vspWithModelAndResourceWithInvalidModel() {
        final var resource = new Resource();
        resource.setCsarUUID(CSAR_UUID);
        final String csarVersionId = "csarVersionId";
        resource.setCsarVersionId(csarVersionId);
        resource.setModel("model1");
        var vendorSoftwareProduct = new VendorSoftwareProduct();
        final List<String> modelList = List.of("model2", "model3");
        vendorSoftwareProduct.setModelList(modelList);

        when(csarOperation.findVsp(resource.getCsarUUID(), resource.getCsarVersionId(), user)).thenReturn(Optional.of(vendorSoftwareProduct));

        final ByActionStatusComponentException actualException = assertThrows(ByActionStatusComponentException.class,
            () -> csarBusinessLogic.getCsarInfo(resource, null, user, null, CSAR_UUID));
        assertEquals(ActionStatus.VSP_MODEL_NOT_ALLOWED, actualException.getActionStatus());
        assertEquals(2, actualException.getParams().length);
        assertEquals(resource.getModel(), actualException.getParams()[0]);
        assertEquals(String.join(", ", modelList), actualException.getParams()[1]);
    }


    @Test
    void testGetCsarInfo_vspWithNoModelAndResourceWithInvalidModel() {
        final var resource = new Resource();
        resource.setCsarUUID(CSAR_UUID);
        final String csarVersionId = "csarVersionId";
        resource.setCsarVersionId(csarVersionId);
        resource.setModel("model1");
        var vendorSoftwareProduct = new VendorSoftwareProduct();
        final List<String> modelList = new ArrayList<>();
        vendorSoftwareProduct.setModelList(modelList);

        when(csarOperation.findVsp(resource.getCsarUUID(), resource.getCsarVersionId(), user)).thenReturn(Optional.of(vendorSoftwareProduct));

        final ByActionStatusComponentException actualException = assertThrows(ByActionStatusComponentException.class,
            () -> csarBusinessLogic.getCsarInfo(resource, null, user, null, CSAR_UUID));
        assertEquals(ActionStatus.VSP_MODEL_NOT_ALLOWED, actualException.getActionStatus());
        assertEquals(2, actualException.getParams().length);
        assertEquals(resource.getModel(), actualException.getParams()[0]);
        assertEquals("SDC AID", actualException.getParams()[1]);
    }

    @Test
    void testGetCsarInfo_vspWithModelAndResourceWithNoModel() {
        final var resource = new Resource();
        resource.setCsarUUID(CSAR_UUID);
        final String csarVersionId = "csarVersionId";
        resource.setCsarVersionId(csarVersionId);
        resource.setModel(null);
        var vendorSoftwareProduct = new VendorSoftwareProduct();
        final List<String> modelList = List.of("model2", "model3");
        vendorSoftwareProduct.setModelList(modelList);

        when(csarOperation.findVsp(resource.getCsarUUID(), resource.getCsarVersionId(), user)).thenReturn(Optional.of(vendorSoftwareProduct));

        final ByActionStatusComponentException actualException = assertThrows(ByActionStatusComponentException.class,
            () -> csarBusinessLogic.getCsarInfo(resource, null, user, null, CSAR_UUID));
        assertEquals(ActionStatus.VSP_MODEL_NOT_ALLOWED, actualException.getActionStatus());
        assertEquals(2, actualException.getParams().length);
        assertEquals("SDC AID", actualException.getParams()[0]);
        assertEquals(String.join(", ", modelList), actualException.getParams()[1]);
    }

    @Test
    void testGetCsarInfo_vspWithNoModelAndResourceWithNoModel() {
        final var resource = new Resource();
        resource.setCsarUUID(CSAR_UUID);
        final String csarVersionId = "csarVersionId";
        resource.setCsarVersionId(csarVersionId);
        resource.setModel(null);
        var vendorSoftwareProduct = new VendorSoftwareProduct();
        final List<String> modelList = new ArrayList<>();
        vendorSoftwareProduct.setModelList(modelList);
        when(csarOperation.findVsp(resource.getCsarUUID(), resource.getCsarVersionId(), user)).thenThrow(new RuntimeException());

        final ByActionStatusComponentException actualException = assertThrows(ByActionStatusComponentException.class,
            () -> csarBusinessLogic.getCsarInfo(resource, null, user, null, CSAR_UUID));
        assertEquals(ActionStatus.VSP_FIND_ERROR, actualException.getActionStatus());
        assertEquals(2, actualException.getParams().length);
        assertEquals(resource.getCsarUUID(), actualException.getParams()[0]);
        assertEquals(resource.getCsarVersionId(), actualException.getParams()[1]);
    }

    @Test
    void testGetCsarInfoWithPayload() throws IOException, URISyntaxException, ZipException {
        // given
        Resource resource = new Resource();
        resource.setName(RESOURCE_NAME);

        Map<String, byte[]> payload = loadPayload(PAYLOAD_NAME);

        // when
        CsarInfo csarInfo = csarBusinessLogic.getCsarInfo(resource, null, user, payload, CSAR_UUID);

        // then
        assertNotNull(csarInfo);

        assertEquals(CSAR_UUID, csarInfo.getCsarUUID());
        assertEquals(CSAR_ENTRY, csarInfo.getMainTemplateName());
        assertEquals(RESOURCE_NAME, csarInfo.getVfResourceName());

        assertTrue(csarInfo.getMainTemplateContent().startsWith(CSAR_ENTRY_CONTENT));
        assertTrue(csarInfo.getCsar().keySet().containsAll(Arrays.asList(CSAR_ENTRY, CSAR_METADATA)));
    }

    @Test
    void testGetCsarInfoWithBadData(){
        // given
        Resource resource = new Resource();
        resource.setName(RESOURCE_NAME);

        Map<String, byte[]> csar_data = new HashMap<>();
        when(csarOperation.findVspLatestPackage(anyString(), any(User.class))).thenReturn(Either.left(csar_data));

        // when/then
        assertThrows(ComponentException.class, () -> csarBusinessLogic.getCsarInfo(resource, null, user, null, CSAR_UUID));
    }

    @Test
    void testValidateCsarBeforeCreate() {
        Resource resource = new Resource();
        StorageOperationStatus status = StorageOperationStatus.OK;
        when(toscaOperationFacade.validateCsarUuidUniqueness(CSAR_UUID)).thenReturn(status);
        csarBusinessLogic.validateCsarBeforeCreate(resource, AuditingActionEnum.ARTIFACT_DOWNLOAD, user, CSAR_UUID);
    }

    @Test
    void testValidateCsarBeforeCreate_ResourceExists() {
        Resource resource = new Resource();
        ResponseFormat responseFormat = new ResponseFormat();
        StorageOperationStatus status = StorageOperationStatus.ENTITY_ALREADY_EXISTS;
        when(toscaOperationFacade.validateCsarUuidUniqueness(CSAR_UUID)).thenReturn(status);
        when(componentsUtils.getResponseFormat(ActionStatus.VSP_ALREADY_EXISTS, CSAR_UUID)).thenReturn(responseFormat);
        assertThrows(ComponentException.class, () -> csarBusinessLogic
            .validateCsarBeforeCreate(resource, AuditingActionEnum.ARTIFACT_DOWNLOAD, user, CSAR_UUID));
    }

    @Test
    void testValidateCsarBeforeCreate_ServiceExists() {
        final var  service = new Service();
        final var status = StorageOperationStatus.ENTITY_ALREADY_EXISTS;
        when(toscaOperationFacade.validateCsarUuidUniqueness(CSAR_UUID)).thenReturn(status);
        csarBusinessLogic.validateCsarBeforeCreate(service, CSAR_UUID);
        verify(toscaOperationFacade).validateCsarUuidUniqueness(CSAR_UUID);
    }

    @Test
    void testValidateCsarBeforeCreate_ServiceValidateError() {
        final var service = new Service();
        final var status = StorageOperationStatus.GENERAL_ERROR;
        when(toscaOperationFacade.validateCsarUuidUniqueness(CSAR_UUID)).thenReturn(status);
        when(componentsUtils.convertFromStorageResponse(status)).thenReturn(ActionStatus.GENERAL_ERROR);
        assertThrows(ComponentException.class, () -> csarBusinessLogic.validateCsarBeforeCreate(service, CSAR_UUID));
        verify(toscaOperationFacade).validateCsarUuidUniqueness(CSAR_UUID);
    }

    @Test
    void testValidateCsarBeforeCreate_Fail() {
        Resource resource = new Resource();
        String csarUUID = "csarUUID";
        when(toscaOperationFacade.validateCsarUuidUniqueness(csarUUID)).thenReturn(StorageOperationStatus.EXEUCTION_FAILED);
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.EXEUCTION_FAILED)).thenReturn(ActionStatus.GENERAL_ERROR);
        assertThrows(ComponentException.class, () -> csarBusinessLogic
            .validateCsarBeforeCreate(resource, AuditingActionEnum.ARTIFACT_DOWNLOAD, user, "csarUUID"));
    }

    private Map<String, byte[]> loadPayload(String payloadName) throws IOException, URISyntaxException, ZipException {
        var path = Paths.get(getClass().getResource("/" + payloadName).toURI());
        byte[] data = Files.readAllBytes(path);

        return ZipUtils.readZip(data, false);
    }
}
