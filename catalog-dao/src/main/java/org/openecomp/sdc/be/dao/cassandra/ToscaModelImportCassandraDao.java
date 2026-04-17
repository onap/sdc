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

import static java.util.function.Predicate.not;
import static org.openecomp.sdc.common.api.Constants.ADDITIONAL_TYPE_DEFINITIONS;
import com.datastax.oss.driver.api.core.CqlSession;

import fj.data.Either;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.openecomp.sdc.be.dao.api.exception.CassandraDaoInitException;
import org.openecomp.sdc.be.dao.api.exception.CassandraDaoInitExceptionProvider;
import org.openecomp.sdc.be.resources.data.ToscaImportByModel;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("tosca-model-import-cassandra-dao")
public class ToscaModelImportCassandraDao extends CassandraDao {

    private static final Logger LOGGER = Logger.getLogger(ToscaModelImportCassandraDao.class.getName());

    private ToscaImportByModelAccessor toscaImportByModelAccessor;


    @Autowired
    public ToscaModelImportCassandraDao(final CassandraClient cassandraClient) {
        super(cassandraClient);
    }


    @PostConstruct
    public void init() {
        final var keyspace = AuditingTypesConstants.ARTIFACT_KEYSPACE;
        if (!client.isConnected()) {
            LOGGER.error(EcompLoggerErrorCode.SCHEMA_ERROR, ToscaModelImportCassandraDao.class.getName(), "Cassandra client isn't connected");
            return;
        }
        final Either<CqlSession , CassandraOperationStatus> connectionResult = client.connect(keyspace);
        if (connectionResult.isRight()) {
            final CassandraDaoInitException exception =
                CassandraDaoInitExceptionProvider.keySpaceConnectError(keyspace, connectionResult.right().value()).get();
            LOGGER.error(EcompLoggerErrorCode.SCHEMA_ERROR, ToscaModelImportCassandraDao.class.getName(), exception.getMessage());
            throw exception;
        }
        session = connectionResult.left().value();
        ToscaModelImportCassandraDaoMapper mapper = new ToscaModelImportCassandraDaoMapperBuilder(session).build();
        toscaImportByModelAccessor = mapper.toscaImportByModelAccessor();
        LOGGER.info("{} successfully initialized", ToscaModelImportCassandraDao.class.getName());
    }

    /**
     * Completely replaces the previous model imports by the imports on the given list that are from the same model.
     * New imports will be added, existing will be replaced and the remaining will be deleted.
     *
     * @param modelId                the model id
     * @param toscaImportByModelList the new list of imports
     */
    public void replaceImports(final String modelId, final List<ToscaImportByModel> toscaImportByModelList) {
        final List<ToscaImportByModel> importOfModelList = toscaImportByModelList.stream()
            .filter(toscaImportByModel -> modelId.equals(toscaImportByModel.getModelId()))
            .collect(Collectors.toList());
        final List<ToscaImportByModel> actualImportOfModelList = toscaImportByModelAccessor.findAllByModel(modelId).all();
        final List<ToscaImportByModel> removedImportList = actualImportOfModelList.stream()
            .filter(not(importOfModelList::contains))
            .filter(not(toscaImport -> ADDITIONAL_TYPE_DEFINITIONS.equals(toscaImport.getFullPath())))
            .collect(Collectors.toList());

        importOfModelList.forEach(toscaImportByModelAccessor::save);
        removedImportList.forEach(toscaImportByModelAccessor::delete);
    }

    /**
     * Saves all imports provided on the list that are from the given modelId.
     *
     * @param modelId                the model id
     * @param toscaImportByModelList the list of imports to save
     */
    public void saveAll(final String modelId, final List<ToscaImportByModel> toscaImportByModelList) {
        toscaImportByModelList.stream()
            .filter(toscaImportByModel -> modelId.equals(toscaImportByModel.getModelId()))
            .forEach(toscaImportByModelAccessor::save);
    }

    public List<ToscaImportByModel> findAllByModel(final String modelId) {
        return toscaImportByModelAccessor.findAllByModel(modelId).all();
    }

    public void deleteAllByModel(final String modelId) {
        final List<ToscaImportByModel> allByModel = findAllByModel(modelId);
        allByModel.forEach(toscaImportByModelAccessor::delete);
    }

}
