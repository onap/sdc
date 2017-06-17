package org.openecomp.core.tools.util;

public class ToolsUtil {

  public static String getParam(String key, String[] args) {

    for (int j = 0; j < args.length; j++) {
      if (args[j].equals("-" + key)) {
        return args[j + 1];
      }
    }
    return null;
  }
}
