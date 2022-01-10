/*
 *
 *  Copyright © 2017-2018 European Support Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 *
 */

package org.openecomp.sdc.heat.services.tree;

import java.io.File;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;

public class HeatTreeManagerUtilTest {

    private static final String TEST_YML = "test.yml";
    private static final String TEST = "test";
    private static final String RESOURCE_DEF = "resource_def";

    @Mock
    private HeatTreeManager heatTreeManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInitHeatTreeManager() {
        FileContentHandler fileContentHandler = getFileContentHandler();

        Mockito.doNothing().when(heatTreeManager).addFile(Mockito.any(), Mockito.any());
        HeatTreeManagerUtil.initHeatTreeManager(fileContentHandler);
        Mockito.verify(heatTreeManager, Mockito.times(0)).addFile(Mockito.any(), Mockito.any());
    }

    @Test
    public void testGetNestedFiles() {
        HeatOrchestrationTemplate heatOrchestrationTemplate = new HeatOrchestrationTemplate();
        heatOrchestrationTemplate.setResources(getResourceMap("Type1.yml"));
        Set<String> nestedFilesSet = HeatTreeManagerUtil.getNestedFiles(heatOrchestrationTemplate);

        Assert.assertNotNull(nestedFilesSet);
        Assert.assertEquals(nestedFilesSet.size(), 1);
    }

    @Test
    public void testGetResourceDefNested() {
        HeatOrchestrationTemplate heatOrchestrationTemplate = new HeatOrchestrationTemplate();
        heatOrchestrationTemplate.setResources(getResourceMap("OS::Heat::ResourceGroup"));

        Set<String> nestedFilesSet = HeatTreeManagerUtil.getNestedFiles(heatOrchestrationTemplate);
        Assert.assertNotNull(nestedFilesSet);
        Assert.assertTrue(nestedFilesSet.size() ==  1 && nestedFilesSet.contains(TEST_YML));
    }

    @Test
    public void testGetArtifactFiles() {
        HeatOrchestrationTemplate heatOrchestrationTemplate = new HeatOrchestrationTemplate();
        heatOrchestrationTemplate.setResources(getResourceMap("Type1.yml"));
        Set<String> nestedFilesSet = HeatTreeManagerUtil.getArtifactFiles("filename.yml", heatOrchestrationTemplate,
                null);

        Assert.assertNotNull(nestedFilesSet);
        Assert.assertTrue(nestedFilesSet.contains(TEST));
    }

    @Test
    public void testGetResourceDefIfProvidedResourceIsNull() {
        Assert.assertNull(HeatTreeManagerUtil.getResourceDef(new Resource()));
    }

    @Test
    public void testGetResourceDef() {
        Resource resource = new Resource();

        Map<String, Object> resourceMap = new HashMap<>();
        Map<String, String> nestedResourceMap = new HashMap<String, String>() {{
            put("type", TEST_YML);
        }};

        resourceMap.put(RESOURCE_DEF, nestedResourceMap);
        resource.setProperties(resourceMap);
        Resource resultResource = HeatTreeManagerUtil.getResourceDef(resource);
        Assert.assertNotNull(resultResource);
        Assert.assertEquals(TEST_YML, resultResource.getType());
    }

    @Test
    public void testCheckResourceGroupTypeValid() {
        Resource resource = new Resource();

        Map<String, Object> resourceMap = new HashMap<>();
        Map<String, Object> nestedResourceMap = new HashMap<String, Object>() {{
            put("type", Collections.emptyList());
        }};

        resourceMap.put(RESOURCE_DEF, nestedResourceMap);
        resource.setProperties(resourceMap);

        GlobalValidationContext globalValidationContextMock = Mockito.mock(GlobalValidationContext.class);
        Mockito.doNothing().when(globalValidationContextMock).addMessage(Mockito.anyString(), Mockito.any(), Mockito
                .anyString());

        HeatTreeManagerUtil.checkResourceGroupTypeValid(TEST_YML, TEST, resource, globalValidationContextMock);

        Mockito.verify(globalValidationContextMock, Mockito.times(1))
                .addMessage(Mockito.anyString(), Mockito.any(), Mockito.anyString());
    }

    @Test
    public void testCheckResourceTypeValid() {
        Resource resource = new Resource();

        Map<String, Object> resourceMap = new HashMap<>();
        Map<String, Object> nestedResourceMap = new HashMap<String, Object>() {{
            put("properties", Collections.emptyList());
        }};

        resourceMap.put(RESOURCE_DEF, nestedResourceMap);
        resource.setProperties(resourceMap);

        GlobalValidationContext globalValidationContextMock = Mockito.mock(GlobalValidationContext.class);
        Mockito.doNothing().when(globalValidationContextMock).addMessage(Mockito.anyString(), Mockito.any(), Mockito
                .anyString());

        HeatTreeManagerUtil.checkResourceTypeValid(TEST_YML, TEST, resource, globalValidationContextMock);

        Mockito.verify(globalValidationContextMock, Mockito.times(1))
                .addMessage(Mockito.anyString(), Mockito.any(), Mockito.anyString());
    }

    @Test
    public void testCheckIfResourceGroupTypeIsNested() {
        Resource resource = new Resource();

        Map<String, Object> resourceMap = new HashMap<>();
        Map<String, Object> nestedResourceMap = new HashMap<String, Object>() {{
            put("type", TEST_YML);
        }};

        resourceMap.put(RESOURCE_DEF, nestedResourceMap);
        resource.setProperties(resourceMap);

        GlobalValidationContext globalValidationContextMock = Mockito.mock(GlobalValidationContext.class);
        Mockito.doNothing().when(globalValidationContextMock).addMessage(Mockito.anyString(), Mockito.any(), Mockito
                .anyString());

        boolean result = HeatTreeManagerUtil
                .checkIfResourceGroupTypeIsNested(TEST_YML, TEST, resource, globalValidationContextMock);

        Mockito.verify(globalValidationContextMock, Mockito.times(1))
                .addMessage(Mockito.anyString(), Mockito.any(), Mockito.anyString());

        Assert.assertTrue(result);
    }

    @Test
    public void testCheckIfResourceGroupTypeIsNestedNull() {
        Assert.assertFalse(HeatTreeManagerUtil.checkIfResourceGroupTypeIsNested(TEST_YML, TEST, new Resource(),
                null));
    }

    private FileContentHandler getFileContentHandler() {
        FileContentHandler fileContentHandler = new FileContentHandler();
        Map<String, byte[]> filesByteMap = new HashMap<>();
        List<URL> urlList = FileUtils.getAllLocations("mock/model");
        File files = new File(urlList.get(0).getPath());
        if (files.isDirectory()) {
            int fileCount = 0;
            for (File file : Objects.requireNonNull(files.listFiles())) {
                byte[] bytesArray = new byte[(int) file.length()];
                filesByteMap.put("File" + ++fileCount, bytesArray);
            }
        }

        fileContentHandler.setFiles(filesByteMap);

        return fileContentHandler;
    }

    private Map<String, Resource> getResourceMap(String type) {
        Resource resource = new Resource();
        resource.setType(type);
        Map<String, String> map = new HashMap<>();
        map.put("get_file", TEST);
        resource.setProperties(new HashMap<String, Object>() {{
            put("get_file", Collections.singletonList(map));
            put("resource_def", new HashMap<String, Object>() {{
                put("type", TEST_YML);
            }});
        }});


        return Stream.of(new AbstractMap.SimpleEntry<>("Res1", resource))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }
}
