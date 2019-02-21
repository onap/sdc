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

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("Response to a test execution")
@Data
public class TestExecutionResponse {

  @ApiModelProperty("Unique ID for this test run")
  private String id;

  @ApiModelProperty("Status of this test run")
  private String status;

  @ApiModelProperty("Results for the individual tests that were run")
  private List<TestExecutionResponseItem> results;

  @ApiModelProperty("Total number of tests to be run")
  private int total;

  @ApiModelProperty("Number of failures")
  private int failures;

  @ApiModelProperty("Number of successes")
  private int successes;

  @ApiModelProperty(hidden = true, value="Who initiated the test run")
  private String initiator;

  @ApiModelProperty("Start date/time of the run")
  private Date startDateTime;

  @ApiModelProperty("Completion date/time of the run")
  private Date completionDateTime;

}
