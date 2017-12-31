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
public class LastSeenNotificationEntityTest {

    @Test
    public void testEquals() {
        UUID uuid = UUID.randomUUID();
        String owner = "owner-" + uuid.toString();
        assertEquals(new LastSeenNotificationEntity(owner, uuid), new LastSeenNotificationEntity(owner, uuid));
    }

    @Test
    public void testUninitializedEquals() {
        assertEquals(new LastSeenNotificationEntity(), new LastSeenNotificationEntity());
    }

    @Test
    public void testUuidNotEqual() {
        String owner = "owner";
        assertNotEquals(new LastSeenNotificationEntity(owner, UUID.randomUUID()),
                new LastSeenNotificationEntity(owner, UUID.randomUUID()));
    }

    @Test
    public void testOwnerNotEqual() {
        UUID uuid = UUID.randomUUID();
        assertNotEquals(new LastSeenNotificationEntity(UUID.randomUUID().toString(), uuid),
                new LastSeenNotificationEntity(UUID.randomUUID().toString(), uuid));
    }

    @Test
    public void testHashCode() {
        UUID uuid = UUID.randomUUID();
        String owner = uuid.toString();
        assertEquals(
                new LastSeenNotificationEntity(owner, uuid).hashCode(),
                new LastSeenNotificationEntity(owner, uuid).hashCode());
    }
}