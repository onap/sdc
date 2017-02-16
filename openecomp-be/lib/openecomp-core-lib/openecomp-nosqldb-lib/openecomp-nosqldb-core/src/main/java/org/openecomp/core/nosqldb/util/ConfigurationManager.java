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

import org.openecomp.core.utilities.yaml.YamlUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Configuration manager.
 */
public class ConfigurationManager {

  private static final String CONFIGURATION_YAML_FILE = "configuration.yaml";
  private static final String cassandraKey = "cassandraConfig";
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
  private static final String cassandraHostsKey = "cassandraHosts";
  private static final String cassandraPortKey = "port";
  private static final String cassandraUsernameKey = "username";
  private static final String cassandraPasswordKey = "password";
  private static final String cassandraAuthenticateKey = "authenticate";
  private static final String cassandraSSLKey = "ssl";
  private static final String cassandraTruststorePathKey = "truststorePath";
  private static final String cassandraTruststorePasswordKey = "truststorePassword";
  private static ConfigurationManager instance = null;
  private final LinkedHashMap<String, Object> cassandraConfiguration;


  private ConfigurationManager() {
    YamlUtil yamlUtil = new YamlUtil();
    String configurationYamlFile = System.getProperty(CONFIGURATION_YAML_FILE);
    InputStream yamlAsString;
    if (configurationYamlFile != null) {
      yamlAsString = getConfigFileIs(configurationYamlFile);
    } else {
      //Load from resources
      yamlAsString = yamlUtil.loadYamlFileIs("/" + CONFIGURATION_YAML_FILE);
    }
    Map<String, LinkedHashMap<String, Object>> configurationMap = yamlUtil.yamlToMap(yamlAsString);
    cassandraConfiguration = configurationMap.get(cassandraKey);

  }

  /**
   * Gets instance.
   *
   * @return the instance
   */
  public static ConfigurationManager getInstance() {
    if (instance == null) {
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
    if (addresses != null) {
      return addresses.split(",");
    }
    List locAddresses = (ArrayList) cassandraConfiguration.get(cassandraHostsKey);
    String[] addressesArray;
    addressesArray = (String[]) locAddresses.toArray(new String[locAddresses.size()]);
    return addressesArray;

  }

  /**
   * Gets key space.
   *
   * @return the key space
   */
  public String getKeySpace() {
    String keySpace = System.getProperty(CASSANDRA_DOX_KEY_STORE);
    if (keySpace == null) {
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
    if (username == null) {
      username = (String) cassandraConfiguration.get(cassandraUsernameKey);
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
    if (password == null) {
      password = (String) cassandraConfiguration.get(cassandraPasswordKey);
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
    if (truststorePath == null) {
      truststorePath = (String) cassandraConfiguration.get(cassandraTruststorePathKey);
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
    if (truststorePassword == null) {
      truststorePassword = (String) cassandraConfiguration.get(cassandraTruststorePasswordKey);
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
    if (sslPort == null) {
      sslPort = (String) cassandraConfiguration.get(cassandraPortKey);
      if (sslPort == null) {
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
    return getBooleanResult(CASSANDRA_SSL, cassandraSSLKey);
  }

  /**
   * Is authenticate boolean.
   *
   * @return the boolean
   */
  public boolean isAuthenticate() {
    return getBooleanResult(CASSANDRA_AUTHENTICATE, cassandraAuthenticateKey);
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

  private InputStream getConfigFileIs(String file) {
    InputStream is = null;
    try {
      is = new FileInputStream(file);
    } catch (FileNotFoundException e0) {
      e0.printStackTrace();
    }
    return is;
  }
}
