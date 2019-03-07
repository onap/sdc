package org.openecomp.sdc.vendorsoftwareproduct.services.impl.etsi;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.sdc.tosca.parser.utils.YamlToObjectConverter;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.tosca.csar.Manifest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ETSIServiceImplTest {
    private ETSIService etsiService;
    private String sol004MetaFile = "TOSCA-Meta-Version: 1.0\n" +
            "CSAR-Version: 1.0\n" +
            "Created-By: Kuku\n" +
            "Entry-Definitions: MainServiceTemplate.yaml\n" +
            "Entry-Manifest: MainServiceTemplate.mf\n" +
            "Entry-Change-Log: MainServiceTemplate.log";
    private String metaFile = "TOSCA-Meta-Version: 1.0\n" +
            "CSAR-Version: 1.0\n" +
            "Created-By: Kuku\n" +
            "Entry-Definitions: MainServiceTemplate.yaml";

    private String finalNonManoLocation = "Deployment/VES_EVENTS/test.xml";
    private String finalOtherNonManoLocation = "Informational/OTHER/test.xml";

    @Before
    public void setUp() throws IOException {
        YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();
        Configuration configuration = yamlToObjectConverter.convert("src/test/resources",
                Configuration.class, "nonManoConfig.yaml");
        etsiService = new ETSIServiceImpl(configuration);
    }

    @After
    public void tearDown() {
        etsiService = null;
    }

    @Test
    public void testIsSol004TrueOrigin() throws IOException {
        FileContentHandler fileContentHandler = new FileContentHandler();
        fileContentHandler.addFile("TOSCA-Metadata/TOSCA.meta.original", sol004MetaFile.getBytes(StandardCharsets.UTF_8));
        assertTrue(etsiService.isSol004WithToscaMetaDirectory(fileContentHandler));
    }

    @Test
    public void testIsSol004True() throws IOException  {
        FileContentHandler fileContentHandler = new FileContentHandler();
        fileContentHandler.addFile("TOSCA-Metadata/TOSCA.meta", sol004MetaFile.getBytes(StandardCharsets.UTF_8));
        assertTrue(etsiService.isSol004WithToscaMetaDirectory(fileContentHandler));
    }

    @Test
    public void testIsSol004False() throws IOException  {
        FileContentHandler fileContentHandler = new FileContentHandler();
        fileContentHandler.addFile("TOSCA-Metadata/TOSCA.meta.original", metaFile.getBytes(StandardCharsets.UTF_8));
        assertFalse(etsiService.isSol004WithToscaMetaDirectory(fileContentHandler));
    }

    @Test
    public void testIsSol004FalseWithNull() throws IOException  {
        FileContentHandler fileContentHandler = new FileContentHandler();
        assertFalse(etsiService.isSol004WithToscaMetaDirectory(fileContentHandler));
    }

    @Test
    public void testMoveNonManoFileToArtifactFolder() throws IOException {
        Map<String, List<String>> nonManoSources = new HashMap<>();
        List<String> sources = new ArrayList<>();
        sources.add("Some/test.xml");
        nonManoSources.put("Some", sources);
        FileContentHandler fileContentHandler = new FileContentHandler();
        fileContentHandler.addFile("Some/test.xml", new byte[1]);
        Manifest manifest = mock(Manifest.class);
        when(manifest.getNonManoSources()).thenReturn(nonManoSources);
        etsiService.moveNonManoFileToArtifactFolder(fileContentHandler, manifest);
        assertTrue(fileContentHandler.containsFile(finalNonManoLocation));
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
        when(manifest.getNonManoSources()).thenReturn(nonManoSources);
        etsiService.moveNonManoFileToArtifactFolder(fileContentHandler, manifest);
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
        when(manifest.getNonManoSources()).thenReturn(nonManoSources);
        etsiService.moveNonManoFileToArtifactFolder(fileContentHandler, manifest);
        assertTrue(fileContentHandler.containsFile(finalNonManoLocation));
    }

    @Test
    public void testMoveNonManoFileToArtifactFolderMoveToKeyFolder() throws IOException {
        Map<String, List<String>> nonManoSources = new HashMap<>();
        List<String> sources = new ArrayList<>();
        sources.add("Artifacts/Deployment/test.xml");
        nonManoSources.put("Some", sources);
        FileContentHandler fileContentHandler = new FileContentHandler();
        fileContentHandler.addFile("Artifacts/Deployment/test.xml", new byte[1]);
        Manifest manifest = mock(Manifest.class);
        when(manifest.getNonManoSources()).thenReturn(nonManoSources);
        etsiService.moveNonManoFileToArtifactFolder(fileContentHandler, manifest);
        assertTrue(fileContentHandler.containsFile(finalNonManoLocation));
    }
}
