package org.openecomp.config.impl;

import static org.openecomp.config.Constants.MBEAN_NAME;

import java.lang.management.ManagementFactory;
import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.openecomp.config.api.ConfigurationManager;

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
