package org.openecomp.core.logging.api.context;

/**
 * Should be used to implement a framework-specific mechanism of propagation of a diagnostic context
 * to child threads.
 */
public interface ContextPropagationService {

  Runnable create(Runnable task);
}
