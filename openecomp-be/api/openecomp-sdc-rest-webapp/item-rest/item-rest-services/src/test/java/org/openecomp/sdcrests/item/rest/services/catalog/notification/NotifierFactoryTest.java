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

package org.openecomp.sdcrests.item.rest.services.catalog.notification;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openecomp.sdcrests.item.types.ItemAction;

/**
 * @author evitaliy
 * @since 26 Nov 2018
 */
public class NotifierFactoryTest {

    private static final String CONFIG_LOCATION_PROPERTY = "configuration.yaml";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void clearConfigLocation() {
        System.clearProperty(CONFIG_LOCATION_PROPERTY);
    }

    @Test
    public void notifierFactoryReturnsAnInstance() {
        assertNotNull(NotifierFactory.getInstance());
    }

    @Test
    public void unsupportedConfigurationNotifierWhenConfigurationLocationNotGiven() {
        assertTrue(NotifierFactory.createInstance() instanceof NotifierFactory.UnsupportedConfigurationNotifier);
    }

    @Test
    public void asyncNotifierReturnedWhenConfigurationCorrect() throws FileNotFoundException {
        String configPath = getConfigPath("catalog-notification-config-correct.yaml");
        System.setProperty(CONFIG_LOCATION_PROPERTY, configPath);
        assertTrue("Configuration file must be present and correct",
                NotifierFactory.createInstance() instanceof AsyncNotifier);
    }

    private String getConfigPath(String classpathFile) throws FileNotFoundException {

        URL resource = Thread.currentThread().getContextClassLoader().getResource(classpathFile);
        if (resource == null) {
            throw new FileNotFoundException("Cannot find resource: " + classpathFile);
        }

        return resource.getPath();
    }

    @Test
    public void unsupportedConfigurationNotifierReturnedWhenConfigurationEmpty() throws FileNotFoundException {
        String configPath = getConfigPath("catalog-notification-config-empty.yaml");
        System.setProperty(CONFIG_LOCATION_PROPERTY, configPath);
        assertTrue(NotifierFactory.createInstance() instanceof NotifierFactory.UnsupportedConfigurationNotifier);
    }

    @Test
    public void unsupportedConfigurationNotifierReturnedWhenConfigurationDoesNotHaveNotificationSection()
            throws FileNotFoundException {
        String configPath = getConfigPath("catalog-notification-config-without-notification-section.yaml");
        System.setProperty(CONFIG_LOCATION_PROPERTY, configPath);
        assertTrue(NotifierFactory.createInstance() instanceof NotifierFactory.UnsupportedConfigurationNotifier);
    }

    @Test
    public void unsupportedConfigurationNotifierThrowsException() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage(startsWith("Cannot send notifications"));
        Set<String> itemIds = Collections.singleton(UUID.randomUUID().toString());
        new NotifierFactory.UnsupportedConfigurationNotifier().execute(itemIds, ItemAction.ARCHIVE);
    }

}
