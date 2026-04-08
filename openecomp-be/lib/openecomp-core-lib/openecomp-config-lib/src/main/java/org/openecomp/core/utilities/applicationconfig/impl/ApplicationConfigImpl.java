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
package org.openecomp.core.utilities.applicationconfig.impl;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import java.util.*;

import org.openecomp.core.nosqldb.impl.cassandra.CassandraSessionFactory;
import org.openecomp.core.utilities.applicationconfig.ApplicationConfig;
import org.openecomp.core.utilities.applicationconfig.dao.type.ApplicationConfigEntity;
import org.openecomp.core.utilities.applicationconfig.type.ConfigurationData;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class ApplicationConfigImpl implements ApplicationConfig {

    private final CqlSession session;
    private final PreparedStatement insertStmt;
    private final PreparedStatement listStmt;
    private final PreparedStatement valueTimestampStmt;
    private final PreparedStatement configDataStmt;

    private static final String CONFIGURATION_SEARCH_ERROR = "CONFIGURATION_NOT_FOUND";
    private static final String CONFIGURATION_SEARCH_ERROR_MSG =
        "Configuration for namespace %s and key %s was not found";

    public ApplicationConfigImpl(CqlSession session) {
        this.session = session;

        this.insertStmt = session.prepare(
            "INSERT INTO application_config (namespace, key, value) VALUES (?, ?, ?)"
        );
        this.listStmt = session.prepare(
            "SELECT namespace, key, value FROM application_config WHERE namespace = ?"
        );
        this.valueTimestampStmt = session.prepare(
            "SELECT writetime(value) as ts FROM application_config WHERE namespace = ? AND key = ?"
        );
        this.configDataStmt = session.prepare(
            "SELECT value, writetime(value) as ts FROM application_config WHERE namespace = ? AND key = ?"
        );
    }

   public ApplicationConfigImpl() {
    this(CassandraSessionFactory.getSession());
}

    @Override
    public ConfigurationData getConfigurationData(String namespace, String key) {
        Row row = session.execute(configDataStmt.bind(namespace, key)).one();
        if (row == null) {
            throw new CoreException(
                new ErrorCode.ErrorCodeBuilder()
                    .withCategory(ErrorCategory.APPLICATION)
                    .withId(CONFIGURATION_SEARCH_ERROR)
                    .withMessage(String.format(CONFIGURATION_SEARCH_ERROR_MSG, namespace, key))
                    .build()
            );
        }
        String value = row.getString("value");
        long ts = row.getLong("ts");
        return new ConfigurationData(value, ts);
    }

    @Override
    public void insertValue(String namespace, String key, String value) {
        session.execute(insertStmt.bind(namespace, key, value));
    }

    @Override
    public Collection<ApplicationConfigEntity> getListOfConfigurationByNamespace(String namespace) {
        List<ApplicationConfigEntity> results = new ArrayList<>();
        ResultSet rs = session.execute(listStmt.bind(namespace));
        for (Row row : rs) {
            results.add(new ApplicationConfigEntity(
                row.getString("namespace"),
                row.getString("key"),
                row.getString("value")
            ));
        }
        return results;
    }

    // optional helper if you still need timestamp retrieval elsewhere
    public long getValueTimestamp(String namespace, String key) {
        Row row = session.execute(valueTimestampStmt.bind(namespace, key)).one();
        return row != null ? row.getLong("ts") : 0L;
    }
}
