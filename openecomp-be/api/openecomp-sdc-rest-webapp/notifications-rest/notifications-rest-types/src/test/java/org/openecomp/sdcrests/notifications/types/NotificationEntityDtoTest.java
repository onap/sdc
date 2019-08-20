/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdcrests.notifications.types;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationEntityDtoTest {
    @Test
    void testBean() {
        assertThat(NotificationEntityDto.class,  allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters()
        ));
    }
    @Test
    void validateArgConstructors() {

        final boolean read = false;
        final UUID eventId = new UUID(10,20);
        final String eventType = "testType";
        final Map<String, Object> eventAttributes = Collections.singletonMap("testKey","testValue");
        NotificationEntityDto notificationEntityDto = new NotificationEntityDto(read,eventId,eventType,eventAttributes);
        assertEquals(notificationEntityDto.isRead(), read);
        assertEquals(notificationEntityDto.getEventId(), eventId);
        assertEquals(notificationEntityDto.getEventType(), eventType);
        assertEquals(notificationEntityDto.getEventAttributes(), eventAttributes);

        final String dataTime = "10-10-2019";
        notificationEntityDto = new NotificationEntityDto(read, eventId, eventType, eventAttributes, dataTime);
        assertEquals(notificationEntityDto.isRead(), read);
        assertEquals(notificationEntityDto.getEventId(), eventId);
        assertEquals(notificationEntityDto.getEventType(), eventType);
        assertEquals(notificationEntityDto.getEventAttributes(), eventAttributes);
        assertEquals(notificationEntityDto.getDateTime(), dataTime);
    }
}
