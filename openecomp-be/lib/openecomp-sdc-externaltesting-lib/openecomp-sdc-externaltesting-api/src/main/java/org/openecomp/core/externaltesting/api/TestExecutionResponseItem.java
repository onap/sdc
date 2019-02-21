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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@SuppressWarnings("unused")
@ApiModel
public class TestExecutionResponseItem {

  @ApiModelProperty("Test that was executed")
  private Test test;

  @ApiModelProperty(value = "Status of test", dataType="string", allowableValues = "Success, Failure")
  private TestExecutionStatus status;

  @ApiModelProperty("Detailed results from the test invocation")
  private String details;


  public Test getTest() {
    return test;
  }

  public void setTest(Test test) {
    this.test = test;
  }

  public TestExecutionStatus getStatus() {
    return status;
  }

  public void setStatus(TestExecutionStatus status) {
    this.status = status;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }
}
