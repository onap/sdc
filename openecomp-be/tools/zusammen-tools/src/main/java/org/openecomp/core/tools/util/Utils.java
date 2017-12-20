package org.openecomp.core.tools.util;

import org.openecomp.sdc.logging.api.Logger;

/**
 * Copyright © 2016-2017 European Support Limited.
 * @author Avrahamg
 * @since April 24, 2017
 * Since it is a command line tools writing to console will be helpful to users.
 */
public class Utils {
  public static void printMessage(Logger logger, String message) {
    /**
     * Since it is a command line tools writing to console will be helpful to users.
     */
    System.out.println(message);
    logger.debug(message);
  }


  public static void logError(Logger logger, Throwable ex) {
    /**
     * Since it is a command line tools writing to console will be helpful to users.
     */
    ex.printStackTrace();
    logger.error(ex.getMessage(),ex);
  }

  public static void logError(Logger logger, String message, Throwable ex) {
    /**
     * Since it is a command line tools writing to console will be helpful to users.
     */
    System.out.println(message);
    ex.printStackTrace();
    logger.error(message,ex);
  }
}

