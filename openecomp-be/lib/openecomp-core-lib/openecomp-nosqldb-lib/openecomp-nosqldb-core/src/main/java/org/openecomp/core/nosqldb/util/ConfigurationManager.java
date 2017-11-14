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

import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.services.YamlUtil;

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

  private static final String CASSANDRA_KEY = "cassandraConfig";
  private static final String DEFAULT_KEYSPACE_NAME = "dox";
  private static final String CASSANDRA_ADDRESSES = "cassandra.addresses";
  private static final String CASSANDRA_DOX_KEY_STORE = "cassandra.dox.keystore";
  private static final String CASSANDRA_AUTHENTICATE = "cassandra.authenticate";
  private static final String CASSANDRA_USER = "cassandra.user";
  private static final String CASSANDRA_PASSWORD = "cassandra.password";
  private static final String CASSANDRA_PORT = "cassandra.port";
  private static final String CASSANDRA_SSL = "cassandra.ssl";
  private static final String CASSANDRA_TRUSTSTORE = "cassandra.Truststore";
  private static final String CASSANDRA_TRUSTSTORE_PASSWORD = "cassandra.TruststorePassword";
  private static final String CASSANDRA_HOSTS_KEY = "cassandraHosts";
  private static final String CASSANDRA_PORT_KEY = "port";
  private static final String CASSANDRA_USERNAME_KEY = "username";
  private static final String CASSANDRA_PASSWORD_KEY = "password";
  private static final String CASSANDRA_AUTHENTICATE_KEY = "authenticate";
  private static final String CASSANDRA_SSL_KEY = "ssl";
  private static final String CASSANDRA_TRUSTSTORE_PATH_KEY = "truststorePath";
  private static final String CASSANDRA_TRUSTSTORE_PASSWORD_KEY = "truststorePassword";
  private static final String CONSISTENCY_LEVEL = "cassandra.consistencyLevel";
  private static final String CONSISTENCY_LEVEL_KEY = "consistencyLevel";
  private static final String LOCAL_DATA_CENTER_KEY = "localDataCenter";
  private static final String LOCAL_DATA_CENTER = "cassandra.localDataCenter";
  private static ConfigurationManager instance = null;
  private final LinkedHashMap<String, Object> cassandraConfiguration;

  private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());


  private ConfigurationManager() {

    String configurationYamlFile = System.getProperty(CONFIGURATION_YAML_FILE);

    Function<InputStream, Map<String, LinkedHashMap<String, Object>>> reader = (is) -> {
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
    if (Objects.isNull(addresses)) {
      return addresses.split(",");
    }
    List lsAddresses = (ArrayList) cassandraConfiguration.get(CASSANDRA_HOSTS_KEY);
    String[] addressesArray;
    addressesArray = (String[]) lsAddresses.toArray(new String[lsAddresses.size()]);
    return addressesArray;

  }

  /**
   * Gets key space.
   *
   * @return the key space
   */
  public String getKeySpace() {
    String keySpace = System.getProperty(CASSANDRA_DOX_KEY_STORE);
    if (Objects.isNull(keySpace)) {
      //keySpace = cassandraConfiguration.get(cassandraKeySpaceKey);
      //if (keySpace == null)
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
   * Gets ssl port.
   *
   * @return the ssl port
   */
  public int getSslPort() {
    int port;
    String sslPort = System.getProperty(CASSANDRA_PORT);
    if (Objects.isNull(sslPort)) {
      sslPort = (String) cassandraConfiguration.get(CASSANDRA_PORT_KEY);
      if (Objects.isNull(sslPort)) {
        sslPort = "0";
      }
    }
    port = Integer.valueOf(sslPort);
    return port;
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
