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

/*
 * Copyright © 2018 Nokia
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

package org.onap.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.onap.config.NonConfigResource.CONFIG_LOCATION;
import static org.onap.config.NonConfigResource.NODE_CONFIG_LOCATION;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class NonConfigResourceTest {

    private static final String RESOURCE_NAME = NonConfigResourceTest.class.getSimpleName() + ".class";
    private final URL sampleUrlResource = NonConfigResourceTest.class.getResource(RESOURCE_NAME);
    private final String sampleResourcePath = sampleUrlResource.getPath();
    private final File sampleResourceFile = new File(sampleResourcePath);

    @Test
    public void testShouldLocateResourceWhenAbsPathProvided2() {
        Map<String, String> properties = Collections.emptyMap();
        Path actualResourcePath = new NonConfigResource(properties::get).locate(sampleResourceFile.toString());
        assertEquals(0, actualResourcePath.compareTo(sampleResourceFile.toPath()));
    }

    @Test
    public void testShouldLocateResourceWhenPresentInFiles2() {
        Map<String, String> properties = Collections.emptyMap();
        NonConfigResource testedObject = new NonConfigResource(properties::get);
        testedObject.add(sampleResourceFile);
        Path thisFilePath = testedObject.locate(RESOURCE_NAME);
        assertEquals(0, thisFilePath.compareTo(sampleResourceFile.toPath()));
    }

    @Test
    public void testShouldLocateResourceWhenNodeConfigPropertyIsSet2() {
        final String path = new File(sampleResourcePath).getParentFile().getPath();
        Map<String, String> properties = Collections.singletonMap(NODE_CONFIG_LOCATION, path);

        NonConfigResource testedNonConfigResource =  new NonConfigResource(properties::get);
        Path thisFilePath = testedNonConfigResource.locate(RESOURCE_NAME);

        NonConfigResource systemNonConfigResource = new NonConfigResource(System.getProperties()::getProperty);
        System.setProperty(NODE_CONFIG_LOCATION, path);
        Path systemFilePath = systemNonConfigResource.locate(RESOURCE_NAME);

        Path testFilePath = sampleResourceFile.toPath();
        assertEquals(0, thisFilePath.compareTo(testFilePath));
        assertEquals(0, systemFilePath.compareTo(testFilePath));
    }

    @Test
    public void testShouldLocateResourceWhenConfigPropertyIsSet2() {

        Map<String, String> properties = Collections.singletonMap(
                CONFIG_LOCATION, new File(sampleResourcePath).getParentFile().getPath());

        NonConfigResource testedNonConfigResource = new NonConfigResource(properties::get);
        Path thisFilePath = testedNonConfigResource.locate(RESOURCE_NAME);
        assertEquals(0, thisFilePath.compareTo(new File(sampleResourcePath).toPath()));
    }

    @Test
    public void testShouldLocatePathWhenResourcePresentInUrls2() throws URISyntaxException {
        Map<String, String> properties = Collections.emptyMap();
        NonConfigResource testedObject = new NonConfigResource(properties::get);
        testedObject.add(sampleUrlResource);
        Path thisFilePath = testedObject.locate(RESOURCE_NAME);
        assertEquals(0, thisFilePath.compareTo(Paths.get(sampleUrlResource.toURI())));
    }

    @Test
    public void testShouldNotLocateResource2() {
        String badResource = "noexistingresource";
        String badPath = "noexistingpath";

        Map<String, String> properties = new HashMap<>();
        NonConfigResource testedObject = new NonConfigResource(properties::get);
        // empty properties and bad resource
        Path thisFilePath = testedObject.locate(badResource);

        properties.put(NODE_CONFIG_LOCATION, badPath);
        // bad path in properties and bad resource
        Path thisFilePath2 = testedObject.locate(badResource);

        assertNull(thisFilePath);
        assertNull(thisFilePath2);
    }
}
