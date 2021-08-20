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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ToscaModelImportCassandraDao;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
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
import org.openecomp.sdc.be.model.operations.api.DerivedFromOperation;
import org.openecomp.sdc.be.resources.data.ModelData;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:application-context-test.xml")
class ModelOperationTest extends ModelTestBase {

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

    private final String modelName = "ETSI-SDC-MODEL-TEST";

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
        final ModelData modelData = new ModelData(modelName,  UniqueIdBuilder.buildModelUid(modelName), ModelTypeEnum.NORMATIVE);
        when(janusGraphGenericDao.createNode(any(),any())).thenReturn(Either.left(modelData));
        final Model createdModel = modelOperation.createModel(new Model(modelName, ModelTypeEnum.NORMATIVE), false);
        assertThat(createdModel).isNotNull();
        assertThat(createdModel.getName()).isEqualTo(modelName);
    }
    
    @Test
    void createDerivedModelSuccessTest() {
        final String derivedModelName = "derivedModel";
        final ModelData modelData = new ModelData(derivedModelName,  UniqueIdBuilder.buildModelUid(derivedModelName), ModelTypeEnum.NORMATIVE);
        when(janusGraphGenericDao.createNode(any(),any())).thenReturn(Either.left(modelData));
        
        final GraphVertex modelVertex = new GraphVertex();
        modelVertex.addMetadataProperty(GraphPropertyEnum.NAME, "baseModel");
        modelVertex.addMetadataProperty(GraphPropertyEnum.MODEL_TYPE, ModelTypeEnum.NORMATIVE.getValue());
        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.MODEL), anyMap())).thenReturn(Either.left(Collections.singletonList(modelVertex)));
        when(janusGraphGenericDao.getChild(eq("uid"), anyString(), eq(GraphEdgeLabels.DERIVED_FROM), eq(NodeTypeEnum.Model), eq(ModelData.class))).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        when(derivedFromOperation.addDerivedFromRelation("model.derivedModel", "model.baseModel", NodeTypeEnum.Model)).thenReturn(Either.left(new GraphRelation()));
        
        final Model createdModel = modelOperation.createModel(new Model(derivedModelName, modelName, ModelTypeEnum.NORMATIVE), false);
        assertThat(createdModel).isNotNull();
        assertThat(createdModel.getName()).isEqualTo(derivedModelName);
    }

    @Test
    void createModelFailWithModelAlreadyExistTest() {
        when(janusGraphGenericDao.createNode(any(),any())).thenReturn(Either.right(JanusGraphOperationStatus.JANUSGRAPH_SCHEMA_VIOLATION));
        final var model = new Model(modelName, ModelTypeEnum.NORMATIVE);
        assertThrows(OperationException.class, () -> modelOperation.createModel(model, false));
    }

    @Test
    void createModelFailTest() {
        when(janusGraphGenericDao.createNode(any(),any())).thenReturn(Either.right(JanusGraphOperationStatus.GRAPH_IS_NOT_AVAILABLE));
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

        verify(toscaModelImportCassandraDao).importAll(modelId, toscaImportByModelList);
    }

    @Test
    void createModelImportsTest_emptyZipContent() {
        var modelId = "modelId";
        modelOperation.createModelImports(modelId, Collections.emptyMap());
        verify(toscaModelImportCassandraDao, never()).importAll(eq(modelId), anyList());
        modelOperation.createModelImports(modelId, null);
        verify(toscaModelImportCassandraDao, never()).importAll(eq(null), anyList());
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
        when(janusGraphGenericDao.getChild("uid", UniqueIdBuilder.buildModelUid(modelName), GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Model,
            ModelData.class)).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
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
        when(janusGraphGenericDao.getChild("uid", UniqueIdBuilder.buildModelUid(modelName), GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Model,
            ModelData.class)).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));

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
}
