/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.notification.dao.types;

import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

/**
 * @author EVITALIY
 * @since 31 Dec 17
 */
public class NotificationEntityTest {

    @Test
    public void testUninitializedEquals() {
        assertEquals(new NotificationEntity(), new NotificationEntity());
    }

    @Test
    public void testEquals() {
        UUID random = UUID.randomUUID();
        assertEquals(createNotificationEntity(random), createNotificationEntity(random));
    }

    @Test
    public void testOwnerNotEquals() {
        UUID random = UUID.randomUUID();
        NotificationEntity mutant = createNotificationEntity(random);
        mutant.setOwnerId(UUID.randomUUID().toString());
        assertNotEquals(mutant, createNotificationEntity(random));
    }

    @Test
    public void testEventIdNotEquals() {
        UUID random = UUID.randomUUID();
        NotificationEntity mutant = createNotificationEntity(random);
        mutant.setEventId(UUID.randomUUID());
        assertNotEquals(mutant, createNotificationEntity(random));
    }

    @Test
    public void testEventTypeNotEquals() {
        UUID random = UUID.randomUUID();
        NotificationEntity mutant = createNotificationEntity(random);
        mutant.setEventType(UUID.randomUUID().toString());
        assertNotEquals(mutant, createNotificationEntity(random));
    }

    @Test
    public void testOriginatorNotEquals() {
        UUID random = UUID.randomUUID();
        NotificationEntity mutant = createNotificationEntity(random);
        mutant.setOriginatorId(UUID.randomUUID().toString());
        assertNotEquals(mutant, createNotificationEntity(random));
    }

    @Test
    public void testReadNotEquals() {
        UUID random = UUID.randomUUID();
        NotificationEntity mutant = createNotificationEntity(random);
        mutant.setRead(false);
        assertNotEquals(mutant, createNotificationEntity(random));
    }

    @Test
    public void testAttributesNotEquals() {
        UUID random = UUID.randomUUID();
        NotificationEntity mutant = createNotificationEntity(random);
        mutant.setEventAttributes(UUID.randomUUID().toString());
        assertNotEquals(mutant, createNotificationEntity(random));
    }

    @Test
    public void testHashCode() {
        UUID random = UUID.randomUUID();
        assertEquals(createNotificationEntity(random).hashCode(), createNotificationEntity(random).hashCode());
    }

    private NotificationEntity createNotificationEntity(UUID random) {
        return new NotificationEntity("owner-" + random, random, "type-" + random,
                "originator-" + random, true, "attributes-" + random);
    }
}