/*
 * Copyright © 2018 European Support Limited
 *
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
 */
package org.openecomp.core.nosqldb.impl.cassandra;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.util.CassandraUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.Version;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.metadata.Node;

class CassandraNoSqlDbImpl implements NoSqlDb {

     private final CqlSession session;
    private final String keyspace;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    public CassandraNoSqlDbImpl(CqlSession session) {
        this.session = session;
        this.keyspace = session.getKeyspace().map(Object::toString).orElse(null);
    }

     @Override
    public void insert(String tableName, String[] colNames, Object[] values) {
        if (colNames.length != values.length) {
            throw new IllegalArgumentException(
                    "Number of columns [" + colNames.length + "] does not match number of values [" + values.length + "]."
            );
        }

        String placeholders = String.join(",", java.util.Collections.nCopies(values.length, "?"));
        String columns = String.join(",", colNames);
        String query = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";

        log.info(query);

        PreparedStatement prepared = session.prepare(query);
        BoundStatement bound = prepared.bind(values);
        session.execute(bound);
    }


    @Override
    public ResultSet execute(String statement) {
        return session.execute(SimpleStatement.newInstance(statement));
    }

    @Override
    public ResultSet execute(String statementName, Object... values) {
        String statement = CassandraUtils.getStatement(statementName);
        if (statement == null) {
            statement = statementName;
        }
        if (values != null) {
            PreparedStatement prepared = session.prepare(statement);
            BoundStatement bound;
            bound = prepared.bind(values);
            return session.execute(bound);
        } else {
            return session.execute(statement);
        }
    }

    public CqlSession getSession() {
        return session;
    }

    @Override
    public String getVersion() {
        try {
            Set<String> versions = session.getMetadata()
                    .getNodes()
                    .values()
                    .stream()
                    .map(Node::getCassandraVersion)   // returns Version
                    .map(Version::toString)           // convert to string
                    .collect(Collectors.toSet());

            return String.join(",", versions);
        } catch (Exception e) {
            log.error("Failed to retrieve version", e);
            return "Failed to retrieve version";
        }
    }
}
