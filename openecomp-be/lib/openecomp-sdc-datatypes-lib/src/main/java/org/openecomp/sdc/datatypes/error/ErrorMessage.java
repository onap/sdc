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

package org.openecomp.sdc.datatypes.error;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ErrorMessage {
  private final ErrorLevel level;
  private final String message;

  public ErrorMessage(ErrorLevel level, String message) {
    this.level = level;
    this.message = message;
  }

  public ErrorLevel getLevel() {
    return level;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public int hashCode() {
    int result = level.hashCode();
    result = 31 * result + message.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    ErrorMessage that = (ErrorMessage) object;

    return level == that.level && message.equals(that.message);

  }

  public static class ErrorMessageUtil {

    /**
     * Add message list.
     *
     * @param fileName the file name
     * @param errorMap the error map
     * @return the list
     */
    public static List<ErrorMessage> addMessage(String fileName,
                                                Map<String, List<ErrorMessage>> errorMap) {
      List<ErrorMessage> fileErrorList;
      fileErrorList = errorMap.get(fileName);
      if (CollectionUtils.isEmpty(fileErrorList)) {
        fileErrorList = new ArrayList<>();
        errorMap.put(fileName, fileErrorList);
      }

      return fileErrorList;
    }
  }


}
