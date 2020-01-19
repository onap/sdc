/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.asdctool.migration.tasks.mig1902;

import fj.data.Either;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.resources.data.ConsumerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;

@Component
public class SdcConsumerMigration implements Migration {

    private static final Logger logger = LoggerFactory.getLogger(SdcConsumerMigration.class);

    private JanusGraphGenericDao janusGraphGenericDao;

    public SdcConsumerMigration(JanusGraphGenericDao janusGraphGenericDao) {
        this.janusGraphGenericDao = janusGraphGenericDao;
    }

    @Override
    public String description() {
        return "remove all consumer nodes";
    }

    @Override
    public DBVersion getVersion() {
        return DBVersion.from(BigInteger.valueOf(1902), BigInteger.valueOf(0));
    }

    @Override
    public MigrationResult migrate() {
        JanusGraphOperationStatus status = null;
        try {
            status = handleConsumerNodes();
            if (status == JanusGraphOperationStatus.OK){
                logger.info("removed all consumer nodes.");
                return MigrationResult.success();
            } else {
                return MigrationResult.error("failed to remove consumer nodes. error: " + status);
            }
        } finally {
            commitOrRollBack(status);
        }
    }

    private void commitOrRollBack(JanusGraphOperationStatus status) {
        if (status == JanusGraphOperationStatus.OK) {
            janusGraphGenericDao.commit();
        } else {
            janusGraphGenericDao.rollback();
        }
    }

    private JanusGraphOperationStatus handleConsumerNodes() {
        logger.info("getting all consumer nodes.");
        return janusGraphGenericDao.getAll(NodeTypeEnum.ConsumerCredentials, ConsumerData.class)
                .either(this::removeConsumerNodes, this::handleError);
    }

    private JanusGraphOperationStatus removeConsumerNodes(List<ConsumerData> consumerNodes){
        logger.info("found {} consumer nodes.", consumerNodes.size());
        return consumerNodes.stream()
                .map(consumerNode -> janusGraphGenericDao.deleteNode(consumerNode, ConsumerData.class))
                .filter(Either::isRight)
                .map(either -> either.right().value())
                .findAny()
                .orElse(JanusGraphOperationStatus.OK);
    }

    private JanusGraphOperationStatus handleError(JanusGraphOperationStatus status){
        if (status == JanusGraphOperationStatus.NOT_FOUND) {
            logger.info("found 0 consumer nodes.");
            return JanusGraphOperationStatus.OK;
        } else{
            return status;
        }
    }

}
