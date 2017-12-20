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

package org.openecomp.core.nosqldb.impl.cassandra;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.util.CassandraUtils;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

class CassandraNoSqlDbImpl implements NoSqlDb {

    private final Session session;
    private final String keySpace;
    private final MappingManager mappingManager;

    private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());


    public CassandraNoSqlDbImpl(Session session) {
        this.session = session;
        this.keySpace = this.session.getLoggedKeyspace();
        this.mappingManager = new MappingManager(this.session);

    }

    @Override
    public void insert(String tableName, String[] colNames, Object[] values) {
        if (colNames.length != values.length) {
            throw new CoreException((new ErrorCode.ErrorCodeBuilder()).withMessage(
                    "number of colmuns[" + colNames.length + "] is not equal to the number of values["
                            + values.length + "].").withId("E0005").withCategory(ErrorCategory.APPLICATION)
                    .build());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("insert into ")
                .append(tableName)
                .append(" (")
                .append(CommonMethods.arrayToCommaSeparatedString(colNames))
                .append(") values (")
                .append(CommonMethods.duplicateStringWithDelimiter("?", ',', values.length))
                .append(")");
        System.out.println(sb.toString());
        PreparedStatement prepared = session.prepare(sb.toString());

        BoundStatement bound;
        bound = prepared.bind(values);
        session.execute(bound);

    }

    @Override
    public ResultSet execute(String statement) {
        return session.execute(statement);
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

    @Override
    public MappingManager getMappingManager() {
        return mappingManager;
    }

    @Override
    public String getVersion() {
        try {
            Set<Host> allHosts = this.session.getCluster().getMetadata().getAllHosts();
            Set<String> versions = allHosts.stream().map(host -> host.getCassandraVersion().toString())
                    .collect(Collectors.toSet());
            return versions.stream().collect(Collectors.joining(","));
        } catch (Exception e){
            log.debug("",e);
            return "Failed to retrieve version";
        }
    }
}
