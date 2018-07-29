package org.onap.config.impl;

import org.onap.config.api.ConfigurationManager;

import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.lang.management.ManagementFactory;

import static org.onap.config.Constants.MBEAN_NAME;

@WebListener
public class ContextListener implements ServletContextListener {

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    try {
      ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(MBEAN_NAME));
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  @Override
  public void contextInitialized(ServletContextEvent arg0) {
    ConfigurationManager.lookup();
  }
}
