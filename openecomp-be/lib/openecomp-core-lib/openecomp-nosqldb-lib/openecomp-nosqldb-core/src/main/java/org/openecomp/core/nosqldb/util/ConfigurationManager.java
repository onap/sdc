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

package org.openecomp.core.nosqldb.util;

import org.apache.commons.collections4.CollectionUtils;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * The type Configuration manager.
 */
public class ConfigurationManager {

  static final String CONFIGURATION_YAML_FILE = "configuration.yaml";
  static private final Integer DEAFAULT_CASSANDRA_PORT = 9042;
  private static final String CASSANDRA = "cassandra";
  private static final String CASSANDRA_KEY = CASSANDRA + "Config";
  private static final String DEFAULT_KEYSPACE_NAME = "dox";
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
  private static final String LOCAL_DATA_CENTER_KEY = "localDataCenter";
  private static final String LOCAL_DATA_CENTER = CASSANDRA + ".localDataCenter";
  private static ConfigurationManager instance = null;
  private final LinkedHashMap<String, Object> cassandraConfiguration;

  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationManager.class);


  private ConfigurationManager() {

    String configurationYamlFile = System.getProperty(CONFIGURATION_YAML_FILE);

    Function<InputStream, Map<String, LinkedHashMap<String, Object>>> reader = is -> {
      YamlUtil yamlUtil = new YamlUtil();
      return yamlUtil.yamlToMap(is);
    };

    try {

      Map<String, LinkedHashMap<String, Object>> configurationMap = configurationYamlFile != null
          ? readFromFile(configurationYamlFile, reader) // load from file
          : FileUtils.readViaInputStream(CONFIGURATION_YAML_FILE, reader); // or from resource
      cassandraConfiguration = configurationMap.get(CASSANDRA_KEY);

    } catch (IOException e) {
      throw new RuntimeException("Failed to read configuration", e);
    }
  }

  /**
   * Gets instance.
   *
   * @return the instance
   */
  public static ConfigurationManager getInstance() {
    if (Objects.isNull(instance)) {
      instance = new ConfigurationManager();
    }
    return instance;
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
    List lsAddresses = (ArrayList) cassandraConfiguration.get(CASSANDRA_HOSTS_KEY);
    if (CollectionUtils.isEmpty(lsAddresses)) {
      LOG.info("No Cassandra hosts are defined.");
    }

    String[] addressesArray;
    addressesArray = (String[]) lsAddresses.toArray(new String[lsAddresses.size()]);
    return addressesArray;

  }

  /**
   * Gets Cassandra port.
   *
   * @return the port
   */
  public int getCassandraPort() {
    Integer cassandraPort = (Integer) cassandraConfiguration.get(CASSANDRA_PORT_KEY);
    if (Objects.isNull(cassandraPort)) {
        cassandraPort = DEAFAULT_CASSANDRA_PORT;
    }
    return cassandraPort;
  }

  /**
   * Gets Cassandra reconnection timeout
   *
   * @return
   */
  public Long getReconnectTimeout() {
    Integer cassandraReconnectTimeout = (Integer) cassandraConfiguration.get(CASSANDRA_RECONNECT_TIMEOUT);
    if (Objects.isNull(cassandraReconnectTimeout)) {
      LOG.info("No Cassandra reconnect timeout are defined.");
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
      username = (String) cassandraConfiguration.get(CASSANDRA_USERNAME_KEY);
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
      password = (String) cassandraConfiguration.get(CASSANDRA_PASSWORD_KEY);
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
      truststorePath = (String) cassandraConfiguration.get(CASSANDRA_TRUSTSTORE_PATH_KEY);
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
      truststorePassword = (String) cassandraConfiguration.get(CASSANDRA_TRUSTSTORE_PASSWORD_KEY);
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
    Boolean res;
    String value;
    if (System.getProperty(property) == null) {
      value = String.valueOf(cassandraConfiguration.get(key));
    } else {
      value = System.getProperty(property);
    }

    res = Boolean.valueOf(value);

    return res;
  }

  private <T> T readFromFile(String file, Function<InputStream, T> reader) throws IOException {
    try (InputStream is = new FileInputStream(file)) {
      return reader.apply(is);
    }
  }

  public String getConsistencyLevel() {
    String consistencyLevel = System.getProperty(CONSISTENCY_LEVEL);
    if (Objects.isNull(consistencyLevel)) {
      consistencyLevel = (String) cassandraConfiguration.get(CONSISTENCY_LEVEL_KEY);
    }

    if (Objects.isNull(consistencyLevel)) {
      consistencyLevel = "LOCAL_QUORUM";
    }
    return consistencyLevel;
  }

  public String getLocalDataCenter() {
    String localDataCenter = System.getProperty(LOCAL_DATA_CENTER);
    if (Objects.isNull(localDataCenter)) {
      localDataCenter = (String) cassandraConfiguration.get(LOCAL_DATA_CENTER_KEY);
    }
    return localDataCenter;
  }
}
