package org.onap.config.type;

import io.vavr.collection.HashMap;
import org.junit.Test;
import org.onap.config.NonConfigResource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NonConfigResourceTest {

    private final URL sampleUrlResource = NonConfigResourceTest.class.getResource("NonConfigResourceTest.class");
    private final String sampleResourcePath = sampleUrlResource.getPath();
    private final File sampleResourceFile = new File(sampleResourcePath);

    @Test
    public void testShouldLocateResourceWhenAbsPathProvided() {
        Path actualResourcePath = NonConfigResource.create().locate(sampleResourceFile.toString());

        assertTrue(actualResourcePath.compareTo(sampleResourceFile.toPath()) == 0);
    }

    @Test
    public void testShouldLocateResourceWhenPresentInFiles() {
        NonConfigResource testedObject = NonConfigResource.create();
        testedObject.add(sampleResourceFile);

        Path thisFilePath = testedObject.locate("NonConfigResourceTest.class");

        assertTrue(thisFilePath.compareTo(sampleResourceFile.toPath()) == 0);
    }


    @Test
    public void testShouldLocateResourceWhenNodeConfigPropertyIsSet() throws URISyntaxException, MalformedURLException {
        HashMap<String, String> systemProperties = HashMap.of("node.config.location", new File(sampleResourcePath).getParentFile().getPath());
        NonConfigResource testedNonConfigResource = NonConfigResource.create(systemProperties);
        Path thisFilePath = testedNonConfigResource.locate("NonConfigResourceTest.class");

        assertTrue(thisFilePath.compareTo(new File(sampleResourcePath).toPath()) == 0);
    }

    @Test
    public void testShouldLocateResourceWhenConfigPropertyIsSet() {
        HashMap<String, String> systemProperties = HashMap.of("config.location", new File(sampleResourcePath).getParentFile().getPath());
        NonConfigResource testedNonConfigResource = NonConfigResource.create(systemProperties);

        Path thisFilePath = testedNonConfigResource.locate("NonConfigResourceTest.class");

        assertTrue(thisFilePath.compareTo(new File(sampleResourcePath).toPath()) == 0);
    }

    @Test
    public void testShouldLocatePathWhenResourcePresentInUrls() throws URISyntaxException {
        NonConfigResource testedObject = NonConfigResource.create();
        testedObject.add(sampleUrlResource);

        Path thisFilePath = testedObject.locate("NonConfigResourceTest.class");

        assertTrue(thisFilePath.compareTo(Paths.get(sampleUrlResource.toURI())) == 0);
    }
    @Test
    public void testShouldNotLocateResource() throws URISyntaxException {
        NonConfigResource testedObject = NonConfigResource.create();

        Path thisFilePath = testedObject.locate("nonexistingresource");

        assertNull(thisFilePath);
    }
}
