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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.common.CommonConfigurationManager;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

/**
 * The type Configuration manager.
 */
public class CassandraConfigurationManager extends CommonConfigurationManager {

    private static final String CASSANDRA = "cassandra";
    private static final String CASSANDRA_KEY = CASSANDRA + "Config";
    private static final String CASSANDRA_ADDRESSES = CASSANDRA + ".addresses";
    private static final String CASSANDRA_DOX_KEY_STORE = CASSANDRA + ".dox.keystore";
    private static final String CASSANDRA_AUTHENTICATE = CASSANDRA + ".authenticate";
    private static final String CASSANDRA_USER = CASSANDRA + ".user";
    private static final String CASSANDRA_PASSWORD = CASSANDRA + ".password";
    private static final String CASSANDRA_SSL = CASSANDRA + ".ssl";
    private static final String CASSANDRA_TRUSTSTORE = CASSANDRA + ".Truststore";
    private static final String CASSANDRA_TRUSTSTORE_PASSWORD = CASSANDRA + ".TruststorePassword";
    private static final String CASSANDRA_HOSTS_KEY = CASSANDRA + "Hosts";
    private static final String CASSANDRA_PORT_KEY = "cassandraPort";
    private static final String CASSANDRA_PORT = CASSANDRA + ".cassandraPort";
    private static final String CASSANDRA_USERNAME_KEY = "username";
    private static final String CASSANDRA_RECONNECT_TIMEOUT = "reconnectTimeout";
    @SuppressWarnings("squid:S2068")
    private static final String CASSANDRA_PASSWORD_KEY = "password";
    private static final String CASSANDRA_AUTHENTICATE_KEY = "authenticate";
    private static final String CASSANDRA_SSL_KEY = "ssl";
    private static final String CASSANDRA_TRUSTSTORE_PATH_KEY = "truststorePath";
    @SuppressWarnings("squid:S2068")
    private static final String CASSANDRA_TRUSTSTORE_PASSWORD_KEY = "truststorePassword";
    private static final String CONSISTENCY_LEVEL = CASSANDRA + ".consistencyLevel";
    private static final String CONSISTENCY_LEVEL_KEY = "consistencyLevel";
    private static final String DEFAULT_KEYSPACE_NAME = "dox";
    private static final String CREATE_KEYSPACE_IF_NOT_EXISTS_KEY = "createKeyspaceIfNotExists";
    private static final String CREATE_KEYSPACE_IF_NOT_EXISTS = CASSANDRA + ".createKeyspaceIfNotExists";
    private static final Integer DEFAULT_CASSANDRA_PORT = 9042;
    private static final Integer DEFAULT_REPLICATION_FACTOR = 3;
    private static final String LOCAL_DATA_CENTER_KEY = "localDataCenter";
    private static final String LOCAL_DATA_CENTER = CASSANDRA + ".localDataCenter";
    private static final String REPLICATION_FACTOR_KEY = "replicationFactor";
    private static final String REPLICATION_FACTOR = CASSANDRA + ".replicationFactor";
    private static final Logger LOG = LoggerFactory.getLogger(CassandraConfigurationManager.class);

    private static CassandraConfigurationManager singletonInstance;

    private CassandraConfigurationManager() {
        super(CASSANDRA_KEY);
    }

    public static synchronized CassandraConfigurationManager getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new CassandraConfigurationManager();
        }
        return singletonInstance;
    }

    /**
     * Get addresses string [ ].
     *
     * @return the string [ ]
     */
    public String[] getAddresses() {
        String addresses = System.getProperty(CASSANDRA_ADDRESSES);
        if (Objects.nonNull(addresses)) {
            return addresses.split(",");
        }
        List<String> lsAddresses = this.getConfigValue(CASSANDRA_HOSTS_KEY, Collections.emptyList());
        if (CollectionUtils.isEmpty(lsAddresses)) {
            LOG.info("No Cassandra hosts are defined.");
        }
        String[] addressesArray;
        addressesArray = lsAddresses.toArray(new String[lsAddresses.size()]);
        return addressesArray;
    }

    /**
     * Gets Cassandra port.
     *
     * @return the port
     */
    public int getCassandraPort() {
        String cassandraPort = System.getProperty(CASSANDRA_PORT);
        if (Objects.isNull(cassandraPort)) {
            cassandraPort = this.getConfigValue(CASSANDRA_PORT_KEY, String.valueOf(DEFAULT_CASSANDRA_PORT));
        }
        return Integer.parseInt(cassandraPort);
    }

    /**
     * Gets Cassandra reconnection timeout
     *
     * @return
     */
    public Long getReconnectTimeout() {
        Integer cassandraReconnectTimeout = this.getConfigValue(CASSANDRA_RECONNECT_TIMEOUT, null);
        if (Objects.isNull(cassandraReconnectTimeout)) {
            LOG.info("No Cassandra reconnect timeout are defined.");
            return null;
        }
        return cassandraReconnectTimeout.longValue();
    }

    /**
     * Gets key space.
     *
     * @return the key space
     */
    public String getKeySpace() {
        String keySpace = System.getProperty(CASSANDRA_DOX_KEY_STORE);
        if (Objects.isNull(keySpace)) {
            keySpace = DEFAULT_KEYSPACE_NAME;
        }
        return keySpace;
    }

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        String username = System.getProperty(CASSANDRA_USER);
        if (Objects.isNull(username)) {
            username = this.getConfigValue(CASSANDRA_USERNAME_KEY, null);
        }
        return username;
    }

    /**
     * Gets password.
     *
     * @return the password
     */
    public String getPassword() {
        String password = System.getProperty(CASSANDRA_PASSWORD);
        if (Objects.isNull(password)) {
            password = this.getConfigValue(CASSANDRA_PASSWORD_KEY, null);
        }
        return password;
    }

    /**
     * Gets truststore path.
     *
     * @return the truststore path
     */
    public String getTruststorePath() {
        String truststorePath = System.getProperty(CASSANDRA_TRUSTSTORE);
        if (Objects.isNull(truststorePath)) {
            truststorePath = this.getConfigValue(CASSANDRA_TRUSTSTORE_PATH_KEY, null);
        }
        return truststorePath;
    }

    /**
     * Gets truststore password.
     *
     * @return the truststore password
     */
    public String getTruststorePassword() {
        String truststorePassword = System.getProperty(CASSANDRA_TRUSTSTORE_PASSWORD);
        if (Objects.isNull(truststorePassword)) {
            truststorePassword = this.getConfigValue(CASSANDRA_TRUSTSTORE_PASSWORD_KEY, null);
        }
        return truststorePassword;
    }

    /**
     * Is ssl boolean.
     *
     * @return the boolean
     */
    public boolean isSsl() {
        return getBooleanResult(CASSANDRA_SSL, CASSANDRA_SSL_KEY);
    }

    /**
     * Is authenticate boolean.
     *
     * @return the boolean
     */
    public boolean isAuthenticate() {
        return getBooleanResult(CASSANDRA_AUTHENTICATE, CASSANDRA_AUTHENTICATE_KEY);
    }

    private Boolean getBooleanResult(String property, String key) {
        String value = System.getProperty(property);
        return Boolean.valueOf(value == null ? String.valueOf(this.getConfigValue(key, false)) : value);
    }

    public String getConsistencyLevel() {
        String consistencyLevel = System.getProperty(CONSISTENCY_LEVEL);
        if (Objects.isNull(consistencyLevel)) {
            consistencyLevel = this.getConfigValue(CONSISTENCY_LEVEL_KEY, null);
        }
        if (Objects.isNull(consistencyLevel)) {
            consistencyLevel = "LOCAL_QUORUM";
        }
        return consistencyLevel;
    }

    public String getLocalDataCenter() {
        String localDataCenter = System.getProperty(LOCAL_DATA_CENTER);
        if (Objects.isNull(localDataCenter)) {
            localDataCenter = this.getConfigValue(LOCAL_DATA_CENTER_KEY, null);
        }
        return localDataCenter;
    }


    public boolean createKeyspaceIfNotExists() {
        return getBooleanResult(CREATE_KEYSPACE_IF_NOT_EXISTS, CREATE_KEYSPACE_IF_NOT_EXISTS_KEY);
    }
    public int getReplicationFactor() {
        String replicationFactor = System.getProperty(REPLICATION_FACTOR);
        if (Objects.isNull(replicationFactor)) {
            replicationFactor = this.getConfigValue(REPLICATION_FACTOR_KEY, String.valueOf(DEFAULT_REPLICATION_FACTOR));
        }
        return Integer.parseInt(replicationFactor);
    }
}
