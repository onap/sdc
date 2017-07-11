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

package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import fj.data.Either;
import org.openecomp.sdc.asdctool.impl.migration.Migration1707Task;
import org.openecomp.sdc.asdctool.impl.migration.MigrationMsg;
import org.openecomp.sdc.asdctool.impl.migration.v1707.MigrationUtils;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;

public abstract class JsonModelMigration<T> implements Migration1707Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonModelMigration.class);

    @Resource(name = "titan-dao")
    TitanDao titanDao;

    @Override
    public boolean migrate() {
        Either<List<T>, ?> elementsToMigrate = getElementsToMigrate();
        return elementsToMigrate.either(this::migrateElementsToNewGraph,
                                        errorStatus -> MigrationUtils.handleError(MigrationMsg.FAILED_TO_RETRIEVE_NODES.getMessage(errorStatus.toString())));
    }

    boolean doPostSaveOperation(T element) {
        return true;
    }

    boolean doPostMigrateOperation(List<T> elements) {
        return true;
    }

    void doPreMigrationOperation(List<T> elements){}

    private boolean migrateElementsToNewGraph(List<T> elementsToMigrate) {
        LOGGER.info(this.description() + ": starting to migrate elements to new graph. elements to migrate: {}", elementsToMigrate.size());
        doPreMigrationOperation(elementsToMigrate);
        for (T node : elementsToMigrate) {
            boolean migratedSuccessfully = migrateElement(node);
            if (!migratedSuccessfully) {
                titanDao.rollback();
                return false;
            }
            titanDao.commit();
        }
        return postMigrate(elementsToMigrate);
    }

    private boolean migrateElement(T node) {
        boolean savedSuccessfully = saveElementIfNotExists(node);
        return savedSuccessfully && doPostSaveOperation(node);
    }

    private boolean postMigrate(List<T> elements) {
        boolean postMigrateSuccessfully = doPostMigrateOperation(elements);
        if (!postMigrateSuccessfully) {
            titanDao.rollback();
            return false;
        }
        titanDao.commit();
        return true;
    }

    private boolean saveElementIfNotExists(T element) {
        return isExists(element).either(isExist -> isExist || save(element),
                                        status -> MigrationUtils.handleError(MigrationMsg.FAILED_TO_GET_NODE_FROM_GRAPH.getMessage(status.toString())));
    }


    private Either<Boolean, ?> isExists(T element) {
        Either<T, ?> byId = getElementFromNewGraph(element);
        return byId.either(existingVal -> Either.left(true),
                           this::getEitherNotExistOrErrorStatus);
    }

    private <S> Either<Boolean, S> getEitherNotExistOrErrorStatus(S status) {
        return status == getNotFoundErrorStatus() ? Either.left(false) : Either.right(status);
    }

    abstract Either<List<T>, ?> getElementsToMigrate();

    abstract Either<T, ?> getElementFromNewGraph(T element);

    abstract boolean save(T element);

    abstract <S extends Enum> S getNotFoundErrorStatus();


}
