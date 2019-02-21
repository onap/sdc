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

@SuppressWarnings({"unused", "WeakerAccess"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("Test to run")
public class TestExecutionRequestItem {

  @ApiModelProperty("Unique id (see list response) of test to perform")
  private String testId;

  @ApiModelProperty("parameters for the test")
  private List<TestParameterValue> parameterValues;

  @ApiModelProperty(hidden = true)
  private List<CsarMetadataContentItem> contentItems;

  public String getTestId() {
    return testId;
  }

  public void setTestId(String testId) {
    this.testId = testId;
  }

  public List<TestParameterValue> getParameterValues() {
    return parameterValues;
  }

  public void setParameterValues(List<TestParameterValue> parameterValues) {
    this.parameterValues = parameterValues;
  }


  public List<CsarMetadataContentItem> getContentItems() {
    return contentItems;
  }

  public void setContentItems(List<CsarMetadataContentItem> contentItems) {
    this.contentItems = contentItems;
  }
}