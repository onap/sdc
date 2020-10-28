/*
 * Copyright © 2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdc.validation.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatStructureUtil;
import org.openecomp.sdc.heat.services.manifest.ManifestUtil;
import org.openecomp.sdc.validation.ResourceValidator;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.sdc.validation.type.ConfigConstants;
import org.openecomp.sdc.validation.util.ValidationUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ResourceBaseValidatorTest {
    private String testValidator = "testValidator";

    @Test
    public void testInvalidResourceType() {
        ResourceBaseValidator resourceBaseValidator = new ResourceBaseValidator();
        Map<String, MessageContainer> messages = testValidator(resourceBaseValidator, "/InvalidResourceType");
        assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
                "WARNING: [RBV1]: A resource has an invalid or unsupported type - null, Resource ID [FSB2]");
    }

    @Test
    public void testInvalidHeatStructure() {
        ResourceBaseValidator resourceBaseValidator = new ResourceBaseValidator();
        Map<String, MessageContainer> messages = testValidator(resourceBaseValidator, "/InvalidHeatStructure");
        assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
                "ERROR: [RBV2]: Invalid HEAT format problem - [while scanning for the next " +
                        "token\n" + "found character '\\t(TAB)' that cannot start any token. " +
                        "(Do not use \\t(TAB) for indentation)\n" +
                        " in 'reader', line 10, column 1:\n" +
                        "    \t\t\tresources:\n" +
                        "    ^\n" +
                        "]");
    }

    @Test
    public void testInitWithEmptyPropertiesMap() {
        ResourceBaseValidator resourceBaseValidator = new ResourceBaseValidator();
        Map<String, Object> properties = new HashMap<>();
        resourceBaseValidator.init(properties);
        assertTrue(MapUtils.isEmpty(resourceBaseValidator.getResourceTypeToImpl()));
    }

    @Test
    public void testInitPropertiesMap() {
        ResourceBaseValidator resourceBaseValidator = new ResourceBaseValidator();
        initProperties(resourceBaseValidator, getValidImplementationConfiguration());

        Map<String, ImplementationConfiguration> resourceTypeToImpl = resourceBaseValidator.getResourceTypeToImpl();
        assertTrue(MapUtils.isNotEmpty(resourceTypeToImpl));
        assertTrue(resourceTypeToImpl.containsKey(testValidator));
    }

    @Test
    public void testInitPropertiesWithString() {
        ResourceBaseValidator resourceBaseValidator = new ResourceBaseValidator();
        Map<String, Object> properties = new HashMap<>();
        properties.put(testValidator, "invalidValue");
        resourceBaseValidator.init(properties);
        assertTrue(MapUtils.isEmpty(resourceBaseValidator.getResourceTypeToImpl()));
    }

    @Test
    public void testInitPropertiesWithoutImplClass() {
        ResourceBaseValidator resourceBaseValidator = new ResourceBaseValidator();
        initProperties(resourceBaseValidator, new HashMap<>());
        assertTrue(MapUtils.isEmpty(resourceBaseValidator.getResourceTypeToImpl()));
    }

    public Map<String, Object> getValidImplementationConfiguration() {
        Map<String, Object> implConfiguration = new HashMap<>();
        implConfiguration.put(ConfigConstants.Impl_Class, "org.openecomp.sdc.validation.impl.validators.ForbiddenResourceGuideLineValidator");
        implConfiguration.put(ConfigConstants.Enable, true);

        return implConfiguration;
    }

    private void initProperties(ResourceBaseValidator resourceBaseValidator, Map<String, Object> implementationConfiguration) {
        Map<String, Object> properties = Collections.singletonMap(testValidator, implementationConfiguration);
        resourceBaseValidator.init(properties);
    }

    public GlobalValidationContext createGlobalContextFromPath(String path) {
        GlobalValidationContext globalValidationContext = new GlobalValidationContext();
        Map<String, byte[]> contentMap = getContentMapByPath(path);
        if (contentMap == null) {
            return null;
        }
        contentMap.forEach(globalValidationContext::addFileContext);

        return globalValidationContext;
    }

    private Map<String, byte[]> getContentMapByPath(String path) {
        Map<String, byte[]> contentMap = new HashMap<>();
        URL url = ResourceBaseValidator.class.getResource(path);
        File pathFile = new File(url.getFile());
        File[] files;
        if (pathFile.isDirectory()) {
            files = pathFile.listFiles();
        } else {
            files = new File[]{pathFile};
        }

        if (files == null || files.length == 0) {
            return null;
        }

        for (File file : files) {

            try (FileInputStream fis = new FileInputStream(file)) {
                contentMap.put(file.getName(), FileUtils.toByteArray(fis));
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file: " + file, e);
            }

        }
        return contentMap;
    }

    public Map<String, MessageContainer> testValidator(Validator validator, String path) {
        GlobalValidationContext globalValidationContext = createGlobalContextFromPath(path);
        validator.validate(globalValidationContext);

        assert globalValidationContext != null;
        return globalValidationContext.getContextMessageContainers();
    }

    public Map<String, MessageContainer> testValidator(ResourceBaseValidator baseValidator,
                                                       ResourceValidator resourceValidator,
                                                       String resourceTypeToValidate, String path) {

        GlobalValidationContext globalContext = Objects.requireNonNull(
                createGlobalContextFromPath(path), "Global validation context cannot be null");

        ManifestContent manifestContent = ValidationUtil.validateManifest(globalContext);
        Map<String, FileData.Type> fileTypeMap = ManifestUtil.getFileTypeMap(manifestContent);
        Map<String, FileData> fileEnvMap = ManifestUtil.getFileAndItsEnv(manifestContent);

        validateFiles(baseValidator, resourceValidator, globalContext, fileEnvMap, fileTypeMap,
                resourceTypeToValidate);

        return globalContext.getContextMessageContainers();
    }

    private void validateFiles(ResourceBaseValidator baseValidator,
                               ResourceValidator resourceValidator,
                               GlobalValidationContext globalContext,
                               Map<String, FileData> fileEnvMap,
                               Map<String, FileData.Type> fileTypeMap,
                               String resourceTypeToValidate) {

        Collection<String> files = globalContext.getFiles();
        for (String fileName : files) {
            if (FileData.isHeatFile(fileTypeMap.get(fileName))) {
                HeatOrchestrationTemplate heatOrchestrationTemplate =
                        ValidationUtil.checkHeatOrchestrationPreCondition(fileName, globalContext);

                if (Objects.isNull(heatOrchestrationTemplate)) {
                    continue;
                }

                ValidationContext validationContext = baseValidator.createValidationContext(fileName,
                        fileEnvMap.get(fileName) == null ? null : fileEnvMap.get(fileName).getFile(),
                        heatOrchestrationTemplate, globalContext);

                validateResources(fileName, resourceValidator, resourceTypeToValidate, validationContext,
                        globalContext);
            }
        }
    }

    private void validateResources(String fileName, ResourceValidator resourceValidator,
                                   String resourceTypeToValidate, ValidationContext validationContext,
                                   GlobalValidationContext globalValidationContext) {

        HeatOrchestrationTemplate heatOrchestrationTemplate =
                ValidationUtil.checkHeatOrchestrationPreCondition(fileName, globalValidationContext);

        Map<String, Resource> resourcesMap =
                Objects.requireNonNull(heatOrchestrationTemplate, "Orchestration template cannot be null").getResources();

        if (MapUtils.isEmpty(resourcesMap)) {
            return;
        }

        resourcesMap.entrySet()
                .stream()
                .filter(resourceEntry -> isResourceNeedToBeTested(resourceEntry.getValue().getType(), resourceTypeToValidate))
                .forEach(resourceEntry ->
                        resourceValidator.validate
                                (fileName, resourceEntry, globalValidationContext, validationContext));
    }

    private boolean isResourceNeedToBeTested(String currResource, String resourceToTest) {
        if (Objects.isNull(resourceToTest)) {
            return HeatStructureUtil.isNestedResource(currResource);
        }

        return currResource.equals(resourceToTest);
    }

    public void validateErrorMessage(String actualMessage, String expected, String... params) {
        assertEquals(actualMessage.replace("\n", "").replace("\r", ""),
                ErrorMessagesFormatBuilder.getErrorWithParameters(expected, params).replace("\n", "")
                        .replace("\r", ""));
    }

}
