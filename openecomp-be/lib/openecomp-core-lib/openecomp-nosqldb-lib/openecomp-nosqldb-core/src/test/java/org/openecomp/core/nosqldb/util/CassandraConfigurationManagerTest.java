/*
 * Copyright © 2016-2018 European Support Limited
 * ================================================================================
 * Modifications Copyright (C) 2021 AT&T.
 * ================================================================================
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

import java.io.FileNotFoundException;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.ExpectedException;

/**
 * @author EVITALIY
 * @since 22 Oct 17
 */
public class CassandraConfigurationManagerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String NON_EXISTENT = "unexistentfile";

    @AfterEach
    public void resetSystemVar() {
        System.clearProperty(CassandraConfigurationManager.JVM_PARAM_CONFIGURATION_FILE);
    }

    @Test
    public void testGetInstanceSystemProperty() throws Throwable {
        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(IsInstanceOf.instanceOf(FileNotFoundException.class));
        System.setProperty(CassandraConfigurationManager.JVM_PARAM_CONFIGURATION_FILE, NON_EXISTENT);
        CassandraConfigurationManager.getInstance();
    }

    @Test()
    public void testGetInstanceDefault() {
        // Do not set the JVM param for config file, by default code will get it from Resource
        CassandraConfigurationManager manager = CassandraConfigurationManager.getInstance();
        Assertions.assertArrayEquals(new String[] {"127.0.0.1"}, manager.getAddresses());
        Assertions.assertEquals(9042, manager.getCassandraPort());
        Assertions.assertEquals("dox", manager.getKeySpace());
        Assertions.assertEquals("LOCAL_QUORUM", manager.getConsistencyLevel());
        Assertions.assertEquals(null, manager.getLocalDataCenter());
        Assertions.assertEquals("Aa1234%^!", manager.getPassword());
        Assertions.assertEquals(30000L, manager.getReconnectTimeout().longValue());
        Assertions.assertEquals("/path/path", manager.getTruststorePath());
        Assertions.assertEquals("Aa123456", manager.getTruststorePassword());
        Assertions.assertFalse(manager.isSsl());
        Assertions.assertFalse(manager.isAuthenticate());
    }
}
