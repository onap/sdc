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

package org.openecomp.sdc.asdctool.impl;

import org.janusgraph.core.JanusGraph;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class GraphMLConverterTest {

    @Test
    public void testImportGraph() {
        String[] args = getInputArgs();
        GraphMLConverter testSubject = new GraphMLConverter();
        assertTrue(testSubject.importGraph(args));
    }

    @Test
    public void testExportGraph() {
        String[] args = getOutputArgs();
        GraphMLConverter testSubject = new GraphMLConverter();
        assertTrue(testSubject.exportGraph(args));
    }

    @Test
    public void testExportGraphMl() {
        String[] args = getOutputArgs();
        GraphMLConverter testSubject = new GraphMLConverter();

        String result = testSubject.exportGraphMl(args);
        assertNotNull(result);
        assertTrue(result.startsWith(args[2]));
    }

    @Test
    public void testFindErrorInJsonGraph() {
        String[] args = getOutputArgs();
        GraphMLConverter testSubject = new GraphMLConverter();
        assertTrue(testSubject.findErrorInJsonGraph(args));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOpenGraphWithBadPath() {

        GraphMLConverter testSubject = new GraphMLConverter();
        testSubject.openGraph("badPath");
    }

    @Test
    public void testExportJsonGraphWithBadOutputDir() {

        GraphMLConverter testSubject = new GraphMLConverter();
        JanusGraph graph = testSubject.openGraph(getJanusGraphConfig());
        assertNull(testSubject.exportJsonGraph(graph, "badOutputDir"));
    }

    @Test
    public void testImportJsonGraph() {
        GraphMLConverter testSubject = new GraphMLConverter();
        JanusGraph graph = testSubject.openGraph(getJanusGraphConfig());

        assertTrue(testSubject.importJsonGraph(graph, getGraphSON(), Collections.emptyList()));
    }

    @Test
    public void testImportJsonGraphNoGraphSONFile() {
        GraphMLConverter testSubject = new GraphMLConverter();
        JanusGraph graph = testSubject.openGraph(getJanusGraphConfig());

        assertFalse(testSubject.importJsonGraph(graph, "noFile", Collections.emptyList()));
    }


    @Test
    public void testExportUsers() {
        GraphMLConverter testSubject = new GraphMLConverter();

        JanusGraph graph = testSubject.openGraph(getJanusGraphConfig());
        String outputDirectory = getOutputJanusGraph();

        String result = testSubject.exportUsers(graph, outputDirectory);
        assertNotNull(result);
        assertTrue(result.startsWith(outputDirectory));
    }

    @Test
    public void testExportUsersWithBadOutputDir() {
        GraphMLConverter testSubject = new GraphMLConverter();

        JanusGraph graph = testSubject.openGraph(getJanusGraphConfig());
        assertNull(testSubject.exportUsers(graph, "badOutputDir"));
    }

    @Test
    public void testExportUsersFromArgs() {
        String[] args = getOutputArgs();
        GraphMLConverter testSubject = new GraphMLConverter();
        assertTrue(testSubject.exportUsers(args));
    }

    private String getJanusGraphConfig() {
        return getClass().getClassLoader().getResource("config/janusgraph.properties").getPath();
    }

    private String getOutputJanusGraph() {
        return new File(getClass().getClassLoader().getResource("graphSON.json").getFile())
                .getAbsolutePath()
                .replace(File.separator + "graphSON.json", "");
    }

    private String getGraphSON() {
        return getClass().getClassLoader().getResource("graphSON.json").getPath();
    }

    private String[] getOutputArgs() {
        return new String[]{"", getJanusGraphConfig(), getOutputJanusGraph()};
    }

    private String[] getInputArgs() {
        return new String[]{"", getJanusGraphConfig(), getGraphSON()};
    }
}
