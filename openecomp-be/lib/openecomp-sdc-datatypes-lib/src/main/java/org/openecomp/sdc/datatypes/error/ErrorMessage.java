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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

@Getter
@EqualsAndHashCode
@ToString
public class ErrorMessage {
  private final ErrorLevel level;
  private final String message;

  public ErrorMessage(ErrorLevel level, String message) {
    this.level = level;
    this.message = message;
  }

  public static class ErrorMessageUtil {

    private ErrorMessageUtil() {
    }

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
