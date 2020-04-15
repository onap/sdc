/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (c) 2019 Samsung
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

package org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts;

import fj.data.Either;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.asdctool.impl.validator.config.ValidationConfigManager;
import org.openecomp.sdc.asdctool.impl.validator.utils.Report;
import org.openecomp.sdc.asdctool.impl.validator.utils.ReportFileNioHelper;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.asdctool.impl.validator.utils.ReportFileNioHelper.withTxtFile;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
public class ArtifactValidationUtilsTest {

    @Mock
    private ArtifactCassandraDao artifactCassandraDao;
    @Mock
    private TopologyTemplateOperation topologyTemplateOperation;
    @InjectMocks
    private ArtifactValidationUtils testSubject;

    @Mock
    private GraphVertex vertex;
    @Mock
    private MapArtifactDataDefinition mapToscaDataDefinition;
    @Mock
    private ArtifactDataDefinition artifactDataDefinition;
    @Mock
    private ArtifactDataDefinition artifactDataDefinitionNotInCassandra;
    @Mock
    private ArtifactDataDefinition artifactDataDefinitionDummy;
    @Mock
    private TopologyTemplate topologyTemplate;

    private static final String ES_ID = "testEsInCassandra";
    private static final String ES_ID_NOT_IN_CASS = "testEsNotInCassandra";
    private static final String TASK_NAME = "testTaskName";
    private static final String UNIQUE_ID = "4321";
    private static final String UNIQUE_ID_VERTEX = "321";

    private final static String resourcePath = new File("src/test/resources").getAbsolutePath();
    private final static String txtReportFilePath = ValidationConfigManager.txtReportFilePath(resourcePath);

    @Before
    public void setup() {
        when(artifactCassandraDao.getCountOfArtifactById(ES_ID)).thenReturn(Either.left(1L));
        when(artifactCassandraDao.getCountOfArtifactById(ES_ID_NOT_IN_CASS))
                .thenReturn(Either.right(CassandraOperationStatus.NOT_FOUND));

        when(artifactDataDefinition.getEsId()).thenReturn(ES_ID);
        when(artifactDataDefinitionNotInCassandra.getEsId()).thenReturn(ES_ID_NOT_IN_CASS);

        when(artifactDataDefinitionNotInCassandra.getUniqueId()).thenReturn(UNIQUE_ID);
        when(vertex.getUniqueId()).thenReturn(UNIQUE_ID_VERTEX);
    }

    @Test
    public void testValidateArtifactsAreInCassandra() {
        // given
        Report report = Report.make();
        List<ArtifactDataDefinition> artifacts = new ArrayList<>();
        artifacts.add(artifactDataDefinition);

        withTxtFile(txtReportFilePath, file -> {
            // when
            ArtifactsVertexResult result = testSubject.validateArtifactsAreInCassandra(
                    report, vertex, TASK_NAME, artifacts, file
            );
            List<String> reportOutputFile = ReportFileNioHelper.readFileAsList(txtReportFilePath);

            // then
            assertTrue(result.getStatus());
            assertEquals(0, result.notFoundArtifacts.size());
            assertEquals("Artifact " + ES_ID + " is in Cassandra", reportOutputFile.get(2));
        });
    }

    @Test
    public void testValidateArtifactsNotInCassandra() {
        // given
        List<ArtifactDataDefinition> artifacts = new ArrayList<>();
        artifacts.add(artifactDataDefinition);
        artifacts.add(artifactDataDefinitionNotInCassandra);

        Report report = Report.make();

        // when
        withTxtFile(txtReportFilePath, file -> {
            ArtifactsVertexResult avr = testSubject.validateArtifactsAreInCassandra(
                    report, vertex, TASK_NAME, artifacts, file
            );
            file.reportEndOfToolRun(report);
            List<String> reportOutputFile = ReportFileNioHelper.readFileAsList(txtReportFilePath);
            // then
            assertFalse(avr.getStatus());
            assertEquals(1, avr.notFoundArtifacts.size());
            assertEquals(UNIQUE_ID, avr.notFoundArtifacts.iterator().next());

            assertEquals("Artifact " + ES_ID + " is in Cassandra", reportOutputFile.get(2));
            assertEquals("Artifact " + ES_ID_NOT_IN_CASS + " doesn't exist in Cassandra",
                    reportOutputFile.get(3));
            assertEquals("Task: " + TASK_NAME, reportOutputFile.get(5));
            assertEquals("FailedVertices: [" + UNIQUE_ID_VERTEX + "]", reportOutputFile.get(6));
        });
    }

    @Test
    public void testIsArtifactsInCassandra() {
        // when
        boolean notInCass = testSubject.isArtifactInCassandra(ES_ID_NOT_IN_CASS);
        boolean inCass = testSubject.isArtifactInCassandra(ES_ID);

        // then
        assertFalse(notInCass);
        assertTrue(inCass);
    }

    @Test
    public void testAddRelevantArtifacts() {
        // given
        Map<String, ArtifactDataDefinition> artifactsMap = new HashMap<>();
        artifactsMap.put(ES_ID_NOT_IN_CASS, artifactDataDefinitionNotInCassandra);
        artifactsMap.put(ES_ID, artifactDataDefinition);

        // when
        List<ArtifactDataDefinition> result = testSubject.addRelevantArtifacts(artifactsMap);

        // then
        result.forEach(Assert::assertNotNull);
    }

    @Test
    public void testAddRelevantArtifactsWithNullEsId() {
        // given
        Map<String, ArtifactDataDefinition> artifactsMap = new HashMap<>();
        artifactsMap.put("", artifactDataDefinitionDummy);

        // when
        List<ArtifactDataDefinition> result = testSubject.addRelevantArtifacts(artifactsMap);

        // then
        assertEquals(0, result.size());
    }

    @Test
    public void testValidateTopologyTemplateArtifacts() {
        // given
        Map<String, ArtifactDataDefinition> artifacts = new HashMap<>();
        Report report = Report.make();
        artifacts.put(ES_ID, artifactDataDefinition);

        when(topologyTemplate.getDeploymentArtifacts()).thenReturn(artifacts);
        when(topologyTemplate.getArtifacts()).thenReturn(artifacts);
        when(topologyTemplate.getServiceApiArtifacts()).thenReturn(artifacts);

        when(mapToscaDataDefinition.getMapToscaDataDefinition()).thenReturn(artifacts);
        Map<String, MapArtifactDataDefinition> artifactsMap = new HashMap<>();
        artifactsMap.put(ES_ID, mapToscaDataDefinition);

        when(topologyTemplate.getInstanceArtifacts()).thenReturn(artifactsMap);
        when(topologyTemplate.getInstDeploymentArtifacts()).thenReturn(artifactsMap);

        when(topologyTemplateOperation.getToscaElement(eq(vertex.getUniqueId()), any()))
                .thenReturn(Either.left(topologyTemplate));

        withTxtFile(txtReportFilePath, file -> {
            // when
            ArtifactsVertexResult result = testSubject.validateTopologyTemplateArtifacts(
                    report, vertex, TASK_NAME, file
            );
            List<String> reportOutputFile = ReportFileNioHelper.readFileAsList(txtReportFilePath);

            // then
            assertTrue(result.getStatus());
            assertEquals(0, result.notFoundArtifacts.size());

            IntStream.range(2, reportOutputFile.size()).forEach(
                    i -> assertEquals("Artifact " + ES_ID + " is in Cassandra", reportOutputFile.get(i)));
        });

    }

    @Test
    public void testValidateTopologyTemplateArtifactsNotFoundToscaElement() {
        // given
        Report report = Report.make();
        when(topologyTemplateOperation.getToscaElement(eq(vertex.getUniqueId()), any()))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));

        // when
        ArtifactsVertexResult result = withTxtFile(txtReportFilePath, file -> {
            return testSubject.validateTopologyTemplateArtifacts(report, vertex, TASK_NAME, file);
        });

        // then
        assertFalse(result.getStatus());
    }
}
