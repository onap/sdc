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

import java.util.List;

@SuppressWarnings("unused")
@ApiModel("Run a set of tests")
public class TestExecutionRequest {
  @ApiModelProperty("Tests to run with their parameters")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<TestExecutionRequestItem> tests;

  @ApiModelProperty("Endpoint at which tests should be run")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String endpoint;

  public List<TestExecutionRequestItem> getTests() {
    return tests;
  }

  public void setTests(List<TestExecutionRequestItem> tests) {
    this.tests = tests;
  }


  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }
}
