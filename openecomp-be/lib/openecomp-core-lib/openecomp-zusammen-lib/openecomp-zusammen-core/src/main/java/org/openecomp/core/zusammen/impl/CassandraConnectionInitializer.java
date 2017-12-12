/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.core.zusammen.impl;


import org.apache.commons.lang3.StringUtils;
import org.openecomp.core.nosqldb.util.CassandraUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Objects;

public class CassandraConnectionInitializer implements ServletContextListener {

  private static final String DATA_CENTER_PROPERTY_NAME = "cassandra.datacenter";
  private static final String CONSISTENCY_LEVEL_PROPERTY_NAME = "cassandra.consistency.level";
  private static final String NODES_PROPERTY_NAME = "cassandra.nodes";
  private static final String AUTHENTICATE_PROPERTY_NAME = "cassandra.authenticate";
  private static final String TRUE = "true";
  private static final String FALSE = "false";
  private static final String SSL_PROPERTY_NAME = "cassandra.ssl";
  private static final String TRUSTSTORE_PROPERTY_NAME = "cassandra.truststore";
  private static final String TRUSTSTORE_PASSWRD_PROPERTY_NAME = "cassandra.truststore.password";
  private static final String USER_PROPERTY_NAME = "cassandra.user";
  private static final String PASSWRD_PROPERTY_NAME = "cassandra.password";
  private static final String KEYSPACE_PROPERTY_NAME = "cassandra.keyspace";
  private static final String ZUSAMMEN = "zusammen";

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    setCassandraConnectionPropertiesToSystem();
  }

  public static void setCassandraConnectionPropertiesToSystem() {

    setNodeProperty();
    setAuthAndSSLProperty();
    setTruststoreProperty();
    setUserPasswrdProperty();
    setKeyspaceProperty();
    setDataCenterAndConsistencyLevelProperty();
  }

  private static void setNodeProperty() {
    if (!System.getProperties().containsKey(NODES_PROPERTY_NAME)) {
      System.setProperty(NODES_PROPERTY_NAME, StringUtils.join(CassandraUtils.getAddresses(),
          ','));
    }
  }

  private static void setAuthAndSSLProperty() {
    if (!System.getProperties().containsKey(AUTHENTICATE_PROPERTY_NAME)) {
      System.setProperty(AUTHENTICATE_PROPERTY_NAME,
          CassandraUtils.isAuthenticate() ? TRUE : FALSE);
    }
    if (!System.getProperties().containsKey(SSL_PROPERTY_NAME)) {
      System.setProperty(SSL_PROPERTY_NAME,
          CassandraUtils.isSsl() ? TRUE : FALSE);
    }
  }

  private static void setTruststoreProperty() {
    if (!System.getProperties().containsKey(TRUSTSTORE_PROPERTY_NAME)) {
      System.setProperty(TRUSTSTORE_PROPERTY_NAME, CassandraUtils.getTruststore());
    }

    if (!System.getProperties().containsKey(TRUSTSTORE_PASSWRD_PROPERTY_NAME)) {
      System.setProperty(TRUSTSTORE_PASSWRD_PROPERTY_NAME, CassandraUtils.getTruststorePassword());
    }
  }

  private static void setUserPasswrdProperty() {
    if (!System.getProperties().containsKey(USER_PROPERTY_NAME)) {
      System.setProperty(USER_PROPERTY_NAME, CassandraUtils.getUser());
    }

    if (!System.getProperties().containsKey(PASSWRD_PROPERTY_NAME)) {
      System.setProperty(PASSWRD_PROPERTY_NAME, CassandraUtils.getPassword());
    }
  }

  private static void setKeyspaceProperty() {
    if (!System.getProperties().containsKey(KEYSPACE_PROPERTY_NAME)) {
      System.setProperty(KEYSPACE_PROPERTY_NAME, ZUSAMMEN);
    }
  }

  private static void setDataCenterAndConsistencyLevelProperty() {
    if (!System.getProperties().containsKey(DATA_CENTER_PROPERTY_NAME)) {
      String dataCenter = CassandraUtils.getLocalDataCenter();
      if (Objects.nonNull(dataCenter)) {
        System.setProperty(DATA_CENTER_PROPERTY_NAME, dataCenter);
      }
    }

    if (!System.getProperties().containsKey(CONSISTENCY_LEVEL_PROPERTY_NAME)) {
      String consistencyLevel = CassandraUtils.getConsistencyLevel();
      if (Objects.nonNull(consistencyLevel)) {
        System.setProperty(CONSISTENCY_LEVEL_PROPERTY_NAME, consistencyLevel);
      }
    }
  }

  // -Dcassandra.nodes=10.147.97.145  -Dcassandra.keyspace=zusammen -Dcassandra.authenticate=true -Dcassandra.ssl=true
  // -Dcassandra.truststore=/apps/jetty/base/be/config/.truststore -Dcassandra.truststore.password=Aa123456
  // -Dcassandra.user=asdc_user -Dcassandra.password=Aa1234%^!

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    //Should provide implementation when in case something specific to class need to be handled
  }
}
