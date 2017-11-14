package org.openecomp.core.zusammen.impl;


import org.apache.commons.lang3.StringUtils;
import org.openecomp.core.nosqldb.util.CassandraUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Avrahamg
 * @since April 25, 2017
 */

public class CassandraConnectionInitializer implements ServletContextListener {

  private static String DATA_CENTER_PROPERTY_NAME = "cassandra.datacenter";
  private static String CONSISTENCY_LEVEL_PROPERTY_NAME = "cassandra.consistency.level";
  private static String NODES_PROPERTY_NAME = "cassandra.nodes";
  private static String AUTHENTICATE_PROPERTY_NAME = "cassandra.authenticate";
  private static String TRUE = "true";
  private static String FALSE = "false";
  private static String SSL_PROPERTY_NAME = "cassandra.ssl";
  private static String TRUSTSTORE_PROPERTY_NAME = "cassandra.truststore";
  private static String TRUSTSTORE_PASSWORD_PROPERTY_NAME = "cassandra.truststore.password";
  private static String USER_PROPERTY_NAME = "cassandra.user";
  private static String PASSWORD_PROPERTY_NAME = "cassandra.password";
  private static String KEYSPACE_PROPERTY_NAME = "cassandra.keyspace";
  private static String ZUSAMMEN = "zusammen";

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    setCassandraConnectionPropertiesToSystem();
  }

  public static void setCassandraConnectionPropertiesToSystem() {

    if (!System.getProperties().containsKey(NODES_PROPERTY_NAME)) {
      System.setProperty(NODES_PROPERTY_NAME, StringUtils.join(CassandraUtils.getAddresses(), ','));
    }

    if (!System.getProperties().containsKey(AUTHENTICATE_PROPERTY_NAME)) {
      System.setProperty(AUTHENTICATE_PROPERTY_NAME,
          CassandraUtils.isAuthenticate() ? TRUE : FALSE);
    }
    if (!System.getProperties().containsKey(SSL_PROPERTY_NAME)) {
      System.setProperty(SSL_PROPERTY_NAME,
          CassandraUtils.isSsl() ? TRUE : FALSE);
    }

    if (!System.getProperties().containsKey(TRUSTSTORE_PROPERTY_NAME)) {
      System.setProperty(TRUSTSTORE_PROPERTY_NAME, CassandraUtils.getTruststore());
    }

    if (!System.getProperties().containsKey(TRUSTSTORE_PASSWORD_PROPERTY_NAME)) {
      System.setProperty(TRUSTSTORE_PASSWORD_PROPERTY_NAME, CassandraUtils.getTruststorePassword());
    }

    if (!System.getProperties().containsKey(USER_PROPERTY_NAME)) {
      System.setProperty(USER_PROPERTY_NAME, CassandraUtils.getUser());
    }

    if (!System.getProperties().containsKey(PASSWORD_PROPERTY_NAME)) {
      System.setProperty(PASSWORD_PROPERTY_NAME, CassandraUtils.getPassword());
    }

    if (!System.getProperties().containsKey(KEYSPACE_PROPERTY_NAME)) {
      System.setProperty(KEYSPACE_PROPERTY_NAME, ZUSAMMEN);
    }


    if (!System.getProperties().containsKey(DATA_CENTER_PROPERTY_NAME)) {
      System.setProperty(DATA_CENTER_PROPERTY_NAME, CassandraUtils.getLocalDataCenter());
    }

    if (!System.getProperties().containsKey(CONSISTENCY_LEVEL_PROPERTY_NAME)) {
      System.setProperty(CONSISTENCY_LEVEL_PROPERTY_NAME, CassandraUtils.getConsistencyLevel());
    }


  }

  //       -Dcassandra.nodes=10.147.97.145  -Dcassandra.keyspace=zusammen -Dcassandra.authenticate=true -Dcassandra.ssl=true
  // -Dcassandra.truststore=/apps/jetty/base/be/config/.truststore -Dcassandra.truststore.password=Aa123456
  // -Dcassandra.user=asdc_user -Dcassandra.password=Aa1234%^!

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {

  }
}
