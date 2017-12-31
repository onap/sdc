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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

/**
 * @author EVITALIY
 * @since 31 Dec 17
 */
public class SubscribersEntityTest {

    private static final Set<String> SUBSCRIBERS;

    static {
        Set<String> subs = new HashSet<>(2);
        subs.add(UUID.randomUUID().toString());
        subs.add(UUID.randomUUID().toString());
        SUBSCRIBERS = Collections.unmodifiableSet(subs);
    }

    @Test
    public void testUninitializedEquals() {
        assertEquals(new SubscribersEntity(), new SubscribersEntity());
    }

    @Test
    public void testEquals() {
        String entity = UUID.randomUUID().toString();
        assertEquals(new SubscribersEntity(entity, SUBSCRIBERS), new SubscribersEntity(entity, SUBSCRIBERS));
    }

    @Test
    public void testEntityNotEquals() {
        assertNotEquals(new SubscribersEntity(UUID.randomUUID().toString(), SUBSCRIBERS),
                new SubscribersEntity(UUID.randomUUID().toString(), SUBSCRIBERS));
    }

    @Test
    public void testSubscribersNotEquals() {
        String entity = UUID.randomUUID().toString();
        assertNotEquals(new SubscribersEntity(entity, SUBSCRIBERS),
                // not using Collections.emptySet() to use the the same implementation class
                new SubscribersEntity(entity, Collections.unmodifiableSet(new HashSet<>(0))));
    }

    @Test
    public void testHashCode() {
        String entity = UUID.randomUUID().toString();
        assertEquals(new SubscribersEntity(entity, SUBSCRIBERS).hashCode(),
                new SubscribersEntity(entity, SUBSCRIBERS).hashCode());
    }
}