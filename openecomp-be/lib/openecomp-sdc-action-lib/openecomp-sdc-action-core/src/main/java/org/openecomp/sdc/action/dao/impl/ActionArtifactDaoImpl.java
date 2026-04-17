/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.action.dao.impl;

import static org.onap.logging.ref.slf4j.ONAPLogConstants.ResponseStatus.COMPLETE;
import static org.onap.logging.ref.slf4j.ONAPLogConstants.ResponseStatus.ERROR;
import static org.openecomp.sdc.action.ActionConstants.TARGET_ENTITY_DB;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_INTERNAL_SERVER_ERR_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_QUERY_FAILURE_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_QUERY_FAILURE_MSG;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.action.dao.ActionArtifactDao;
import org.openecomp.sdc.action.dao.ActionArtifactDaoInternal;
import org.openecomp.sdc.action.dao.ActionArtifactMapper;
import org.openecomp.sdc.action.dao.ActionArtifactMapperBuilder;
import org.openecomp.sdc.action.dao.types.ActionArtifactEntity;
import org.openecomp.sdc.action.errors.ActionException;
import org.openecomp.sdc.action.logging.CategoryLogLevel;
import org.openecomp.sdc.action.types.ActionArtifact;
import org.openecomp.sdc.action.types.ActionSubOperation;
import org.openecomp.sdc.action.util.ActionUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import com.datastax.oss.driver.api.core.AllNodesFailedException;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.annotations.Query;

public class ActionArtifactDaoImpl extends CassandraBaseDao<ActionArtifactEntity> implements ActionArtifactDao {

    private final ActionArtifactDaoInternal dao;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    public ActionArtifactDaoImpl(CqlSession session) {
        super(session);
        ActionArtifactMapper mapper = new ActionArtifactMapperBuilder(session).build();
        this.dao = mapper.actionArtifactDao();
    }



    @Override
    protected Object[] getKeys(ActionArtifactEntity entity) {
        return new Object[]{entity.getArtifactUuId(), entity.getEffectiveVersion()};
    }

    @Override
    public Collection<ActionArtifactEntity> list(ActionArtifactEntity entity) {
        return null;
    }

    @Override
    public void uploadArtifact(ActionArtifact data) {
        log.debug(" entering uploadArtifact with artifactName= " + data.getArtifactName());
        try {
            ActionUtil.actionLogPreProcessor(ActionSubOperation.CREATE_ACTION_ARTIFACT, TARGET_ENTITY_DB);
            this.create(data.toEntity());
            ActionUtil.actionLogPostProcessor(COMPLETE, null, "", false);
            log.metrics("");
        } catch (AllNodesFailedException noHostAvailableException) {
            logGenericException(noHostAvailableException);
            throw new ActionException(ACTION_INTERNAL_SERVER_ERR_CODE, ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
        }
        log.debug(" exit uploadArtifact with artifactName= " + data.getArtifactName());
    }

    @Override
    public ActionArtifact downloadArtifact(int effectiveVersion, String artifactUuId) {
        log.debug(" entering downloadArtifact with artifactUUID= " + artifactUuId);
        ActionArtifact actionArtifact = null;
        try {
            ActionUtil.actionLogPreProcessor(ActionSubOperation.GET_ARTIFACT_BY_ARTIFACTUUID, TARGET_ENTITY_DB);

            Optional<ActionArtifactEntity> optionalEntity = dao.getArtifactByUuId(effectiveVersion, artifactUuId);

            ActionUtil.actionLogPostProcessor(COMPLETE, null, "", false);
            log.metrics("");

            if (optionalEntity.isPresent()) {
                actionArtifact = optionalEntity.get().toDto();
            }

        } catch (AllNodesFailedException ex) {
            logGenericException(ex);
            throw new ActionException(ACTION_INTERNAL_SERVER_ERR_CODE, ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
        }
        log.debug(" exit downloadArtifact with artifactUUID= " + artifactUuId);
        return actionArtifact;
    }

    @Override
    public void updateArtifact(ActionArtifact data) {
        log.debug(" entering updateArtifact with artifactName= " + data.getArtifactName());
        try {
            ActionUtil.actionLogPreProcessor(ActionSubOperation.UPDATE_ACTION_ARTIFACT, TARGET_ENTITY_DB);
            this.update(data.toEntity());
            ActionUtil.actionLogPostProcessor(COMPLETE, null, "", false);
            log.metrics("");
        } catch (AllNodesFailedException noHostAvailableException) {
            logGenericException(noHostAvailableException);
            throw new ActionException(ACTION_INTERNAL_SERVER_ERR_CODE, ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
        }
        log.debug(" exit updateArtifact with artifactName= " + data.getArtifactName());
    }

    private void logGenericException(Exception exception) {
        ActionUtil.actionLogPostProcessor(ERROR, ACTION_QUERY_FAILURE_CODE, ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG, false);
        log.metrics("");
        ActionUtil.actionErrorLogProcessor(CategoryLogLevel.FATAL, ACTION_QUERY_FAILURE_CODE, ACTION_QUERY_FAILURE_MSG);
        log.error(exception.getMessage());
    }

    @Override
    protected String getTableName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTableName'");
    }

    @Override
    protected String[] getColumns(ActionArtifactEntity entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getColumns'");
    }

    @Override
    protected Object[] getValues(ActionArtifactEntity entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getValues'");
    }
}
