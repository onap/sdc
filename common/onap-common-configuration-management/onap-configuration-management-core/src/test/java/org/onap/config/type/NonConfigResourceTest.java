package org.onap.config.type;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.onap.config.NonConfigResource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NonConfigResourceTest {

    private final URL sampleUrlResource = NonConfigResourceTest.class.getResource("NonConfigResourceTest.class");
    private final String sampleResourcePath = sampleUrlResource.getPath();
    private final File sampleResourceFile = new File(sampleResourcePath);

    @Test
    public void testShouldLocateResourceWhenAbsPathProvided2() {
        Map<String, String> properties = ImmutableMap.of();
        Path actualResourcePath = NonConfigResource.create(properties::get).locate(sampleResourceFile.toString());

        assertTrue(actualResourcePath.compareTo(sampleResourceFile.toPath()) == 0);
    }

    @Test
    public void testShouldLocateResourceWhenPresentInFiles2() {
        Map<String, String> properties = ImmutableMap.of();
        NonConfigResource testedObject = NonConfigResource.create(properties::get);
        testedObject.add(sampleResourceFile);

        Path thisFilePath = testedObject.locate("NonConfigResourceTest.class");

        assertTrue(thisFilePath.compareTo(sampleResourceFile.toPath()) == 0);
    }

    @Test
    public void testShouldLocateResourceWhenNodeConfigPropertyIsSet2() throws URISyntaxException, MalformedURLException {
        Map<String, String> properties = ImmutableMap.of("node.config.location", new File(sampleResourcePath).getParentFile().getPath());
        NonConfigResource testedNonConfigResource = NonConfigResource.create(properties::get);
        System.getProperties().setProperty("node.config.location", new File(sampleResourcePath).getParentFile().getPath());
        Path thisFilePath = testedNonConfigResource.locate("NonConfigResourceTest.class");

        assertTrue(thisFilePath.compareTo(new File(sampleResourcePath).toPath()) == 0);
    }

    @Test
    public void testShouldLocateResourceWhenConfigPropertyIsSet2() {
        Map<String, String> properties = ImmutableMap.of("config.location", new File(sampleResourcePath).getParentFile().getPath());
        NonConfigResource testedNonConfigResource = NonConfigResource.create(properties::get);
        Path thisFilePath = testedNonConfigResource.locate("NonConfigResourceTest.class");

        assertTrue(thisFilePath.compareTo(new File(sampleResourcePath).toPath()) == 0);
    }

    @Test
    public void testShouldLocatePathWhenResourcePresentInUrls2() throws URISyntaxException {
        Map<String, String> properties = ImmutableMap.of();
        NonConfigResource testedObject = NonConfigResource.create(properties::get);
        testedObject.add(sampleUrlResource);

        Path thisFilePath = testedObject.locate("NonConfigResourceTest.class");

        assertTrue(thisFilePath.compareTo(Paths.get(sampleUrlResource.toURI())) == 0);
    }

    @Test
    public void testShouldNotLocateResource2() throws URISyntaxException {
        Map<String, String> properties = ImmutableMap.of();
        NonConfigResource testedObject = NonConfigResource.create(properties::get);

        Path thisFilePath = testedObject.locate("nonexistingresource");

        assertNull(thisFilePath);
    }
}
