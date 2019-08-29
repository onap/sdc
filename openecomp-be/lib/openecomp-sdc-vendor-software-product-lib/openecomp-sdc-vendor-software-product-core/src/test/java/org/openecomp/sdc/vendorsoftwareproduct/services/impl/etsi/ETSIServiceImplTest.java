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

package org.openecomp.sdc.vendorsoftwareproduct.services.impl.etsi;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.parser.utils.YamlToObjectConverter;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.tosca.csar.Manifest;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;

public class ETSIServiceImplTest {

    private ETSIService etsiService;
    private String sol004MetaFile = "TOSCA-Meta-Version: 1.0\n" +
        "CSAR-Version: 1.0\n" +
        "Created-By: Kuku\n" +
        "Entry-Definitions: MainServiceTemplate.yaml\n" +
        "ETSI-Entry-Manifest: MainServiceTemplate.mf\n" +
        "ETSI-Entry-Change-Log: MainServiceTemplate.log";
    private String metaFile = "TOSCA-Meta-Version: 1.0\n" +
        "CSAR-Version: 1.0\n" +
        "Created-By: Kuku\n" +
        "Entry-Definitions: MainServiceTemplate.yaml";

    private String finalNonManoLocation = "Deployment/VES_EVENTS/test.xml";

    @Before
    public void setUp() throws IOException {
        YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();
        Configuration configuration = yamlToObjectConverter.convert("src/test/resources",
                Configuration.class, "nonManoConfig.yaml");
        etsiService = Mockito.spy(new ETSIServiceImpl(configuration));
    }

    @After
    public void tearDown() {
        etsiService = null;
    }

    @Test
    public void testIsSol004TrueOrigin() throws IOException {
        FileContentHandler fileContentHandler = new FileContentHandler();
        fileContentHandler
            .addFile("TOSCA-Metadata/TOSCA.meta.original", sol004MetaFile.getBytes(StandardCharsets.UTF_8));
        assertTrue(etsiService.isSol004WithToscaMetaDirectory(fileContentHandler));
    }

    @Test
    public void testIsSol004True() throws IOException {
        FileContentHandler fileContentHandler = new FileContentHandler();
        fileContentHandler.addFile("TOSCA-Metadata/TOSCA.meta", sol004MetaFile.getBytes(StandardCharsets.UTF_8));
        assertTrue(etsiService.isSol004WithToscaMetaDirectory(fileContentHandler));
    }

    @Test
    public void testIsSol004False() throws IOException {
        FileContentHandler fileContentHandler = new FileContentHandler();
        fileContentHandler.addFile("TOSCA-Metadata/TOSCA.meta.original", metaFile.getBytes(StandardCharsets.UTF_8));
        assertFalse(etsiService.isSol004WithToscaMetaDirectory(fileContentHandler));
    }

    @Test
    public void testIsSol004FalseWithNull() throws IOException {
        FileContentHandler fileContentHandler = new FileContentHandler();
        assertFalse(etsiService.isSol004WithToscaMetaDirectory(fileContentHandler));
    }

    @Test
    public void testMoveNonManoFileToArtifactFolder() throws IOException {
        final Map<String, List<String>> nonManoTypeAndSourceMapInManifest = new HashMap<>();
        nonManoTypeAndSourceMapInManifest.put("Some", Collections.singletonList("Some/test.xml"));
        final Manifest manifest = mock(Manifest.class);
        when(manifest.getNonManoSources()).thenReturn(nonManoTypeAndSourceMapInManifest);
        final FileContentHandler fileContentHandler = new FileContentHandler();
        fileContentHandler.addFile("Some/test.xml", new byte[1]);
        fileContentHandler.addFile("TOSCA-Metadata/TOSCA.meta.original", new byte[1]);
        fileContentHandler.addFile("MainServiceTemplate.mf", new byte[1]);
        doReturn(manifest).when(etsiService).getManifest(fileContentHandler);
        doReturn(Paths.get("")).when(etsiService).getOriginalManifestPath(fileContentHandler);
        etsiService.moveNonManoFileToArtifactFolder(fileContentHandler);
        assertThat("Should contain moved file", fileContentHandler.getFileList(), hasItem(finalNonManoLocation));
    }


    @Test
    public void testMoveNonManoFileInArtifactFolderToNonManoOnapPath() throws IOException {
        final Map<String, List<String>> nonManoTypeAndSourceMapInManifest = new HashMap<>();
        nonManoTypeAndSourceMapInManifest.put("Some", Collections.singletonList("Artifacts/Some/test.xml"));
        final FileContentHandler fileContentHandler = new FileContentHandler();
        fileContentHandler.addFile("Some/test.xml", new byte[1]);
        final Manifest manifest = mock(Manifest.class);
        doReturn(manifest).when(etsiService).getManifest(fileContentHandler);
        doReturn(Paths.get("")).when(etsiService).getOriginalManifestPath(fileContentHandler);
        when(manifest.getNonManoSources()).thenReturn(nonManoTypeAndSourceMapInManifest);
        etsiService.moveNonManoFileToArtifactFolder(fileContentHandler);
        assertThat("Should contain moved file", fileContentHandler.getFileList(), hasItem(finalNonManoLocation));
    }

    @Test
    public void testMoveNonManoFileToArtifactFolderFileNotInFolder() throws IOException {
        Map<String, List<String>> nonManoSources = new HashMap<>();
        List<String> sources = new ArrayList<>();
        sources.add("test.xml");
        nonManoSources.put("foo", sources);
        FileContentHandler fileContentHandler = new FileContentHandler();
        fileContentHandler.addFile("test.xml", new byte[1]);
        Manifest manifest = mock(Manifest.class);
        doReturn(manifest).when(etsiService).getManifest(fileContentHandler);
        doReturn(Paths.get("")).when(etsiService).getOriginalManifestPath(fileContentHandler);
        when(manifest.getNonManoSources()).thenReturn(nonManoSources);
        etsiService.moveNonManoFileToArtifactFolder(fileContentHandler);
        String finalOtherNonManoLocation = "Informational/OTHER/test.xml";
        assertFalse(fileContentHandler.containsFile(finalOtherNonManoLocation));
    }

    @Test
    public void testMoveNonManoFileToArtifactFolderNoMove() throws IOException {
        Map<String, List<String>> nonManoSources = new HashMap<>();
        List<String> sources = new ArrayList<>();
        sources.add(finalNonManoLocation);
        nonManoSources.put("Some", sources);
        FileContentHandler fileContentHandler = new FileContentHandler();
        fileContentHandler.addFile(finalNonManoLocation, new byte[1]);
        Manifest manifest = mock(Manifest.class);
        doReturn(manifest).when(etsiService).getManifest(fileContentHandler);
        doReturn(Paths.get("")).when(etsiService).getOriginalManifestPath(fileContentHandler);
        when(manifest.getNonManoSources()).thenReturn(nonManoSources);
        etsiService.moveNonManoFileToArtifactFolder(fileContentHandler);
        assertTrue(fileContentHandler.containsFile(finalNonManoLocation));
    }

    @Test
    public void testMoveNonManoFileToArtifactFolderMoveToKeyFolder() throws IOException {
        Map<String, List<String>> nonManoSources = new HashMap<>();
        List<String> sources = new ArrayList<>();
        sources.add("Artifacts/Deployment/test.xml");
        nonManoSources.put("Some", sources);
        FileContentHandler fileContentHandler = new FileContentHandler();
        fileContentHandler.addFile("Deployment/test.xml", new byte[1]);
        Manifest manifest = mock(Manifest.class);
        doReturn(manifest).when(etsiService).getManifest(fileContentHandler);
        doReturn(Paths.get("")).when(etsiService).getOriginalManifestPath(fileContentHandler);
        when(manifest.getNonManoSources()).thenReturn(nonManoSources);
        etsiService.moveNonManoFileToArtifactFolder(fileContentHandler);
        assertTrue(fileContentHandler.containsFile(finalNonManoLocation));
    }

    @Test
    public void givenManifestNotInRoot_moveNonManoFileToNonManoOnapFolder() throws IOException {
        //given manifest non mano files under key "onap_other", inside and outside Artifacts folder,
        // with relative and absolute paths.
        final Map<String, List<String>> nonManoTypeAndSourceMapInManifest = new HashMap<>();
        nonManoTypeAndSourceMapInManifest.put("onap_other",
            Arrays.asList("../../Artifacts/Artifacts/Deployment/relativePathInsideSubArtifact.xml",
                "../../Files/scriptInFilesPath.sh",
                "/Artifacts/Deployment/absolutePathInsideArtifact.xml"));
        //given ONAP package fileHandler
        final FileContentHandler fileContentHandler = new FileContentHandler();
        fileContentHandler.addFile("Artifacts/Deployment/relativePathInsideSubArtifact.xml", new byte[1]);
        fileContentHandler.addFile("Deployment/absolutePathInsideArtifact.xml", new byte[1]);
        fileContentHandler.addFile("Files/scriptInFilesPath.sh", new byte[1]);
        //given onboarded manifest in two/lvlFolder folder
        final Manifest manifest = mock(Manifest.class);
        doReturn(manifest).when(etsiService).getManifest(fileContentHandler);
        doReturn(Paths.get("two/lvlFolder")).when(etsiService).getOriginalManifestPath(fileContentHandler);
        when(manifest.getNonManoSources()).thenReturn(nonManoTypeAndSourceMapInManifest);
        //when files are non mano moved
        etsiService.moveNonManoFileToArtifactFolder(fileContentHandler);
        assertThat("Should contain moved file", fileContentHandler.getFileList(),
            hasItem("Deployment/OTHER/relativePathInsideSubArtifact.xml"));
        assertThat("Should contain moved file", fileContentHandler.getFileList(),
            hasItem("Deployment/OTHER/absolutePathInsideArtifact.xml"));
        assertThat("Should contain moved file", fileContentHandler.getFileList(),
            hasItem("Deployment/OTHER/scriptInFilesPath.sh"));
    }

    @Test
    public void givenManifestInRoot_moveNonManoFileToNonManoOnapFolder() throws IOException {
        //given manifest non mano files under key "onap_other", inside and outside Artifacts folder
        final Map<String, List<String>> nonManoSourceMap = new HashMap<>();
        nonManoSourceMap.put("onap_other",
            Arrays.asList("Artifacts/Deployment/ANOTHER/authorized_keys",
                "Files/scriptInFilesPath.sh")
        );
        //given manifest non mano file under key "Some"
        nonManoSourceMap.put("Some",
            Collections.singletonList("Files/willMoveToSome.sh")
        );
        //given ONAP package fileHandler
        final FileContentHandler fileContentHandler = new FileContentHandler();
        fileContentHandler.addFile("Deployment/ANOTHER/authorized_keys", new byte[1]);
        fileContentHandler.addFile("Files/scriptInFilesPath.sh", new byte[1]);
        fileContentHandler.addFile("Files/willMoveToSome.sh", new byte[1]);
        fileContentHandler.addFile("Deployment/willNotMove.xml", new byte[1]);
        //given onboarded manifest in root folder
        final Manifest manifest = mock(Manifest.class);
        when(manifest.getNonManoSources()).thenReturn(nonManoSourceMap);
        doReturn(manifest).when(etsiService).getManifest(fileContentHandler);
        doReturn(Paths.get("")).when(etsiService).getOriginalManifestPath(fileContentHandler);
        final Optional<Map<String, Path>> fromToPathMap = etsiService
            .moveNonManoFileToArtifactFolder(fileContentHandler);
        assertThat("Files should be moved", fromToPathMap.isPresent(), is(true));
        assertThat("Should contain moved file", fileContentHandler.getFileList(),
            hasItem("Deployment/OTHER/authorized_keys"));
        assertThat("Should contain moved file", fileContentHandler.getFileList(),
            hasItem("Deployment/OTHER/scriptInFilesPath.sh"));
        assertThat("Should contain moved file", fileContentHandler.getFileList(),
            hasItem("Deployment/VES_EVENTS/willMoveToSome.sh"));
        assertThat("Should contain not moved file", fileContentHandler.getFileList(),
            hasItem("Deployment/willNotMove.xml"));
    }

    @Test
    public void givenMovedFiles_updateDescriptorReferences() {
        //given moved files
        final Map<String, Path> fromToPathMap = new HashMap<>();
        final String file1OriginalPath = "Artifacts/Deployment/ANOTHER/authorized_keys";
        final Path file1Path = Paths.get("Artifacts", "Deployment", "OTHER", "authorized_keys");
        fromToPathMap.put(file1OriginalPath, file1Path);
        final String file2OriginalPath = "Artifacts/Deployment/ANOTHER/image";
        final Path file2Path = Paths.get("Artifacts", "Deployment", "OTHER", "image");
        fromToPathMap.put(file2OriginalPath, file2Path);
        //given main descriptor
        final InputStream mainServiceTemplateYamlFile = getClass().getClassLoader()
            .getResourceAsStream("vnfPackage/vnf1/Definitions/MainServiceTemplate.yaml");
        final ServiceTemplate mainServiceTemplate = new YamlUtil()
            .yamlToObject(mainServiceTemplateYamlFile, ServiceTemplate.class);
        final HashMap<String, ServiceTemplate> serviceTemplateMap = new HashMap<>();
        serviceTemplateMap.put("MainServiceTemplate.yaml", mainServiceTemplate);
        final ToscaServiceModel toscaServiceModel = new ToscaServiceModel(null, serviceTemplateMap,
            "MainServiceTemplate.yaml");
        //when descriptor is updated
        etsiService.updateMainDescriptorPaths(toscaServiceModel, fromToPathMap);
        //then
        final String serviceTemplatesAsYaml = new YamlUtil().objectToYaml(toscaServiceModel.getServiceTemplates());
        assertThat("Descriptor should not contain reference to file", serviceTemplatesAsYaml,
            not(containsString(file1OriginalPath)));
        assertThat("Descriptor should not contain reference to file", serviceTemplatesAsYaml,
            not(containsString(file2OriginalPath)));
        assertThat("Descriptor should contain reference to file", serviceTemplatesAsYaml,
            containsString(file1Path.toString()));
        assertThat("Descriptor should contain reference to file", serviceTemplatesAsYaml,
            containsString(file2Path.toString()));
    }

}
