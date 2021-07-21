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

import fj.data.Either;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ToscaModelImportCassandraDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.data.model.ToscaImportByModel;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.OperationException;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.ModelOperationExceptionSupplier;
import org.openecomp.sdc.be.resources.data.ModelData;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("model-operation")
public class ModelOperation {

    private static final Logger log = Logger.getLogger(ModelOperation.class);

    private final JanusGraphGenericDao janusGraphGenericDao;
    private final JanusGraphDao janusGraphDao;
    private final ToscaModelImportCassandraDao toscaModelImportCassandraDao;

    @Autowired
    public ModelOperation(final JanusGraphGenericDao janusGraphGenericDao,
                          final JanusGraphDao janusGraphDao,
                          final ToscaModelImportCassandraDao toscaModelImportCassandraDao) {
        this.janusGraphGenericDao = janusGraphGenericDao;
        this.janusGraphDao = janusGraphDao;
        this.toscaModelImportCassandraDao = toscaModelImportCassandraDao;
    }

    public Model createModel(final Model model, final boolean inTransaction) {
        Model result = null;
        final var modelData = new ModelData(model.getName(), UniqueIdBuilder.buildModelUid(model.getName()));
        try {
            final Either<ModelData, JanusGraphOperationStatus> createNode = janusGraphGenericDao.createNode(modelData, ModelData.class);
            if (createNode.isRight()) {
                final var janusGraphOperationStatus = createNode.right().value();
                log.error(EcompLoggerErrorCode.DATA_ERROR, ModelOperation.class.getName(), "Problem while creating model, reason {}",
                    janusGraphOperationStatus);
                if (janusGraphOperationStatus == JanusGraphOperationStatus.JANUSGRAPH_SCHEMA_VIOLATION) {
                    throw ModelOperationExceptionSupplier.modelAlreadyExists(model.getName()).get();
                }
                throw new OperationException(ActionStatus.GENERAL_ERROR,
                    String.format("Failed to create model %s on JanusGraph with %s error", model, janusGraphOperationStatus));
            }
            result = new Model(createNode.left().value().getName());
            return result;
        } finally {
            if (!inTransaction) {
                if (Objects.nonNull(result)) {
                    janusGraphGenericDao.commit();
                } else {
                    janusGraphGenericDao.rollback();
                }
            }
        }
    }

    public Optional<GraphVertex> findModelVertexByName(final String name) {
        if (StringUtils.isEmpty(name)) {
            return Optional.empty();
        }
        final Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.NAME, name);
        props.put(GraphPropertyEnum.UNIQUE_ID, UniqueIdBuilder.buildModelUid(name));
        final List<GraphVertex> modelVerticesList = findModelVerticesByCriteria(props);
        if (modelVerticesList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(modelVerticesList.get(0));
    }

    public Optional<Model> findModelByName(final String name) {
        if (StringUtils.isEmpty(name)) {
            return Optional.empty();
        }
        final Optional<GraphVertex> modelVertexOpt = findModelVertexByName(name);
        if (modelVertexOpt.isEmpty()) {
            return Optional.empty();
        }

        final GraphVertex graphVertex = modelVertexOpt.get();
        return Optional.of(convertToModel(graphVertex));
    }

    public void createModelImports(final String modelId, final Map<String, byte[]> zipContent) {
        if (MapUtils.isEmpty(zipContent)) {
            return;
        }
        final List<ToscaImportByModel> toscaImportByModelList = zipContent.entrySet().stream()
            .map(entry -> {
                final String path = entry.getKey();
                final byte[] bytes = entry.getValue();
                final String content = new String(bytes, StandardCharsets.UTF_8);
                final var toscaImportByModel = new ToscaImportByModel();
                toscaImportByModel.setModelId(modelId);
                toscaImportByModel.setFullPath(path);
                toscaImportByModel.setContent(content);
                return toscaImportByModel;
            }).collect(Collectors.toList());
        toscaModelImportCassandraDao.importAll(modelId, toscaImportByModelList);
    }

    /**
     * Finds all the models.
     *
     * @return the list of models
     */
    public List<Model> findAllModels() {
        return findModelsByCriteria(Collections.emptyMap());
    }

    private List<Model> findModelsByCriteria(final Map<GraphPropertyEnum, Object> propertyCriteria) {
        final List<GraphVertex> modelVerticesByCriteria = findModelVerticesByCriteria(propertyCriteria);
        if (modelVerticesByCriteria.isEmpty()) {
            return Collections.emptyList();
        }

        return modelVerticesByCriteria.stream().map(this::convertToModel).collect(Collectors.toList());
    }

    private List<GraphVertex> findModelVerticesByCriteria(final Map<GraphPropertyEnum, Object> propertyCriteria) {
        final Either<List<GraphVertex>, JanusGraphOperationStatus> result = janusGraphDao.getByCriteria(VertexTypeEnum.MODEL, propertyCriteria);
        if (result.isRight()) {
            final var janusGraphOperationStatus = result.right().value();
            if (janusGraphOperationStatus == JanusGraphOperationStatus.NOT_FOUND) {
                return Collections.emptyList();
            }
            final var operationException = ModelOperationExceptionSupplier.failedToRetrieveModels(janusGraphOperationStatus).get();
            log.error(EcompLoggerErrorCode.DATA_ERROR, this.getClass().getName(), operationException.getMessage());
            throw operationException;
        }
        return result.left().value();
    }

    private Model convertToModel(final GraphVertex modelGraphVertex) {
        return new Model((String) modelGraphVertex.getMetadataProperty(GraphPropertyEnum.NAME));
    }
}


