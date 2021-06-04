/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.core.nosqldb.util;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertNotNull;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author EVITALIY
 * @since 22 Oct 17
 */
public class ConfigurationManagerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String NON_EXISTENT = "unexistentfile";

    @Before
    public void resetInstance() throws NoSuchFieldException, IllegalAccessException {
        Field field = ConfigurationManager.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test
    public void testGetInstanceSystemProperty() throws Throwable {

        expectedException.expect(IOException.class);
        expectedException.expectMessage(containsString(NON_EXISTENT));

        try (ConfigurationSystemPropertyUpdater updater = new ConfigurationSystemPropertyUpdater(NON_EXISTENT)) {
            ConfigurationManager.getInstance();
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            throw cause == null ? e : cause;
        }
    }

    @Test()
    public void testGetInstanceDefault() {

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
        public void close() {

            if (oldValue == null) {
                System.clearProperty(ConfigurationManager.CONFIGURATION_YAML_FILE);
            } else {
                System.setProperty(ConfigurationManager.CONFIGURATION_YAML_FILE, oldValue);
            }
        }
    }
}
