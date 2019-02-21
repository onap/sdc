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

import java.util.List;

/**
 * Parameter for test that may require user input.
 */
@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("Test Parameter Definition")
@Data
class TestParameter {

  @ApiModelProperty("Internal ID for this parameter.  This is the key to use when invoking the test.")
  private String id;

  @ApiModelProperty(value="Label to display to user for this field", example="Some Label")
  private String label;

  @ApiModelProperty(dataType = "string", allowableValues =  "text, numeric, boolean, date, datetime, select, multi-select, hidden")
  private String inputType;

  @ApiModelProperty(value="Optional max length for UI field", example="3")
  private Integer maxLength;

  @ApiModelProperty(value="Optional min length for UI field", example="1")
  private Integer minLength;

  @ApiModelProperty(value="Optional min value for numeric UI field", example="1")
  private Integer min;

  @ApiModelProperty(value="Optional max value for numeric UI field", example="100")
  private Integer max;

  @ApiModelProperty("Optional placeholder text for UI field - appears in grey when no value entered by user")
  private String placeholder;

  @ApiModelProperty(value="Default value to place in UI field when displayed in a UI", example="B")
  private String defaultValue;

  @ApiModelProperty("If true, present the field in the UI as disabled")
  private boolean disabled;

  @ApiModelProperty(value = "For select and multi-select properties, this is the list of options to display", example = "B,N,H")
  private List<TestParameterChoice> choices;

  @ApiModelProperty("If true, user or API must provide a value for this parameter")
  private boolean required;

}
  
