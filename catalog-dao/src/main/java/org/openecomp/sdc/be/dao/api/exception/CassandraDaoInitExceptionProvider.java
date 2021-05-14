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

package org.openecomp.sdc.be.dao.api.exception;

import java.util.function.Supplier;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;

public class CassandraDaoInitExceptionProvider {

    private CassandraDaoInitExceptionProvider() {

    }

    public static Supplier<CassandraDaoInitException> keySpaceConnectError(final String keyspace, final CassandraOperationStatus cassandraOperationStatus) {
        var errorMsg = String.format("Could not connect to keyspace '%s'. Operation status was '%s'", keyspace, cassandraOperationStatus);
        return () -> new CassandraDaoInitException(errorMsg);
    }

}
