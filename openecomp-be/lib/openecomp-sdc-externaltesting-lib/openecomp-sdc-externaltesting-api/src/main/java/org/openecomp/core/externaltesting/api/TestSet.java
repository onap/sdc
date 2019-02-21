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
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("A Set of Tests")
public class TestSet {

  @ApiModelProperty(value="Unique ID for test set.", example = "set1")
  private String id;

  @ApiModelProperty("Title for user for test set.")
  private String title;

  @ApiModelProperty(value = "Children test sets")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<TestSet> sets;

  @ApiModelProperty("Tests in this set.")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Test> tests;

  @ApiModelProperty("Endpoint at which the test set is defined")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String endpointId;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<TestSet> getSets() {
    return sets;
  }

  public void setSets(List<TestSet> sets) {
    this.sets = sets;
  }

  public List<Test> getTests() {
    return tests;
  }

  public void setTests(List<Test> tests) {
    this.tests = tests;
  }

  public String getEndpointId() {
    return endpointId;
  }

  public void setEndpointId(String endpointId) {
    this.endpointId = endpointId;
  }
}
