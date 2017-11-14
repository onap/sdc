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

  private static String DATA_CENTER_PROPERTY_NAME = "cassandra.datacenter";;
  private static String CONSISTENCY_LEVEL_PROPERTY_NAME = "cassandra.consistency.level";

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    setCassandraConnectionPropertiesToSystem();
  }

  public static void setCassandraConnectionPropertiesToSystem() {
    if (!System.getProperties().containsKey("cassandra.nodes")) {
      System.setProperty("cassandra.nodes", StringUtils.join(CassandraUtils.getAddresses(), ','));
    }
    if (!System.getProperties().containsKey("cassandra.authenticate")) {
      System
          .setProperty("cassandra.authenticate",
              CassandraUtils.isAuthenticate() ? "true" : "false");
    }
    if (!System.getProperties().containsKey("cassandra.ssl")) {
      System.setProperty("cassandra.ssl",
          CassandraUtils.isSsl() ? "true" : "false");
    }
    if (!System.getProperties().containsKey("cassandra.truststore")) {
      System.setProperty("cassandra.truststore", CassandraUtils.getTruststore());
    }
    if (!System.getProperties().containsKey("cassandra.truststore.password")) {
      System.setProperty("cassandra.truststore.password", CassandraUtils.getTruststorePassword());
    }
    if (!System.getProperties().containsKey("cassandra.user")) {
      System.setProperty("cassandra.user", CassandraUtils.getUser());
    }
    if (!System.getProperties().containsKey("cassandra.password")) {
      System.setProperty("cassandra.password", CassandraUtils.getPassword());
    }
    if (!System.getProperties().containsKey("cassandra.keyspace")) {
      System.setProperty("cassandra.keyspace", "zusammen");
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
