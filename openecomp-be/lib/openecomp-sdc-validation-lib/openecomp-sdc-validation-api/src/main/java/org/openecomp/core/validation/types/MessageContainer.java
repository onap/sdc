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

package org.openecomp.core.validation.types;

import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class MessageContainer {

  private List<ErrorMessage> errorMessageList = new ArrayList<>();

  public List<ErrorMessage> getErrorMessageList() {
    return errorMessageList;
  }

  public MessageBuilder getMessageBuilder() {
    return new MessageBuilder();
  }

  /**
   * Gets error message list by level.
   *
   * @param level the level
   * @return the error message list by level
   */
  public List<ErrorMessage> getErrorMessageListByLevel(ErrorLevel level) {

    List<ErrorMessage> errors = new ArrayList<>();
    errorMessageList.stream().filter(new Predicate<ErrorMessage>() {
      @Override
      public boolean test(ErrorMessage errorMessage) {
        return errorMessage.getLevel().equals(level);
      }
    }).forEach(errorMessage -> errors.add(errorMessage));
    return errors;
  }

  public class MessageBuilder {

    String message;
    ErrorLevel level;

    MessageBuilder setMessage(String message) {
      this.message = message;
      return this;
    }

    MessageBuilder setLevel(ErrorLevel level) {
      this.level = level;
      return this;
    }

    void create() {
      ErrorMessage errorMessage = new ErrorMessage(level, message);
      if (!errorMessageList.contains(errorMessage)) {
        errorMessageList.add(errorMessage);
      }
    }
  }
}
