package org.openecomp.config.impl;

import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.config.ConfigurationUtils;
import org.openecomp.config.Constants;

import java.sql.Driver;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * The type Configuration data source.
 */
public final class ConfigurationDataSource {

  private static BasicDataSource configDBDatasource = null;
  private static Set<String> validCallers = Collections.unmodifiableSet(new HashSet<>(
      Arrays.asList(ConfigurationUtils.class.getName(), CliConfigurationImpl.class.getName())));

  static {
    try {
      configDBDatasource = initDataSource();
    } catch (Exception exception) {
      System.err.println("Datasource initialization error. Configuration management will be using in-memory persistence.");
    }
  }

  /**
   * Lookup basic data source.
   *
   * @return the basic data source
   * @throws Exception the exception
   */
  public static BasicDataSource lookup() throws Exception {
    if (validCallers.contains(Thread.currentThread().getStackTrace()[2].getClassName())) {
      return configDBDatasource;
    } else {
      return null;
    }
  }

  /**
   * Init data source basic data source.
   *
   * @return the basic data source
   * @throws Exception the exception
   */
  public static BasicDataSource initDataSource() throws Exception {
    ImmutableConfiguration dbConfig = ConfigurationRepository.lookup()
        .getConfigurationFor(Constants.DEFAULT_TENANT, Constants.DB_NAMESPACE);
    if (StringUtils.isEmpty(dbConfig.getString("dbhost"))) {
      return null;
    }
    BasicDataSource datasource = new BasicDataSource();
    String driverClassName = dbConfig.getString("driverClassName");
    String jdbcUrl = dbConfig.getString("jdbcURL");
    if (!isDriverSuitable(driverClassName, jdbcUrl)) {
      driverClassName = getDriverFor(jdbcUrl);
    }
    datasource.setDriverClassName(driverClassName);
    datasource.setUrl(jdbcUrl);
    String dbuser = dbConfig.getString("dbuser");
    String dbpassword = dbConfig.getString("dbpassword");
    if (dbuser != null && dbuser.trim().length() > 0) {
      datasource.setUsername(dbuser);
    }
    if (dbpassword != null && dbpassword.trim().length() > 0) {
      datasource.setPassword(dbpassword);
    }
    return datasource;
  }

  private static boolean isDriverSuitable(String driverClassName, String url) {
    if (driverClassName == null || driverClassName.trim().length() == 0) {
      return false;
    }
    try {
      Driver driver = Driver.class.cast(Class.forName(driverClassName).newInstance());
      return driver.acceptsURL(url);
    } catch (Exception exception) {
      exception.printStackTrace();
      return false;
    }
  }

  private static String getDriverFor(String url) throws Exception {
    ServiceLoader<Driver> loader = ServiceLoader.load(Driver.class);
    for (Driver driver : loader) {
      if (driver.acceptsURL(url)) {
        return driver.getClass().getName();
      }
    }
    throw new RuntimeException("No Suitable driver found for " + url);
  }

}
