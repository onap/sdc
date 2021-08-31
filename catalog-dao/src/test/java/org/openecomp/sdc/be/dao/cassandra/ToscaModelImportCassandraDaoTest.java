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

package org.openecomp.sdc.be.dao.cassandra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import fj.data.Either;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.api.exception.CassandraDaoInitException;
import org.openecomp.sdc.be.dao.api.exception.CassandraDaoInitExceptionProvider;
import org.openecomp.sdc.be.data.model.ToscaImportByModel;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;

class ToscaModelImportCassandraDaoTest {

    @Mock
    private CassandraClient cassandraClient;

    @InjectMocks
    private ToscaModelImportCassandraDao toscaModelImportCassandraDao;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAllByModelTest() {
        final var toscaImportByModelAccessorMock = mock(ToscaImportByModelAccessor.class);
        toscaModelImportCassandraDao = new ToscaModelImportCassandraDao(toscaImportByModelAccessorMock, null);
        final var modelId = "modelId";
        final ToscaImportByModel toscaImportByModel1 = new ToscaImportByModel();
        final ToscaImportByModel toscaImportByModel2 = new ToscaImportByModel();
        final List<ToscaImportByModel> expectedImportModelList = List.of(toscaImportByModel1, toscaImportByModel2);

        final Result<ToscaImportByModel> result = mock(Result.class);
        when(result.all()).thenReturn(expectedImportModelList);
        when(toscaImportByModelAccessorMock.findAllByModel(modelId)).thenReturn(result);

        final List<ToscaImportByModel> actualImportModelList = toscaModelImportCassandraDao.findAllByModel(modelId);
        assertEquals(expectedImportModelList.size(), actualImportModelList.size());
        assertTrue(actualImportModelList.contains(toscaImportByModel1));
        assertTrue(actualImportModelList.contains(toscaImportByModel2));
    }

    @Test
    void importAllTest() {
        final var toscaImportByModelAccessorMock = mock(ToscaImportByModelAccessor.class);
        final Mapper<ToscaImportByModel> toscaImportByModelMapperMock = mock(Mapper.class);
        toscaModelImportCassandraDao = new ToscaModelImportCassandraDao(toscaImportByModelAccessorMock, toscaImportByModelMapperMock);

        final var modelId = "modelId";
        final var toscaImportByModel1 = createToscaModelByImport(modelId, "path/model/1");
        final var toscaImportByModel2 = createToscaModelByImport(modelId, "path/model/2");
        final var toscaImportByModel3 = createToscaModelByImport(modelId, "path/model/3");
        final var toscaImportByModelWrongModel = createToscaModelByImport("otherModel", "path/wrong-model/1");
        final var importModelList = List.of(toscaImportByModel1, toscaImportByModel2, toscaImportByModel3);

        final var toscaImportByModelDatabase1 = createToscaModelByImport(modelId, "toscaImportByModelDatabase1");
        final Result<ToscaImportByModel> findAllByModelResult = mock(Result.class);
        when(findAllByModelResult.all()).thenReturn(List.of(toscaImportByModel1, toscaImportByModelDatabase1));
        when(toscaImportByModelAccessorMock.findAllByModel(modelId)).thenReturn(findAllByModelResult);

        toscaModelImportCassandraDao.replaceImports(modelId, importModelList);

        verify(toscaImportByModelMapperMock).save(toscaImportByModel1);
        verify(toscaImportByModelMapperMock).save(toscaImportByModel2);
        verify(toscaImportByModelMapperMock).save(toscaImportByModel3);
        verify(toscaImportByModelMapperMock, never()).save(toscaImportByModelWrongModel);
        verify(toscaImportByModelMapperMock).delete(toscaImportByModelDatabase1.getModelId(), toscaImportByModelDatabase1.getFullPath());
        verify(toscaImportByModelMapperMock, never()).delete(toscaImportByModel1.getModelId(), toscaImportByModel1.getFullPath());
        verify(toscaImportByModelMapperMock, never()).delete(toscaImportByModel2.getModelId(), toscaImportByModel2.getFullPath());
        verify(toscaImportByModelMapperMock, never()).delete(toscaImportByModel3.getModelId(), toscaImportByModel3.getFullPath());
        verify(toscaImportByModelMapperMock, never()).delete(toscaImportByModelWrongModel.getModelId(), toscaImportByModelWrongModel.getFullPath());
    }

    @Test
    void initSuccessTest() {
        toscaModelImportCassandraDao = new ToscaModelImportCassandraDao(cassandraClient);
        when(cassandraClient.isConnected()).thenReturn(true);
        final Session sessionMock = mock(Session.class);
        final MappingManager mappingManagerMock = mock(MappingManager.class);
        when(cassandraClient.connect(AuditingTypesConstants.ARTIFACT_KEYSPACE)).thenReturn(Either.left(new ImmutablePair<>(sessionMock, mappingManagerMock)));
        toscaModelImportCassandraDao.init();
        verify(cassandraClient).connect(AuditingTypesConstants.ARTIFACT_KEYSPACE);
        verify(mappingManagerMock).mapper(ToscaImportByModel.class);
        verify(mappingManagerMock).createAccessor(ToscaImportByModelAccessor.class);
    }

    @Test
    void initTest_clientNotConnected() {
        toscaModelImportCassandraDao = new ToscaModelImportCassandraDao(cassandraClient);
        when(cassandraClient.isConnected()).thenReturn(false);
        toscaModelImportCassandraDao.init();
        verify(cassandraClient, never()).connect(anyString());
    }

    @Test
    void initTest_keyspaceConnectionFailure() {
        toscaModelImportCassandraDao = new ToscaModelImportCassandraDao(cassandraClient);
        when(cassandraClient.isConnected()).thenReturn(true);
        when(cassandraClient.connect(AuditingTypesConstants.ARTIFACT_KEYSPACE))
            .thenReturn(Either.right(CassandraOperationStatus.KEYSPACE_NOT_CONNECTED));

        final CassandraDaoInitException actualException = assertThrows(CassandraDaoInitException.class, () -> toscaModelImportCassandraDao.init());

        final CassandraDaoInitException expectedException = CassandraDaoInitExceptionProvider
            .keySpaceConnectError(AuditingTypesConstants.ARTIFACT_KEYSPACE, CassandraOperationStatus.KEYSPACE_NOT_CONNECTED).get();
        assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    private ToscaImportByModel createToscaModelByImport(final String modelId, final String path) {
        final var toscaImportByModel3 = new ToscaImportByModel();
        toscaImportByModel3.setModelId(modelId);
        toscaImportByModel3.setFullPath(path);
        return toscaImportByModel3;
    }
}