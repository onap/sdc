/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.sdc.frontend.ci.tests.execute.sanity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.onap.sdc.backend.ci.tests.config.Config;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ComponentType;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.HttpHeaderEnum;
import org.onap.sdc.backend.ci.tests.utils.Utils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.frontend.ci.tests.datatypes.ComponentData;
import org.onap.sdc.frontend.ci.tests.datatypes.ModelName;
import org.onap.sdc.frontend.ci.tests.datatypes.ResourceCreateData;
import org.onap.sdc.frontend.ci.tests.datatypes.ToscaTypeUploadEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.DriverFactory;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.flow.AddComponentPropertyFlow;
import org.onap.sdc.frontend.ci.tests.flow.AddNodeToCompositionFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVfFlow;
import org.onap.sdc.frontend.ci.tests.flow.DownloadCsarArtifactFlow;
import org.onap.sdc.frontend.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ModelToscaTypeImportTest extends SetupCDTest {

    private static final String NODE_TYPE_TO_ADD = "Network";
    private static final String MODEL_VNFD_TYPES = "vnfd_types";
    private static final String MODEL_NSD_TYPES = "nsd_types";
    private static final String TYPE_URL = "http://%s:%s/sdc2/rest/v1/catalog/uploadType/%s";
    private static final String NODE_TYPE_URL = "http://%s:%s/sdc2/rest/v1/catalog/upload/multipart";
    private static final String IMPORT_PATH = System.getProperty("user.dir").concat("/src/test/resources/Files/imports/tosca");
    private static final String NODE_TYPE_DIRECTORY = IMPORT_PATH.concat(File.separator).concat("node-types");
    private static final String TOSCA_CAPABILITIES_NETWORK_LINK = "tosca.capabilities.network.Linkable";
    private static final String ADDITIONAL_TYPE_DEFINITIONS = "additional_type_definitions";
    private static final String ADDITIONAL_SERVICE_DATA = "tosca.datatypes.nfv.AdditionalServiceData";

    private WebDriver webDriver;
    private HomePage homePage;

    @BeforeMethod
    public void init() {
        webDriver = DriverFactory.getDriver();
        homePage = new HomePage(webDriver);
    }

    @Test
    public void importTypesForModel() throws Exception {
        for (final var type : ToscaTypeUploadEnum.values()) {
            assertTrue(postType(type.getUrlSuffix(), type.getDirectory(), type.getZipParam(), type.isMetadata()),
                String.format("Import of '%s' should return true", type));
        }
    }

    @Test(dependsOnMethods = "importTypesForModel")
    public void addNodeType() throws Exception {
        assertTrue(postNodeType(NODE_TYPE_TO_ADD));
    }

    @Test(dependsOnMethods = "addNodeType")
    public void verifyToscaTypesIncludedInCsar() throws Exception {

        final var vf = new ResourceCreateData();
        vf.setRandomName(ElementFactory.getResourcePrefix() + "-VF");
        vf.setCategory(ResourceCategoryEnum.GENERIC_ABSTRACT.getSubCategory());
        vf.setTagList(Arrays.asList(vf.getName()));
        vf.setDescription("Test");
        vf.setVendorName("EST");
        vf.setVendorRelease("2.5.1");
        vf.setVendorModelNumber("0001");
        vf.setModel(ModelName.ETSI_SOL001_v2_5_1.getName());

        final var createVfFlow = new CreateVfFlow(webDriver, vf);
        createVfFlow.run(homePage);
        ComponentPage resourceCreatePage = createVfFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ResourceCreatePage"));
        resourceCreatePage.isLoaded();

        final var parentComponent = new ComponentData();
        parentComponent.setName(vf.getName());
        parentComponent.setVersion("0.1");
        parentComponent.setComponentType(ComponentType.RESOURCE);
        final var addComponent = new ComponentData();
        addComponent.setName("Network");
        addComponent.setVersion("1.0");
        addComponent.setComponentType(ComponentType.RESOURCE);

        final var compositionPage = resourceCreatePage.goToComposition();
        compositionPage.isLoaded();
        final var addNodeToCompositionFlow = new AddNodeToCompositionFlow(webDriver, parentComponent, addComponent);
        addNodeToCompositionFlow.run(compositionPage);
        compositionPage.isLoaded();
        final var serviceComponentPage = compositionPage.goToServiceGeneral();

        final Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put("AdditionalServiceData", ADDITIONAL_SERVICE_DATA);
        resourceCreatePage = addProperty(serviceComponentPage, propertyMap, vf.getName());

        final var downloadCsarArtifactFlow = new DownloadCsarArtifactFlow(webDriver);
        downloadCsarArtifactFlow.setWaitBeforeGetTheFile(5L);
        downloadCsarArtifactFlow.run(resourceCreatePage);
        final var toscaArtifactsPage = downloadCsarArtifactFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ToscaArtifactsPage"));
        assertTrue(toscaArtifactsPage.getDownloadedArtifactList().size() > 0, "No artifact download was found");
        toscaArtifactsPage.getDownloadedArtifactList().get(0);

        final var downloadFolderPath = getConfig().getDownloadAutomationFolder();
        final Map<String, byte[]> csarFiles = FileHandling.getFilesFromZip(downloadFolderPath, toscaArtifactsPage.getDownloadedArtifactList().get(0));

        assertEquals(8, csarFiles.size());
        assertTrue(csarFiles.keySet().stream().filter(filename -> filename.contains(NODE_TYPE_TO_ADD.concat("-template.yml"))).findAny().isPresent());
        assertTrue(csarFiles.keySet().stream().filter(filename -> filename.contains(MODEL_VNFD_TYPES.concat(".yaml"))).findAny().isPresent());
        assertTrue(csarFiles.keySet().stream().filter(filename -> filename.contains(MODEL_NSD_TYPES.concat(".yaml"))).findAny().isPresent());
        assertTrue(csarFiles.keySet().stream().filter(filename -> filename.contains("-template-interface".concat(".yml"))).findAny().isPresent());
        assertTrue(csarFiles.keySet().stream().filter(filename -> filename.contains(ADDITIONAL_TYPE_DEFINITIONS.concat(".yaml"))).findAny().isPresent());
        assertTrue(csarFiles.values().stream().filter(bytes -> new String(bytes).contains(TOSCA_CAPABILITIES_NETWORK_LINK)).findAny().isPresent());
        assertTrue(csarFiles.values().stream().filter(bytes -> new String(bytes).contains(ADDITIONAL_SERVICE_DATA)).findAny().isPresent());
    }

    private ComponentPage addProperty(ComponentPage serviceComponentPage, final Map<String, String> propertyMap, String name) {
        final AddComponentPropertyFlow addComponentPropertyFlow = new AddComponentPropertyFlow(webDriver, propertyMap);
        serviceComponentPage.isLoaded();
        final ComponentPage resourcePropertiesAssignmentPage = serviceComponentPage.goToPropertiesAssignment();
        addComponentPropertyFlow.run(resourcePropertiesAssignmentPage);
        return resourcePropertiesAssignmentPage.clickOnGeneralMenuItem(ResourceCreatePage.class);
    }

    private boolean postNodeType(final String directory) throws Exception {
        final var yamlDirectory = new File(NODE_TYPE_DIRECTORY.concat(File.separator).concat(directory));
        final var builder = MultipartEntityBuilder.create();
        builder.addTextBody("resourceMetadata", getJsonString(yamlDirectory));
        final var zipFile = getImportZipFile(yamlDirectory);
        builder.addBinaryBody("resourceZip", zipFile, ContentType.APPLICATION_OCTET_STREAM, zipFile.getName());
        try {
            final Config config = Utils.getConfig();
            postEntity(String.format(NODE_TYPE_URL, config.getCatalogBeHost(), config.getCatalogBePort()), "jh0003", builder.build());
        } finally {
            zipFile.delete();
        }
        return true;
    }

    private boolean postType(final String urlSuffix, final String directory, final String zipParam,
                             final boolean metadata) throws Exception {
        final var yamlDirectory = new File(IMPORT_PATH.concat(File.separator).concat(directory));
        final var builder = MultipartEntityBuilder.create();
        if (metadata) {
            builder.addTextBody("toscaTypeMetadata", getJsonString(yamlDirectory));
        }
        builder.addTextBody("model", ModelName.ETSI_SOL001_v2_5_1.getName());
        final var zipFile = getImportZipFile(yamlDirectory);
        builder.addBinaryBody(zipParam, zipFile, ContentType.APPLICATION_OCTET_STREAM, zipFile.getName());

        try {
            final Config config = Utils.getConfig();
            postEntity(String.format(TYPE_URL, config.getCatalogBeHost(), config.getCatalogBePort(), urlSuffix), "cs0008", builder.build());
        } finally {
            zipFile.delete();
        }
        return true;
    }

    private void postEntity(final String url, final String user, final HttpEntity entity) throws Exception {
        final var httpPost = new HttpPost(String.format(url));
        httpPost.setHeader(HttpHeaderEnum.USER_ID.getValue(), user);
        httpPost.setEntity(entity);

        try (final var client = HttpClients.createDefault()) {
            final var response = client.execute(httpPost);
            assertEquals(201, response.getStatusLine().getStatusCode());
        }
    }

    private File getImportZipFile(final File yamlDirectory) throws Exception {
        final var fileToZip = yamlDirectory.listFiles((dir, filename) -> filename.toLowerCase().endsWith(".yml"))[0];
        final var zipName =
            yamlDirectory.getAbsolutePath().concat(File.separator).concat(fileToZip.getName()).concat(UUID.randomUUID().toString()).concat(".zip");
        try (final var zipOut = new ZipOutputStream(new FileOutputStream(zipName));
            final var fis = new FileInputStream(fileToZip)) {
            final var zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);
            final var bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
        return new File(zipName);
    }

    private String getJsonString(final File yamlDirectory) throws Exception {
        return new String(
            Files.readAllBytes(Paths.get(yamlDirectory.listFiles((dir, filename) -> filename.toLowerCase().endsWith(".json"))[0].getAbsolutePath())));
    }
}
