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


import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.health.HealthCheckDao;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

public class HealthCheckDaoImpl implements HealthCheckDao {

    private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();

    private static final CheckHealthAccessor accessor =
            noSqlDb.getMappingManager().createAccessor(CheckHealthAccessor.class);
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckDaoImpl.class);

    @Override
    public boolean checkHealth() throws Exception {
        try {
            ResultSet resultSet = accessor.checkHealth();
            return resultSet.getColumnDefinitions().contains("key");
        } catch (DriverException ex) {
            logger.error("Health check failure" + ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("Health check failure" + ex.getMessage(), ex);
            throw new Exception("Internal Error.");
        }
    }

    @Override
    public String getVersion() {
        return noSqlDb.getVersion();
    }

    @Accessor
    interface CheckHealthAccessor {

        @Query("SELECT * FROM application_config LIMIT 1")
        ResultSet checkHealth();

    }

}
