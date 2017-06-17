package org.openecomp.core.tools.util;

import org.openecomp.sdc.logging.api.Logger;

/**
 * @author Avrahamg
 * @since April 24, 2017
 */
public class Utils {
  public static void printMessage(Logger logger, String message) {
    System.out.println(message);
    logger.debug(message);
  }
}

