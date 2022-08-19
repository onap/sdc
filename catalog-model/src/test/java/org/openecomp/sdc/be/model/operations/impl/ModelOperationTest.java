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
package org.openecomp.sdc.be.model.operations.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.model.operations.impl.ModelOperation.ADDITIONAL_TYPE_DEFINITIONS_PATH;

import fj.data.Either;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ToscaModelImportCassandraDao;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.data.model.ToscaImportByModel;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.ModelTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.ModelOperationExceptionSupplier;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.OperationException;
import org.openecomp.sdc.be.model.normatives.ElementTypeEnum;
import org.openecomp.sdc.be.model.operations.api.DerivedFromOperation;
import org.openecomp.sdc.be.resources.data.ModelData;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:application-context-test.xml")
class ModelOperationTest extends ModelTestBase {

    private static final String modelName = "ETSI-SDC-MODEL-TEST";
    @InjectMocks
    private ModelOperation modelOperation;
    @Mock
    private JanusGraphGenericDao janusGraphGenericDao;
    @Mock
    private JanusGraphDao janusGraphDao;
    @Mock
    private ToscaModelImportCassandraDao toscaModelImportCassandraDao;
    @Mock
    private DerivedFromOperation derivedFromOperation;

    @BeforeAll
    static void beforeAllInit() {
        init();
    }

    @BeforeEach
    void beforeEachInit() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createModelSuccessTest() {
        final ModelData modelData = new ModelData(modelName, UniqueIdBuilder.buildModelUid(modelName), ModelTypeEnum.NORMATIVE);
        when(janusGraphGenericDao.createNode(any(), any())).thenReturn(Either.left(modelData));
        final Model createdModel = modelOperation.createModel(new Model(modelName, ModelTypeEnum.NORMATIVE), false);
        assertThat(createdModel).isNotNull();
        assertThat(createdModel.getName()).isEqualTo(modelName);
    }

    @Test
    void createDerivedModelSuccessTest() {
        final String derivedModelName = "derivedModel";
        final ModelData modelData = new ModelData(derivedModelName, UniqueIdBuilder.buildModelUid(derivedModelName), ModelTypeEnum.NORMATIVE);
        when(janusGraphGenericDao.createNode(any(), any())).thenReturn(Either.left(modelData));

        final GraphVertex modelVertex = new GraphVertex();
        modelVertex.addMetadataProperty(GraphPropertyEnum.NAME, "baseModel");
        modelVertex.addMetadataProperty(GraphPropertyEnum.MODEL_TYPE, ModelTypeEnum.NORMATIVE.getValue());
        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.MODEL), anyMap())).thenReturn(Either.left(Collections.singletonList(modelVertex)));
        when(janusGraphGenericDao.getChild(eq("uid"), anyString(), eq(GraphEdgeLabels.DERIVED_FROM), eq(NodeTypeEnum.Model),
            eq(ModelData.class))).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        when(derivedFromOperation.addDerivedFromRelation("model.derivedModel", "model.baseModel", NodeTypeEnum.Model)).thenReturn(
            Either.left(new GraphRelation()));

        final Model createdModel = modelOperation.createModel(new Model(derivedModelName, modelName, ModelTypeEnum.NORMATIVE), false);
        assertThat(createdModel).isNotNull();
        assertThat(createdModel.getName()).isEqualTo(derivedModelName);
    }

    @Test
    void createModelFailWithModelAlreadyExistTest() {
        when(janusGraphGenericDao.createNode(any(), any())).thenReturn(Either.right(JanusGraphOperationStatus.JANUSGRAPH_SCHEMA_VIOLATION));
        final var model = new Model(modelName, ModelTypeEnum.NORMATIVE);
        assertThrows(OperationException.class, () -> modelOperation.createModel(model, false));
    }

    @Test
    void createModelFailTest() {
        when(janusGraphGenericDao.createNode(any(), any())).thenReturn(Either.right(JanusGraphOperationStatus.GRAPH_IS_NOT_AVAILABLE));
        final var model = new Model(modelName, ModelTypeEnum.NORMATIVE);
        assertThrows(OperationException.class, () -> modelOperation.createModel(model, false));
    }

    @Test
    void createModelImportsSuccessTest() {
        var modelId = "modelId";
        var contentEntry1 = "contentEntry1";
        var pathEntry1 = "entry1";
        var contentEntry2 = "contentEntry2";
        var pathEntry2 = "entry2/path";
        final Map<String, byte[]> zipContent = new TreeMap<>();
        zipContent.put(pathEntry1, contentEntry1.getBytes(StandardCharsets.UTF_8));
        zipContent.put(pathEntry2, contentEntry2.getBytes(StandardCharsets.UTF_8));

        modelOperation.createModelImports(modelId, zipContent);

        final var toscaImport1 = new ToscaImportByModel();
        toscaImport1.setModelId(modelId);
        toscaImport1.setContent(contentEntry1);
        toscaImport1.setFullPath(pathEntry1);
        final var toscaImport2 = new ToscaImportByModel();
        toscaImport2.setModelId(modelId);
        toscaImport2.setContent(contentEntry2);
        toscaImport2.setFullPath(pathEntry2);
        final List<ToscaImportByModel> toscaImportByModelList = List.of(toscaImport1, toscaImport2);

        verify(toscaModelImportCassandraDao).replaceImports(modelId, toscaImportByModelList);
    }

    @Test
    void createModelImportsTest_emptyZipContent() {
        var modelId = "modelId";
        modelOperation.createModelImports(modelId, Collections.emptyMap());
        verify(toscaModelImportCassandraDao, never()).replaceImports(eq(modelId), anyList());
        modelOperation.createModelImports(modelId, null);
        verify(toscaModelImportCassandraDao, never()).replaceImports(eq(null), anyList());
    }

    @Test
    void findModelVertexSuccessTest() {
        final ArgumentCaptor<Map<GraphPropertyEnum, Object>> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        final GraphVertex expectedVertex = new GraphVertex();
        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.MODEL), mapArgumentCaptor.capture())).thenReturn(Either.left(List.of(expectedVertex)));
        var modelName = "modelName";
        final Optional<GraphVertex> modelVertexByNameOpt = modelOperation.findModelVertexByName(modelName);
        assertTrue(modelVertexByNameOpt.isPresent());
        assertEquals(expectedVertex, modelVertexByNameOpt.get());
        final Map<GraphPropertyEnum, Object> value = mapArgumentCaptor.getValue();
        assertEquals(modelName, value.get(GraphPropertyEnum.NAME));
        assertEquals(UniqueIdBuilder.buildModelUid(modelName), value.get(GraphPropertyEnum.UNIQUE_ID));
    }

    @Test
    void findModelVertexTest_modelNotFound() {
        final ArgumentCaptor<Map<GraphPropertyEnum, Object>> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.MODEL), mapArgumentCaptor.capture()))
            .thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        var modelName = "modelName";

        final Optional<GraphVertex> modelVertexByNameOpt = modelOperation.findModelVertexByName(modelName);

        assertTrue(modelVertexByNameOpt.isEmpty());
        final Map<GraphPropertyEnum, Object> value = mapArgumentCaptor.getValue();
        assertEquals(modelName, value.get(GraphPropertyEnum.NAME));
        assertEquals(UniqueIdBuilder.buildModelUid(modelName), value.get(GraphPropertyEnum.UNIQUE_ID));
    }

    @Test
    void findModelVertexTest_janusGraphError() {
        final ArgumentCaptor<Map<GraphPropertyEnum, Object>> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.MODEL), mapArgumentCaptor.capture()))
            .thenReturn(Either.right(JanusGraphOperationStatus.GENERAL_ERROR));
        var modelName = "modelName";

        final var actualException = assertThrows(OperationException.class, () -> modelOperation.findModelVertexByName(modelName));

        assertEquals(ActionStatus.GENERAL_ERROR, actualException.getActionStatus());
        final Map<GraphPropertyEnum, Object> value = mapArgumentCaptor.getValue();
        assertEquals(modelName, value.get(GraphPropertyEnum.NAME));
        assertEquals(UniqueIdBuilder.buildModelUid(modelName), value.get(GraphPropertyEnum.UNIQUE_ID));
    }

    @Test
    void findModelVertexTest_emptyOrNullModelName() {
        assertTrue(modelOperation.findModelVertexByName("").isEmpty());
        assertTrue(modelOperation.findModelVertexByName(null).isEmpty());
    }

    @Test
    void findModelByNameSuccessTest() {
        final ArgumentCaptor<Map<GraphPropertyEnum, Object>> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        var modelName = "modelName";
        final GraphVertex expectedVertex = mock(GraphVertex.class);
        when(expectedVertex.getMetadataProperty(GraphPropertyEnum.NAME)).thenReturn(modelName);
        when(expectedVertex.getMetadataProperty(GraphPropertyEnum.MODEL_TYPE)).thenReturn(ModelTypeEnum.NORMATIVE.getValue());
        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.MODEL), mapArgumentCaptor.capture())).thenReturn(Either.left(List.of(expectedVertex)));
        when(janusGraphGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Model), UniqueIdBuilder.buildModelUid(modelName),
            GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Model, ModelData.class)).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        final Optional<Model> modelByNameOpt = modelOperation.findModelByName(modelName);

        final Map<GraphPropertyEnum, Object> value = mapArgumentCaptor.getValue();
        assertEquals(modelName, value.get(GraphPropertyEnum.NAME));
        assertEquals(UniqueIdBuilder.buildModelUid(modelName), value.get(GraphPropertyEnum.UNIQUE_ID));

        final Model expectedModel = new Model(modelName, ModelTypeEnum.NORMATIVE);
        assertTrue(modelByNameOpt.isPresent());
        assertEquals(expectedModel, modelByNameOpt.get());
    }

    @Test
    void findModelByNameTest_modelNameNotFound() {
        final ArgumentCaptor<Map<GraphPropertyEnum, Object>> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        var modelName = "modelName";
        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.MODEL), mapArgumentCaptor.capture()))
            .thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        final Optional<Model> modelByNameOpt = modelOperation.findModelByName(modelName);
        assertTrue(modelByNameOpt.isEmpty());
    }

    @Test
    void findModelByNameTest_emptyOrNullModelName() {
        assertTrue(modelOperation.findModelByName("").isEmpty());
        assertTrue(modelOperation.findModelByName(null).isEmpty());
    }

    @Test
    void findAllModelsSuccessTest() {
        final GraphVertex expectedVertex = mock(GraphVertex.class);
        when(expectedVertex.getMetadataProperty(GraphPropertyEnum.NAME)).thenReturn(modelName);
        when(expectedVertex.getMetadataProperty(GraphPropertyEnum.MODEL_TYPE)).thenReturn(ModelTypeEnum.NORMATIVE.getValue());
        when(janusGraphDao.getByCriteria(VertexTypeEnum.MODEL, Collections.emptyMap())).thenReturn(Either.left(List.of(expectedVertex)));
        when(janusGraphGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Model), UniqueIdBuilder.buildModelUid(modelName),
            GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Model, ModelData.class)).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));

        final List<Model> actualModelList = modelOperation.findAllModels();
        assertFalse(actualModelList.isEmpty());
        assertEquals(1, actualModelList.size());
        assertEquals(modelName, actualModelList.get(0).getName());
    }

    @Test
    void findAllModelsTest_noModelsFound() {
        when(janusGraphDao.getByCriteria(VertexTypeEnum.MODEL, Collections.emptyMap())).thenReturn(Either.left(Collections.emptyList()));
        final List<Model> actualModelList = modelOperation.findAllModels();
        assertTrue(actualModelList.isEmpty());
    }

    @Test
    void findAllModelsTest_janusGraphNotFound() {
        when(janusGraphDao.getByCriteria(VertexTypeEnum.MODEL, Collections.emptyMap()))
            .thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        final List<Model> actualModelList = modelOperation.findAllModels();
        assertTrue(actualModelList.isEmpty());
    }

    @Test
    void findAllModelsTest_janusGraphError() {
        when(janusGraphDao.getByCriteria(VertexTypeEnum.MODEL, Collections.emptyMap()))
            .thenReturn(Either.right(JanusGraphOperationStatus.GENERAL_ERROR));
        final var actualException = assertThrows(OperationException.class, () -> modelOperation.findAllModels());
        final var expectedException = ModelOperationExceptionSupplier.failedToRetrieveModels(JanusGraphOperationStatus.GENERAL_ERROR).get();
        assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    void findAllModelImportsTest() {
        //given
        final var modelName = "modelName";
        final var parentModelName = "parentModelName";
        final GraphVertex expectedVertex = mock(GraphVertex.class);
        when(expectedVertex.getMetadataProperty(GraphPropertyEnum.NAME)).thenReturn(modelName);
        when(expectedVertex.getMetadataProperty(GraphPropertyEnum.MODEL_TYPE)).thenReturn(ModelTypeEnum.NORMATIVE_EXTENSION.getValue());
        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.MODEL), anyMap())).thenReturn(Either.left(List.of(expectedVertex)));

        final var modelData = new ModelData(parentModelName, parentModelName, ModelTypeEnum.NORMATIVE);
        final ImmutablePair<ModelData, GraphEdge> modelDataGraphEdgePair = new ImmutablePair<>(modelData, null);

        when(janusGraphGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Model), UniqueIdBuilder.buildModelUid(modelName),
            GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Model, ModelData.class)).thenReturn(Either.left(modelDataGraphEdgePair));

        final ArrayList<ToscaImportByModel> childModelImportList = new ArrayList<>();
        childModelImportList.add(createModelImport(modelName, "anyPath1"));
        childModelImportList.add(createModelImport(modelName, "anyPath2"));
        when(toscaModelImportCassandraDao.findAllByModel(modelName)).thenReturn(new ArrayList<>(childModelImportList));
        final ArrayList<ToscaImportByModel> parentModelImportList = new ArrayList<>();
        parentModelImportList.add(createModelImport(parentModelName, "anyPath1"));
        parentModelImportList.add(createModelImport(parentModelName, "anyPath2"));
        when(toscaModelImportCassandraDao.findAllByModel(parentModelName)).thenReturn(parentModelImportList);

        //when
        final List<ToscaImportByModel> actualModelImportList = modelOperation.findAllModelImports(modelName, true);

        //then
        assertFalse(actualModelImportList.isEmpty());
        assertEquals(childModelImportList.size() + parentModelImportList.size(), actualModelImportList.size());
        Stream.concat(childModelImportList.stream(), parentModelImportList.stream())
            .forEach(toscaImportByModel -> assertTrue(actualModelImportList.contains(toscaImportByModel)));
    }

    @Test
    void addTypesToDefaultImportsTest_nonExistingAdditionalTypesImport() throws IOException {
        var modelName = "model";
        final Path testResourcePath = Path.of("src/test/resources/modelOperation");

        final var dataTypesPath = testResourcePath.resolve(Path.of("input-data_types.yaml"));
        final var dataTypes = Files.readString(dataTypesPath);

        final Path import1RelativePath = Path.of("original-import-1.yaml");
        final Path import1Path = testResourcePath.resolve(import1RelativePath);
        final Path import2RelativePath = Path.of("original-import-2.yaml");
        final Path import2Path = testResourcePath.resolve(import2RelativePath);

        var toscaImportByModel1 = new ToscaImportByModel();
        toscaImportByModel1.setModelId(modelName);
        toscaImportByModel1.setFullPath(import1RelativePath.toString());
        toscaImportByModel1.setContent(Files.readString(import1Path));

        var toscaImportByModel2 = new ToscaImportByModel();
        toscaImportByModel2.setModelId(modelName);
        toscaImportByModel2.setFullPath(import2RelativePath.toString());
        toscaImportByModel2.setContent(Files.readString(import2Path));

        final List<ToscaImportByModel> modelImports = new ArrayList<>();
        modelImports.add(toscaImportByModel1);
        modelImports.add(toscaImportByModel2);
        when(toscaModelImportCassandraDao.findAllByModel(modelName)).thenReturn(modelImports);

        modelOperation.addTypesToDefaultImports(ElementTypeEnum.DATA_TYPE, dataTypes, modelName);
        ArgumentCaptor<List<ToscaImportByModel>> importListArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(toscaModelImportCassandraDao).saveAll(eq(modelName), importListArgumentCaptor.capture());

        final List<ToscaImportByModel> actualImportList = importListArgumentCaptor.getValue();
        assertEquals(3, actualImportList.size());
        assertTrue(actualImportList.contains(toscaImportByModel1));
        assertTrue(actualImportList.contains(toscaImportByModel2));

        var expectedAdditionalTypesImport = new ToscaImportByModel();
        expectedAdditionalTypesImport.setModelId(modelName);
        expectedAdditionalTypesImport.setFullPath(ADDITIONAL_TYPE_DEFINITIONS_PATH.toString());
        expectedAdditionalTypesImport.setContent(Files.readString(testResourcePath.resolve(Path.of("expected-additional_types-1.yaml"))));
        final ToscaImportByModel actualAdditionalTypesImport =
            actualImportList.stream().filter(expectedAdditionalTypesImport::equals).findFirst().orElse(null);
        assertNotNull(actualAdditionalTypesImport);
        assertEquals(expectedAdditionalTypesImport.getContent(), actualAdditionalTypesImport.getContent());

        var expectedImport1 = new ToscaImportByModel();
        expectedImport1.setModelId(modelName);
        expectedImport1.setFullPath(import1RelativePath.toString());
        expectedImport1.setContent(Files.readString(testResourcePath.resolve(Path.of("expected-import-1.yaml"))));
        final ToscaImportByModel actualImport1 = actualImportList.stream().filter(expectedImport1::equals).findFirst().orElse(null);
        assertNotNull(actualImport1);
        assertEquals(expectedImport1.getContent(), actualImport1.getContent());

        var expectedImport2 = new ToscaImportByModel();
        expectedImport2.setModelId(modelName);
        expectedImport2.setFullPath(import2RelativePath.toString());
        expectedImport2.setContent(Files.readString(testResourcePath.resolve(Path.of("expected-import-2.yaml"))));
        final ToscaImportByModel actualImport2 = actualImportList.stream().filter(expectedImport2::equals).findFirst().orElse(null);
        assertNotNull(actualImport2);
        assertEquals(expectedImport2.getContent(), actualImport2.getContent());
    }

    @Test
    void addArtifactsToDefaultImportsTest_nonExistingAdditionalTypesImport() throws IOException {
        var modelName = "model";
        final Path testResourcePath = Path.of("src/test/resources/modelOperation");

        final var dataTypesPath = testResourcePath.resolve(Path.of("input-artifact_types.yaml"));
        final var dataTypes = Files.readString(dataTypesPath);

        final Path import1RelativePath = Path.of("original-import-3.yaml");
        final Path import1Path = testResourcePath.resolve(import1RelativePath);

        var toscaImportByModel1 = new ToscaImportByModel();
        toscaImportByModel1.setModelId(modelName);
        toscaImportByModel1.setFullPath(import1RelativePath.toString());
        toscaImportByModel1.setContent(Files.readString(import1Path));

        final List<ToscaImportByModel> modelImports = new ArrayList<>();
        modelImports.add(toscaImportByModel1);
        when(toscaModelImportCassandraDao.findAllByModel(modelName)).thenReturn(modelImports);

        modelOperation.addTypesToDefaultImports(ElementTypeEnum.ARTIFACT_TYPE, dataTypes, modelName);
        ArgumentCaptor<List<ToscaImportByModel>> importListArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(toscaModelImportCassandraDao).saveAll(eq(modelName), importListArgumentCaptor.capture());

        final List<ToscaImportByModel> actualImportList = importListArgumentCaptor.getValue();
        assertEquals(2, actualImportList.size());
        assertTrue(actualImportList.contains(toscaImportByModel1));

        var expectedAdditionalTypesImport = new ToscaImportByModel();
        expectedAdditionalTypesImport.setModelId(modelName);
        expectedAdditionalTypesImport.setFullPath(ADDITIONAL_TYPE_DEFINITIONS_PATH.toString());
        expectedAdditionalTypesImport.setContent(Files.readString(testResourcePath.resolve(Path.of("expected-additional_types-3.yaml"))));
        final ToscaImportByModel actualAdditionalTypesImport =
            actualImportList.stream().filter(expectedAdditionalTypesImport::equals).findFirst().orElse(null);
        assertNotNull(actualAdditionalTypesImport);
        assertEquals(expectedAdditionalTypesImport.getContent(), actualAdditionalTypesImport.getContent());

        var expectedImport1 = new ToscaImportByModel();
        expectedImport1.setModelId(modelName);
        expectedImport1.setFullPath(import1RelativePath.toString());
        expectedImport1.setContent(Files.readString(testResourcePath.resolve(Path.of("expected-import-3.yaml"))));
        final ToscaImportByModel actualImport1 = actualImportList.stream().filter(expectedImport1::equals).findFirst().orElse(null);
        assertNotNull(actualImport1);
        assertEquals(expectedImport1.getContent(), actualImport1.getContent());

    }

    @Test
    void addTypesToDefaultImportsTest_existingAdditionalTypesImport() throws IOException {
        var modelName = "model";
        final Path testResourcePath = Path.of("src/test/resources/modelOperation");

        final var dataTypesPath = testResourcePath.resolve(Path.of("input-data_types.yaml"));
        final var dataTypes = Files.readString(dataTypesPath);

        final Path import1RelativePath = Path.of("original-import-1.yaml");
        final Path import1Path = testResourcePath.resolve(import1RelativePath);

        var toscaImportByModel1 = new ToscaImportByModel();
        toscaImportByModel1.setModelId(modelName);
        toscaImportByModel1.setFullPath(import1RelativePath.toString());
        toscaImportByModel1.setContent(Files.readString(import1Path));

        var originalAdditionalTypesImport = new ToscaImportByModel();
        originalAdditionalTypesImport.setModelId(modelName);
        originalAdditionalTypesImport.setFullPath(ADDITIONAL_TYPE_DEFINITIONS_PATH.toString());
        final Path originalAdditionalTypesImportPath = testResourcePath.resolve(Path.of("original-additional_types-1.yaml"));
        originalAdditionalTypesImport.setContent(Files.readString(originalAdditionalTypesImportPath));

        final List<ToscaImportByModel> modelImports = new ArrayList<>();
        modelImports.add(toscaImportByModel1);
        modelImports.add(originalAdditionalTypesImport);
        when(toscaModelImportCassandraDao.findAllByModel(modelName)).thenReturn(modelImports);

        modelOperation.addTypesToDefaultImports(ElementTypeEnum.DATA_TYPE, dataTypes, modelName);
        ArgumentCaptor<List<ToscaImportByModel>> importListArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(toscaModelImportCassandraDao).saveAll(eq(modelName), importListArgumentCaptor.capture());

        final List<ToscaImportByModel> actualImportList = importListArgumentCaptor.getValue();
        assertEquals(2, actualImportList.size());
        assertTrue(actualImportList.contains(toscaImportByModel1));

        var expectedAdditionalTypesImport = new ToscaImportByModel();
        expectedAdditionalTypesImport.setModelId(modelName);
        expectedAdditionalTypesImport.setFullPath(ADDITIONAL_TYPE_DEFINITIONS_PATH.toString());
        expectedAdditionalTypesImport.setContent(Files.readString(testResourcePath.resolve(Path.of("expected-additional_types-2.yaml"))));
        final ToscaImportByModel actualAdditionalTypesImport =
            actualImportList.stream().filter(expectedAdditionalTypesImport::equals).findFirst().orElse(null);
        assertNotNull(actualAdditionalTypesImport);
        assertEquals(expectedAdditionalTypesImport.getContent(), actualAdditionalTypesImport.getContent());

        var expectedImport1 = new ToscaImportByModel();
        expectedImport1.setModelId(modelName);
        expectedImport1.setFullPath(import1RelativePath.toString());
        expectedImport1.setContent(Files.readString(testResourcePath.resolve(Path.of("expected-import-1.yaml"))));
        final ToscaImportByModel actualImport1 = actualImportList.stream().filter(expectedImport1::equals).findFirst().orElse(null);
        assertNotNull(actualImport1);
        assertEquals(expectedImport1.getContent(), actualImport1.getContent());
        
        // Update the added additional type
        final var updatedDataTypesPath = testResourcePath.resolve(Path.of("input-data_types-updated.yaml"));
        final var updatedDataTypes = Files.readString(updatedDataTypesPath);
        modelOperation.updateTypesInAdditionalTypesImport(ElementTypeEnum.DATA_TYPE, updatedDataTypes, modelName);
        
        ArgumentCaptor<List<ToscaImportByModel>> updatedImportListArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(toscaModelImportCassandraDao, times(2)).saveAll(eq(modelName), updatedImportListArgumentCaptor.capture());

        final List<ToscaImportByModel> updatedActualImportList = updatedImportListArgumentCaptor.getValue();
        assertEquals(2, updatedActualImportList.size());
        
        var expectedUpdatedAdditionalTypesImport = new ToscaImportByModel();
        expectedUpdatedAdditionalTypesImport.setModelId(modelName);
        expectedUpdatedAdditionalTypesImport.setFullPath(ADDITIONAL_TYPE_DEFINITIONS_PATH.toString());
        expectedUpdatedAdditionalTypesImport.setContent(Files.readString(testResourcePath.resolve(Path.of("expected-additional_types-2-updated.yaml"))));
        final ToscaImportByModel actualUpdatedAdditionalTypesImport =
            actualImportList.stream().filter(expectedUpdatedAdditionalTypesImport::equals).findFirst().orElse(null);
        assertNotNull(actualUpdatedAdditionalTypesImport);
        
        assertTrue(actualUpdatedAdditionalTypesImport.getContent().contains("added_property_1"));
        assertTrue(actualUpdatedAdditionalTypesImport.getContent().contains("added_property_2"));
    }

    @Test
    void addArtifactsToDefaultImportsTest_existingAdditionalTypesImport() throws IOException {
        var modelName = "model";
        final Path testResourcePath = Path.of("src/test/resources/modelOperation");

        final var dataTypesPath = testResourcePath.resolve(Path.of("input-artifact_types.yaml"));
        final var dataTypes = Files.readString(dataTypesPath);

        final Path import1RelativePath = Path.of("original-import-3.yaml");
        final Path import1Path = testResourcePath.resolve(import1RelativePath);

        var toscaImportByModel1 = new ToscaImportByModel();
        toscaImportByModel1.setModelId(modelName);
        toscaImportByModel1.setFullPath(import1RelativePath.toString());
        toscaImportByModel1.setContent(Files.readString(import1Path));

        var originalAdditionalTypesImport = new ToscaImportByModel();
        originalAdditionalTypesImport.setModelId(modelName);
        originalAdditionalTypesImport.setFullPath(ADDITIONAL_TYPE_DEFINITIONS_PATH.toString());
        final Path originalAdditionalTypesImportPath = testResourcePath.resolve(Path.of("original-additional_types-2.yaml"));
        originalAdditionalTypesImport.setContent(Files.readString(originalAdditionalTypesImportPath));

        final List<ToscaImportByModel> modelImports = new ArrayList<>();
        modelImports.add(toscaImportByModel1);
        modelImports.add(originalAdditionalTypesImport);
        when(toscaModelImportCassandraDao.findAllByModel(modelName)).thenReturn(modelImports);

        modelOperation.addTypesToDefaultImports(ElementTypeEnum.ARTIFACT_TYPE, dataTypes, modelName);
        ArgumentCaptor<List<ToscaImportByModel>> importListArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(toscaModelImportCassandraDao).saveAll(eq(modelName), importListArgumentCaptor.capture());

        final List<ToscaImportByModel> actualImportList = importListArgumentCaptor.getValue();
        assertEquals(2, actualImportList.size());
        assertTrue(actualImportList.contains(toscaImportByModel1));

        var expectedAdditionalTypesImport = new ToscaImportByModel();
        expectedAdditionalTypesImport.setModelId(modelName);
        expectedAdditionalTypesImport.setFullPath(ADDITIONAL_TYPE_DEFINITIONS_PATH.toString());
        expectedAdditionalTypesImport.setContent(Files.readString(testResourcePath.resolve(Path.of("expected-additional_types-3.yaml"))));
        final ToscaImportByModel actualAdditionalTypesImport =
            actualImportList.stream().filter(expectedAdditionalTypesImport::equals).findFirst().orElse(null);
        assertNotNull(actualAdditionalTypesImport);
        assertEquals(expectedAdditionalTypesImport.getContent(), actualAdditionalTypesImport.getContent());

        var expectedImport1 = new ToscaImportByModel();
        expectedImport1.setModelId(modelName);
        expectedImport1.setFullPath(import1RelativePath.toString());
        expectedImport1.setContent(Files.readString(testResourcePath.resolve(Path.of("expected-import-3.yaml"))));
        final ToscaImportByModel actualImport1 = actualImportList.stream().filter(expectedImport1::equals).findFirst().orElse(null);
        assertNotNull(actualImport1);
        assertEquals(expectedImport1.getContent(), actualImport1.getContent());

    }

    private ToscaImportByModel createModelImport(final String parentModelName, final String importPath) {
        var toscaImportByModel = new ToscaImportByModel();
        toscaImportByModel.setModelId(parentModelName);
        toscaImportByModel.setFullPath(importPath);
        return toscaImportByModel;
    }
}
