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
package org.openecomp.sdc.health.impl;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import org.openecomp.sdc.health.HealthCheckDao;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

public class HealthCheckDaoImpl implements HealthCheckDao {

    private final CqlSession session;
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckDaoImpl.class);

    public HealthCheckDaoImpl(CqlSession session) {
        this.session = session;
    }

    @Override
public boolean checkHealth() throws Exception {
    try {
        SimpleStatement stmt = SimpleStatement.builder("SELECT * FROM application_config LIMIT 1").build();
        ResultSet resultSet = session.execute(stmt);

        Row firstRow = resultSet.one();
        return firstRow != null && firstRow.getColumnDefinitions().contains("key");

    } catch (DriverException ex) {
        logger.error("Health check failure: " + ex.getMessage(), ex);
        throw ex;
    } catch (Exception ex) {
        logger.error("Health check failure: " + ex.getMessage(), ex);
        throw new Exception("Internal Error.");
    }
}

    @Override
    public String getVersion() {
        return "Cassandra Driver 4.17";
    }
}
