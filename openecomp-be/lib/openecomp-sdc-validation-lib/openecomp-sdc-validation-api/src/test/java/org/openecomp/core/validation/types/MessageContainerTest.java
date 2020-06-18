/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.core.validation.types;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.datatypes.error.ErrorLevel;

public class MessageContainerTest {

    final static MessageContainer container = new MessageContainer();
    final static String mess1 = "Message 1";
    final static String mess2 = "Message 2";
    final static String mess3 = "Third Message";
    final static String mess4 = "That's mess 4";

    @BeforeAll
    public static void setup() {
        container.getMessageBuilder()
                .setMessage(mess1)
                .setLevel(ErrorLevel.ERROR).create();
        container.getMessageBuilder()
                .setMessage(mess2)
                .setLevel(ErrorLevel.INFO).create();
        container.getMessageBuilder()
                .setMessage(mess3)
                .setLevel(ErrorLevel.INFO).create();
        container.getMessageBuilder()
                .setMessage(mess4)
                .setLevel(ErrorLevel.WARNING).create();
    }

    @Test
    public void getErrorMessageListTest() {
        assertEquals(4, container.getErrorMessageList().size());
        assertEquals(mess1, container.getErrorMessageList().get(0).getMessage());
        assertEquals(mess2, container.getErrorMessageList().get(1).getMessage());
        assertEquals(ErrorLevel.ERROR, container.getErrorMessageList().get(0).getLevel());
        assertEquals(ErrorLevel.INFO, container.getErrorMessageList().get(1).getLevel());
    }
    @Test
    public void getErrorMessageListByLevelTest() {
        assertEquals(1, container.getErrorMessageListByLevel(ErrorLevel.WARNING).size());
        assertEquals(2, container.getErrorMessageListByLevel(ErrorLevel.INFO).size());
        assertEquals(1, container.getErrorMessageListByLevel(ErrorLevel.ERROR).size());
        assertEquals(mess1, container.getErrorMessageListByLevel(ErrorLevel.ERROR).get(0).getMessage());
        assertEquals(ErrorLevel.ERROR, container.getErrorMessageListByLevel(ErrorLevel.ERROR).get(0).getLevel());
    }
}
