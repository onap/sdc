/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.core.validation.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;

public class MessageContainerUtil {

    /**
     * Gets message by level.
     *
     * @param level    the level
     * @param messages the messages
     * @return the message by level
     */
    public static Map<String, List<ErrorMessage>> getMessageByLevel(ErrorLevel level, Map<String, List<ErrorMessage>> messages) {
        if (messages == null) {
            return null;
        }
        Map<String, List<ErrorMessage>> filteredMessages = new HashMap<>();
        messages.entrySet().forEach(entry -> entry.getValue().stream().filter(message -> message.getLevel().equals(level))
            .forEach(message -> addMessage(entry.getKey(), message, filteredMessages)));
        return filteredMessages;
    }

    private static void addMessage(String fileName, ErrorMessage message, Map<String, List<ErrorMessage>> messages) {
        List<ErrorMessage> messageList = messages.computeIfAbsent(fileName, k -> new ArrayList<>());
        messageList.add(message);
    }

    public static String getErrorMessagesListAsString(Map<String, List<ErrorMessage>> messages) {
        StringBuilder concatErrorMessage = new StringBuilder();
        for (Map.Entry<String, List<ErrorMessage>> errorMessageEntry : messages.entrySet()) {
            appendErrorMessageAsString(concatErrorMessage, errorMessageEntry.getKey(), errorMessageEntry.getValue());
        }
        return concatErrorMessage.toString();
    }

    private static void appendErrorMessageAsString(StringBuilder concatErrorMessage, String fileName, List<ErrorMessage> errorMessageList) {
        for (ErrorMessage errorMessage : errorMessageList) {
            addErrorMessage(concatErrorMessage, fileName, errorMessage);
        }
    }

    private static void addErrorMessage(StringBuilder concatErrorMessage, String fileName, ErrorMessage errorMessage) {
        concatErrorMessage.append(fileName).append(" : ");
        concatErrorMessage.append(errorMessage.getMessage());
        concatErrorMessage.append("\n");
    }
}
