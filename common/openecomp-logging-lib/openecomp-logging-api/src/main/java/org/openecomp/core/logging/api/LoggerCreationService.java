package org.openecomp.core.logging.api;

/**
 * Implements a framework-specific logging, to be used by {@link LoggerFactory}.
 */
public interface LoggerCreationService {

  Logger getLogger(String className);

  Logger getLogger(Class<?> clazz);
}
