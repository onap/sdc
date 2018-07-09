package org.onap.config.type;

import org.junit.Test;
import org.onap.config.NonConfigResource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class NonConfigResourceTest {

    private final URL sampleUrlResource = NonConfigResourceTest.class.getResource("NonConfigResourceTest.class");
    private final String sampleResourcePath = sampleUrlResource.getPath();
    private final File sampleResourceFile = new File(sampleResourcePath);

    @Test
    public void testShouldLocateResourceWhenAbsPathProvided() {
        Path actualResourcePath = NonConfigResource.locate(sampleResourceFile.toString());

        assertTrue(actualResourcePath.compareTo(sampleResourceFile.toPath()) == 0);
    }

    @Test
    public void testShouldLocateResourceWhenPresentInFiles() {
        NonConfigResource.add(sampleResourceFile);

        Path thisFilePath = NonConfigResource.locate("NonConfigResourceTest.class");

        assertTrue(thisFilePath.compareTo(sampleResourceFile.toPath()) == 0);
    }


    @Test
    public void testShouldLocateResourceWhenNodeConfigPropertyIsSet() throws URISyntaxException, MalformedURLException {
        Properties systemProperties = System.getProperties();
        systemProperties.setProperty("node.config.location", new File(sampleResourcePath).getParentFile().getPath());

        Path thisFilePath = NonConfigResource.locate("NonConfigResourceTest.class");

        assertTrue(thisFilePath.compareTo(new File(sampleResourcePath).toPath()) == 0);

        systemProperties.remove("node.config.location");
    }

    @Test
    public void testShouldLocateResourceWhenConfigPropertyIsSet() {
        Properties systemProperties = System.getProperties();
        String configLocationPropertyVal = systemProperties.getProperty("config.location");
        systemProperties.setProperty("config.location", new File(sampleResourcePath).getParentFile().getPath());

        Path thisFilePath = NonConfigResource.locate("NonConfigResourceTest.class");

        assertTrue(thisFilePath.compareTo(new File(sampleResourcePath).toPath()) == 0);

        systemProperties.setProperty("config.location", configLocationPropertyVal);
    }

    @Test
    public void testShouldLocatePathWhenResourcePresentInUrls() throws URISyntaxException {
        NonConfigResource.add(sampleUrlResource);

        Path thisFilePath = NonConfigResource.locate("NonConfigResourceTest.class");

        assertTrue(thisFilePath.compareTo(Paths.get(sampleUrlResource.toURI())) == 0);
    }
}
