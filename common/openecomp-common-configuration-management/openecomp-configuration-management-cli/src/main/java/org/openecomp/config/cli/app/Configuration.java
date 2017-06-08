package org.openecomp.config.cli.app;

import org.openecomp.config.api.ConfigurationChangeListener;
import org.openecomp.config.api.ConfigurationManager;

import java.util.HashMap;
import java.util.Map;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * The type Configuration.
 */
public class Configuration {

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   * @throws Exception the exception
   */
  public static void main(String[] args) throws Exception {

    String host = getValueFor("host", args);
    String port = getValueFor("port", args);

    if (host == null) {
      host = "127.0.0.1";
    }
    if (port == null) {
      port = "9999";
    }
    JMXServiceURL url =
        new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
    Map<String, String[]> env = new HashMap<>();
    //populate dummy value. need to be changed as per impl.
    String[] credentials = {"principal", "password"};
    env.put(JMXConnector.CREDENTIALS, credentials);

    try (JMXConnector jmxc = JMXConnectorFactory.connect(url, env)) {

      boolean isUpdate = isKeyPresent("update", args);

      Map<String, Object> input = new HashMap<>();
      input.put("ImplClass", isUpdate ? "org.openecomp.config.type.ConfigurationUpdate"
          : "org.openecomp.config.type.ConfigurationQuery");
      input.put("externalLookup", isKeyPresent("lookup", args));
      input.put("nodeOverride", isKeyPresent("nodeOverride", args));
      input.put("nodeSpecific", isKeyPresent("nodeSpecific", args));
      input.put("fallback", isKeyPresent("fallback", args));
      input.put("latest", isKeyPresent("latest", args));
      input.put("tenant", getValueFor("tenant", args));
      input.put("namespace", getValueFor("namespace", args));
      input.put("key", getValueFor("key", args));
      input.put("value", getValueFor("value", args));


      if (!isKeyPresent("list", args) && getValueFor("key", args) == null) {
        throw new RuntimeException("Key is missing.");
      }
      if (isKeyPresent("update", args) && getValueFor("value", args) == null) {
        throw new RuntimeException("Value is missing.");
      }


      MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
      ObjectName mbeanName = new ObjectName("org.openecomp.jmx:name=SystemConfig");
      org.openecomp.config.api.ConfigurationManager conf =
          JMX.newMBeanProxy(mbsc, mbeanName, org.openecomp.config.api.ConfigurationManager.class,
              true);

      boolean isGet = isKeyPresent("get", args);
      boolean isList = isKeyPresent("list", args);
      if (isGet) {
        System.out.println(conf.getConfigurationValue(input));
      } else if (isList) {
        Map<String, String> map = conf.listConfiguration(input);
        for (Map.Entry<String, String> entry : map.entrySet()) {
          System.out.println(entry.getKey() + " : " + entry.getValue());
        }
      } else if (isUpdate) {
        conf.updateConfigurationValue(input);
      }
    }
  }

  private static boolean isSwitch(String key) {
    return key.startsWith("-");
  }

  private static String getValueFor(String key, String[] args) {
    for (int i = 0; i < args.length; i++) {
      if (isSwitch(args[i])) {
        String node = args[i].substring(1);
        if (node.equalsIgnoreCase(key) && i < args.length - 1) {
          return args[i + 1].trim().length() == 0 ? null : args[i + 1].trim();
        }
      }
    }
    return null;
  }

  private static boolean isKeyPresent(String key, String[] args) {
    for (int i = 0; i < args.length; i++) {
      if (isSwitch(args[i])) {
        String node = args[i].substring(1);
        if (node.equalsIgnoreCase(key)) {
          return true;
        }
      }
    }
    return false;
  }
}
