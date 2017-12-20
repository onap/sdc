package org.openecomp.sdc.logging.api.context;

import org.testng.annotations.Test;

import java.util.ServiceLoader;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author evitaliy
 * @since 14/09/2016.
 */
public class TaskFactoryTest {

  @Test(expectedExceptions = RuntimeException.class)
  public void testNoImplementation() throws Exception {

    assertFalse(ServiceLoader.load(ContextPropagationService.class).iterator().hasNext());

    try {
      TaskFactory.create(() -> {
      });
    } catch (RuntimeException e) {
      Throwable cause = e.getCause();
      assertNotNull(cause);
      assertTrue(cause.getMessage().contains(ContextPropagationService.class.getName()));
      throw e;
    }
  }
}