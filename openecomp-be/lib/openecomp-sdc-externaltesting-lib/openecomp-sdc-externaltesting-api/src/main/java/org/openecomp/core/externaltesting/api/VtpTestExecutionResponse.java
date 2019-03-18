/*
 * Copyright © 2019 iconectiv
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

package org.openecomp.core.externaltesting.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.Map;

@Data
public class VtpTestExecutionResponse {
  private String scenario;
  private String testCaseName;
  private String testSuiteName;
  private String executionId;
  private Map<String,String> parameters;
  private Object results;
  private String status;
  private String startTime; // don't bother to convert various ISO8601 formats.
  private String endTime;   // don't bother to convert various ISO8601 formats.

  /**
   * In the event on an error, code provided.
   */
  @JsonInclude(value= JsonInclude.Include.NON_NULL)
  private String code;

  /**
   * Error message
   */
  @JsonInclude(value= JsonInclude.Include.NON_NULL)
  private String message;

  /**
   * In the event of an unexpected status.
   */
  @JsonInclude(value= JsonInclude.Include.NON_NULL)
  private Integer httpStatus;
}
