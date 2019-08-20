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
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UpdateNotificationResponseStatusTest {
    @Test
    void testBean() {
        assertThat(UpdateNotificationResponseStatus.class,  allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters()
        ));
    }
    @Test
    void validateAddStructureErrorCorrectlyAddsOneError() {
        UpdateNotificationResponseStatus updateNotificationResponseStatus = new UpdateNotificationResponseStatus();

        assertTrue(updateNotificationResponseStatus.getErrors().isEmpty());
        final String notificationId = "testId";
        final ErrorMessage testError = new ErrorMessage(ErrorLevel.ERROR,"test Error");
        updateNotificationResponseStatus.addStructureError(notificationId, testError);

        assertFalse(updateNotificationResponseStatus.getErrors().isEmpty());
        assertEquals(updateNotificationResponseStatus.getErrors().size(),1);
        assertEquals(updateNotificationResponseStatus.getErrors().get(notificationId).size(),1);
        assertEquals(updateNotificationResponseStatus.getErrors().get(notificationId).get(0),testError);
    }
    @Test
    void validateAddStructureErrorCorrectlyAddsTwoErrorsToOneKey() {
        UpdateNotificationResponseStatus updateNotificationResponseStatus = new UpdateNotificationResponseStatus();

        assertTrue(updateNotificationResponseStatus.getErrors().isEmpty());
        final String notificationId = "testId";
        final ErrorMessage testError01 = new ErrorMessage(ErrorLevel.ERROR,"test Error01");
        final ErrorMessage testError02 = new ErrorMessage(ErrorLevel.ERROR,"test Error02");
        final List<ErrorMessage > testErrorsList = new ArrayList<>();
        Collections.addAll(testErrorsList,testError01,testError02);
        updateNotificationResponseStatus.addStructureError(notificationId, testError01);
        updateNotificationResponseStatus.addStructureError(notificationId, testError02);

        assertFalse(updateNotificationResponseStatus.getErrors().isEmpty());
        assertEquals(updateNotificationResponseStatus.getErrors().size(),1);
        assertEquals(updateNotificationResponseStatus.getErrors().get(notificationId).size(),2);
        assertArrayEquals(updateNotificationResponseStatus.getErrors().get(notificationId).toArray(), testErrorsList.toArray());
    }
    @Test
    void validateAddStructureErrorCorrectlyAddsTwoErrorsToTwoKeys() {
        UpdateNotificationResponseStatus updateNotificationResponseStatus = new UpdateNotificationResponseStatus();

        assertTrue(updateNotificationResponseStatus.getErrors().isEmpty());
        final String notificationId01 = "testId01";
        final ErrorMessage testError01 = new ErrorMessage(ErrorLevel.ERROR,"test Error01");
        final String notificationId02 = "testId02";
        final ErrorMessage testError02 = new ErrorMessage(ErrorLevel.ERROR,"test Error02");
        updateNotificationResponseStatus.addStructureError(notificationId01, testError01);
        updateNotificationResponseStatus.addStructureError(notificationId02, testError02);

        assertFalse(updateNotificationResponseStatus.getErrors().isEmpty());
        assertEquals(updateNotificationResponseStatus.getErrors().size(),2);
        assertEquals(updateNotificationResponseStatus.getErrors().get(notificationId01).size(),1);
        assertEquals(updateNotificationResponseStatus.getErrors().get(notificationId01).get(0), testError01);
        assertEquals(updateNotificationResponseStatus.getErrors().get(notificationId02).size(),1);
        assertEquals(updateNotificationResponseStatus.getErrors().get(notificationId02).get(0), testError02);
    }
}
