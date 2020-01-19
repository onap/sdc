/*
 * Copyright (c) 2018 Huawei Intellectual Property.
 * Modifications Copyright (c) 2019 Samsung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.asdctool.impl.internal.tool;

import fj.data.Either;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.tosca.ToscaRepresentation;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CsarGeneratorTest {

    @InjectMocks
    private CsarGenerator test;

    @Mock
    private JanusGraphDao janusGraphDao;

    @Mock
    private Component component;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private ArtifactCassandraDao artifactCassandraDao;

    @Mock
    private ToscaExportHandler toscaExportHandler;

    @Mock
    CsarUtils csarUtils;

    @Mock
    private ArtifactDefinition toscaTemplate;

    @Mock
    private ArtifactDefinition toscaCsar;

    @Mock
    ToscaRepresentation toscaRepresentation;

    private static final String ANSWER = "yes";
    private static final String UUID = "123";
    private static final String UNIQUE_ID = "321";
    private static final String PAYLOAD = "testPayload";
    private static final String TEMPLATE_LABEL = "TEMPLATE_LABEL";
    private static final String CSAR_LABEL = "CSAR_LABEL";

    private PrintStream systemOut;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayInputStream inContent = new ByteArrayInputStream(ANSWER.getBytes());

    private final Map<GraphPropertyEnum, Object> service = new EnumMap<>(GraphPropertyEnum.class);
    private final Map<String, ArtifactDefinition> toscaArtifact = new HashMap<>();

    private final Scanner scanner = new Scanner(inContent);
    private final List<GraphVertex> criteria = new ArrayList<>();
    private final GraphVertex graphVertex = new GraphVertex();

    @BeforeClass
    public static void setup() {
        initConfigurationManager();
    }

    @Before
    public void setupSystemOut() {
        systemOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void revertSystemOut() {
        System.setOut(systemOut);
    }

    @Test
    public void testGenerateCsar() {
        // given
        graphVertex.setUniqueId(UNIQUE_ID);
        criteria.add(graphVertex);

        service.put(GraphPropertyEnum.UUID, UUID);
        service.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        service.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
        graphVertex.setMetadataProperties(service);

        when(janusGraphDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, service))
            .thenReturn(Either.left(criteria));
        when(toscaOperationFacade.getToscaFullElement(any(String.class)))
            .thenReturn(Either.left(component));
        when(janusGraphDao.getChildVertex(graphVertex, EdgeLabelEnum.TOSCA_ARTIFACTS,
            JsonParseFlagEnum.ParseJson)).thenReturn(Either.left(graphVertex));

        // when
        test.generateCsar(UUID, scanner);
        String[] consoleOutput = getOutputAsStringArray();

        // then
        checkBasicInformation(consoleOutput);
        assertEquals("", consoleOutput[8]);
    }

    @Test
    public void testGenerateCsarWithBadService() {
        // given
        graphVertex.setUniqueId(UNIQUE_ID);
        criteria.add(graphVertex);

        service.put(GraphPropertyEnum.UUID, UUID);
        service.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        service.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
        graphVertex.setMetadataProperties(service);

        when(janusGraphDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, service))
            .thenReturn(Either.left(criteria));
        when(toscaOperationFacade.getToscaFullElement(any(String.class)))
            .thenReturn(Either.right(StorageOperationStatus.MATCH_NOT_FOUND));

        // when
        test.generateCsar(UUID, scanner);
        String[] consoleOutput = getOutputAsStringArray();

        // then
        checkBasicInformation(consoleOutput);
        assertEquals("Failed to fetch certified service with UUID " + UUID, consoleOutput[8]);
        assertEquals("", consoleOutput[9]);
    }

    @Test
    public void testGenerateCsarWithNoCertifiedUUI() {
        // given
        service.put(GraphPropertyEnum.UUID, UUID);
        service.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        service.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());

        when(janusGraphDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, service))
            .thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));

        // when
        test.generateCsar(UUID, scanner);
        String[] consoleOutput = getOutputAsStringArray();

        // then
        assertEquals("No certified service with UUID " + UUID, consoleOutput[0]);
        assertEquals("", consoleOutput[1]);
    }

    @Test
    public void testFullFlow() {
        // given
        graphVertex.setUniqueId(UNIQUE_ID);
        criteria.add(graphVertex);

        service.put(GraphPropertyEnum.UUID, UUID);
        service.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        service.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
        graphVertex.setMetadataProperties(service);

        when(toscaTemplate.getArtifactLabel()).thenReturn(TEMPLATE_LABEL);
        when(toscaTemplate.getPayloadData()).thenReturn(PAYLOAD.getBytes());
        when(toscaTemplate.getArtifactType()).thenReturn(ArtifactTypeEnum.TOSCA_TEMPLATE.getType());
        toscaArtifact.put("toscaTemplate", toscaTemplate);

        when(toscaCsar.getArtifactLabel()).thenReturn(CSAR_LABEL);
        when(toscaCsar.getPayloadData()).thenReturn(PAYLOAD.getBytes());
        when(toscaCsar.getArtifactType()).thenReturn(ArtifactTypeEnum.TOSCA_CSAR.getType());
        toscaArtifact.put("toscaCsar", toscaCsar);

        when(component.getUniqueId()).thenReturn(UNIQUE_ID);
        when(component.getToscaArtifacts()).thenReturn(toscaArtifact);

        when(janusGraphDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, service))
            .thenReturn(Either.left(criteria));
        when(toscaOperationFacade.getToscaFullElement(any(String.class)))
            .thenReturn(Either.left(component));
        when(janusGraphDao.getChildVertex(graphVertex, EdgeLabelEnum.TOSCA_ARTIFACTS,
            JsonParseFlagEnum.ParseJson)).thenReturn(Either.left(graphVertex));

        when(toscaRepresentation.getMainYaml()).thenReturn(PAYLOAD);
        when(toscaExportHandler.exportComponent(component))
            .thenReturn(Either.left(toscaRepresentation));

        when(csarUtils.createCsar(component, true, true))
            .thenReturn(Either.left(PAYLOAD.getBytes()));

        // when
        test.generateCsar(UUID, scanner);
        String[] consoleOutput = getOutputAsStringArray();

        // then
        checkBasicInformation(consoleOutput);
        assertEquals("create artifact  success " + TEMPLATE_LABEL, consoleOutput[8]);
        assertEquals("create artifact unique id " + UNIQUE_ID + "." + TEMPLATE_LABEL,
            consoleOutput[9]);
        assertEquals("Artifact generated and saved into Cassandra " + TEMPLATE_LABEL,
            consoleOutput[10]);

        assertEquals("create artifact  success " + CSAR_LABEL, consoleOutput[11]);
        assertEquals("create artifact unique id " + UNIQUE_ID + "." + CSAR_LABEL,
            consoleOutput[12]);
        assertEquals("Artifact generated and saved into Cassandra " + CSAR_LABEL,
            consoleOutput[13]);
        assertEquals("", consoleOutput[14]);
    }

    @Test
    public void testFullFlowWithBadPayload() {
        // given
        graphVertex.setUniqueId(UNIQUE_ID);
        criteria.add(graphVertex);

        service.put(GraphPropertyEnum.UUID, UUID);
        service.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        service.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
        graphVertex.setMetadataProperties(service);

        when(toscaTemplate.getArtifactType()).thenReturn(ArtifactTypeEnum.TOSCA_TEMPLATE.getType());
        toscaArtifact.put("toscaTemplate", toscaTemplate);

        when(toscaCsar.getArtifactType()).thenReturn(ArtifactTypeEnum.TOSCA_CSAR.getType());
        toscaArtifact.put("toscaCsar", toscaCsar);

        when(component.getToscaArtifacts()).thenReturn(toscaArtifact);

        when(janusGraphDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, service))
            .thenReturn(Either.left(criteria));
        when(toscaOperationFacade.getToscaFullElement(any(String.class)))
            .thenReturn(Either.left(component));
        when(janusGraphDao.getChildVertex(graphVertex, EdgeLabelEnum.TOSCA_ARTIFACTS,
            JsonParseFlagEnum.ParseJson)).thenReturn(Either.left(graphVertex));

        when(toscaExportHandler.exportComponent(component)).thenReturn(Either.right(null));
        when(csarUtils.createCsar(component, true, true)).thenReturn(Either.right(null));

        // when
        test.generateCsar(UUID, scanner);
        String[] consoleOutput = getOutputAsStringArray();

        // then
        checkBasicInformation(consoleOutput);
        assertEquals("create artifact failed ", consoleOutput[8]);
        assertEquals("create artifact failed ", consoleOutput[9]);
        assertEquals("", consoleOutput[10]);
    }

    private void checkBasicInformation(String[] consoleOutput) {
        assertEquals("component from type\t" + ComponentTypeEnum.SERVICE.name(), consoleOutput[0]);
        assertEquals("component name", consoleOutput[1]);
        assertEquals("component version", consoleOutput[2]);
        assertEquals("component state\t" + LifecycleStateEnum.CERTIFIED.name(), consoleOutput[3]);
        assertEquals("component is highest", consoleOutput[4]);
        assertEquals("component is archived", consoleOutput[5]);
        assertEquals("", consoleOutput[6]);
        assertEquals("Generate CSAR (yes/no)?", consoleOutput[7]);
    }

    private String[] getOutputAsStringArray() {
        return outContent.toString()
                .replace("\t\t\n", "\n")
                .replace("\t\n", "\n")
                .replace("\t\t", "\t")
                .replace("\t\r", "")
                .replace("\r", "")
                .split("\n", -1);
    }

    private static void initConfigurationManager() {
        String confPath = new File(Objects
            .requireNonNull(
                CsarGenerator.class.getClassLoader().getResource("config/configuration.yaml"))
            .getFile()).getParent();
        ConfigurationSource confSource =
            new FSConfigurationSource(ExternalConfiguration.getChangeListener(), confPath);
        new ConfigurationManager(confSource);
    }
}
