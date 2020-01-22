/*

 * Copyright (c) 2018 Huawei Intellectual Property.

 * Modifications Copyright (c) 2019 Samsung

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

package org.openecomp.sdc.be.components.csar;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.BaseBusinessLogicMock;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CsarOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.zip.ZipUtils;
import org.openecomp.sdc.common.zip.exception.ZipException;
import org.openecomp.sdc.exception.ResponseFormat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


public class CsarBusinessLogicTest extends BaseBusinessLogicMock {
    static ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
            "src/test/resources/config/catalog-be");
    static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);


    private CsarOperation csarOperation = Mockito.mock(CsarOperation.class);
    private ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
    private ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);
    private User user = Mockito.mock(User.class);
    private YamlTemplateParsingHandler yamlHandler = Mockito.mock(YamlTemplateParsingHandler.class);

    private CsarBusinessLogic test = new CsarBusinessLogic(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation,
        groupBusinessLogic, interfaceOperation,  interfaceLifecycleTypeOperation, yamlHandler, artifactToscaOperation);

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

    @Before
    public void setUp() throws Exception {
        test.setCsarOperation(csarOperation);
        test.setToscaOperationFacade(toscaOperationFacade);
        test.setComponentsUtils(componentsUtils);
    }

    @Test()
    public void testGetCsarInfo() {
        // given
        Resource resource = new Resource();
        resource.setName(RESOURCE_NAME);

        Map<String, byte[]> csar_data = new HashMap<>();
        csar_data.put(CSAR_METADATA, CSAR_METADATA_CONTENT.getBytes());
        csar_data.put(CSAR_ENTRY, CSAR_ENTRY_CONTENT.getBytes());
        when(csarOperation.getCsar(anyString(), any(User.class))).thenReturn(Either.left(csar_data));

        // when
        CsarInfo csarInfo = test.getCsarInfo(resource, null, user, null, CSAR_UUID);

        // then
        assertNotNull(csarInfo);

        assertEquals(CSAR_UUID, csarInfo.getCsarUUID());
        assertEquals(CSAR_ENTRY, csarInfo.getMainTemplateName());
        assertEquals(RESOURCE_NAME, csarInfo.getVfResourceName());

        assertEquals(CSAR_ENTRY_CONTENT, csarInfo.getMainTemplateContent());
        assertTrue(csarInfo.getCsar().keySet().containsAll(Arrays.asList(CSAR_ENTRY, CSAR_METADATA)));
    }

    @Test()
    public void testGetCsarInfoWithPayload() throws IOException, URISyntaxException, ZipException {
        // given
        Resource resource = new Resource();
        resource.setName(RESOURCE_NAME);

        Map<String, byte[]> payload = loadPayload(PAYLOAD_NAME);

        // when
        CsarInfo csarInfo = test.getCsarInfo(resource, null, user, payload, CSAR_UUID);

        // then
        assertNotNull(csarInfo);

        assertEquals(CSAR_UUID, csarInfo.getCsarUUID());
        assertEquals(CSAR_ENTRY, csarInfo.getMainTemplateName());
        assertEquals(RESOURCE_NAME, csarInfo.getVfResourceName());

        assertTrue(csarInfo.getMainTemplateContent().startsWith(CSAR_ENTRY_CONTENT));
        assertTrue(csarInfo.getCsar().keySet().containsAll(Arrays.asList(CSAR_ENTRY, CSAR_METADATA)));
    }

    @Test(expected = ComponentException.class)
    public void testGetCsarInfoWithBadData(){
        // given
        Resource resource = new Resource();
        resource.setName(RESOURCE_NAME);

        Map<String, byte[]> csar_data = new HashMap<>();
        when(csarOperation.getCsar(anyString(), any(User.class))).thenReturn(Either.left(csar_data));

        // when
        test.getCsarInfo(resource, null, user, null, CSAR_UUID);
    }

    @Test
    public void testValidateCsarBeforeCreate() {
        Resource resource = new Resource();
        StorageOperationStatus status = StorageOperationStatus.OK;
        when(toscaOperationFacade.validateCsarUuidUniqueness(CSAR_UUID)).thenReturn(status);
        test.validateCsarBeforeCreate(resource, AuditingActionEnum.ARTIFACT_DOWNLOAD, user, CSAR_UUID);
    }

    @Test(expected = ComponentException.class)
    public void testValidateCsarBeforeCreate_Exists() {
        Resource resource = new Resource();
        ResponseFormat responseFormat = new ResponseFormat();
        StorageOperationStatus status = StorageOperationStatus.ENTITY_ALREADY_EXISTS;
        when(toscaOperationFacade.validateCsarUuidUniqueness(CSAR_UUID)).thenReturn(status);
        when(componentsUtils.getResponseFormat(ActionStatus.VSP_ALREADY_EXISTS, CSAR_UUID)).thenReturn(responseFormat);
        test.validateCsarBeforeCreate(resource, AuditingActionEnum.ARTIFACT_DOWNLOAD, user, "csarUUID");
    }

    @Test(expected = ComponentException.class)
    public void testValidateCsarBeforeCreate_Fail() {
        Resource resource = new Resource();
        String csarUUID = "csarUUID";
        when(toscaOperationFacade.validateCsarUuidUniqueness(csarUUID)).thenReturn(StorageOperationStatus.EXEUCTION_FAILED);
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.EXEUCTION_FAILED)).thenReturn(ActionStatus.GENERAL_ERROR);
        test.validateCsarBeforeCreate(resource, AuditingActionEnum.ARTIFACT_DOWNLOAD, user, "csarUUID");
    }

    public Map<String, byte[]> loadPayload(String payloadName) throws IOException, URISyntaxException, ZipException {

        Path path = Paths.get(getClass().getResource("/" + payloadName).toURI());
        byte[] data = Files.readAllBytes(path);

        return ZipUtils.readZip(data, false);
    }
}
