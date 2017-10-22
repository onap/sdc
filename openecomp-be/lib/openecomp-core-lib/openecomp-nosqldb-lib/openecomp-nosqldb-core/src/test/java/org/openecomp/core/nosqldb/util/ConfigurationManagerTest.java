package org.openecomp.core.nosqldb.util;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.testng.Assert.*;

/**
 * @author EVITALIY
 * @since 22 Oct 17
 */
public class ConfigurationManagerTest {

    private static final String NON_EXISTENT = "unexistentfile";

    @BeforeMethod
    public void resetInstance() throws NoSuchFieldException, IllegalAccessException {
        Field field = ConfigurationManager.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test(expectedExceptions = IOException.class,
            expectedExceptionsMessageRegExp = ".*" + NON_EXISTENT + ".*file specified.*")
    public void testGetInstanceSystemProperty() throws Throwable {

        try (ConfigurationSystemPropertyUpdater updater = new ConfigurationSystemPropertyUpdater(NON_EXISTENT)) {
            ConfigurationManager.getInstance();
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            throw cause == null ? e : cause;
        }
    }

    @Test()
    public void testGetInstanceDefault() throws Exception {

        try (ConfigurationSystemPropertyUpdater property = new ConfigurationSystemPropertyUpdater()) {
            ConfigurationManager manager = ConfigurationManager.getInstance();
            assertNotNull(manager.getUsername());
        }
    }


    private static class ConfigurationSystemPropertyUpdater implements Closeable {

        private final String oldValue;

        private ConfigurationSystemPropertyUpdater(String value) {
            this.oldValue = System.getProperty(ConfigurationManager.CONFIGURATION_YAML_FILE);
            System.setProperty(ConfigurationManager.CONFIGURATION_YAML_FILE, value);
        }

        private ConfigurationSystemPropertyUpdater() {
            this.oldValue = System.getProperty(ConfigurationManager.CONFIGURATION_YAML_FILE);
            System.clearProperty(ConfigurationManager.CONFIGURATION_YAML_FILE);
        }

        @Override
        public void close() throws IOException {

            if (oldValue == null) {
                System.clearProperty(ConfigurationManager.CONFIGURATION_YAML_FILE);
            } else {
                System.setProperty(ConfigurationManager.CONFIGURATION_YAML_FILE, oldValue);
            }
        }
    }
}