package org.openecomp.sdc.logging;

import org.openecomp.sdc.logging.api.LoggerFactory;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import static org.testng.Assert.assertEquals;

/**
 * @author evitaliy
 * @since 12/09/2016.
 */
public class LoggerFactoryTest {

  @Test
  public void testCreate() throws Exception {
    // test that the service loader loads the right implementation
    LoggerFactory.getLogger(LoggerFactoryTest.class);
    Field factory = LoggerFactory.class.getDeclaredField("SERVICE");
    factory.setAccessible(true);
    Object implementation = factory.get(null);
    assertEquals(SLF4JLoggerCreationService.class, implementation.getClass());
  }
}