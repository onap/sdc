package org.openecomp.config.impl;

import static org.openecomp.config.Constants.MBEAN_NAME;

import org.apache.commons.beanutils.FluentPropertyBeanIntrospector;
import org.openecomp.config.api.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

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
    try {
      ConfigurationManager.lookup();
      Logger logger = LoggerFactory.getLogger(FluentPropertyBeanIntrospector.class);
      Method method = logger.getClass().getDeclaredMethod("getLevel");
      method.setAccessible(true);
      Object object = method.invoke(logger);
      method = logger.getClass().getDeclaredMethod("setLevel", object.getClass());
      method.setAccessible(true);
      Field field = object.getClass().getDeclaredField("ERROR");
      field.setAccessible(true);
      method.invoke(logger, field.get(logger));
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

}
