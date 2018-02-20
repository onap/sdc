/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.activityspec.api.rest.types;

import java.util.List;

public class ActivitySpecGetResponse  {
  private String name;
  private String description;
  private List<String> categoryList;
  private List<ActivitySpecParameterDto> inputParameters;
  private List<ActivitySpecParameterDto> outputParameters;
  private String status;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getCategoryList() {
    return categoryList;
  }

  public void setCategoryList(List<String> categoryList) {
    this.categoryList = categoryList;
  }

  public List<ActivitySpecParameterDto> getInputParameters() {
    return inputParameters;
  }

  public void setInputParameters(List<ActivitySpecParameterDto> inputParameters) {
    this.inputParameters = inputParameters;
  }

  public List<ActivitySpecParameterDto> getOutputParameters() {
    return outputParameters;
  }

  public void setOutputParameters(List<ActivitySpecParameterDto> outputParameters) {
    this.outputParameters = outputParameters;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
