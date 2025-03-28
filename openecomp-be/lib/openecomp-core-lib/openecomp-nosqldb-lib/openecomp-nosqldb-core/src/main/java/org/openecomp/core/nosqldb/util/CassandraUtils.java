/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017, 2021 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.core.nosqldb.util;

import java.util.HashMap;
import java.util.Map;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;

public class CassandraUtils {

    private static final String CASSANDRA_STATEMENT_DEFINITION_FILE = "cassandraStatements.json";
    private static Map<String, String> statementMap = new HashMap<>();

    public static String[] getAddresses() {
        return CassandraConfigurationManager.getInstance().getAddresses();
    }

    public static Long getReconnectTimeout() {
        return CassandraConfigurationManager.getInstance().getReconnectTimeout();
    }

    public static String getKeySpace() {
        return CassandraConfigurationManager.getInstance().getKeySpace();
    }

    /**
     * Gets statement.
     *
     * @param statementName the statement name
     * @return the statement
     */
    public static String getStatement(String statementName) {
        if (statementMap.size() == 0) {
            statementMap = FileUtils.readViaInputStream(CASSANDRA_STATEMENT_DEFINITION_FILE, stream -> JsonUtil.json2Object(stream, Map.class));
        }
        return statementMap.get(statementName);
    }

    public static String getUser() {
        return CassandraConfigurationManager.getInstance().getUsername();
    }

    public static String getPassword() {
        return CassandraConfigurationManager.getInstance().getPassword();
    }

    public static String getTruststore() {
        return CassandraConfigurationManager.getInstance().getTruststorePath();
    }

    public static String getTruststorePassword() {
        return CassandraConfigurationManager.getInstance().getTruststorePassword();
    }

    public static int getCassandraPort() {
        return CassandraConfigurationManager.getInstance().getCassandraPort();
    }

    public static boolean isSsl() {
        return CassandraConfigurationManager.getInstance().isSsl();
    }

    public static boolean isAuthenticate() {
        return CassandraConfigurationManager.getInstance().isAuthenticate();
    }

    public static String getConsistencyLevel() {
        return CassandraConfigurationManager.getInstance().getConsistencyLevel();
    }

    public static String getLocalDataCenter() {
        return CassandraConfigurationManager.getInstance().getLocalDataCenter();
    }

    public static boolean createKeyspaceIfNotExists() {
        return CassandraConfigurationManager.getInstance().createKeyspaceIfNotExists();
    }

    public static int getReplicationFactor() {
        return CassandraConfigurationManager.getInstance().getReplicationFactor();
    }
}
