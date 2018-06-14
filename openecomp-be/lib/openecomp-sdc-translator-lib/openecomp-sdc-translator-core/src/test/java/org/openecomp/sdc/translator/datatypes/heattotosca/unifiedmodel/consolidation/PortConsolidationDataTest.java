/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.heat.datatypes.model.Resource;

public class PortConsolidationDataTest {

    private static final String PORT_NODE_TYPE_1 = "portNodeType1";
    private static final String PORT_NODE_TYPE_2 = "portNodeType2";
    private static final String PORT_NODE_TEMPLATE_ID_1 = "portNodeTemplateId1";
    private static final String PORT_NODE_TEMPLATE_ID_2 = "portNodeTemplateId2";
    private static final String SUB_INTERFACE_NODE_TEMPLATE_ID_1 = "subInterfaceNodeTemplateId1";
    private static final String SUB_INTERFACE_NODE_TEMPLATE_ID_2 = "subInterfaceNodeTemplateId2";
    private static final String PORT_RESOURCE_ID = "portResourceId";
    private static final String PORT_RESOURCE_TYPE = "portResourceType";

    private final EnumMap<ServiceTemplateFileNameEnum, FilePortConsolidationData> mockMap =
            new EnumMap<>(ServiceTemplateFileNameEnum.class);

    @Mock
    private FilePortConsolidationData mockFilePortConsolidationData1;
    @Mock
    private FilePortConsolidationData mockFilePortConsolidationData2;

    private final Resource resource = new Resource();
    private final PortConsolidationData consolidationData = new PortConsolidationData();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        addMocksToMap();
    }

    @Test
    public void testAddConsolidationData_noNewCreated() {
        String serviceTemplateName = ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1.name();
        consolidationData.addPortTemplateConsolidationData(serviceTemplateName, PORT_NODE_TYPE_1,
                PORT_RESOURCE_ID, PORT_RESOURCE_TYPE);
        consolidationData.getFilePortConsolidationData(serviceTemplateName);
        Set<String> expectedServiceTemplateNames = new HashSet<>();
        expectedServiceTemplateNames.add(serviceTemplateName);
        checkPortConsolidationData(consolidationData, serviceTemplateName, expectedServiceTemplateNames);

        consolidationData.addPortTemplateConsolidationData(serviceTemplateName, PORT_NODE_TYPE_1,
                PORT_RESOURCE_ID, PORT_RESOURCE_TYPE);
        consolidationData.getFilePortConsolidationData(serviceTemplateName);
        checkPortConsolidationData(consolidationData, serviceTemplateName, expectedServiceTemplateNames);
    }

    @Test
    public void testAddConsolidationData_DiffNodeTypes() {
        String serviceTemplateName = ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1.name();
        consolidationData.addPortTemplateConsolidationData(serviceTemplateName, PORT_NODE_TYPE_1,
                PORT_RESOURCE_ID, PORT_RESOURCE_TYPE);
        Set<String> expectedServiceTemplateNames = new HashSet<>();
        expectedServiceTemplateNames.add(serviceTemplateName);
        checkPortConsolidationData(consolidationData, serviceTemplateName, expectedServiceTemplateNames);
        consolidationData.addPortTemplateConsolidationData(serviceTemplateName, PORT_NODE_TYPE_2,
                PORT_RESOURCE_ID, PORT_RESOURCE_TYPE);
        checkPortConsolidationData(consolidationData, serviceTemplateName, expectedServiceTemplateNames);
    }

    @Test
    public void testAddConsolidationData_DiffServiceTemplate() {
        String serviceTemplateName1 = ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1.name();
        consolidationData.addPortTemplateConsolidationData(
                serviceTemplateName1, PORT_NODE_TYPE_1, PORT_RESOURCE_ID, PORT_RESOURCE_TYPE);
        Set<String> expectedServiceTemplateNames =  new HashSet<>();
        expectedServiceTemplateNames.add(serviceTemplateName1);
        checkPortConsolidationData(consolidationData, serviceTemplateName1, expectedServiceTemplateNames);

        String serviceTemplateName2 = ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_2.name();
        consolidationData.addPortTemplateConsolidationData(
                serviceTemplateName2, PORT_NODE_TYPE_2, PORT_RESOURCE_ID, PORT_RESOURCE_TYPE);
        expectedServiceTemplateNames.add(serviceTemplateName2);
        checkPortConsolidationData(consolidationData, serviceTemplateName2, expectedServiceTemplateNames);
    }

    @Test
    public void testAddConsolidationData_DiffNodeTypes_testWithMock() throws Exception {
        String serviceTemplateName = ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1.name();
        addAndCheckPortTemplateConsolidationData(serviceTemplateName,
                PORT_NODE_TYPE_1);
        addAndCheckPortTemplateConsolidationData(serviceTemplateName,
                PORT_NODE_TYPE_2);
    }

    @Test
    public void testAddConsolidationData_DiffServiceTemplate_testWithMock() throws Exception {
        addAndCheckPortTemplateConsolidationData(ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1.name(),
                PORT_NODE_TYPE_1);
        addAndCheckPortTemplateConsolidationData(ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_2.name(),
                PORT_NODE_TYPE_2);
    }

    @Test
    public void testAddSubInterfaceConsolidationData_Same() throws Exception {
        String serviceTemplateName = ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1.name();
        addAndCheckSubInterfaceTemplateConsolidationData(serviceTemplateName,
                SUB_INTERFACE_NODE_TEMPLATE_ID_1, PORT_NODE_TEMPLATE_ID_1);
        addAndCheckSubInterfaceTemplateConsolidationData(serviceTemplateName,
                SUB_INTERFACE_NODE_TEMPLATE_ID_1, PORT_NODE_TEMPLATE_ID_1, 2);
    }

    @Test
    public void testAddSubInterfaceConsolidationData_diffNodeTempId() throws Exception {
        String serviceTemplateName = ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1.name();
        addAndCheckSubInterfaceTemplateConsolidationData(serviceTemplateName,
                SUB_INTERFACE_NODE_TEMPLATE_ID_1, PORT_NODE_TEMPLATE_ID_1);
        addAndCheckSubInterfaceTemplateConsolidationData(serviceTemplateName,
                SUB_INTERFACE_NODE_TEMPLATE_ID_2, PORT_NODE_TEMPLATE_ID_1);
    }

    @Test
    public void testAddSubInterfaceConsolidationData_diffNodeTempId_diffParentPort() throws Exception {
        String serviceTemplateName = ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1.name();
        addAndCheckSubInterfaceTemplateConsolidationData(serviceTemplateName,
                SUB_INTERFACE_NODE_TEMPLATE_ID_1, PORT_NODE_TEMPLATE_ID_1);
        addAndCheckSubInterfaceTemplateConsolidationData(serviceTemplateName,
                SUB_INTERFACE_NODE_TEMPLATE_ID_2, PORT_NODE_TEMPLATE_ID_2);
    }

    @Test
    public void testAddSubInterfaceConsolidationData_diffServiceTemp() throws Exception {
        addAndCheckSubInterfaceTemplateConsolidationData(
                ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1.name(),
                SUB_INTERFACE_NODE_TEMPLATE_ID_1, SUB_INTERFACE_NODE_TEMPLATE_ID_1);
        addAndCheckSubInterfaceTemplateConsolidationData(
                ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_2.name(),
                SUB_INTERFACE_NODE_TEMPLATE_ID_2, SUB_INTERFACE_NODE_TEMPLATE_ID_2);
    }

    private void addAndCheckPortTemplateConsolidationData(String serviceTemplateFileName,
            String portNodeType) throws Exception {
        FilePortConsolidationData filePortConsolidationDataMock =
                setFilePortConsolidationDataMock(serviceTemplateFileName);

        consolidationData.addPortTemplateConsolidationData(
                serviceTemplateFileName, portNodeType, PORT_RESOURCE_ID, PORT_RESOURCE_TYPE);

        Mockito.verify(filePortConsolidationDataMock)
               .addPortTemplateConsolidationData(portNodeType, PORT_RESOURCE_ID, PORT_RESOURCE_TYPE);
    }

    private void addAndCheckSubInterfaceTemplateConsolidationData(String serviceTemplateFileName,
            String subInterfaceNodeTemplateId, String subInterfaceNodeTemplateType) throws Exception {
        addAndCheckSubInterfaceTemplateConsolidationData(serviceTemplateFileName, subInterfaceNodeTemplateId,
                subInterfaceNodeTemplateType, 1);
    }

    private void addAndCheckSubInterfaceTemplateConsolidationData(String serviceTemplateFileName,
            String subInterfaceNodeTemplateId, String parentPortNodeTemplateId, int expectedTime) throws Exception {

        FilePortConsolidationData filePortConsolidationDataMock =
                setFilePortConsolidationDataMock(serviceTemplateFileName);
        consolidationData.addSubInterfaceTemplateConsolidationData(serviceTemplateFileName, resource,
                subInterfaceNodeTemplateId, parentPortNodeTemplateId);

        Mockito.verify(filePortConsolidationDataMock, Mockito.times(expectedTime))
                .addSubInterfaceTemplateConsolidationData(resource, subInterfaceNodeTemplateId,
                        parentPortNodeTemplateId);
    }

    private void addMocksToMap() {
        mockMap.put(ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1, mockFilePortConsolidationData1);
        mockMap.put(ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_2, mockFilePortConsolidationData2);
    }

    private FilePortConsolidationData setFilePortConsolidationDataMock(String serviceTemplateName) throws Exception {
        FilePortConsolidationData filePortConsolidationDataMock =
                getFileComputeConsolidationDataMock(serviceTemplateName);
        consolidationData.setFilePortConsolidationData(serviceTemplateName, filePortConsolidationDataMock);
        return filePortConsolidationDataMock;
    }

    private FilePortConsolidationData getFileComputeConsolidationDataMock(String serviceTemplateName) throws Exception {
        ServiceTemplateFileNameEnum enumValue = ServiceTemplateFileNameEnum.valueOf(serviceTemplateName);
        FilePortConsolidationData mock = mockMap.get(enumValue);
        if (mock == null) {
            throw new Exception("This service Template File Name doesn't supported. "
                                        + "Please add it to ServiceTemplateFileName enum");
        }
        return mock;
    }

    private void checkPortConsolidationData(PortConsolidationData consolidationData,
            String serviceTemplateFileName, Set<String> expectedServiceTemplateNames) {
        FilePortConsolidationData filePortConsolidationData = consolidationData
                .getFilePortConsolidationData(serviceTemplateFileName);
        Assert.assertNotNull(filePortConsolidationData);
        checkGetAllServiceTemplateFileNames(consolidationData, expectedServiceTemplateNames);
    }

    private void checkGetAllServiceTemplateFileNames(PortConsolidationData consolidationData,
                Set<String> expectedServiceTemplateNames) {
        Set<String> allServiceTemplateFileNames = consolidationData.getAllServiceTemplateFileNames();
        Assert.assertNotNull(allServiceTemplateFileNames);
        Assert.assertEquals(expectedServiceTemplateNames.size(), allServiceTemplateFileNames.size());
        Assert.assertTrue(allServiceTemplateFileNames.containsAll(expectedServiceTemplateNames));
    }
}
