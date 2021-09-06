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
package org.openecomp.sdc.be.components.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.exception.BusinessException;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.ModelOperationExceptionSupplier;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.OperationException;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.common.zip.ZipUtils;
import org.openecomp.sdc.common.zip.exception.ZipException;

class ModelBusinessLogicTest {

    @InjectMocks
    private ModelBusinessLogic modelBusinessLogic;
    @Mock
    private ModelOperation modelOperation;
    @Mock
    private DataTypeImportManager dataTypeImportManager;
    private Model model;
    private final Path modelImportsResourcePath = Path.of("src/test/resources/modelImports");

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        initTestData();
    }

    private void initTestData() {
        model = new Model("ETSI-SDC-MODEL-TEST");
    }

    @Test
    void createModelTest() {
        when(modelOperation.createModel(model, false)).thenReturn(model);
        final Model result = modelBusinessLogic.createModel(model);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(model.getName());
    }

    @Test
    void createModelFailTest() {
        when(modelOperation.createModel(model, false))
            .thenThrow(ModelOperationExceptionSupplier.modelAlreadyExists(model.getName()).get());
        final BusinessException exception = assertThrows(BusinessException.class, () -> modelBusinessLogic.createModel(model));
        assertThat(((OperationException) exception).getActionStatus().name()).isEqualTo(ActionStatus.MODEL_ALREADY_EXISTS.name());
        assertThat(((OperationException) exception).getParams()).contains(model.getName());
    }

    @Test
    void createModelImportsSuccessTest() throws IOException, ZipException {
        final var modelId = "modelId";
        final var resolve = modelImportsResourcePath.resolve("modelWithSubFolderAndEmptyFolder.zip");
        final var zipBytes = Files.readAllBytes(resolve);
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);
        final Map<String, byte[]> expectedZipMap = ZipUtils.readZip(zipBytes, false);

        when(modelOperation.findModelByName(modelId)).thenReturn(Optional.of(new Model(modelId)));
        doNothing().when(modelOperation).createModelImports(eq(modelId), anyMap());

        modelBusinessLogic.createModelImports(modelId, byteArrayInputStream);

        final ArgumentCaptor<Map<String, byte[]>> zipMapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(modelOperation).createModelImports(eq(modelId), zipMapArgumentCaptor.capture());
        expectedZipMap.keySet().forEach(key -> assertTrue(zipMapArgumentCaptor.getValue().containsKey(key), "Expecting import " + key));
    }

    @Test
    void createModelImportsTest_invalidModel() {
        //given an empty model
        final var modelId = "";

        final var emptyByteArrayInputStream = new ByteArrayInputStream(new byte[0]);
        var actualOperationException = assertThrows(OperationException.class,
            () -> modelBusinessLogic.createModelImports(modelId, emptyByteArrayInputStream));

        var expectedOperationException = ModelOperationExceptionSupplier.invalidModel(modelId).get();
        assertEquals(actualOperationException.getActionStatus(), expectedOperationException.getActionStatus());
        assertEquals(actualOperationException.getParams().length, expectedOperationException.getParams().length);
        assertEquals(actualOperationException.getParams()[0], expectedOperationException.getParams()[0]);

        //given a null model
        actualOperationException = assertThrows(OperationException.class,
            () -> modelBusinessLogic.createModelImports(null, emptyByteArrayInputStream));

        expectedOperationException = ModelOperationExceptionSupplier.invalidModel(null).get();
        assertEquals(actualOperationException.getActionStatus(), expectedOperationException.getActionStatus());
        assertEquals(actualOperationException.getParams().length, expectedOperationException.getParams().length);
        assertEquals(actualOperationException.getParams()[0], expectedOperationException.getParams()[0]);
    }

    @Test
    void createModelImportsTest_nullInputStream() {
        final var modelId = "modelId";

        final OperationException actualOperationException = assertThrows(OperationException.class,
            () -> modelBusinessLogic.createModelImports(modelId, null));

        final OperationException expectedOperationException = ModelOperationExceptionSupplier.emptyModelImports().get();
        assertEquals(actualOperationException.getActionStatus(), expectedOperationException.getActionStatus());
        assertEquals(actualOperationException.getParams().length, expectedOperationException.getParams().length);
    }

    @Test
    void createModelImportsTest_emptyModelImports() throws IOException {
        final var modelId = "modelId";

        final var resolve = modelImportsResourcePath.resolve("emptyModelImports.zip");
        final var zipBytes = Files.readAllBytes(resolve);
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);

        when(modelOperation.findModelByName(modelId)).thenReturn(Optional.of(new Model(modelId)));

        final OperationException actualOperationException = assertThrows(OperationException.class,
            () -> modelBusinessLogic.createModelImports(modelId, byteArrayInputStream));

        final OperationException expectedOperationException = ModelOperationExceptionSupplier.emptyModelImports().get();
        assertEquals(actualOperationException.getActionStatus(), expectedOperationException.getActionStatus());
        assertEquals(actualOperationException.getParams().length, expectedOperationException.getParams().length);
    }

    @Test
    void createModelImportsTest_modelNotFound() {
        final var modelId = "modelId";
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(new byte[0]);

        when(modelOperation.findModelByName(modelId)).thenReturn(Optional.empty());

        final OperationException actualOperationException = assertThrows(OperationException.class,
            () -> modelBusinessLogic.createModelImports(modelId, byteArrayInputStream));

        final OperationException expectedOperationException = ModelOperationExceptionSupplier.invalidModel(modelId).get();
        assertEquals(actualOperationException.getActionStatus(), expectedOperationException.getActionStatus());
        assertEquals(actualOperationException.getParams().length, expectedOperationException.getParams().length);
    }

    @Test
    void findModelSuccessTest() {
        final var modelId = "modelId";
        when(modelOperation.findModelByName(modelId)).thenReturn(Optional.of(new Model(modelId)));
        final Optional<Model> actualModel = modelBusinessLogic.findModel(modelId);
        assertTrue(actualModel.isPresent());
        assertEquals(new Model(modelId), actualModel.get());
    }

    @Test
    void findModelTest_emptyOrNullModelName() {
        when(modelOperation.findModelByName(anyString())).thenReturn(Optional.of(new Model()));
        var actualModel = modelBusinessLogic.findModel("");
        assertTrue(actualModel.isEmpty());
        actualModel = modelBusinessLogic.findModel(null);
        assertTrue(actualModel.isEmpty());
    }

    @Test
    void listModelsSuccessTest() {
        final List<Model> expectedModelList = List.of(new Model());
        when(modelOperation.findAllModels()).thenReturn(expectedModelList);
        final List<Model> actualModelList = modelBusinessLogic.listModels();
        assertEquals(expectedModelList, actualModelList, "The model list should be as expected");
    }

    @Test
    void listModelsTest_emptyList() {
        when(modelOperation.findAllModels()).thenReturn(Collections.emptyList());
        final List<Model> actualModelList = modelBusinessLogic.listModels();
        assertTrue(actualModelList.isEmpty(), "The model list should be empty");
    }
}
