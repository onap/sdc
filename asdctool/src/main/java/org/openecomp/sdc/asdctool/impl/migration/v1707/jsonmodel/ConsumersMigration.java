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
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ConsumerOperation;
import org.openecomp.sdc.be.resources.data.ConsumerData;

import javax.annotation.Resource;
import java.util.List;

import static org.openecomp.sdc.asdctool.impl.migration.v1707.MigrationUtils.handleError;

public class ConsumersMigration extends JsonModelMigration<ConsumerData> {

    @Resource(name = "consumer-operation")
    private ConsumerOperation consumerOperation;

    @Resource(name = "consumer-operation-mig")
    private ConsumerOperation consumerOperationMigration;

    @Override
    Either<List<ConsumerData>, ?> getElementsToMigrate() {
        return consumerOperation.getAll();
    }

    @Override
    Either<ConsumerData, ?> getElementFromNewGraph(ConsumerData element) {
        return consumerOperationMigration.getCredentials(element.getConsumerDataDefinition().getConsumerName());
    }

    @Override
    boolean save(ConsumerData element) {
        return consumerOperationMigration.createCredentials(element)
                .either(savedConsumer -> true,
                        err -> handleError(String.format("failed to save consumer %s. reason: %s", element.getConsumerDataDefinition().getConsumerName(), err.name())));
    }

    @Override
    StorageOperationStatus getNotFoundErrorStatus() {
        return StorageOperationStatus.NOT_FOUND;
    }

    @Override
    public String description() {
        return "consumers migration";
    }
}
