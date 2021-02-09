/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Samsung Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.user;

import static com.google.code.beanmatchers.BeanMatchers.isABeanWithValidGettersAndSettersExcluding;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class UserMessageTest {
    private static final UserOperationEnum USER_OPERATION = UserOperationEnum.CREATE;
    private  static final String USER_ID = "mock-user";
    private static final String ROLE = "mock-role";

    private UserMessage createUserMessage() {
        return new UserMessage(USER_OPERATION, USER_ID, ROLE);
    }

    @Test
    public void testCtor() {
        UserMessage userMessage = createUserMessage();
        assertThat(userMessage.getOperation(), is(USER_OPERATION));
        assertThat(userMessage.getUserId(), is(USER_ID));
        assertThat(userMessage.getRole(), is(ROLE));
    }

    @Test
    public void testGettersSetters() {
        UserMessage userMessage = createUserMessage();
        assertThat(userMessage, isABeanWithValidGettersAndSettersExcluding("messageType"));
    }

    @Test
    public void testGetMessageType() {
        UserMessage userMessage = createUserMessage();
        assertThat(userMessage.getMessageType(), is(UserMessage.class.getSimpleName()));
    }
}
