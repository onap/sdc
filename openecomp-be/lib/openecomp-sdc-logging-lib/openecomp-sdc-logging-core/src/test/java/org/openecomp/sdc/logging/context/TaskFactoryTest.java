package org.openecomp.sdc.logging.context;

import org.openecomp.sdc.logging.api.context.TaskFactory;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import static org.testng.Assert.assertEquals;

/**
 * @author evitaliy
 * @since 12/09/2016.
 */
public class TaskFactoryTest {

  @Test
  public void testCreate() throws Exception {
    // test that the service loader loads the right implementation
    TaskFactory.create(() -> {
    });
    Field factory = TaskFactory.class.getDeclaredField("SERVICE");
    factory.setAccessible(true);
    Object implementation = factory.get(null);
    assertEquals(MDCPropagationService.class, implementation.getClass());
  }
}